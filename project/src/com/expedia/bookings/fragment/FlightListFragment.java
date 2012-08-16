package com.expedia.bookings.fragment;

import android.app.Activity;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.FlightTripOverviewActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightSearch;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.FlightTripLeg;
import com.expedia.bookings.section.SectionFlightLeg;
import com.expedia.bookings.section.SectionFlightLeg.SectionFlightLegListener;
import com.expedia.bookings.widget.FlightAdapter;
import com.mobiata.android.util.Ui;
import com.mobiata.flightlib.data.Airport;
import com.mobiata.flightlib.data.sources.FlightStatsDbUtils;

// IMPLEMENTATION NOTE: This implementation heavily leans towards the user only picking
// two legs of a flight (outbound and inbound).  If you want to adapt it for 3+ legs, you
// will need to rewrite a good portion of it.
public class FlightListFragment extends ListFragment implements SectionFlightLegListener {

	public static final String TAG = FlightListFragment.class.getName();

	private static final String INSTANCE_LEG_POSITION = "INSTANCE_LEG_POSITION";

	private FlightAdapter mAdapter;

	private FlightListFragmentListener mListener;

	private ImageView mBackgroundView;
	private TextView mNumFlightsTextView;
	private SectionFlightLeg mSectionFlightLeg;

	private Drawable mHeaderDrawable;

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

		// DELETE EVENTUALLY: For now, just set the header to always be SF
		setHeaderDrawable(getResources().getDrawable(R.drawable.san_francisco));
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

		mBackgroundView = Ui.findView(v, R.id.background_view);

		// Configure the header
		ListView lv = Ui.findView(v, android.R.id.list);
		lv.setDividerHeight(0);
		ViewGroup header = (ViewGroup) inflater.inflate(R.layout.snippet_flight_header, lv, false);
		mNumFlightsTextView = Ui.findView(header, R.id.num_flights_text_view);
		mSectionFlightLeg = Ui.findView(header, R.id.flight_leg);
		mSectionFlightLeg.setListener(this);
		lv.addHeaderView(header);
		lv.setHeaderDividersEnabled(false);

		displayBackground();
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

		// Set the leg as selected
		FlightTrip trip = mAdapter.getItem(position - numHeaderViews);
		FlightLeg leg = trip.getLeg(mLegPosition);
		FlightSearch flightSearch = Db.getFlightSearch();
		flightSearch.setSelectedLeg(mLegPosition, new FlightTripLeg(trip, leg));

		// If we need to select another leg, continue; otherwise go to next page
		if (flightSearch.getSelectedFlightTrip() == null) {
			mLegPosition++;

			displayHeaderLeg();

			onLegPositionChanged();
		}
		else {
			Intent intent = new Intent(getActivity(), FlightTripOverviewActivity.class);
			intent.putExtra(FlightTripOverviewActivity.EXTRA_TRIP_KEY, trip.getProductKey());
			startActivity(intent);
		}
	}

	/**
	 * We want to be able to handle back presses
	 * @return true if back press was consumed, false otherwise
	 */
	public boolean onBackPressed() {
		if (mLegPosition > 0) {
			deselectOutboundLeg();
			return true;
		}

		return false;
	}

	//////////////////////////////////////////////////////////////////////////
	// Header control

	public void setHeaderDrawable(Drawable drawable) {
		mHeaderDrawable = drawable;
		displayBackground();
	}

	private void displayBackground() {
		if (mBackgroundView != null) {
			mBackgroundView.setImageDrawable(mHeaderDrawable);
		}
	}

	private void displayHeaderLeg() {
		if (mSectionFlightLeg != null) {
			if (mLegPosition == 0) {
				mSectionFlightLeg.setVisibility(View.GONE);
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

	public void deselectOutboundLeg() {
		Db.getFlightSearch().setSelectedLeg(mLegPosition, null);
		Db.getFlightSearch().clearQuery(mLegPosition); // #443: Clear cached query
		mLegPosition--;

		onLegPositionChanged();

		displayHeaderLeg();
	}

	public void onLegPositionChanged() {
		mAdapter.setLegPosition(mLegPosition);

		mAdapter.setFlightTripQuery(Db.getFlightSearch().queryTrips(mLegPosition));

		mListener.onSelectionChanged(mLegPosition);

		// Scroll to top after reloading list with new results
		if (getView() != null) {
			getListView().setSelection(0);
		}
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
		public void onSelectionChanged(int newLegPosition);
	}

	//////////////////////////////////////////////////////////////////////////
	// SectionFlightLegListener

	@Override
	public void onDeselect() {
		deselectOutboundLeg();
	}
}
