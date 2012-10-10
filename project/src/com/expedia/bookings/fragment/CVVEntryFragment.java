package com.expedia.bookings.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.section.CVVSection;
import com.expedia.bookings.section.CreditCardInputSection;
import com.expedia.bookings.section.CreditCardInputSection.CreditCardInputListener;
import com.expedia.bookings.section.CreditCardSection;
import com.expedia.bookings.utils.Ui;

public class CVVEntryFragment extends Fragment implements CreditCardInputListener {

	public static final String TAG = CVVEntryFragment.class.getName();

	private static final String ARG_PERSON_NAME = "ARG_PERSON_NAME";
	private static final String ARG_CARD_NAME = "ARG_CARD_NAME";

	private CreditCardSection mCreditCardSection;

	private CVVSection mCVVSection;

	private CreditCardInputSection mCreditCardInputSection;

	private CVVEntryFragmentListener mListener;

	public static CVVEntryFragment newInstance(String personName, String cardName) {
		CVVEntryFragment fragment = new CVVEntryFragment();
		Bundle args = new Bundle();
		args.putString(ARG_PERSON_NAME, personName);
		args.putString(ARG_CARD_NAME, cardName);
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
		mCVVSection.setExplanationText(Html.fromHtml(getString(R.string.cvv_code_TEMPLATE, cardName)));
		mCreditCardSection.setName(personName);

		return v;
	}

	@Override
	public void onResume() {
		super.onResume();

		syncBookButtonState();
	}

	private void syncBookButtonState() {
		mCreditCardInputSection.setBookButtonEnabled(mCVVSection.getCvv().length() >= 3);
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
