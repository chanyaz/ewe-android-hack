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
import com.mobiata.flightlib.data.FlightCode;
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
        TripFlight compFlight = (TripFlight) component;
        FlightStatusProvider flightStatusProvider = new FlightStatusProvider();

        FlightTrip flightTrip = compFlight.getFlightTrip();
        int legCounts = flightTrip.getLegCount();

        FlightLeg flightLeg = null;
        String confirmationNumber = null;
        for (int i = 0; i < legCounts; i++) {
            flightLeg = flightTrip.getLeg(i);
            DateTime endTime = new DateTime(flightLeg.getLastWaypoint().getMostRelevantDateTime());
            if (endTime.isBeforeNow()) {
                continue;
            }
            break;
        }

        Flight flight = flightStatusProvider.getMostRelevantFlightSegmentGear(flightLeg);
        FlightCode flightCode = flight.getPrimaryFlightCode();

        List<FlightConfirmation> confirmations = compFlight.getConfirmations();
        for (int i = 0; i < confirmations.size(); i++) {
            FlightConfirmation flightConfirmation = confirmations.get(i);
            Log.d(GearAccessoryProviderService.TAG, "AirlineInfo Name:  " + flightCode.getAirline().mAirlineName + ", Code:  " + flightCode.mAirlineCode + " , Carrier " + flightConfirmation.getCarrier());
            String flightName = flightCode.getAirline().mAirlineName;
            if (flightConfirmation != null && flightName != null && (flightName.startsWith(flightConfirmation.getCarrier()) || flightName.equalsIgnoreCase(flightConfirmation.getCarrier()))) {
                confirmationNumber = confirmations.get(i).getConfirmationCode();
                break;
            }
        }
        Log.d(GearAccessoryProviderService.TAG, "confirmationNumber  == " + confirmationNumber);

        String flightStatus = flightStatusProvider.getFlightStatus(flight);
        Waypoint startWaypoint = flight.mOrigin;
        DateTimeZone timeZone = DateTimeZone.forOffsetMillis(TimeZone.getDefault().getOffset(Calendar.ZONE_OFFSET));
        DateTime startDateTime = new DateTime(startWaypoint.getMostRelevantDateTime().getTimeInMillis());
        DateTime localDateTime = startDateTime.toDateTime(timeZone);

        Waypoint endWaypoint = flight.getArrivalWaypoint();
        DateTime endDate = new DateTime(endWaypoint.getMostRelevantDateTime().getTimeInMillis());
        DateTime endDateTime = endDate.toDateTime(timeZone);

        responseForGear = new JSONObject();
        try {
            responseForGear.put("type", "FLIGHT");
            responseForGear.put("confirmationNo", confirmationNumber);
            responseForGear.put("airlineName", flightCode.getAirline().mAirlineName + " " + flightCode.mNumber);
            responseForGear.put("flightStatus", flightStatus);
            responseForGear.put("departureTime", calFormatter.format(localDateTime.getMillis()));
            responseForGear.put("flightLegStartMillis", localDateTime.getMillis());
            responseForGear.put("flightLegEndMillis", endDateTime.getMillis());
            responseForGear.put("landingInformation", calFormatter.format(endWaypoint.getMostRelevantDateTime().getTime()) + " (" + endWaypoint.getAirport().toJson().getString("name") + ")");
            responseForGear.put("flightTerminalInfo", getTerminalInformation(startWaypoint));
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