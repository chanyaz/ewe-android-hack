package com.expedia.bookings.activity;

import java.util.Calendar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.data.*;
import com.expedia.bookings.fragment.AirportPickerFragment;
import com.expedia.bookings.fragment.AirportPickerFragment.AirportPickerFragmentListener;
import com.expedia.bookings.fragment.CalendarDialogFragment;
import com.expedia.bookings.fragment.CalendarDialogFragment.CalendarDialogFragmentListener;
import com.expedia.bookings.fragment.PassengerPickerFragment;
import com.expedia.bookings.fragment.StatusFragment;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.DebugMenu;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.android.hockey.helper.HockeyPuck;
import com.mobiata.android.util.AndroidUtils;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;

public class FlightSearchActivity extends SherlockFragmentActivity implements AirportPickerFragmentListener,
		CalendarDialogFragmentListener {

	private static final String TAG_AIRPORT_PICKER = "TAG_AIRPORT_PICKER";
	private static final String TAG_DATE_PICKER = "TAG_DATE_PICKER";
	private static final String TAG_PASSENGER_PICKER = "TAG_PASSENGER_PICKER";
	private static final String TAG_STATUS = "TAG_STATUS";

	private static final String DOWNLOAD_KEY = "com.expedia.bookings.flights";

	// Controls the ratio of how large a selected EditText should take up
	// 1 == takes up the full size, 0 == takes up 50%.
	private static final float EDITTEXT_EXPANSION_RATIO = .25f;

	private View mFocusStealer;
	private LinearLayout mAirportsContainer;
	private EditText mDepartureAirportEditText;
	private EditText mArrivalAirportEditText;
	private Button mDatesButton;
	private View mPassengersButton;

	private HockeyPuck mHockeyPuck;

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle(R.string.search_flights);

		// Inflate the views
		setContentView(R.layout.activity_flight_search);

		mFocusStealer = Ui.findView(this, R.id.focus_stealer);

		// Configure airport pickers
		mAirportsContainer = Ui.findView(this, R.id.airports_container);
		mDepartureAirportEditText = Ui.findView(this, R.id.departure_airport_edit_text);
		mArrivalAirportEditText = Ui.findView(this, R.id.arrival_airport_edit_text);

		OnFocusChangeListener airportFocusChangeListener = new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					setFragment(TAG_AIRPORT_PICKER);

					if (v == mDepartureAirportEditText) {
						setAirportPickerFilter(mDepartureAirportEditText.getText());
					}
					else {
						setAirportPickerFilter(mArrivalAirportEditText.getText());
					}

					expandAirportEditText(v);
				}
				else {
					// If we're losing focus, set the airport code to what was in the field
					//
					// Note: this will not work if the EditTexts start displaying text different from
					// the airportCode.  But I expect the design of this page will change at some point anyhow.
					FlightSearchParams params = Db.getFlightSearch().getSearchParams();
					if (v == mDepartureAirportEditText) {
						params.setDepartureAirportCode(mDepartureAirportEditText.getText().toString().toUpperCase());
					}
					else {
						params.setArrivalAirportCode(mArrivalAirportEditText.getText().toString().toUpperCase());
					}
				}
			}
		};

		mDepartureAirportEditText.setOnFocusChangeListener(airportFocusChangeListener);
		mArrivalAirportEditText.setOnFocusChangeListener(airportFocusChangeListener);

		mDepartureAirportEditText.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_NEXT) {
					// Treat it like a click
					onAirportClick(mDepartureAirportEditText.getText().toString());
					return true;
				}

				return false;
			}
		});

		mArrivalAirportEditText.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_NEXT) {
					// Treat it like a click
					onAirportClick(mArrivalAirportEditText.getText().toString());
					return true;
				}

				return false;
			}
		});

		// Configure date button
		mDatesButton = Ui.findView(this, R.id.dates_button);
		mDatesButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				clearEditTextFocus();

				setFragment(TAG_DATE_PICKER);
			}
		});

		// Configure passenger button
		mPassengersButton = Ui.findView(this, R.id.passengers_button);
		mPassengersButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				clearEditTextFocus();

				setFragment(TAG_PASSENGER_PICKER);
			}
		});

		updateDateButton();

		// HockeyApp init
		mHockeyPuck = new HockeyPuck(this, Codes.HOCKEY_APP_ID, !AndroidUtils.isRelease(this));
		mHockeyPuck.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();

		mDepartureAirportEditText.addTextChangedListener(mAirportTextWatcher);
		mArrivalAirportEditText.addTextChangedListener(mAirportTextWatcher);

		if (BackgroundDownloader.getInstance().isDownloading(DOWNLOAD_KEY)) {
			showLoading();
			BackgroundDownloader.getInstance().registerDownloadCallback(DOWNLOAD_KEY, mDownloadCallback);
		}

		//HockeyApp crash
		mHockeyPuck.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();

		mDepartureAirportEditText.removeTextChangedListener(mAirportTextWatcher);
		mArrivalAirportEditText.removeTextChangedListener(mAirportTextWatcher);

		BackgroundDownloader.getInstance().unregisterDownloadCallback(DOWNLOAD_KEY);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		mHockeyPuck.onSaveInstanceState(outState);
	}

	//////////////////////////////////////////////////////////////////////////
	// View control

	// It is surprisingly difficult to get rid of an EditText's focus.  This
	// method should be called when you want to ensure that the EditTexts on
	// the screen no longer have focus.
	private void clearEditTextFocus() {
		EditText textWithFocus = null;

		if (mDepartureAirportEditText.hasFocus()) {
			textWithFocus = mDepartureAirportEditText;
		}
		else if (mArrivalAirportEditText.hasFocus()) {
			textWithFocus = mArrivalAirportEditText;
		}

		if (textWithFocus != null) {
			mFocusStealer.requestFocus();

			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(textWithFocus.getWindowToken(), 0);
		}

		resetAirportEditTexts();
	}

	private static final String DATE_FORMAT = "MMM d";

	private void updateDateButton() {
		// It's always round trip at this point, but at some point we need to handle no params
		// (or one way) too.
		FlightSearchParams params = Db.getFlightSearch().getSearchParams();

		if (params.isRoundTrip()) {
			CharSequence start = DateFormat.format(DATE_FORMAT, params.getDepartureDate().getCalendar());
			CharSequence end = DateFormat.format(DATE_FORMAT, params.getReturnDate().getCalendar());
			mDatesButton.setText(getString(R.string.round_trip_TEMPLATE, start, end));
		}
		else {
			CharSequence start = DateFormat.format(DATE_FORMAT, params.getDepartureDate().getCalendar());
			mDatesButton.setText(getString(R.string.one_way_TEMPLATE, start));
		}
	}

	private void expandAirportEditText(View focusView) {
		final View expandingView;
		final View shrinkingView;

		if (focusView == mDepartureAirportEditText) {
			expandingView = mDepartureAirportEditText;
			shrinkingView = mArrivalAirportEditText;
		}
		else {
			expandingView = mArrivalAirportEditText;
			shrinkingView = mDepartureAirportEditText;
		}

		// Show an animation to expand/collapse one or the other.
		final LinearLayout.LayoutParams expandingLayoutParams = (LinearLayout.LayoutParams) expandingView
				.getLayoutParams();
		final LinearLayout.LayoutParams shrinkingLayoutParams = (LinearLayout.LayoutParams) shrinkingView
				.getLayoutParams();
		final float halfWeight = mAirportsContainer.getWeightSum() / 2;
		final float startVal = expandingLayoutParams.weight - halfWeight;
		final float endVal = halfWeight * EDITTEXT_EXPANSION_RATIO;

		ValueAnimator anim = ValueAnimator.ofFloat(startVal, endVal);
		anim.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animator) {
				float val = (Float) animator.getAnimatedValue();
				expandingLayoutParams.weight = halfWeight + val;
				shrinkingLayoutParams.weight = halfWeight - val;
				mAirportsContainer.requestLayout();
			}
		});
		anim.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
		anim.start();
	}

	private void resetAirportEditTexts() {
		// Animate both views back to 50/50
		final float halfWeight = mAirportsContainer.getWeightSum() / 2;

		final LinearLayout.LayoutParams expandingLayoutParams;
		final LinearLayout.LayoutParams shrinkingLayoutParams;

		if (((LinearLayout.LayoutParams) mDepartureAirportEditText.getLayoutParams()).weight > halfWeight) {
			expandingLayoutParams = (LinearLayout.LayoutParams) mArrivalAirportEditText.getLayoutParams();
			shrinkingLayoutParams = (LinearLayout.LayoutParams) mDepartureAirportEditText.getLayoutParams();
		}
		else {
			expandingLayoutParams = (LinearLayout.LayoutParams) mDepartureAirportEditText.getLayoutParams();
			shrinkingLayoutParams = (LinearLayout.LayoutParams) mArrivalAirportEditText.getLayoutParams();
		}

		float startVal = expandingLayoutParams.weight - halfWeight;

		// If they weren't already focused, don't bother with this animation
		if (Math.abs(startVal) < .001) {
			return;
		}

		ValueAnimator anim = ValueAnimator.ofFloat(expandingLayoutParams.weight - halfWeight, 0);
		anim.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animator) {
				float val = (Float) animator.getAnimatedValue();
				expandingLayoutParams.weight = halfWeight + val;
				shrinkingLayoutParams.weight = halfWeight - val;
				mAirportsContainer.requestLayout();
			}
		});
		anim.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
		anim.start();
	}

	//////////////////////////////////////////////////////////////////////////
	// Fragment control

	/**
	 * Sets the currently displayed Fragment in the content pane.
	 * 
	 * The tag determines which Fragment is shown.  If it's null,
	 * then all Fragments will be detached from the content pane.
	 * 
	 * If the tag Fragment is already being displayed, nothing will
	 * happen.  
	 * 
	 * @param tag the fragment to display, or null to display none
	 * @return the Fragment to be loaded into the content pane
	 */
	private Fragment setFragment(String tag) {
		FragmentManager fm = getSupportFragmentManager();

		Fragment newFragment = fm.findFragmentByTag(tag);
		Fragment currentFragment = fm.findFragmentById(R.id.content_frame);

		// It's already on the correct fragment, don't do anything
		if (newFragment != null && currentFragment != null && newFragment == currentFragment) {
			return currentFragment;
		}

		// If we're doing a download and we're NOT setting the status tag, cancel the download
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (tag != null && !tag.equals(TAG_STATUS) && bd.isDownloading(DOWNLOAD_KEY)) {
			bd.cancelDownload(DOWNLOAD_KEY);
		}

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

		ft.setCustomAnimations(R.anim.slide_up, R.anim.slide_down);

		if (currentFragment != null) {
			ft.detach(currentFragment);
		}

		if (tag != null) {
			if (newFragment == null) {
				if (tag.equals(TAG_AIRPORT_PICKER)) {
					newFragment = new AirportPickerFragment();
				}
				else if (tag.equals(TAG_DATE_PICKER)) {
					FlightSearchParams params = Db.getFlightSearch().getSearchParams();
					CalendarDialogFragment fragment;
					if (params.isRoundTrip()) {
						fragment = CalendarDialogFragment.newInstance(params.getDepartureDate()
								.getCalendar(), params.getReturnDate().getCalendar());
					}
					else {
						fragment = CalendarDialogFragment.newInstance(params.getDepartureDate()
								.getCalendar(), null);
					}
					fragment.setShowsDialog(false);
					newFragment = fragment;
				}
				else if (tag.equals(TAG_PASSENGER_PICKER)) {
					newFragment = new PassengerPickerFragment();
				}
				else if (tag.equals(TAG_STATUS)) {
					newFragment = new StatusFragment();
				}

				ft.add(R.id.content_frame, newFragment, tag);
			}
			else {
				ft.attach(newFragment);
			}
		}

		ft.commit();

		return newFragment;
	}

	private void showLoading() {
		StatusFragment fragment = (StatusFragment) setFragment(TAG_STATUS);
		fragment.showLoading(getString(R.string.loading_flights));
	}

	private void showError(CharSequence errorText) {
		StatusFragment fragment = (StatusFragment) setFragment(TAG_STATUS);
		fragment.showError(errorText);
	}

	//////////////////////////////////////////////////////////////////////////
	// Action bar

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_flight_search, menu);

		DebugMenu.onCreateOptionsMenu(this, menu);

		mHockeyPuck.onCreateOptionsMenu(menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		DebugMenu.onPrepareOptionsMenu(this, menu);

		mHockeyPuck.onPrepareOptionsMenu(menu);

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.search:
			if (!BackgroundDownloader.getInstance().isDownloading(DOWNLOAD_KEY)) {
				clearEditTextFocus();
				showLoading();
				BackgroundDownloader.getInstance().startDownload(DOWNLOAD_KEY, mDownload, mDownloadCallback);
			}
			return true;
		case R.id.settings:
			Intent intent = new Intent(this, ExpediaBookingPreferenceActivity.class);
			startActivity(intent);
			return true;
		}

		if (DebugMenu.onOptionsItemSelected(this, item) || mHockeyPuck.onOptionsItemSelected(item)) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	//////////////////////////////////////////////////////////////////////////
	// Airport textview config

	private TextWatcher mAirportTextWatcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			setAirportPickerFilter(s);
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			// Do nothing
		}

		@Override
		public void afterTextChanged(Editable s) {
			// Do nothing
		}
	};

	private void setAirportPickerFilter(CharSequence s) {
		AirportPickerFragment fragment = Ui.findSupportFragment(this, TAG_AIRPORT_PICKER);
		if (fragment != null) {
			fragment.filter(s);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// AirportPickerFragmentListener

	@Override
	public void onAirportClick(String airportCode) {
		FlightSearchParams params = Db.getFlightSearch().getSearchParams();
		if (mDepartureAirportEditText.hasFocus()) {
			params.setDepartureAirportCode(airportCode);
			mDepartureAirportEditText.setText(airportCode);
			mArrivalAirportEditText.requestFocus();
		}
		else {
			params.setArrivalAirportCode(airportCode);
			mArrivalAirportEditText.setText(airportCode);

			// Act like they just clicked the dates button
			mDatesButton.performClick();
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// CalendarDialogFragmentListener

	@Override
	public void onChangeDates(Calendar start, Calendar end) {
		FlightSearchParams params = Db.getFlightSearch().getSearchParams();
		params.setDepartureDate(new Date(start));
		if (end != null) {
			params.setReturnDate(new Date(end));
		}
		else {
			params.setReturnDateEmpty();
		}
		updateDateButton();
	}

	//////////////////////////////////////////////////////////////////////////
	// Downloads

	private Download<FlightSearchResponse> mDownload = new Download<FlightSearchResponse>() {
		@Override
		public FlightSearchResponse doDownload() {
			ExpediaServices services = new ExpediaServices(FlightSearchActivity.this);
			BackgroundDownloader.getInstance().addDownloadListener(DOWNLOAD_KEY, services);
			return services.flightSearch(Db.getFlightSearch().getSearchParams(), 0);
		}
	};

	private OnDownloadComplete<FlightSearchResponse> mDownloadCallback = new OnDownloadComplete<FlightSearchResponse>() {
		@Override
		public void onDownload(FlightSearchResponse response) {
			Log.i("Finished flights download!");

			FlightSearch search = Db.getFlightSearch();
			search.setSearchResponse(response);

			if (response == null) {
				showError(getString(R.string.error_server));
			}
			else if (response.hasErrors()) {
				showError(getString(R.string.error_loading_flights_TEMPLATE, response.getErrors().get(0)
						.getPresentableMessage(FlightSearchActivity.this)));
			}
			else if (response.getTripCount() == 0) {
				showError(getString(R.string.error_no_flights_found));
			}
			else {
				startActivity(new Intent(FlightSearchActivity.this, FlightSearchResultsActivity.class));
				setFragment(null);
			}
		}
	};
}
