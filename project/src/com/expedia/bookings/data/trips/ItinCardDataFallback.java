package com.expedia.bookings.data.trips;

import android.content.Context;
import android.text.format.DateUtils;

import com.expedia.bookings.data.trips.TripComponent.Type;

public class ItinCardDataFallback extends ItinCardData {

	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTOR
	//////////////////////////////////////////////////////////////////////////////////////

	public ItinCardDataFallback(TripComponent tripComponent) {
		super(tripComponent);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	@Override
	public TripComponent.Type getTripComponentType() {
		return TripComponent.Type.FALLBACK;
	}

	public CharSequence getRelativeDetailsStartDate(Context context) {
		long time = getStartDate().getMillisFromEpoch();
		long now = System.currentTimeMillis();
		long duration = Math.abs(now - time);

		if (duration < DateUtils.WEEK_IN_MILLIS) {
			return DateUtils.getRelativeTimeSpanString(time, now, DateUtils.DAY_IN_MILLIS, 0);
		}
		else {
			return DateUtils.getRelativeTimeSpanString(context, time, false);
		}
	}

	public Type getType() {
		return getTripComponent().getType();
	}

}