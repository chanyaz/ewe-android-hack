package com.expedia.bookings.utils;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.FlightSearchActivity;
import com.expedia.bookings.data.ChildTraveler;
import com.expedia.bookings.widget.ChildAgeSpinnerAdapter;
import com.expedia.bookings.widget.FlightsChildAgeSpinnerAdapter;
import com.mobiata.android.util.SettingUtils;

public class GuestsPickerUtils {

	private static final int MAX_GUESTS = 6;
	public static final int MIN_ADULTS = 1;
	private static final int MAX_ADULTS = 6;
	public static final int MIN_CHILDREN = 0;
	private static final int MAX_CHILDREN = 4;
	private static final int DEFAULT_CHILD_AGE = 10;
	public static final int MIN_CHILD_AGE = 0;
	public static final int MAX_CHILD_AGE = 17;

	public static void updateNumberPickerRanges(com.expedia.bookings.widget.NumberPicker adultsNumberPicker,
												com.expedia.bookings.widget.NumberPicker childrenNumberPicker) {
		adultsNumberPicker.setMinValue(MIN_ADULTS);
		adultsNumberPicker.setMaxValue(getMaxAdults(childrenNumberPicker.getValue()));
		childrenNumberPicker.setMinValue(MIN_CHILDREN);
		childrenNumberPicker.setMaxValue(getMaxChildren(adultsNumberPicker.getValue()));
	}

	public static int getMaxAdults(int numChildren) {
		return Math.min(MAX_ADULTS, MAX_GUESTS - numChildren);
	}

	public static int getMaxChildren(int numAdults) {
		return Math.min(MAX_CHILDREN, MAX_GUESTS - numAdults);
	}

	public static void configureAndUpdateDisplayedValues(Context context,
														 com.expedia.bookings.widget.SimpleNumberPicker adultsNumberPicker,
														 com.expedia.bookings.widget.SimpleNumberPicker childrenNumberPicker) {
		int numAdults = adultsNumberPicker.getValue();
		int numChildren = childrenNumberPicker.getValue();

		adultsNumberPicker.setMinValue(MIN_ADULTS);
		adultsNumberPicker.setMaxValue(getMaxAdults(numChildren));
		childrenNumberPicker.setMinValue(MIN_CHILDREN);
		childrenNumberPicker.setMaxValue(getMaxChildren(numAdults));

		adultsNumberPicker.setValue(numAdults);
		childrenNumberPicker.setValue(numChildren);
	}

	public static void showOrHideChildAgeSpinners(Context context, List<ChildTraveler> children, View container, OnItemSelectedListener listener) {
		showOrHideChildAgeSpinners(context, children, container, listener, View.GONE);
	}

	public static void showOrHideChildAgeSpinners(Context context, List<ChildTraveler> children, View container,
												  OnItemSelectedListener listener, int hiddenState) {
		if (container == null) {
			return;
		}

		if (hiddenState != View.GONE && hiddenState != View.INVISIBLE) {
			throw new IllegalArgumentException("hiddenState must be one of View.GONE or View.INVISIBLE");
		}

		int numChildren = children == null ? 0 : children.size();

		if (numChildren == 0) {
			return;
		}

		for (int i = 0; i < MAX_CHILDREN; i++) {
			View row = GuestsPickerUtils.getChildAgeLayout(container, i);
			int visibility = i < numChildren ? View.VISIBLE : hiddenState;
			row.setVisibility(visibility);

			// This is needed for landscape view
			if (row.getParent() instanceof ViewGroup) {
				ViewGroup parent = ((ViewGroup) row.getParent());
				if (parent.getChildAt(0) == row) {
					parent.setVisibility(visibility);
				}
			}

			if (i < numChildren) {
				Spinner spinner = (Spinner) row;
				if (row.getTag() == null) {
					// Use the row's Tag to determine if we've initialized this label/spinner yet.
					row.setTag(i);
					spinner.setPrompt(context.getString(R.string.prompt_select_child_age,
						GuestsPickerUtils.MIN_CHILD_AGE, GuestsPickerUtils.MAX_CHILD_AGE));
					if (calledFromFlightsSearchActivity(context)) {
						spinner.setAdapter(new FlightsChildAgeSpinnerAdapter(context));
						spinner.setSelection(getChildSpinnerSelection(children.get(i)) - MIN_CHILD_AGE);
					}
					else {
						spinner.setAdapter(new ChildAgeSpinnerAdapter(context));
						spinner.setSelection(children.get(i).getAge() - MIN_CHILD_AGE);
					}
					spinner.setOnItemSelectedListener(listener);
				}
			}
		}
	}

	public static int getChildSpinnerSelection(ChildTraveler child) {
		int selection;
		if (child.getAge() == 0) {
			selection = child.usingSeat() ? 1 : 0;
		}
		else if (child.getAge() == 1) {
			selection = child.usingSeat() ? 3 : 2;
		}
		else {
			selection = child.getAge() + 2;
		}
		return selection;
	}

	public static void resizeChildrenList(Context context, List<ChildTraveler> children, int count) {
		while (children.size() > count) {
			children.remove(children.size() - 1);
		}
		while (children.size() < count) {
			children.add(getDefaultChildTraveler(context, children.size()));
		}
	}

	public static ChildTraveler getDefaultChildTraveler(Context context, int index) {
		int defaultAge = SettingUtils.get(context, "default_child_age_" + index, DEFAULT_CHILD_AGE);
		boolean defaultSeatUse = SettingUtils.get(context, "default_child_seat_use_" + index, false);
		return new ChildTraveler(defaultAge, defaultSeatUse);
	}

	public static void updateDefaultChildAges(Context context, List<ChildTraveler> children) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		for (int i = 0; i < children.size(); i++) {
			editor.putInt("default_child_age_" + i, children.get(i).getAge());
		}
		SettingUtils.commitOrApply(editor);
	}

	public static void updateDefaultChildTravelers(Context context, List<ChildTraveler> children) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		for (int i = 0; i < children.size(); i++) {
			editor.putInt("default_child_age_" + i, children.get(i).getAge());
			editor.putBoolean("default_child_seat_use_" + i, children.get(i).usingSeat());
		}
		SettingUtils.commitOrApply(editor);
	}

	public static void setChildrenFromSpinners(Context context, View container, List<ChildTraveler> children) {
		for (int i = 0; i < children.size(); i++) {
			View row = getChildAgeLayout(container, i);
			if (row == null) {
				continue;
			}
			Integer position = (Integer) ((Spinner) row).getSelectedItem();
			ChildTraveler child = calledFromFlightsSearchActivity(context) ? spinnerPositionToChildTraveler(position) : new ChildTraveler(position, false);
			children.set(i, child);
		}
	}

	public static ChildTraveler spinnerPositionToChildTraveler(int position) {
		ChildTraveler child = new ChildTraveler();
		if (position != 0 && position != 2) {
			child.setSeatUse(true);
		}
		if (position <= 1) {
			child.setAge(0);
		}
		else if (position == 2 || position == 3) {
			child.setAge(1);
		}
		else {
			child.setAge(position -= 2);
		}
		return child;
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

	private static boolean calledFromFlightsSearchActivity(Context context) {
		return context instanceof FlightSearchActivity ? true : false;
	}

}
