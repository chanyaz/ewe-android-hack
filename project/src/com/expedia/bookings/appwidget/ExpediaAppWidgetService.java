package com.expedia.bookings.appwidget;

import java.util.List;

import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.ReadablePeriod;
import org.joda.time.Seconds;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.expedia.bookings.data.Distance;
import com.expedia.bookings.data.Distance.DistanceUnit;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.Property;
import com.mobiata.android.Log;

public class ExpediaAppWidgetService extends Service {

	//////////////////////////////////////////////////////////////////////////
	// Constants

	public static final String TAG = "ExpediaWidget";

	private static final String WIDGET_KEY_SEARCH = "WIDGET_KEY_SEARCH";

	// How often we should update the widget, regardless of anything else
	private static final ReadablePeriod UPDATE_INTERVAL = Hours.ONE;

	// The distance one must be from their last search location before we want to update
	private static final Distance UPDATE_DISTANCE_MILES = new Distance(5, DistanceUnit.MILES);

	// The minimum time between updates, regardless of when one is requested
	private static final ReadablePeriod MINIMUM_UPDATE_INTERVAL = Hours.ONE;

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

	private HotelSearchParams mSearchParams;
	private List<Property> mDeals;

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	public void onCreate() {
		super.onCreate();

		Log.i(TAG, "ExpediaAppWidgetService.onCreate()");
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
	}

	@Override
	public IBinder onBind(Intent intent) {
		// Ignore; nothing ever binds to this Service
		return null;
	}

	//////////////////////////////////////////////////////////////////////////
	// OTHER STUFF
}
