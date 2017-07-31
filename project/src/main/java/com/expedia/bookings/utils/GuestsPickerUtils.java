package com.expedia.bookings.utils;

import java.util.List;

import com.expedia.bookings.data.ChildTraveler;

public class GuestsPickerUtils {

	private static final int MAX_GUESTS = 6;
	public static final int MIN_ADULTS = 1;
	private static final int MAX_ADULTS = 6;
	private static final int MAX_CHILDREN = 4;
	public static final int MIN_CHILD_AGE = 0;
	public static final int MAX_CHILD_AGE = 17;
	public static final int MIN_RAIL_CHILD_AGE = 0;
	public static final int MAX_RAIL_CHILD_AGE = 15;
	public static final int MIN_RAIL_YOUTH_AGE = 16;
	public static final int MAX_RAIL_YOUTH_AGE = 25;
	public static final int MIN_RAIL_SENIORS_AGE = 60;
	public static final int MAX_RAIL_SENIORS_AGE = 62;

	public static final int MIN_CHILD_PC_AGE = 2;
	public static final int MIN_ADULT_CHILD_PC_AGE = 12;
	public static final int MIN_ADULT_PC_AGE = 18;
	public static final int MAX_SEAT_PREFERENCE = 2;

	public static int getMaxAdults(int numChildren) {
		return Math.min(MAX_ADULTS, MAX_GUESTS - numChildren);
	}

	public static int getMaxChildren(int numAdults) {
		return Math.min(MAX_CHILDREN, MAX_GUESTS - numAdults);
	}

	public static boolean moreInfantsThanAvailableLaps(int numAdults, List<ChildTraveler> children) {
		int infantCount = 0;
		int adultChildCount = 0;
		for (ChildTraveler c : children) {
			int age = c.getAge();
			if (age < GuestsPickerUtils.MIN_CHILD_PC_AGE) {
				infantCount++;
			}
			else if (age < GuestsPickerUtils.MIN_ADULT_PC_AGE && age >= GuestsPickerUtils.MIN_ADULT_CHILD_PC_AGE) {
				adultChildCount++;
			}
		}
		return infantCount > numAdults + adultChildCount;
	}
}
