package com.expedia.bookings.fragment;

import java.util.Calendar;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.FlightTripLeg;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.section.FlightLayoverSection;
import com.expedia.bookings.section.FlightPathSection;
import com.expedia.bookings.section.FlightSegmentSection;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.flightlib.utils.DateTimeUtils;
import com.mobiata.flightlib.utils.FormatUtils;

public class FlightDetailsFragment extends Fragment {

	public static final String TAG = FlightDetailsFragment.class.getName();

	private static final String ARG_TRIP_LEG = "ARG_TRIP_LEG";

	// Cached copies, not to be stored
	private FlightTripLeg mFlightTripLeg;
	private FlightTrip mFlightTrip;
	private FlightLeg mFlightLeg;

	public static FlightDetailsFragment newInstance(FlightTrip trip, FlightLeg leg) {
		FlightDetailsFragment fragment = new FlightDetailsFragment();
		Bundle args = new Bundle();
		JSONUtils.putJSONable(args, ARG_TRIP_LEG, new FlightTripLeg(trip, leg));
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_flight_details, container, false);

		FlightTrip trip = getFlightTrip();
		FlightLeg leg = getFlightLeg();

		// Format header
		String duration = DateTimeUtils.formatDuration(getResources(), (int) (leg.getDuration() / 60000));
		String distance = FormatUtils.formatDistance(getActivity(), leg.getDistanceInMiles());
		Ui.setText(v, R.id.duration_distance_text_view,
				Html.fromHtml(getString(R.string.time_distance_TEMPLATE, duration, distance)));

		// Figure out which string to use for the upper-right label
		int bookNowResId;
		if (trip.getLegCount() == 1) {
			bookNowResId = R.string.one_way_price_TEMPLATE;
		}
		else {
			if (trip.getLeg(0).equals(leg)) {
				bookNowResId = R.string.round_trip_price_TEMPLATE;
			}
			else {
				bookNowResId = R.string.book_now_price_TEMPLATE;
			}
		}

		Ui.setText(v, R.id.book_price_text_view,
				Html.fromHtml(getString(bookNowResId, trip.getTotalFare().getFormattedMoney(Money.F_NO_DECIMAL))));

		// Format content
		ViewGroup infoContainer = Ui.findView(v, R.id.flight_info_container);

		// Initial header
		FlightPathSection flightPathSection = (FlightPathSection) inflater.inflate(R.layout.section_flight_path,
				infoContainer, false);
		flightPathSection.bind(leg.getSegment(0));
		infoContainer.addView(flightPathSection);

		// Add each card, with layovers in between
		int cardMargins = (int) getResources().getDimension(R.dimen.flight_segment_margin);
		Calendar minTime = leg.getFirstWaypoint().getMostRelevantDateTime();
		Calendar maxTime = leg.getLastWaypoint().getMostRelevantDateTime();
		int segmentCount = leg.getSegmentCount();
		for (int a = 0; a < segmentCount; a++) {
			if (a != 0) {
				FlightLayoverSection flightLayoverSection = (FlightLayoverSection) inflater.inflate(
						R.layout.section_flight_layover, infoContainer, false);
				flightLayoverSection.bind(leg.getSegment(a - 1), leg.getSegment(a));
				infoContainer.addView(flightLayoverSection);
			}

			FlightSegmentSection flightSegmentSection = (FlightSegmentSection) inflater.inflate(
					R.layout.section_flight_segment, infoContainer, false);
			flightSegmentSection.bind(leg.getSegment(a), trip.getFlightSegmentAttributes(leg).get(a), minTime, maxTime);
			MarginLayoutParams params = (MarginLayoutParams) flightSegmentSection.getLayoutParams();
			params.setMargins(cardMargins, cardMargins, cardMargins, cardMargins);
			infoContainer.addView(flightSegmentSection);
		}

		return v;
	}

	public FlightTripLeg getFlightTripLeg() {
		if (mFlightTripLeg == null) {
			mFlightTripLeg = JSONUtils.getJSONable(getArguments(), ARG_TRIP_LEG, FlightTripLeg.class);
		}
		return mFlightTripLeg;
	}

	public FlightTrip getFlightTrip() {
		if (mFlightTrip == null) {
			mFlightTrip = getFlightTripLeg().getFlightTrip();
		}
		return mFlightTrip;
	}

	public FlightLeg getFlightLeg() {
		if (mFlightLeg == null) {
			mFlightLeg = getFlightTripLeg().getFlightLeg();
		}
		return mFlightLeg;
	}
}
