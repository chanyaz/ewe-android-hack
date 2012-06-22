package com.expedia.bookings.activity;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.section.ISectionEditable.SectionChangeListener;
import com.expedia.bookings.section.SectionEditCreditCard;
import com.mobiata.android.util.Ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class FlightPaymenyCreditCardActivity extends Activity {

	BillingInfo mBi;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_flight_payment_creditcard);

	}

	@Override
	public void onResume() {
		super.onResume();
		//We should always put this stuff in onResume, as it will set the values correctly if we get here on the back stack or from someplace wierd...
		mBi = Db.getBillingInfo();

		final SectionEditCreditCard cc = Ui.findView(this, R.id.creditcard_section);
		cc.bind(mBi);

		final Button done = Ui.findView(this, R.id.done);
		done.setEnabled(cc.hasValidInput());

		cc.addChangeListener(new SectionChangeListener() {
			@Override
			public void onChange() {
				if (cc.hasValidInput()) {
					done.setEnabled(cc.hasValidInput());
				}
			}

		});

		done.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

	}

}
