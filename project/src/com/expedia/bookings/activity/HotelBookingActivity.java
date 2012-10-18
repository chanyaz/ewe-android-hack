package com.expedia.bookings.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.fragment.BlurredBackgroundFragment;
import com.expedia.bookings.fragment.BookingInProgressDialogFragment;
import com.expedia.bookings.fragment.CVVEntryFragment;
import com.expedia.bookings.fragment.CVVEntryFragment.CVVEntryFragmentListener;
import com.expedia.bookings.utils.Ui;

public class HotelBookingActivity extends SherlockFragmentActivity implements CVVEntryFragmentListener {
	private static final String DOWNLOAD_KEY = "com.expedia.bookings.hotel.checkout";

	private static final String STATE_CVV_ERROR_MODE = "STATE_CVV_ERROR_MODE";

	private static final int DIALOG_CALLBACK_INVALID_CC = 1;
	private static final int DIALOG_CALLBACK_EXPIRED_CC = 2;

	private Context mContext;

	private CVVEntryFragment mCVVEntryFragment;
	private BookingInProgressDialogFragment mProgressFragment;

	private boolean mCvvErrorModeEnabled;

	private BlurredBackgroundFragment mBgFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = this;

		if (savedInstanceState != null) {
			mCvvErrorModeEnabled = savedInstanceState.getBoolean(STATE_CVV_ERROR_MODE);
		}

		setContentView(R.layout.activity_hotel_booking);

		setTitle(R.string.title_complete_booking);

		mCVVEntryFragment = Ui.findSupportFragment(this, CVVEntryFragment.TAG);
		mProgressFragment = Ui.findSupportFragment(this, BookingInProgressDialogFragment.TAG);

		if (savedInstanceState == null) {
			// Determine the data displayed on the CVVEntryFragment
			BillingInfo billingInfo = Db.getBillingInfo();
			StoredCreditCard cc = billingInfo.getStoredCard();

			String personName;
			String cardName;
			if (cc != null) {
				Traveler traveler = Db.getTravelers().get(0);
				personName = traveler.getFirstName() + " " + traveler.getLastName();

				cardName = cc.getDescription();
			}
			else {
				personName = billingInfo.getNameOnCard();

				String ccNumber = billingInfo.getNumber();
				cardName = getString(R.string.card_ending_TEMPLATE, ccNumber.substring(ccNumber.length() - 4));
			}

			mCVVEntryFragment = CVVEntryFragment.newInstance(personName, cardName);

			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.add(R.id.cvv_frame, mCVVEntryFragment, CVVEntryFragment.TAG);
			ft.commit();
		}

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	//////////////////////////////////////////////////////////////////////////
	// Action bar

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home: {
			finish();
			break;
		}
		}

		return super.onOptionsItemSelected(item);
	}

	// CVVEntryFragmentListener implementation

	@Override
	public void onBook(String cvv) {

	}
}