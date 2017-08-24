package com.expedia.bookings.server;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.data.FlightStatsFlightResponse;
import com.mobiata.android.Log;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.data.FlightCode;
import com.mobiata.flightlib.data.Waypoint;

public class FlightStatsFlightStatusResponseHandler extends JsonResponseHandler<FlightStatsFlightResponse> {

	private final String mAirline;

	public FlightStatsFlightStatusResponseHandler(String airline) {
		mAirline = airline;
	}

	@Override
	public FlightStatsFlightResponse handleJson(JSONObject response) {
		FlightStatsFlightResponse fsResponse = new FlightStatsFlightResponse();

		try {
			JSONArray flightStatuses = response.getJSONArray("flightStatuses");
			if (flightStatuses != null) {
				for (int i = 0; i < flightStatuses.length(); i++) {
					fsResponse.addFlight(parseFlight(flightStatuses.getJSONObject(i)));
				}
			}
		}
		catch (JSONException e) {
			Log.w("Error parsing FlightStatus response from FlightStats", e);
		}

		return fsResponse;
	}

	private Flight parseFlight(JSONObject json) throws JSONException {
		Flight flight = new Flight();

		JSONObject operationalTimes = json.getJSONObject("operationalTimes");

		flight.mFlightHistoryId = json.getInt("flightId");
		flight.mStatusCode = json.getString("status");

		parseFlightCode(json, flight, true);

		flight.setOriginWaypoint(new Waypoint(Waypoint.ACTION_DEPARTURE));
		flight.getOriginWaypoint().mAirportCode = json.getString("departureAirportFsCode");
		addDateTime(flight.getOriginWaypoint(), Waypoint.POSITION_UNKNOWN, Waypoint.ACCURACY_UNKNOWN, json, "departureDate");
		addDateTime(flight.getOriginWaypoint(), Waypoint.POSITION_UNKNOWN, Waypoint.ACCURACY_SCHEDULED, operationalTimes, "publishedDeparture");
		addDateTime(flight.getOriginWaypoint(), Waypoint.POSITION_GATE, Waypoint.ACCURACY_SCHEDULED, operationalTimes, "scheduledGateDeparture");
		addDateTime(flight.getOriginWaypoint(), Waypoint.POSITION_GATE, Waypoint.ACCURACY_ESTIMATED, operationalTimes, "estimatedGateDeparture");
		addDateTime(flight.getOriginWaypoint(), Waypoint.POSITION_GATE, Waypoint.ACCURACY_ACTUAL, operationalTimes, "actualGateDeparture");
		addDateTime(flight.getOriginWaypoint(), Waypoint.POSITION_RUNWAY, Waypoint.ACCURACY_SCHEDULED, operationalTimes, "flightPlanPlannedDeparture");
		addDateTime(flight.getOriginWaypoint(), Waypoint.POSITION_RUNWAY, Waypoint.ACCURACY_ESTIMATED, operationalTimes, "estimatedRunwayDeparture");
		addDateTime(flight.getOriginWaypoint(), Waypoint.POSITION_RUNWAY, Waypoint.ACCURACY_ACTUAL, operationalTimes, "actualRunwayDeparture");

		flight.setDestinationWaypoint(new Waypoint(Waypoint.ACTION_ARRIVAL));
		flight.getDestinationWaypoint().mAirportCode = json.getString("arrivalAirportFsCode");

		if (json.has("divertedAirportFsCode")) {
			flight.setDivertedWaypoint(new Waypoint(Waypoint.ACTION_DIVERTED));
			flight.getDivertedWaypoint().mAirportCode = json.getString("divertedAirportFsCode");
		}

		Waypoint arrival = flight.getArrivalWaypoint();
		addDateTime(arrival, Waypoint.POSITION_UNKNOWN, Waypoint.ACCURACY_UNKNOWN, json, "arrivalDate");
		addDateTime(arrival, Waypoint.POSITION_UNKNOWN, Waypoint.ACCURACY_SCHEDULED, operationalTimes, "publishedArrival");
		addDateTime(arrival, Waypoint.POSITION_GATE, Waypoint.ACCURACY_SCHEDULED, operationalTimes, "scheduledGateArrival");
		addDateTime(arrival, Waypoint.POSITION_GATE, Waypoint.ACCURACY_ESTIMATED, operationalTimes, "estimatedGateArrival");
		addDateTime(arrival, Waypoint.POSITION_GATE, Waypoint.ACCURACY_ACTUAL, operationalTimes, "actualGateArrival");
		addDateTime(arrival, Waypoint.POSITION_RUNWAY, Waypoint.ACCURACY_SCHEDULED, operationalTimes, "flightPlanPlannedArrival");
		addDateTime(arrival, Waypoint.POSITION_RUNWAY, Waypoint.ACCURACY_ESTIMATED, operationalTimes, "estimatedRunwayArrival");
		addDateTime(arrival, Waypoint.POSITION_RUNWAY, Waypoint.ACCURACY_ACTUAL, operationalTimes, "actualRunwayArrival");

		if (json.has("codeshares")) {
			JSONArray codeshares = json.getJSONArray("codeshares");
			for (int i = 0; i < codeshares.length(); i++) {
				parseFlightCode(codeshares.getJSONObject(i), flight, false);
			}
		}

		if (json.has("airportResources")) {
			JSONObject airportResources = json.getJSONObject("airportResources");
			flight.getOriginWaypoint().setTerminal(airportResources.optString("departureTerminal", null));
			flight.getOriginWaypoint().setGate(airportResources.optString("departureGate", null));
			arrival.setTerminal(airportResources.optString("arrivalTerminal", null));
			arrival.setGate(airportResources.optString("arrivalGate", null));
			flight.mBaggageClaim = airportResources.optString("baggage", null);
		}

		if (json.has("flightEquipment")) {
			JSONObject flightEquipment = json.getJSONObject("flightEquipment");
			if (flightEquipment.has("actualEquipmentIataCode")) {
				flight.mAircraftType = flightEquipment.getString("actualEquipmentIataCode");
			}
			else {
				flight.mAircraftType = flightEquipment.optString("scheduledEquipmentIataCode", null);
			}
		}

		return flight;
	}

	private void parseFlightCode(JSONObject json, Flight flight, boolean isOperator) throws JSONException {
		String carrierCodeKey = "carrierFsCode";
		if (!json.has(carrierCodeKey)) {
			carrierCodeKey = "fsCode";
		}

		if (json.has(carrierCodeKey) && json.has("flightNumber")) {
			FlightCode flightCode = new FlightCode();
			flightCode.mAirlineCode = json.getString(carrierCodeKey);
			flightCode.mNumber = json.getString("flightNumber");
			String relationship = json.optString("relationship");
			int flags = 0;

			if (isOperator) {
				flags = Flight.F_OPERATING_AIRLINE_CODE | Flight.F_PRIMARY_AIRLINE_CODE;
			}

			if (mAirline != null && mAirline.length() > 0) {
				flags |= (flightCode.mAirlineCode.equalsIgnoreCase(mAirline)) ? Flight.F_PRIMARY_AIRLINE_CODE : 0;
			}
			else if ("S".equals(relationship) || "X".equals(relationship)) {
				flags |= Flight.F_PRIMARY_AIRLINE_CODE;
			}

			flight.addFlightCode(flightCode, flags);
		}
		else {
			Log.i("skipping parseFlightCode due to missing carrierFsCode and/or flightNumber");
		}
	}

	private void addDateTime(Waypoint wp, int position, int accuracy, JSONObject times, String timeLabel) throws JSONException {
		if (times.has(timeLabel)) {
			wp.addDateTime(position, accuracy, times.getJSONObject(timeLabel).getString("dateLocal"));
		}
	}

}
