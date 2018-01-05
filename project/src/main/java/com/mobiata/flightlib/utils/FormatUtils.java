package com.mobiata.flightlib.utils;

import java.text.NumberFormat;

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

	public static final int F_METRIC = 1;
	public static final int F_IMPERIAL = 2;
	public static final int F_LONG = 8; // "pass in F_LONG if you want "100 miles" instead of "100 mi". defaults to abbreviated template

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

	private static final double MILES_TO_KILOMETERS = 1.609344;


	/**
	 * Format a distance into a string that looks like "500 mi", or something similar. Use the
	 * flags to achieve the functionality you desire, such as unit type (miles or km) and template
	 * length (short or long). If no flags are given, the default is the short template using imperial
	 * units, e.g. "500 mi". If you pass both F_METRIC and F_IMPERIAL, we choose F_METRIC.
	 * @param context - used to access Resources
	 * @param miles - number of miles
	 * @param flags F_METRIC - print distance in kilometers, km
	 *              F_IMPERIAL - print distance in miles, mi
	 *              F_LONG - use the long string template, i.e "500 miles" instead of "500 mi"
	 * @return formatted distance string in your specified unit and string template length
	 */
	public static String formatDistance(Context context, int miles, int flags) {
		NumberFormat numFormat = NumberFormat.getIntegerInstance();
		numFormat.setGroupingUsed(true);

		int distanceMetricTemplateResId = R.string.distance_metric_template;
		int distanceImperialTemplateResId = R.string.distance_imperial_template;
		if ((flags & F_LONG) != 0) {
			distanceMetricTemplateResId = R.string.distance_metric_full_template;
			distanceImperialTemplateResId = R.string.distance_imperial_full_template;
		}

		if ((flags & F_METRIC) != 0) {
			int km = (int) Math.round(miles * MILES_TO_KILOMETERS);
			return context.getString(distanceMetricTemplateResId, numFormat.format(km));
		}
		else {
			return context.getString(distanceImperialTemplateResId, numFormat.format(miles));
		}
	}

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
