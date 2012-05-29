package com.expedia.bookings.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.fragment.AirportPickerFragment;
import com.expedia.bookings.fragment.AirportPickerFragment.AirportPickerFragmentListener;
import com.expedia.bookings.fragment.CalendarPickerFragment;
import com.expedia.bookings.fragment.PassengerPickerFragment;
import com.expedia.bookings.utils.Ui;

public class FlightSearchActivity extends FragmentActivity implements AirportPickerFragmentListener {

	private static final String TAG_AIRPORT_PICKER = "TAG_AIRPORT_PICKER";
	private static final String TAG_DATE_PICKER = "TAG_DATE_PICKER";
	private static final String TAG_PASSENGER_PICKER = "TAG_PASSENGER_PICKER";

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
				setFragment(TAG_DATE_PICKER);
			}
		});

		// Configure passenger button
		mPassengersButton = Ui.findView(this, R.id.passengers_button);
		mPassengersButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setFragment(TAG_PASSENGER_PICKER);
			}
		});

		// Set initial fragment
		if (savedInstanceState == null) {
			setFragment(TAG_AIRPORT_PICKER);
		}

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
	// Fragment control

	public void setFragment(String tag) {
		FragmentManager fm = getSupportFragmentManager();

		Fragment newFragment = fm.findFragmentByTag(tag);
		Fragment currentFragment = fm.findFragmentById(R.id.content_frame);

		// It's already on the correct fragment, don't do anything
		if (newFragment != null && currentFragment != null && newFragment == currentFragment) {
			return;
		}

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		if (newFragment == null) {
			if (tag.equals(TAG_AIRPORT_PICKER)) {
				newFragment = new AirportPickerFragment();
			}
			else if (tag.equals(TAG_DATE_PICKER)) {
				newFragment = new CalendarPickerFragment();
			}
			else if (tag.equals(TAG_PASSENGER_PICKER)) {
				newFragment = new PassengerPickerFragment();
			}
		}

		// Adds or replaces current content in container
		ft.replace(R.id.content_frame, newFragment);

		// Set a fade transition (for now)
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

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
}
