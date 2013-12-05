package com.expedia.bookings.utils;

import android.content.Context;
import android.content.res.Resources;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Distance;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.pos.PointOfSale;
import com.mobiata.flightlib.data.Waypoint;
import com.mobiata.flightlib.utils.FormatUtils;

public class FlightUtils {

	/**
	 * Returns the best Terminal/Gate string available, depending on the waypoint.
	 * If the gate and terminal are both known, something like "Terminal X, Gate Y",
	 * otherwise something less specific. If neither is known, "Gate TBD".
	 */
	public static String getTerminalGateString(Context context, Waypoint waypoint) {
		Resources res = context.getResources();
		if (waypoint.hasGate() && waypoint.hasTerminal()) {
			return waypoint.isInternationalTerminal()
					? res.getString(R.string.International_Terminal_Gate_X_TEMPLATE, waypoint.getGate())
					: res.getString(R.string.Terminal_X_Gate_Y_TEMPLATE, waypoint.getTerminal(), waypoint.getGate());
		}
		else if (waypoint.hasGate()) {
			//gate only
			return res.getString(R.string.Gate_X_TEMPLATE, waypoint.getGate());
		}
		else if (waypoint.hasTerminal()) {
			//terminal only
			return waypoint.isInternationalTerminal()
					? res.getString(R.string.International_Terminal)
					: res.getString(R.string.Terminal_X_TEMPLATE, waypoint.getTerminal());
		}
		else {
			//no gate or terminal info
			return res.getString(R.string.Gate_To_Be_Determined_abbrev);
		}
	}

	public static String formatDistance(Context context, FlightLeg leg) {
		return formatDistance(context, leg, 0);
	}

	public static String formatDistance(Context context, FlightLeg leg, int flags) {
		flags |= PointOfSale.getPointOfSale().getDistanceUnit() == Distance.DistanceUnit.MILES ? FormatUtils.F_IMPERIAL
				: FormatUtils.F_METRIC;
		return FormatUtils.formatDistance(context, leg.getDistanceInMiles(), flags);
	}

}
