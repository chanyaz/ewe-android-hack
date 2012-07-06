package com.expedia.bookings.activity;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
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
	Button mDoneBtn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_flight_payment_creditcard);

		mSectionCreditCardNum = Ui.findView(this, R.id.creditcard_section);
		mSectionCreditCardType = Ui.findView(this, R.id.creditcard_type_section);
		mDoneBtn = Ui.findView(this, R.id.done);

		mSectionCreditCardNum.addChangeListener(new SectionChangeListener() {
			@Override
			public void onChange() {
				mDoneBtn.setEnabled(mSectionCreditCardNum.hasValidInput() && mSectionCreditCardType.hasValidInput());
			}

		});

		mDoneBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();

		mBillingInfo = Db.getBillingInfo();
		bindAll();
	}

	public void bindAll() {
		mSectionCreditCardNum.bind(mBillingInfo);
		mSectionCreditCardType.bind(mBillingInfo);
	}

}
