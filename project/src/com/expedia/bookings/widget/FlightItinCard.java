package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.trips.TripComponent;
import com.expedia.bookings.data.trips.TripFlight;

public class FlightItinCard extends ItinCard {
	public FlightItinCard(Context context) {
		super(context);
	}

	public FlightItinCard(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected String getHeaderText(TripComponent tripComponent) {
		FlightTrip flight = ((TripFlight) tripComponent).getFlightTrip();
		return flight.getLeg(flight.getLegCount() - 1).getLastWaypoint().getAirport().mCity;
	}

	@Override
	protected View getDetailsView(LayoutInflater inflater, ViewGroup container, TripComponent tripComponent) {
		View view = inflater.inflate(R.layout.include_itin_card_flight, container, false);

		return view;
	}
}