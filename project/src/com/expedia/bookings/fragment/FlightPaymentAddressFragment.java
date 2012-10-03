package com.expedia.bookings.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.FlightPaymentOptionsActivity.Validatable;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.section.ISectionEditable.SectionChangeListener;
import com.expedia.bookings.section.SectionLocation;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.Ui;

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

		if (savedInstanceState == null) {
			OmnitureTracking.trackPageLoadFlightCheckoutPaymentEditAddress(getActivity());
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_flight_payment_address, container, false);
		mAttemptToLeaveMade = false;
		mSectionLocation = Ui.findView(v, R.id.address_section);

		mBillingInfo = Db.getWorkingBillingInfoManager().getWorkingBillingInfo();

		mSectionLocation.addChangeListener(new SectionChangeListener() {
			@Override
			public void onChange() {
				if (mAttemptToLeaveMade) {
					//If we tried to leave, but we had invalid input, we should update the validation feedback with every change
					mSectionLocation.hasValidInput();
				}
				//Attempt to save on change
				Db.getWorkingBillingInfoManager().attemptWorkingBillingInfoSave(getActivity(), false);
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
		mBillingInfo = Db.getWorkingBillingInfoManager().getWorkingBillingInfo();
		mSectionLocation.bind(mBillingInfo.getLocation());

		View focused = this.getView().findFocus();
		if (focused == null || !(focused instanceof EditText)) {
			focused = Ui.findView(mSectionLocation, R.id.edit_address_line_one);
		}
		final View finalFocused = focused;
		if (finalFocused != null && finalFocused instanceof EditText) {
			finalFocused.postDelayed(new Runnable() {
				@Override
				public void run() {
					//Dumb but effective - show the keyboard by emulating a click on the view
					finalFocused.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(),
							SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
					finalFocused.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(),
							SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));
				}
			}, 200);
		}

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
