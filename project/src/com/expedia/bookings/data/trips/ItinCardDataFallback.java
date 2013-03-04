package com.expedia.bookings.data.trips;

import java.util.Locale;

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

		CharSequence ret;
		if (duration < DateUtils.WEEK_IN_MILLIS) {
			ret = DateUtils.getRelativeTimeSpanString(time, now, DateUtils.DAY_IN_MILLIS, 0);
		}
		else {
			ret = DateUtils.getRelativeTimeSpanString(context, time, false);
		}

		ret = ret.subSequence(0, 1).toString().toUpperCase(Locale.getDefault()) + ret.subSequence(1, ret.length());
		return ret;
	}

	public Type getType() {
		return getTripComponent().getType();
	}

}