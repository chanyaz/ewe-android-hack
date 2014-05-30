package com.expedia.bookings.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.HotelSearchResponse;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate.UserPriceType;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.utils.CalendarUtils;
import com.expedia.bookings.utils.LayoutUtils;
import com.expedia.bookings.widget.HotelAdapter;
import com.expedia.bookings.widget.PlaceholderTagProgressBar;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.Ui;

public class HotelListFragment extends ListFragment {

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

		mListener = Ui.findFragmentListener(this, HotelListFragmentListener.class);

		mAdapter = new HotelAdapter(getActivity());

		// Disable highlighting if we're on phone UI
		mAdapter.highlightSelectedPosition(AndroidUtils.isHoneycombTablet(activity));

		mListener.onHotelListFragmentAttached(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_hotel_list, container, false);

		// Configure ListView
		ListView listView = Ui.findView(view, android.R.id.list);

		mHotelListHeader = (ViewGroup) view.findViewById(R.id.hotel_list_header);

		// We expect hotel_list_header to be missing on phone. In this case, add it as a list header
		if (mHotelListHeader == null) {
			mHotelListHeader = Ui.inflate(inflater, R.layout.include_hotel_list_header, null, false);

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

		if (getActivity() != null) {
			boolean shouldShowVipIcon = PointOfSale.getPointOfSale().supportsVipAccess()
					&& User.isElitePlus(getActivity());
			mAdapter.setShowVipIcon(shouldShowVipIcon);
		}

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

	public void showPlaceholder() {
		if (mSearchProgressBar != null) {
			mSearchProgressBar.setVisibility(View.VISIBLE);
		}
	}

	public void hidePlaceholder() {
		if (mSearchProgressBar != null) {
			mSearchProgressBar.setVisibility(View.GONE);
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
			mSearchProgressBar.setVisibility(View.VISIBLE);
			setHeaderVisibility(View.GONE);
			mAdapter.setSearchResponse(null);
		}
	}

	public void notifySearchStarted() {
		if (mAdapter != null) {
			mAdapter.setSelectedPosition(-1);
			mListNeedsReset = true;
		}
	}

	public void notifySearchComplete() {
		// #1303: Don't execute if not attached to Activity
		if (!isAdded()) {
			return;
		}

		updateViews();
		if (mListNeedsReset) {
			resetToTop();
			mListNeedsReset = false;
		}
	}

	public void notifyFilterChanged() {
		if (Db.getHotelSearch().getSearchResponse() != null) {
			updateViews();
			resetToTop();
		}
	}

	public void notifyPropertySelected() {
		int position = mAdapter.getPositionOfProperty(Db.getHotelSearch().getSelectedProperty());
		if (position != mAdapter.getSelectedPosition()) {
			mAdapter.setSelectedPosition(position);
			mAdapter.notifyDataSetChanged();
		}
	}

	public void clearSelectedProperty() {
		mAdapter.setSelectedPosition(-1);
		mAdapter.notifyDataSetChanged();
	}

	//////////////////////////////////////////////////////////////////////////
	// Update views

	private void updateViews() {
		// Update header
		updateHeaderLawyerLabel();
		updateHeaderDateRange();

		// Update ListItem or show status
		HotelSearchResponse response = Db.getHotelSearch().getSearchResponse();
		if (response == null) {
			updateStatus(true);
		}
		else if (response.hasErrors()) {
			updateStatus(false);
		}
		else {
			updateSearchResults();
		}

		if (mAdapter != null) {
			mAdapter.setShowDistance(mShowDistances);
		}
	}

	private void updateHeaderLawyerLabel() {
		HotelSearchResponse searchResponse = Db.getHotelSearch().getSearchResponse();
		if (mLawyerLabelTextView != null && searchResponse != null) {
			boolean isTablet = ExpediaBookingApp.useTabletInterface(getActivity());
			UserPriceType priceType = searchResponse.getUserPriceType();
			if (priceType == null) {
				mLawyerLabelTextView.setText(null);
			}
			else if (priceType == UserPriceType.RATE_FOR_WHOLE_STAY_WITH_TAXES) {
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
	}

	private void updateHeaderDateRange() {
		// only update if view has been initialized
		if (mSearchDateRangeText != null) {
			HotelSearchParams params = Db.getHotelSearch().getSearchParams();

			if (CalendarUtils.isSearchDateTonight(params)) {
				mSearchDateRangeText.setText(getString(R.string.Tonight));
			}
			else {
				mSearchDateRangeText.setText(CalendarUtils.formatDateRange2(getActivity(), params,
						DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH));
			}
		}
	}

	private void updateSearchResults() {
		HotelSearchResponse response = Db.getHotelSearch().getSearchResponse();
		mAdapter.setSearchResponse(response);

		if (Db.getHotelSearch().getSelectedProperty() != null) {
			// In case there is a currently selected property, select it on the screen.
			mAdapter.setSelectedProperty(Db.getHotelSearch().getSelectedProperty());
		}

		if (response.getPropertiesCount() == 0) {
			showPlaceholder();
			setHeaderVisibility(View.GONE);
			mSearchProgressBar.setText(LayoutUtils.noHotelsFoundMessage(getActivity()));
			mSearchProgressBar.setShowProgress(false);
		}
		else if (mAdapter.getCount() == 0) {
			showPlaceholder();
			setHeaderVisibility(View.GONE);
			mSearchProgressBar.setText(R.string.no_filter_results);
			mSearchProgressBar.setShowProgress(false);
		}
		else {
			hidePlaceholder();
			setHeaderVisibility(View.VISIBLE);
			updateHeaderLawyerLabel();
		}
	}

	private void resetToTop() {
		if (Db.getHotelSearch().getSelectedProperty() == null) {
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
	// Listener

	public interface HotelListFragmentListener {
		public void onHotelListFragmentAttached(HotelListFragment fragment);

		public void onSortButtonClicked();
		public void onListItemClicked(Property property, int position);
	}
}
