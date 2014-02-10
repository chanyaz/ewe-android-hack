package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.fragment.base.LobableFragment;
import com.expedia.bookings.model.FlightPaymentFlowState;
import com.expedia.bookings.model.HotelPaymentFlowState;
import com.expedia.bookings.section.SectionBillingInfo;
import com.expedia.bookings.section.SectionStoredCreditCard;
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

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_checkout_payment_button, null);
		mStoredCreditCardBtn = Ui.findView(v, R.id.stored_creditcard_section_button);
		mEmptyPaymentBtn = Ui.findView(v, R.id.payment_info_btn);
		mManualCreditCardBtn = Ui.findView(v, R.id.creditcard_section_button);

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

	public boolean isValid() {
		if (Db.hasBillingInfo()) {
			if (getLob() == LineOfBusiness.FLIGHTS) {
				FlightPaymentFlowState state = FlightPaymentFlowState.getInstance(getActivity());
				return state.hasAValidCardSelected(Db.getBillingInfo());
			}
			else if (getLob() == LineOfBusiness.HOTELS) {
				HotelPaymentFlowState state = HotelPaymentFlowState.getInstance(getActivity());
				return state.hasAValidCardSelected(Db.getBillingInfo());
			}
		}
		return false;
	}
}
