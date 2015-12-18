package com.expedia.bookings.fragment;

import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.FlightPaymentOptionsActivity.YoYoMode;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.model.FlightPaymentFlowState;
import com.expedia.bookings.section.SectionBillingInfo;
import com.expedia.bookings.section.SectionLocation;
import com.expedia.bookings.section.SectionStoredCreditCard;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.BookingInfoUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.utils.WalletUtils;
import com.google.android.gms.wallet.MaskedWallet;

public class FlightPaymentOptionsFragment extends ChangeWalletFragment {

	SectionLocation mSectionCurrentBillingAddress;
	SectionBillingInfo mSectionCurrentCreditCard;
	SectionBillingInfo mSectionPartialCard;
	SectionStoredCreditCard mSectionStoredPayment;

	TextView mStoredPaymentsLabel;
	View mStoredPaymentsLabelDiv;
	TextView mCurrentPaymentLabel;
	View mCurrentPaymentLabelDiv;
	TextView mNewPaymentLabel;
	View mNewPaymentLabelDiv;
	View mPartialCardDiv;
	ViewGroup mCurrentPaymentContainer;
	ViewGroup mStoredCardsContainer;
	ViewGroup mCurrentStoredPaymentContainer;
	View mCurrentPaymentCcAddressDiv;

	View mNewCreditCardBtn;
	FlightPaymentFlowState mValidationState;

	FlightPaymentYoYoListener mListener;

	public static FlightPaymentOptionsFragment newInstance() {
		return new FlightPaymentOptionsFragment();
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);

		mListener = Ui.findFragmentListener(this, FlightPaymentYoYoListener.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_flight_payment_options, container, false);

		//Sections
		mSectionCurrentBillingAddress = Ui.findView(v, R.id.current_payment_address_section);
		mSectionCurrentCreditCard = Ui.findView(v, R.id.current_payment_cc_section);
		mSectionStoredPayment = Ui.findView(v, R.id.stored_creditcard_section);

		//Other views
		mStoredPaymentsLabel = Ui.findView(v, R.id.stored_payments_label);
		mStoredPaymentsLabelDiv = Ui.findView(v, R.id.stored_payments_label_div);
		mCurrentPaymentLabel = Ui.findView(v, R.id.current_payment_label);
		mCurrentPaymentLabelDiv = Ui.findView(v, R.id.current_payment_label_div);
		mNewPaymentLabel = Ui.findView(v, R.id.new_payment_label);
		mNewPaymentLabelDiv = Ui.findView(v, R.id.new_payment_label_div);
		mCurrentPaymentContainer = Ui.findView(v, R.id.current_payment_container);
		mStoredCardsContainer = Ui.findView(v, R.id.new_payment_stored_cards);
		mCurrentStoredPaymentContainer = Ui.findView(v, R.id.current_stored_payment_container);
		mCurrentPaymentCcAddressDiv = Ui.findView(v, R.id.current_payment_cc_address_divider);

		mSectionPartialCard = Ui.findView(v, R.id.new_payment_partial_card);
		mPartialCardDiv = Ui.findView(v, R.id.new_payment_partial_card_divider);

		mNewCreditCardBtn = Ui.findView(v, R.id.new_payment_new_card);

		mStoredPaymentsLabel.setAllCaps(true);
		mCurrentPaymentLabel.setAllCaps(true);
		mNewPaymentLabel.setAllCaps(true);

		mSectionStoredPayment.setLineOfBusiness(LineOfBusiness.FLIGHTS);

		if (!PointOfSale.getPointOfSale().requiresBillingAddressFlights()) {
			mCurrentPaymentCcAddressDiv.setVisibility(View.GONE);
			mSectionCurrentBillingAddress.setVisibility(View.GONE);
		}

		mCurrentStoredPaymentContainer.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mSectionStoredPayment.getStoredCreditCard().isGoogleWallet()) {
					changeMaskedWallet();
				}
				else {
					mListener.setMode(YoYoMode.NONE);
					mListener.moveBackwards();
				}
			}
		});

		mNewCreditCardBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mListener != null) {
					Db.getWorkingBillingInfoManager().shiftWorkingBillingInfo(new BillingInfo());
					mListener.setMode(YoYoMode.YOYO);
					mListener.moveForward();

					OmnitureTracking.trackLinkFlightCheckoutPaymentEnterManually();
				}
			}
		});

		mSectionCurrentBillingAddress.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mListener != null) {
					mListener.setMode(YoYoMode.EDIT);
					mListener.displayAddress();
				}
			}
		});

		mSectionCurrentCreditCard.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mListener != null) {
					mListener.setMode(YoYoMode.EDIT);
					mListener.displayCreditCard();
				}
			}
		});

		mSectionPartialCard.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!Db.getWorkingBillingInfoManager().getWorkingBillingInfo().hasStoredCard()) {
					mListener.setMode(YoYoMode.YOYO);
					mListener.moveForward();
				}
				else {
					BillingInfo noStored = new BillingInfo(Db.getWorkingBillingInfoManager().getWorkingBillingInfo());
					noStored.setStoredCard(null);
					Db.getWorkingBillingInfoManager().shiftWorkingBillingInfo(noStored);
					mListener.setMode(YoYoMode.YOYO);
					mListener.moveForward();
				}
			}
		});

		List<StoredCreditCard> cards = BookingInfoUtils.getStoredCreditCards(getActivity());

		if (cards != null && cards.size() > 0) {
			int paymentOptionPadding = getResources().getDimensionPixelSize(R.dimen.payment_option_vertical_padding);
			boolean firstCard = true;
			String selectedId = null;
			if (Db.getWorkingBillingInfoManager() != null
				&& Db.getWorkingBillingInfoManager().getWorkingBillingInfo() != null
				&& Db.getWorkingBillingInfoManager().getWorkingBillingInfo().hasStoredCard()) {
				selectedId = Db.getWorkingBillingInfoManager().getWorkingBillingInfo().getStoredCard().getId();
			}

			//Inflate stored cards
			Resources res = getResources();
			for (int i = 0; i < cards.size(); i++) {
				final StoredCreditCard storedCard = cards.get(i);

				//Skip this card if it is the selected card
				if (selectedId != null && storedCard.getId() != null  && selectedId.compareToIgnoreCase(storedCard.getId()) == 0) {
					continue;
				}

				SectionStoredCreditCard card = new SectionStoredCreditCard(getActivity());
				card.setLineOfBusiness(LineOfBusiness.FLIGHTS);
				card.configure(R.drawable.ic_credit_card, 0, 0);
				card.bind(storedCard);
				card.setPadding(0, paymentOptionPadding, 0, paymentOptionPadding);
				card.setBackgroundResource(R.drawable.bg_payment_method_row);

				if (Db.getTripBucket().getFlight().isPaymentTypeSupported(card.getStoredCreditCard().getType())) {
					card.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							if (storedCard.isGoogleWallet()) {
								changeMaskedWallet();
							}
							else {
								onStoredCardSelected(storedCard);

								OmnitureTracking.trackLinkFlightCheckoutPaymentSelectExisting();
							}
						}
					});
				}
				else {
					card.setEnabled(false);
					card.bindCardNotSupported();
				}

				//Add dividers
				if (!firstCard) {
					View divider = new View(getActivity());
					LinearLayout.LayoutParams divLayoutParams = new LinearLayout.LayoutParams(
							LayoutParams.MATCH_PARENT, res.getDimensionPixelSize(R.dimen.simple_grey_divider_height));
					divider.setLayoutParams(divLayoutParams);
					divider.setBackgroundColor(res.getColor(R.color.divider_grey));
					mStoredCardsContainer.addView(divider);
				}

				mStoredCardsContainer.addView(card);
				firstCard = false;
			}
		}

		updateVisibilities();

		return v;
	}

	@Override
	public void onStart() {
		super.onStart();
		OmnitureTracking.trackPageLoadFlightCheckoutPaymentSelect();
	}

	@Override
	public void onResume() {
		super.onResume();
		mValidationState = FlightPaymentFlowState.getInstance(getActivity());

		BillingInfo billingInfo = Db.getWorkingBillingInfoManager().getWorkingBillingInfo();

		mSectionCurrentBillingAddress.bind(billingInfo.getLocation());
		mSectionCurrentCreditCard.bind(billingInfo);
		mSectionStoredPayment.bind(billingInfo.getStoredCard());
		mSectionPartialCard.bind(billingInfo);
	}

	@Override
	public void onDetach() {
		super.onDetach();

		mListener = null; // Just in case Wallet is leaking
	}

	public void updateVisibilities() {
		List<StoredCreditCard> cards = BookingInfoUtils.getStoredCreditCards(getActivity());

		BillingInfo billingInfo = Db.getWorkingBillingInfoManager().getWorkingBillingInfo();

		//Set visibilities
		boolean hasAccountCards = cards != null && cards.size() > 0;
		boolean hasSelectedStoredCard = billingInfo.hasStoredCard();
		boolean onlyAccountCardIsSelected = cards != null && cards.size() == 1 && hasSelectedStoredCard;

		if (mValidationState == null) {
			mValidationState = FlightPaymentFlowState.getInstance(getActivity());
		}

		boolean addressValid = mValidationState.hasValidBillingAddress(billingInfo);
		boolean addressRequired = PointOfSale.getPointOfSale().requiresBillingAddressFlights();
		boolean cardValid = mValidationState.hasValidCardInfo(billingInfo);
		boolean displayManualCurrentPayment = !hasSelectedStoredCard && (addressValid && cardValid);
		boolean displayPartialPayment = !displayManualCurrentPayment && ((addressRequired && addressValid) || cardValid);

		mSectionPartialCard.setVisibility(displayPartialPayment ? View.VISIBLE : View.GONE);
		mPartialCardDiv.setVisibility(mSectionPartialCard.getVisibility());

		mCurrentPaymentLabel.setVisibility(hasSelectedStoredCard || displayManualCurrentPayment ? View.VISIBLE
				: View.GONE);
		mCurrentPaymentLabelDiv.setVisibility(mCurrentPaymentLabel.getVisibility());

		mCurrentPaymentContainer.setVisibility(displayManualCurrentPayment ? View.VISIBLE : View.GONE);
		mCurrentStoredPaymentContainer.setVisibility(hasSelectedStoredCard ? View.VISIBLE : View.GONE);

		String paymentText = hasSelectedStoredCard || displayManualCurrentPayment ? getString(R.string.or_select_new_paymet_method)
				: getString(R.string.select_payment);
		mNewPaymentLabel.setText(paymentText);
		mNewPaymentLabelDiv.setVisibility(mNewPaymentLabel.getVisibility());

		mStoredPaymentsLabel.setVisibility(hasAccountCards && !onlyAccountCardIsSelected ? View.VISIBLE : View.GONE);
		mStoredPaymentsLabelDiv.setVisibility(mStoredPaymentsLabel.getVisibility());
		mStoredCardsContainer.setVisibility(mStoredPaymentsLabel.getVisibility());
	}

	private void onStoredCardSelected(StoredCreditCard storedCard) {
		Db.getWorkingBillingInfoManager().getWorkingBillingInfo().setStoredCard(storedCard);

		if (mListener != null) {
			mListener.setMode(YoYoMode.NONE);
			mListener.moveBackwards();
		}
	}

	public interface FlightPaymentYoYoListener {
		void moveForward();

		void setMode(YoYoMode mode);

		boolean moveBackwards();

		void displayOptions();

		void displayAddress();

		void displayCreditCard();

		void displaySaveDialog();

		void displayCheckout();
	}

	//////////////////////////////////////////////////////////////////////////
	// ChangeWalletFragment

	@Override
	protected void onMaskedWalletChanged(MaskedWallet maskedWallet) {
		// Add the current traveler from the wallet, if it is full of data and we have none at the moment
		Traveler traveler = WalletUtils.addWalletAsTraveler(getActivity(), maskedWallet);
		BookingInfoUtils.insertTravelerDataIfNotFilled(getActivity(), traveler, LineOfBusiness.FLIGHTS);

		onStoredCardSelected(WalletUtils.convertToStoredCreditCard(maskedWallet));
	}

	@Override
	protected void onCriticalWalletError() {
		mListener.setMode(YoYoMode.NONE);
		mListener.moveBackwards();
	}
}
