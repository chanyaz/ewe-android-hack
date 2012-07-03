package com.expedia.bookings.activity;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightPassenger;
import com.expedia.bookings.section.ISectionEditable.SectionChangeListener;
import com.expedia.bookings.section.SectionEditTravelerInfo;
import com.mobiata.android.util.Ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class FlightTravelerInfoTwoActivity extends Activity {

	public final static String PASSENGER_INDEX = "PASSENGER_INDEX";

	FlightPassenger mPassenger;
	SectionEditTravelerInfo mSectionTravelerInfo;
	Button mFinishButton;
	int mPassengerIndex = -1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_flight_traveler_info_step2);

		mFinishButton = Ui.findView(this, R.id.finish);
		mSectionTravelerInfo = Ui.findView(this, R.id.traveler_info);

		mPassengerIndex = getIntent().getIntExtra(PASSENGER_INDEX, -1);
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
