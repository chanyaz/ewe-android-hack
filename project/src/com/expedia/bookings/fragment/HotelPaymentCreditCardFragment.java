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
import com.expedia.bookings.activity.HotelPaymentOptionsActivity.Validatable;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.User;
import com.expedia.bookings.section.ISectionEditable.SectionChangeListener;
import com.expedia.bookings.section.SectionBillingInfo;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.Ui;

public class HotelPaymentCreditCardFragment extends Fragment implements Validatable {

	private static final String STATE_TAG_ATTEMPTED_LEAVE = "STATE_TAG_ATTEMPTED_LEAVE";

	BillingInfo mBillingInfo;

	SectionBillingInfo mHotelSectionCreditCard;

	boolean mAttemptToLeaveMade = false;

	public static HotelPaymentCreditCardFragment newInstance() {
		HotelPaymentCreditCardFragment fragment = new HotelPaymentCreditCardFragment();
		Bundle args = new Bundle();
		//TODO:Set args here..
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onStart() {
		super.onStart();
		OmnitureTracking.trackPageLoadHotelsCheckoutPaymentEditCard(getActivity());
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(User.isLoggedIn(getActivity()) ? R.layout.fragment_hotel_payment_creditcard_logged_in
				: R.layout.fragment_hotel_payment_creditcard, container, false);

		mAttemptToLeaveMade = savedInstanceState != null ? savedInstanceState.getBoolean(STATE_TAG_ATTEMPTED_LEAVE,
				false) : false;

		mBillingInfo = Db.getWorkingBillingInfoManager().getWorkingBillingInfo();

		if (User.isLoggedIn(getActivity())) {
			mBillingInfo.setEmail(Db.getUser().getPrimaryTraveler().getEmail());
		}

		mHotelSectionCreditCard = Ui.findView(v, R.id.creditcard_section);
		mHotelSectionCreditCard.addChangeListener(new SectionChangeListener() {
			@Override
			public void onChange() {
				if (mAttemptToLeaveMade) {
					//If we tried to leave, but we had invalid input, we should update the validation feedback with every change
					mHotelSectionCreditCard.hasValidInput();
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
		bindAll();

		View focused = this.getView().findFocus();
		if (focused == null || !(focused instanceof EditText)) {
			focused = Ui.findView(mHotelSectionCreditCard, R.id.edit_creditcard_number);
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
		outState.putBoolean(STATE_TAG_ATTEMPTED_LEAVE, mAttemptToLeaveMade);
	}

	@Override
	public boolean attemptToLeave() {
		mAttemptToLeaveMade = true;
		return mHotelSectionCreditCard != null ? mHotelSectionCreditCard.hasValidInput() : false;
	}

	public void bindAll() {
		mHotelSectionCreditCard.bind(mBillingInfo);
	}
}
