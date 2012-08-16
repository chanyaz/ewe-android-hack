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
import com.expedia.bookings.model.YoYo;
import com.expedia.bookings.section.ISectionEditable.SectionChangeListener;
import com.expedia.bookings.section.SectionTravelerInfo;
import com.mobiata.android.util.Ui;

public class FlightTravelerInfoOneActivity extends Activity {

	FlightPassenger mPassenger;
	SectionTravelerInfo mSectionTravelerInfo;
	Button mDoneBtn;
	int mPassengerIndex = -1;

	boolean mAttemptToLeaveMade = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_flight_traveler_info_step1);

		mDoneBtn = Ui.findView(this, R.id.done);
		mSectionTravelerInfo = Ui.findView(this, R.id.traveler_info);

		mPassengerIndex = getIntent().getIntExtra(Codes.PASSENGER_INDEX, -1);
		if (mPassengerIndex >= 0) {
			mPassenger = Db.getFlightPassengers().get(mPassengerIndex);
		}

		final YoYo yoyo = getIntent().getParcelableExtra(YoYo.TAG_YOYO);
		mDoneBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mAttemptToLeaveMade = true;
				if (mSectionTravelerInfo.hasValidInput()) {
					Intent intent = yoyo.generateIntent(FlightTravelerInfoOneActivity.this, getIntent());
					startActivity(intent);
				}
				mDoneBtn.setEnabled(mSectionTravelerInfo.hasValidInput());
			}
		});

		mSectionTravelerInfo.addChangeListener(new SectionChangeListener() {
			@Override
			public void onChange() {
				if (mAttemptToLeaveMade) {
					mDoneBtn.setEnabled(mSectionTravelerInfo.hasValidInput());
				}
			}
		});

		if (yoyo != null) {
			if (yoyo.isLast(FlightTravelerInfoOneActivity.class)) {
				//Done
				mDoneBtn.setText(getString(R.string.button_done));
			}
			else {
				//Next
				mDoneBtn.setText(getString(R.string.next));
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		mSectionTravelerInfo.bind(mPassenger);
	}

}
