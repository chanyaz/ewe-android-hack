package com.expedia.bookings.fragment;

import android.app.Activity;
import android.database.DataSetObserver;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.section.SectionFlightLeg;
import com.expedia.bookings.section.SectionFlightLeg.SectionFlightLegListener;
import com.expedia.bookings.widget.FlightAdapter;
import com.mobiata.android.util.Ui;
import com.mobiata.flightlib.data.Airport;
import com.mobiata.flightlib.data.sources.FlightStatsDbUtils;

// IMPLEMENTATION NOTE: This implementation heavily leans towards the user only picking
// two legs of a flight (outbound and inbound).  If you want to adapt it for 3+ legs, you
// will need to rewrite a good portion of it.
public class FlightListFragment extends ListFragment implements SectionFlightLegListener, OnScrollListener {

	public static final String TAG = FlightListFragment.class.getName();

	private static final String INSTANCE_LEG_POSITION = "INSTANCE_LEG_POSITION";

	private FlightAdapter mAdapter;

	private FlightListFragmentListener mListener;

	private ListView mListView;
	private TextView mNumFlightsTextView;
	private SectionFlightLeg mSectionFlightLeg;

	private int mLegPosition;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			mLegPosition = savedInstanceState.getInt(INSTANCE_LEG_POSITION);
		}
		else {
			mLegPosition = 0;
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof FlightListFragmentListener)) {
			throw new RuntimeException("FlightListFragment activity must implement FlightListFragmentListener!");
		}

		mListener = (FlightListFragmentListener) activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// If the parent is finishing already, don't bother with displaying the data
		if (getActivity().isFinishing()) {
			return null;
		}

		View v = inflater.inflate(R.layout.fragment_flight_list, container, false);

		// Configure the header
		mListView = Ui.findView(v, android.R.id.list);
		mListView.setDividerHeight(0);
		ViewGroup header = (ViewGroup) inflater.inflate(R.layout.snippet_flight_header, mListView, false);
		mNumFlightsTextView = Ui.findView(header, R.id.num_flights_text_view);
		mSectionFlightLeg = Ui.findView(header, R.id.flight_leg);
		mSectionFlightLeg.setListener(this);
		mListView.addHeaderView(header);
		mListView.setHeaderDividersEnabled(false);

		displayHeaderLeg();

		// Add the adapter
		mAdapter = new FlightAdapter(getActivity(), savedInstanceState);
		mAdapter.registerDataSetObserver(mDataSetObserver);
		setListAdapter(mAdapter);

		// Set initial data
		onLegPositionChanged();

		return v;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(INSTANCE_LEG_POSITION, mLegPosition);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		mAdapter.destroy();
		mAdapter = null;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		// Adjust clicks for the header view count
		int numHeaderViews = l.getHeaderViewsCount();
		if (position < numHeaderViews) {
			return;
		}

		// Notify that a flight leg was clicked
		FlightTrip trip = mAdapter.getItem(position - numHeaderViews);
		FlightLeg leg = trip.getLeg(mLegPosition);
		mListener.onFlightLegClick(trip, leg, mLegPosition);
	}

	//////////////////////////////////////////////////////////////////////////
	// Header control

	private boolean usesDynamicBlur() {
		return Build.VERSION.SDK_INT >= 11;
	}

	private void displayHeaderLeg() {
		if (mSectionFlightLeg != null) {
			if (mLegPosition == 0) {
				if (usesDynamicBlur()) {
					mSectionFlightLeg.setVisibility(View.INVISIBLE);
				}
				else {
					mSectionFlightLeg.setVisibility(View.GONE);
				}
			}
			else {
				mSectionFlightLeg.setVisibility(View.VISIBLE);
				mSectionFlightLeg.bind(Db.getFlightSearch().getSelectedLegs()[0], true);
			}
		}
	}

	private void displayNumFlights() {
		if (mNumFlightsTextView != null) {
			int count = mAdapter.getCount();
			if (count == 0) {
				mNumFlightsTextView.setText(null);
			}
			else {
				FlightSearchParams params = Db.getFlightSearch().getSearchParams();
				String airportCode = (mLegPosition == 0) ? params.getArrivalAirportCode() : params
						.getDepartureAirportCode();
				Airport airport = FlightStatsDbUtils.getAirport(airportCode);
				String city = airport.mCity;
				if (TextUtils.isEmpty(city)) {
					city = airportCode;
				}
				mNumFlightsTextView.setText(getResources().getQuantityString(R.plurals.num_flights_to_destination,
						count, count, city).toUpperCase());
			}
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// List control

	public void reset() {
		mLegPosition = 0;
		mAdapter.setFlightTripQuery(null);
	}

	public void setLegPosition(int legPosition) {
		mLegPosition = legPosition;

		if (isAdded()) {
			onLegPositionChanged();
		}
	}

	public int getLegPosition() {
		return mLegPosition;
	}

	public void onLegPositionChanged() {
		mAdapter.setLegPosition(mLegPosition);

		mAdapter.setFlightTripQuery(Db.getFlightSearch().queryTrips(mLegPosition));

		// Scroll to top after reloading list with new results
		if (mListView != null) {
			mListView.setSelection(0);

			// Only dynamically blur background if there is no header
			// flight card being shown.
			if (mLegPosition == 0 && usesDynamicBlur()) {
				mListView.setOnScrollListener(this);
			}
			else {
				mListView.setOnScrollListener(null);
				mListener.onBlur(1.0f);
			}
		}

		displayHeaderLeg();
	}

	//////////////////////////////////////////////////////////////////////////
	// Dataset observer

	private DataSetObserver mDataSetObserver = new DataSetObserver() {
		@Override
		public void onChanged() {
			displayNumFlights();
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// FlightListFragmentListener

	public interface FlightListFragmentListener {
		public void onFlightLegClick(FlightTrip trip, FlightLeg leg, int legPosition);

		public void onDeselectFlightLeg();

		public void onBlur(float alpha);
	}

	//////////////////////////////////////////////////////////////////////////
	// SectionFlightLegListener

	@Override
	public void onDeselect() {
		mListener.onDeselectFlightLeg();
	}

	//////////////////////////////////////////////////////////////////////////
	// OnScrollListener

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if (view.getChildCount() > 0) {
			if (firstVisibleItem == 0) {
				View header = view.getChildAt(0);
				mListener.onBlur((float) -header.getTop() / (float) header.getHeight());
			}
			else {
				mListener.onBlur(1.0f);
			}
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// Do nothing
	}
}
