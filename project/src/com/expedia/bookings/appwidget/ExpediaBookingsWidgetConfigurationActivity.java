package com.expedia.bookings.appwidget;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Address;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.widget.WidgetPreviewHandler;
import com.mobiata.android.LocationServices;
import com.mobiata.android.util.NetUtils;
import com.mobiata.android.util.SettingUtils;
import com.mobiata.hotellib.data.Codes;
import com.mobiata.hotellib.utils.StrUtils;

public class ExpediaBookingsWidgetConfigurationActivity extends Activity {

	private static final String SPECIFIC_CITY = "specificCity";
	private static final String LAST_SEARCH = "lastSearch";
	private static final String CURRENT_LOCATION = "currentLocation";

	private static final String SELECTED_OPTION = "SELECTED_OPTION";
	private static final String PROGRESS_BAR_SHOWING = "PROGRESS_BAR_SHOWING";

	private static final int DIALOG_LOCATION_SUGGESTIONS = 0;

	private ImageButton mCurrentLocationOption;
	private ImageButton mLastSearchOption;
	private ImageButton mSpecifyLocationOption;
	private TextView mSpecificLocationTextView;
	private TextView mErrorTextView;
	private ProgressBar mProgressBar;

	private int mAppWidgetId;
	private String mSelectedOption;
	private Thread mGeocodeThread;
	private List<Address> mAddresses;
	private WidgetPreviewHandler mWidgetPreviewHandler;

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

		setContentView(R.layout.activity_widget_config);

		Button addWidgetButton = (Button) findViewById(R.id.add_widget_button);
		mCurrentLocationOption = (ImageButton) findViewById(R.id.current_location_option);
		mLastSearchOption = (ImageButton) findViewById(R.id.last_search_option);
		mSpecifyLocationOption = (ImageButton) findViewById(R.id.specify_location_option);
		mProgressBar = (ProgressBar) findViewById(R.id.location_search_progress_bar);
		mSpecificLocationTextView = (TextView) findViewById(R.id.location_option_text_view);
		mErrorTextView = (TextView) findViewById(R.id.error_text_view);

		mWidgetPreviewHandler = new WidgetPreviewHandler(this);

		addWidgetButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mSelectedOption.equals(SPECIFIC_CITY)) {
					geoCodeLocation();
				}
				else {
					saveLastSearchOrCurrentLocationOption();
					installWidget();
				}
			}
		});

		mCurrentLocationOption.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mSelectedOption = CURRENT_LOCATION;
				setupCheckedState();
				return true;
			}
		});

		mLastSearchOption.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mSelectedOption = LAST_SEARCH;
				setupCheckedState();
				return true;
			}
		});

		mSpecifyLocationOption.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mSelectedOption = SPECIFIC_CITY;
				setupCheckedState();
				return true;
			}
		});

		mSelectedOption = CURRENT_LOCATION;

		if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_OPTION)) {
			mSelectedOption = savedInstanceState.getString(SELECTED_OPTION);
			if (savedInstanceState.getBoolean(PROGRESS_BAR_SHOWING, false)) {
				mProgressBar.setVisibility(View.VISIBLE);
			}
		}

		String specficLocation = PreferenceManager.getDefaultSharedPreferences(this).getString(
				Codes.WIDGET_SPECIFIC_LOCATION_PREFIX, "");
		mSpecificLocationTextView.setText(specficLocation);

	}

	@Override
	protected void onResume() {
		super.onResume();
		setupCheckedState();
		mWidgetPreviewHandler.loadPreviewHotels();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(SELECTED_OPTION, mSelectedOption);
		outState.putBoolean(PROGRESS_BAR_SHOWING, (mProgressBar.getVisibility() == View.VISIBLE));
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_LOCATION_SUGGESTIONS:
			CharSequence[] freeFormLocations = StrUtils.formatAddresses(mAddresses);

			AlertDialog.Builder builder = new Builder(this);
			builder.setTitle(R.string.ChooseLocation);
			builder.setItems(freeFormLocations, new Dialog.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					Address address = mAddresses.get(which);
					String formattedAddress = StrUtils.removeUSAFromAddress(address);
					mSpecificLocationTextView.setText(formattedAddress);
					saveSpecificLocation(formattedAddress, address.getLatitude(), address.getLongitude());
					installWidget();
				}
			});
			builder.setNegativeButton(android.R.string.cancel, null);
			return builder.create();
		}
		return super.onCreateDialog(id);
	}

	/*
	 * Given that we cannot have a radiogroup setup
	 * such that the radiobuttons are nested in a layout structure
	 * to achieve the desired layout structure, have to simulate our 
	 * own radiogroup behavior such that we can have an edit text next to
	 * the radio button.
	 * See for details: http://code.google.com/p/android/issues/detail?id=8952
	 */
	private void setupCheckedState() {
		if (mSelectedOption.equals(CURRENT_LOCATION)) {
			mCurrentLocationOption.setPressed(true);
			mCurrentLocationOption.setEnabled(false);

			mLastSearchOption.setPressed(false);
			mLastSearchOption.setEnabled(true);

			mSpecifyLocationOption.setPressed(false);
			mSpecifyLocationOption.setEnabled(true);
		}
		else if (mSelectedOption.equals(LAST_SEARCH)) {
			mCurrentLocationOption.setPressed(false);
			mCurrentLocationOption.setEnabled(true);

			mLastSearchOption.setPressed(true);
			mLastSearchOption.setEnabled(false);

			mSpecifyLocationOption.setPressed(false);
			mSpecifyLocationOption.setEnabled(true);

		}
		else if (mSelectedOption.equals(SPECIFIC_CITY)) {
			mCurrentLocationOption.setPressed(false);
			mCurrentLocationOption.setEnabled(true);

			mLastSearchOption.setPressed(false);
			mLastSearchOption.setEnabled(true);

			mSpecifyLocationOption.setPressed(true);
			mSpecifyLocationOption.setEnabled(false);
		}
	}

	private void geoCodeLocation() {
		if (!NetUtils.isOnline(this)) {
			showError(R.string.error_no_internet);
			return;
		}

		mProgressBar.setVisibility(View.VISIBLE);
		clearError();

		if (mGeocodeThread != null) {
			mGeocodeThread.interrupt();
		}

		mGeocodeThread = new Thread(new Runnable() {

			@Override
			public void run() {
				mAddresses = LocationServices.geocode(ExpediaBookingsWidgetConfigurationActivity.this,
						mSpecificLocationTextView.getText().toString());
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mProgressBar.setVisibility(View.INVISIBLE);
						if (mAddresses != null && mAddresses.size() > 1) {
							showDialog(DIALOG_LOCATION_SUGGESTIONS);
						}
						else if (mAddresses != null && mAddresses.size() > 0) {
							Address address = mAddresses.get(0);
							String formattedAddress = StrUtils.removeUSAFromAddress(address);
							mSpecificLocationTextView.setText(formattedAddress);
							saveSpecificLocation(formattedAddress, address.getLatitude(), address.getLongitude());
							installWidget();
						}
						else {
							showError(R.string.geolocation_failed);
						}
					}
				});
			}
		});
		mGeocodeThread.start();
	}

	private void showError(int strId) {
		mErrorTextView.setText(strId);
	}

	private void clearError() {
		mErrorTextView.setText(null);
	}

	private void saveSpecificLocation(String formattedAddress, double latitude, double longitude) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this.getApplicationContext());
		Editor editor = prefs.edit();
		editor.putBoolean(Codes.WIDGET_SHOW_HOTELS_NEAR_YOU_PREFIX + mAppWidgetId, false);
		editor.putBoolean(Codes.WIDGET_HOTELS_FROM_LAST_SEARCH_PREFIX + mAppWidgetId, false);
		editor.putString(Codes.WIDGET_SPECIFIC_LOCATION_PREFIX + mAppWidgetId, formattedAddress);
		editor.putFloat(Codes.WIDGET_LOCATION_LAT_PREFIX + mAppWidgetId, (float) latitude);
		editor.putFloat(Codes.WIDGET_LOCATION_LON_PREFIX + mAppWidgetId, (float) longitude);
		SettingUtils.commitOrApply(editor);
	}

	private void saveLastSearchOrCurrentLocationOption() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		Editor editor = prefs.edit();
		editor.putBoolean(Codes.WIDGET_SHOW_HOTELS_NEAR_YOU_PREFIX + mAppWidgetId,
				mSelectedOption.equals(CURRENT_LOCATION));
		editor.putBoolean(Codes.WIDGET_HOTELS_FROM_LAST_SEARCH_PREFIX + mAppWidgetId,
				mSelectedOption.equals(LAST_SEARCH));
		editor.putString(Codes.WIDGET_SPECIFIC_LOCATION_PREFIX + mAppWidgetId, null);
		SettingUtils.commitOrApply(editor);
	}

	private void installWidget() {
		AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(ExpediaBookingsWidgetConfigurationActivity.this);
		RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget);
		RemoteViews widgetContainer = new RemoteViews(getPackageName(),R.layout.widget_contents);
		views.addView(R.id.hotel_info_contents, widgetContainer);
		views.setViewVisibility(R.id.navigation_container, View.GONE);
		
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
