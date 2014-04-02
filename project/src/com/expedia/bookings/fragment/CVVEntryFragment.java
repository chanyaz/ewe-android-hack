package com.expedia.bookings.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.CreditCardType;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.section.CreditCardInputSection;
import com.expedia.bookings.section.CreditCardInputSection.CreditCardInputListener;
import com.expedia.bookings.section.CreditCardSection;
import com.expedia.bookings.utils.CurrencyUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.CVVTextView;
import com.mobiata.android.json.JSONUtils;

public class CVVEntryFragment extends Fragment implements CreditCardInputListener {

	public static final String TAG = CVVEntryFragment.class.getName();

	private static final String ARG_BILLING_INFO = "ARG_BILLING_INFO";

	private CreditCardSection mCreditCardSection;

	private CVVTextView mCVVTextView;

	private CreditCardInputSection mCreditCardInputSection;

	private CVVEntryFragmentListener mListener;

	private int mMinCvvLen;

	public static CVVEntryFragment newInstance(BillingInfo billingInfo) {
		CVVEntryFragment fragment = new CVVEntryFragment();
		Bundle args = new Bundle();
		JSONUtils.putJSONable(args, ARG_BILLING_INFO, billingInfo);
		return fragment;
	}

	public static CVVEntryFragment newInstance() {
		return new CVVEntryFragment();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mListener = Ui.findFragmentListener(this, CVVEntryFragmentListener.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_cvv_entry, container, false);

		// Cache views
		mCreditCardSection = Ui.findView(v, R.id.credit_card_section);
		mCVVTextView = Ui.findView(v, R.id.cvv_text_view);
		mCreditCardInputSection = Ui.findView(v, R.id.credit_card_input_section);

		// Set this up to listen to the credit card IME
		mCreditCardInputSection.setListener(this);

		return v;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		bind();
	}

	@Override
	public void onResume() {
		super.onResume();
		syncBookButtonState();
	}

	private void syncBookButtonState() {
		mCreditCardInputSection.setBookButtonEnabled(mCVVTextView.getCvv().length() >= mMinCvvLen);
	}

	private void syncCVVTextFilter() {
		if (mCVVTextView != null) {
			InputFilter[] filters = new InputFilter[1];
			if (getCurrentCCType() == CreditCardType.AMERICAN_EXPRESS) {
				filters[0] = new InputFilter.LengthFilter(4);
			}
			else {
				filters[0] = new InputFilter.LengthFilter(3);
			}
			mCVVTextView.setFilters(filters);
		}

	}

	public void resetCVVText() {
		if (mCVVTextView != null) {
			mCVVTextView.setText("");
		}
		syncCVVTextFilter();
	}

	private CreditCardType getCurrentCCType() {
		StoredCreditCard cc = Db.getBillingInfo().getStoredCard();
		if (cc != null) {
			return cc.getType();
		}
		else if (Db.getBillingInfo().getNumber() != null) {
			String ccNumber = Db.getBillingInfo().getNumber();
			return CurrencyUtils.detectCreditCardBrand(ccNumber);
		}
		else {
			return CreditCardType.UNKNOWN;
		}
	}

	public void setCvvErrorMode(boolean enabled) {
		mCVVTextView.setCvvErrorMode(enabled);

		updateSherlockActionBar();
	}

	public void bind() {
		View v = getView();

		BillingInfo billingInfo = Db.getBillingInfo();
		if (getArguments() != null && getArguments().containsKey(ARG_BILLING_INFO)) {
			// If we passed BillingInfo to getInstance, make that sticky.
			billingInfo = JSONUtils.getJSONable(getArguments(), ARG_BILLING_INFO, BillingInfo.class);
		}

		StoredCreditCard cc = billingInfo.getStoredCard();

		String personName = "";
		String cardName = "";
		CreditCardType cardType = getCurrentCCType();
		String cardNumber = billingInfo.getNumber();
		if (cc != null) {
			Traveler traveler = Db.getTravelers().get(0);
			personName = traveler.getFirstName() + " " + traveler.getLastName();
			cardName = cc.getDescription();
		}
		else if (billingInfo.getNumber() != null && billingInfo.getNumber().length() >= 4) {
			personName = billingInfo.getNameOnCard();
			String ccNumber = billingInfo.getNumber();
			cardName = getString(R.string.card_ending_TEMPLATE, ccNumber.substring(ccNumber.length() - 4));
		}

		resetCVVText();

		//1752. VSC Change cvv prompt text
		if (ExpediaBookingApp.IS_VSC) {
			TextView cvvPromptTextView = Ui.findView(v, R.id.cvv_prompt_text_view);
			cvvPromptTextView.setText(getString(Ui
				.obtainThemeResID(getActivity(), R.attr.cvvEntryExplainationText)));
		}
		else {
			TextView cvvPromptTextView = Ui.findView(v, R.id.cvv_prompt_text_view);
			cvvPromptTextView.setText(Html.fromHtml(getString(R.string.cvv_code_TEMPLATE, cardName)));
		}

		// Subprompt, i.e. "see front/back of card"
		TextView cvvSubpromptTextView = Ui.findView(v, R.id.cvv_subprompt_text_view);
		if (cvvSubpromptTextView != null) {
			cvvSubpromptTextView.setText(
				cardType == CreditCardType.AMERICAN_EXPRESS
					? R.string.See_front_of_card
					: R.string.See_back_of_card
			);
		}

		updateSherlockActionBar();

		mCreditCardSection.bind(personName, cardType, cardNumber);

		// Configure vars that drive this fragment
		mMinCvvLen = (cardType == CreditCardType.AMERICAN_EXPRESS) ? 4 : 3;
	}

	// Special case for the subprompt ("see back of card"), if it's in the ActionBar (for phone)
	private void updateSherlockActionBar() {
		if (!(getActivity() instanceof SherlockFragmentActivity)) {
			return;
		}

		CreditCardType cardType = getCurrentCCType();

		ActionBar actionBar = ((SherlockFragmentActivity) getActivity()).getSupportActionBar();
		View actionBarView = actionBar.getCustomView();
		TextView abSubpromptTextView = Ui.findView(actionBarView, R.id.subtitle);
		if (abSubpromptTextView != null) {
			abSubpromptTextView.setText(
				cardType == CreditCardType.AMERICAN_EXPRESS
					? R.string.See_front_of_card
					: R.string.See_back_of_card
			);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// CreditCardInputListener

	@Override
	public void onKeyPress(int code) {
		if (code == CreditCardInputSection.CODE_BOOK) {
			mListener.onBook(mCVVTextView.getCvv());
		}
		else {
			mCVVTextView.onKeyPress(code);
			syncBookButtonState();
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// CVVEntryFragmentListener

	public interface CVVEntryFragmentListener {
		public void onBook(String cvv);
	}
}
