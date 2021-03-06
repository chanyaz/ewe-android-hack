package com.expedia.bookings.enums;

import org.joda.time.LocalDate;

import android.util.Pair;

import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.utils.GuestsPickerUtils;

public enum PassengerCategory {
		ADULT,
		SENIOR,
		ADULT_CHILD,
		CHILD,
		INFANT_IN_SEAT,
		INFANT_IN_LAP;

	// Returns inclusive age-in-years bounds for given PassengerCategory
	public static Pair<Integer, Integer> getAcceptableAgeRange(PassengerCategory category) {
		// first == lowest age, second == highest age
		Pair<Integer, Integer> returnedRange = null;
		switch(category) {
		case ADULT:
		case SENIOR:
			returnedRange = new Pair<>(GuestsPickerUtils.MIN_ADULT_PC_AGE, 200);
			break;
		case ADULT_CHILD:
			returnedRange = new Pair<>(GuestsPickerUtils.MIN_ADULT_CHILD_PC_AGE, GuestsPickerUtils.MIN_ADULT_PC_AGE);
			break;
		case CHILD:
			returnedRange = new Pair<>(GuestsPickerUtils.MIN_CHILD_PC_AGE, GuestsPickerUtils.MIN_ADULT_CHILD_PC_AGE);
			break;
		case INFANT_IN_LAP:
		case INFANT_IN_SEAT:
			returnedRange = new Pair<>(0, GuestsPickerUtils.MIN_CHILD_PC_AGE);
			break;
		}
		return returnedRange;
	}

	public static boolean isDateWithinPassengerCategoryRange(LocalDate birthdate, FlightSearchParams params, PassengerCategory passengerCategory) {
		Pair<Integer, Integer> inclusiveAgeBounds = getAcceptableAgeRange(passengerCategory);
		LocalDate earliestBase = params.isRoundTrip() ? params.getReturnDate() : params.getDepartureDate();
		LocalDate earliestBirthdateAllowed = earliestBase.minusYears(inclusiveAgeBounds.second);
		LocalDate latestBirthdateAllowed  = params.getDepartureDate().minusYears(inclusiveAgeBounds.first);
		boolean afterEarliest = birthdate.compareTo(earliestBirthdateAllowed) > 0;
		boolean beforeLatest = birthdate.compareTo(latestBirthdateAllowed) <= 0;
		return beforeLatest && afterEarliest;
	}
}
