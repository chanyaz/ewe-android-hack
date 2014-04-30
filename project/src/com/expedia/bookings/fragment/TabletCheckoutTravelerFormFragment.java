package com.expedia.bookings.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.fragment.base.TabletCheckoutDataFormFragment;
import com.expedia.bookings.interfaces.ICheckoutDataListener;
import com.expedia.bookings.section.ISectionEditable;
import com.expedia.bookings.section.InvalidCharacterHelper;
import com.expedia.bookings.section.SectionTravelerInfoTablet;
import com.expedia.bookings.section.TravelerAutoCompleteAdapter;
import com.expedia.bookings.utils.BookingInfoUtils;
import com.mobiata.android.util.Ui;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class TabletCheckoutTravelerFormFragment extends TabletCheckoutDataFormFragment implements InputFilter {

	public static TabletCheckoutTravelerFormFragment newInstance(LineOfBusiness lob) {
		TabletCheckoutTravelerFormFragment frag = new TabletCheckoutTravelerFormFragment();
		frag.setLob(lob);
		return frag;
	}

	private static final String STATE_TRAVELER_NUMBER = "STATE_TRAVELER_NUMBER";
	private static final String STATE_FORM_IS_OPEN = "STATE_FORM_IS_OPEN";

	private int mTravelerNumber = -1;
	private String mHeaderString;
	private SectionTravelerInfoTablet mSectionTraveler;
	private AutoCompleteTextView mFirstNameTravelerAutoComplete;
	private TravelerAutoCompleteAdapter mTravelerAdapter;
	private boolean mAttemptToLeaveMade = false;
	private ICheckoutDataListener mListener;
	private boolean mFormOpen = false;

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
			if (Db.getWorkingTravelerManager().getAttemptToLoadFromDisk() && Db.getWorkingTravelerManager()
				.hasTravelerOnDisk(getActivity())) {
				Db.getWorkingTravelerManager().loadWorkingTravelerFromDisk(getActivity());
			}
			mFormOpen = savedInstanceState.getBoolean(STATE_FORM_IS_OPEN, false);
		}

		mTravelerAdapter = new TravelerAutoCompleteAdapter(getActivity());

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

			setHeadingText(mHeaderString);
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

				String checkoutEmail = BookingInfoUtils.getCheckoutEmail(getActivity(), getLob());
				if (!TextUtils.isEmpty(checkoutEmail)) {
					billingInfo.setEmail(checkoutEmail);
				}
				else {
					//TODO this is highly unlikely to happen. Since tablet checkout is still in the works, let's come back here when UI is in place.
					billingInfo.setEmail(null);
					Ui.showToast(getActivity(), R.string.please_enter_a_valid_email_address);
				}

				//Save BillingInfo
				billingInfo.save(getActivity());

				mListener.onCheckoutDataUpdated();
				clearForm();

				Ui.hideKeyboard(getActivity(), InputMethodManager.HIDE_NOT_ALWAYS);
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
			mSectionTraveler = (SectionTravelerInfoTablet) View
				.inflate(getActivity(), R.layout.section_flight_tablet_edit_traveler,
					null);

			//Here we setup our flights specific animations, namely just

			final View seatingPrefBtn = Ui.findView(mSectionTraveler, R.id.seating_pref_btn);
			final View specialAssistanceBtn = Ui.findView(mSectionTraveler, R.id.special_assistance_btn);
			final View redressBtn = Ui.findView(mSectionTraveler, R.id.redress_btn);

			final View seatPrefField = Ui.findView(mSectionTraveler, R.id.edit_seat_preference_spinner);
			final View specialAssistanceField = Ui.findView(mSectionTraveler, R.id.edit_assistance_preference_spinner);
			final View redressField = Ui.findView(mSectionTraveler, R.id.edit_redress_number);

			final View okBtn = Ui.findView(mSectionTraveler, R.id.ok_btn);

			okBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					okBtn.setVisibility(View.GONE);
					seatPrefField.setVisibility(View.GONE);
					specialAssistanceField.setVisibility(View.GONE);
					redressField.setVisibility(View.GONE);
					seatingPrefBtn.setVisibility(View.VISIBLE);
					specialAssistanceBtn.setVisibility(View.VISIBLE);
					redressBtn.setVisibility(View.VISIBLE);
				}
			});

			seatingPrefBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					okBtn.setVisibility(View.VISIBLE);
					seatPrefField.setVisibility(View.VISIBLE);
					specialAssistanceField.setVisibility(View.GONE);
					redressField.setVisibility(View.GONE);
					seatingPrefBtn.setVisibility(View.GONE);
					specialAssistanceBtn.setVisibility(View.GONE);
					redressBtn.setVisibility(View.GONE);
				}
			});

			specialAssistanceBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					okBtn.setVisibility(View.VISIBLE);
					seatPrefField.setVisibility(View.GONE);
					specialAssistanceField.setVisibility(View.VISIBLE);
					redressField.setVisibility(View.GONE);
					seatingPrefBtn.setVisibility(View.GONE);
					specialAssistanceBtn.setVisibility(View.GONE);
					redressBtn.setVisibility(View.GONE);
				}
			});


			redressBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					okBtn.setVisibility(View.VISIBLE);
					seatPrefField.setVisibility(View.GONE);
					specialAssistanceField.setVisibility(View.GONE);
					redressField.setVisibility(View.VISIBLE);
					seatingPrefBtn.setVisibility(View.GONE);
					specialAssistanceBtn.setVisibility(View.GONE);
					redressBtn.setVisibility(View.GONE);
				}
			});
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

		//We set up the AutoComplete view, but we let the focus
		EditText editName = Ui.findView(mSectionTraveler, R.id.edit_first_name);
		if (editName != null && editName instanceof AutoCompleteTextView) {
			mFirstNameTravelerAutoComplete = (AutoCompleteTextView) editName;
			//Create a filter that
			InputFilter[] filters = new InputFilter[] { this };
			mFirstNameTravelerAutoComplete.setFilters(filters);
			mFirstNameTravelerAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Traveler trav = mTravelerAdapter.getItem(position);
					if (trav != null) {
						Db.getWorkingTravelerManager().setWorkingTravelerAndBase(trav);
						bindToDb(mTravelerNumber);
					}
				}
			});
		}

		formContainer.addView(mSectionTraveler);
	}

	public void setHeaderText(String headerString) {
		mHeaderString = headerString;
	}

	@Override
	protected void onFormClosed() {
		if (mFormOpen) {
			mFirstNameTravelerAutoComplete.dismissDropDown();
			mFirstNameTravelerAutoComplete.setAdapter((ArrayAdapter<Traveler>) null);

			clearForm();
		}
		mFormOpen = false;
	}

	@Override
	public void onFormOpened() {
		if (!mFormOpen || mFirstNameTravelerAutoComplete.hasFocus()) {
			mFirstNameTravelerAutoComplete.setOnFocusChangeListener(mNameFocusListener);
			mFirstNameTravelerAutoComplete.requestFocus();//<-- This fires the onFocusChangeListener
			mFirstNameTravelerAutoComplete.postDelayed(new Runnable() {
				@Override
				public void run() {
					Context context = getActivity();
					if (context != null) {
						InputMethodManager imm = (InputMethodManager) context
							.getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.showSoftInput(mFirstNameTravelerAutoComplete, 0);
					}
				}
			}, 250);
		}
		mFormOpen = true;
	}

	//////////////////////////////////////////////////////////////////////////
	// Focus listener

	private View.OnFocusChangeListener mNameFocusListener = new View.OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (mFirstNameTravelerAutoComplete != null) {
				if (hasFocus) {
					mFirstNameTravelerAutoComplete.setAdapter(mTravelerAdapter);
					mFirstNameTravelerAutoComplete.showDropDown();
				}
				else {
					mFirstNameTravelerAutoComplete.dismissDropDown();
					mFirstNameTravelerAutoComplete.setAdapter((ArrayAdapter<Traveler>) null);

				}
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// InputFilter

	@Override
	public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
		return null;
	}
}
