package com.expedia.bookings.appwidget;

import java.util.List;

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
import com.expedia.bookings.data.Property;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.mobiata.android.Log;

public class ExpediaAppWidgetService extends Service implements ConnectionCallbacks, OnConnectionFailedListener,
		LocationListener {

	//////////////////////////////////////////////////////////////////////////
	// Constants

	public static final String TAG = "ExpediaWidget";

	private static final String WIDGET_KEY_SEARCH = "WIDGET_KEY_SEARCH";

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

	private HotelSearchParams mSearchParams;
	private List<Property> mDeals;

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
	}

	//////////////////////////////////////////////////////////////////////////
	// OTHER STUFF
}
