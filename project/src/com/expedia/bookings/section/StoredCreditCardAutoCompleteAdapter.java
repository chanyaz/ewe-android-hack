package com.expedia.bookings.section;

import java.util.List;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.utils.BookingInfoUtils;
import com.mobiata.android.util.Ui;

public class StoredCreditCardAutoCompleteAdapter extends ArrayAdapter<StoredCreditCard> implements Filterable {

	private StoredCreditCardFilter mFilter = new StoredCreditCardFilter();

	public StoredCreditCardAutoCompleteAdapter(Context context) {
		super(context, R.layout.traveler_autocomplete_row);
	}

	@Override
	public int getCount() {
		return getAvailableStoredCards().size();
	}

	@Override
	public StoredCreditCard getItem(int position) {
		if (getCount() > position) {
			return getAvailableStoredCards().get(position);
		}
		return null;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		StoredCreditCard card = getItem(position);

		View retView = convertView;
		if (retView == null) {
			retView = View.inflate(getContext(), R.layout.stored_creditcard_autocomplete_row, null);
		}

		TextView tv = Ui.findView(retView, android.R.id.text1);
		ImageView icon = Ui.findView(retView, android.R.id.icon);
		tv.setText(card.getDescription());
		icon.setImageResource(BookingInfoUtils.getTabletCardIcon(card.getType()));

		return retView;
	}

	private List<StoredCreditCard> getAvailableStoredCards() {
		return BookingInfoUtils.getStoredCreditCards(getContext());
	}

	@Override
	public Filter getFilter() {
		return mFilter;
	}

	private class StoredCreditCardFilter extends Filter {

		@Override
		//The return value of this gets stuck into the EditText when we select an item from the dropdown
		public CharSequence convertResultToString(Object resultValue) {
			return "";
		}

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			//We never filter stored credit cards
			return new FilterResults();
		}

		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			notifyDataSetChanged();
		}
	}

}
