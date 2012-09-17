package com.expedia.bookings.widget;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.StoredCreditCard;

public class StoredCardSpinnerAdapter extends BaseAdapter implements SpinnerAdapter {
	LayoutInflater mInflater;
	List<StoredCreditCard> mCards;
	Context mContext;
	int mSelected;

	public StoredCardSpinnerAdapter(Context context, List<StoredCreditCard> cards) {
		mCards = cards;
		mContext = context;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mSelected = 0;
	}

	public void setSelected(int pos) {
		mSelected = pos;
	}

	public int getSelected() {
		return mSelected;
	}

	public StoredCreditCard getSelectedCard() {
		return (StoredCreditCard) getItem(mSelected);
	}

	@Override
	public int getCount() {
		return mCards.size() + 1;
	}

	@Override
	public Object getItem(int position) {
		if (position < mCards.size()) {
			return mCards.get(position);
		}
		else {
			return null;
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		CardHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.row_stored_credit_card, parent, false);
			holder = new CardHolder();
			holder.image = (ImageView) convertView.findViewById(R.id.image);
			holder.title = (TextView) convertView.findViewById(R.id.title);
			holder.radioButton = (RadioButton) convertView.findViewById(R.id.radio_button);
			convertView.setTag(holder);
		}
		else {
			holder = (CardHolder) convertView.getTag();
		}

		if (holder.radioButton != null) {
			holder.radioButton.setChecked(position == mSelected);
		}

		StoredCreditCard card = (StoredCreditCard) getItem(position);
		if (card == null) {
			holder.title.setText(mContext.getString(R.string.enter_a_new_card));
			holder.image.setImageResource(R.drawable.ic_new_credit_card);
		}
		else {
			holder.title.setText(card.getDescription());
			if (position == mSelected) {
				holder.image.setImageResource(R.drawable.ic_selected_credit_card);
			}
			else {
				holder.image.setImageResource(R.drawable.ic_select_credit_card);
			}
		}

		return convertView;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		CardHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.selected_stored_credit_card, parent, false);
			holder = new CardHolder();
			holder.title = (TextView) convertView.findViewById(R.id.title);
			convertView.setTag(holder);
		}
		else {
			holder = (CardHolder) convertView.getTag();
		}

		TextView text = holder.title;

		StoredCreditCard card = (StoredCreditCard) getItem(position);
		if (card == null) {
			text.setText(mContext.getString(R.string.enter_a_new_card));
		}
		else {
			text.setText(card.getDescription());
		}

		return convertView;
	}

	private class CardHolder {
		public ImageView image;
		public TextView title;
		public RadioButton radioButton;
	}
}
