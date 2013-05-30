package com.expedia.bookings.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.FlightPaymentOptionsActivity.Validatable;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.User;
import com.expedia.bookings.section.ISectionEditable.SectionChangeListener;
import com.expedia.bookings.section.InvalidCharacterHelper.InvalidCharacterListener;
import com.expedia.bookings.section.InvalidCharacterHelper.Mode;
import com.expedia.bookings.section.InvalidCharacterHelper;
import com.expedia.bookings.section.SectionBillingInfo;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.FocusViewRunnable;
import com.expedia.bookings.utils.Ui;

public class FlightPaymentCreditCardFragment extends Fragment implements Validatable {

	private static final String STATE_TAG_ATTEMPTED_LEAVE = "STATE_TAG_ATTEMPTED_LEAVE";

	BillingInfo mBillingInfo;

	SectionBillingInfo mSectionCreditCard;

	boolean mAttemptToLeaveMade = false;

	public static FlightPaymentCreditCardFragment newInstance() {
		FlightPaymentCreditCardFragment fragment = new FlightPaymentCreditCardFragment();
		Bundle args = new Bundle();
		//TODO:Set args here..
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onStart() {
		super.onStart();
		OmnitureTracking.trackPageLoadFlightCheckoutPaymentEditCard(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_flight_payment_creditcard, container, false);

		mAttemptToLeaveMade = savedInstanceState != null ? savedInstanceState.getBoolean(STATE_TAG_ATTEMPTED_LEAVE,
				false) : false;

		mBillingInfo = Db.getWorkingBillingInfoManager().getWorkingBillingInfo();

		if (User.isLoggedIn(getActivity())) {
			mBillingInfo.setEmail(Db.getUser().getPrimaryTraveler().getEmail());
		}

		mSectionCreditCard = Ui.findView(v, R.id.creditcard_section);
		mSectionCreditCard.addChangeListener(new SectionChangeListener() {
			@Override
			public void onChange() {
				if (mAttemptToLeaveMade) {
					//If we tried to leave, but we had invalid input, we should update the validation feedback with every change
					mSectionCreditCard.hasValidInput();
				}
				//Attempt to save on change
				Db.getWorkingBillingInfoManager().attemptWorkingBillingInfoSave(getActivity(), false);
			}
		});

		mSectionCreditCard.addInvalidCharacterListener(new InvalidCharacterListener() {
			@Override
			public void onInvalidCharacterEntered(CharSequence text, Mode mode) {
				InvalidCharacterHelper.showInvalidCharacterPopup(getFragmentManager(), mode);
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
			focused = Ui.findView(mSectionCreditCard, R.id.edit_creditcard_number);
		}
		if (focused != null && focused instanceof EditText) {
			FocusViewRunnable.focusView(this, focused);
		}

		if (User.isLoggedIn(getActivity())) {
			EditText cardHolderName = Ui.findView(mSectionCreditCard, R.id.edit_name_on_card);
			if (TextUtils.isEmpty(cardHolderName.getText())) {
				cardHolderName.setText(Db.getUser().getPrimaryTraveler().getFullName());
			}
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
		return mSectionCreditCard != null ? mSectionCreditCard.hasValidInput() : false;
	}

	public void bindAll() {
		mSectionCreditCard.bind(mBillingInfo);
	}
}
