package com.expedia.bookings.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.fragment.base.TabletCheckoutDataFormFragment;
import com.expedia.bookings.interfaces.ICheckoutDataListener;
import com.expedia.bookings.section.ISectionEditable;
import com.expedia.bookings.section.InvalidCharacterHelper;
import com.expedia.bookings.utils.BookingInfoUtils;
import com.expedia.bookings.section.SectionTravelerInfoTablet;
import com.mobiata.android.util.Ui;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class TabletCheckoutTravelerFormFragment extends TabletCheckoutDataFormFragment {

	public static TabletCheckoutTravelerFormFragment newInstance(LineOfBusiness lob) {
		TabletCheckoutTravelerFormFragment frag = new TabletCheckoutTravelerFormFragment();
		frag.setLob(lob);
		return frag;
	}

	private static final String STATE_TRAVELER_NUMBER = "STATE_TRAVELER_NUMBER";

	private int mTravelerNumber = -1;
	private SectionTravelerInfoTablet mSectionTraveler;
	boolean mAttemptToLeaveMade = false;
	private ICheckoutDataListener mListener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mAttemptToLeaveMade = false;
		mListener = Ui.findFragmentListener(this, ICheckoutDataListener.class);
	}

	@Override
	public void onResume() {
		super.onResume();
		bindToDb(mTravelerNumber);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mTravelerNumber = savedInstanceState.getInt(STATE_TRAVELER_NUMBER, mTravelerNumber);
			if (Db.getWorkingTravelerManager().getAttemptToLoadFromDisk() && Db.getWorkingTravelerManager().hasTravelerOnDisk(getActivity())) {
				Db.getWorkingTravelerManager().loadWorkingTravelerFromDisk(getActivity());
			}
		}
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(STATE_TRAVELER_NUMBER, mTravelerNumber);
	}

	public void bindToDb(int travelerNumber) {
		if (mTravelerNumber != travelerNumber || Db.getWorkingTravelerManager().getWorkingTraveler() == null) {
			Db.getWorkingTravelerManager().setWorkingTravelerAndBase(Db.getTravelers().get(travelerNumber));
		}
		mTravelerNumber = travelerNumber;
		if (mSectionTraveler != null && travelerNumber >= 0 && travelerNumber < Db.getTravelers().size()) {
			mSectionTraveler.bind(Db.getWorkingTravelerManager().getWorkingTraveler());

			setHeadingText(getString(R.string.traveler_information_TEMPLATE, travelerNumber + 1));
			setHeadingButtonText(getString(R.string.done));
			setHeadingButtonOnClick(mHeaderButtonClickListener);
		}
	}

	private OnClickListener mHeaderButtonClickListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			mAttemptToLeaveMade = true;
			if (mSectionTraveler != null && mSectionTraveler.hasValidInput()) {
				Db.getWorkingTravelerManager().commitWorkingTravelerToDB(mTravelerNumber, getActivity());

				BillingInfo billingInfo = Db.getBillingInfo();
				Traveler traveler = Db.getTravelers().get(mTravelerNumber);
				billingInfo.setFirstName(traveler.getFirstName());
				billingInfo.setLastName(traveler.getLastName());
				billingInfo.setTelephone(traveler.getPhoneNumber());
				billingInfo.setTelephoneCountryCode(traveler.getPhoneCountryCode());
				//Save BillingInfo
				billingInfo.save(getActivity());

				String checkoutEmail = BookingInfoUtils.getCheckoutEmail(getActivity(), getLob());
				if (!TextUtils.isEmpty(checkoutEmail)) {
					Db.getBillingInfo().setEmail(checkoutEmail);
				}
				else {
					//TODO this is highly unlikely to happen. Since tablet checkout is still in the works, let's come back here when UI is in place.
					Ui.showToast(getActivity(), R.string.please_enter_a_valid_email_address);
				}

				mListener.onCheckoutDataUpdated();
				clearForm();
				getActivity().onBackPressed();
			}
		}
	};

	private void clearForm() {
		Db.getWorkingTravelerManager().clearWorkingTraveler(getActivity());
		mTravelerNumber = -1;
	}

	@Override
	protected void setUpFormContent(ViewGroup formContainer) {
		//This will probably end up having way more moving parts than this...
		formContainer.removeAllViews();
		if (getLob() == LineOfBusiness.HOTELS) {
			mSectionTraveler = (SectionTravelerInfoTablet) View.inflate(getActivity(),
				R.layout.section_hotel_tablet_edit_traveler, null);
		}
		else if (getLob() == LineOfBusiness.FLIGHTS) {
			mSectionTraveler = (SectionTravelerInfoTablet) View.inflate(getActivity(), R.layout.section_flight_tablet_edit_traveler,
				null);
		}
		mSectionTraveler.setLob(getLob());

		mSectionTraveler.addChangeListener(new ISectionEditable.SectionChangeListener() {
			@Override
			public void onChange() {
				if (mAttemptToLeaveMade) {
					//If we tried to leave, but we had invalid input, we should update the validation feedback with every change
					mSectionTraveler.hasValidInput();
				}

				//We attempt a save on change
				Db.getWorkingTravelerManager().attemptWorkingTravelerSave(getActivity(), false);
			}
		});

		mSectionTraveler.addInvalidCharacterListener(new InvalidCharacterHelper.InvalidCharacterListener() {
			@Override
			public void onInvalidCharacterEntered(CharSequence text, InvalidCharacterHelper.Mode mode) {
				InvalidCharacterHelper.showInvalidCharacterPopup(getFragmentManager(), mode);
			}
		});


		formContainer.addView(mSectionTraveler);

	}

	@Override
	protected void onFormClosed() {
		clearForm();
	}
}
