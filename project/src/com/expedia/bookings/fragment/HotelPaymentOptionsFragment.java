package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
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
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.data.User;
import com.expedia.bookings.model.HotelPaymentFlowState;
import com.expedia.bookings.section.SectionBillingInfo;
import com.expedia.bookings.section.SectionStoredCreditCard;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.utils.WalletUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.wallet.MaskedWallet;
import com.google.android.gms.wallet.WalletConstants;
import com.mobiata.android.util.ViewUtils;

public class HotelPaymentOptionsFragment extends WalletFragment {

	SectionBillingInfo mSectionCurrentCreditCard;
	SectionStoredCreditCard mSectionStoredPayment;
	View mNewCreditCardBtn;

	TextView mStoredPaymentsLabel;
	View mStoredPaymentsLabelDiv;
	TextView mCurrentPaymentLabel;
	View mCurrentPaymentLabelDiv;
	TextView mNewPaymentLabel;
	View mNewPaymentLabelDiv;
	ViewGroup mCurrentPaymentContainer;
	ViewGroup mStoredCardsContainer;
	ViewGroup mCurrentStoredPaymentContainer;

	View mPaymentInformationErrorImage;

	HotelPaymentFlowState mValidationState;

	HotelPaymentYoYoListener mListener;

	public static HotelPaymentOptionsFragment newInstance() {
		HotelPaymentOptionsFragment fragment = new HotelPaymentOptionsFragment();
		Bundle args = new Bundle();
		//TODO:Set args here..
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onStart() {
		super.onStart();
		OmnitureTracking.trackPageLoadHotelsCheckoutPaymentSelect(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_hotel_payment_options, container, false);

		//Sections
		mSectionCurrentCreditCard = Ui.findView(v, R.id.current_payment_cc_section);
		mSectionStoredPayment = Ui.findView(v, R.id.stored_creditcard_section);

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

					OmnitureTracking.trackLinkHotelsCheckoutPaymentEnterManually(getActivity());
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

		List<StoredCreditCard> cards = getStoredCreditCards();

		if (cards != null && cards.size() > 0) {
			int paymentOptionPadding = getResources().getDimensionPixelSize(R.dimen.payment_option_vertical_padding);
			boolean firstCard = true;

			//Inflate stored cards
			Resources res = getResources();
			for (int i = 0; i < cards.size(); i++) {
				final StoredCreditCard storedCard = cards.get(i);

				//Skip this card if it is the selected card
				if (Db.getWorkingBillingInfoManager().getWorkingBillingInfo().getStoredCard() != null
						&& Db.getWorkingBillingInfoManager().getWorkingBillingInfo().getStoredCard().getId()
								.compareToIgnoreCase(storedCard.getId()) == 0) {
					continue;
				}

				SectionStoredCreditCard card = (SectionStoredCreditCard) inflater.inflate(
						R.layout.section_hotel_display_stored_credit_card, null);
				card.setUseActiveCardIcon(false);
				card.bind(storedCard);
				card.setPadding(0, paymentOptionPadding, 0, paymentOptionPadding);
				card.setBackgroundResource(R.drawable.bg_payment_method_row);
				card.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (storedCard.isGoogleWallet()) {
							changeMaskedWallet();
						}
						else {
							onStoredCardSelected(storedCard);

							OmnitureTracking.trackLinkHotelsCheckoutPaymentSelectExisting(getActivity());
						}
					}
				});

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
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof HotelPaymentYoYoListener)) {
			throw new RuntimeException(HotelPaymentOptionsFragment.class.getSimpleName() + " activity must implement "
					+ HotelPaymentYoYoListener.class.getSimpleName());
		}

		mListener = (HotelPaymentYoYoListener) activity;
	}

	@Override
	public void onPause() {
		super.onPause();
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

	public void updateVisibilities() {
		List<StoredCreditCard> cards = getStoredCreditCards();

		//Set visibilities
		boolean hasAccountCards = cards != null && cards.size() > 0;
		int numAccountCards = cards == null ? 0 : cards.size();
		boolean hasSelectedStoredCard = Db.getWorkingBillingInfoManager().getWorkingBillingInfo().getStoredCard() != null;

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

	private List<StoredCreditCard> getStoredCreditCards() {
		List<StoredCreditCard> cards = new ArrayList<StoredCreditCard>();

		if (Db.getMaskedWallet() != null) {
			cards.add(WalletUtils.convertToStoredCreditCard(Db.getMaskedWallet()));
		}

		if (User.isLoggedIn(getActivity()) && Db.getUser() != null && Db.getUser().getStoredCreditCards() != null) {
			cards.addAll(Db.getUser().getStoredCreditCards());
		}

		return cards;
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
	// Google Wallet
	//
	// This page primarily deals with *changing* the masked wallet

	private void changeMaskedWallet() {
		if (mConnectionResult != null) {
			resolveUnsuccessfulConnectionResult();
		}
		else {
			MaskedWallet maskedWallet = Db.getMaskedWallet();
			mWalletClient.changeMaskedWallet(maskedWallet.getGoogleTransactionId(),
					maskedWallet.getMerchantTransactionId(), this);
		}
	}

	// OnMaskedWalletLoadedListener

	@Override
	public void onMaskedWalletLoaded(ConnectionResult status, MaskedWallet wallet) {
		super.onMaskedWalletLoaded(status, wallet);

		mConnectionResult = status;
		mRequestCode = REQUEST_CODE_RESOLVE_CHANGE_MASKED_WALLET;

		// This callback is the result of a call to changeMaskedWallet(), so the result should
		// never be isSuccess() because changeMaskedWallet() should never return a MaskedWallet
		if (status.hasResolution()) {
			mProgressDialog.dismiss();
			resolveUnsuccessfulConnectionResult();
		}
		else {
			// This should never happen, but who knows!
			handleUnrecoverableGoogleWalletError(status.getErrorCode());
		}
	}

	// Lifecycle - TODO: MOVE

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		mProgressDialog.hide();

		// Retrieve the error code, if available
		int errorCode = -1;
		if (data != null) {
			errorCode = data.getIntExtra(WalletConstants.EXTRA_ERROR_CODE, -1);
		}

		switch (requestCode) {
		case REQUEST_CODE_RESOLVE_ERR:
			mWalletClient.connect();
			break;
		case REQUEST_CODE_RESOLVE_CHANGE_MASKED_WALLET:
			switch (resultCode) {
			case Activity.RESULT_OK:
				MaskedWallet maskedWallet = data.getParcelableExtra(WalletConstants.EXTRA_MASKED_WALLET);
				Db.setMaskedWallet(maskedWallet);
				WalletUtils.bindWalletToBillingInfo(maskedWallet, Db.getWorkingBillingInfoManager()
						.getWorkingBillingInfo());
				onStoredCardSelected(WalletUtils.convertToStoredCreditCard(maskedWallet));
				break;
			case Activity.RESULT_CANCELED:
				// Who cares if they canceled?  Just stay as before
				break;
			default:
				handleError(errorCode);
			}
			break;
		}
	}

}
