package com.expedia.bookings.appwidget;

import java.util.Arrays;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

import com.expedia.bookings.tracking.OmnitureTracking;
import com.mobiata.android.Log;

public class ExpediaAppWidgetProvider extends AppWidgetProvider {

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);

		Log.i(ExpediaAppWidgetService.TAG, "ExpediaAppWidgetProvider.onEnabled()");

		// Start up the app widget service
		context.startService(new Intent(context, ExpediaAppWidgetService.class));

		// Track that the widget has been installed
		OmnitureTracking.trackSimpleEvent(context, null, null, "App.Widget.Install");
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);

		Log.i(ExpediaAppWidgetService.TAG,
				"ExpediaAppWidgetProvider.onUpdate(" + appWidgetManager + ", " + Arrays.toString(appWidgetIds) + ")");
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);

		Log.i(ExpediaAppWidgetService.TAG, "ExpediaAppWidgetProvider.onDeleted(" + Arrays.toString(appWidgetIds) + ")");
	}

	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);

		Log.i(ExpediaAppWidgetService.TAG, "ExpediaAppWidgetProvider.onDisabled()");

		context.stopService(new Intent(context, ExpediaAppWidgetService.class));
	}
}
