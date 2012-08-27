package com.expedia.bookings.activity;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightPassenger;
import com.expedia.bookings.model.YoYo;
import com.expedia.bookings.section.ISectionEditable.SectionChangeListener;
import com.expedia.bookings.section.SectionTravelerInfo;
import com.mobiata.android.util.Ui;

public class FlightTravelerInfoOneActivity extends SherlockActivity {

	FlightPassenger mPassenger;
	SectionTravelerInfo mSectionTravelerInfo;
	int mPassengerIndex = -1;

	boolean mAttemptToLeaveMade = false;
	YoYo mYoYo;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_flight_traveler_info_step1);

		mSectionTravelerInfo = Ui.findView(this, R.id.traveler_info);

		mPassengerIndex = getIntent().getIntExtra(Codes.PASSENGER_INDEX, -1);
		if (mPassengerIndex >= 0) {
			mPassenger = Db.getFlightPassengers().get(mPassengerIndex);
		}

		mYoYo = getIntent().getParcelableExtra(YoYo.TAG_YOYO);

		mSectionTravelerInfo.addChangeListener(new SectionChangeListener() {
			@Override
			public void onChange() {
				if (mAttemptToLeaveMade) {
					//If we tried to leave, but we had invalid input, we should update the validation feedback with every change
					mSectionTravelerInfo.hasValidInput();
				}
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		mSectionTravelerInfo.bind(mPassenger);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = this.getSupportMenuInflater();
	    if(mYoYo != null && mYoYo.isLast(this.getClass())){
	    	inflater.inflate(R.menu.menu_done, menu);
	    }else{
	    	inflater.inflate(R.menu.menu_next, menu);
	    }
	    menu.findItem(R.id.menu_yoyo).getActionView().setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mAttemptToLeaveMade = true;
				if (mSectionTravelerInfo.hasValidInput()) {
					Intent intent = mYoYo.generateIntent(FlightTravelerInfoOneActivity.this, getIntent());
					startActivity(intent);
				}
			}		
		});
	    return true;
	}
	
}
