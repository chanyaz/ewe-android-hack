package com.expedia.bookings.widget.itin;

import android.content.Context;
import android.text.TextUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.trips.ItinCardDataHotelAttach;
import com.expedia.bookings.data.trips.TripFlight;

public class HotelAttachCardContentGenerator extends AttachCardContentGenerator<ItinCardDataHotelAttach> {
	public HotelAttachCardContentGenerator(Context context, ItinCardDataHotelAttach itinCardData) {
		super(context, itinCardData);
	}

	@Override
	public int getButtonImageResId() {
		return R.drawable.ic_hotel_attach;
	}

	@Override
	public String getButtonText() {
		FlightTrip flightTrip = ((TripFlight) getItinCardData().getTripComponent()).getFlightTrip();
		FlightLeg flightLeg = flightTrip.getLeg(0);

		if (flightLeg != null && flightLeg.getLastWaypoint() != null
				&& flightLeg.getLastWaypoint().getAirport() != null
				&& !TextUtils.isEmpty(flightLeg.getLastWaypoint().getAirport().mCity)) {

			return getContext().getString(R.string.add_hotel_TEMPLATE, flightLeg.getLastWaypoint().getAirport().mCity);
		}

		return getContext().getString(R.string.add_hotel_fallback);
	}
}