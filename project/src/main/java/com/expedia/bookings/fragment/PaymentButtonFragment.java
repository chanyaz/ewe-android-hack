package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.fragment.base.LobableFragment;
import com.expedia.bookings.model.FlightPaymentFlowState;
import com.expedia.bookings.model.HotelPaymentFlowState;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.section.SectionBillingInfo;
import com.expedia.bookings.section.SectionStoredCreditCard;
import com.expedia.bookings.widget.TextView;
import com.mobiata.android.util.Ui;

public class PaymentButtonFragment extends LobableFragment {

	public static PaymentButtonFragment newInstance(LineOfBusiness lob) {
		PaymentButtonFragment frag = new PaymentButtonFragment();
		frag.setLob(lob);
		return frag;
	}

	private ViewGroup mEmptyPaymentBtn;
	private SectionStoredCreditCard mStoredCreditCardBtn;
	private SectionBillingInfo mManualCreditCardBtn;
	private ViewGroup mCCFeesMessageContainer;
	private TextView mCCFeesMessageText;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_checkout_payment_button, null);
		mStoredCreditCardBtn = Ui.findView(v, R.id.stored_creditcard_section_button);
		mEmptyPaymentBtn = Ui.findView(v, R.id.payment_info_btn);
		mManualCreditCardBtn = Ui.findView(v, R.id.creditcard_section_button);
		mCCFeesMessageContainer = Ui.findView(v, R.id.credit_card_fees_container);
		mCCFeesMessageText = Ui.findView(v, R.id.credit_card_fees_message_text);

		mStoredCreditCardBtn.setLineOfBusiness(getLob());

		//We init these here for later use;
		if (getLob() == LineOfBusiness.HOTELS) {
			HotelPaymentFlowState.getInstance(getActivity());
		}
		else {
			FlightPaymentFlowState.getInstance(getActivity());
		}

		return v;
	}

	@Override
	public void onLobSet(LineOfBusiness lob) {
		//We do everything at bind time
	}

	@Override
	public void onResume() {
		super.onResume();
		Events.register(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		Events.unregister(this);
	}


	public void setEnabled(boolean enable) {
		mEmptyPaymentBtn.setEnabled(enable);
		mStoredCreditCardBtn.setEnabled(enable);
		mManualCreditCardBtn.setEnabled(enable);
	}

	public void bindToDb() {
		if (mStoredCreditCardBtn != null && Db.hasBillingInfo()) {
			BillingInfo bi = Db.getBillingInfo();
			boolean hasValidCardSelected = false;
			if (getLob() == LineOfBusiness.HOTELS) {
				HotelPaymentFlowState state = HotelPaymentFlowState.getInstance(getActivity());
				hasValidCardSelected = state.hasAValidCardSelected(bi);
			}
			else {
				FlightPaymentFlowState state = FlightPaymentFlowState.getInstance(getActivity());
				hasValidCardSelected = state.hasAValidCardSelected(bi);
				// Set show CC fee to true, so that it can be eligible to be shown in cost breakdown.
				Db.getTripBucket().getFlight().getFlightTrip().setShowFareWithCardFee(true);
			}

			// LCC Fees callout
			if (getLob() == LineOfBusiness.FLIGHTS) {
				FlightTrip trip = Db.getTripBucket().getFlight().getFlightTrip();
				Money cardFee = Db.getTripBucket().getFlight().getCardFee(bi);
				if (cardFee != null && trip.showFareWithCardFee(getActivity(), bi)) {
					mCCFeesMessageText.setText(Html.fromHtml(getString(R.string.airline_card_fee_TEMPLATE,
						cardFee.getFormattedMoney())));
					mCCFeesMessageContainer.setVisibility(View.VISIBLE);
					Events.post(new Events.LCCPaymentFeesAdded());
				}
				else {
					mCCFeesMessageContainer.setVisibility(View.GONE);
				}
			}

			if (bi.hasStoredCard()) {
				mStoredCreditCardBtn.bind(bi.getStoredCard());

				mStoredCreditCardBtn.setVisibility(View.VISIBLE);
				mEmptyPaymentBtn.setVisibility(View.GONE);
				mManualCreditCardBtn.setVisibility(View.GONE);
			}
			else if (hasValidCardSelected) {
				mManualCreditCardBtn.bind(bi);

				mStoredCreditCardBtn.setVisibility(View.GONE);
				mEmptyPaymentBtn.setVisibility(View.GONE);
				mManualCreditCardBtn.setVisibility(View.VISIBLE);
			}
			else {
				mStoredCreditCardBtn.setVisibility(View.GONE);
				mEmptyPaymentBtn.setVisibility(View.VISIBLE);
				mManualCreditCardBtn.setVisibility(View.GONE);
			}
		}
	}

	/**
	 * Returns whether a valid credit card is selected. Updates the button to show or hide a validation
	 * checkmark image.
	 *
	 * @return
	 */
	public boolean validate() {
		if (Db.hasBillingInfo()) {
			BillingInfo bi = Db.getBillingInfo();
			if (bi.hasStoredCard()) {
				Ui.findView(mStoredCreditCardBtn, R.id.validation_checkmark).setVisibility(View.VISIBLE);
				return true;
			}
			else if (getLob() == LineOfBusiness.FLIGHTS) {
				FlightPaymentFlowState state = FlightPaymentFlowState.getInstance(getActivity());
				Ui.findView(mManualCreditCardBtn, R.id.validation_checkmark).setVisibility(View.VISIBLE);
				return state.hasAValidCardSelected(bi);
			}
			else if (getLob() == LineOfBusiness.HOTELS) {
				HotelPaymentFlowState state = HotelPaymentFlowState.getInstance(getActivity());
				Ui.findView(mManualCreditCardBtn, R.id.validation_checkmark).setVisibility(View.VISIBLE);
				return state.hasAValidCardSelected(bi);
			}
		}
		Ui.findView(mStoredCreditCardBtn, R.id.validation_checkmark).setVisibility(View.GONE);
		Ui.findView(mManualCreditCardBtn, R.id.validation_checkmark).setVisibility(View.GONE);
		return false;
	}
}
