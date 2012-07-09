package com.expedia.bookings.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightPassenger;
import com.expedia.bookings.section.ISectionEditable.SectionChangeListener;
import com.expedia.bookings.section.SectionTravelerInfo;
import com.mobiata.android.Log;
import com.mobiata.android.util.Ui;

public class FlightTravelerInfoOneActivity extends Activity {
 
	FlightPassenger mPassenger;
	SectionTravelerInfo mSectionTravelerInfo;
	Button mNextButton;
	int mPassengerIndex = -1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_flight_traveler_info_step1);

		mNextButton = Ui.findView(this, R.id.next);
		mSectionTravelerInfo = Ui.findView(this, R.id.traveler_info);

		mPassengerIndex = getIntent().getIntExtra(Codes.PASSENGER_INDEX, -1);
		if (mPassengerIndex >= 0) {
			mPassenger = Db.getFlightPassengers().get(mPassengerIndex);
		}

		mNextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent nextIntent = new Intent(FlightTravelerInfoOneActivity.this, FlightTravelerInfoTwoActivity.class);
				nextIntent.fillIn(getIntent(), 0);
				startActivity(nextIntent);
			}
		});

		mSectionTravelerInfo.addChangeListener(new SectionChangeListener() {
			@Override
			public void onChange() {
				Log.i("mSectionTravelerInfo onChange");
				
				mNextButton.setEnabled(mSectionTravelerInfo.hasValidInput());
				
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();

		mSectionTravelerInfo.bind(mPassenger);

	}

}
