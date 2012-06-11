package com.expedia.bookings.fragment;

import java.util.Calendar;
import java.util.TimeZone;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.utils.Ui;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.data.Waypoint;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TripFragment extends Fragment {
	private static final String ARG_POSITION = "ARG_POSITION";
	private static final String ARG_LEG_POS = "ARG_LEG_POS";

	/**
	 * Used to describe the position of the Waypoint within the leg.
	 */
	private enum HeaderPosition {
		FIRST, INTERMEDIATE, LAST
	}

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

		//Add all of the content
		for (int i = 0; i < leg.getSegmentCount(); i++) {
			Flight seg = leg.getSegment(i);

			if (i == 0) {
				headerView = buildSegmentHeader(inflater, seg, tripContainer, HeaderPosition.FIRST);
			}
			else {
				//This changes layover times from the arrival or departure time to a range of time
				updateSegmentHeaderTime(headerView, seg);
			}

			ViewGroup timeLineContainer = (ViewGroup) headerView.findViewById(R.id.flight_details_segment_info_ll);
			View segmentView = buildSegmentView(inflater, seg, timeLineContainer);

			timeLineContainer.addView(segmentView);
			tripContainer.addView(headerView);

			if (i == leg.getSegmentCount() - 1) {
				//last one ...
				tripContainer.addView(buildSegmentHeader(inflater, seg, tripContainer, HeaderPosition.LAST));
			}
			else {
				headerView = buildSegmentHeader(inflater, seg, tripContainer, HeaderPosition.INTERMEDIATE);
			}
		}

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

		Ui.setText(wpView, R.id.flight_details_duration_tv,
				getFlightDuration(orig.getMostRelevantDateTime(), dest.getMostRelevantDateTime()));

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
	 * Creates and returns one of the waypoint headers with the neighboring Circle (and a layout place to add details)
	 * @param inflater
	 * @param segment
	 * @param tripContainer
	 * @param pos
	 * @return
	 */
	private View buildSegmentHeader(LayoutInflater inflater, Flight segment, ViewGroup tripContainer, HeaderPosition pos) {
		View segHead = inflater.inflate(R.layout.snippet_flight_detail_segment, tripContainer, false);

		Waypoint wp;
		if (pos == HeaderPosition.LAST || pos == HeaderPosition.INTERMEDIATE) {
			wp = segment.mDestination;
		}
		else {
			wp = segment.mOrigin;
		}

		Calendar cal = wp.getMostRelevantDateTime();

		boolean twentyFourHourClock = DateFormat.is24HourFormat(this.getActivity());
		String time = genBaseTime(cal, twentyFourHourClock).toString();
		time += getAmPm(cal, twentyFourHourClock).toString().toLowerCase();

		if (pos == HeaderPosition.INTERMEDIATE) {
			//We change the orientation of the linear layout so we can have a time range, followed by the name of the airport on another line.
			LinearLayout textLayout = ((LinearLayout) Ui.findView(segHead, R.id.flight_details_waypoint_text_ll));
			textLayout.setOrientation(LinearLayout.VERTICAL);
			textLayout.forceLayout();
		}

		Ui.setText(segHead, R.id.flight_details_departure_time_tv, time);
		Ui.setText(segHead, R.id.flight_details_departure_airport_tv, String.format(
				this.getString(R.string.airport_name_and_code_TEMPLATE), wp.getAirport().mName, wp.mAirportCode));

		if (pos == HeaderPosition.FIRST)
			segHead.findViewById(R.id.flight_details_header_line_up).setVisibility(View.INVISIBLE);

		if (pos == HeaderPosition.LAST)
			segHead.findViewById(R.id.flight_details_header_line_down).setVisibility(View.INVISIBLE);

		return segHead;
	}

	/**
	 * This method updates the header from a single time (e.g. 10:24pm) to a time range
	 * (e.g. 10:24pm - 11:52pm) this is used to append the second time to the layout for 
	 * layovers
	 * @param segHead
	 * @param segment
	 * @return
	 */
	private View updateSegmentHeaderTime(View segHead, Flight segment) {
		Waypoint wp = segment.mOrigin;

		Calendar cal = wp.getMostRelevantDateTime();
		boolean twentyFourHourClock = DateFormat.is24HourFormat(this.getActivity());
		String time = genBaseTime(cal, twentyFourHourClock).toString();
		time += getAmPm(cal, twentyFourHourClock).toString().toLowerCase();

		TextView landTime = (TextView) Ui.findView(segHead, R.id.flight_details_departure_time_tv);
		time = String.format(getString(R.string.layover_time_range_TEMPLATE), landTime.getText(), time);
		Ui.setText(segHead, R.id.flight_details_departure_time_tv, time);

		return segHead;
	}

	//////////////////////////////////////////////////////////////////////////
	// Time helpers

	private CharSequence genBaseTime(Calendar cal, boolean twentyFourHour) {
		CharSequence retVal = "";

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

	private CharSequence getFlightDuration(Calendar orig, Calendar dest) {
		//TODO:Move to someplace static...
		int minutes = 1000 * 60;
		int hours = minutes * 60;

		String retStr = "";

		long dif = dest.getTimeInMillis() - orig.getTimeInMillis();
		if (dif < 0)
			dif = orig.getTimeInMillis() - dest.getTimeInMillis();

		retStr += String.format("%dh%dm", (int) Math.floor(dif / hours), (int) Math.round((dif % hours) / minutes));
		return retStr;
	}

}
