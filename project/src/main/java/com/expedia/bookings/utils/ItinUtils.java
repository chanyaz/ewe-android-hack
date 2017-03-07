package com.expedia.bookings.utils;

import org.joda.time.DateTime;

import android.content.Context;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.trips.TripComponent;
import com.mobiata.android.util.SettingUtils;

public class ItinUtils {

	public static boolean shouldShowCheckInLink(Context context, TripComponent.Type type, DateTime flightTime,
		String checkInLink) {
		if (ExpediaBookingApp.useTabletInterface() || !type.equals(TripComponent.Type.FLIGHT) || Strings.isEmpty(checkInLink)) {
			return false;
		}

		//For settings in debug mode
		if (BuildConfig.DEBUG && SettingUtils
			.get(context, context.getString(R.string.preference_show_itin_checkin), false)) {
			return true;
		}

		int hoursBetween = JodaUtils.hoursBetween(DateTime.now(), flightTime);

		return hoursBetween >= 1 && hoursBetween < 24;
	}

}
