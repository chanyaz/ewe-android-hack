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

	SectionBillingInfo mSectionCreditCard;
	Button mDoneBtn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_flight_payment_creditcard);

		mBillingInfo = Db.getBillingInfo();

		mDoneBtn = Ui.findView(this, R.id.done);

		mSectionCreditCard = Ui.findView(this, R.id.creditcard_section);

		mSectionCreditCard.addChangeListener(mDoneButtonEnabler);
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
			mDoneBtn.setEnabled(mSectionCreditCard.hasValidInput());
		}
	};

	@Override
	public void onResume() {
		super.onResume();
		mDoneBtn.setText(CheckoutFlowState.getInstance(this).getFlowButtonText(this, mBillingInfo));
		bindAll();
	}

	public void bindAll() {
		mSectionCreditCard.bind(mBillingInfo);
	}

}
