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

import android.app.AlertDialog;
import android.content.DialogInterface;
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
		if (User.isLoggedIn(this)) {
			setContentView(R.layout.activity_flight_payment_creditcard_logged_in);
			Db.getBillingInfo().setEmail(Db.getUser().getEmail());//Set the billingInfo email address
		}
		else {
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
					if (User.isLoggedIn(FlightPaymentCreditCardActivity.this) && mBillingInfo.getStoredCard() == null) {
						//If we are logged in, and the current card is not stored, we open a dialog asking to save
						AlertDialog.Builder builder = new AlertDialog.Builder(FlightPaymentCreditCardActivity.this);
						builder.setMessage("Are you sure you want to exit?")
								.setCancelable(false)
								.setTitle(R.string.save_billing_info)
								.setMessage(R.string.save_billing_info_message)
								.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										mBillingInfo.setSaveCardToExpediaAccount(true);
										Intent intent = mYoYo.generateIntent(FlightPaymentCreditCardActivity.this,
												getIntent());
										startActivity(intent);
									}
								})
								.setNegativeButton(R.string.dont_save, new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int id) {
										mBillingInfo.setSaveCardToExpediaAccount(false);
										Intent intent = mYoYo.generateIntent(FlightPaymentCreditCardActivity.this,
												getIntent());
										startActivity(intent);
									}
								});
						AlertDialog alert = builder.create();
						alert.show();

					}
					else {
						Intent intent = mYoYo.generateIntent(FlightPaymentCreditCardActivity.this, getIntent());
						startActivity(intent);
					}
				}
			}
		});
		return true;
	}

}
