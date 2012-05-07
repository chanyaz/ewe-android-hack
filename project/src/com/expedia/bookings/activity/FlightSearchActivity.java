package com.expedia.bookings.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.fragment.AirlinePickerFragment;
import com.expedia.bookings.fragment.AirlinePickerFragment.AirlinePickerFragmentListener;
import com.expedia.bookings.utils.Ui;

public class FlightSearchActivity extends FragmentActivity implements AirlinePickerFragmentListener {

	private AirlinePickerFragment mAirlinePickerFragment;

	private View mFocusStealer;

	private EditText mDepartureAirportEditText;
	private EditText mArrivalAirportEditText;

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_flight_search);

		mFocusStealer = Ui.findView(this, R.id.focus_stealer);

		mFocusStealer.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					// Detach all fragments
					FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
					ft.detach(mAirlinePickerFragment);
					ft.commit();
				}
			}
		});

		// Configure airport pickers
		mDepartureAirportEditText = Ui.findView(this, R.id.departure_airport_edit_text);
		mArrivalAirportEditText = Ui.findView(this, R.id.arrival_airport_edit_text);

		OnFocusChangeListener airportFocusChangeListener = new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					if (mAirlinePickerFragment.isDetached()) {
						getSupportFragmentManager().beginTransaction().attach(mAirlinePickerFragment).commit();
					}

					if (v == mDepartureAirportEditText) {
						mAirlinePickerFragment.filter(mDepartureAirportEditText.getText());
					}
					else if (v == mArrivalAirportEditText) {
						mAirlinePickerFragment.filter(mArrivalAirportEditText.getText());
					}
				}
			}
		};

		mDepartureAirportEditText.setOnFocusChangeListener(airportFocusChangeListener);
		mArrivalAirportEditText.setOnFocusChangeListener(airportFocusChangeListener);

		// For now, just add the airline picker fragment; at some point we'll want to
		// manage which fragment is shown.
		mAirlinePickerFragment = Ui.findSupportFragment(this, AirlinePickerFragment.TAG);
		if (mAirlinePickerFragment == null) {
			mAirlinePickerFragment = new AirlinePickerFragment();
			getSupportFragmentManager().beginTransaction().add(R.id.content_frame, mAirlinePickerFragment,
					AirlinePickerFragment.TAG).commit();
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
			mAirlinePickerFragment.filter(s);
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

	//////////////////////////////////////////////////////////////////////////
	// AirlinePickerFragmentListener

	@Override
	public void onAirportClick(String airportCode) {
		if (mDepartureAirportEditText.hasFocus()) {
			Db.getFlightSearchParams().setDepartureAirportCode(airportCode);
			mDepartureAirportEditText.setText(airportCode);
			mArrivalAirportEditText.requestFocus();
		}
		else {
			Db.getFlightSearchParams().setArrivalAirportCode(airportCode);
			mArrivalAirportEditText.setText(airportCode);
			mFocusStealer.requestFocus();
		}
	}
}
