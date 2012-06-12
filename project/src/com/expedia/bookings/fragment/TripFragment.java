package com.expedia.bookings.fragment;

import java.util.Calendar;
import java.util.TimeZone;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.data.Waypoint;
import com.mobiata.flightlib.utils.DateTimeUtils;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class TripFragment extends Fragment {
	private static final String ARG_POSITION = "ARG_POSITION";
	private static final String ARG_LEG_POS = "ARG_LEG_POS";

	public static TripFragment newInstance(int position, int legPosition) {
		TripFragment fragment = new TripFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_POSITION, position);
		args.putInt(ARG_LEG_POS, legPosition);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Bundle args = getArguments();

		FlightTrip trip = Db.getFlightSearch().getTrips(args.getInt(ARG_LEG_POS, 0)).get(args.getInt(ARG_POSITION));
		FlightLeg leg = trip.getLeg(args.getInt(ARG_LEG_POS, 0));

		View view = inflater.inflate(R.layout.fragment_trip, container, false);
		ViewGroup tripContainer = Ui.findView(view, R.id.trip_container);
		View headerView = null;
		Flight lastSeg = null;
		Flight curSeg = null;
		for (int i = 0; i < leg.getSegmentCount(); i++) {
			curSeg = leg.getSegment(i);

			headerView = buildSegmentHeader(inflater, lastSeg, curSeg, tripContainer);

			ViewGroup timeLineContainer = (ViewGroup) headerView.findViewById(R.id.flight_details_segment_info_ll);
			View segmentView = buildSegmentView(inflater, curSeg, timeLineContainer);

			timeLineContainer.addView(segmentView);
			tripContainer.addView(headerView);

			lastSeg = curSeg;
		}
		//Add the final destination
		tripContainer.addView(buildSegmentHeader(inflater, lastSeg, null, tripContainer));

		return view;
	}

	/**
	 * Creates an returns one of the detail cards
	 * @param inflater
	 * @param segment
	 * @param tripContainer
	 * @return
	 */
	private View buildSegmentView(LayoutInflater inflater, Flight segment, ViewGroup tripContainer) {
		View wpView = inflater.inflate(R.layout.snippet_flight_detail_segment_info, tripContainer, false);

		Waypoint orig = segment.mOrigin;
		Waypoint dest = segment.mDestination;

		String origCity = orig.getAirport().mCity;
		String destCity = dest.getAirport().mCity;
		String cityToCity = String.format(this.getString(R.string.city_to_city_TEMPLATE), origCity, destCity);
		String carrier = "" + segment.getOperatingFlightCode().getAirline().mAirlineName;
		String flightNumber = "" + segment.getOperatingFlightCode().mNumber;

		boolean twentyFourHourClock = DateFormat.is24HourFormat(this.getActivity());

		Ui.setText(wpView, R.id.flight_details_dep_arr_tv, cityToCity);
		Ui.setText(wpView, R.id.flight_details_carrier_tv, carrier);
		Ui.setText(wpView, R.id.flight_details_flight_number_tv, flightNumber);
		Ui.setText(wpView, R.id.flight_details_departure_time_tv,
				genBaseTime(orig.getMostRelevantDateTime(), twentyFourHourClock));
		Ui.setText(wpView, R.id.flight_details_departure_tz_tv,
				orig.getAirport().mTimeZone.getDisplayName(false, TimeZone.SHORT));
		Ui.setText(wpView, R.id.flight_details_departure_ampm_tv,
				getAmPm(orig.getMostRelevantDateTime(), twentyFourHourClock));
		Ui.setText(wpView, R.id.flight_details_arrival_time_tv,
				genBaseTime(dest.getMostRelevantDateTime(), twentyFourHourClock));
		Ui.setText(wpView, R.id.flight_details_arrival_tz_tv,
				dest.getAirport().mTimeZone.getDisplayName(false, TimeZone.SHORT));
		Ui.setText(wpView, R.id.flight_details_arrival_ampm_tv,
				getAmPm(dest.getMostRelevantDateTime(), twentyFourHourClock));

		//TODO:This needs to be uncommented after pulling
		//Ui.setText(wpView, R.id.flight_details_duration_tv, DateTimeUtils.formatDuration(getResources(), segment.getTripTime()));
		
		ViewGroup amenities = (ViewGroup) wpView.findViewById(R.id.flight_details_data_amenities_ll);

		//Add placeholders
		//TODO:Get real amenities...
		for (int i = 0; i < 4; i++) {
			View amen = inflater.inflate(R.layout.snippet_flight_detail_amenity, amenities, false);
			if (i == 0)
				Ui.setText(amen, R.id.flight_amenity_label_tv, "Need");
			if (i == 1)
				Ui.setText(amen, R.id.flight_amenity_label_tv, "Real");
			if (i == 2)
				Ui.setText(amen, R.id.flight_amenity_label_tv, "Amenities");
			if (i == 3)
				Ui.setText(amen, R.id.flight_amenity_label_tv, "Data");

			amenities.addView(amen);
		}

		return wpView;
	}

	/**
	 * Builds the sement header and returns it.
	 * 
	 * @param inflater
	 * @param prevSeg the previous segment ( or null if this is the first segment )
	 * @param curSeg the current segment ( or null if this is the final segment ) 
	 * @param tripContainer the viewgroup this stuff is going to be added to
	 * @return
	 */
	private View buildSegmentHeader(LayoutInflater inflater, Flight prevSeg, Flight curSeg,
			ViewGroup tripContainer) {
		View segHead = inflater.inflate(R.layout.snippet_flight_detail_segment, tripContainer, false);

		String time = "";
		String airport = "";
		boolean twentyFourHourClock = DateFormat.is24HourFormat(this.getActivity());

		//Set up connecting lines
		if (prevSeg == null) {
			segHead.findViewById(R.id.flight_details_header_line_up).setVisibility(View.GONE);
		}
		if (curSeg == null) {
			segHead.findViewById(R.id.flight_details_header_line_down).setVisibility(View.GONE);
		}

		if (prevSeg != null && curSeg != null) {
			//This case is a layover
			LinearLayout textLayout = ((LinearLayout) Ui.findView(segHead, R.id.flight_details_waypoint_text_ll));
			textLayout.setOrientation(LinearLayout.VERTICAL);

			time = String.format(
					getString(R.string.layover_time_range_and_duration_TEMPLATE),
					genBaseTime(prevSeg.mDestination.getMostRelevantDateTime(), twentyFourHourClock).toString()
							+ getAmPm(prevSeg.mDestination.getMostRelevantDateTime(), twentyFourHourClock).toString(),
					genBaseTime(curSeg.mOrigin.getMostRelevantDateTime(), twentyFourHourClock).toString()
							+ getAmPm(curSeg.mOrigin.getMostRelevantDateTime(), twentyFourHourClock).toString(),
					getDuration(curSeg.mOrigin.getMostRelevantDateTime(),
							curSeg.mOrigin.getMostRelevantDateTime()));

			airport = String.format(getString(R.string.airport_name_and_code_TEMPLATE),
					curSeg.mOrigin.getAirport().mName, curSeg.mOrigin.mAirportCode);

		}
		else {
			//First or last flight segment
			Waypoint wp;
			if (prevSeg == null) {
				//First segment
				wp = curSeg.mOrigin;
			}
			else {
				//Last segment
				wp = prevSeg.mDestination;
			}

			time = genBaseTime(wp.getMostRelevantDateTime(), twentyFourHourClock).toString()
					+ getAmPm(wp.getMostRelevantDateTime(), twentyFourHourClock).toString();

			airport = String.format(
					this.getString(R.string.airport_name_and_code_TEMPLATE), wp.getAirport().mName, wp.mAirportCode);
		}

		Ui.setText(segHead, R.id.flight_details_departure_time_tv, time);
		Ui.setText(segHead, R.id.flight_details_departure_airport_tv, airport);

		return segHead;
	}

	//////////////////////////////////////////////////////////////////////////
	// Time helpers

	private CharSequence genBaseTime(Calendar cal, boolean twentyFourHour) {
		CharSequence retVal = "";

		// TODO: System time

		if (!twentyFourHour) {
			//noon/midnight == 0 in calendar.Hour time
			retVal = String.format("%d:%02d", (cal.get(Calendar.HOUR) == 0) ? 12 : cal.get(Calendar.HOUR),
					cal.get(Calendar.MINUTE));
		}
		else {
			retVal = String.format("%d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
		}

		return retVal;
	}

	private CharSequence getAmPm(Calendar cal, boolean twentyFourHour) {
		if (twentyFourHour) {
			return "";
		}
		else {
			return DateFormat.format("aa", cal.getTimeInMillis()).toString().toUpperCase();
		}
	}

	private CharSequence getDuration(Calendar orig, Calendar dest) {
		int dif = (int) (Math.abs(dest.getTimeInMillis() - orig.getTimeInMillis()) / 1000);
		Log.i("DIF:" + dif);
		return DateTimeUtils.formatDuration(getResources(), dif);
	}

}
