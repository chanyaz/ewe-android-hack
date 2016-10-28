package com.expedia.bookings.fragment;

import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.PaymentType;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.section.CreditCardInputSection;
import com.expedia.bookings.section.CreditCardInputSection.CreditCardInputListener;
import com.expedia.bookings.section.CreditCardSection;
import com.expedia.bookings.text.HtmlCompat;
import com.expedia.bookings.utils.CurrencyUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.CVVTextView;
import com.mobiata.android.json.JSONUtils;

public class CVVEntryFragment extends Fragment implements CreditCardInputListener {

	public static final String TAG = CVVEntryFragment.class.getName();

	private static final String ARG_BILLING_INFO = "ARG_BILLING_INFO";

	private CreditCardSection mCreditCardSection;

	private CVVTextView mCVVTextView;
	private TextView mCVVPromptTextView;
	private TextView mCVVSubpromptTextView;
	private View mBookButton;

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
	public void onAttach(Context context) {
		super.onAttach(context);

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
		mBookButton = Ui.findView(v, R.id.finish_booking_button);

		// Set this up to listen to the credit card IME
		mCreditCardInputSection.setListener(this);

		// Setup a listener for the finish booking button
		if (mBookButton != null) {
			mBookButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					mListener.onBook(mCVVTextView.getCvv());
				}
			});
		}

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
		if (mBookButton != null) {
			mBookButton.setEnabled(mCVVTextView.getCvv().length() >= mMinCvvLen);
		}
	}

	private void syncCVVTextFilter() {
		if (mCVVTextView != null) {
			InputFilter[] filters = new InputFilter[1];
			if (getCurrentCCType() == PaymentType.CARD_AMERICAN_EXPRESS) {
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

	private PaymentType getCurrentCCType() {
		StoredCreditCard cc = Db.getBillingInfo().getStoredCard();
		if (cc != null) {
			return cc.getType();
		}
		else if (Db.getBillingInfo().getNumber() != null) {
			String ccNumber = Db.getBillingInfo().getNumber();
			return CurrencyUtils.detectCreditCardBrand(ccNumber);
		}
		else {
			return PaymentType.UNKNOWN;
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

		PaymentType cardType = getCurrentCCType();

		String personFirstInitial = null;
		String personLastName = null;
		String cardNumber = null;
		String cardName = null;
		if (billingInfo.getStoredCard() != null) {
			StoredCreditCard storedCard = billingInfo.getStoredCard();
			Traveler traveler = Db.getTravelers().get(0);
			personFirstInitial = getFirstCharacter(traveler.getFirstName());
			personLastName = traveler.getLastName();
			cardNumber = storedCard.getCardNumber();
			cardName = storedCard.getDescription();
		}
		else if (billingInfo.getNumber() != null && billingInfo.getNumber().length() >= 4) {
			final String nameOnCard = billingInfo.getNameOnCard();
			personFirstInitial = getFirstCharacter(nameOnCard);
			final int lastNameIndex = nameOnCard.indexOf(' ') + 1;
			personLastName = nameOnCard.substring(lastNameIndex);
			cardNumber = billingInfo.getNumber();
			cardName = getString(R.string.card_ending_TEMPLATE, cardNumber.substring(cardNumber.length() - 4));
		}

		resetCVVText();

		mCVVPromptTextView.setText(
			HtmlCompat.fromHtml(getResources().getString(R.string.security_code_TEMPLATE, cardName)));

		// Subprompt, i.e. "see front/back of card"
		if (mCVVSubpromptTextView != null) {
			mCVVSubpromptTextView.setText(
				cardType == PaymentType.CARD_AMERICAN_EXPRESS
					? R.string.See_front_of_card
					: R.string.See_back_of_card
			);
		}

		updateActionBar();
		StringBuilder signatureNameBuilder = new StringBuilder();
		if (!TextUtils.isEmpty(personFirstInitial)) {
			signatureNameBuilder.append(personFirstInitial);
			signatureNameBuilder.append(". ");
		}
		if (!TextUtils.isEmpty(personLastName)) {
			signatureNameBuilder.append(personLastName);
		}
		String signatureName = signatureNameBuilder.toString();
		mCreditCardSection.bind(signatureName, cardType, cardNumber);

		// A few minor UI tweaks on phone, depending on if amex
		boolean amex = cardType == PaymentType.CARD_AMERICAN_EXPRESS;

		mCVVPromptTextView.setVisibility(amex ? View.GONE : View.VISIBLE);
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mCVVTextView.getLayoutParams();
		if (amex) {
			params.addRule(RelativeLayout.ALIGN_RIGHT, 0);
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			params.addRule(RelativeLayout.CENTER_VERTICAL);
		}
		else {
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
			params.addRule(RelativeLayout.ALIGN_RIGHT, R.id.signature_strip_frame);
			params.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.signature_strip_frame);
			params.addRule(RelativeLayout.ALIGN_TOP, R.id.signature_strip_frame);

			int right = amex ? R.dimen.cvv_text_view_right_margin_amex : R.dimen.cvv_text_view_right_margin_other;
			params.rightMargin = getResources().getDimensionPixelOffset(right);
			mCVVTextView.setLayoutParams(params);
		}

		// Configuration vars that drive this fragment (on phone or tablet)
		mMinCvvLen = amex ? 4 : 3;
	}

	private String getFirstCharacter(String input) {
		if (!TextUtils.isEmpty(input)) {
			return input.substring(0, 1);
		}

		return null;
	}

	// Special case for the subprompt ("see back of card"), if it's in the ActionBar (for phone)
	private void updateActionBar() {
		if (!(getActivity() instanceof FragmentActivity)) {
			return;
		}

		PaymentType cardType = getCurrentCCType();

		ActionBar actionBar = ((FragmentActivity) getActivity()).getActionBar();
		View actionBarView = actionBar.getCustomView();
		if (actionBarView != null) {
			TextView abSubpromptTextView = Ui.findView(actionBarView, R.id.subtitle);
			if (abSubpromptTextView != null) {
				abSubpromptTextView.setText(
					cardType == PaymentType.CARD_AMERICAN_EXPRESS
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
		void onBook(String cvv);
	}
}
