package com.expedia.bookings.activity;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;

import com.expedia.bookings.tracking.TrackingUtils;
import com.mobiata.android.DebugUtils;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.hotellib.Params;
import com.nullwire.trace.ExceptionHandler;
import com.omniture.AppMeasurement;

public class ExpediaBookingApp extends com.activeandroid.Application implements UncaughtExceptionHandler {

	private UncaughtExceptionHandler mOriginalUncaughtExceptionHandler;

	@Override
	public void onCreate() {
		super.onCreate();

		Params params = Params.getInstance();
		params.mIsRelease = AndroidUtils.isRelease(this);
		boolean isLogEnablerInstalled = DebugUtils.isLogEnablerInstalled(this);
		Log.configureLogging("ExpediaBookings", !params.mIsRelease || isLogEnablerInstalled);

		// Setup Omniture logging for crashes
		mOriginalUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);

		// Setup our personal logging for crashes
		if (params.mIsRelease && !isLogEnablerInstalled) {
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
		s.prop36 = printWriter.toString();

		TrackingUtils.trackOnClick(s);

		// Call the original exception handler
		mOriginalUncaughtExceptionHandler.uncaughtException(thread, ex);
	}
}
