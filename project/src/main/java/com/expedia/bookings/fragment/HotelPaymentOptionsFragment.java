package com.expedia.bookings.fragment;

import java.util.List;

import android.app.Activity;
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
import com.expedia.bookings.activity.HotelPaymentOptionsActivity.YoYoMode;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.model.HotelPaymentFlowState;
import com.expedia.bookings.section.SectionBillingInfo;
import com.expedia.bookings.section.SectionStoredCreditCard;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.BookingInfoUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.utils.WalletUtils;
import com.google.android.gms.wallet.MaskedWallet;
import com.mobiata.android.util.ViewUtils;

public class HotelPaymentOptionsFragment extends ChangeWalletFragment {

	SectionBillingInfo mSectionCurrentCreditCard;
	SectionStoredCreditCard mSectionStoredPayment;

	TextView mStoredPaymentsLabel;
	View mStoredPaymentsLabelDiv;
	TextView mCurrentPaymentLabel;
	View mCurrentPaymentLabelDiv;
	TextView mNewPaymentLabel;
	View mNewPaymentLabelDiv;
	ViewGroup mCurrentPaymentContainer;
	ViewGroup mStoredCardsContainer;
	ViewGroup mCurrentStoredPaymentContainer;

	View mNewCreditCardBtn;
	View mPaymentInformationErrorImage;

	HotelPaymentFlowState mValidationState;

	HotelPaymentYoYoListener mListener;

	public static HotelPaymentOptionsFragment newInstance() {
		return new HotelPaymentOptionsFragment();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mListener = Ui.findFragmentListener(this, HotelPaymentYoYoListener.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_hotel_payment_options, container, false);

		//Sections
		mSectionCurrentCreditCard = Ui.findView(v, R.id.current_payment_cc_section);
		mSectionStoredPayment = Ui.findView(v, R.id.stored_creditcard_section);
		mSectionStoredPayment.setLineOfBusiness(LineOfBusiness.HOTELS);

		//Section error indicators
		mPaymentInformationErrorImage = Ui.findView(mSectionCurrentCreditCard, R.id.error_image);

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

		mNewCreditCardBtn = Ui.findView(v, R.id.new_payment_new_card);

		mNewCreditCardBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if (mListener != null) {
					Db.getWorkingBillingInfoManager().shiftWorkingBillingInfo(new BillingInfo());
					mListener.setMode(YoYoMode.YOYO);
					mListener.moveForward();

					OmnitureTracking.trackLinkHotelsCheckoutPaymentEnterManually();
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

		ViewUtils.setAllCaps(mStoredPaymentsLabel);
		ViewUtils.setAllCaps(mCurrentPaymentLabel);
		ViewUtils.setAllCaps(mNewPaymentLabel);

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
				if (storedCard == null) {
					continue;
				}

				//Skip this card if it is the selected card
				if (selectedId != null && storedCard.getId() != null && selectedId.compareToIgnoreCase(storedCard.getId()) == 0) {
					continue;
				}

				SectionStoredCreditCard card = new SectionStoredCreditCard(getActivity());
				card.setLineOfBusiness(LineOfBusiness.HOTELS);
				card.configure(R.drawable.ic_credit_card_white, android.R.color.white,
						R.color.hotels_cc_text_color_secondary);
				card.bind(storedCard);
				card.setPadding(0, paymentOptionPadding, 0, paymentOptionPadding);
				card.setBackgroundResource(R.drawable.bg_payment_method_row);

				if (Db.getTripBucket().getHotel().isCardTypeSupported(card.getStoredCreditCard().getType())) {
					card.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							if (storedCard.isGoogleWallet()) {
								changeMaskedWallet();
							}
							else {
								onStoredCardSelected(storedCard);

								OmnitureTracking.trackLinkHotelsCheckoutPaymentSelectExisting();
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
					divider.setBackgroundColor(0x63FFFFFF);
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
		OmnitureTracking.trackPageLoadHotelsCheckoutPaymentSelect();
	}

	@Override
	public void onResume() {
		super.onResume();
		mValidationState = HotelPaymentFlowState.getInstance(getActivity());

		BillingInfo mBillingInfo = Db.getWorkingBillingInfoManager().getWorkingBillingInfo();

		mSectionCurrentCreditCard.bind(mBillingInfo);
		mSectionStoredPayment.bind(mBillingInfo.getStoredCard());
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onDetach() {
		super.onDetach();

		mListener = null; // Just in case Wallet is leaking
	}

	public void updateVisibilities() {
		List<StoredCreditCard> cards = BookingInfoUtils.getStoredCreditCards(getActivity());

		//Set visibilities
		boolean hasAccountCards = cards != null && cards.size() > 0;
		int numAccountCards = cards == null ? 0 : cards.size();
		boolean hasSelectedStoredCard = Db.getWorkingBillingInfoManager().getWorkingBillingInfo().hasStoredCard();

		if (mValidationState == null) {
			mValidationState = HotelPaymentFlowState.getInstance(getActivity());
		}
		boolean addressValid = mValidationState.hasValidBillingAddress(Db.getWorkingBillingInfoManager()
				.getWorkingBillingInfo());
		boolean cardValid = mValidationState
				.hasValidCardInfo(Db.getWorkingBillingInfoManager().getWorkingBillingInfo());

		// Note: the display of the manualCurrentPayment here has slightly different meaning than in other parts of the
		// checkout flow. For instance, we want to allow the user to book if they have valid information for credit
		// card and address elsewhere, on HotelPaymentFlowState. If we don't require address then we want to ensure that
		// we allow the user to book in places where we validate against address and credit card.
		//
		// Here, we want to display this container even if some of the information is missing. We don't require address
		// on all POS, so HotelPaymentFlowState address validation will return true. Unfortunately, this is a little bit
		// misleading because in this point in the code it is implied that if the address is valid then the user has been
		// entering an address which will not be the case for certain POS. That is why need to inspect on the POS to
		// determine whether or not we should display manual current payment.
		//
		// tl;dr payment validation is complicated and happens in a lot of places and should probably be refactored
		boolean displayManualCurrentPayment = !hasSelectedStoredCard && addressValid && cardValid;

		mCurrentPaymentLabel.setVisibility(hasSelectedStoredCard || displayManualCurrentPayment ? View.VISIBLE
				: View.GONE);
		mCurrentPaymentLabelDiv.setVisibility(mCurrentPaymentLabel.getVisibility());

		mCurrentPaymentContainer.setVisibility(displayManualCurrentPayment ? View.VISIBLE : View.GONE);
		mCurrentStoredPaymentContainer.setVisibility(hasSelectedStoredCard ? View.VISIBLE : View.GONE);

		if (displayManualCurrentPayment) {
			this.mPaymentInformationErrorImage.setVisibility(cardValid && addressValid ? View.GONE : View.VISIBLE);
		}

		mNewPaymentLabel
				.setText(hasSelectedStoredCard || displayManualCurrentPayment ? getString(R.string.or_select_new_paymet_method)
						: getString(R.string.select_payment));
		mNewPaymentLabelDiv.setVisibility(mNewPaymentLabel.getVisibility());

		boolean displayUnselectedStoredCardsLabel = false;
		int remainingUnselectedStoredCards = numAccountCards;

		if (hasAccountCards) {
			if (hasSelectedStoredCard) {
				remainingUnselectedStoredCards--;
			}
			displayUnselectedStoredCardsLabel = remainingUnselectedStoredCards > 0;
		}

		mStoredPaymentsLabel.setVisibility(displayUnselectedStoredCardsLabel ? View.VISIBLE : View.GONE);
		mStoredPaymentsLabelDiv.setVisibility(displayUnselectedStoredCardsLabel ? View.VISIBLE : View.GONE);
		mStoredCardsContainer.setVisibility(displayUnselectedStoredCardsLabel ? View.VISIBLE : View.GONE);
	}

	private void onStoredCardSelected(StoredCreditCard storedCard) {
		Db.getWorkingBillingInfoManager().getWorkingBillingInfo().setStoredCard(storedCard);

		if (mListener != null) {
			mListener.setMode(YoYoMode.NONE);
			mListener.moveBackwards();
		}
	}

	public interface HotelPaymentYoYoListener {
		public void moveForward();

		public void setMode(YoYoMode mode);

		public boolean moveBackwards();

		public void displayOptions();

		public void displayCreditCard();

		public void displaySaveDialog();

		public void displayCheckout();
	}

	//////////////////////////////////////////////////////////////////////////
	// ChangeWalletFragment

	@Override
	protected void onMaskedWalletChanged(MaskedWallet maskedWallet) {
		// Add the current traveler from the wallet, if it is full of data and we have none at the moment
		Traveler traveler = WalletUtils.addWalletAsTraveler(getActivity(), maskedWallet);
		BookingInfoUtils.insertTravelerDataIfNotFilled(getActivity(), traveler, LineOfBusiness.HOTELS);

		onStoredCardSelected(WalletUtils.convertToStoredCreditCard(maskedWallet));
	}

	@Override
	protected void onCriticalWalletError() {
		mListener.setMode(YoYoMode.NONE);
		mListener.moveBackwards();
	}
}
