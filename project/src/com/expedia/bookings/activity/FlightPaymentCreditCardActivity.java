package com.expedia.bookings.activity;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.model.YoYo;
import com.expedia.bookings.section.ISectionEditable.SectionChangeListener;
import com.expedia.bookings.section.SectionBillingInfo;
import com.mobiata.android.util.Ui;

import android.app.Activity;
import android.content.Intent;
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

		final YoYo yoyo = getIntent().getParcelableExtra(YoYo.TAG_YOYO);
		mDoneBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(FlightPaymentCreditCardActivity.this,
						(yoyo == null || yoyo.isEmpty(FlightPaymentCreditCardActivity.class)) ? FlightCheckoutActivity.class: yoyo.popNextTrick(FlightPaymentCreditCardActivity.class));
				intent.putExtra(YoYo.TAG_YOYO, yoyo);
				if(yoyo.isEmpty(FlightPaymentCreditCardActivity.class)){
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				}
				startActivity(intent);
			}
		});
		
		if(yoyo != null){
			if(yoyo.isLast()){
				//Done
				mDoneBtn.setText(getString(R.string.button_done));
			}else{
				//Next
				mDoneBtn.setText(getString(R.string.next));
			}
		}
		
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
		bindAll();
	}

	public void bindAll() {
		mSectionCreditCard.bind(mBillingInfo);
	}

}
