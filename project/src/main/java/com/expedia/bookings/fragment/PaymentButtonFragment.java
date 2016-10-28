package com.expedia.bookings.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.trips.TripBucketItem;
import com.expedia.bookings.fragment.base.LobableFragment;
import com.expedia.bookings.model.FlightPaymentFlowState;
import com.expedia.bookings.model.HotelPaymentFlowState;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.section.SectionBillingInfo;
import com.expedia.bookings.section.SectionStoredCreditCard;
import com.expedia.bookings.section.StoredCreditCardSpinnerAdapter;
import com.expedia.bookings.text.HtmlCompat;
import com.expedia.bookings.utils.BookingInfoUtils;
import com.expedia.bookings.widget.TextView;
import com.mobiata.android.util.Ui;

public class PaymentButtonFragment extends LobableFragment {

	public static PaymentButtonFragment newInstance(LineOfBusiness lob) {
		PaymentButtonFragment frag = new PaymentButtonFragment();
		frag.setLob(lob);
		return frag;
	}

	public interface IPaymentButtonListener {
		void onCreditCardEditButtonPressed();

		void onAddNewCreditCardSelected();

		void onStoredCreditCardChosen();
	}

	private ViewGroup mEmptyPaymentBtn;
	private SectionStoredCreditCard mStoredCreditCardBtn;
	private SectionBillingInfo mManualCreditCardBtn;
	private ViewGroup mCCFeesMessageContainer;
	private TextView mCCFeesMessageText;

	private TextView mEmptyCCFakeSpinner;
	private TextView mStoredCCFakeSpinner;
	private TextView mNewCCFakeSpinner;
	private TextView mEmptyCCEditText;
	private TextView mStoredCCEditText;
	private TextView mNewCCEditText;


	private IPaymentButtonListener mPaymentButtonListener;

	private ListPopupWindow mStoredCardPopup;
	private StoredCreditCardSpinnerAdapter mStoredCreditCardAdapter;

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		mPaymentButtonListener = Ui.findFragmentListener(this, IPaymentButtonListener.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_checkout_payment_button, null);
		// Stored CC init views
		mStoredCreditCardBtn = Ui.findView(v, R.id.stored_creditcard_section_button);
		mStoredCCFakeSpinner = Ui.findView(mStoredCreditCardBtn, R.id.stored_creditcard_fake_spinner);
		mStoredCCFakeSpinner.setOnClickListener(mStoredCardButtonClickListener);
		mStoredCCEditText = Ui.findView(mStoredCreditCardBtn, R.id.stored_creditcard_edit_button);
		mStoredCCEditText.setOnClickListener(mEditListener);

		// Empty CC init views
		mEmptyPaymentBtn = Ui.findView(v, R.id.payment_info_btn);
		mEmptyCCFakeSpinner = Ui.findView(mEmptyPaymentBtn, R.id.empty_saved_creditcard_fake_spinner);
		mEmptyCCFakeSpinner.setOnClickListener(mStoredCardButtonClickListener);
		mEmptyCCEditText = Ui.findView(mEmptyPaymentBtn, R.id.empty_edit_creditcard_button);
		mEmptyCCEditText.setOnClickListener(mEditListener);

		// Manually entered CC init views
		mManualCreditCardBtn = Ui.findView(v, R.id.creditcard_section_button);
		mNewCCFakeSpinner = Ui.findView(mManualCreditCardBtn, R.id.new_creditcard_fake_spinner);
		mNewCCFakeSpinner.setOnClickListener(mStoredCardButtonClickListener);
		mNewCCEditText = Ui.findView(mManualCreditCardBtn, R.id.new_creditcard_edit_button);
		mNewCCEditText.setOnClickListener(mEditListener);

		mCCFeesMessageContainer = Ui.findView(v, R.id.credit_card_fees_container);
		mCCFeesMessageText = Ui.findView(v, R.id.credit_card_fees_message_text);

		mStoredCreditCardBtn.setLineOfBusiness(getLob());

		//We init these here for later use;
		TripBucketItem item = null;
		if (getLob() == LineOfBusiness.HOTELS) {
			HotelPaymentFlowState.getInstance(getActivity());
			item = Db.getTripBucket().getHotel();
		}
		else {
			FlightPaymentFlowState.getInstance(getActivity());
			item = Db.getTripBucket().getFlight();
		}

		mStoredCreditCardAdapter = new StoredCreditCardSpinnerAdapter(getActivity(), item);

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
				Money cardFee = Db.getTripBucket().getFlight().getPaymentFee(bi);
				if (cardFee != null && trip.showFareWithCardFee(getActivity(), bi)) {
					mCCFeesMessageText.setText(HtmlCompat.fromHtml(getString(R.string.airline_card_fee_TEMPLATE,
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
				mStoredCCFakeSpinner.setVisibility(User.isLoggedIn(getActivity()) ? View.VISIBLE : View.INVISIBLE);
				mEmptyPaymentBtn.setVisibility(View.GONE);
				mManualCreditCardBtn.setVisibility(View.GONE);
			}
			else if (hasValidCardSelected) {
				mManualCreditCardBtn.bind(bi);
				mStoredCreditCardBtn.setVisibility(View.GONE);
				mEmptyPaymentBtn.setVisibility(View.GONE);
				mManualCreditCardBtn.setVisibility(View.VISIBLE);
				mNewCCFakeSpinner.setVisibility(User.isLoggedIn(getActivity()) ? View.VISIBLE : View.INVISIBLE);
			}
			else {
				mStoredCreditCardBtn.setVisibility(View.GONE);
				mEmptyPaymentBtn.setVisibility(View.VISIBLE);
				mEmptyCCFakeSpinner.setVisibility(User.isLoggedIn(getActivity()) ? View.VISIBLE : View.INVISIBLE);
				mManualCreditCardBtn.setVisibility(View.GONE);
			}
		}
	}

	private View.OnClickListener mStoredCardButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			if (mStoredCardPopup == null) {
				mStoredCardPopup = new ListPopupWindow(getActivity());
				mStoredCardPopup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
						if (position == mStoredCreditCardAdapter.getCount() - 1) {
							mPaymentButtonListener.onAddNewCreditCardSelected();
							mStoredCardPopup.dismiss();
							return;
						}
						else if (position == 0) {
							return;
						}
						StoredCreditCard card = mStoredCreditCardAdapter.getItem(position);
						if (card != null && card.isSelectable()) {
							Db.getWorkingBillingInfoManager().shiftWorkingBillingInfo(new BillingInfo());
							// Don't allow selection of invalid card types.

							boolean isValidCard = true;
							if (getLob() == LineOfBusiness.FLIGHTS &&
								!Db.getTripBucket().getFlight().isPaymentTypeSupported(card.getType())) {
								isValidCard = false;
							}
							if (getLob() == LineOfBusiness.HOTELS &&
								!Db.getTripBucket().getHotel().isPaymentTypeSupported(card.getType())) {
								isValidCard = false;
							}

							if (isValidCard) {
								StoredCreditCard currentCC = Db.getBillingInfo().getStoredCard();
								if (currentCC != null) {
									BookingInfoUtils.resetPreviousCreditCardSelectState(getActivity(), currentCC);
								}
								Db.getWorkingBillingInfoManager().getWorkingBillingInfo().setStoredCard(card);
								Db.getWorkingBillingInfoManager().commitWorkingBillingInfoToDB();
								bindToDb();
								mStoredCardPopup.dismiss();
								mPaymentButtonListener.onStoredCreditCardChosen();
							}
						}
					}
				});
			}
			mStoredCardPopup.setAnchorView(view);
			mStoredCardPopup.setAdapter(mStoredCreditCardAdapter);
			mStoredCardPopup.setContentWidth(measureContentWidth(mStoredCreditCardAdapter));
			mStoredCardPopup.show();
		}
	};

	private View.OnClickListener mEditListener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			mPaymentButtonListener.onCreditCardEditButtonPressed();
		}
	};

	/**
	 * Returns whether a valid credit card is selected. Updates the button to show or hide a validation
	 * checkmark image.
	 */
	public boolean validate() {
		if (Db.hasBillingInfo()) {
			BillingInfo bi = Db.getBillingInfo();
			if (bi.hasStoredCard()) {
				boolean isValid = true;
				if (getLob() == LineOfBusiness.FLIGHTS) {
					isValid = Db.getTripBucket().getFlight().isPaymentTypeSupported(bi.getStoredCard().getType());
				}
				else if (getLob() == LineOfBusiness.HOTELS) {
					isValid = Db.getTripBucket().getHotel().isPaymentTypeSupported(bi.getStoredCard().getType());
				}
				return isValid;
			}
			else if (getLob() == LineOfBusiness.FLIGHTS) {
				FlightPaymentFlowState state = FlightPaymentFlowState.getInstance(getActivity());
				return state.hasAValidCardSelected(bi);
			}
			else if (getLob() == LineOfBusiness.HOTELS) {
				HotelPaymentFlowState state = HotelPaymentFlowState.getInstance(getActivity());
				return state.hasAValidCardSelected(bi);
			}
		}
		return false;
	}

	private ViewGroup mMeasureParent;

	// Copied from AOSP, ListPopupWindow.java
	private int measureContentWidth(ListAdapter adapter) {
		// Menus don't tend to be long, so this is more sane than it looks.
		int width = 0;
		View itemView = null;
		int itemType = 0;
		final int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		final int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		final int count = adapter.getCount();
		for (int i = 0; i < count; i++) {
			final int positionType = adapter.getItemViewType(i);
			if (positionType != itemType) {
				itemType = positionType;
				itemView = null;
			}
			if (mMeasureParent == null) {
				mMeasureParent = new FrameLayout(getActivity());
			}
			itemView = adapter.getView(i, itemView, mMeasureParent);
			itemView.measure(widthMeasureSpec, heightMeasureSpec);
			width = Math.max(width, itemView.getMeasuredWidth());
		}
		return width + 32;
	}
}
