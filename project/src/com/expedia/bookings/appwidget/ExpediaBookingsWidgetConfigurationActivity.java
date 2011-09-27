package com.expedia.bookings.appwidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.model.WidgetConfigurationState;

public class ExpediaBookingsWidgetConfigurationActivity extends Activity {

	private int mAppWidgetId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// setting the result code to be cancelled
		// so that if the user backs out of the activity
		// the widget is not setup
		setResult(Activity.RESULT_CANCELED);

		// get the app widget ids from the intent
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		}
		
		saveLastSearchOrCurrentLocationOption();
		installWidget();
	}

	private void saveLastSearchOrCurrentLocationOption() {
		WidgetConfigurationState cs = new WidgetConfigurationState(this);
		cs.setAppWidgetId(mAppWidgetId);
		cs.setExactSearchLocation(null);
		cs.save();
	}

	private void installWidget() {
		AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(ExpediaBookingsWidgetConfigurationActivity.this);
		RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget);
		RemoteViews widgetContainer = new RemoteViews(getPackageName(), R.layout.widget_contents);
		views.addView(R.id.hotel_info_contents, widgetContainer);
		widgetContainer.setViewVisibility(R.id.navigation_container, View.GONE);

		appWidgetManager.updateAppWidget(mAppWidgetId, views);
		Intent resultValue = new Intent();
		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
		setResult(RESULT_OK, resultValue);
		Intent intent = new Intent(ExpediaBookingsService.START_CLEAN_SEARCH_ACTION);
		intent.putExtra(Codes.APP_WIDGET_ID, mAppWidgetId);
		startService(intent);
		finish();
	}
}
