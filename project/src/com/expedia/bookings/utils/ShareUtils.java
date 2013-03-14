package com.expedia.bookings.utils;

import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.text.Html;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.pos.PointOfSale;
import com.mobiata.flightlib.data.Airport;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.data.Layover;
import com.mobiata.flightlib.utils.DateTimeUtils;
import com.mobiata.flightlib.utils.FormatUtils;

public class ShareUtils {

	private Context mContext;

	public ShareUtils(Context context) {
		mContext = context;
	}

	public String getFlightShareSubject(FlightTrip trip) {
		return getFlightShareSubject(trip.getLeg(0), trip.getLeg(trip.getLegCount() - 1));
	}

	public String getFlightShareSubject(FlightLeg leg) {
		return getFlightShareSubject(leg, leg);
	}

	private String getFlightShareSubject(FlightLeg firstLeg, FlightLeg lastLeg) {
		String destinationCity = StrUtils.getWaypointCityOrCode(firstLeg.getLastWaypoint());

		long start = DateTimeUtils.getTimeInLocalTimeZone(firstLeg.getFirstWaypoint().getMostRelevantDateTime())
				.getTime();
		long end = DateTimeUtils.getTimeInLocalTimeZone(
				lastLeg.getLastWaypoint().getMostRelevantDateTime()).getTime();
		String dateRange = DateUtils.formatDateRange(mContext, start, end, DateUtils.FORMAT_NUMERIC_DATE
				| DateUtils.FORMAT_SHOW_DATE);

		return mContext.getString(R.string.share_template_subject_flight, destinationCity, dateRange);
	}

	public String getFlightShareEmail(FlightTrip trip, List<Traveler> travelers) {
		return getFlightShareEmail(trip, trip.getLeg(0), trip.getLeg(trip.getLegCount() - 1), travelers);
	}

	public String getFlightShareEmail(FlightLeg leg, List<Traveler> travelers) {
		return getFlightShareEmail(null, leg, leg, travelers);
	}

	private String getFlightShareEmail(FlightTrip trip, FlightLeg firstLeg, FlightLeg lastLeg, List<Traveler> travelers) {
		int numTravelers = travelers.size();
		boolean moreThanOneLeg = firstLeg != lastLeg;

		String originCity = StrUtils.getWaypointCityOrCode(firstLeg.getFirstWaypoint());
		String destinationCity = StrUtils.getWaypointCityOrCode(firstLeg.getLastWaypoint());

		// Construct the body
		StringBuilder body = new StringBuilder();
		body.append(mContext.getString(R.string.share_hi));

		body.append("\n\n");

		if (!moreThanOneLeg) {
			body.append(mContext.getString(R.string.share_flight_one_way_TEMPLATE, originCity, destinationCity));
		}
		else {
			// Assume round trip for now
			body.append(mContext.getString(R.string.share_flight_round_trip_TEMPLATE, originCity, destinationCity));
		}

		body.append("\n\n");

		if (trip != null && !TextUtils.isEmpty(trip.getItineraryNumber())) {
			body.append(mContext.getString(R.string.share_flight_itinerary_TEMPLATE, trip.getItineraryNumber()));

			body.append("\n\n");
		}

		if (moreThanOneLeg) {
			body.append(mContext.getString(R.string.share_flight_section_outbound));

			body.append("\n\n");
		}

		addShareLeg(body, firstLeg);

		// Assume only round trips
		if (moreThanOneLeg) {
			body.append("\n\n");

			body.append(mContext.getString(R.string.share_flight_section_return));

			body.append("\n\n");

			addShareLeg(body, lastLeg);
		}

		body.append("\n\n");

		body.append(mContext.getString(R.string.share_travelers_section));

		body.append("\n");

		for (int i = 0; i < numTravelers; i++) {
			Traveler traveler = travelers.get(i);
			body.append(traveler.getFirstName() + " " + traveler.getLastName());
			body.append("\n");
		}

		body.append("\n");

		body.append(mContext.getString(R.string.share_template_long_ad, PointOfSale.getPointOfSale().getAppInfoUrl()));

		return body.toString();
	}

	private void addShareLeg(StringBuilder sb, FlightLeg flightLeg) {
		Resources res = mContext.getResources();
		int segCount = flightLeg.getSegmentCount();

		for (int a = 0; a < segCount; a++) {
			Flight flight = flightLeg.getSegment(a);

			if (a > 0) {
				Layover layover = new Layover(flightLeg.getSegment(a - 1), flight);
				String duration = DateTimeUtils.formatDuration(res, layover.mDuration);
				String waypoint = StrUtils.formatWaypoint(flight.mOrigin);
				sb.append(Html.fromHtml(mContext.getString(R.string.layover_duration_location_TEMPLATE, duration,
						waypoint)));
				sb.append("\n\n");
			}

			sb.append(mContext.getString(R.string.path_template, formatAirport(flight.mOrigin.getAirport()),
					formatAirport(flight.mDestination.getAirport())));
			sb.append("\n");
			long start = DateTimeUtils.getTimeInLocalTimeZone(flight.mOrigin.getMostRelevantDateTime()).getTime();
			sb.append(DateUtils.formatDateTime(mContext, start, DateUtils.FORMAT_SHOW_DATE
					| DateUtils.FORMAT_ABBREV_WEEKDAY | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_WEEKDAY));
			sb.append("\n");
			long end = DateTimeUtils.getTimeInLocalTimeZone(flight.mDestination.getMostRelevantDateTime()).getTime();
			sb.append(DateUtils.formatDateRange(mContext, start, end, DateUtils.FORMAT_SHOW_TIME));
			sb.append("\n");
			sb.append(FormatUtils.formatFlightNumber(flight, mContext));

			if (a + 1 != segCount) {
				sb.append("\n\n");
			}
		}
	}

	private String formatAirport(Airport airport) {
		if (!TextUtils.isEmpty(airport.mCity)) {
			return airport.mCity + " (" + airport.mAirportCode + ")";
		}
		else {
			return airport.mAirportCode;
		}
	}
}
