package com.expedia.bookings.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
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
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.fragment.base.TabletCheckoutDataFormFragment;
import com.expedia.bookings.interfaces.ICheckoutDataListener;
import com.expedia.bookings.section.ISectionEditable;
import com.expedia.bookings.section.SectionBillingInfo;
import com.expedia.bookings.section.SectionLocation;
import com.expedia.bookings.section.StoredCreditCardAutoCompleteAdapter;
import com.mobiata.android.util.Ui;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class TabletCheckoutPaymentFormFragment extends TabletCheckoutDataFormFragment implements InputFilter {

	public static TabletCheckoutPaymentFormFragment newInstance(LineOfBusiness lob) {
		TabletCheckoutPaymentFormFragment frag = new TabletCheckoutPaymentFormFragment();
		frag.setLob(lob);
		return frag;
	}

	private static final String STATE_FORM_IS_OPEN = "STATE_FORM_IS_OPEN";

	private AutoCompleteTextView mCreditCardNumberAutoComplete;
	private StoredCreditCardAutoCompleteAdapter mStoredCreditCardAdapter;
	private boolean mAttemptToLeaveMade = false;
	private SectionBillingInfo mSectionBillingInfo;
	private SectionLocation mSectionLocation;
	private ICheckoutDataListener mListener;
	private boolean mFormOpen = false;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mAttemptToLeaveMade = false;
		mListener = Ui.findFragmentListener(this, ICheckoutDataListener.class);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			if (Db.getWorkingBillingInfoManager().getAttemptToLoadFromDisk() && Db.getWorkingBillingInfoManager()
				.hasBillingInfoOnDisk(getActivity())) {
				Db.getWorkingBillingInfoManager().loadWorkingBillingInfoFromDisk(getActivity());
			}
			mFormOpen = savedInstanceState.getBoolean(STATE_FORM_IS_OPEN, false);
		}
		mStoredCreditCardAdapter = new StoredCreditCardAutoCompleteAdapter(getActivity());
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
		bindToDb();
		if (mFormOpen) {
			onFormOpened();
		}
		else {
			onFormClosed();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(STATE_FORM_IS_OPEN, mFormOpen);
	}


	public void bindToDb() {
		if (mSectionBillingInfo != null) {
			BillingInfo info = new BillingInfo(Db.getBillingInfo());
			info.setStoredCard(null);
			Db.getWorkingBillingInfoManager().setWorkingBillingInfoAndBase(info);
			mSectionBillingInfo.bind(Db.getWorkingBillingInfoManager().getWorkingBillingInfo());
		}
		setHeadingText(getString(R.string.payment_method));
		setHeadingButtonText(getString(R.string.done));
		setHeadingButtonOnClick(mTopRightClickListener);

		if (mSectionLocation != null) {
			mSectionLocation.bind(Db.getWorkingBillingInfoManager().getWorkingBillingInfo().getLocation());
		}

	}

	private OnClickListener mTopRightClickListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			mAttemptToLeaveMade = true;
			if (mSectionBillingInfo != null && mSectionLocation != null && mSectionBillingInfo.hasValidInput()
				&& mSectionLocation.hasValidInput()) {
				commitAndLeave();
			}

		}
	};

	private void commitAndLeave() {
		Db.getWorkingBillingInfoManager().commitWorkingBillingInfoToDB();
		mListener.onCheckoutDataUpdated();
		Ui.hideKeyboard(getActivity(), InputMethodManager.HIDE_NOT_ALWAYS);
		closeForm(true);
	}

	protected void setUpFormContent(ViewGroup formContainer) {
		//This will probably end up having way more moving parts than this...
		formContainer.removeAllViews();

		//Add a focus stealer
		View view = new View(getActivity());
		view.setFocusable(true);
		view.setFocusableInTouchMode(true);
		formContainer.addView(view, new ViewGroup.LayoutParams(0, 0));

		//Actual form data
		if (getLob() == LineOfBusiness.HOTELS) {
			mSectionBillingInfo = (SectionBillingInfo) View.inflate(getActivity(),
				R.layout.section_hotel_edit_creditcard, null);
		}
		else if (getLob() == LineOfBusiness.FLIGHTS) {
			mSectionBillingInfo = (SectionBillingInfo) View.inflate(getActivity(),
				R.layout.section_flight_edit_creditcard, null);
		}

		mSectionBillingInfo.setLineOfBusiness(getLob());
		mSectionBillingInfo.addChangeListener(new ISectionEditable.SectionChangeListener() {
			@Override
			public void onChange() {
				if (mAttemptToLeaveMade) {
					mSectionBillingInfo.hasValidInput();
					mSectionLocation.hasValidInput();
				}

				//We attempt to save on change
				Db.getWorkingBillingInfoManager().attemptWorkingBillingInfoSave(getActivity(), false);
			}
		});

		formContainer.addView(mSectionBillingInfo);

		//We set up the AutoComplete view such that it will be the first name or last name field depending on POS.
		//We leave the remainder of the AutoComplete's setup to onFormOpen and onFocus.
		EditText editName = Ui.findView(mSectionBillingInfo, R.id.edit_creditcard_number);
		if (editName != null && editName instanceof AutoCompleteTextView) {
			mCreditCardNumberAutoComplete = (AutoCompleteTextView) editName;
			InputFilter[] filters = new InputFilter[] { this };
			mCreditCardNumberAutoComplete.setFilters(filters);
			mCreditCardNumberAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					StoredCreditCard card = mStoredCreditCardAdapter.getItem(position);
					if (card != null) {
						Db.getWorkingBillingInfoManager().getWorkingBillingInfo().setStoredCard(card);
						commitAndLeave();
					}
				}
			});
		}

		mSectionLocation = Ui.findView(mSectionBillingInfo, R.id.section_location_address);
	}

	@Override
	protected void onFormClosed() {
		if (isResumed() && mFormOpen) {
			mAttemptToLeaveMade = false;
			Db.getWorkingBillingInfoManager().deleteWorkingBillingInfoFile(getActivity());
			mCreditCardNumberAutoComplete.dismissDropDown();
			mCreditCardNumberAutoComplete.setAdapter((ArrayAdapter<StoredCreditCard>) null);
		}
		mFormOpen = false;
	}

	@Override
	public void onFormOpened() {
		if (isResumed() && (!mFormOpen || mCreditCardNumberAutoComplete.hasFocus())) {
			mCreditCardNumberAutoComplete.setOnFocusChangeListener(mCardNumberFocusChangeListener);
			mCreditCardNumberAutoComplete.requestFocus();//<-- This fires the onFocusChangeListener
			mCreditCardNumberAutoComplete.postDelayed(new Runnable() {
				@Override
				public void run() {
					Context context = getActivity();
					if (context != null) {
						InputMethodManager imm = (InputMethodManager) context
							.getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.showSoftInput(mCreditCardNumberAutoComplete, 0);
					}
				}
			}, 250);
		}
		mFormOpen = true;
	}

	//////////////////////////////////////////////////////////////////////////
	// Focus listener

	private View.OnFocusChangeListener mCardNumberFocusChangeListener = new View.OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (mCreditCardNumberAutoComplete != null) {
				if (hasFocus) {
					mCreditCardNumberAutoComplete.setAdapter(mStoredCreditCardAdapter);
					mCreditCardNumberAutoComplete.showDropDown();
				}
				else {
					mCreditCardNumberAutoComplete.dismissDropDown();
					mCreditCardNumberAutoComplete.setAdapter((ArrayAdapter<StoredCreditCard>) null);
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
