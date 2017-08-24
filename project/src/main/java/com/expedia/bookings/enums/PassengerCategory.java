package com.expedia.bookings.enums;


import org.joda.time.LocalDate;

import android.content.Context;
import android.util.Pair;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.utils.GuestsPickerUtils;

public enum PassengerCategory {
		ADULT(GuestsPickerUtils.MIN_ADULT_PC_AGE,
			GuestsPickerUtils.MAX_ADULT_AGE,
			R.string.flight_checkout_adult_age_label,
			R.string.ticket_type_adult,
			R.string.traveler_adult_error,
			R.string.traveler_adult_error),
		SENIOR(GuestsPickerUtils.MIN_ADULT_PC_AGE,
			GuestsPickerUtils.MAX_ADULT_AGE,
			R.string.flight_checkout_adult_age_label,
			R.string.ticket_type_adult,
			R.string.traveler_adult_error,
			R.string.traveler_adult_error),
		ADULT_CHILD(GuestsPickerUtils.MIN_ADULT_CHILD_PC_AGE,
			GuestsPickerUtils.MIN_ADULT_PC_AGE,
			R.string.flight_checkout_youth_age_label,
			R.string.ticket_type_youth,
			R.string.traveler_adult_child_error,
			R.string.traveler_youth_error_message),
		CHILD(GuestsPickerUtils.MIN_CHILD_PC_AGE,
			GuestsPickerUtils.MIN_ADULT_CHILD_PC_AGE,
			R.string.flight_checkout_children_age_label,
			R.string.ticket_type_child,
			R.string.traveler_child_error,
			R.string.traveler_child_error_message),
		INFANT_IN_SEAT(GuestsPickerUtils.MIN_RAIL_CHILD_AGE,
			GuestsPickerUtils.MIN_CHILD_PC_AGE,
			R.string.flight_checkout_infant_age_label,
			R.string.ticket_type_infant,
			R.string.traveler_infant_error,
			R.string.traveler_infant_error),
		INFANT_IN_LAP(GuestsPickerUtils.MIN_RAIL_CHILD_AGE,
			GuestsPickerUtils.MIN_CHILD_PC_AGE,
			R.string.flight_checkout_infant_age_label,
			R.string.ticket_type_infant,
			R.string.traveler_infant_error,
			R.string.traveler_infant_error);


	private final Pair<Integer, Integer> ageRange;
	private final int controlErrorString;
	private final int bucketedCategoryString;
	private final int bucketedAgeString;
	private final int bucketedErrorString;

	PassengerCategory(Integer minimumAge, Integer maximumAge, int ageString, int categoryString, int controlErrorString, int bucketedErrorString) {
		this.ageRange = new Pair<>(minimumAge, maximumAge);
		this.bucketedAgeString = ageString;
		this.bucketedCategoryString = categoryString;
		this.controlErrorString = controlErrorString;
		this.bucketedErrorString = bucketedErrorString;
	}

	public String getErrorString(Context context) {
		boolean isBucketed = Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppFlightTravelerFormRevamp);
		int errorString = isBucketed ? bucketedErrorString : controlErrorString;
		return context.getString(errorString);
	}

	public String getBucketedAgeString(Context context) {
		return context.getString(bucketedAgeString);
	}

	public String getBucketedCategoryString(Context context) {
		return context.getString(bucketedCategoryString);
	}

	private Pair<Integer, Integer> getAgeRange() {
		return ageRange;
	}

	public static boolean isDateWithinPassengerCategoryRange(LocalDate birthdate, FlightSearchParams params, PassengerCategory passengerCategory) {
		LocalDate earliestBase = params.isRoundTrip() ? params.getReturnDate() : params.getDepartureDate();
		return isDateWithinPassengerCategoryRange(birthdate, earliestBase, params.getDepartureDate(), passengerCategory);
	}

	public static boolean isDateWithinPassengerCategoryRange(LocalDate birthDate, LocalDate earliestBase, LocalDate oldestBase,
		PassengerCategory passengerCategory) {
		Pair<Integer, Integer> inclusiveAgeBounds = passengerCategory.getAgeRange();
		LocalDate earliestBirthDateAllowed = earliestBase.minusYears(inclusiveAgeBounds.second);
		LocalDate latestBirthDateAllowed  = oldestBase.minusYears(inclusiveAgeBounds.first);
		boolean afterEarliest = birthDate.compareTo(earliestBirthDateAllowed) > 0;
		boolean beforeLatest = birthDate.compareTo(latestBirthDateAllowed) <= 0;
		return beforeLatest && afterEarliest;
	}
}

