package com.expedia.bookings.activity;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;

import com.activeandroid.ActiveAndroid;
import com.expedia.bookings.R;
import com.expedia.bookings.appwidget.ExpediaBookingsWidgetProvider;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.tracking.TrackingUtils;
import com.expedia.bookings.utils.LocaleUtils;
import com.mobiata.android.DebugUtils;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.SettingUtils;
import com.nullwire.trace.ExceptionHandler;
import com.omniture.AppMeasurement;

public class ExpediaBookingApp extends Application implements UncaughtExceptionHandler {

	private UncaughtExceptionHandler mOriginalUncaughtExceptionHandler;

	@Override
	public void onCreate() {
		super.onCreate();

		ActiveAndroid.initialize(this);

		boolean isRelease = AndroidUtils.isRelease(this);
		boolean isLogEnablerInstalled = DebugUtils.isLogEnablerInstalled(this);
		Log.configureLogging("ExpediaBookings", !isRelease || isLogEnablerInstalled);

		// Setup Omniture logging for crashes
		mOriginalUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);

		// Setup our personal logging for crashes
		if (isRelease && !isLogEnablerInstalled) {
			ExceptionHandler.register(this, "http://www.mobiata.com/appsupport/ftandroid/trace.php");
		}

		// Initialize some parts of the code that require a Context
		Rate.initInclusivePrices(this);

		// Fill POS based on locale if it's not already filled.
		// Do it here so it becomes a sticky preference, i.e. it won't
		// change magically if the user changes his locale. Chances are, he wants
		// to keep using the same Expedia POS even if he changes his locale.
		String posKey = getString(R.string.PointOfSaleKey);
		if (null == SettingUtils.get(this, posKey, null)) {
			SettingUtils.save(this, posKey, LocaleUtils.getDefaultPointOfSale(this));
		}

		LocaleUtils.onPointOfSaleChanged(this);

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
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		// Log the crash
		Log.d("Tracking \"crash\" onClick");
		AppMeasurement s = new AppMeasurement(this);
		TrackingUtils.addStandardFields(this, s);
		s.events = "event39";
		s.eVar28 = s.prop16 = "App.Crash";

		final Writer writer = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(writer);
		ex.printStackTrace(printWriter);
		s.prop36 = ex.getMessage() + "|" + writer.toString();

		Log.i("prop36: " + s.prop36);

		TrackingUtils.trackOnClick(s);

		// Call the original exception handler
		mOriginalUncaughtExceptionHandler.uncaughtException(thread, ex);
	}

	//////////////////////////////////////////////////////////////////////////
	// All-app utilities

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
