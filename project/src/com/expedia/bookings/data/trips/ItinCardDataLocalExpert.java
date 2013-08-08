package com.expedia.bookings.data.trips;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import com.expedia.bookings.data.LocalExpertSite.Destination;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.utils.JodaUtils;

public class ItinCardDataLocalExpert extends ItinCardData {
	private static final int VALID_MINUTES_BEFORE = 60 * 24 * 2; // 2 days
	private static final int VALID_MINUTES_AFTER = 0;

	private static final String HAWAII = "hi";
	private static final String LAS_VEGAS = "las vegas";
	private static final String ORLANDO = "orlando";

	private static final List<String> CITIES = new ArrayList<String>() {
		{
			add(LAS_VEGAS);
			add(ORLANDO);
		}
	};

	private static final List<String> STATES = new ArrayList<String>() {
		{
			add(HAWAII);
		}
	};

	public ItinCardDataLocalExpert(TripHotel tripComponent) {
		super(tripComponent);
	}

	@Override
	public boolean hasSummaryData() {
		return false;
	}

	@Override
	public boolean hasDetailData() {
		return false;
	}

	public Destination getSiteDestination() {
		Location location = ((TripHotel) getTripComponent()).getProperty().getLocation();
		if (location == null) {
			return null;
		}

		final String city = location.getCity().toLowerCase();
		final String state = location.getStateCode().toLowerCase();

		if (state.equals(HAWAII)) {
			return Destination.HAWAII;
		}
		else if (city.equals(LAS_VEGAS)) {
			return Destination.LAS_VEGAS;
		}
		else if (city.equals(ORLANDO)) {
			return Destination.ORLANDO;
		}

		return null;
	}

	public static boolean validLocation(Location location) {
		if (location == null) {
			return false;
		}

		if (STATES.contains(location.getStateCode().toLowerCase())) {
			return true;
		}

		if (CITIES.contains(location.getCity().toLowerCase())) {
			return true;
		}

		return false;
	}

	public static boolean validDateTime(DateTime startDateTime, DateTime endDateTime) {
		LocalDate startDate = startDateTime.toLocalDate();
		LocalDate endDate = startDateTime.toLocalDate();
		LocalDate now = LocalDate.now();
		return JodaUtils.daysBetween(now, startDate) <= 2 && JodaUtils.isBeforeOrEquals(now, endDate);
	}
}
