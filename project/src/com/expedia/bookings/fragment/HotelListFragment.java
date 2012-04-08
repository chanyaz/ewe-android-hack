package com.expedia.bookings.fragment;

import android.app.ListFragment;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchResultsFragmentActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.SearchResponse;
import com.expedia.bookings.widget.HotelAdapter;
import com.expedia.bookings.widget.PlaceholderTagProgressBar;

public class HotelListFragment extends ListFragment {

	private static final String INSTANCE_STATUS = "INSTANCE_STATUS";
	private static final String INSTANCE_SHOW_DISTANCES = "INSTANCE_SHOW_DISTANCES";

	private String mStatus;

	private boolean mShowDistances;

	private HotelAdapter mAdapter;

	private ViewGroup mHeaderLayout;
	private TextView mNumHotelsTextView;
	private TextView mSortTypeTextView;

	private PlaceholderTagProgressBar mSearchProgressBar;

	public static HotelListFragment newInstance() {
		return new HotelListFragment();
	}

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			mStatus = savedInstanceState.getString(INSTANCE_STATUS, null);
			mShowDistances = savedInstanceState.getBoolean(INSTANCE_SHOW_DISTANCES);
		}

		mAdapter = new HotelAdapter(getActivity());
		setListAdapter(mAdapter);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_hotel_list, container, false);

		mHeaderLayout = (ViewGroup) view.findViewById(R.id.header_layout);
		mNumHotelsTextView = (TextView) view.findViewById(R.id.num_hotels_text_view);
		mSortTypeTextView = (TextView) view.findViewById(R.id.sort_type_text_view);

		ViewGroup placeholderContainer = (ViewGroup) view.findViewById(android.R.id.empty);
		ProgressBar placeholderProgressBar = (ProgressBar) view.findViewById(R.id.placeholder_progress_bar);
		TextView placeholderProgressTextView = (TextView) view.findViewById(R.id.placeholder_progress_text_view);
		mSearchProgressBar = new PlaceholderTagProgressBar(placeholderContainer, placeholderProgressBar,
				placeholderProgressTextView);

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
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(INSTANCE_STATUS, mStatus);
		outState.putBoolean(INSTANCE_SHOW_DISTANCES, mShowDistances);
	}

	//////////////////////////////////////////////////////////////////////////
	// ListFragment overrides

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		((SearchResultsFragmentActivity) getActivity()).propertySelected((Property) mAdapter.getItem(position),
				SearchResultsFragmentActivity.SOURCE_LIST);
	}

	//////////////////////////////////////////////////////////////////////////
	// Fragment control

	public void setShowDistances(boolean showDistances) {
		mShowDistances = showDistances;

		if (mAdapter != null) {
			mAdapter.setShowDistance(mShowDistances);
		}
	}

	private void updateStatus(boolean showProgressBar) {
		updateStatus(mStatus, showProgressBar);
	}

	public void updateStatus(String status, boolean showProgressBar) {
		mStatus = status;

		if (mSearchProgressBar != null && mAdapter != null) {
			mSearchProgressBar.setText(status);
			mSearchProgressBar.setShowProgress(showProgressBar);
			setHeaderVisibility(View.GONE);
			mAdapter.setSearchResponse(null);
		}
	}

	public void notifySearchStarted() {
		mAdapter.setSelectedPosition(-1);
	}

	public void notifySearchComplete() {
		updateSearchResults();
	}

	public void notifyFilterChanged() {
		updateSearchResults();
	}

	public void notifyPropertySelected() {
		int position = getPositionOfProperty(Db.getSelectedProperty());
		if (position != mAdapter.getSelectedPosition()) {
			mAdapter.setSelectedPosition(position);
			mAdapter.notifyDataSetChanged();
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
		SearchResponse response = Db.getSearchResponse();
		if (response == null) {
			updateStatus(true);
		}
		else if (response.hasErrors()) {
			updateStatus(false);
		}
		else {
			updateSearchResults();
		}
	}

	private void updateSearchResults() {
		SearchResponse response = Db.getSearchResponse();
		mAdapter.setSearchResponse(response);

		// In case there is a currently selected property, select it on the screen.
		mAdapter.setSelectedPosition(getPositionOfProperty(Db.getSelectedProperty()));

		if (response.getPropertiesCount() == 0) {
			setHeaderVisibility(View.GONE);
			mSearchProgressBar.setText(R.string.ean_error_no_results);
			mSearchProgressBar.setShowProgress(false);
		}
		else if (mAdapter.getCount() == 0) {
			setHeaderVisibility(View.GONE);
			mSearchProgressBar.setText(R.string.no_filter_results);
			mSearchProgressBar.setShowProgress(false);
		}
		else {
			updateNumHotels();
			updateSortLabel(response);
			setHeaderVisibility(View.VISIBLE);
			mAdapter.setShowDistance(mShowDistances);
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
