package com.expedia.bookings.appwidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.RemoteViews;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.model.WidgetConfigurationState;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.mobiata.android.Log;

public class ExpediaBookingsWidgetProvider extends AppWidgetProvider {

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);

		Log.i("ExpediaBookings widgets have been enabled.");

		// Enable receiver for updates to search parameters
		// and location as well
		PackageManager pm = context.getPackageManager();
		pm.setComponentEnabledSetting(new ComponentName(context, ExpediaBookingsService.class),
				PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

		// Track that the widget has been installed
		OmnitureTracking.trackSimpleEvent(context, null, null, "App.Widget.Install");
	}

	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);

		// Disable receiver updates
		PackageManager pm = context.getPackageManager();
		pm.setComponentEnabledSetting(new ComponentName(context, ExpediaBookingsService.class),
				PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);

		// Track that the widget has been uninstalled
		OmnitureTracking.trackSimpleEvent(context, null, null, "App.Widget.Remove");
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		// Remove this widget from the list of tracking widget
		Intent intent = new Intent(ExpediaBookingsService.getCancelUpdateAction(context));
		intent.putExtra(Codes.APP_WIDGET_ID, appWidgetIds[0]);
		context.startService(intent);
		WidgetConfigurationState.deleteWidgetConfigState(context, appWidgetIds[0]);
		super.onDeleted(context, appWidgetIds);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		int[] existingAppWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(
				new ComponentName(context, ExpediaBookingsWidgetProvider.class));
		WidgetConfigurationState.reconcileWidgetConfigurationStates(context, existingAppWidgetIds);

		for (int id : appWidgetIds) {
			// add the widget if it doesn't exist
			if (WidgetConfigurationState.getWidgetConfiguration(context, id) == null) {
				saveLastSearchOrCurrentLocationOption(context, id);
				Intent intent = new Intent(ExpediaBookingsService.getStartCleanSearchAction(context));
				intent.putExtra(Codes.APP_WIDGET_ID, id);
				context.startService(intent);
			}
		}

		RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget);
		RemoteViews widgetContainer = new RemoteViews(context.getPackageName(), R.layout.widget_contents);
		rv.addView(R.id.hotel_info_contents, widgetContainer);
		widgetContainer.setViewVisibility(R.id.navigation_container, View.GONE);

		Intent intent = new Intent(ExpediaBookingsService.getStartSearchAction(context));
		context.startService(intent);

		for (int appWidgetId : appWidgetIds) {
			appWidgetManager.updateAppWidget(appWidgetId, rv);
		}

		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	private void saveLastSearchOrCurrentLocationOption(Context context, int mAppWidgetId) {
		WidgetConfigurationState cs = new WidgetConfigurationState();
		cs.setAppWidgetId(mAppWidgetId);
		cs.setExactSearchLocation(null);
		cs.save();
	}
}
