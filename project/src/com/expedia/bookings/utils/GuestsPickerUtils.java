package com.expedia.bookings.utils;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.widget.Spinner;

import com.expedia.bookings.R;
import com.expedia.bookings.data.SearchParams;

public class GuestsPickerUtils {

	private static final int MAX_PER_TYPE = 4;
	private static final int MAX_GUESTS = 5;
	private static final int MIN_ADULTS = 1;
	private static final int MIN_CHILDREN = 0;
	private static final int DEFAULT_CHILD_AGE = 10;

	public static void configureDisplayedValues(Context context, android.widget.NumberPicker adultsNumberPicker,
			android.widget.NumberPicker childrenNumberPicker) {
		adultsNumberPicker.setDisplayedValues(getAdultsDisplayedValues(context));
		childrenNumberPicker.setDisplayedValues(getChildDisplayedValues(context));
	}

	private static String[] getAdultsDisplayedValues(Context context) {
		Resources r = context.getResources();
		String[] adultDisplayedValues = new String[MAX_PER_TYPE];
		for (int a = 1; a <= MAX_PER_TYPE; a++) {
			adultDisplayedValues[a - 1] = r.getQuantityString(R.plurals.number_of_adults, a, a);
		}
		return adultDisplayedValues;
	}

	private static String[] getChildDisplayedValues(Context context) {
		Resources r = context.getResources();
		String[] childDisplayedValues = new String[MAX_PER_TYPE + 1];
		for (int a = 0; a <= MAX_PER_TYPE; a++) {
			childDisplayedValues[a] = r.getQuantityString(R.plurals.number_of_children, a, a);
		}
		return childDisplayedValues;
	}

	// Handles updates to which values are valid in NumberPicker setups
	public static void updateNumberPickerRanges(android.widget.NumberPicker adultsNumberPicker,
			android.widget.NumberPicker childrenNumberPicker) {
		adultsNumberPicker.setMinValue(MIN_ADULTS);
		adultsNumberPicker.setMaxValue(getMaxAdults(childrenNumberPicker.getValue()));
		childrenNumberPicker.setMinValue(MIN_CHILDREN);
		childrenNumberPicker.setMaxValue(getMaxChildren(adultsNumberPicker.getValue()));
	}

	public static void updateNumberPickerRanges(com.expedia.bookings.widget.NumberPicker adultsNumberPicker,
			com.expedia.bookings.widget.NumberPicker childrenNumberPicker) {
		adultsNumberPicker.setMinValue(MIN_ADULTS);
		adultsNumberPicker.setMaxValue(getMaxAdults(childrenNumberPicker.getValue()));
		childrenNumberPicker.setMinValue(MIN_CHILDREN);
		childrenNumberPicker.setMaxValue(getMaxChildren(adultsNumberPicker.getValue()));
	}

	public static int getMaxPerType() {
		return MAX_PER_TYPE;
	}

	private static int getMaxAdults(int numChildren) {
		return Math.min(MAX_PER_TYPE, MAX_GUESTS - numChildren);
	}

	private static int getMaxChildren(int numAdults) {
		return Math.min(MAX_PER_TYPE, MAX_GUESTS - numAdults);
	}

	public static void configureAndUpdateDisplayedValues(Context context,
			com.mobiata.android.widget.NumberPicker adultsNumberPicker,
			com.mobiata.android.widget.NumberPicker childrenNumberPicker) {
		int numAdults = adultsNumberPicker.getCurrent();
		int numChildren = childrenNumberPicker.getCurrent();

		adultsNumberPicker.setRange(MIN_ADULTS, getMaxAdults(numChildren), getAdultsDisplayedValues(context));
		childrenNumberPicker.setRange(MIN_CHILDREN, getMaxChildren(numAdults), getChildDisplayedValues(context));

		adultsNumberPicker.setCurrent(numAdults);
		childrenNumberPicker.setCurrent(numChildren);
	}

	public static void updateSearchParamsGuestCounts(SearchParams searchParams, int numAdults, int numChildren) {
		searchParams.setNumAdults(numAdults);
		List<Integer> children = searchParams.getChildren();
		if (children == null) {
			children = new ArrayList<Integer>(numChildren);
			searchParams.setChildren(children);
		}
		while (children.size() > numChildren) {
			children.remove(children.size() - 1);
		}
		while (children.size() < numChildren) {
			children.add(DEFAULT_CHILD_AGE);
		}
	}

	public static void setChildrenSearchParamFromSpinners(View parent, SearchParams searchParams) {
		List<Integer> children = searchParams.getChildren();
		for (int i = 0; i < searchParams.getNumChildren(); i++) {
			View row = getChildAgeLayout(parent, i);
			if (row == null) {
				continue;
			}

			Spinner ageSpinner = (Spinner) row.findViewById(R.id.child_x_age_spinner);
			if (ageSpinner == null) {
				continue;
			}

			Integer age = (Integer) ageSpinner.getSelectedItem();
			children.set(i, age);
		}
	}
	
	public static View getChildAgeLayout(View parent, int index) {
		int resId = -1;
		switch (index) {
		case 0:
			resId = R.id.child_1_age_layout;
			break;
		case 1:
			resId = R.id.child_2_age_layout;
			break;
		case 2:
			resId = R.id.child_3_age_layout;
			break;
		case 3:
			resId = R.id.child_4_age_layout;
			break;
		}
		return parent.findViewById(resId);
	}

}
