package com.expedia.bookings.widget;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.WebViewActivity;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.trips.Insurance;
import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.data.trips.ItinCardDataFlight;
import com.expedia.bookings.data.trips.TripComponent;
import com.expedia.bookings.data.trips.Insurance.InsuranceLineOfBusiness;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.data.trips.TripFlight;
import com.expedia.bookings.section.FlightLegSummarySection;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.util.ViewUtils;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.data.Waypoint;
import com.mobiata.flightlib.utils.DateTimeUtils;

public class FlightItinCard extends ItinCard {
	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	private ItinCardDataFlight mData;
	private TripFlight mTripFlight;

	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	//////////////////////////////////////////////////////////////////////////////////////

	public FlightItinCard(Context context) {
		this(context, null);
	}

	public FlightItinCard(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// OVERRIDES
	//////////////////////////////////////////////////////////////////////////////////////

	@Override
	public int getTypeIconResId() {
		return R.drawable.ic_type_circle_flight;
	}

	@Override
	public Type getType() {
		return Type.FLIGHT;
	}

	@Override
	public void bind(ItinCardData itinCardData) {
		mData = (ItinCardDataFlight) itinCardData;
		mTripFlight = (TripFlight) mData.getTripComponent();

		super.bind(itinCardData);
	}

	@Override
	protected String getHeaderImageUrl(TripComponent tripComponent) {
		TripFlight tripFlight = (TripFlight) tripComponent;
		if (tripFlight != null && mData != null && tripFlight.getLegDestinationImageUrl(mData.getLegNumber()) != null) {
			return tripFlight.getLegDestinationImageUrl(mData.getLegNumber());
		}
		else {
			return "";
		}
	}

	@Override
	protected String getHeaderText(TripComponent tripComponent) {
		if (mData != null) {
			return mData.getFlightLeg().getFirstWaypoint().getAirport().mCity + " -> "
					+ mData.getFlightLeg().getLastWaypoint().getAirport().mCity;
		}

		return "Flight Card";
	}

	@Override
	protected View getDetailsView(LayoutInflater inflater, ViewGroup container, TripComponent tripComponent) {
		View view = inflater.inflate(R.layout.include_itin_card_flight, container, false);

		if (mTripFlight != null && mTripFlight.getFlightTrip() != null && mTripFlight.getFlightTrip().getLegCount() > 0) {
			Resources res = getResources();
			FlightLeg leg = mData.getFlightLeg();

			TextView confirmationCodeLabel = Ui.findView(view, R.id.confirmation_code_label);
			TextView passengersLabel = Ui.findView(view, R.id.passengers_label);
			TextView bookingInfoLabel = Ui.findView(view, R.id.booking_info_label);
			TextView insuranceLabel = Ui.findView(view, R.id.insurance_label);

			ViewUtils.setAllCaps(confirmationCodeLabel);
			ViewUtils.setAllCaps(passengersLabel);
			ViewUtils.setAllCaps(bookingInfoLabel);
			ViewUtils.setAllCaps(insuranceLabel);

			//Map
			MapImageView mapImageView = Ui.findView(view, R.id.mini_map);
			List<Location> airPorts = new ArrayList<Location>();
			airPorts.add(waypointToLocation(leg.getFirstWaypoint()));
			for (int i = 0; i < leg.getSegmentCount(); i++) {
				Waypoint wp = leg.getSegment(i).mDestination;
				airPorts.add(waypointToLocation(wp));
			}
			mapImageView.setCenterPoint(airPorts.size() > 0 ? airPorts.get(0) : null);
			for (Location loc : airPorts) {
				mapImageView.setPoiPoint(loc);
			}

			//Arrival / Departure times
			Calendar departureTimeCal = leg.getFirstWaypoint().getMostRelevantDateTime();
			Calendar arrivalTimeCal = leg.getLastWaypoint().getMostRelevantDateTime();

			String departureTime = formatTime(departureTimeCal);
			String departureTz = res.getString(R.string.depart_tz_TEMPLATE, departureTimeCal
					.getTimeZone().getDisplayName(false, TimeZone.SHORT));
			String arrivalTime = formatTime(arrivalTimeCal);
			String arrivalTz = res.getString(R.string.arrive_tz_TEMPLATE, arrivalTimeCal.getTimeZone()
					.getDisplayName(false, TimeZone.SHORT));

			Ui.setText(view, R.id.departure_time, departureTime);
			Ui.setText(view, R.id.departure_time_tz, departureTz);
			Ui.setText(view, R.id.arrival_time, arrivalTime);
			Ui.setText(view, R.id.arrival_time_tz, arrivalTz);

			//Traveler names
			StringBuilder travelerSb = new StringBuilder();
			for (Traveler trav : mTripFlight.getTravelers()) {
				travelerSb.append(",");
				travelerSb.append(" ");
				if (!TextUtils.isEmpty(trav.getFirstName())) {
					travelerSb.append(trav.getFirstName().trim());
					travelerSb.append(" ");
				}
				if (!TextUtils.isEmpty(trav.getMiddleName())) {
					travelerSb.append(trav.getMiddleName().trim());
					travelerSb.append(" ");
				}
				if (!TextUtils.isEmpty(trav.getLastName())) {
					travelerSb.append(trav.getLastName().trim());
				}
			}
			String travString = travelerSb.toString().replaceFirst(",", "").trim();
			Ui.setText(view, R.id.passenger_name_list, travString);

			//Booking info (View receipt and polocies)
			View bookingInfo = Ui.findView(view, R.id.booking_info);
			final String infoUrl = mTripFlight.getParentTrip().getDetailsUrl();
			if (!TextUtils.isEmpty(infoUrl)) {
				bookingInfo.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						Intent insuranceIntent = WebViewActivity.getIntent(getContext(), infoUrl, R.style.FlightTheme,
								R.string.insurance, true);
						getContext().startActivity(insuranceIntent);
					}
				});
			}

			//Confirmation code
			if (mTripFlight.getConfirmations() != null && mTripFlight.getConfirmations().size() > 0
					&& !TextUtils.isEmpty(mTripFlight.getConfirmations().get(0).getConfirmationCode())) {
				//TODO: Confirmation codes come in an array, I think there should only ever be one, but in the event of more than one, we should do something...
				Ui.setText(view, R.id.confirmation_code, mTripFlight.getConfirmations().get(0).getConfirmationCode());
			}
			else {
				Ui.setText(view, R.id.confirmation_code, R.string.missing_booking_code);
			}

			//Insurance
			boolean hasInsurance = (this.mTripFlight.getParentTrip().getTripInsurance() != null && this.mTripFlight
					.getParentTrip().getTripInsurance().size() > 0);
			int insuranceVisibility = hasInsurance ? View.VISIBLE : View.GONE;
			View insuranceDivider = Ui.findView(view, R.id.insurance_divider);
			View insuranceContainer = Ui.findView(view, R.id.insurance_container);
			insuranceLabel.setVisibility(insuranceVisibility);
			insuranceDivider.setVisibility(insuranceVisibility);
			insuranceContainer.setVisibility(insuranceVisibility);
			if (hasInsurance) {
				Insurance insurance = null;
				for (int i = 0; i < this.mTripFlight.getParentTrip().getTripInsurance().size(); i++) {
					if (mTripFlight.getParentTrip().getTripInsurance().get(i).getLineOfBusiness()
							.equals(InsuranceLineOfBusiness.AIR)) {
						insurance = mTripFlight.getParentTrip().getTripInsurance().get(i);
					}
				}
				if (insurance != null) {
					TextView insuranceName = Ui.findView(view, R.id.insurance_name);
					insuranceName.setText(insurance.getPolicyName());
					final Insurance finalInsurance = insurance;

					View insuranceLinkView = Ui.findView(view, R.id.insurance_button);
					insuranceLinkView.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							Intent insuranceIntent = WebViewActivity.getIntent(getContext(),
									finalInsurance.getTermsUrl(), R.style.FlightTheme, R.string.insurance, true);
							getContext().startActivity(insuranceIntent);
						}
					});
				}
			}

			//Add the flight stuff
			ViewGroup flightLegContainer = Ui.findView(view, R.id.flight_leg_container);
			for (int j = 0; j < leg.getSegmentCount(); j++) {
				Flight segment = leg.getSegment(j);

				boolean isFirstSegment = (j == 0);
				boolean isLastSegment = (j == leg.getSegmentCount() - 1);

				if (isFirstSegment) {
					flightLegContainer.addView(getWayPointView(segment.mOrigin, WaypointType.DEPARTURE, inflater));
					flightLegContainer.addView(getDividerView());
				}

				flightLegContainer.addView(getFlightView(segment, departureTimeCal, arrivalTimeCal, inflater));
				flightLegContainer.addView(getDividerView());

				if (isLastSegment) {
					flightLegContainer.addView(getWayPointView(segment.mDestination, WaypointType.ARRIVAL, inflater));
				}
				else {
					flightLegContainer.addView(getWayPointView(segment.mDestination, WaypointType.LAYOVER, inflater));
					flightLegContainer.addView(getDividerView());
				}

			}

		}

		return view;
	}

	@Override
	protected View getSummaryView(LayoutInflater inflater, ViewGroup container, TripComponent tripComponent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected SummaryButton getSummaryLeftButton() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected SummaryButton getSummaryRightButton() {
		// TODO Auto-generated method stub
		return null;
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	private enum WaypointType {
		DEPARTURE, ARRIVAL, LAYOVER
	}

	private View getWayPointView(Waypoint waypoint, WaypointType type, LayoutInflater inflater) {
		View v = inflater.inflate(R.layout.snippet_itin_waypoint_row, null);
		Resources res = getResources();
		//TODO: Set icon based on type

		String airportName = waypoint.getAirport().mName;
		Ui.setText(v, R.id.layover_airport_name, airportName);
		if (type.equals(WaypointType.LAYOVER)) {
			//TODO: Need to get a different set of gates, so we will need another waypoint object...

			if (TextUtils.isEmpty(waypoint.getTerminal()) || TextUtils.isEmpty(waypoint.getGate())) {
				Ui.setText(v, R.id.layover_terminal_gate_one, R.string.no_terminal_gate_information);
				Ui.findView(v, R.id.layover_terminal_gate_two).setVisibility(View.GONE);
			}
			else {
				String arrivalGate = res.getString(R.string.arrival_terminal_TEMPLATE,
						waypoint.getTerminal(), waypoint.getGate());
				String departureGate = res.getString(R.string.arrival_terminal_TEMPLATE,
						waypoint.getTerminal(), waypoint.getGate());

				Ui.setText(v, R.id.layover_terminal_gate_one, arrivalGate);
				Ui.setText(v, R.id.layover_terminal_gate_two, departureGate);
			}

		}
		else {
			Ui.findView(v, R.id.layover_terminal_gate_two).setVisibility(View.GONE);

			if (TextUtils.isEmpty(waypoint.getTerminal()) || TextUtils.isEmpty(waypoint.getGate())) {
				Ui.setText(v, R.id.layover_terminal_gate_one, R.string.no_terminal_gate_information);
			}
			else {
				String termGate = res.getString(R.string.generic_terminal_TEMPLATE,
						waypoint.getTerminal(), waypoint.getGate());
				Ui.setText(v, R.id.layover_terminal_gate_one, termGate);
			}
		}

		return v;
	}

	private View getFlightView(Flight flight, Calendar minTime, Calendar maxTime, LayoutInflater inflater) {
		FlightLegSummarySection v = (FlightLegSummarySection) inflater.inflate(
				R.layout.section_flight_leg_summary_itin, null);
		v.bindFlight(flight, minTime, maxTime);
		return v;
	}

	private View getDividerView() {
		//TODO: WHY U NO USE DIMENS!?!?!
		View v = new View(this.getContext());
		v.setBackgroundColor(getResources().getColor(R.color.itin_divider_color));
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 1);
		lp.leftMargin = 10;
		lp.rightMargin = 10;
		lp.topMargin = 10;
		lp.bottomMargin = 10;
		v.setLayoutParams(lp);
		return v;
	}

	private String formatTime(Calendar cal) {
		DateFormat df = android.text.format.DateFormat.getTimeFormat(getContext());
		return df.format(DateTimeUtils.getTimeInLocalTimeZone(cal));
	}

	private Location waypointToLocation(Waypoint wp) {
		Location location = new Location();
		location.setLatitude(wp.getAirport().getLatE6() / 1E6);
		location.setLongitude(wp.getAirport().getLonE6() / 1E6);
		location.setCity(wp.getAirport().mCity);
		location.setCountryCode(wp.getAirport().mCountryCode);
		return location;
	}
}