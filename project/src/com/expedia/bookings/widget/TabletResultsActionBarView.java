package com.expedia.bookings.widget;

import org.joda.time.LocalDate;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.enums.ResultsState;
import com.expedia.bookings.graphics.PercentageFadeColorDrawable;
import com.expedia.bookings.interfaces.IBackManageable;
import com.expedia.bookings.interfaces.IMeasurementListener;
import com.expedia.bookings.interfaces.helpers.BackManager;
import com.expedia.bookings.interfaces.helpers.StateListenerHelper;
import com.expedia.bookings.utils.GridManager;
import com.expedia.bookings.utils.JodaUtils;
import com.mobiata.android.util.Ui;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class TabletResultsActionBarView extends RelativeLayout implements IMeasurementListener, IBackManageable {

	private GridManager mGrid = new GridManager();
	private ResultsState mResultsState = ResultsState.OVERVIEW;

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

	@Override
	public void onDetachedFromWindow() {
		super.onDetachedFromWindow();
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
		if (Db.getFlightSearch() != null && Db.getFlightSearch().getSearchParams() != null
				&& Db.getFlightSearch().getSearchParams().getArrivalLocation() != null
				&& !TextUtils.isEmpty(Db.getFlightSearch().getSearchParams().getArrivalLocation().getCity())
				&& Db.getFlightSearch().getSearchParams().getDepartureDate() != null) {

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

	/*
	 * RESULTS STATE LISTENER
	 */

	public StateListenerHelper<ResultsState> mStateHelper = new StateListenerHelper<ResultsState>() {

		@Override
		public void onStateTransitionStart(ResultsState stateOne, ResultsState stateTwo) {
			//Touch
			if (mSearchBarClickListener != null) {
				mSearchBarClickListener.setClickEnabled(false);
			}

			//Vis
			if (stateOne == ResultsState.HOTELS || stateTwo == ResultsState.HOTELS) {
				mHotelsTitleTv.setVisibility(View.VISIBLE);
				if (mResultsState == ResultsState.OVERVIEW) {
					mHotelsTitleTv.setAlpha(0f);
				}
			}
			if (stateOne == ResultsState.FLIGHTS || stateTwo == ResultsState.FLIGHTS) {
				mFlightsTitleTv.setVisibility(View.VISIBLE);
				if (mResultsState == ResultsState.OVERVIEW) {
					mFlightsTitleTv.setAlpha(0f);
				}
			}
			if (stateOne == ResultsState.OVERVIEW || stateTwo == ResultsState.OVERVIEW) {
				mSearchBar.setVisibility(View.VISIBLE);
			}

			//layer type
			int layerType = View.LAYER_TYPE_HARDWARE;
			if ((stateOne == ResultsState.OVERVIEW || stateOne == ResultsState.FLIGHTS)
					&& (stateTwo == ResultsState.OVERVIEW || stateTwo == ResultsState.FLIGHTS)) {
				//to or from flights mode
				mSearchBar.setLayerType(layerType, null);
				mFlightsTitleTv.setLayerType(layerType, null);
			}
			else if ((stateOne == ResultsState.OVERVIEW || stateOne == ResultsState.HOTELS)
					&& (stateTwo == ResultsState.OVERVIEW || stateTwo == ResultsState.HOTELS)) {
				//to or from hotels mode
				mSearchBar.setLayerType(layerType, null);
				mHotelsTitleTv.setLayerType(layerType, null);
			}

		}

		@Override
		public void onStateTransitionUpdate(ResultsState stateOne, ResultsState stateTwo, float percentage) {
			if (stateOne == ResultsState.OVERVIEW && stateTwo == ResultsState.FLIGHTS) {
				mSearchBar.setAlpha(1f - percentage);
				mActionBarBg.setPercentage(percentage);
				mFlightsTitleTv.setAlpha(percentage);
			}
			else if (stateOne == ResultsState.FLIGHTS && stateTwo == ResultsState.OVERVIEW) {
				mSearchBar.setAlpha(percentage);
				mActionBarBg.setPercentage(1f - percentage);
				mFlightsTitleTv.setAlpha(1f - percentage);
			}

			if (stateOne == ResultsState.OVERVIEW && stateTwo == ResultsState.HOTELS) {
				mSearchBar.setAlpha(1f - percentage);
				mActionBarBg.setPercentage(percentage);
				mHotelsTitleTv.setAlpha(percentage);
			}
			else if (stateOne == ResultsState.HOTELS && stateTwo == ResultsState.OVERVIEW) {
				mSearchBar.setAlpha(percentage);
				mActionBarBg.setPercentage(1f - percentage);
				mHotelsTitleTv.setAlpha(1f - percentage);
			}

		}

		@Override
		public void onStateTransitionEnd(ResultsState stateOne, ResultsState stateTwo) {
			//Touch
			if (mSearchBarClickListener != null) {
				mSearchBarClickListener.setClickEnabled(true);
			}

			//layer type
			int layerType = View.LAYER_TYPE_NONE;
			if ((stateOne == ResultsState.OVERVIEW || stateOne == ResultsState.FLIGHTS)
					&& (stateTwo == ResultsState.OVERVIEW || stateTwo == ResultsState.FLIGHTS)) {
				//to or from flights mode
				mSearchBar.setLayerType(layerType, null);
				mFlightsTitleTv.setLayerType(layerType, null);
			}
			else if ((stateOne == ResultsState.OVERVIEW || stateOne == ResultsState.HOTELS)
					&& (stateTwo == ResultsState.OVERVIEW || stateTwo == ResultsState.HOTELS)) {
				//to or from hotels mode
				mSearchBar.setLayerType(layerType, null);
				mHotelsTitleTv.setLayerType(layerType, null);
			}

		}

		@Override
		public void onStateFinalized(ResultsState state) {
			mResultsState = state;
			switch (mResultsState) {
			case HOTELS: {
				mHotelsTitleTv.setAlpha(1f);
				mSearchBar.setVisibility(View.INVISIBLE);
				mFlightsTitleTv.setVisibility(View.INVISIBLE);
				mHotelsTitleTv.setVisibility(View.VISIBLE);
				mActionBarBg.setPercentage(1f);
				break;
			}
			case FLIGHTS: {
				mFlightsTitleTv.setAlpha(1f);
				mSearchBar.setVisibility(View.INVISIBLE);
				mFlightsTitleTv.setVisibility(View.VISIBLE);
				mHotelsTitleTv.setVisibility(View.INVISIBLE);
				mActionBarBg.setPercentage(1f);
				break;
			}
			default: {
				mSearchBar.setAlpha(1f);
				mSearchBar.setVisibility(View.VISIBLE);
				mFlightsTitleTv.setVisibility(View.INVISIBLE);
				mHotelsTitleTv.setVisibility(View.INVISIBLE);
				mActionBarBg.setPercentage(0f);
				break;
			}
			}

			if (mSearchBarClickListener != null) {
				mSearchBarClickListener.setClickEnabled(mResultsState == ResultsState.OVERVIEW);
			}

		}

	};

	/*
	 * IMeasurementListener
	 */

	@Override
	public void onContentSizeUpdated(int totalWidth, int totalHeight, boolean isLandscape) {
		//Setup grid manager
		mGrid.setGridSize(1, 3);
		mGrid.setDimensions(totalWidth, totalHeight);

		//We set the search bar to be centered between the app icon and left edge of the 3rd column
		int width = mGrid.getColWidth(0) + mGrid.getColWidth(1) - 2 * getLeft();
		int left = Math.round(getLeft() / 2f);
		LayoutParams params = (LayoutParams) mSearchBar.getLayoutParams();
		params.width = width;
		params.leftMargin = left;
		mSearchBar.setLayoutParams(params);
	}

	/*
	 * BACK STACK MANAGEMENT
	 */

	@Override
	public BackManager getBackManager() {
		return mBackManager;
	}

	private BackManager mBackManager = new BackManager(this) {

		@Override
		public boolean handleBackPressed() {
			return false;
		}

	};

}
