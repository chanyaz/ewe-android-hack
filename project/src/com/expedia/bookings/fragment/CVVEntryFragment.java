package com.expedia.bookings.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.CreditCardType;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.section.CVVSection;
import com.expedia.bookings.section.CreditCardInputSection;
import com.expedia.bookings.section.CreditCardInputSection.CreditCardInputListener;
import com.expedia.bookings.section.CreditCardSection;
import com.expedia.bookings.utils.CurrencyUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;

public class CVVEntryFragment extends Fragment implements CreditCardInputListener {

	public static final String TAG = CVVEntryFragment.class.getName();

	private static final String ARG_PERSON_NAME = "ARG_PERSON_NAME";
	private static final String ARG_CARD_NAME = "ARG_CARD_NAME";
	private static final String ARG_CARD_TYPE = "ARG_CARD_TYPE";

	private CreditCardSection mCreditCardSection;

	private CVVSection mCVVSection;

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

			cardType = cc.getCardType();

			// Temporary fix to avoid crashing.  Only the latest servers (trunk) have 
			// credit card type being returned with stored credit cards, so if the
			// type wasn't returned (or isn't recognized) we default to VISA to avoid
			// a crash.
			if (cardType == null) {
				Log.w("Could not get credit card type enum!  Defaulting to VISA.  What I saw was this: " + cc.getType());

				cardType = CreditCardType.VISA;
			}
		}
		else {
			personName = billingInfo.getNameOnCard();

			String ccNumber = billingInfo.getNumber();
			cardName = context.getString(R.string.card_ending_TEMPLATE, ccNumber.substring(ccNumber.length() - 4));

			cardType = CurrencyUtils.detectCreditCardBrand(context, ccNumber);
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

		if (activity instanceof CVVEntryFragmentListener) {
			mListener = (CVVEntryFragmentListener) activity;
		}
		else {
			throw new RuntimeException("CVVEntryFragment Activity must implement CVVEntryFragmentListener!");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_cvv_entry, container, false);

		// Cache views
		mCreditCardSection = Ui.findView(v, R.id.credit_card_section);
		mCVVSection = Ui.findView(v, R.id.cvv_entry_section);
		mCreditCardInputSection = Ui.findView(v, R.id.credit_card_input_section);

		// Set this up to listen to the credit card IME
		mCreditCardInputSection.setListener(this);

		// Bind data to views
		Bundle args = getArguments();
		String personName = args.getString(ARG_PERSON_NAME);
		String cardName = args.getString(ARG_CARD_NAME);
		CreditCardType cardType = JSONUtils.getEnum(args, ARG_CARD_TYPE, CreditCardType.class);
		mCVVSection.setExplanationText(Html.fromHtml(getString(R.string.cvv_code_TEMPLATE, cardName)));
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
		mCreditCardInputSection.setBookButtonEnabled(mCVVSection.getCvv().length() >= mMinCvvLen);
	}

	public void setCvvErrorMode(boolean enabled) {
		mCVVSection.setCvvErrorMode(enabled);
	}

	//////////////////////////////////////////////////////////////////////////
	// CreditCardInputListener

	@Override
	public void onKeyPress(int code) {
		if (code == CreditCardInputSection.CODE_BOOK) {
			mListener.onBook(mCVVSection.getCvv());
		}
		else {
			mCVVSection.onKeyPress(code);

			syncBookButtonState();
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// CVVEntryFragmentListener

	public interface CVVEntryFragmentListener {
		public void onBook(String cvv);
	}
}
