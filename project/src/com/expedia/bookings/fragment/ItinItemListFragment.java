package com.expedia.bookings.fragment;

import com.expedia.bookings.widget.ItinItemAdapter;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

public class ItinItemListFragment extends ListFragment {

	public static final String TAG = "TAG_ITIN_ITEM_LIST_FRAGMENT";

	public static ItinItemListFragment newInstance() {
		return new ItinItemListFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setListAdapter(new ItinItemAdapter(this.getActivity()));

	}

	@Override
	public void onResume() {
		super.onResume();

		this.getListView().setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
				int first = arg0.getFirstVisiblePosition();
				int last = Math.min(arg0.getLastVisiblePosition() + 1, arg0.getChildCount());
				for (int i = first; i < last && i >= 0 && i < arg0.getChildCount(); i++) {
					arg0.getChildAt(i).invalidate();
				}
			}

			@Override
			public void onScrollStateChanged(AbsListView arg0, int arg1) {
				// TODO Auto-generated method stub

			}
		});
	}
}
