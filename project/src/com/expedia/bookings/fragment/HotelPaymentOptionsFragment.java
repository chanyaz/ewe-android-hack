package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.expedia.bookings.model.PaymentFlowState;
import com.expedia.bookings.section.HotelSectionStoredCreditCard;
import com.expedia.bookings.section.SectionBillingInfo;
import com.expedia.bookings.section.SectionLocation;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.util.ViewUtils;

public class HotelPaymentOptionsFragment extends Fragment {

	SectionLocation mSectionCurrentBillingAddress;
	SectionBillingInfo mSectionCurrentCreditCard;
	HotelSectionStoredCreditCard mSectionStoredPayment;
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

	View mAddressErrorImage;
	View mCardErrorImage;

	PaymentFlowState mValidationState;

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
		mSectionCurrentBillingAddress = Ui.findView(v, R.id.current_payment_address_section);
		mSectionCurrentCreditCard = Ui.findView(v, R.id.current_payment_cc_section);
		mSectionStoredPayment = Ui.findView(v, R.id.stored_creditcard_section);

		//Section error indicators
		mAddressErrorImage = Ui.findView(mSectionCurrentBillingAddress, R.id.error_image);
		mCardErrorImage = Ui.findView(mSectionCurrentCreditCard, R.id.error_image);

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

		ViewUtils.setAllCaps(mStoredPaymentsLabel);
		ViewUtils.setAllCaps(mCurrentPaymentLabel);
		ViewUtils.setAllCaps(mNewPaymentLabel);

		List<StoredCreditCard> cards = new ArrayList<StoredCreditCard>();

		//Populate stored creditcard list
		if (User.isLoggedIn(getActivity()) && Db.getUser() != null && Db.getUser().getStoredCreditCards() != null) {
			cards = Db.getUser().getStoredCreditCards();
		}

		if (cards != null && cards.size() > 0) {
			//Inflate stored cards
			Resources res = getResources();
			for (int i = 0; i < cards.size(); i++) {
				final StoredCreditCard storedCard = cards.get(i);
				HotelSectionStoredCreditCard card = (HotelSectionStoredCreditCard) inflater.inflate(
						R.layout.section_hotel_display_stored_credit_card, null);
				card.setUseActiveCardIcon(false, false);
				card.bind(cards.get(i));
				card.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Db.getWorkingBillingInfoManager().getWorkingBillingInfo().setStoredCard(storedCard);
						if (mListener != null) {
							mListener.setMode(YoYoMode.NONE);
							mListener.moveBackwards();
							
							OmnitureTracking.trackLinkHotelsCheckoutPaymentSelectExisting(getActivity());
						}
					}
				});

				//Add dividers
				if (i != 0) {
					View divider = new View(getActivity());
					LinearLayout.LayoutParams divLayoutParams = new LinearLayout.LayoutParams(
							LayoutParams.MATCH_PARENT, res.getDimensionPixelSize(R.dimen.simple_grey_divider_height));
					divider.setLayoutParams(divLayoutParams);
					divider.setBackgroundColor(0x63FFFFFF);
					mStoredCardsContainer.addView(divider);
				}
				mStoredCardsContainer.addView(card);
			}
		}

		updateVisibilities();

		return v;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof HotelPaymentYoYoListener)) {
			throw new RuntimeException("HotelPaymentOptiosnFragment activity must implement HotelPaymentYoYoListener!");
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
		mValidationState = PaymentFlowState.getInstance(getActivity());

		BillingInfo mBillingInfo = Db.getWorkingBillingInfoManager().getWorkingBillingInfo();

		mSectionCurrentBillingAddress.bind(mBillingInfo.getLocation());
		mSectionCurrentCreditCard.bind(mBillingInfo);
		mSectionStoredPayment.bind(mBillingInfo.getStoredCard());
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	public void updateVisibilities() {
		List<StoredCreditCard> cards = new ArrayList<StoredCreditCard>();

		//Populate stored creditcard list
		if (User.isLoggedIn(getActivity()) && Db.getUser() != null && Db.getUser().getStoredCreditCards() != null) {
			cards = Db.getUser().getStoredCreditCards();
		}

		//Set visibilities
		boolean hasAccountCards = cards != null && cards.size() > 0;
		boolean hasSelectedStoredCard = Db.getWorkingBillingInfoManager().getWorkingBillingInfo().getStoredCard() != null;

		if (mValidationState == null) {
			mValidationState = PaymentFlowState.getInstance(getActivity());
		}
		boolean addressValid = mValidationState.hasValidBillingAddress(Db.getWorkingBillingInfoManager()
				.getWorkingBillingInfo());
		boolean cardValid = mValidationState
				.hasValidCardInfo(Db.getWorkingBillingInfoManager().getWorkingBillingInfo());
		boolean displayManualCurrentPayment = !hasSelectedStoredCard && (addressValid || cardValid);

		mCurrentPaymentLabel.setVisibility(hasSelectedStoredCard || displayManualCurrentPayment ? View.VISIBLE
				: View.GONE);
		mCurrentPaymentLabelDiv.setVisibility(mCurrentPaymentLabel.getVisibility());

		mCurrentPaymentContainer.setVisibility(displayManualCurrentPayment ? View.VISIBLE : View.GONE);
		mCurrentStoredPaymentContainer.setVisibility(hasSelectedStoredCard ? View.VISIBLE : View.GONE);

		if (displayManualCurrentPayment) {
			this.mAddressErrorImage.setVisibility(addressValid ? View.GONE : View.VISIBLE);
			this.mCardErrorImage.setVisibility(cardValid ? View.GONE : View.VISIBLE);
		}

		mNewPaymentLabel
				.setText(hasSelectedStoredCard || displayManualCurrentPayment ? getString(R.string.or_select_new_paymet_method)
						: getString(R.string.select_payment));
		mNewPaymentLabelDiv.setVisibility(mNewPaymentLabel.getVisibility());

		mStoredPaymentsLabel.setVisibility(hasAccountCards ? View.VISIBLE : View.GONE);
		mStoredPaymentsLabelDiv.setVisibility(mStoredPaymentsLabel.getVisibility());
		mStoredCardsContainer.setVisibility(hasAccountCards ? View.VISIBLE : View.GONE);
	}

	public interface HotelPaymentYoYoListener {
		public void moveForward();

		public void setMode(YoYoMode mode);

		public boolean moveBackwards();

		public void displayOptions();

		public void displayAddress();

		public void displayCreditCard();

		public void displaySaveDialog();

		public void displayCheckout();
	}
}
