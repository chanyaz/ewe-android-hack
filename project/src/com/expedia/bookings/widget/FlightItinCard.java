package com.expedia.bookings.widget;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.trips.TripComponent;
import com.expedia.bookings.data.trips.TripComponent.Type;
import com.expedia.bookings.data.trips.TripFlight;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.util.ViewUtils;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.data.Waypoint;
import com.mobiata.flightlib.utils.DateTimeUtils;

public class FlightItinCard extends ItinCard {
	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	private TripFlight mTripFlight;
	private Waypoint mDestination;

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
	public void bind(TripComponent tripComponent) {
		mTripFlight = ((TripFlight) tripComponent);

		if (mTripFlight != null && mTripFlight.getFlightTrip().getLegCount() > 0) {
			mDestination = mTripFlight.getFlightTrip().getLeg(mTripFlight.getFlightTrip().getLegCount() - 1)
					.getLastWaypoint();
			super.bind(tripComponent);
		}
	}

	@Override
	protected String getHeaderImageUrl(TripComponent tripComponent) {
		return ((TripFlight) tripComponent).getDestinationImageUrl();
	}

	@Override
	protected String getHeaderText(TripComponent tripComponent) {
		if (mDestination != null) {
			return mDestination.getAirport().mCity;
		}

		return "Flight Card";
	}

	@Override
	protected View getDetailsView(LayoutInflater inflater, ViewGroup container, TripComponent tripComponent) {
		View view = inflater.inflate(R.layout.include_itin_card_flight, container, false);

		if (mTripFlight != null && mTripFlight.getFlightTrip() != null && mTripFlight.getFlightTrip().getLegCount() > 0) {
			Resources res = getResources();

			TextView confirmationCodeLabel = Ui.findView(view, R.id.confirmation_code_label);
			TextView passengersLabel = Ui.findView(view, R.id.passengers_label);

			ViewUtils.setAllCaps(confirmationCodeLabel);
			ViewUtils.setAllCaps(passengersLabel);

			FlightLeg firstLeg = mTripFlight.getFlightTrip().getLeg(0);
			FlightLeg lastLeg = mTripFlight.getFlightTrip().getLeg(mTripFlight.getFlightTrip().getLegCount() - 1);

			String departureTime = formatTime(firstLeg.getFirstWaypoint().getMostRelevantDateTime());
			String departureTz = String.format(res.getString(R.string.depart_tz_TEMPLATE), firstLeg.getFirstWaypoint()
					.getMostRelevantDateTime().getTimeZone().getDisplayName(false, TimeZone.SHORT));
			String arrivalTime = formatTime(lastLeg.getLastWaypoint().getMostRelevantDateTime());
			String arrivalTz = String.format(res.getString(R.string.arrive_tz_TEMPLATE), lastLeg.getLastWaypoint()
					.getMostRelevantDateTime().getTimeZone().getDisplayName(false, TimeZone.SHORT));

			Ui.setText(view, R.id.departure_time, departureTime);
			Ui.setText(view, R.id.departure_time_tz, departureTz);
			Ui.setText(view, R.id.arrival_time, arrivalTime);
			Ui.setText(view, R.id.arrival_time_tz, arrivalTz);

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

			//TODO: Use a real value. Seems like we might need to add an accessor someplace in the Flight Class in flight lib for this
			String bookingCode = "ROBOT";
			Ui.setText(view, R.id.confirmation_code, bookingCode);

			//Add the flight stuff
			ViewGroup flightLegContainer = Ui.findView(view, R.id.flight_leg_container);
			for (int i = 0; i < mTripFlight.getFlightTrip().getLegCount(); i++) {
				FlightLeg f = mTripFlight.getFlightTrip().getLeg(i);
				for (int j = 0; j < f.getSegmentCount(); j++) {
					Flight segment = f.getSegment(j);

					boolean isFirstSegment = (i == 0 && j == 0);
					boolean isLastSegment = (i == mTripFlight.getFlightTrip().getLegCount() - 1 && j == f
							.getSegmentCount() - 1);

					if (isFirstSegment) {
						flightLegContainer.addView(getWayPointView(segment.mOrigin, WaypointType.DEPARTURE, inflater));
						flightLegContainer.addView(getDividerView());
					}
					else {
						flightLegContainer.addView(getFlightView(segment));
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
			}

		}

		return view;
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
		
		String airportName = waypoint.mAirportCode;
		Ui.setText(v, R.id.layover_airport_name, airportName);
		if(type.equals(WaypointType.LAYOVER)){
			//TODO: Need to get a different set of gates...
			String arrivalGate = String.format(res.getString(R.string.arrival_terminal_TEMPLATE), waypoint.getTerminal(), waypoint.getGate());
			String departureGate = String.format(res.getString(R.string.arrival_terminal_TEMPLATE), waypoint.getTerminal(), waypoint.getGate());
			Ui.setText(v, R.id.layover_terminal_gate_one, arrivalGate);
			Ui.setText(v, R.id.layover_terminal_gate_two, departureGate);
			
		}else{
			Ui.findView(v,R.id.layover_terminal_gate_two ).setVisibility(View.GONE);
			
			String termGate = String.format(res.getString(R.string.generic_terminal_TEMPLATE), waypoint.getTerminal(), waypoint.getGate());
			Ui.setText(v, R.id.layover_terminal_gate_one, termGate);
		}
		
		return v;
	}

	private View getFlightView(Flight flight) {
		TextView tv = new TextView(this.getContext());
		tv.setText("FLIGHT:" + flight.getPrimaryFlightCode().mAirlineName + " " + flight.getPrimaryFlightCode().mNumber);
		return tv;
	}

	private View getDividerView() {
		//TODO: WHY U NO USE DIMENS!?!?!
		View v = new View(this.getContext());
		v.setBackgroundColor(Color.argb(128, 255, 255, 255));
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
}