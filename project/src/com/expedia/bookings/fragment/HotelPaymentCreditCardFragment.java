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
import com.expedia.bookings.activity.HotelPaymentOptionsActivity.Validatable;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.section.ISectionEditable.SectionChangeListener;
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

		mSectionBillingInfo = Ui.findView(v, R.id.creditcard_section);
		mSectionBillingInfo.addChangeListener(new SectionChangeListener() {
			@Override
			public void onChange() {
				if (mAttemptToLeaveMade) {
					//If we tried to leave, but we had invalid input, we should update the validation feedback with every change
					mSectionBillingInfo.hasValidInput();
				}
				//Attempt to save on change
				Db.getWorkingBillingInfoManager().attemptWorkingBillingInfoSave(getActivity(), false);
			}
		});

		PointOfSale.RequiredPaymentFields requiredFields = PointOfSale.getPointOfSale()
				.getRequiredPaymentFieldsHotels();
		if (requiredFields.equals(PointOfSale.RequiredPaymentFields.POSTAL_CODE)) {
			// grab reference to the SectionLocation as we will need to perform validation
			mSectionLocation = Ui.findView(v, R.id.section_location_address);
			mSectionLocation.setLineOfBusiness(LineOfBusiness.HOTELS);
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

			// Give US users a streamlined keyboard approach. TODO move this info shared ExpediaConfig json file
			if (PointOfSale.getPointOfSale().getPointOfSaleId() == PointOfSaleId.UNITED_STATES) {
				EditText postalCodeEditText = Ui.findView(v, R.id.edit_address_postal_code);
				postalCodeEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
			}
		}
		else if (requiredFields.equals(PointOfSale.RequiredPaymentFields.NONE)) {
			// remove the SectionLocation/postalCode as it is not needed
			ViewGroup vg = Ui.findView(v, R.id.edit_creditcard_exp_date_and_zipcode_container);
			vg.removeView(Ui.findView(v, R.id.section_location_address));
		}

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
			focused = Ui.findView(mSectionBillingInfo, R.id.edit_creditcard_number);
		}
		if (focused != null && focused instanceof EditText) {
			FocusViewRunnable.focusView(this, focused);
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
		return hasValidInput();
	}

	public void bindAll() {
		mSectionBillingInfo.bind(mBillingInfo);
	}

	/**
	 * Performs validation on the form. We can possibly have both SectionBillingInfo and SectionLocation, so we must
	 * account for the different combinations. SectionLocation is null when it is not required based on the POS, which
	 * ultimately means that the location validation is successful (as it does not exist, heh).
	 */
	private boolean hasValidInput() {
		boolean hasValidCreditCard = mSectionBillingInfo != null ? mSectionBillingInfo.hasValidInput() : false;
		boolean hasValidPaymentLocation = mSectionLocation != null ? mSectionLocation.hasValidInput() : true;
		return hasValidCreditCard && hasValidPaymentLocation;
	}
}
