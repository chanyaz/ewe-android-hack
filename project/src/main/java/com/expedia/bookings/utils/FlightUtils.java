package com.expedia.bookings.utils;

import org.joda.time.LocalDate;

import android.content.Context;
import android.content.res.Resources;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchParams;
import com.mobiata.flightlib.data.Waypoint;
import com.squareup.phrase.Phrase;

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

	public static String totalDurationContDesc(Context context, int legDuration) {
		String duration;
		String totalDurationContDesc;
		int minutes = Math.abs(legDuration % 60);
		int hours = Math.abs(legDuration / 60);

		if (hours > 0) {
			duration = Phrase.from(context, R.string.flight_hour_min_duration_template_cont_desc)
				.put("h", hours)
				.put("m", minutes)
				.format().toString();
		}
		else {
			duration = Phrase.from(context, R.string.flight_min_duration_template_cont_desc)
				.put("m", minutes)
				.format().toString();
		}
		totalDurationContDesc = Phrase.from(context, R.string.package_flight_overview_total_duration_TEMPLATE).put("duration", duration).format().toString();
		return totalDurationContDesc;
	}

	////////////////////////////////////////////
	// Flight Details Baggage Fees

	public interface OnBaggageFeeViewClicked {
		void onBaggageFeeViewClicked(String title, String url);
	}

	 /*
	  * Helper method to check if it's valid to start the flight search.
	  */
	public static boolean dateRangeSupportsFlightSearch(Context context) {
		FlightSearchParams params = Db.getFlightSearch().getSearchParams();
		LocalDate searchDate = params.getDepartureDate();
		LocalDate arrivalDate = params.getReturnDate();
		LocalDate maxSearchDate = LocalDate.now()
			.plusDays(context.getResources().getInteger(R.integer.calendar_max_days_flight_search) + 1);
		return arrivalDate != null ? arrivalDate.isBefore(maxSearchDate) : searchDate.isBefore(maxSearchDate);
	}
}
