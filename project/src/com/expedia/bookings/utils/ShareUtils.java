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
import com.expedia.bookings.data.Money;
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
		int numLegs = trip.getLegCount();
		FlightLeg firstLeg = trip.getLeg(0);
		String destinationCity = StrUtils.getWaypointCityOrCode(firstLeg.getLastWaypoint());

		long start = DateTimeUtils.getTimeInLocalTimeZone(firstLeg.getFirstWaypoint().getMostRelevantDateTime())
				.getTime();
		long end = DateTimeUtils.getTimeInLocalTimeZone(
				trip.getLeg(numLegs - 1).getLastWaypoint().getMostRelevantDateTime()).getTime();
		String dateRange = DateUtils.formatDateRange(mContext, start, end, DateUtils.FORMAT_NUMERIC_DATE
				| DateUtils.FORMAT_SHOW_DATE);
		return mContext.getString(R.string.share_flight_title_TEMPLATE, destinationCity, dateRange);
	}

	public String getFlightShareEmail(FlightTrip trip, List<Traveler> travelers) {
		FlightLeg firstLeg = trip.getLeg(0);
		int numTravelers = travelers.size();
		int numLegs = trip.getLegCount();

		String originCity = StrUtils.getWaypointCityOrCode(firstLeg.getFirstWaypoint());
		String destinationCity = StrUtils.getWaypointCityOrCode(firstLeg.getLastWaypoint());

		// Construct the body
		StringBuilder body = new StringBuilder();
		body.append(mContext.getResources().getQuantityString(R.plurals.share_flight_start, numTravelers));

		body.append("\n\n");

		if (numLegs == 1) {
			body.append(mContext.getString(R.string.share_flight_one_way_TEMPLATE, originCity, destinationCity));
		}
		else {
			// Assume round trip for now
			body.append(mContext.getString(R.string.share_flight_round_trip_TEMPLATE, originCity, destinationCity));
		}

		body.append("\n\n");

		body.append(mContext.getString(R.string.share_flight_itinerary_TEMPLATE, trip.getItineraryNumber()));

		body.append("\n\n");

		Traveler traveler;
		// Note: Arguments for string are slightly different depending on single vs. multiple travelers
		if (numTravelers == 1) {
			traveler = travelers.get(0);
			body.append(mContext.getResources().getQuantityString(R.plurals.share_flight_name_TEMPLATE, numTravelers,
					traveler.getFirstName() + " " + traveler.getLastName()));
			body.append("\n\n");
		}
		else {
			for (int i = 0; i < numTravelers; i++) {
				traveler = travelers.get(i);
				body.append(mContext.getResources().getQuantityString(R.plurals.share_flight_name_TEMPLATE,
						numTravelers,
						i + 1, traveler.getFirstName() + " " + traveler.getLastName()));
				body.append("\n");
			}
			body.append("\n");
		}

		body.append(mContext.getString(R.string.share_flight_section_outbound));

		body.append("\n\n");

		addShareLeg(body, firstLeg);

		// Assume only round trips
		if (numLegs == 2) {
			body.append("\n\n");

			body.append(mContext.getString(R.string.share_flight_section_return));

			body.append("\n\n");

			addShareLeg(body, trip.getLeg(1));
		}

		body.append("\n\n");

		body.append(mContext.getResources().getQuantityString(R.plurals.share_flight_ticket_cost_TEMPLATE,
				numTravelers, trip.getBaseFare().getFormattedMoney()));

		body.append("\n");

		Money taxesAndFees = new Money(trip.getTaxes());
		taxesAndFees.add(trip.getFees());

		body.append(mContext.getString(R.string.share_flight_taxes_fees_TEMPLATE, taxesAndFees.getFormattedMoney()));

		body.append("\n\n");

		body.append(mContext.getString(R.string.share_flight_airfare_total_TEMPLATE, trip.getTotalFare()
				.getFormattedMoney()));

		body.append("\n\n");

		body.append(mContext.getString(R.string.share_flight_additional_fees_TEMPLATE, trip.getBaggageFeesUrl()));

		body.append("\n\n");

		body.append(mContext.getString(R.string.share_flight_support_TEMPLATE, PointOfSale.getPointOfSale()
				.getSupportPhoneNumber()));

		body.append("\n\n");

		body.append(mContext.getString(R.string.share_flight_shill_app));

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
