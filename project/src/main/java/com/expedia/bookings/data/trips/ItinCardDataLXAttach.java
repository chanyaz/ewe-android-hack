package com.expedia.bookings.data.trips;

import android.content.Context;

import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.lx.LXSearchParams;
import com.expedia.bookings.utils.LXDataUtils;

public class ItinCardDataLXAttach extends ItinCardData {

	private TripHotel tripHotel;

	public ItinCardDataLXAttach(TripHotel tripHotel) {
		super(tripHotel);
		this.tripHotel = tripHotel;
	}

	@Override
	public boolean hasSummaryData() {
		return false;
	}

	@Override
	public boolean hasDetailData() {
		return false;
	}

	public Property getProperty() {
		return tripHotel.getProperty();
	}

	public LXSearchParams getLxSearchParams(Context context) {
		return LXDataUtils.fromHotelParams(context, tripHotel);
	}
}
