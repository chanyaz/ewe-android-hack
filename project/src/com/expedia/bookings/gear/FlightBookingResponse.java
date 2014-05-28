package com.expedia.bookings.gear;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.trips.FlightConfirmation;
import com.expedia.bookings.data.trips.TripComponent;
import com.expedia.bookings.data.trips.TripFlight;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.data.Waypoint;

public class FlightBookingResponse extends GearResponse {

    private SimpleDateFormat calFormatter = new SimpleDateFormat("h:mm a");

    @Override
    public JSONObject getResponseForGear() {

        generateFlightResponse();

        return responseForGear;
    }


    public void generateFlightResponse() {

        TripComponent component = this.tripComponent;

        DateTime startDate = component.getStartDate();

        TripFlight compFlight = (TripFlight) component;
        FlightStatusProvider flightStatusProvider = new FlightStatusProvider();

        FlightTrip flightTrip = compFlight.getFlightTrip();
        int legCounts = flightTrip.getLegCount();
        Log.d("GearAccessoryProviderService", "legCounts " + legCounts);
        FlightLeg flightLeg = null;
        List<FlightConfirmation> confirmations = compFlight.getConfirmations();
        String confirmationNumber = null;
        String airlineName = null;
        for (int i = 0; i < legCounts; i++) {
            flightLeg = flightTrip.getLeg(i);
            DateTime endTime = new DateTime(flightLeg.getLastWaypoint().getMostRelevantDateTime());
            Log.d("GearAccessoryProviderService", endTime.toString());
            Log.d("GearAccessoryProviderService", DateTime.now().toString());
            Log.d("GearAccessoryProviderService", "cond " + endTime.isBeforeNow());
			if (confirmations != null && i < confirmations.size()) {
				confirmationNumber = getConfirmationNo(confirmations.get(i));
				airlineName = getAirlineName(confirmations.get(i));
			}
            if (endTime.isBeforeNow()) {
                continue;
            }
            break;
        }

        Flight flight = flightStatusProvider.getMostRelevantFlightSegmentGear(flightLeg);

        String flightStatus = flightStatusProvider.getFlightStatus(flight);

        Waypoint waypoint = flightLeg.getFirstWaypoint();
        DateTime startDateTime = new DateTime(waypoint.getMostRelevantDateTime().getTimeInMillis());
        DateTimeZone timeZone = DateTimeZone.forOffsetMillis(TimeZone.getDefault().getOffset(Calendar.ZONE_OFFSET));
        DateTime localDateTime = startDateTime.toDateTime(timeZone);

        DateTime endDate = new DateTime(flightLeg.getLastWaypoint().getMostRelevantDateTime().getTimeInMillis());
        DateTime endDateTime = endDate.toDateTime(timeZone);

        responseForGear = new JSONObject();
        try {
            responseForGear.put("type", "FLIGHT");
            responseForGear.put("confirmationNo", confirmationNumber);
            responseForGear.put("airlineName", airlineName + " " + flightLeg.getSegment(0).getPrimaryFlightCode().mNumber);
            responseForGear.put("flightStatus", flightStatus);
            responseForGear.put("departureTime", calFormatter.format(localDateTime.getMillis()));
            responseForGear.put("flightLegStartMillis", localDateTime.getMillis());
            responseForGear.put("flightLegEndMillis", endDateTime.getMillis());
            responseForGear.put("landingInformation", calFormatter.format(flightLeg.getLastWaypoint().getMostRelevantDateTime().getTime()) + " (" + flightLeg.getLastWaypoint().getAirport().toJson().getString("name") + ")");
            responseForGear.put("flightTerminalInfo", getTerminalInformation(flightLeg.getFirstWaypoint()));
            responseForGear.put("startDate", compFlight.getStartDate());
        } catch (Exception e) {
            Log.e(GearAccessoryProviderService.TAG, " Exception HotelsBookingResponse -- ", e);
        }

    }


    public String getConfirmationNo(FlightConfirmation obj) {
        return obj.getConfirmationCode();
    }

    public String getAirlineName(FlightConfirmation obj) {
        return obj.getCarrier();
    }

    public String getTerminalInformation(Waypoint waypoint) throws JSONException {
        StringBuffer sb = new StringBuffer();
        sb.append(waypoint.getAirport().mName);
        if (waypoint.hasTerminal()) {
            sb.append("-").append("Terminal ").append(waypoint.getTerminal());
        }
        if (waypoint.hasGate()) {
            sb.append(", ").append("Gate ").append(waypoint.getGate());
        }
        return sb.toString();
    }

}