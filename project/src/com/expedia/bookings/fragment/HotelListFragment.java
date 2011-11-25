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
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchResultsFragmentActivity;
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

	public static HotelListFragment newInstance() {
		return new HotelListFragment();
	}

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		((SearchResultsFragmentActivity) getActivity()).mEventManager.registerEventHandler(this);
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
				((SearchResultsFragmentActivity) getActivity()).showSortDialog();
			}
		});

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		updateViews();
	}

	@Override
	public void onDetach() {
		super.onDetach();

		((SearchResultsFragmentActivity) getActivity()).mEventManager.unregisterEventHandler(this);
	}

	//////////////////////////////////////////////////////////////////////////
	// ListFragment overrides

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		((SearchResultsFragmentActivity) getActivity()).propertySelected((Property) mAdapter.getItem(position));
	}

	//////////////////////////////////////////////////////////////////////////
	// EventHandler implementation

	@Override
	public void handleEvent(int eventCode, Object data) {
		switch (eventCode) {
		case SearchResultsFragmentActivity.EVENT_SEARCH_STARTED:
			mAdapter.setSelectedPosition(-1);
			displaySearchStatus();
			break;
		case SearchResultsFragmentActivity.EVENT_SEARCH_PROGRESS:
			displaySearchStatus();
			break;
		case SearchResultsFragmentActivity.EVENT_SEARCH_COMPLETE:
			updateSearchResults();
			break;
		case SearchResultsFragmentActivity.EVENT_SEARCH_ERROR:
			displaySearchError();
			break;
		case SearchResultsFragmentActivity.EVENT_FILTER_CHANGED:
			updateSearchResults();
			break;
		case SearchResultsFragmentActivity.EVENT_PROPERTY_SELECTED:
			int position = getPositionOfProperty((Property) data);
			if (position != mAdapter.getSelectedPosition()) {
				mAdapter.setSelectedPosition(position);
				mAdapter.notifyDataSetChanged();
			}
			break;
		}
	}

	private int getPositionOfProperty(Property property) {
		if (property != null) {
			int count = mAdapter.getCount();
			for (int position = 0; position < count; position++) {
				if (mAdapter.getItem(position) == property) {
					return position;
				}
			}
		}
		return -1;
	}

	//////////////////////////////////////////////////////////////////////////
	// Header views

	private void updateNumHotels() {
		// only update if view has been initialized
		if (mNumHotelsTextView != null) {
			int count = mAdapter.getCount();
			mNumHotelsTextView.setText(Html.fromHtml(getResources().getQuantityString(R.plurals.number_of_results,
					count, count)));
		}
	}

	private void updateSortLabel(SearchResponse response) {
		// only update if view has been initialized
		if (mSortTypeTextView != null) {
			String sortType = getString(response.getFilter().getSort().getDescriptionResId());
			mSortTypeTextView.setText(Html.fromHtml(getString(R.string.sort_hotels_template, sortType)));
		}
	}

	private void updateViews() {
		SearchResponse response = getInstance().mSearchResponse;
		if (response == null) {
			displaySearchStatus();
		}
		else if (response.hasErrors()) {
			displaySearchError();
		}
		else {
			updateSearchResults();
		}
	}

	private void displaySearchStatus() {
		if (mMessageTextView != null && mAdapter != null) {
			mMessageTextView.setText(getInstance().mSearchStatus);
			setHeaderVisibility(View.GONE);
			mAdapter.setSearchResponse(null);
		}
	}

	private void displaySearchError() {
		if (mMessageTextView != null && mAdapter != null) {
			String errorMsg = getInstance().mSearchStatus;
			mMessageTextView.setText(errorMsg);
			setHeaderVisibility(View.GONE);
			mAdapter.setSearchResponse(null);
		}
	}

	private void updateSearchResults() {
		SearchResponse response = getInstance().mSearchResponse;
		mAdapter.setSearchResponse(response);

		// In case there is a currently selected property, select it on the screen.
		mAdapter.setSelectedPosition(getPositionOfProperty(getInstance().mProperty));

		if (response.getPropertiesCount() == 0) {
			setHeaderVisibility(View.GONE);
			mMessageTextView.setText(R.string.ean_error_no_results);
		}
		else if (mAdapter.getCount() == 0) {
			setHeaderVisibility(View.GONE);
			mMessageTextView.setText(R.string.no_filter_results);
		}
		else {
			updateNumHotels();
			updateSortLabel(response);
			setHeaderVisibility(View.VISIBLE);
			mAdapter.setShowDistance(getInstance().mShowDistance);
		}
	}

	private void setHeaderVisibility(int visibility) {
		if (mHeaderLayout != null) {
			mHeaderLayout.setVisibility(visibility);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Convenience method

	public SearchResultsFragmentActivity.InstanceFragment getInstance() {
		return ((SearchResultsFragmentActivity) getActivity()).mInstance;
	}
}
