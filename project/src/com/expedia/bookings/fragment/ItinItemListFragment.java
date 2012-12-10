package com.expedia.bookings.fragment;

import com.expedia.bookings.widget.ItinItemAdapter;

import android.os.Bundle;
import android.support.v4.app.ListFragment;

public class ItinItemListFragment extends ListFragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setListAdapter(new ItinItemAdapter(this.getActivity()));
	}
}
