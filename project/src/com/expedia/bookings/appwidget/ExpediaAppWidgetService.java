package com.expedia.bookings.appwidget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.ReadablePeriod;
import org.joda.time.Seconds;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import com.expedia.bookings.R;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.HotelSearchResponse;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Distance.DistanceUnit;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.StrUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;

public class ExpediaAppWidgetService extends Service implements ConnectionCallbacks, OnConnectionFailedListener,
		LocationListener {

	//////////////////////////////////////////////////////////////////////////
	// Constants

	public static final String TAG = "ExpediaWidget";

	private static final String WIDGET_KEY_SEARCH = "WIDGET_KEY_SEARCH";

	// The maximum # of properties to show in the widget
	private static final int MAX_DEALS = 5;

	// How often we should update the widget, regardless of anything else
	private static final ReadablePeriod UPDATE_INTERVAL = Hours.ONE;

	// The distance one must be from their last search location before we want to update
	private static final int UPDATE_DISTANCE_METERS = 8000; // 8km

	// The minimum time between updates, regardless of when one is requested
	private static final ReadablePeriod MINIMUM_UPDATE_INTERVAL = Minutes.minutes(15);

	// If we try to refresh but fail for some reason, we keep doubling the backoff time
	private static final ReadablePeriod CONNECTION_ERROR_BACKOFF = Minutes.ONE;

	// The maximum backoff time
	private static final ReadablePeriod MAX_CONNECTION_ERROR_BACKOFF = Hours.ONE;

	// How long before rotating the shown hotel
	private static final ReadablePeriod ROTATE_INTERVAL = Seconds.seconds(5);

	// When the user interacts with the widget, how long we should delay until the next rotation
	private static final ReadablePeriod INTERACTION_DELAY = Seconds.seconds(30);

	// Actions used to change things in the service
	private static final String ACTION_PREVIOUS = "ACTION_PREVIOUS";
	private static final String ACTION_NEXT = "ACTION_NEXT";

	//////////////////////////////////////////////////////////////////////////
	// Data

	private LocationClient mLocationClient;

	private HotelSearchParams mSearchParams = new HotelSearchParams();
	private List<Property> mDeals = new ArrayList<Property>();
	private DateTime mLastUpateTimestamp;
	private boolean mLoadedDeals = false;

	// 0 == branding, 1-X == deals
	private int mCurrentPosition = 0;

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	public void onCreate() {
		super.onCreate();

		Log.d(TAG, "ExpediaAppWidgetService.onCreate()");

		mLocationClient = new LocationClient(this, this, this);
		mLocationClient.connect();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "ExpediaAppWidgetService.onStartCommand(" + intent + ", " + flags + ", " + startId + ")");

		String action = intent.getAction();
		if (!TextUtils.isEmpty(action)) {
			int posChange = 0;
			if (action.equals(ACTION_NEXT)) {
				posChange = 1;
			}
			else if (action.equals(ACTION_PREVIOUS)) {
				posChange = -1;
			}

			if (posChange != 0) {
				int numPositions = mDeals.size() + 1;
				mCurrentPosition = (mCurrentPosition + numPositions + posChange) % numPositions;
			}
		}

		// There might be a new widget or something; might as well update widgets
		updateWidgets();

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		Log.d(TAG, "ExpediaAppWidgetService.onDestroy()");

		if (mLocationClient.isConnected()) {
			mLocationClient.removeLocationUpdates(this);
		}
		mLocationClient.disconnect();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// Ignore; nothing ever binds to this Service
		return null;
	}

	//////////////////////////////////////////////////////////////////////////
	// Widget Views

	private void updateWidgets() {
		Log.v(TAG, "Updating widget views...");

		// Configure remote views
		RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.app_widget);

		if (mDeals.size() > 0) {
			remoteViews.setViewVisibility(R.id.loading_text_container, View.GONE);
			remoteViews.setViewVisibility(R.id.widget_contents_container, View.VISIBLE);

			if (mCurrentPosition == 0) {
				// Show branding
				remoteViews.setViewVisibility(R.id.branding_container, View.VISIBLE);
				remoteViews.setViewVisibility(R.id.hotels_container, View.GONE);
				remoteViews.setViewVisibility(R.id.widget_hang_tag, View.VISIBLE);
			}
			else {
				// Show a property
				remoteViews.setViewVisibility(R.id.branding_container, View.GONE);
				remoteViews.setViewVisibility(R.id.hotels_container, View.VISIBLE);
				remoteViews.setViewVisibility(R.id.widget_hang_tag, View.GONE);

				Property property = mDeals.get(mCurrentPosition - 1);

				remoteViews.setTextViewText(R.id.hotel_name_text_view, property.getName());

				String location = property.getDistanceFromUser().formatDistance(this,
						DistanceUnit.getDefaultDistanceUnit());
				remoteViews.setTextViewText(R.id.location_text_view, location);

				remoteViews.setTextViewText(R.id.price_text_view,
						StrUtils.formatHotelPrice(property.getLowestRate().getDisplayPrice()));

				if (property.getLowestRate().isOnSale()) {
					remoteViews.setViewVisibility(R.id.sale_text_view, View.VISIBLE);
					remoteViews.setTextViewText(R.id.sale_text_view,
							getString(R.string.widget_savings_template, property.getLowestRate().getDiscountPercent()));

					remoteViews.setTextColor(R.id.price_text_view,
							getResources().getColor(R.color.hotel_price_sale_text_color));
				}
				else {
					remoteViews.setViewVisibility(R.id.sale_text_view, View.GONE);

					remoteViews.setTextColor(R.id.price_text_view,
							getResources().getColor(R.color.hotel_price_text_color));
				}

				remoteViews.setImageViewResource(R.id.hotel_image_view, R.drawable.widget_thumbnail_background);
			}

			remoteViews.setOnClickPendingIntent(R.id.prev_hotel_btn, createPendingIntent(ACTION_PREVIOUS));
			remoteViews.setOnClickPendingIntent(R.id.next_hotel_btn, createPendingIntent(ACTION_NEXT));
		}
		else {
			// Show text
			remoteViews.setViewVisibility(R.id.loading_text_container, View.VISIBLE);
			remoteViews.setViewVisibility(R.id.widget_contents_container, View.GONE);
			remoteViews.setViewVisibility(R.id.widget_hang_tag, View.GONE);

			if (!mLoadedDeals) {
				remoteViews.setTextViewText(R.id.widget_text_view, getString(R.string.loading_hotels));
			}
			else {
				remoteViews.setTextViewText(R.id.widget_text_view, "ERROR CASE");
			}
		}

		// Update all widgets with the same content
		AppWidgetManager widgetManager = AppWidgetManager.getInstance(this);
		int[] widgets = widgetManager.getAppWidgetIds(new ComponentName(this, ExpediaAppWidgetProvider.class));
		widgetManager.updateAppWidget(widgets, remoteViews);
	}

	private PendingIntent createPendingIntent(String action) {
		Intent intent = new Intent(this, getClass());
		intent.setAction(action);
		return PendingIntent.getService(this, 0, intent, 0);
	}

	//////////////////////////////////////////////////////////////////////////
	// Search

	private void startNewSearch() {
		Log.i(TAG, "Widget is starting a new search...");

		// Clear old results
		mDeals.clear();
		mLoadedDeals = false;
		mCurrentPosition = 0;
		updateWidgets();

		// Start new search
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(WIDGET_KEY_SEARCH)) {
			Log.w(TAG, "Widget was already doing a search when a new one was started!");
			bd.cancelDownload(WIDGET_KEY_SEARCH);
		}
		bd.startDownload(WIDGET_KEY_SEARCH, mSearchDownload, mSearchCallback);
	}

	private Download<HotelSearchResponse> mSearchDownload = new Download<HotelSearchResponse>() {
		@Override
		public HotelSearchResponse doDownload() {
			ExpediaServices services = new ExpediaServices(getApplicationContext());
			BackgroundDownloader.getInstance().addDownloadListener(WIDGET_KEY_SEARCH, services);
			return services.search(mSearchParams, ExpediaServices.F_FROM_WIDGET);
		}
	};

	private OnDownloadComplete<HotelSearchResponse> mSearchCallback = new OnDownloadComplete<HotelSearchResponse>() {
		@Override
		public void onDownload(HotelSearchResponse results) {
			Log.i(TAG, "Widget received search response: " + results);

			mLoadedDeals = true;
			mDeals.addAll(getDeals(results));
			updateWidgets();
		}
	};

	// Filters through a full response to find the best deals
	private List<Property> getDeals(HotelSearchResponse response) {
		List<Property> deals = new ArrayList<Property>();

		if (response != null && !response.hasErrors() && response.getPropertiesCount() > 0) {
			List<Property> properties = new ArrayList<Property>(response.getProperties());

			// Sort by rating, so we only show the best
			Collections.sort(properties, Property.RATING_COMPARATOR);

			// Find hotels on sale first
			Iterator<Property> propertyIterator = properties.iterator();
			while (propertyIterator.hasNext() && deals.size() < MAX_DEALS) {
				Property property = propertyIterator.next();
				if (property.getLowestRate().isOnSale()) {
					deals.add(property);
					propertyIterator.remove();
				}
			}

			// Then add any highly-rated hotels
			propertyIterator = properties.iterator();
			while (propertyIterator.hasNext() && deals.size() < MAX_DEALS) {
				Property property = propertyIterator.next();
				if (property.isHighlyRated()) {
					deals.add(property);
					propertyIterator.remove();
				}
			}

			// Add the rest until we get to the max
			propertyIterator = properties.iterator();
			while (propertyIterator.hasNext() && deals.size() < MAX_DEALS) {
				Property property = propertyIterator.next();
				deals.add(property);
			}
		}

		return deals;
	}

	//////////////////////////////////////////////////////////////////////////
	// ConnectionCallbacks

	@Override
	public void onConnected(Bundle connectionHint) {
		Log.d(TAG, "ExpediaAppWidgetService.onConnected(" + connectionHint + ")");

		LocationRequest request = new LocationRequest();
		request.setPriority(LocationRequest.PRIORITY_LOW_POWER);
		request.setFastestInterval(MINIMUM_UPDATE_INTERVAL.toPeriod().toStandardDuration().getMillis());
		request.setInterval(request.getFastestInterval());
		request.setSmallestDisplacement(UPDATE_DISTANCE_METERS);

		mLocationClient.requestLocationUpdates(request, this);
	}

	@Override
	public void onDisconnected() {
		Log.d(TAG, "ExpediaAppWidgetService.onDisconnected()");
	}

	//////////////////////////////////////////////////////////////////////////
	// OnConnectionFailedListener

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.e(TAG, "ExpediaAppWidgetService.onConnectionFailed(" + result + ")");
	}

	//////////////////////////////////////////////////////////////////////////
	// LocationListener

	@Override
	public void onLocationChanged(Location location) {
		Log.d(TAG, "ExpediaAppWidgetService.onLocationChanged(" + location + ")");

		// Due to the way we setup LocationRequest, this should only be called
		// when we actually want to do a new search (due to the user moving).
		mSearchParams.setSearchLatLon(location.getLatitude(), location.getLongitude());
		startNewSearch();
	}

	//////////////////////////////////////////////////////////////////////////
	// OTHER STUFF
}
