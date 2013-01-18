package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;

import com.expedia.bookings.R;
import com.expedia.bookings.data.trips.TripComponent;

public class ItinItemAdapter extends BaseAdapter {
	private Context mContext;
	private int mLaunchHeaderHeight;

	private List<TripComponent> mItems = new ArrayList<TripComponent>();

	public ItinItemAdapter(Context context) {
		mContext = context;
		mLaunchHeaderHeight = context.getResources().getDimensionPixelSize(R.dimen.launch_header_height);
	}

	@Override
	public int getCount() {
		if (mItems != null) {
			return mItems.size() + 1;
		}

		return 1;
	}

	@Override
	public TripComponent getItem(int position) {
		if (mItems != null) {
			return mItems.get(position - 1);
		}

		return null;
	}

	@Override
	public long getItemId(int position) {
		if (mItems != null) {
			return position - 1;
		}

		return -1;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup Parent) {
		if (position > 0) {
			ItinCard card;
			if (convertView != null && convertView instanceof ItinCard) {
				card = (ItinCard) convertView;
			}
			else {
				card = new HotelItinCard(mContext);
			}

			//bind card and stuff...
			card.bind(getItem(position));

			card.showExpanded(position == 1);
			card.showBottomPadding(position == getCount() - 1);

			return card;
		}
		else {
			View view = new View(mContext);
			view.setLayoutParams(new LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, mLaunchHeaderHeight));

			return view;
		}
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
		mItems = items;
		notifyDataSetChanged();
	}
}
