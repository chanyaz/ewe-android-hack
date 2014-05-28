package com.expedia.bookings.fragment;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

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
import com.mobiata.android.util.AndroidUtils;

public class CVVEntryFragment extends Fragment implements CreditCardInputListener {

	public static final String TAG = CVVEntryFragment.class.getName();

	private static final String ARG_BILLING_INFO = "ARG_BILLING_INFO";

	private CreditCardSection mCreditCardSection;

	private CVVTextView mCVVTextView;
	private TextView mCVVPromptTextView;
	private TextView mCVVSubpromptTextView;

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
		mCVVPromptTextView = Ui.findView(v, R.id.cvv_prompt_text_view);
		mCVVSubpromptTextView = Ui.findView(v, R.id.cvv_subprompt_text_view);

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

		updateActionBar();
	}

	public void bind() {
		BillingInfo billingInfo = Db.getBillingInfo();
		if (getArguments() != null && getArguments().containsKey(ARG_BILLING_INFO)) {
			// If we passed BillingInfo to getInstance, make that sticky.
			billingInfo = JSONUtils.getJSONable(getArguments(), ARG_BILLING_INFO, BillingInfo.class);
		}

		CreditCardType cardType = getCurrentCCType();

		String personName, cardNumber, cardName;
		if (billingInfo.getStoredCard() != null) {
			StoredCreditCard storedCard = billingInfo.getStoredCard();
			Traveler traveler = Db.getTravelers().get(0);
			personName = traveler.getFirstName() + " " + traveler.getLastName();
			cardNumber = storedCard.getCardNumber();
			cardName = storedCard.getDescription();
		}
		else if (billingInfo.getNumber() != null && billingInfo.getNumber().length() >= 4) {
			personName = billingInfo.getNameOnCard();
			cardNumber = billingInfo.getNumber();
			cardName = getString(R.string.card_ending_TEMPLATE, cardNumber.substring(cardNumber.length() - 4));
		}
		else {
			personName = null;
			cardName = null;
			cardNumber = null;
		}

		resetCVVText();

		//1752. VSC Change cvv prompt text
		int cvvEntryTitleResId = Ui.obtainThemeResID(getActivity(), R.attr.cvvEntryTitleText);
		mCVVPromptTextView.setText(Html.fromHtml(getString(cvvEntryTitleResId, cardName)));

		// Subprompt, i.e. "see front/back of card"
		if (mCVVSubpromptTextView != null) {
			mCVVSubpromptTextView.setText(
				cardType == CreditCardType.AMERICAN_EXPRESS
					? R.string.See_front_of_card
					: R.string.See_back_of_card
			);
		}

		updateActionBar();

		mCreditCardSection.bind(personName, cardType, cardNumber);

		// A few minor UI tweaks on phone, depending on if amex
		if (!AndroidUtils.isTablet(getActivity())) {
			boolean amex = cardType == CreditCardType.AMERICAN_EXPRESS;
			mCVVPromptTextView.setVisibility(amex ? View.GONE : View.VISIBLE);
			FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mCVVTextView.getLayoutParams();
			int right = amex ? R.dimen.cvv_text_view_right_margin_amex : R.dimen.cvv_text_view_right_margin_other;
			int top = amex ? R.dimen.cvv_text_view_top_margin_amex : R.dimen.cvv_text_view_top_margin_other;
			params.rightMargin = getResources().getDimensionPixelOffset(right);
			params.topMargin = getResources().getDimensionPixelOffset(top);
			mCVVTextView.setLayoutParams(params);
		}

		// Configure vars that drive this fragment
		mMinCvvLen = (cardType == CreditCardType.AMERICAN_EXPRESS) ? 4 : 3;
	}

	// Special case for the subprompt ("see back of card"), if it's in the ActionBar (for phone)
	private void updateActionBar() {
		if (!(getActivity() instanceof FragmentActivity)) {
			return;
		}

		CreditCardType cardType = getCurrentCCType();

		ActionBar actionBar = ((FragmentActivity) getActivity()).getActionBar();
		View actionBarView = actionBar.getCustomView();
		if (actionBarView != null) {
			TextView abSubpromptTextView = Ui.findView(actionBarView, R.id.subtitle);
			if (abSubpromptTextView != null) {
				abSubpromptTextView.setText(
					cardType == CreditCardType.AMERICAN_EXPRESS
						? R.string.See_front_of_card
						: R.string.See_back_of_card
				);
			}
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
