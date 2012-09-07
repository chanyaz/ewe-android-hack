package com.expedia.bookings.fragment;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.FlightPaymentOptionsActivity.Validatable;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.section.SectionLocation;
import com.expedia.bookings.section.ISectionEditable.SectionChangeListener;
import com.expedia.bookings.utils.Ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FlightPaymentAddressFragment extends Fragment implements Validatable {

	BillingInfo mBillingInfo;

	SectionLocation mSectionLocation;

	boolean mAttemptToLeaveMade = false;

	public static FlightPaymentAddressFragment newInstance() {
		FlightPaymentAddressFragment fragment = new FlightPaymentAddressFragment();
		Bundle args = new Bundle();
		//TODO:Set args here..
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_flight_payment_address, container, false);
		mAttemptToLeaveMade = false;
		mSectionLocation = Ui.findView(v, R.id.address_section);

		mBillingInfo = Db.getBillingInfo();
		if (mBillingInfo.getLocation() == null) {
			mBillingInfo.setLocation(new Location());
		}

		mSectionLocation.addChangeListener(new SectionChangeListener() {
			@Override
			public void onChange() {
				if (mAttemptToLeaveMade) {
					//If we tried to leave, but we had invalid input, we should update the validation feedback with every change
					mSectionLocation.hasValidInput();
				}
			}
		});

		return v;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mAttemptToLeaveMade = false;
	}

	@Override
	public void onResume() {
		super.onResume();
		mBillingInfo = Db.getBillingInfo();
		mSectionLocation.bind(mBillingInfo.getLocation());
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean attemptToLeave() {
		mAttemptToLeaveMade = true;
		return mSectionLocation != null ? mSectionLocation.hasValidInput() : false;
	}
}
