package com.expedia.bookings.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class CVVEntryFragment extends Fragment implements CreditCardInputListener {

	public static final String TAG = CVVEntryFragment.class.getName();

	private static final String ARG_PERSON_NAME = "ARG_PERSON_NAME";
	private static final String ARG_CARD_NAME = "ARG_CARD_NAME";
	private static final String ARG_CARD_TYPE = "ARG_CARD_TYPE";

	private CreditCardSection mCreditCardSection;

	private CVVTextView mCVVTextView;

	private CreditCardInputSection mCreditCardInputSection;

	private CVVEntryFragmentListener mListener;

	private int mMinCvvLen;

	public static CVVEntryFragment newInstance(Context context, BillingInfo billingInfo) {
		// Determine the data displayed on the CVVEntryFragment
		StoredCreditCard cc = billingInfo.getStoredCard();

		String personName;
		String cardName;
		CreditCardType cardType;
		if (cc != null) {
			Traveler traveler = Db.getTravelers().get(0);
			personName = traveler.getFirstName() + " " + traveler.getLastName();

			cardName = cc.getDescription();

			cardType = cc.getType();
		}
		else {
			personName = billingInfo.getNameOnCard();

			String ccNumber = billingInfo.getNumber();
			cardName = context.getString(R.string.card_ending_TEMPLATE, ccNumber.substring(ccNumber.length() - 4));

			cardType = CurrencyUtils.detectCreditCardBrand(ccNumber);
		}

		return CVVEntryFragment.newInstance(personName, cardName, cardType);
	}

	public static CVVEntryFragment newInstance(String personName, String cardName, CreditCardType cardType) {
		CVVEntryFragment fragment = new CVVEntryFragment();
		Bundle args = new Bundle();
		args.putString(ARG_PERSON_NAME, personName);
		args.putString(ARG_CARD_NAME, cardName);
		JSONUtils.putEnum(args, ARG_CARD_TYPE, cardType);
		fragment.setArguments(args);
		return fragment;
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

		// Bind data to views
		Bundle args = getArguments();
		String personName = args.getString(ARG_PERSON_NAME);
		String cardName = args.getString(ARG_CARD_NAME);
		CreditCardType cardType = JSONUtils.getEnum(args, ARG_CARD_TYPE, CreditCardType.class);
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
		mCreditCardSection.bind(personName, cardType);

		// Configure vars that drive this fragment
		mMinCvvLen = (cardType == CreditCardType.AMERICAN_EXPRESS) ? 4 : 3;

		return v;
	}

	@Override
	public void onResume() {
		super.onResume();

		syncBookButtonState();
	}

	private void syncBookButtonState() {
		mCreditCardInputSection.setBookButtonEnabled(mCVVTextView.getCvv().length() >= mMinCvvLen);
	}

	public void setCvvErrorMode(boolean enabled) {
		mCVVTextView.setCvvErrorMode(enabled);
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
