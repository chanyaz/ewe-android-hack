package com.expedia.bookings.activity;

import java.text.DateFormat;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightSegment;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.utils.Ui;

public class FlightDetailsActivity extends FragmentActivity {

	public static final String EXTRA_STARTING_POSITION = "EXTRA_STARTING_POSITION";
	public static final String EXTRA_IS_INBOUND = "EXTRA_IS_INBOUND";

	private FlightAdapter mAdapter;

	private ViewPager mPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_flight_details);

		mPager = Ui.findView(this, R.id.pager);
		mAdapter = new FlightAdapter(getSupportFragmentManager(), getIntent().getBooleanExtra(EXTRA_IS_INBOUND, true));
		mPager.setAdapter(mAdapter);

		if (savedInstanceState == null) {
			mPager.setCurrentItem(getIntent().getIntExtra(EXTRA_STARTING_POSITION, 0));
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// View Pager adapter

	public static class FlightAdapter extends FragmentStatePagerAdapter {

		private boolean mIsInbound;

		public FlightAdapter(FragmentManager fm, boolean isInbound) {
			super(fm);

			mIsInbound = isInbound;
		}

		@Override
		public Fragment getItem(int position) {
			return TripFragment.newInstance(position, mIsInbound);
		}

		@Override
		public int getCount() {
			return Db.getFlightSearchResponse().getTripCount();
		}
	}

	public static class TripFragment extends Fragment {

		private static final String ARG_POSITION = "ARG_POSITION";
		private static final String ARG_IS_INBOUND = "ARG_IS_INBOUND";

		public static TripFragment newInstance(int position, boolean isInbound) {
			TripFragment fragment = new TripFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_POSITION, position);
			args.putBoolean(ARG_IS_INBOUND, isInbound);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.fragment_trip, container, false);

			Bundle args = getArguments();

			FlightTrip trip = Db.getFlightSearchResponse().getTrip(args.getInt(ARG_POSITION));
			FlightLeg leg = (args.getBoolean(ARG_IS_INBOUND)) ? trip.getInboundLeg() : trip.getOutboundLeg();

			// Construct the trip.  This is a bit complex.
			ViewGroup tripContainer = Ui.findView(view, R.id.trip_container);

			int segmentCount = leg.getSegmentCount();
			for (int segmentIndex = 0; segmentIndex < segmentCount; segmentIndex++) {
				FlightSegment segment = leg.getSegment(segmentIndex);

				if (segmentIndex == 0) {
					// Add the initial departure waypoint
					addWaypoint(inflater, tripContainer, segment, true);
					addDivider(inflater, tripContainer);
				}
				else {
					// Add a layover waypoint
					addLayover(inflater, tripContainer, leg.getSegment(segmentIndex - 1), segment);
					addDivider(inflater, tripContainer);
				}

				// Add departing flight details
				addFlightMovement(inflater, tripContainer, segment, true);
				addDivider(inflater, tripContainer);

				// Add the flight info row
				addFlightInfo(inflater, tripContainer, segment);
				addDivider(inflater, tripContainer);

				// Add arrival flight details
				addFlightMovement(inflater, tripContainer, segment, false);
				addDivider(inflater, tripContainer);

				if (segmentIndex == segmentCount - 1) {
					// Add the final destination waypoint
					addWaypoint(inflater, tripContainer, segment, false);
				}
			}

			return view;
		}

		private void addDivider(LayoutInflater inflater, ViewGroup tripContainer) {
			View view = inflater.inflate(R.layout.snippet_flight_detail_divider, tripContainer, false);
			tripContainer.addView(view);
		}

		private void addWaypoint(LayoutInflater inflater, ViewGroup tripContainer, FlightSegment flightSegment,
				boolean isDeparture) {
			// TODO: Fill in with actual data
			View view = inflater.inflate(R.layout.snippet_waypoint, tripContainer, false);

			if (isDeparture) {
				Ui.setText(view, R.id.location_text_view, "Some City");
				Ui.setText(view, R.id.airport_name_text_view, "Some Airport Name");
			}
			else {
				Ui.setText(view, R.id.location_text_view, "Some City");
				Ui.setText(view, R.id.airport_name_text_view, "Some Airport Name");
			}

			tripContainer.addView(view);
		}

		private void addLayover(LayoutInflater inflater, ViewGroup tripContainer, FlightSegment lastFlightSegment,
				FlightSegment nextSegment) {
			// TODO: calculate actuallayover
			View view = inflater.inflate(R.layout.snippet_layover, tripContainer, false);
			Ui.setText(view, R.id.layover_text_view, "3h 33m Layover");
			Ui.setText(view, R.id.airport_name_text_view, nextSegment.getDepartureAirportCode());
			tripContainer.addView(view);
		}

		private void addFlightMovement(LayoutInflater inflater, ViewGroup tripContainer, FlightSegment flightSegment,
				boolean isDeparture) {
			View view = inflater.inflate(R.layout.snippet_flight_movement, tripContainer, false);

			DateFormat df = android.text.format.DateFormat.getTimeFormat(getActivity());

			if (isDeparture) {
				Ui.setText(view, R.id.time_text_view, df.format(flightSegment.getDepartureTime().toMillis(false)));
				Ui.setText(view, R.id.airport_code_text_view, flightSegment.getDepartureAirportCode());
			}
			else {
				Ui.setText(view, R.id.time_text_view, df.format(flightSegment.getArrivalTime().toMillis(false)));
				Ui.setText(view, R.id.airport_code_text_view, flightSegment.getArrivalAirportCode());
			}

			tripContainer.addView(view);
		}

		private void addFlightInfo(LayoutInflater inflater, ViewGroup tripContainer, FlightSegment flightSegment) {
			// TODO: Calculate/display more correct data!
			View view = inflater.inflate(R.layout.snippet_flight_info, tripContainer, false);
			Ui.setText(view, R.id.duration_text_view, "1h 33m");
			Ui.setText(view, R.id.flight_number_text_view,
					flightSegment.getAirlineCode() + " " + flightSegment.getFlightNumber());
			tripContainer.addView(view);
		}
	}
}
