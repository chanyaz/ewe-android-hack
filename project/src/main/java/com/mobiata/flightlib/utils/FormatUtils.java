package com.mobiata.flightlib.utils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import android.content.Context;

import com.expedia.bookings.R;
import com.mobiata.flightlib.data.Airline;
import com.mobiata.flightlib.data.Airport;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.data.FlightCode;
import com.mobiata.flightlib.data.Waypoint;
import com.mobiata.flightlib.data.sources.FlightStatsDbUtils;

/**
 * For formatting strings.
 *
 * These methods assume nothing exists...
 * @author dlew
 *
 */
public class FormatUtils {

	public static String formatFlightNumber(Flight flight, Context context) {
		String unknown = context.getString(R.string.unknown);
		if (flight == null || flight.getPrimaryFlightCode() == null) {
			return unknown;
		}

		FlightCode code = flight.getPrimaryFlightCode();
		Airline airline = FlightStatsDbUtils.getAirline(code.mAirlineCode);
		if (airline == null) {
			return unknown + " " + getFlightNumber(flight);
		}
		else if (airline.mAirlineName != null) {
			return (airline.mAirlineName) + " " + getFlightNumber(flight);
		}
		else if (airline.mAirlineCode != null) {
			return (airline.mAirlineCode) + " " + getFlightNumber(flight);
		}
		else {
			return unknown + " " + getFlightNumber(flight);
		}
	}

	private static String getFlightNumber(Flight flight) {
		if (flight != null && flight.getPrimaryFlightCode() != null && flight.getPrimaryFlightCode().mNumber != null) {
			return flight.getPrimaryFlightCode().mNumber;
		}
		return "";
	}

	public static String getCityName(Waypoint waypoint, Context context) {
		if (waypoint != null) {
			Airport airport = waypoint.getAirport();
			if (airport != null) {
				if (airport.mCity != null) {
					return airport.mCity;
				}
				else if (airport.mAirportCode != null) {
					return airport.mAirportCode;
				}
			}
		}
		return context.getString(R.string.unknown);
	}

	public static String formatAirline(Airline airline, Context context) {
		if (airline == null) {
			return context.getString(R.string.unknown);
		}
		else if (airline.mAirlineName != null) {
			if (airline.mAirlineCode != null) {
				return airline.mAirlineName + " (" + airline.mAirlineCode + ")";
			}
			else {
				return airline.mAirlineName;
			}
		}
		else if (airline.mAirlineCode != null) {
			return airline.mAirlineCode;
		}
		else {
			return context.getString(R.string.unknown);
		}
	}

	/* Imperial/Metric System Tools */

	public static String formatTimeZone(Airport airport, DateTime time, int maxLength) {
		if (airport != null && airport.mTimeZone != null) {
			return formatTimeZone(airport.mTimeZone, time, maxLength);
		}
		return "";
	}

	private static String formatTimeZone(DateTimeZone tz, DateTime time, int maxLength) {
		String timeZoneStr = tz.getShortName(time.getMillis());
		if (timeZoneStr != null) {
			if (tz.getOffset(time) == 0) {
				timeZoneStr = "GMT";
			}
			else if (timeZoneStr.startsWith("GMT+0")) {
				timeZoneStr = "GMT+" + timeZoneStr.substring(5);
			}
			else if (timeZoneStr.startsWith("GMT-0")) {
				timeZoneStr = "GMT-" + timeZoneStr.substring(5);
			}
			else if (timeZoneStr.startsWith("+0")) {
				timeZoneStr = "GMT+" + timeZoneStr.substring(2);
			}
			else if (timeZoneStr.startsWith("-0")) {
				timeZoneStr = "GMT-" + timeZoneStr.substring(2);
			}
			else if (timeZoneStr.startsWith("+") || timeZoneStr.startsWith("-")) {
				timeZoneStr = "GMT" + timeZoneStr.substring(1);
			}
			else if (maxLength > 0 && timeZoneStr.length() > maxLength) {
				int offsetSeconds = tz.getOffset(time) / 1000;
				int offsetMinutes = Math.abs(offsetSeconds / 60);
				int offsetHours = offsetMinutes / 60;
				offsetMinutes -= (offsetHours * 60);

				timeZoneStr = "GMT";
				if (offsetHours > 0 || offsetMinutes > 0) {
					timeZoneStr += ((offsetSeconds > 0) ? "+" : "-") + offsetHours;
					if (offsetMinutes > 0) {
						timeZoneStr += ":" + ((offsetMinutes < 10) ? "0" : "") + offsetMinutes;
					}
				}
			}

			if (timeZoneStr.endsWith(":00")) {
				timeZoneStr = timeZoneStr.substring(0, timeZoneStr.length() - 3);
			}
		}
		return timeZoneStr;
	}

}
