package com.expedia.bookings.appwidget;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.ReadablePeriod;
import org.joda.time.Seconds;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.HotelDetailsFragmentActivity;
import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.bitmaps.L2ImageCache;
import com.expedia.bookings.data.Distance.DistanceUnit;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.HotelSearchResponse;
import com.expedia.bookings.data.Media;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.JodaUtils;
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
		LocationListener, L2ImageCache.OnBitmapLoaded {

	//////////////////////////////////////////////////////////////////////////
	// Constants

	public static final String TAG = "ExpediaWidget";

	private static final String WIDGET_KEY_SEARCH = "WIDGET_KEY_SEARCH";

	// The maximum # of properties to show in the widget
	private static final int MAX_DEALS = 5;

	// How often we should update the widget, by default
	private static final ReadablePeriod UPDATE_INTERVAL = Hours.ONE;

	// How often we should update the widget in low power mode
	private static final ReadablePeriod UPDATE_INTERVAL_LOW_ENERGY = Hours.SIX;

	// The distance one must be from their last search location before we want to update
	private static final int UPDATE_DISTANCE_METERS = 8000; // 8km

	// The minimum time between updates, regardless of when one is requested
	private static final ReadablePeriod MINIMUM_UPDATE_INTERVAL = Minutes.minutes(15);

	// If we try to refresh but fail for some reason, we keep doubling the backoff time
	private static final ReadablePeriod CONNECTION_ERROR_BACKOFF = Minutes.ONE;

	// The maximum backoff time
	private static final ReadablePeriod MAX_CONNECTION_ERROR_BACKOFF = Hours.ONE;

	// The maximum backoff time in low energy mode
	private static final ReadablePeriod MAX_CONNECTION_ERROR_BACKOFF_LOW_ENERGY = Hours.SIX;

	// How long before rotating the shown hotel
	private static final ReadablePeriod ROTATE_INTERVAL = Seconds.seconds(5);

	// When the user interacts with the widget, how long we should delay until the next rotation
	private static final ReadablePeriod INTERACTION_DELAY = Seconds.seconds(30);

	// So that we're loading URLs on our own key
	private static final String WIDGET_THUMBNAIL_KEY_PREFIX = "WIDGET_THUMBNAIL_KEY.";

	// Actions used to change things in the service
	private static final String ACTION_PREVIOUS = "ACTION_PREVIOUS";
	private static final String ACTION_NEXT = "ACTION_NEXT";

	//////////////////////////////////////////////////////////////////////////
	// Data

	private LocationClient mLocationClient;

	// Indicates when the widget should use low energy; this should be enabled
	// whenever the system is not actively displaying the widget (to the best
	// of our knowledge).
	private boolean mUseLowEnergy;

	private HotelSearchParams mSearchParams = new HotelSearchParams();
	private List<Property> mDeals = new ArrayList<Property>();
	private DateTime mLastUpateTimestamp;
	private boolean mLoadedDeals = false;

	// 0 == branding, 1-X == deals
	private int mCurrentPosition = 0;

	private Handler mHandler;

	private int mFailureCount = 0;

	// You have to request new location updates when switching between power
	// modes; however, this causes the location listener to fire once
	// immediately.  So we have to verify location updates ourselves, too.
	private Location mLastLocation;

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	public void onCreate() {
		super.onCreate();

		Log.d(TAG, "ExpediaAppWidgetService.onCreate()");

		mHandler = new LeakSafeHandler(this);

		mLocationClient = new LocationClient(this, this, this);
		mLocationClient.connect();

		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		registerReceiver(mScreenReceiver, filter);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "ExpediaAppWidgetService.onStartCommand(" + intent + ", " + flags + ", " + startId + ")");

		if (intent != null && !TextUtils.isEmpty(intent.getAction())) {
			String action = intent.getAction();
			if (action.equals(ACTION_NEXT)) {
				cancelRotation();
				rotateProperty(1);
				setupNextRotation(true);
			}
			else if (action.equals(ACTION_PREVIOUS)) {
				cancelRotation();
				rotateProperty(-1);
				setupNextRotation(true);
			}
		}

		// There might be a new widget or something; might as well update widgets
		updateWidgets(true);

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

		// Clear out any Handler messages so we can let that shut down
		mHandler.removeMessages(WHAT_ROTATE);
		mHandler.removeMessages(WHAT_UPDATE);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// Ignore; nothing ever binds to this Service
		return null;
	}

	//////////////////////////////////////////////////////////////////////////
	// General Utility

	private long getMillisFromPeriod(ReadablePeriod period) {
		return period.toPeriod().toStandardDuration().getMillis();
	}

	//////////////////////////////////////////////////////////////////////////
	// Power states
	//
	// If the user's screen is off, we want to go in low power mode

	private final BroadcastReceiver mScreenReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "Screen state changed: " + intent.getAction());

			setPowerState(intent.getAction().equals(Intent.ACTION_SCREEN_OFF));
		}
	};

	private final BroadcastReceiver mTimeTickReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (!mSearchParams.isDefaultStay()) {
				Log.i(TAG, "TIME_TICK started new search, it's a new day!");
				startNewSearch();
			}
			else {
				Log.v(TAG, "TIME_TICK received, but date is still valid");
			}
		}
	};

	private void setPowerState(boolean useLowPower) {
		Log.i(TAG, "Switching power state to " + (useLowPower ? "LOW" : "HIGH") + " energy");

		mUseLowEnergy = useLowPower;

		// Update location request based on power state
		requestLocationUpdates();

		// Turn on/off property rotation based on power state
		if (useLowPower) {
			cancelRotation();
		}
		else {
			setupNextRotation(false);
		}

		// Check if we're overdue for a search; only valid if going to high energy
		if (!useLowPower && JodaUtils.isExpired(mLastUpateTimestamp, getNextSearchDelay())
				&& !mSearchParams.isDefaultStay()) {
			startNewSearch();
		}
		else {
			scheduleSearch();
		}

		// Setup time tick receiver if high power, otherwise disable
		if (useLowPower) {
			try {
				unregisterReceiver(mTimeTickReceiver);
			}
			catch (IllegalArgumentException e) {
				// Ignore; we don't care if we were already unregistered
			}
		}
		else {
			registerReceiver(mTimeTickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
		}
	}

	private void requestLocationUpdates() {
		if (mLocationClient != null) {
			LocationRequest request = new LocationRequest();
			request.setPriority(mUseLowEnergy ? LocationRequest.PRIORITY_NO_POWER : LocationRequest.PRIORITY_LOW_POWER);
			request.setFastestInterval(getMillisFromPeriod(MINIMUM_UPDATE_INTERVAL));
			request.setInterval(request.getFastestInterval());
			request.setSmallestDisplacement(UPDATE_DISTANCE_METERS);

			mLocationClient.requestLocationUpdates(request, this);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Widget Views

	private void updateWidgets(boolean animate) {
		Log.v(TAG, "Updating widget views...");

		// Configure remote views
		RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.app_widget);

		Intent onClickWidgetIntent = new Intent(this, SearchActivity.class);
		if (mDeals.size() > 0) {
			// We remove all views/re-add so that layout animations play
			if (animate) {
				remoteViews.removeAllViews(R.id.widget_contents_container);
				remoteViews.addView(R.id.widget_contents_container,
						new RemoteViews(getPackageName(), R.layout.app_widget_contents));
			}

			remoteViews.setViewVisibility(R.id.loading_text_container, View.GONE);
			remoteViews.setViewVisibility(R.id.widget_results_container, View.VISIBLE);

			if (mCurrentPosition == 0) {
				// Remove hangatg/re-add so that layout animations play (it's separate from the contents)
				if (animate) {
					remoteViews.removeAllViews(R.id.widget_hang_tag);
					remoteViews.addView(R.id.widget_hang_tag,
							new RemoteViews(getPackageName(), R.layout.app_widget_hangtag));
				}

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

				Media thumbnail = property.getThumbnail();
				if (thumbnail != null) {
					String url = thumbnail.getOriginalUrl();
					Bitmap bitmap = L2ImageCache.sGeneralPurpose.getImage(url, false);
					if (bitmap != null) {
						remoteViews.setImageViewBitmap(R.id.hotel_image_view, bitmap);
					}
					else {
						remoteViews.setImageViewResource(R.id.hotel_image_view, R.drawable.widget_thumbnail_background);

						L2ImageCache.sGeneralPurpose.loadImage(WIDGET_THUMBNAIL_KEY_PREFIX + url, url, false, this);
					}
				}

				onClickWidgetIntent = HotelDetailsFragmentActivity.createIntent(this, 0, mSearchParams, property);
			}

			remoteViews.setOnClickPendingIntent(R.id.prev_hotel_btn, createPendingIntent(ACTION_PREVIOUS));
			remoteViews.setOnClickPendingIntent(R.id.next_hotel_btn, createPendingIntent(ACTION_NEXT));
		}
		else {
			// Show text
			remoteViews.setViewVisibility(R.id.loading_text_container, View.VISIBLE);
			remoteViews.setViewVisibility(R.id.widget_results_container, View.GONE);
			remoteViews.setViewVisibility(R.id.widget_hang_tag, View.GONE);

			if (!mLoadedDeals) {
				remoteViews.setTextViewText(R.id.widget_text_view, getString(R.string.loading_hotels));
			}
			else {
				// If we either had no results, or there was an error, display that no hotels were found
				remoteViews.setTextViewText(R.id.widget_text_view, getString(R.string.progress_search_failed));
			}
		}

		remoteViews.setOnClickPendingIntent(R.id.widget_root,
				PendingIntent.getActivity(this, 0, onClickWidgetIntent, PendingIntent.FLAG_UPDATE_CURRENT));

		// Update all widgets with the same content
		AppWidgetManager widgetManager = AppWidgetManager.getInstance(this);
		int[] widgets = widgetManager.getAppWidgetIds(new ComponentName(this, ExpediaBookingsWidgetProvider.class));
		widgetManager.updateAppWidget(widgets, remoteViews);
	}

	private PendingIntent createPendingIntent(String action) {
		Intent intent = new Intent(this, getClass());
		intent.setAction(action);
		return PendingIntent.getService(this, 0, intent, 0);
	}

	//////////////////////////////////////////////////////////////////////////
	// Search

	private void scheduleSearch() {
		mHandler.removeMessages(WHAT_UPDATE);

		Message msg = mHandler.obtainMessage(WHAT_UPDATE);
		long delay = getNextSearchDelay();
		mHandler.sendMessageDelayed(msg, delay);

		Log.d(TAG, "Scheduling next automatic search in " + (new Duration(delay)).getStandardMinutes() + " minutes");
	}

	private long getNextSearchDelay() {
		if (mFailureCount != 0) {
			// Exponential back-off between attempts
			ReadablePeriod backoffBase = mUseLowEnergy ? MINIMUM_UPDATE_INTERVAL : CONNECTION_ERROR_BACKOFF;
			ReadablePeriod maxBackoff = mUseLowEnergy ? MAX_CONNECTION_ERROR_BACKOFF_LOW_ENERGY
					: MAX_CONNECTION_ERROR_BACKOFF;
			return Math.min(getMillisFromPeriod(backoffBase) * (long) Math.pow(2, mFailureCount - 1),
					getMillisFromPeriod(maxBackoff));
		}
		else {
			ReadablePeriod delayPeriod = mUseLowEnergy ? UPDATE_INTERVAL_LOW_ENERGY : UPDATE_INTERVAL;
			return getMillisFromPeriod(delayPeriod);
		}
	}

	private void startNewSearch() {
		Log.i(TAG, "Widget is starting a new search...");

		// Clear old results
		mDeals.clear();
		mLoadedDeals = false;
		mCurrentPosition = 0;
		updateWidgets(true);

		// Start new search if one isn't currently running
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (!bd.isDownloading(WIDGET_KEY_SEARCH)) {
			// 2178: Ensure that we're searching for *today*
			mSearchParams.setDefaultStay();

			bd.startDownload(WIDGET_KEY_SEARCH, mSearchDownload, mSearchCallback);
		}
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
			mLastUpateTimestamp = DateTime.now();
			mDeals.addAll(getDeals(results));
			updateWidgets(true);

			// If there was an error getting results, try again (soon)
			if (results == null || results.hasErrors()) {
				mFailureCount++;
				Log.w(TAG, "Search resulted in failure, increasing failure count to " + mFailureCount);
			}
			else {
				mFailureCount = 0;
				setupNextRotation(false);
			}

			scheduleSearch();
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
	// Rotating widget views

	/**
	 * Rotates the property shown on the widget; does NOT update widget Views,
	 * leaves that to the caller.
	 */
	private void rotateProperty(int rotateBy) {
		Log.v(TAG, "Rotating properties by: " + rotateBy);
		int numPositions = mDeals.size() + 1;
		mCurrentPosition = (mCurrentPosition + numPositions + rotateBy) % numPositions;
	}

	private void setupNextRotation(boolean delayDueToInteraction) {
		mHandler.removeMessages(WHAT_ROTATE);

		if (!mUseLowEnergy && mDeals.size() != 0) {
			Message msg = mHandler.obtainMessage(WHAT_ROTATE);
			ReadablePeriod delayPeriod = delayDueToInteraction ? INTERACTION_DELAY : ROTATE_INTERVAL;
			mHandler.sendMessageDelayed(msg, getMillisFromPeriod(delayPeriod));
		}
	}

	private void cancelRotation() {
		Log.d(TAG, "Cancelling property rotations");
		mHandler.removeMessages(WHAT_ROTATE);
	}

	//////////////////////////////////////////////////////////////////////////
	// ConnectionCallbacks

	@Override
	public void onConnected(Bundle connectionHint) {
		Log.d(TAG, "ExpediaAppWidgetService.onConnected(" + connectionHint + ")");

		requestLocationUpdates();
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

		if (mLastLocation == null || (location.distanceTo(mLastLocation) > UPDATE_DISTANCE_METERS
				&& JodaUtils.isExpired(mLastUpateTimestamp, getMillisFromPeriod(MINIMUM_UPDATE_INTERVAL)))) {
			mSearchParams.setSearchLatLon(location.getLatitude(), location.getLongitude());
			startNewSearch();
			mLastLocation = location;
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// L2ImageCache.OnBitmapLoaded

	@Override
	public void onBitmapLoaded(String url, Bitmap bitmap) {
		Log.d(TAG, "Loaded widget image: " + url);
		updateWidgets(false);
	}

	@Override
	public void onBitmapLoadFailed(String url) {
		// Ignore
	}

	//////////////////////////////////////////////////////////////////////////
	// Handler

	private static final int WHAT_ROTATE = 1;

	private static final int WHAT_UPDATE = 2;

	private static final class LeakSafeHandler extends Handler {

		private WeakReference<ExpediaAppWidgetService> mWeakReference;

		protected LeakSafeHandler(ExpediaAppWidgetService service) {
			mWeakReference = new WeakReference<ExpediaAppWidgetService>(service);
		}

		@Override
		public void handleMessage(Message msg) {
			ExpediaAppWidgetService service = mWeakReference.get();
			if (service != null) {
				switch (msg.what) {
				case WHAT_ROTATE:
					service.rotateProperty(1);
					service.updateWidgets(true);
					service.setupNextRotation(false);
					break;
				case WHAT_UPDATE:
					service.startNewSearch();
					break;
				}
			}
		}
	}
}
