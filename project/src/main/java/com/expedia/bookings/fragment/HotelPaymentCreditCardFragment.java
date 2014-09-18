package com.expedia.bookings.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.activity.HotelPaymentOptionsActivity.Validatable;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.section.ISectionEditable.SectionChangeListener;
import com.expedia.bookings.section.InvalidCharacterHelper;
import com.expedia.bookings.section.InvalidCharacterHelper.InvalidCharacterListener;
import com.expedia.bookings.section.InvalidCharacterHelper.Mode;
import com.expedia.bookings.section.SectionBillingInfo;
import com.expedia.bookings.section.SectionLocation;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.FocusViewRunnable;
import com.expedia.bookings.utils.Ui;

public class HotelPaymentCreditCardFragment extends Fragment implements Validatable {

	private static final String STATE_TAG_ATTEMPTED_LEAVE = "STATE_TAG_ATTEMPTED_LEAVE";

	BillingInfo mBillingInfo;

	SectionBillingInfo mSectionBillingInfo;
	SectionLocation mSectionLocation;

	boolean mAttemptToLeaveMade = false;

	public static HotelPaymentCreditCardFragment newInstance() {
		return new HotelPaymentCreditCardFragment();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mAttemptToLeaveMade = false;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_hotel_payment_creditcard, container, false);

		if (ExpediaBookingApp.IS_VSC) {
			// 1600. VSC Hide zipCode Field from CCEntry Screen
			View view = Ui.findView(v, R.id.section_location_address);
			view.setVisibility(View.INVISIBLE);

			// 1601. VSC Disable predictive text input for "Name on card" field in CCEntry Screen
			EditText nameOnCard = (EditText)Ui.findView(v, R.id.edit_name_on_card);
			nameOnCard.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		}

		mAttemptToLeaveMade = savedInstanceState != null ? savedInstanceState.getBoolean(STATE_TAG_ATTEMPTED_LEAVE,
				false) : false;

		mBillingInfo = Db.getWorkingBillingInfoManager().getWorkingBillingInfo();

		SectionChangeListener sectionListener = new SectionChangeListener() {
			@Override
			public void onChange() {
				if (mAttemptToLeaveMade) {
					//If we tried to leave, but we had invalid input, we should update the validation feedback with every change
					if (mSectionBillingInfo != null) {
						mSectionBillingInfo.performValidation();
					}
					if (mSectionLocation != null) {
						mSectionLocation.performValidation();
					}
				}
				//Attempt to save on change
				Db.getWorkingBillingInfoManager().attemptWorkingBillingInfoSave(getActivity(), false);
			}
		};

		InvalidCharacterListener invalidCharacterListener = new InvalidCharacterListener() {
			@Override
			public void onInvalidCharacterEntered(CharSequence text, Mode mode) {
				InvalidCharacterHelper.showInvalidCharacterPopup(getFragmentManager(), mode);
			}
		};

		mSectionBillingInfo = Ui.findView(v, R.id.creditcard_section);
		mSectionBillingInfo.setLineOfBusiness(LineOfBusiness.HOTELS);
		mSectionLocation = Ui.findView(v, R.id.section_location_address);
		mSectionLocation.setLineOfBusiness(LineOfBusiness.HOTELS);

		mSectionBillingInfo.addChangeListener(sectionListener);
		mSectionLocation.addChangeListener(sectionListener);

		mSectionBillingInfo.addInvalidCharacterListener(invalidCharacterListener);
		mSectionLocation.addInvalidCharacterListener(invalidCharacterListener);

		return v;
	}

	@Override
	public void onStart() {
		super.onStart();
		OmnitureTracking.trackPageLoadHotelsCheckoutPaymentEditCard(getActivity());
	}

	@Override
	public void onResume() {
		super.onResume();
		mBillingInfo = Db.getWorkingBillingInfoManager().getWorkingBillingInfo();
		bindAll();

		View focused = this.getView().findFocus();
		if (focused == null || !(focused instanceof EditText)) {
			focused = Ui.findView(mSectionBillingInfo, R.id.edit_creditcard_number);
		}
		if (focused != null && focused instanceof EditText) {
			FocusViewRunnable.focusView(this, focused);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(STATE_TAG_ATTEMPTED_LEAVE, mAttemptToLeaveMade);
	}

	/**
	 * Performs validation on the form. We can possibly have both SectionBillingInfo and SectionLocation, so we must
	 * account for the different combinations. SectionLocation is null when it is not required based on the POS, which
	 * ultimately means that the location validation is successful (as it does not exist, heh).
	 */
	@Override
	public boolean attemptToLeave() {
		mAttemptToLeaveMade = true;
		boolean hasValidCreditCard = mSectionBillingInfo != null ? mSectionBillingInfo.performValidation() : false;
		boolean hasValidPaymentLocation = mSectionLocation != null ? mSectionLocation.performValidation() : true;
		return hasValidCreditCard && hasValidPaymentLocation;
	}

	public void bindAll() {
		mSectionBillingInfo.bind(mBillingInfo);
	}

}
