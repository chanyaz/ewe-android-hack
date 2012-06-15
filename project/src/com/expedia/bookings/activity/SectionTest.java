package com.expedia.bookings.activity;

import java.util.Calendar;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.widget.SectionAddress;
import com.expedia.bookings.widget.SectionCreditCard;
import com.mobiata.android.Log;
import com.mobiata.android.util.Ui;

import android.app.Activity;
import android.os.Bundle;

public class SectionTest extends Activity {

	BillingInfo mBi;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_section_test);
		
		
		mBi = new BillingInfo();
		mBi.setLocation(new Location());
		mBi.setNumber("");
		mBi.setSecurityCode("");
		mBi.setExpirationDate(Calendar.getInstance());
		
		SectionCreditCard cc = Ui.findView(this, R.id.creditcard_section);
		cc.bind(mBi);

		SectionAddress sa = Ui.findView(this, R.id.address_section);
		sa.bind(mBi.getLocation());

		
		
		new Thread(new Runnable() {
			public void run() {
				try {
					while (true) {

						Thread.sleep(2000);
						Location mLoc = mBi.getLocation();
						Log.i(mLoc.getStreetAddressString() + " " + mLoc.getCity() + " " + mLoc.getStateCode() + " "
								+ mLoc.getPostalCode());
						Log.i(mBi.getNumber() + " " + mBi.getSecurityCode() + " " + mBi.getExpirationDate().get(Calendar.MONTH) + "/" + mBi.getExpirationDate().get(Calendar.YEAR));
					}
				}
				catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

}
