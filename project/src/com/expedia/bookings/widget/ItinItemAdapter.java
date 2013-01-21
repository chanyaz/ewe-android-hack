package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.expedia.bookings.data.trips.TripComponent;

public class ItinItemAdapter extends BaseAdapter {
	ArrayList<TripComponent> mItems = new ArrayList<TripComponent>();
	Context mContext;

	public ItinItemAdapter(Context context) {
		mContext = context;
	}

	@Override
	public int getCount() {
		return mItems.size();
	}

	@Override
	public TripComponent getItem(int arg0) {
		return mItems.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup Parent) {
		ItinCard card;
		if (convertView == null) {
			card = new HotelItinCard(mContext);
		}
		else {
			card = (ItinCard) convertView;
		}

		//bind card and stuff...
		card.bind(getItem(position));

		card.showExpanded(position == 0);
		card.showBottomPadding(position == getCount() - 1);

		return card;
	}

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
		clearItinItems();
		addAllItinItems(items);
	}
}
