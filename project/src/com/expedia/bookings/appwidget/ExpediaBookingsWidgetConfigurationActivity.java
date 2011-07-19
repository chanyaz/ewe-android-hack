package com.expedia.bookings.appwidget;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

import com.expedia.bookings.R;
import com.mobiata.hotellib.data.Codes;

public class ExpediaBookingsWidgetConfigurationActivity extends Activity {

	private int mAppWidgetId;
	private static final int DIALOG_WIDGET_CONFIGIURATION = 0;
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
		    mAppWidgetId = extras.getInt(
		            AppWidgetManager.EXTRA_APPWIDGET_ID, 
		            AppWidgetManager.INVALID_APPWIDGET_ID);
		}
		
		setContentView(R.layout.activity_widget_confirmation);
		// present the options of area in which to display hotels
		// (Current location or based on last search) via a dialog
		showDialog(DIALOG_WIDGET_CONFIGIURATION);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id) {
		case DIALOG_WIDGET_CONFIGIURATION: {
			AlertDialog.Builder builder = new Builder(this);
			builder.setTitle("Configure Widget");
			
			CharSequence[] widgetOptions = new CharSequence[2];
			widgetOptions[0] = "Show hotels near you";
			widgetOptions[1] = "Show hotels based on last search";
			
			builder.setItems(widgetOptions, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(ExpediaBookingsWidgetConfigurationActivity.this);
					RemoteViews views = new RemoteViews(getPackageName(),
							R.layout.widget);
							appWidgetManager.updateAppWidget(mAppWidgetId, views);
					Intent resultValue = new Intent();
					resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ExpediaBookingsWidgetConfigurationActivity.this);
					Editor editor = prefs.edit();
					editor.putBoolean(Codes.WIDGET_SHOW_HOTELS_NEAR_YOU, (which == 0));
					editor.commit();
					setResult(RESULT_OK, resultValue);
					Intent intent = new Intent(ExpediaBookingsService.START_CLEAN_SEARCH_ACTION);
					sendBroadcast(intent);
					finish();
				}
			});
			builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			});
			return builder.create();
		}
		}
		return super.onCreateDialog(id);
	}
	
	
}
