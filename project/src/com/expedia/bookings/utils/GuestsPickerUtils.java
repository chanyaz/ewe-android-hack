package com.expedia.bookings.utils;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.widget.ChildAgeSpinnerAdapter;
import com.mobiata.android.util.SettingUtils;

public class GuestsPickerUtils {

	private static final int MAX_PER_TYPE = 4;
	private static final int MAX_GUESTS = 5;
	private static final int MIN_ADULTS = 1;
	private static final int MIN_CHILDREN = 0;
	private static final int DEFAULT_CHILD_AGE = 10;
	public static final int MIN_CHILD_AGE = 0;
	public static final int MAX_CHILD_AGE = 17;

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

	public static void showOrHideChildAgeSpinners(Context context, List<Integer> children, View container,
			OnItemSelectedListener listener) {
		if (container == null) {
			return;
		}

		int numChildren = children == null ? 0 : children.size();

		if (numChildren == 0) {
			container.setVisibility(View.GONE);
			return;
		}

		for (int i = 0; i < GuestsPickerUtils.getMaxPerType(); i++) {
			View row = GuestsPickerUtils.getChildAgeLayout(container, i);
			int visibility = i < numChildren ? View.VISIBLE : View.GONE;
			row.setVisibility(visibility);

			// This is needed for landscape view
			if (row.getParent() instanceof ViewGroup) {
				ViewGroup parent = ((ViewGroup) row.getParent());
				if (parent.getChildAt(0) == row) {
					parent.setVisibility(visibility);
				}
			}

			if (i < numChildren && row.getTag() == null) {
				// Use the row's Tag to determine if we've initialized this label/spinner yet.
				row.setTag(i);
				TextView label = (TextView) row.findViewById(R.id.child_x_text);
				label.setText(context.getString(R.string.child_x, i + 1));

				Spinner spinner = (Spinner) row.findViewById(R.id.child_x_age_spinner);
				spinner.setPrompt(context.getString(R.string.prompt_select_child_age, GuestsPickerUtils.MIN_CHILD_AGE,
						GuestsPickerUtils.MAX_CHILD_AGE));
				spinner.setAdapter(new ChildAgeSpinnerAdapter(context));
				spinner.setSelection(children.get(i) - MIN_CHILD_AGE);
				spinner.setOnItemSelectedListener(listener);
			}
		}

		container.setVisibility(View.VISIBLE);
	}

	public static void resizeChildrenList(Context context, List<Integer> children, int count) {
		while (children.size() > count) {
			children.remove(children.size() - 1);
		}
		while (children.size() < count) {
			children.add(getDefaultChildAge(context, children.size()));
		}
	}

	public static int getDefaultChildAge(Context context, int index) {
		return SettingUtils.get(context, "default_child_age_" + index, DEFAULT_CHILD_AGE);
	}

	public static void updateDefaultChildAges(Context context, List<Integer> children) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		for (int i = 0; i < children.size(); i++) {
			editor.putInt("default_child_age_" + i, children.get(i));
		}
		SettingUtils.commitOrApply(editor);
	}

	public static void setChildrenFromSpinners(Context context, View container, List<Integer> children) {
		for (int i = 0; i < children.size(); i++) {
			View row = getChildAgeLayout(container, i);
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
