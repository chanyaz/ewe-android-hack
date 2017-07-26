package com.expedia.bookings.enums;

import org.joda.time.LocalDate;

import android.util.Pair;

import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.abacus.AbacusUtils;
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
			int maxChildAge = Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppFlightTravelerFormRevamp)
				? GuestsPickerUtils.MAX_CHILD_PC_AGE : GuestsPickerUtils.MIN_ADULT_CHILD_PC_AGE;
			returnedRange = new Pair<>(GuestsPickerUtils.MIN_CHILD_PC_AGE, maxChildAge);
			break;
		case INFANT_IN_LAP:
		case INFANT_IN_SEAT:
			int maxInfantAge = Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppFlightTravelerFormRevamp)
				? GuestsPickerUtils.MAX_INFANT_AGE : GuestsPickerUtils.MIN_CHILD_PC_AGE;
			returnedRange = new Pair<>(GuestsPickerUtils.MIN_CHILD_AGE, maxInfantAge);
			break;
		}
		return returnedRange;
	}

	public static boolean isDateWithinPassengerCategoryRange(LocalDate birthdate, FlightSearchParams params, PassengerCategory passengerCategory) {
		LocalDate earliestBase = params.isRoundTrip() ? params.getReturnDate() : params.getDepartureDate();
		return isDateWithinPassengerCategoryRange(birthdate, earliestBase, params.getDepartureDate(), passengerCategory);
	}

	public static boolean isDateWithinPassengerCategoryRange(LocalDate birthDate, LocalDate earliestBase, LocalDate oldestBase,
		PassengerCategory passengerCategory) {
		Pair<Integer, Integer> inclusiveAgeBounds = getAcceptableAgeRange(passengerCategory);
		LocalDate earliestBirthDateAllowed = earliestBase.minusYears(inclusiveAgeBounds.second);
		LocalDate latestBirthDateAllowed  = oldestBase.minusYears(inclusiveAgeBounds.first);
		boolean afterEarliest = birthDate.compareTo(earliestBirthDateAllowed) > 0;
		boolean beforeLatest = birthDate.compareTo(latestBirthDateAllowed) <= 0;
		return beforeLatest && afterEarliest;
	}
}

