package com.expedia.bookings.data.trips;

import org.joda.time.LocalDate;

import android.content.Context;

import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.lx.LxSearchParams;
import com.expedia.bookings.utils.LXDataUtils;

public class ItinCardDataLXAttach extends ItinCardData {

	private final TripHotel tripHotel;

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

	public LxSearchParams getLxSearchParams(Context context) {
		return LXDataUtils
			.fromHotelParams(context, new LocalDate(tripHotel.getStartDate()),
				new LocalDate(tripHotel.getEndDate()), tripHotel.getProperty().getLocation());
	}
}
