package com.expedia.bookings.data.trips;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.expedia.bookings.data.DateTime;
import com.expedia.bookings.data.Location;
import com.mobiata.flightlib.utils.DateTimeUtils;

public class ItinCardDataLocalExpert extends ItinCardData {
	private static final int VALID_MINUTES_BEFORE = 60 * 24 * 2; // 2 days
	private static final int VALID_MINUTES_AFTER = 0;

	private static final List<String> CITIES = new ArrayList<String>() {
		{
			add("orlando");
			add("las vegas");
		}
	};

	private static final List<String> STATES = new ArrayList<String>() {
		{
			add("hi");
		}
	};

	public ItinCardDataLocalExpert(TripComponent tripComponent) {
		super(tripComponent);
	}

	@Override
	public boolean hasDetailData() {
		return false;
	}

	public static boolean validLocation(Location location) {
		if (location == null) {
			return false;
		}

		if (CITIES.contains(location.getCity().toLowerCase())) {
			return true;
		}

		if (STATES.contains(location.getStateCode().toLowerCase())) {
			return true;
		}

		return false;
	}

	public static boolean validDateTime(DateTime startDateTime, DateTime endDateTime) {
		Calendar start = DateTimeUtils.roundCalendar(startDateTime.getCalendar());
		Calendar end = DateTimeUtils.roundCalendar(endDateTime.getCalendar());
		Calendar now = DateTimeUtils.roundCalendar(Calendar.getInstance());

		if (DateTimeUtils.compareDateTimes(start, now) <= VALID_MINUTES_BEFORE
				&& DateTimeUtils.compareDateTimes(end, now) <= VALID_MINUTES_AFTER) {
			return true;
		}

		return false;
	}
}
