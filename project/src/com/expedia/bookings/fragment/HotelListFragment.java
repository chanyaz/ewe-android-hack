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
import com.mobiata.android.util.Ui;

public class HotelListFragment extends ListFragment {

	private static final String INSTANCE_SHOW_DISTANCES = "INSTANCE_SHOW_DISTANCES";

	private boolean mShowDistances;
	private boolean mListNeedsReset = false;

	private HotelAdapter mAdapter;

	private ViewGroup mHotelListHeader;
	private TextView mSearchDateRangeText;
	private TextView mLawyerLabelTextView;

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
			mShowDistances = savedInstanceState.getBoolean(INSTANCE_SHOW_DISTANCES);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mListener = Ui.findFragmentListener(this, HotelListFragmentListener.class);

		mAdapter = new HotelAdapter(getActivity());

		mAdapter.highlightSelectedPosition(false);

		mListener.onHotelListFragmentAttached(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_hotel_list, container, false);

		// Configure ListView
		ListView listView = Ui.findView(view, android.R.id.list);

		mHotelListHeader = Ui.inflate(inflater, R.layout.include_hotel_list_header, null, false);

		// In order for setVisibility() to work consistently on mHeaderLayout as an
		// official headerView, it needs to be wrapped in another ViewGroup.
		FrameLayout layout = new FrameLayout(getActivity());
		layout.addView(mHotelListHeader);
		listView.addHeaderView(layout);

		mSearchDateRangeText = (TextView) mHotelListHeader.findViewById(R.id.search_date_range_text);
		mLawyerLabelTextView = (TextView) mHotelListHeader.findViewById(R.id.lawyer_label_text_view);

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
		if (response == null || response.hasErrors()) {
			// ignore
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
			UserPriceType priceType = searchResponse.getUserPriceType();
			if (priceType == null) {
				mLawyerLabelTextView.setText(null);
			}
			else if (priceType == UserPriceType.RATE_FOR_WHOLE_STAY_WITH_TAXES) {
				mLawyerLabelTextView.setText(getString(R.string.total_price_for_stay));
			}
			else {
				mLawyerLabelTextView.setText(getString(R.string.prices_avg_per_night));
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

		if (response.getPropertiesCount() == 0) {
			setHeaderVisibility(View.GONE);
		}
		else if (mAdapter.getCount() == 0) {
			setHeaderVisibility(View.GONE);
		}
		else {
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
		public void onListItemClicked(Property property, int position);
	}
}
