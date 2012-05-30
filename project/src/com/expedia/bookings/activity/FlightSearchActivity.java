package com.expedia.bookings.activity;

import java.util.Calendar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Date;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.fragment.AirportPickerFragment;
import com.expedia.bookings.fragment.AirportPickerFragment.AirportPickerFragmentListener;
import com.expedia.bookings.fragment.CalendarDialogFragment;
import com.expedia.bookings.fragment.CalendarDialogFragment.CalendarDialogFragmentListener;
import com.expedia.bookings.fragment.PassengerPickerFragment;
import com.expedia.bookings.utils.Ui;

public class FlightSearchActivity extends FragmentActivity implements AirportPickerFragmentListener,
		CalendarDialogFragmentListener {

	private static final String TAG_AIRPORT_PICKER = "TAG_AIRPORT_PICKER";
	private static final String TAG_DATE_PICKER = "TAG_DATE_PICKER";
	private static final String TAG_PASSENGER_PICKER = "TAG_PASSENGER_PICKER";

	private View mFocusStealer;
	private EditText mDepartureAirportEditText;
	private EditText mArrivalAirportEditText;
	private Button mDatesButton;
	private View mPassengersButton;

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_flight_search);

		mFocusStealer = Ui.findView(this, R.id.focus_stealer);

		// Configure airport pickers
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
					else if (v == mArrivalAirportEditText) {
						setAirportPickerFilter(mArrivalAirportEditText.getText());
					}
				}
			}
		};

		mDepartureAirportEditText.setOnFocusChangeListener(airportFocusChangeListener);
		mArrivalAirportEditText.setOnFocusChangeListener(airportFocusChangeListener);

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

		// Set initial fragment
		if (savedInstanceState == null) {
			setFragment(TAG_AIRPORT_PICKER);
		}

		updateDateButton();

		getActionBar().setTitle(R.string.search_flights);
	}

	@Override
	protected void onResume() {
		super.onResume();

		mDepartureAirportEditText.addTextChangedListener(mAirportTextWatcher);
		mArrivalAirportEditText.addTextChangedListener(mAirportTextWatcher);
	}

	@Override
	protected void onPause() {
		super.onPause();

		mDepartureAirportEditText.removeTextChangedListener(mAirportTextWatcher);
		mArrivalAirportEditText.removeTextChangedListener(mAirportTextWatcher);
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
	}

	private static final String DATE_FORMAT = "MMM d";

	private void updateDateButton() {
		// It's always round trip at this point, but at some point we need to handle no params
		// (or one way) too.
		FlightSearchParams params = Db.getFlightSearchParams();

		CharSequence start = DateFormat.format(DATE_FORMAT, params.getDepartureDate().getCalendar());
		CharSequence end = DateFormat.format(DATE_FORMAT, params.getReturnDate().getCalendar());

		mDatesButton.setText(getString(R.string.round_trip_TEMPLATE, start, end));
	}

	//////////////////////////////////////////////////////////////////////////
	// Fragment control

	private void setFragment(String tag) {
		FragmentManager fm = getSupportFragmentManager();

		Fragment newFragment = fm.findFragmentByTag(tag);
		Fragment currentFragment = fm.findFragmentById(R.id.content_frame);

		// It's already on the correct fragment, don't do anything
		if (newFragment != null && currentFragment != null && newFragment == currentFragment) {
			return;
		}

		if (newFragment == null) {
			if (tag.equals(TAG_AIRPORT_PICKER)) {
				newFragment = new AirportPickerFragment();
			}
			else if (tag.equals(TAG_DATE_PICKER)) {
				FlightSearchParams params = Db.getFlightSearchParams();
				CalendarDialogFragment fragment = CalendarDialogFragment.newInstance(params.getDepartureDate()
						.getCalendar(), params.getReturnDate().getCalendar());
				fragment.setShowsDialog(false);
				newFragment = fragment;
			}
			else if (tag.equals(TAG_PASSENGER_PICKER)) {
				newFragment = new PassengerPickerFragment();
			}
		}

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

		// Set a fade transition (for now)
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

		// Adds or replaces current content in container
		ft.replace(R.id.content_frame, newFragment, tag);

		ft.commit();
	}

	//////////////////////////////////////////////////////////////////////////
	// Action bar

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_flight_search, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.search:
			startActivity(new Intent(this, FlightSearchResultsActivity.class));
			break;
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
		Fragment currFragment = Ui.findSupportFragment(this, R.id.content_frame);
		if (currFragment != null && currFragment instanceof AirportPickerFragment) {
			AirportPickerFragment airportFragment = (AirportPickerFragment) currFragment;
			airportFragment.filter(s);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// AirportPickerFragmentListener

	@Override
	public void onAirportClick(String airportCode) {
		FlightSearchParams params = Db.getFlightSearchParams();
		if (mDepartureAirportEditText.hasFocus()) {
			params.setDepartureAirportCode(airportCode);
			mDepartureAirportEditText.setText(airportCode);
			mArrivalAirportEditText.requestFocus();
		}
		else {
			params.setArrivalAirportCode(airportCode);
			mArrivalAirportEditText.setText(airportCode);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// CalendarDialogFragmentListener

	@Override
	public void onChangeDates(Calendar start, Calendar end) {
		FlightSearchParams params = Db.getFlightSearchParams();
		params.setDepartureDate(new Date(start));
		params.setReturnDate(new Date(end));
		updateDateButton();
	}
}
