package com.expedia.bookings.activity;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;

import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.tracking.TrackingUtils;
import com.mobiata.android.DebugUtils;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;
import com.nullwire.trace.ExceptionHandler;
import com.omniture.AppMeasurement;

public class ExpediaBookingApp extends com.activeandroid.Application implements UncaughtExceptionHandler {

	private UncaughtExceptionHandler mOriginalUncaughtExceptionHandler;

	@Override
	public void onCreate() {
		super.onCreate();

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

		TrackingUtils.trackOnClick(s);

		// Call the original exception handler
		mOriginalUncaughtExceptionHandler.uncaughtException(thread, ex);
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
		if(mListeners == null) {
			mListeners = new ArrayList<OnSearchParamsChangedInWidgetListener>();
		}
		mListeners.add(listener);
	}
	
	public void unregisterSearchParamsChangedInWidgetListener(OnSearchParamsChangedInWidgetListener listener) {
		if(mListeners == null) {
			return;
		}
		mListeners.remove(listener);
	}
	
	public void broadcastSearchParamsChangedInWidget(SearchParams searchParams) {
		if(mListeners != null) {
			for(OnSearchParamsChangedInWidgetListener listener : mListeners) {
				listener.onSearchParamsChanged(searchParams);
			}
		}
	}
	
}
