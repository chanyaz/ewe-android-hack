package com.expedia.bookings.fragment;

import java.util.Calendar;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.SearchResponse;
import com.expedia.bookings.utils.LocaleUtils;
import com.expedia.bookings.widget.HotelAdapter;
import com.expedia.bookings.widget.PlaceholderTagProgressBar;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.Ui;

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
	private boolean mListNeedsReset = false;

	private HotelAdapter mAdapter;

	private ViewGroup mHotelListHeader;
	private TextView mSearchDateRangeText;
	private TextView mSortTypeTextView;
	private TextView mLawyerLabelTextView;

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
			mStatus = savedInstanceState.getString(INSTANCE_STATUS);
			mShowDistances = savedInstanceState.getBoolean(INSTANCE_SHOW_DISTANCES);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof HotelListFragmentListener)) {
			throw new RuntimeException("HotelListFragment Activity must implement HotelListFragmentListener!");
		}

		mListener = (HotelListFragmentListener) activity;

		mAdapter = new HotelAdapter(getActivity());

		// Disable highlighting if we're on phone UI
		mAdapter.highlightSelectedPosition(AndroidUtils.isHoneycombTablet(activity));
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_hotel_list, container, false);

		// Configure ListView
		ListView listView = Ui.findView(view, android.R.id.list);
		listView.setOnScrollListener(this);

		mHotelListHeader = (ViewGroup) view.findViewById(R.id.hotel_list_header);

		// We expect hotel_list_header to be missing on phone. In this case, add it as a list header
		if (mHotelListHeader == null) {
			mHotelListHeader = (ViewGroup) inflater.inflate(R.layout.include_hotel_list_header, null, false);

			// In order for setVisibility() to work consistently on mHeaderLayout as an
			// official headerView, it needs to be wrapped in another ViewGroup.
			FrameLayout layout = new FrameLayout(getActivity());
			layout.addView(mHotelListHeader);

			listView.addHeaderView(layout);
		}

		mSearchDateRangeText = (TextView) mHotelListHeader.findViewById(R.id.search_date_range_text);
		mSortTypeTextView = (TextView) mHotelListHeader.findViewById(R.id.sort_type_text_view);
		mLawyerLabelTextView = (TextView) mHotelListHeader.findViewById(R.id.lawyer_label_text_view);

		ViewGroup placeholderContainer = (ViewGroup) view.findViewById(R.id.placeholder_container);
		ProgressBar placeholderProgressBar = (ProgressBar) view.findViewById(R.id.placeholder_progress_bar);
		TextView placeholderProgressTextView = (TextView) view.findViewById(R.id.placeholder_progress_text_view);
		mSearchProgressBar = new PlaceholderTagProgressBar(placeholderContainer, placeholderProgressBar,
				placeholderProgressTextView);

		if (mSortTypeTextView != null) {
			mSortTypeTextView.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					mListener.onSortButtonClicked();
				}
			});
		}

		updateLawyerLabel();

		// Configure the phone vs. tablet ui different
		if (!AndroidUtils.isHoneycombTablet(getActivity())) {
			mSearchProgressBar.setVisibility(View.GONE);

			Ui.findView(view, R.id.no_filter_results_text_view).setVisibility(View.VISIBLE);
		}

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		setListAdapter(mAdapter);
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

		if (position - l.getHeaderViewsCount() > -1) {
			mListener.onListItemClicked((Property) mAdapter.getItem(position - l.getHeaderViewsCount()), position);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Fragment control

	public void setShowDistances(boolean showDistances) {
		mShowDistances = showDistances;

		if (mAdapter != null) {
			mAdapter.setShowDistance(mShowDistances);
		}
	}

	private void updateLawyerLabel() {
		boolean isTablet = AndroidUtils.isTablet(getActivity());
		if (LocaleUtils.doesPointOfSaleHaveInclusivePricing(getActivity())) {
			if (isTablet) {
				mLawyerLabelTextView.setText(getString(R.string.total_price_for_stay_punctuated));
			}
			else {
				mLawyerLabelTextView.setText(getString(R.string.total_price_for_stay));
			}
		}
		else {
			if (isTablet) {
				mLawyerLabelTextView.setText(getString(R.string.prices_avg_per_night_short));
			}
			else {
				mLawyerLabelTextView.setText(getString(R.string.prices_avg_per_night));
			}
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
		mListNeedsReset = true;
	}

	public void notifySearchComplete() {
		updateSearchResults();
		if (mListNeedsReset) {
			resetToTop();
			mListNeedsReset = false;
		}
	}

	public void notifyFilterChanged() {
		if (Db.getSearchResponse() != null) {
			updateSearchResults();
			resetToTop();
		}
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
		if (mSearchDateRangeText != null) {
			SearchParams params = Db.getSearchParams();
			CharSequence from = DateFormat.format("MMM d", params.getCheckInDate());
			CharSequence to = DateFormat.format("MMM d", params.getCheckOutDate());
			mSearchDateRangeText.setText(getString(R.string.date_range_TEMPLATE, from, to));
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

		if (Db.getSelectedProperty() != null) {
			// In case there is a currently selected property, select it on the screen.
			int position = getPositionOfProperty(Db.getSelectedProperty());
			mAdapter.setSelectedPosition(position);
		}

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
			setHeaderVisibility(View.VISIBLE);
			updateLawyerLabel();
			mAdapter.setShowDistance(mShowDistances);
		}
	}

	private void resetToTop() {
		if (Db.getSelectedProperty() == null) {
			final ListView lv = getListView();
			lv.post(new Runnable() {
				@Override
				public void run() {
					lv.setSelection(0);
				}
			});
		}
	}

	private void setHeaderVisibility(int visibility) {
		if (mHotelListHeader != null) {
			mHotelListHeader.setVisibility(visibility);
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
