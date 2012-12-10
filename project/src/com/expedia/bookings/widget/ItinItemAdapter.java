package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import com.expedia.bookings.data.ItinItem;
import com.expedia.bookings.data.ItinItem.ItinItemType;
import com.expedia.bookings.data.Itinerary;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class ItinItemAdapter extends BaseAdapter {

	ArrayList<ItinItem> mItems = new ArrayList<ItinItem>();
	Context mContext;

	public ItinItemAdapter(Context context) {
		mContext = context;

		//TODO: REMOVE!
		for (int i = 0; i < 5; i++) {
			ItinItem item = new ItinItem();
			item.setItinType(i % 2 == 0 ? ItinItemType.FLIGHT : ItinItemType.HOTEL);
			mItems.add(item);
		}
	}

	@Override
	public int getCount() {
		return mItems.size();
	}

	@Override
	public ItinItem getItem(int arg0) {
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
			card = new ItinCard(mContext);
		}
		else {
			card = (ItinCard) convertView;
		}

		//bind card and stuff...
		//card.bind(getItem(position));

		card.showExpanded(position == 0);

		return card;
	}

	public void addItinItem(ItinItem item) {
		mItems.add(item);
	}

	public void addAllItinItems(List<ItinItem> items) {
		mItems.addAll(items);
	}

	public void clearItinItems() {
		mItems.clear();
	}

	public void setItinItems(List<ItinItem> items) {
		clearItinItems();
		addAllItinItems(items);
	}
}
