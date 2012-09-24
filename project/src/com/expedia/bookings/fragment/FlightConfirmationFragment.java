package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightSearch;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Itinerary;
import com.mobiata.android.util.Ui;
import com.mobiata.flightlib.data.Airport;

// We can assume that if this fragment loaded we successfully booked, so most
// data we need to grab is available.
public class FlightConfirmationFragment extends Fragment {

	public static final String TAG = FlightConfirmationFragment.class.getName();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_flight_confirmation, container, false);

		FlightSearch search = Db.getFlightSearch();
		FlightTrip trip = search.getSelectedFlightTrip();
		FlightLeg leg1 = trip.getLeg(0);
		Airport destinationAirport = leg1.getLastWaypoint().getAirport();
		Itinerary itinerary = Db.getItinerary(trip.getItineraryNumber());

		Ui.setText(v, R.id.going_to_text_view,
				getString(R.string.yay_going_somewhere_TEMPLATE, destinationAirport.mCity));

		Ui.setText(v, R.id.itinerary_text_view,
				Html.fromHtml(getString(R.string.itinerary_confirmation_TEMPLATE, itinerary.getItineraryNumber(),
						Db.getFlightBookingEmail())));

		Ui.setText(v, R.id.hotels_action_text_view, getString(R.string.hotels_in_TEMPLATE, destinationAirport.mCity));

		// We need to capitalize in code because the all_caps field isn't until a later API
		Ui.setText(v, R.id.get_a_room_text_view, getString(R.string.get_a_room).toUpperCase());
		Ui.setText(v, R.id.more_actions_text_view, getString(R.string.more_actions).toUpperCase());

		return v;
	}
}
