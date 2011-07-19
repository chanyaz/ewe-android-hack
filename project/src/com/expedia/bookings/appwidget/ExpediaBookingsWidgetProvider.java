package com.expedia.bookings.appwidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.RemoteViews;

import com.expedia.bookings.R;
import com.mobiata.android.Log;

public class ExpediaBookingsWidgetProvider extends AppWidgetProvider {

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
		
		Log.i("ExpediaBookings widgets have been enabled.");
		
		// Enable receiver for updates to search parameters
		// and location as well
		PackageManager pm = context.getPackageManager();
		pm.setComponentEnabledSetting(new ComponentName(context, ExpediaBookingsWidgetReceiver.class),
				PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
	}
	
	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);
		
		// Disable receiver updates
		PackageManager pm = context.getPackageManager();
		pm.setComponentEnabledSetting(new ComponentName(context, ExpediaBookingsWidgetReceiver.class),
				PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
		
		// Cancel periodic updates of the widget
		context.startService(new Intent(ExpediaBookingsService.CANCEL_UPDATE_ACTION));
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		
		RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget);
		// Right now, we only support one widget a time - but should someone want to add the same
		// widget multiple times, we need to update them all the same way
		for (int appWidgetId : appWidgetIds) {
			appWidgetManager.updateAppWidget(appWidgetId, rv);
		}
		
		Intent intent = new Intent(ExpediaBookingsService.START_CLEAN_SEARCH_ACTION);
		context.sendBroadcast(intent);
	}
	
	

}
