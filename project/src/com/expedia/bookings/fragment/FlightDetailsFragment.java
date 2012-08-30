package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.FlightTripLeg;
import com.expedia.bookings.data.Money;
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

		String duration = DateTimeUtils.formatDuration(getResources(), (int) (leg.getDuration() / 60000));
		String distance = FormatUtils.formatDistance(getActivity(), leg.getDistanceInMiles());

		Ui.setText(v, R.id.duration_distance_text_view,
				Html.fromHtml(getString(R.string.time_distance_TEMPLATE, duration, distance)));
		Ui.setText(v, R.id.book_price_text_view,
				Html.fromHtml(getString(R.string.book_now_price_TEMPLATE,
						trip.getTotalFare().getFormattedMoney(Money.F_NO_DECIMAL))));

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
