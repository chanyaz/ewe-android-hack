package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.expedia.bookings.data.trips.TripComponent;

public class ItinItemAdapter extends BaseAdapter {
	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	private Context mContext;
	private List<TripComponent> mItems = new ArrayList<TripComponent>();

	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTOR
	//////////////////////////////////////////////////////////////////////////////////////

	public ItinItemAdapter(Context context) {
		mContext = context;
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// OVERRIDES
	//////////////////////////////////////////////////////////////////////////////////////

	@Override
	public int getCount() {
		if (mItems != null) {
			return mItems.size();
		}

		return 0;
	}

	@Override
	public TripComponent getItem(int position) {
		if (mItems != null) {
			return mItems.get(position);
		}

		return null;
	}

	@Override
	public long getItemId(int position) {
		if (mItems != null) {
			return position;
		}

		return -1;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup Parent) {
		ItinCard card;
		if (convertView != null && convertView instanceof ItinCard) {
			card = (ItinCard) convertView;
		}
		else {
			card = new HotelItinCard(mContext);
		}

		//bind card and stuff...
		card.bind(getItem(position));
		card.showSummary(position == 0);

		return card;
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	public void addItinItem(TripComponent item) {
		mItems.add(item);
		notifyDataSetChanged();
	}

	public void addAllItinItems(List<TripComponent> items) {
		mItems.addAll(items);
		notifyDataSetChanged();
	}

	public void clearItinItems() {
		mItems.clear();
		notifyDataSetChanged();
	}

	public void setItinItems(List<TripComponent> items) {
		mItems = items;
		notifyDataSetChanged();
	}
}