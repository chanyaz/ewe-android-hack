package com.expedia.bookings.activity;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.model.YoYo;
import com.expedia.bookings.section.ISectionEditable.SectionChangeListener;
import com.expedia.bookings.section.SectionLocation;
import com.mobiata.android.util.Ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class FlightPaymentAddressActivity extends SherlockActivity {

	BillingInfo mBillingInfo;

	SectionLocation mSectionLocation;

	boolean mAttemptToLeaveMade = false;
	YoYo mYoYo;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_flight_payment_address);
		mSectionLocation = Ui.findView(this, R.id.address_section);

		mBillingInfo = Db.getBillingInfo();
		if (mBillingInfo.getLocation() == null) {
			mBillingInfo.setLocation(new Location());
		}
		
		mYoYo = getIntent().getParcelableExtra(YoYo.TAG_YOYO);

		mSectionLocation.addChangeListener(new SectionChangeListener() {
			@Override
			public void onChange() {
				if (mAttemptToLeaveMade) {
					//If we tried to leave, but we had invalid input, we should update the validation feedback with every change
					mSectionLocation.hasValidInput();
				}
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();

		mSectionLocation.bind(mBillingInfo.getLocation());
		
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
				if (mSectionLocation.hasValidInput()) {
					Intent intent = mYoYo.generateIntent(FlightPaymentAddressActivity.this, getIntent());
					startActivity(intent);
				}
			}		
		});
	    return true;
	}

}
