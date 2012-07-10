package com.expedia.bookings.activity;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.model.CheckoutFlowState;
import com.expedia.bookings.section.ISectionEditable.SectionChangeListener;
import com.expedia.bookings.section.SectionBillingInfo;
import com.mobiata.android.util.Ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class FlightPaymentCreditCardActivity extends Activity {

	BillingInfo mBillingInfo;

	SectionBillingInfo mSectionCreditCardNum;
	SectionBillingInfo mSectionCreditCardType;
	SectionBillingInfo mSectionContactInfo;
	Button mDoneBtn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_flight_payment_creditcard);

		mBillingInfo = Db.getBillingInfo();

		mDoneBtn = Ui.findView(this, R.id.done);

		mSectionCreditCardNum = Ui.findView(this, R.id.creditcard_section);
		mSectionCreditCardType = Ui.findView(this, R.id.creditcard_type_section);
		mSectionContactInfo = Ui.findView(this, R.id.contact_info_section);

		mSectionCreditCardNum.addChangeListener(mDoneButtonEnabler);
		mSectionCreditCardNum.addChangeListener(mDoneButtonEnabler);
		mSectionContactInfo.addChangeListener(mDoneButtonEnabler);
		mDoneButtonEnabler.onChange();

		mDoneBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				CheckoutFlowState.getInstance(FlightPaymentCreditCardActivity.this).moveToNextActivityInCheckout(
						FlightPaymentCreditCardActivity.this, mBillingInfo);
			}
		});
	}

	SectionChangeListener mDoneButtonEnabler = new SectionChangeListener() {
		@Override
		public void onChange() {
			mDoneBtn.setEnabled(mSectionCreditCardNum.hasValidInput() && mSectionCreditCardType.hasValidInput()
					&& mSectionContactInfo.hasValidInput());
		}
	};

	@Override
	public void onResume() {
		super.onResume();
		mDoneBtn.setText(CheckoutFlowState.getInstance(this).getFlowButtonText(this, mBillingInfo));
		bindAll();
	}

	public void bindAll() {
		mSectionCreditCardNum.bind(mBillingInfo);
		mSectionCreditCardType.bind(mBillingInfo);
		mSectionContactInfo.bind(mBillingInfo);
	}

}
