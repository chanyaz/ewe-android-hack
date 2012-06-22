package com.expedia.bookings.activity;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.section.SectionDisplayAddress;
import com.expedia.bookings.section.SectionDisplayContactInfo;
import com.expedia.bookings.section.SectionDisplayCreditCard;
import com.mobiata.android.util.Ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class FlightPaymentSummaryActivity extends Activity {

	BillingInfo mBi;
	SectionDisplayCreditCard mCreditCardSegment;
	SectionDisplayAddress mAddressSegment;
	SectionDisplayContactInfo mContactSegment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_flight_payment_summary);

	}

	@Override
	public void onResume() {
		super.onResume();
		//We should always put this stuff in onResume, as it will set the values correctly if we get here on the back stack or from someplace wierd...
		mBi = Db.getBillingInfo();

		Button saveBtn = Ui.findView(this, R.id.save);
		Button loadBtn = Ui.findView(this, R.id.load);

		mCreditCardSegment = Ui.findView(this, R.id.creditcard_section);
		mAddressSegment = Ui.findView(this, R.id.address_section);
		mContactSegment = Ui.findView(this, R.id.contact_info_section);

		if (mBi.getLocation() == null) {
			mBi.setLocation(new Location());
		}

		bindAll();

		saveBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mBi != null) {
					mBi.save(FlightPaymentSummaryActivity.this);
				}
			}
		});
		loadBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mBi != null) {
					mBi.load(FlightPaymentSummaryActivity.this);
					bindAll();
				}
			}
		});

		mCreditCardSegment.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent editContact = new Intent(FlightPaymentSummaryActivity.this, FlightPaymenyCreditCardActivity.class);
				startActivity(editContact);
			}
		});

		mAddressSegment.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent editContact = new Intent(FlightPaymentSummaryActivity.this, FlightPaymenyAddressActivity.class);
				startActivity(editContact);
			}
		});

		mContactSegment.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent editContact = new Intent(FlightPaymentSummaryActivity.this, FlightPaymenyContactActivity.class);
				startActivity(editContact);
			}
			//
		});
	}

	public void bindAll() {
		mCreditCardSegment.bind(mBi);
		mAddressSegment.bind(mBi.getLocation());
		mContactSegment.bind(mBi);
	}

}
