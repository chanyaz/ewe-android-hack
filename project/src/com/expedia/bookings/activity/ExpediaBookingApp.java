package com.expedia.bookings.activity;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;

import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;

import com.activeandroid.ActiveAndroid;
import com.expedia.bookings.R;
import com.expedia.bookings.appwidget.ExpediaBookingsWidgetProvider;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.FontCache;
import com.mobiata.android.DebugUtils;
import com.mobiata.android.Log;
import com.mobiata.android.bitmaps.TwoLevelImageCache;
import com.mobiata.android.debug.MemoryUtils;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.SettingUtils;
import com.mobiata.flightlib.data.sources.FlightStatsDbUtils;
import com.nullwire.trace.ExceptionHandler;

public class ExpediaBookingApp extends Application implements UncaughtExceptionHandler {
	private static final String PREF_FIRST_LAUNCH = "PREF_FIRST_LAUNCH";

	private UncaughtExceptionHandler mOriginalUncaughtExceptionHandler;

	@Override
	public void onCreate() {
		super.onCreate();

		ActiveAndroid.initialize(this);

		boolean isRelease = AndroidUtils.isRelease(this);
		boolean isLogEnablerInstalled = DebugUtils.isLogEnablerInstalled(this);
		Log.configureLogging("ExpediaBookings", !isRelease || isLogEnablerInstalled);

		try {
			FlightStatsDbUtils.createDatabaseIfNotExists(this);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}

		try {
			final ApplicationInfo ai = this.getPackageManager().getApplicationInfo(this.getPackageName(),
					PackageManager.GET_META_DATA);
			if (ai.metaData != null) {
				String key = (String) ai.metaData.get("com.google.android.maps.v2.API_KEY");
				if (isRelease && !getString(R.string.mapsv2_prod_key).equals(key)) {
					throw new RuntimeException(
							"You are not using the release maps key for a release build. Edit AndroidManifest.xml");
				}

				if (!isRelease && !getString(R.string.mapsv2_dev_key).equals(key)) {
					throw new RuntimeException(
							"You are using the release maps key for a debug build. Edit AndroidManifest.xml");
				}
			}
		}
		catch (PackageManager.NameNotFoundException e) {
			throw new RuntimeException(e);
		}

		// Setup Omniture logging for crashes
		mOriginalUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);

		// Setup our personal logging for crashes
		if (isRelease && !isLogEnablerInstalled) {
			ExceptionHandler.register(this, "http://www.mobiata.com/appsupport/ftandroid/trace.php");
		}

		// Initialize some parts of the code that require a Context
		PointOfSale.init(this);
		FontCache.initialize(this);
		AdTracker.initialize(this);
		ItineraryManager.getInstance().init(this);

		if (!SettingUtils.get(this, PREF_FIRST_LAUNCH, false)) {
			SettingUtils.save(this, PREF_FIRST_LAUNCH, true);
			AdTracker.trackFirstLaunch();
		}

		// #13097: We need a way to disable the widget on ICS tablets.  This is a hacky way of doing so,
		// in that it requires the app to be launched at least once before it can be disabled, but it's
		// the best we can do for the time being.
		if (AndroidUtils.isHoneycombTablet(this)) {
			try {
				PackageManager pm = getPackageManager();
				ComponentName cn = new ComponentName(this, ExpediaBookingsWidgetProvider.class);
				pm.setComponentEnabledSetting(cn, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
						PackageManager.DONT_KILL_APP);
			}
			catch (Exception e) {
				// Just in case, PM can be touchy
				Log.w("PackageManager blew up.", e);
			}
		}

		// Some useful info to have on hand in case of memory crashes
		long maxMemory = Runtime.getRuntime().maxMemory();
		int memoryClass = ((ActivityManager) getSystemService(ACTIVITY_SERVICE)).getMemoryClass();
		Log.i("MaxMemory=" + maxMemory + " bytes (" + (maxMemory / 1048576) + "MB) MemoryClass=" + memoryClass + "MB");

		// Init TwoLevelImageCache
		TwoLevelImageCache.init(this);
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		OmnitureTracking.trackCrash(this, ex);

		// Perform a heap dump on OOME for easy reference.
		String exceptionClass = ex == null ? "" : ex.getClass() == null ? "" : ex.getClass().getName();
		Log.d("ExpediaBookingApp exception handler with exception of class " + exceptionClass);
		if (OutOfMemoryError.class.equals(ex.getClass())) {
			MemoryUtils.dumpHprofDataToSdcard("dump.hprof", getApplicationContext());
		}

		// Call the original exception handler
		mOriginalUncaughtExceptionHandler.uncaughtException(thread, ex);
	}

	//////////////////////////////////////////////////////////////////////////
	// All-app utilities

	// #11076 - for Android 3.0, we still use the phone version of the app due to crippling bugs.
	public static boolean useTabletInterface(Context context) {
		return AndroidUtils.getSdkVersion() >= 12
				&& (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// LISTENERS FOR WHEN SEARCH PARAMS CHANGE IN THE WIDGET
	//////////////////////////////////////////////////////////////////////////////////////////
	/*
	 *  The app maintains a list of listeners to notify when search params
	 *  change in the widget. This is so that we can easily propogate through the
	 *  the app the need to use searchParams from the widget instead of the ones
	 *  being driven by user parameters set within the app
	 */
	public interface OnSearchParamsChangedInWidgetListener {
		public void onSearchParamsChanged(SearchParams searchParams);
	};

	private ArrayList<OnSearchParamsChangedInWidgetListener> mListeners;

	public void registerSearchParamsChangedInWidgetListener(OnSearchParamsChangedInWidgetListener listener) {
		if (mListeners == null) {
			mListeners = new ArrayList<OnSearchParamsChangedInWidgetListener>();
		}
		mListeners.add(listener);
	}

	public void unregisterSearchParamsChangedInWidgetListener(OnSearchParamsChangedInWidgetListener listener) {
		if (mListeners == null) {
			return;
		}
		mListeners.remove(listener);
	}

	public void broadcastSearchParamsChangedInWidget(SearchParams searchParams) {
		if (mListeners != null) {
			for (OnSearchParamsChangedInWidgetListener listener : mListeners) {
				listener.onSearchParamsChanged(searchParams);
			}
		}
	}
}
