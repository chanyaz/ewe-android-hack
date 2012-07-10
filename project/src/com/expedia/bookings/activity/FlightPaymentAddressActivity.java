package com.expedia.bookings.activity;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.model.CheckoutFlowState;
import com.expedia.bookings.section.ISectionEditable.SectionChangeListener;
import com.expedia.bookings.section.SectionLocation;
import com.mobiata.android.util.Ui;

import android.app.Activity;
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
	}

	@Override
	public void onResume() {
		super.onResume();

		mDoneBtn.setText(CheckoutFlowState.getInstance(this).getFlowButtonText(this, mBillingInfo));

		mSectionLocation.bind(mBillingInfo.getLocation());
		mDoneBtn.setEnabled(mSectionLocation.hasValidInput());

		mSectionLocation.addChangeListener(new SectionChangeListener() {
			@Override
			public void onChange() {
				mDoneBtn.setEnabled(mSectionLocation.hasValidInput());
			}

		});

		mDoneBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				CheckoutFlowState.getInstance(FlightPaymentAddressActivity.this).moveToNextActivityInCheckout(
						FlightPaymentAddressActivity.this, mBillingInfo);
			}
		});

	}

}
