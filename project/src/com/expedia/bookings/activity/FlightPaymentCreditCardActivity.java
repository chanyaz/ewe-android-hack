package com.expedia.bookings.activity;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.User;
import com.expedia.bookings.model.YoYo;
import com.expedia.bookings.section.ISectionEditable.SectionChangeListener;
import com.expedia.bookings.section.SectionBillingInfo;
import com.mobiata.android.util.Ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class FlightPaymentCreditCardActivity extends SherlockActivity {

	BillingInfo mBillingInfo;

	SectionBillingInfo mSectionCreditCard;

	boolean mAttemptToLeaveMade = false;
	YoYo mYoYo;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(User.isLoggedIn(this)){
			setContentView(R.layout.activity_flight_payment_creditcard_logged_in);
			Db.getBillingInfo().setEmail(Db.getUser().getEmail());//Set the billingInfo email address
		}else{
			setContentView(R.layout.activity_flight_payment_creditcard);
		}

		mBillingInfo = Db.getBillingInfo();

		mSectionCreditCard = Ui.findView(this, R.id.creditcard_section);

		mYoYo = getIntent().getParcelableExtra(YoYo.TAG_YOYO);

		mSectionCreditCard.addChangeListener(new SectionChangeListener() {
			@Override
			public void onChange() {
				if (mAttemptToLeaveMade) {
					//If we tried to leave, but we had invalid input, we should update the validation feedback with every change
					mSectionCreditCard.hasValidInput();
				}
			}
		});

	}

	@Override
	public void onResume() {
		super.onResume();
		bindAll();
	}

	public void bindAll() {
		mSectionCreditCard.bind(mBillingInfo);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = this.getSupportMenuInflater();
		if (mYoYo != null && mYoYo.isLast(this.getClass())) {
			inflater.inflate(R.menu.menu_done, menu);
		}
		else {
			inflater.inflate(R.menu.menu_next, menu);
		}
		menu.findItem(R.id.menu_yoyo).getActionView().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mAttemptToLeaveMade = true;
				if (mSectionCreditCard.hasValidInput()) {
					Intent intent = mYoYo.generateIntent(FlightPaymentCreditCardActivity.this, getIntent());
					startActivity(intent);
				}
			}
		});
		return true;
	}

}
