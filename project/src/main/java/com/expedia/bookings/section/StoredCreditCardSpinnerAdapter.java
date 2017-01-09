package com.expedia.bookings.section;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.PaymentType;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.data.trips.TripBucketItem;
import com.expedia.bookings.data.utils.ValidFormOfPaymentUtils;
import com.expedia.bookings.utils.AccessibilityUtil;
import com.expedia.bookings.utils.BookingInfoUtils;
import com.expedia.bookings.utils.CreditCardUtils;
import com.expedia.bookings.widget.ContactDetailsCompletenessStatus;
import com.expedia.bookings.widget.ContactDetailsCompletenessStatusImageView;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.Ui;
import com.expedia.bookings.data.Db;
import com.squareup.phrase.Phrase;

public class StoredCreditCardSpinnerAdapter extends ArrayAdapter<StoredCreditCard> {

	private static final int ITEM_VIEW_TYPE_SELECT_CREDITCARD = 0;
	private static final int ITEM_VIEW_TYPE_CREDITCARD = 1;
	private static final int ITEM_VIEW_TYPE_TEMP_CREDITCARD = 2;
	private static final int ITEM_VIEW_TYPE_ADD_CREDITCARD = 3;
	private static final int ITEM_VIEW_TYPE_COUNT = 4;

	private TripBucketItem mTripBucketItem;

	//It will be set true if user chose 'Save' on filling in new card details. If he chose 'No Thanks', it will be set false.
	private boolean hasTemporarilySavedCard = false;

	//In tablet stored credit card is list popup in which we show 2 additional cards 'Saved cards' and 'Add New card'
	boolean isTablet = AndroidUtils.isTablet(getContext());

	public StoredCreditCardSpinnerAdapter(Context context, TripBucketItem item) {
		super(context, R.layout.traveler_autocomplete_row);
		mTripBucketItem = item;
		hasTemporarilySavedCard = Db.getTemporarilySavedCard() != null;
	}

	@Override
	public int getCount() {
		//If it is a tablet then count increases by 2 because of 2 additional cards 'Saved cards' and 'Add New card' and 'Add New card' shown in tablet
		return getAvailableStoredCards().size() + (hasTemporarilySavedCard ? 1 : 0) + (isTablet ? 2 : 0);
	}

	@Override
	public StoredCreditCard getItem(int position) {
		int itemType = getItemViewType(position);
		if (itemType == ITEM_VIEW_TYPE_CREDITCARD) {
			if (getCount() > position - (hasTemporarilySavedCard ? 1 : 0)) {
				//In case of tablet 0th position is acquired by 'Saved cards' tile
				return getAvailableStoredCards().get(position - (isTablet ? 1 : 0));
			}
		}
		return null;
	}

	public boolean isTemporarilySavedCard(int position) {
		return getItemViewType(position) == ITEM_VIEW_TYPE_TEMP_CREDITCARD;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return getView(position, convertView, parent);
	}

	@Override
	public int getViewTypeCount() {
		return ITEM_VIEW_TYPE_COUNT;
	}

	@Override
	public int getItemViewType(int position) {
		/* In phones Temporarily saved card is positioned at the bottom of the Stored Cards List.
		Whereas in tablet last position is acquired by the card 'Add New card' tile
		*/
		if (hasTemporarilySavedCard && position == getCount() - 1 - (isTablet ? 1 : 0)) {
			return ITEM_VIEW_TYPE_TEMP_CREDITCARD;
		}
		else if (isTablet && position == 0) {
			return ITEM_VIEW_TYPE_SELECT_CREDITCARD;
		}
		else if (isTablet && position == getCount() - 1) {
			return ITEM_VIEW_TYPE_ADD_CREDITCARD;
		}
		else {
			return ITEM_VIEW_TYPE_CREDITCARD;
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final int itemType = getItemViewType(position);

		View retView = convertView;
		TextView tv;
		switch(itemType) {
		case ITEM_VIEW_TYPE_SELECT_CREDITCARD:
			retView = View.inflate(getContext(), R.layout.credit_card_autocomplete_row, null);
			tv = Ui.findView(retView, R.id.text1);
			tv.setText(R.string.saved_cards);
			tv.setCompoundDrawablesWithIntrinsicBounds(
				getContext().getResources().getDrawable(R.drawable.saved_payment), null, null, null);
			retView.setEnabled(false);
			break;
		case ITEM_VIEW_TYPE_CREDITCARD:
			StoredCreditCard card = getItem(position);
			if (card.isGoogleWallet()) {
				// Hide google wallet stored card because it is not supported
				retView = View.inflate(getContext(), R.layout.hidden_credit_card, null);
			}
			else {
				retView = View.inflate(getContext(), R.layout.credit_card_autocomplete_row, null);
			}
			retView.setEnabled(card.isSelectable());

			retView = bindStoredCardTile(retView, card.getType(),
				Phrase.from(getContext(), R.string.stored_card_TEMPLATE)
					.put("cardtype", card.getDescription()).format().toString(),
				(Db.getBillingInfo().getStoredCard() != null
					&& Db.getBillingInfo().getStoredCard().compareTo(card) == 0));
			break;
		case ITEM_VIEW_TYPE_ADD_CREDITCARD:
			retView = View.inflate(getContext(), R.layout.credit_card_autocomplete_row, null);
			tv = Ui.findView(retView, R.id.text1);
			tv.setText(R.string.add_new_card);
			tv.setCompoundDrawablesWithIntrinsicBounds(
				getContext().getResources().getDrawable(R.drawable.add_plus), null, null, null);
			break;
		case ITEM_VIEW_TYPE_TEMP_CREDITCARD:
			retView = View.inflate(getContext(), R.layout.credit_card_autocomplete_row, null);
			BillingInfo info = Db.getTemporarilySavedCard();
			PaymentType cardType = info.getPaymentType();
			retView = bindStoredCardTile(retView, cardType, getUserSavedTemporaryCardText(cardType, info.getNumber()), info.getSaveCardToExpediaAccount());
			retView.setEnabled(!Db.getTemporarilySavedCard().getSaveCardToExpediaAccount());
			break;
		}

		return retView;
	}

	private View bindStoredCardTile(View retView, PaymentType cardType, String storedCardName, Boolean isSelectable) {
		TextView tv;
		tv = Ui.findView(retView, R.id.text1);
		ContactDetailsCompletenessStatusImageView rStatus = Ui.findView(retView, R.id.card_info_status_icon);
		rStatus.setStatus(
			isSelectable ? ContactDetailsCompletenessStatus.COMPLETE : ContactDetailsCompletenessStatus.DEFAULT);

		String storedCardText = storedCardName;
		// Show a special icon for an invalid credit card (can happen in flights mode)
		int imgRes = R.drawable.unsupported_card;
		if (mTripBucketItem != null) {
			boolean isValidCard = mTripBucketItem.isPaymentTypeSupported(cardType);
			if (isValidCard) {
				if (rStatus.getVisibility() == View.GONE) {
					rStatus.setVisibility(View.VISIBLE);
				}
				imgRes = BookingInfoUtils.getTabletCardIcon(cardType);
			}
			else {
				rStatus.setVisibility(View.GONE);
				storedCardText = ValidFormOfPaymentUtils.getInvalidFormOfPaymentMessage(getContext(), cardType, mTripBucketItem.getLineOfBusiness());
				String disabledString = getContext().getString(R.string.accessibility_cont_desc_card_is_disabled);
				AccessibilityUtil.appendRoleContDesc(tv, storedCardText + ", " + disabledString, R.string.accessibility_cont_desc_role_button);
			}
		}
		tv.setCompoundDrawablesWithIntrinsicBounds(imgRes, 0, 0, 0);
		tv.setText(storedCardText);
		return retView;
	}

	private String getUserSavedTemporaryCardText(PaymentType paymentType, String cardNumber) {
		return Phrase.from(getContext(), R.string.temporarily_saved_card_TEMPLATE)
			.put("cardtype", CreditCardUtils.getHumanReadableCardTypeName(getContext(), paymentType))
			.put("cardno", cardNumber.substring(cardNumber.length() - 4, cardNumber.length()))
			.format().toString();
	}

	private List<StoredCreditCard> getAvailableStoredCards() {
		return BookingInfoUtils.getStoredCreditCards(getContext());
	}

	public void refresh(TripBucketItem item) {
		mTripBucketItem = item;
		hasTemporarilySavedCard = Db.getTemporarilySavedCard() != null;
		notifyDataSetChanged();
	}
}
