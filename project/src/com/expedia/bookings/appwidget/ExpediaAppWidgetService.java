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

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;

import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.HotelSearchResponse;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.server.ExpediaServices;
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

	//////////////////////////////////////////////////////////////////////////
	// Data

	private LocationClient mLocationClient;

	private HotelSearchParams mSearchParams = new HotelSearchParams();
	private List<Property> mDeals = new ArrayList<Property>();
	private DateTime mLastUpateTimestamp;

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	public void onCreate() {
		super.onCreate();

		Log.i(TAG, "ExpediaAppWidgetService.onCreate()");

		mLocationClient = new LocationClient(this, this, this);
		mLocationClient.connect();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "ExpediaAppWidgetService.onStartCommand(" + intent + ", " + flags + ", " + startId + ")");

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		Log.i(TAG, "ExpediaAppWidgetService.onDestroy()");

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
	// Search

	private void startNewSearch() {
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
			mDeals.clear();
			mDeals.addAll(getDeals(results));
		}
	};

	// Filters through a full response to find the best deals
	private List<Property> getDeals(HotelSearchResponse response) {
		List<Property> deals = new ArrayList<Property>();

		if (response != null && !response.hasErrors() && response.getPropertiesCount() != 0) {
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
		Log.i(TAG, "ExpediaAppWidgetService.onLocationChanged(" + location + ")");

		// Due to the way we setup LocationRequest, this should only be called
		// when we actually want to do a new search (due to the user moving).
		startNewSearch();
	}

	//////////////////////////////////////////////////////////////////////////
	// OTHER STUFF
}
