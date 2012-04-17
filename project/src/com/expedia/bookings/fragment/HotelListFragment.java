package com.expedia.bookings.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.SearchResponse;
import com.expedia.bookings.widget.HotelAdapter;
import com.expedia.bookings.widget.PlaceholderTagProgressBar;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.Ui;

// TODO: ADD SCROLLBAR
// TODO: ADD PHONE HEADER
public class HotelListFragment extends ListFragment implements OnScrollListener {

	// MAX_THUMBNAILS is not often hit, due to how the algorithm works.  More often,
	// the # of thumbnails is somewhere between MAX_THUMBNAILS / 2 and MAX_THUMBNAILS.
	private static final int MAX_THUMBNAILS = 50;

	// The tolerance of change of ListView center item before we do another imageview trim
	private static final int TRIM_TOLERANCE = 5;

	private static final String INSTANCE_STATUS = "INSTANCE_STATUS";
	private static final String INSTANCE_SHOW_DISTANCES = "INSTANCE_SHOW_DISTANCES";

	private String mStatus;

	private boolean mShowDistances;

	private HotelAdapter mAdapter;

	private ViewGroup mHeaderLayout;
	private TextView mNumHotelsTextView;
	private TextView mSortTypeTextView;

	private PlaceholderTagProgressBar mSearchProgressBar;

	private HotelListFragmentListener mListener;

	// Last center item we trimmed on
	private int mLastCenter = -999;

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
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof HotelListFragmentListener)) {
			throw new RuntimeException("HotelListFragment Activity must implement HotelListFragmentListener!");
		}

		mListener = (HotelListFragmentListener) activity;
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
				mListener.onSortButtonClicked();
			}
		});

		// Configure ListView
		ListView listView = Ui.findView(view, android.R.id.list);
		listView.setOnScrollListener(this);

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

		mListener.onListItemClicked((Property) mAdapter.getItem(position), position);
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
			// Never display the header on non-tablet UIs
			if (!AndroidUtils.isHoneycombTablet(getActivity())) {
				visibility = View.GONE;
			}

			mHeaderLayout.setVisibility(visibility);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// OnScrollListener

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		// Trim the ends (recycle images)
		if (totalItemCount > MAX_THUMBNAILS) {
			final int center = firstVisibleItem + (visibleItemCount / 2);

			// Don't always trim drawables; only trim them if we've moved the list far enough away from where
			// we last were.
			if (center < mLastCenter - TRIM_TOLERANCE || center > mLastCenter + TRIM_TOLERANCE) {
				mLastCenter = center;

				int start = center - (MAX_THUMBNAILS / 2);
				int end = center + (MAX_THUMBNAILS / 2);

				// prevent overflow
				start = start < 0 ? 0 : start;
				end = end > totalItemCount ? totalItemCount : end;

				mAdapter.trimDrawables(start, end);
			}
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// Do nothing
	}

	//////////////////////////////////////////////////////////////////////////
	// Listener

	public interface HotelListFragmentListener {
		public void onSortButtonClicked();

		public void onListItemClicked(Property property, int position);
	}
}
