package com.expedia.bookings.utils;

import android.content.Context;
import android.content.res.Resources;
import android.widget.NumberPicker;

import com.expedia.bookings.R;

public class GuestsPickerUtils {

	private static final int MAX_PER_TYPE = 4;
	private static final int MAX_GUESTS = 5;

	public static void configureDisplayedValues(Context context, NumberPicker adultsNumberPicker,
			NumberPicker childrenNumberPicker) {
		Resources r = context.getResources();
		String[] adultDisplayedValues = new String[MAX_PER_TYPE];
		for (int a = 1; a <= MAX_PER_TYPE; a++) {
			adultDisplayedValues[a - 1] = r.getQuantityString(R.plurals.number_of_adults, a, a);
		}
		String[] childDisplayedValues = new String[MAX_PER_TYPE + 1];
		for (int a = 0; a <= MAX_PER_TYPE; a++) {
			childDisplayedValues[a] = r.getQuantityString(R.plurals.number_of_children, a, a);
		}
		adultsNumberPicker.setDisplayedValues(adultDisplayedValues);
		childrenNumberPicker.setDisplayedValues(childDisplayedValues);
	}

	// Handles updates to which values are valid in NumberPicker setups
	public static void updateNumberPickerRanges(NumberPicker adultsNumberPicker, NumberPicker childrenNumberPicker) {
		int adults = adultsNumberPicker.getValue();
		int children = childrenNumberPicker.getValue();
		int total = adults + children;
		int remaining = MAX_GUESTS - total;

		adultsNumberPicker.setMinValue(1);
		adultsNumberPicker.setMaxValue(Math.min(MAX_PER_TYPE, adults + remaining));
		childrenNumberPicker.setMinValue(0);
		childrenNumberPicker.setMaxValue(Math.min(MAX_PER_TYPE, children + remaining));
	}
}
