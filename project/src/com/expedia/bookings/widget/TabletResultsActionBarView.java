package com.expedia.bookings.widget;

import org.joda.time.LocalDate;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.RelativeLayout;

import com.actionbarsherlock.app.ActionBar;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.TabletResultsActivity.GlobalResultsState;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.graphics.PercentageFadeColorDrawable;
import com.expedia.bookings.interfaces.ITabletResultsController;
import com.expedia.bookings.utils.ColumnManager;
import com.expedia.bookings.utils.JodaUtils;
import com.mobiata.android.util.Ui;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class TabletResultsActionBarView extends RelativeLayout implements ITabletResultsController {

	private ColumnManager mColumnManager = new ColumnManager(3);
	private GlobalResultsState mResultsState = GlobalResultsState.DEFAULT;

	private ActionBar mActionBar;
	private PercentageFadeColorDrawable mActionBarBg;

	private TextView mSearchBar;
	private TextView mFlightsTitleTv;
	private TextView mHotelsTitleTv;

	private DisableableClickWrapper mSearchBarClickListener;

	public TabletResultsActionBarView(Context context) {
		super(context);
		init(context, null);
	}

	public TabletResultsActionBarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attr) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.actionbar_tablet_results, this);
		mSearchBar = Ui.findView(view, R.id.results_search_bar);
		mFlightsTitleTv = Ui.findView(view, R.id.title_text_flights);
		mHotelsTitleTv = Ui.findView(view, R.id.title_text_hotels);

		mActionBarBg = new PercentageFadeColorDrawable(getResources()
				.getColor(R.color.tablet_results_ab_default),
				getResources().getColor(R.color.tablet_results_ab_flights));
	}

	public void bindFromDb(Context context) {
		if (Db.getFlightSearch() != null && Db.getFlightSearch().getSearchParams() != null) {
			String city = Db.getFlightSearch().getSearchParams().getArrivalLocation().getCity();
			String flightTitle = context.getResources().getString(
					R.string.actionbar_tablet_results_flights_title_TEMPLATE, city);
			String hotelTitle = context.getResources().getString(
					R.string.actionbar_tablet_results_hotels_title_TEMPLATE, city);

			mFlightsTitleTv.setText(flightTitle);
			mHotelsTitleTv.setText(hotelTitle);

			String dateStr;
			int flags = DateUtils.FORMAT_NO_YEAR | DateUtils.FORMAT_ABBREV_MONTH | DateUtils.FORMAT_ABBREV_WEEKDAY;
			LocalDate startDate = Db.getFlightSearch().getSearchParams().getDepartureDate();
			if (Db.getFlightSearch().getSearchParams().isRoundTrip()) {
				LocalDate endDate = Db.getFlightSearch().getSearchParams().getReturnDate();
				dateStr = JodaUtils.formatDateRange(context, startDate, endDate, flags);
			}
			else {
				dateStr = JodaUtils.formatLocalDate(context, startDate, flags);
			}

			mSearchBar.setText(context.getResources().getString(R.string.destination_and_date_range_TEMPLATE, city,
					dateStr));
		}
	}

	public void attachToActionBar(ActionBar bar) {
		mActionBar = bar;
		mActionBar.setDisplayShowTitleEnabled(false);
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setBackgroundDrawable(mActionBarBg);
		mActionBar.setDisplayShowCustomEnabled(true);
		mActionBar.setCustomView(this);
	}

	public void setSearchBarOnClickListener(final OnClickListener listener) {
		mSearchBarClickListener = new DisableableClickWrapper(listener);
		mSearchBar.setOnClickListener(mSearchBarClickListener);
	};

	/**
	 * ITabletResultsController STUFF
	 */

	@Override
	public void setGlobalResultsState(GlobalResultsState state) {
		mResultsState = state;
		switch (mResultsState) {
		case HOTELS: {
			mSearchBar.setVisibility(View.INVISIBLE);
			mFlightsTitleTv.setVisibility(View.INVISIBLE);
			mHotelsTitleTv.setVisibility(View.VISIBLE);
			mActionBarBg.setPercentage(1f);
			break;
		}
		case FLIGHTS: {
			mSearchBar.setVisibility(View.INVISIBLE);
			mFlightsTitleTv.setVisibility(View.VISIBLE);
			mHotelsTitleTv.setVisibility(View.INVISIBLE);
			mActionBarBg.setPercentage(1f);
			break;
		}
		default: {
			mSearchBar.setVisibility(View.VISIBLE);
			mFlightsTitleTv.setVisibility(View.INVISIBLE);
			mHotelsTitleTv.setVisibility(View.INVISIBLE);
			mActionBarBg.setPercentage(0f);
			break;
		}
		}

		if (mSearchBarClickListener != null) {
			mSearchBarClickListener.setClickEnabled(mResultsState == GlobalResultsState.DEFAULT);
		}
	}

	@Override
	public void setAnimatingTowardsVisibility(GlobalResultsState state) {
		switch (state) {
		case HOTELS: {
			mHotelsTitleTv.setVisibility(View.VISIBLE);
			if (mResultsState == GlobalResultsState.DEFAULT) {
				mHotelsTitleTv.setAlpha(0f);
			}
			break;
		}
		case FLIGHTS: {
			mFlightsTitleTv.setVisibility(View.VISIBLE);
			if (mResultsState == GlobalResultsState.DEFAULT) {
				mFlightsTitleTv.setAlpha(0f);
			}
			break;
		}
		default: {
			mSearchBar.setVisibility(View.VISIBLE);
			break;
		}
		}
	}

	@Override
	public void setHardwareLayerForTransition(int layerType, GlobalResultsState stateOne, GlobalResultsState stateTwo) {
		if ((stateOne == GlobalResultsState.DEFAULT || stateOne == GlobalResultsState.FLIGHTS)
				&& (stateTwo == GlobalResultsState.DEFAULT || stateTwo == GlobalResultsState.FLIGHTS)) {
			//to or from flights mode
			mSearchBar.setLayerType(layerType, null);
			mFlightsTitleTv.setLayerType(layerType, null);
		}
		else if ((stateOne == GlobalResultsState.DEFAULT || stateOne == GlobalResultsState.HOTELS)
				&& (stateTwo == GlobalResultsState.DEFAULT || stateTwo == GlobalResultsState.HOTELS)) {
			//to or from hotels mode
			mSearchBar.setLayerType(layerType, null);
			mHotelsTitleTv.setLayerType(layerType, null);
		}
	}

	@Override
	public void blockAllNewTouches(View requester) {
		if (mSearchBarClickListener != null) {
			mSearchBarClickListener.setClickEnabled(false);
		}
	}

	@Override
	public void animateToFlightsPercentage(float percentage) {
		mSearchBar.setAlpha(percentage);
		mActionBarBg.setPercentage(1f - percentage);
		mFlightsTitleTv.setAlpha(1f - percentage);

	}

	@Override
	public void animateToHotelsPercentage(float percentage) {
		mSearchBar.setAlpha(percentage);
		mActionBarBg.setPercentage(1f - percentage);
		mHotelsTitleTv.setAlpha(1f - percentage);
	}

	@Override
	public void updateContentSize(int totalWidth, int totalHeight) {
		mColumnManager.setTotalWidth(totalWidth);

		//We set the search bar to be centered between the app icon and left edge of the 3rd column
		int width = mColumnManager.getColWidth(0) + mColumnManager.getColWidth(1) - 2 * getLeft();
		int left = Math.round(getLeft() / 2f);
		LayoutParams params = (LayoutParams) mSearchBar.getLayoutParams();
		params.width = width;
		params.leftMargin = left;
		mSearchBar.setLayoutParams(params);
	}

	@Override
	public boolean handleBackPressed() {
		return false;
	}

	/*
	 * DisableableClickWrapper - A class for wrapping an OnClickListener and not firing onClick if disabled
	 */

	private class DisableableClickWrapper implements OnClickListener {
		private OnClickListener mListener;
		private boolean mClickEnabled = true;

		public DisableableClickWrapper(OnClickListener listener) {
			mListener = listener;
		}

		public void setClickEnabled(boolean enabled) {
			mClickEnabled = enabled;
		}

		@Override
		public void onClick(View arg0) {
			if (mClickEnabled) {
				mListener.onClick(arg0);
			}
		}
	}

}
