package com.expedia.bookings.activity;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;

import com.expedia.bookings.model.WidgetConfigurationState;
import com.expedia.bookings.tracking.TrackingUtils;
import com.mobiata.android.DebugUtils;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.SettingUtils;
import com.nullwire.trace.ExceptionHandler;
import com.omniture.AppMeasurement;

public class ExpediaBookingApp extends com.activeandroid.Application implements UncaughtExceptionHandler {

	private UncaughtExceptionHandler mOriginalUncaughtExceptionHandler;
	
	private static final String NUM_LAUNCHES = "NUM_LAUNCHES";
	private static final String APP_VERSION = "APP_VERSION";
	private static final String WIDGET_NOTIFICATION_SHOWN = "WIDGET_NOTIFICATION_SHOWN";
	private static final int THRESHOLD_LAUNCHES = 2;

	
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
	
	public boolean toShowWidgetNotification() {
		// reset bookkeeping if the app was upgraded
		// so that the widget can be shown again
		if(wasAppUpgraded()) {
			SettingUtils.save(this, NUM_LAUNCHES, 1);
			SettingUtils.save(this, WIDGET_NOTIFICATION_SHOWN, false);
		}
		
		// wait for 2 launches before deciding to show the widget
		if(getNumLaunches() > THRESHOLD_LAUNCHES) {
			return !wasWidgetNotificationShown() && !areWidgetsInstalled();
		}
		
		return false; 
	}
	
	public void markWidgetNotificationAsShown() {
		SettingUtils.save(this, WIDGET_NOTIFICATION_SHOWN, true);
	}
	
	public void incrementLaunches() {
		int numLaunches = SettingUtils.get(this, NUM_LAUNCHES, 0);
		SettingUtils.save(this, NUM_LAUNCHES, ++numLaunches);
	}
	
	private int getNumLaunches() {
		int numLaunches = SettingUtils.get(this, NUM_LAUNCHES, 0); 
		return numLaunches;
	}
	
	private boolean wasAppUpgraded() {
		String currentVersionNumber = AndroidUtils.getAppVersion(this);
		String savedVersionNumber = SettingUtils.get(this, APP_VERSION, null);
		SettingUtils.save(this, APP_VERSION, currentVersionNumber);
		return !currentVersionNumber.equals(savedVersionNumber);
	}
	
	private boolean wasWidgetNotificationShown() {
		return SettingUtils.get(this,WIDGET_NOTIFICATION_SHOWN, false);
	}
	
	private boolean areWidgetsInstalled() {
		ArrayList<Object> widgetConfigs = WidgetConfigurationState.getAll(this);
		return !widgetConfigs.isEmpty();
	}
}
