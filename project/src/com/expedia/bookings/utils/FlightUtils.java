package com.expedia.bookings.utils;

import android.content.Context;

import com.expedia.bookings.R;
import com.mobiata.flightlib.data.Airport;
import com.mobiata.flightlib.data.Waypoint;

public class FlightUtils {

	/**
	 * Returns the full name of the terminal for the passed waypoint. Normally this will
	 * be something like "Terminal A" or "Terminal 5" but in the case of airports with an
	 * international terminal, it could return "International Terminal". All localized, 
	 * of course.
	 */
	public static String getTerminalName(Context context, Waypoint waypoint) {
		String terminal = waypoint.getTerminal();
		if (waypoint.getAirport().mHasInternationalTerminalI && terminal.equals("I")) {
			return context.getString(R.string.International_Terminal);
		}
		return context.getString(R.string.Terminal_X_TEMPLATE, terminal);
	}
}
