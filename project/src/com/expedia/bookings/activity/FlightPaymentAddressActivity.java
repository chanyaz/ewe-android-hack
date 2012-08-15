package com.expedia.bookings.activity;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.model.YoYo;
import com.expedia.bookings.section.ISectionEditable.SectionChangeListener;
import com.expedia.bookings.section.SectionLocation;
import com.mobiata.android.util.Ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class FlightPaymentAddressActivity extends Activity {

	BillingInfo mBillingInfo;

	SectionLocation mSectionLocation;
	Button mDoneBtn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_flight_payment_address);
		mDoneBtn = Ui.findView(this, R.id.done);
		mSectionLocation = Ui.findView(this, R.id.address_section);

		mBillingInfo = Db.getBillingInfo();
		if (mBillingInfo.getLocation() == null) {
			mBillingInfo.setLocation(new Location());
		}
		
		final YoYo yoyo = getIntent().getParcelableExtra(YoYo.TAG_YOYO);
		mDoneBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
//				Intent intent = new Intent(FlightPaymentAddressActivity.this,
//						(yoyo == null || yoyo.isEmpty(FlightPaymentAddressActivity.class)) ? FlightPaymentCreditCardActivity.class: yoyo.popNextTrick(FlightPaymentAddressActivity.class));
//				intent.putExtra(YoYo.TAG_YOYO, yoyo);
//				if(yoyo.isEmpty(FlightPaymentAddressActivity.class)){
//					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//					intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//				}
				Intent intent = yoyo.generateIntent(FlightPaymentAddressActivity.this, getIntent());
				startActivity(intent);
			}
		});
		
		if(yoyo != null){
			if(yoyo.isLast(FlightPaymentAddressActivity.class)){
				//Done
				mDoneBtn.setText(getString(R.string.button_done));
			}else{
				//Next
				mDoneBtn.setText(getString(R.string.next));
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		mSectionLocation.bind(mBillingInfo.getLocation());
		mDoneBtn.setEnabled(mSectionLocation.hasValidInput());

		mSectionLocation.addChangeListener(new SectionChangeListener() {
			@Override
			public void onChange() {
				mDoneBtn.setEnabled(mSectionLocation.hasValidInput());
			}

		});
	}

}
