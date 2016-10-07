package com.expedia.bookings.fragment;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearch.FlightTripQuery;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.FlightTripLeg;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.enums.ResultsFlightsListState;
import com.expedia.bookings.enums.ResultsListState;
import com.expedia.bookings.fragment.base.ResultsListFragment;
import com.expedia.bookings.interfaces.IResultsFlightSelectedListener;
import com.expedia.bookings.widget.FlightAdapter;
import com.expedia.bookings.widget.TabletFlightAdapter;
import com.expedia.bookings.widget.TextView;
import com.mobiata.android.util.Ui;

/**
 * ResultsFlightListFragment: The flight list fragment designed for tablet results 2013
 */
public class ResultsFlightListFragment extends ResultsListFragment<ResultsFlightsListState> {

	public interface IFlightListHeaderClickListener {
		void onTopRightClicked();
	}

	private static final String STATE_LEG_NUMBER = "STATE_LEG_NUMBER";

	private TextView mStickySubtitleTv;
	private TextView mCardFeeWarningTv;

	private ListAdapter mAdapter;
	private int mLegNumber = -1;
	private IResultsFlightSelectedListener mFlightSelectedListener;
	private IFlightListHeaderClickListener mListHeaderClickListener;

	private boolean mEnableOnListItemClick = true;

	private float mListHeaderRevealHeight;

	public static ResultsFlightListFragment getInstance(int legPosition) {
		ResultsFlightListFragment frag = new ResultsFlightListFragment();
		frag.setLegPosition(legPosition);
		return frag;
	}

	@Override
	public int getLayoutResId() {
		return R.layout.fragment_tablet_results_flight_list;
	}

	@Override
	public float getMaxHeaderTranslateY() {
		if (getActivity() == null) {
			return 0f;
		}
		return getListView().getTop()
			+ getListView().getMaxDistanceFromTop()
			+ getListView().getPaddingTop()
			+ getResources().getDimension(R.dimen.results_list_header_reveal_height)
			- getStickyHeader().getTop()
			- getStickyHeader().getHeight();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mLegNumber = savedInstanceState.getInt(STATE_LEG_NUMBER, -1);
		}
		setListViewContentDescription(R.string.tablet_results_flight_list_cont_desc);
		View v = super.onCreateView(inflater, container, savedInstanceState);
		mStickySubtitleTv = Ui.findView(v, R.id.sticky_subtitle);
		setSubtitleText();
		mCardFeeWarningTv = Ui.findView(v, R.id.card_fee_warning_text);
		setCardFeeWarningTextAndVisibility();
		return v;
	}

	public void setListHeaderExpansionPercentage(float percentage) {
		getListView().setTranslationY(mListHeaderRevealHeight * percentage);
		mStickySubtitleTv.setAlpha(percentage);
		mCardFeeWarningTv.setAlpha(percentage);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(STATE_LEG_NUMBER, mLegNumber);
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);

		mFlightSelectedListener = Ui.findFragmentListener(this, IResultsFlightSelectedListener.class);
		mListHeaderClickListener = Ui.findFragmentListener(this, IFlightListHeaderClickListener.class, false);

		mListHeaderRevealHeight = getResources().getDimension(R.dimen.results_list_header_reveal_height);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		if (mAdapter != null) {
			mAdapter.unregisterDataSetObserver(mDataSetObserver);
		}
	}

	public void setLegPosition(int legNumber) {
		mLegNumber = legNumber;
	}

	/**
	 * Call this any time that you need to reset the query. Basically, any time that you move forward,
	 * in the flights flow, by selecting a leg, you want to clear the query of the next leg. The reason
	 * is that the flights present in the second leg is dependent upon which flight has been selected
	 * from the first leg. This method call takes care of resetting the query, and ensuring all observers
	 * are setup to properly receive changes, from the filter, for instance.
	 */
	public void resetQuery() {
		Db.getFlightSearch().setSelectedLeg(mLegNumber, null);
		Db.getFlightSearch().clearQuery(mLegNumber);
		resetAdapterQuery();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if (mEnableOnListItemClick && (getPercentage() == 1 || getPercentage() == 0)) {
			int headerCount = getListView().getHeaderViewsCount();
			int itemPosition = position - headerCount;
			if (itemPosition >= 0) {
				FlightTrip trip = ((FlightAdapter) mAdapter).getItem(itemPosition);
				if (trip != null) {
					Db.getFlightSearch().setSelectedLeg(mLegNumber, new FlightTripLeg(trip, trip.getLeg(mLegNumber)));
					mFlightSelectedListener.onFlightSelected(mLegNumber);
					getListView().setItemChecked(position, true);
				}
			}
		}
	}

	@Override
	protected ListAdapter initializeAdapter() {
		FlightAdapter adapter = new TabletFlightAdapter();
		mAdapter = adapter;
		mAdapter.registerDataSetObserver(mDataSetObserver);

		// Setup data
		adapter.setLegPosition(mLegNumber);

		resetAdapterQuery();
		return mAdapter;
	}

	private void resetAdapterQuery() {
		FlightAdapter adapter = (FlightAdapter) mAdapter;
		if (Db.getFlightSearch() != null && Db.getFlightSearch().getSearchResponse() != null && adapter != null) {
			if (mLegNumber > 0) {
				FlightTripQuery previousQuery = Db.getFlightSearch().queryTrips(mLegNumber - 1);
				adapter.setFlightTripQuery(Db.getFlightSearch().queryTrips(mLegNumber),
					previousQuery.getMinTime(),
					previousQuery.getMaxTime());
			}
			else {
				adapter.setFlightTripQuery(Db.getFlightSearch().queryTrips(mLegNumber));
			}
		}
	}

	@Override
	protected CharSequence initializeStickyHeaderString() {
		int count = mAdapter == null ? 0 : mAdapter.getCount();

		CharSequence text;
		if (mLegNumber < 1) {
			text = getResources().getQuantityString(R.plurals.x_Flights_TEMPLATE, count, count);
		}
		else {
			text = getResources().getQuantityString(R.plurals.x_Return_Flights_TEMPLATE, count, count);
		}
		return text;
	}

	@Override
	protected OnClickListener initializeTopRightTextButtonOnClickListener() {
		return new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (mListHeaderClickListener == null) {
					ResultsFlightListFragment.this.setPercentage(1f, 200);
				}
				else {
					mListHeaderClickListener.onTopRightClicked();
				}
			}

		};
	}

	@Override
	protected boolean initializeTopRightTextButtonEnabled() {
		return mLegNumber <= 0;
	}

	private final DataSetObserver mDataSetObserver = new DataSetObserver() {
		@Override
		public void onChanged() {
			setStickyHeaderText(initializeStickyHeaderString());
			setSubtitleText();
		}
	};

	private void setSubtitleText() {
		if (mStickySubtitleTv != null) {
			int labelResId = Db.getFlightSearch().getSearchParams().isRoundTrip() ? R.string.prices_roundtrip_label :
				R.string.prices_oneway_label;
			mStickySubtitleTv.setText(getString(labelResId));
		}
	}

	private void setCardFeeWarningTextAndVisibility() {
		boolean isVisible = PointOfSale.getPointOfSale().shouldShowAirlinePaymentMethodFeeMessage();
		if (isVisible) {
			if (PointOfSale.getPointOfSale().airlineMayChargePaymentMethodFee()) {
				mCardFeeWarningTv.setText(getString(R.string.airline_may_charge_notice));
			}
			else {
				mCardFeeWarningTv.setText(getString(R.string.airline_charge_notice));
			}
		}
		mCardFeeWarningTv.setVisibility(isVisible ? View.VISIBLE : View.GONE);
	}

	@Override
	protected ResultsFlightsListState translateState(ResultsListState state) {
		if (state == ResultsListState.AT_TOP) {
			return ResultsFlightsListState.FLIGHTS_LIST_AT_TOP;
		}
		else if (state == ResultsListState.AT_BOTTOM) {
			return ResultsFlightsListState.FLIGHTS_LIST_AT_BOTTOM;
		}
		return null;
	}

	@Override
	protected ResultsFlightsListState getDefaultState() {
		return ResultsFlightsListState.FLIGHTS_LIST_AT_BOTTOM;
	}
}
