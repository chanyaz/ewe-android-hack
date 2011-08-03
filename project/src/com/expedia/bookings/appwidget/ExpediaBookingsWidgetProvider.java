package com.expedia.bookings.appwidget;

import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.mobiata.android.Log;
import com.mobiata.hotellib.data.Codes;

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

	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		// Remove this widget from the list of tracking widget
		Intent intent = new Intent(ExpediaBookingsService.CANCEL_UPDATE_ACTION);
		intent.putExtra(Codes.APP_WIDGET_ID, appWidgetIds[0]);
		context.startService(intent);

		super.onDeleted(context, appWidgetIds);
	}
}
