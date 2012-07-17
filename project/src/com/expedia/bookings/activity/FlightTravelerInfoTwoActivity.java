package com.expedia.bookings.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightPassenger;
import com.expedia.bookings.section.ISectionEditable.SectionChangeListener;
import com.expedia.bookings.section.SectionTravelerInfo;
import com.mobiata.android.util.Ui;

public class FlightTravelerInfoTwoActivity extends Activity {

	FlightPassenger mPassenger;
	SectionTravelerInfo mSectionTravelerInfo;
	Button mFinishButton;
	int mPassengerIndex = -1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_flight_traveler_info_step2);

		mFinishButton = Ui.findView(this, R.id.finish);
		mSectionTravelerInfo = Ui.findView(this, R.id.traveler_info);

		//TODO:Determine if this is a domestic flight or not...
		boolean domesticFlight = false;
		if (domesticFlight) {
			//If this is a domestic flight we don't need the passport country so we remove it.
			View passportCountryLabel = Ui.findView(mSectionTravelerInfo, R.id.passport_country_label);
			View passportCountrySpinner = Ui.findView(mSectionTravelerInfo, R.id.edit_passport_country_spinner);

			ViewGroup labelParent = (ViewGroup) passportCountryLabel.getParent();
			ViewGroup spinnerParent = (ViewGroup) passportCountrySpinner.getParent();

			labelParent.removeView(passportCountryLabel);
			spinnerParent.removeView(passportCountrySpinner);
		}

		mPassengerIndex = getIntent().getIntExtra(Codes.PASSENGER_INDEX, -1);
		if (mPassengerIndex >= 0) {
			mPassenger = Db.getFlightPassengers().get(mPassengerIndex);
		}

		mFinishButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent backToCheckoutIntent = new Intent(FlightTravelerInfoTwoActivity.this,
						FlightCheckoutActivity.class);
				backToCheckoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				backToCheckoutIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(backToCheckoutIntent);
			}
		});

		mSectionTravelerInfo.addChangeListener(new SectionChangeListener() {
			@Override
			public void onChange() {
				if (mSectionTravelerInfo.hasValidInput()) {
					mFinishButton.setEnabled(mSectionTravelerInfo.hasValidInput());
				}
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		mSectionTravelerInfo.bind(mPassenger);

	}

}
