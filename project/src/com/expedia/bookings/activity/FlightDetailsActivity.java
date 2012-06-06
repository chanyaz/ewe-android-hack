package com.expedia.bookings.activity;

import java.sql.Time;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.utils.Ui;
import com.mobiata.flightlib.data.Airport;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.data.FlightNote;
import com.mobiata.flightlib.data.Waypoint;
import com.mobiata.flightlib.utils.DateTimeUtils;
import com.mobiata.flightlib.utils.FormatUtils;

public class FlightDetailsActivity extends FragmentActivity {

	public static final String EXTRA_STARTING_POSITION = "EXTRA_STARTING_POSITION";
	public static final String EXTRA_LEG_POSITION = "EXTRA_LEG_POSITION";

	private FlightAdapter mAdapter;

	private ViewPager mPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_flight_details);

		mPager = Ui.findView(this, R.id.pager);
		mAdapter = new FlightAdapter(getSupportFragmentManager(), getIntent().getBooleanExtra(EXTRA_LEG_POSITION, true));
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
			return Db.getFlightSearch().getSearchResponse().getTripCount();
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

			FlightTrip trip = Db.getFlightSearch().getSearchResponse().getTrip(args.getInt(ARG_POSITION));
			FlightLeg leg = trip.getLeg(args.getInt(EXTRA_LEG_POSITION, 0));

			// Construct the trip.  This is a bit complex.
			ViewGroup tripContainer = Ui.findView(view, R.id.trip_container);

			
			for(int i = 0; i < leg.getSegmentCount(); i++){
				Flight seg = leg.getSegment(i);
				
				View segmentView = buildSegmentView(inflater, seg,tripContainer);
				
				tripContainer.addView(segmentView);
			}
			
			//int segmentCount = leg.getSegmentCount();
			
//			for (int segmentIndex = 0; segmentIndex < segmentCount; segmentIndex++) {
//				Flight segment = leg.getSegment(segmentIndex);
//
//				View wayPoint;
//				if (segmentIndex == 0) {
//					// If this is the last waypoint then we don't need to add any info...
//					wayPoint = buildWaypoint(inflater,  segment, tripContainer, SegmentPosition.DEPARTURE);	
//				}else if (segmentIndex < segmentCount - 1){
//					//We will add a waypoint and a bunch of info for the segment
//					wayPoint = buildWaypoint(inflater,  segment, tripContainer, SegmentPosition.INTERMEDIATE);
//				}else{
//					wayPoint = buildWaypoint(inflater,  segment, tripContainer, SegmentPosition.ARRIVAL);
//				}
//				
//				tripContainer.addView(wayPoint);
//			}

			return view;
		}
		
		private View buildSegmentView(LayoutInflater inflater, Flight segment, ViewGroup tripContainer){
			View wpView = inflater.inflate(R.layout.snippet_flight_detail_segment_info, tripContainer, false);

			Waypoint orig = segment.mOrigin;
			Waypoint dest = segment.mDestination;
			
			String origCity = orig.getAirport().mCity;
			String destCity = dest.getAirport().mCity;
			String cityToCity = origCity + " to " + destCity;
			
			
 			String origTz = orig.getAirport().mTimeZone.getDisplayName();
 			String destTz = orig.getAirport().mTimeZone.getDisplayName();
			
			String carrier = "" + segment.getOperatingFlightCode().getAirline().mAirlineName;
			String flightNumber = "" + segment.getOperatingFlightCode().mNumber;
//			Time takeOffTime = new Time(segment.mLastNotifiedTakeoffTime);
//			String depTime = "" + takeOffTime.getHours() + ":" + takeOffTime.getMinutes();
		
			
			boolean twentyFourHourClock = false;//TODO:get from someplace
			
			
			Ui.setText(wpView, R.id.flight_details_dep_arr_tv, cityToCity);
			Ui.setText(wpView, R.id.flight_details_carrier_tv, carrier);
			Ui.setText(wpView, R.id.flight_details_flight_number_tv, flightNumber);
			Ui.setText(wpView, R.id.flight_details_departure_time_tv, genBaseTime(orig.getMostRelevantDateTime(),twentyFourHourClock));
			Ui.setText(wpView, R.id.flight_details_departure_tz_tv, orig.getAirport().mTimeZone.getDisplayName(false,TimeZone.SHORT));
			Ui.setText(wpView, R.id.flight_details_departure_ampm_tv, getAmPm(orig.getMostRelevantDateTime(),twentyFourHourClock));
			Ui.setText(wpView, R.id.flight_details_arrival_time_tv, genBaseTime(dest.getMostRelevantDateTime(),twentyFourHourClock));
			Ui.setText(wpView, R.id.flight_details_arrival_tz_tv, dest.getAirport().mTimeZone.getDisplayName(false,TimeZone.SHORT));
			Ui.setText(wpView, R.id.flight_details_arrival_ampm_tv, getAmPm(dest.getMostRelevantDateTime(),twentyFourHourClock));
			Ui.setText(wpView, R.id.flight_details_duration_tv, getFlightDuration(orig.getMostRelevantDateTime(),dest.getMostRelevantDateTime()));
			
			ViewGroup amenities = (ViewGroup)wpView.findViewById(R.id.flight_details_data_amenities_ll);
			for(int i = 0; i < 4; i++){
				View amen = inflater.inflate(R.layout.snippet_flight_detail_amenity, amenities,false);
				Ui.setText(amen, R.id.flight_amenity_label_tv,"Meal");
				amenities.addView(amen);
			}
			
			return wpView;
		}
		
		private String genBaseTime(Calendar cal, boolean twentyFourHour){
			String retVal = "";
			
			if(!twentyFourHour){
				retVal = String.format("%d:%02d",cal.get(Calendar.HOUR),cal.get(Calendar.MINUTE));
			}else{
				retVal = String.format("%d:%02d",cal.get(Calendar.HOUR_OF_DAY),cal.get(Calendar.MINUTE));
			}
			
			return retVal;
		}
		
		//TODO:We need to use locals...
		private String getAmPm(Calendar cal, boolean twentyFourHour){
			if(twentyFourHour){
				return "";
			}else if(cal.get(Calendar.HOUR_OF_DAY)%12 > 0){
				return "PM";
			}else{
				return "AM";
			}
		}
		
		
		//TODO:This whole thing needs to be rewritten inorder to use locals etc..
		private String getFlightDuration(Calendar orig, Calendar dest ){
			String retStr = "";
			int minutes = 1000 * 60;
			int hours = minutes * 60;
			int days = hours * 24;
			
			//TODO:Don't assume orig before dest
			long dif = dest.getTimeInMillis() - orig.getTimeInMillis();
			if(dif/days > 1){
				retStr += "" + (int)Math.floor(dif/days) + "d";
			}
			
			
			
			retStr += String.format("%dh%dm", (int)Math.floor(dif/hours),(int)Math.round((dif%hours)/minutes));
			return retStr;
		}
		

//		@Override
//		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//			View view = inflater.inflate(R.layout.fragment_trip, container, false);
//
//			Bundle args = getArguments();
//
//			FlightTrip trip = Db.getFlightSearchResponse().getTrip(args.getInt(ARG_POSITION));
//			FlightLeg leg = trip.getLeg(args.getInt(EXTRA_LEG_POSITION, 0));
//
//			// Construct the trip.  This is a bit complex.
//			ViewGroup tripContainer = Ui.findView(view, R.id.trip_container);
//
//			int segmentCount = leg.getSegmentCount();
//			for (int segmentIndex = 0; segmentIndex < segmentCount; segmentIndex++) {
//				Flight segment = leg.getSegment(segmentIndex);
//
//				if (segmentIndex == 0) {
//					// Add the initial departure waypoint
//					addWaypoint(inflater, tripContainer, segment, true);
//					addDivider(inflater, tripContainer);
//				}
//				else {
//					// Add a layover waypoint
//					addLayover(inflater, tripContainer, leg.getSegment(segmentIndex - 1), segment);
//					addDivider(inflater, tripContainer);
//				}
//
//				// Add departing flight details
//				addFlightMovement(inflater, tripContainer, segment, true);
//				addDivider(inflater, tripContainer);
//
//				// Add the flight info row
//				addFlightInfo(inflater, tripContainer, segment);
//				addDivider(inflater, tripContainer);
//
//				// Add arrival flight details
//				addFlightMovement(inflater, tripContainer, segment, false);
//				addDivider(inflater, tripContainer);
//
//				if (segmentIndex == segmentCount - 1) {
//					// Add the final destination waypoint
//					addWaypoint(inflater, tripContainer, segment, false);
//				}
//			}
//
//			return view;
//		}

//		private void addDivider(LayoutInflater inflater, ViewGroup tripContainer) {
//			View view = inflater.inflate(R.layout.snippet_flight_detail_divider, tripContainer, false);
//			tripContainer.addView(view);
//		}
//
//		private void addWaypoint(LayoutInflater inflater, ViewGroup tripContainer, Flight segment,
//				boolean isDeparture) {
//			View view = inflater.inflate(R.layout.snippet_waypoint, tripContainer, false);
//
//			Waypoint waypoint = (isDeparture) ? segment.mOrigin : segment.mDestination;
//			Airport airport = waypoint.getAirport();
//
//			String location = airport.mCity;
//			if (!TextUtils.isEmpty(airport.mStateCode)) {
//				location += ", " + airport.mStateCode;
//			}
//
//			Ui.setText(view, R.id.location_text_view, location);
//			Ui.setText(view, R.id.airport_name_text_view, airport.mName);
//
//			tripContainer.addView(view);
//		}
//
//		private void addLayover(LayoutInflater inflater, ViewGroup tripContainer, Flight lastSegment,
//				Flight nextSegment) {
//			int duration = DateTimeUtils.compareDateTimes(lastSegment.mDestination.getMostRelevantDateTime(),
//					nextSegment.mOrigin.getMostRelevantDateTime());
//
//			String durationStr = DateTimeUtils.formatDuration(getResources(), duration);
//
//			View view = inflater.inflate(R.layout.snippet_layover, tripContainer, false);
//			Ui.setText(view, R.id.layover_text_view, Html.fromHtml(getString(R.string.layover_template, durationStr)));
//			Ui.setText(view, R.id.airport_name_text_view, nextSegment.mOrigin.mAirportCode);
//			tripContainer.addView(view);
//		}
//
//		private void addFlightMovement(LayoutInflater inflater, ViewGroup tripContainer, Flight segment,
//				boolean isDeparture) {
//			View view = inflater.inflate(R.layout.snippet_flight_movement, tripContainer, false);
//
//			DateFormat df = android.text.format.DateFormat.getTimeFormat(getActivity());
//
//			Waypoint waypoint = (isDeparture) ? segment.mOrigin : segment.mDestination;
//			Ui.setText(view, R.id.time_text_view, df.format(waypoint.getMostRelevantDateTime().getTime()));
//			Ui.setText(view, R.id.airport_code_text_view, waypoint.mAirportCode);
//
//			tripContainer.addView(view);
//		}
//
//		private void addFlightInfo(LayoutInflater inflater, ViewGroup tripContainer, Flight segment) {
//			int duration = DateTimeUtils.compareDateTimes(segment.mOrigin.getMostRelevantDateTime(),
//					segment.mDestination.getMostRelevantDateTime());
//			String durationStr = DateTimeUtils.formatDuration(getResources(), duration);
//
//			View view = inflater.inflate(R.layout.snippet_flight_info, tripContainer, false);
//			Ui.setText(view, R.id.duration_text_view, durationStr);
//			Ui.setText(view, R.id.flight_number_text_view, FormatUtils.formatFlightNumberShort(segment, getActivity()));
//			tripContainer.addView(view);
//		}
	}
}
