package com.expedia.bookings.section;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.data.TripBucketItem;
import com.expedia.bookings.utils.BookingInfoUtils;
import com.expedia.bookings.utils.WalletUtils;
import com.expedia.bookings.widget.RoundImageView;
import com.mobiata.android.util.Ui;

public class StoredCreditCardSpinnerAdapter extends ArrayAdapter<StoredCreditCard> {

	private static final int ITEM_VIEW_TYPE_SELECT_CREDITCARD = 0;
	private static final int ITEM_VIEW_TYPE_CREDITCARD = 1;
	private static final int ITEM_VIEW_TYPE_ADD_CREDITCARD = 2;
	private static final int ITEM_VIEW_TYPE_COUNT = 3;

	private TripBucketItem mTripBucketItem;
	private boolean isAddStoredCardEnabled = true;

	public StoredCreditCardSpinnerAdapter(Context context, TripBucketItem item) {
		super(context, R.layout.traveler_autocomplete_row);
		mTripBucketItem = item;
	}

	public StoredCreditCardSpinnerAdapter(Context context, TripBucketItem item, boolean addStoredCardEnabled) {
		super(context, R.layout.traveler_autocomplete_row);
		mTripBucketItem = item;
		isAddStoredCardEnabled = addStoredCardEnabled;
	}

	@Override
	public int getCount() {
		return getAvailableStoredCards().size() + (isAddStoredCardEnabled ? 2 : 1);
	}

	@Override
	public StoredCreditCard getItem(int position) {
		int itemType = getItemViewType(position);
		if (itemType == ITEM_VIEW_TYPE_CREDITCARD) {
			if (getCount() > position - 1) {
				return getAvailableStoredCards().get(position - 1);
			}
		}
		return null;
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
		if (isAddStoredCardEnabled && position == getCount() - 1) {
			return ITEM_VIEW_TYPE_ADD_CREDITCARD;
		}
		else if (position == 0) {
			return ITEM_VIEW_TYPE_SELECT_CREDITCARD;
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
		ImageView icon;
		RoundImageView rIcon;
		switch(itemType) {
		case ITEM_VIEW_TYPE_SELECT_CREDITCARD:
			retView = View.inflate(getContext(), R.layout.travelers_popup_header_footer_row, null);
			tv = Ui.findView(retView, R.id.text1);
			icon = Ui.findView(retView, R.id.icon);
			tv.setText(R.string.saved_creditcards);
			icon.setBackgroundResource(R.drawable.saved_payment);
			retView.setEnabled(false);
			break;
		case ITEM_VIEW_TYPE_CREDITCARD:
			StoredCreditCard card = getItem(position);
			if (card.isGoogleWallet() && !WalletUtils.isWalletSupported(mTripBucketItem.getLineOfBusiness())) {
				// Hide google wallet stored card if its not supported
				retView = View.inflate(getContext(), R.layout.hidden_credit_card, null);
			}
			else {
				retView = View.inflate(getContext(), R.layout.credit_card_autocomplete_row, null);
			}

			tv = Ui.findView(retView, R.id.text1);
			rIcon = Ui.findView(retView, R.id.icon);
			tv.setText(card.getDescription());
			retView.setEnabled(card.isSelectable());

			// Show a special icon for an invalid credit card (can happen in flights mode)
			boolean isValidCard = true;
			if (mTripBucketItem != null) {
				isValidCard = mTripBucketItem.isCardTypeSupported(card.getType());
			}

			int imgRes = isValidCard ? BookingInfoUtils.getTabletCardIcon(card.getType()) :
				R.drawable.ic_tablet_checkout_disabled_credit_card;
			rIcon.setImageDrawable(getContext().getResources().getDrawable(imgRes));
			break;
		case ITEM_VIEW_TYPE_ADD_CREDITCARD:
			retView = View.inflate(getContext(), R.layout.travelers_popup_header_footer_row, null);
			tv = Ui.findView(retView, R.id.text1);
			icon = Ui.findView(retView, R.id.icon);
			tv.setText(R.string.add_new_creditcard);
			icon.setBackgroundResource(R.drawable.add_plus);
			break;
		}

		return retView;
	}

	private List<StoredCreditCard> getAvailableStoredCards() {
		return BookingInfoUtils.getStoredCreditCards(getContext());
	}

}
