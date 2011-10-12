package com.expedia.bookings.fragment;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TabletActivity;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.SearchResponse;
import com.expedia.bookings.fragment.EventManager.EventHandler;
import com.expedia.bookings.widget.HotelAdapter;

public class HotelListFragment extends ListFragment implements EventHandler {

	private HotelAdapter mAdapter;

	private ViewGroup mHeaderLayout;
	private TextView mNumHotelsTextView;
	private TextView mSortTypeTextView;
	private TextView mMessageTextView;

	private PopupWindow mSortPopup;

	public static HotelListFragment newInstance() {
		return new HotelListFragment();
	}

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		((TabletActivity) getActivity()).registerEventHandler(this);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mAdapter = new HotelAdapter(getActivity());
		setListAdapter(mAdapter);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_hotel_list, container, false);

		mHeaderLayout = (ViewGroup) view.findViewById(R.id.header_layout);
		mNumHotelsTextView = (TextView) view.findViewById(R.id.num_hotels_text_view);
		mSortTypeTextView = (TextView) view.findViewById(R.id.sort_type_text_view);
		mMessageTextView = (TextView) view.findViewById(android.R.id.empty);

		mSortTypeTextView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				((TabletActivity) getActivity()).showSortDialog();
			}
		});

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();

		mMessageTextView.setText(((TabletActivity) getActivity()).getSearchStatus());
	}

	@Override
	public void onDetach() {
		super.onDetach();
		((TabletActivity) getActivity()).unregisterEventHandler(this);
	}

	//////////////////////////////////////////////////////////////////////////
	// ListFragment overrides

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		((TabletActivity) getActivity()).propertySelected((Property) mAdapter.getItem(position));
	}

	//////////////////////////////////////////////////////////////////////////
	// Sort popup window

	public void showSortPopup() {
		if (mSortPopup.isShowing()) {
			return;
		}
	}

	public void hideSortPopup() {
		mSortPopup.dismiss();
	}

	//////////////////////////////////////////////////////////////////////////
	// EventHandler implementation

	@Override
	public void handleEvent(int eventCode, Object data) {
		switch (eventCode) {
		case TabletActivity.EVENT_SEARCH_STARTED:
			mAdapter.setSearchResponse(null);
			mMessageTextView.setText(R.string.progress_searching_hotels);
			mHeaderLayout.setVisibility(View.GONE);
			break;
		case TabletActivity.EVENT_SEARCH_PROGRESS:
			mMessageTextView.setText((String) data);
			break;
		case TabletActivity.EVENT_SEARCH_COMPLETE:
			SearchResponse searchResponse = (SearchResponse) data;
			mAdapter.setSearchResponse(searchResponse);
			updateNumHotels();
			updateSortLabel();
			mHeaderLayout.setVisibility(View.VISIBLE);
			break;
		case TabletActivity.EVENT_SEARCH_ERROR:
			mMessageTextView.setText((String) data);
			break;
		case TabletActivity.EVENT_FILTER_CHANGED:
			mAdapter.rebuildCache();
			updateNumHotels();
			updateSortLabel();
			break;
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Header views

	private void updateNumHotels() {
		int count = mAdapter.getCount();
		mNumHotelsTextView.setText(Html.fromHtml(getResources().getQuantityString(R.plurals.number_of_results, count,
				count)));
	}

	private void updateSortLabel() {
		SearchResponse searchResponse = ((TabletActivity) getActivity()).getSearchResultsToDisplay();
		String sortType = getString(searchResponse.getFilter().getSort().getDescriptionResId());
		mSortTypeTextView.setText(Html.fromHtml(getString(R.string.sort_hotels_template, sortType)));
	}
}
