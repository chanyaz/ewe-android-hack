package com.expedia.bookings.activity;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Location;
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_flight_payment_address);

	}

	@Override
	public void onResume() {
		super.onResume();
		//We should always put this stuff in onResume, as it will set the values correctly if we get here on the back stack or from someplace wierd...
		mBillingInfo = Db.getBillingInfo();

		final SectionLocation sci = Ui.findView(this, R.id.address_section);
		if (mBillingInfo.getLocation() == null) {
			mBillingInfo.setLocation(new Location());
		}
		sci.bind(mBillingInfo.getLocation());

		final Button done = Ui.findView(this, R.id.done);
		done.setEnabled(sci.hasValidInput());

		sci.addChangeListener(new SectionChangeListener() {
			@Override
			public void onChange() {
				if (sci.hasValidInput()) {
					done.setEnabled(sci.hasValidInput());
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
