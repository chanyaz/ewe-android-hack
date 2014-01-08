package com.expedia.bookings.test.tests.pageModels.hotels;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;
import android.view.View;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.test.tests.pageModels.common.ScreenActions;
import com.expedia.bookings.test.utils.TestPreferences;
import com.expedia.bookings.widget.SimpleNumberPicker;

public class HotelsGuestPicker extends ScreenActions {

	private static final int SEARCH_BUTTON_ID = R.id.search_button;

	private static final int INCREMENT_BUTTON_ID = R.id.increment;
	private static final int DECREMENT_BUTTON_ID = R.id.decrement;

	private static final int ADULT_PICKER_VIEW_ID = R.id.adults_number_picker;
	private static final int CHILD_PICKER_VIEW_ID = R.id.children_number_picker;

	private static final int LOWER_TEXT_VIEW_ID = R.id.text_lower;
	private static final int CURRENT_TEXT_VIEW_ID = R.id.text_current;
	private static final int HIGHER_TEXT_VIEW_ID = R.id.text_higher;

	private static final int SELECT_CHILD_AGE_PLURAL_ID = R.plurals.select_each_childs_age;
	private static final int NUMBER_OF_ADULTS_PLURAL_ID = R.plurals.number_of_adults_TEMPLATE;
	private static final int NUMBER_OF_CHILDREN_PLURAL_ID = R.plurals.number_of_children;

	public HotelsGuestPicker(Instrumentation instrumentation, Activity activity, Resources res,
			TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

	// Object access

	public View searchButton() {
		return getView(SEARCH_BUTTON_ID);
	}

	public View incrementChildrenButton() {
		return getView(CHILD_PICKER_VIEW_ID).findViewById(INCREMENT_BUTTON_ID);
	}

	public View decrementChildrenButton() {
		return getView(CHILD_PICKER_VIEW_ID).findViewById(DECREMENT_BUTTON_ID);
	}

	public View incrementAdultsButton() {
		return getView(ADULT_PICKER_VIEW_ID).findViewById(INCREMENT_BUTTON_ID);
	}

	public View decrementAdultsButton() {
		return getView(ADULT_PICKER_VIEW_ID).findViewById(DECREMENT_BUTTON_ID);
	}

	public String selectChildAgePlural(int quantity) {
		return mRes.getQuantityString(SELECT_CHILD_AGE_PLURAL_ID, quantity);
	}

	public String childPickerStringPlural(int numberOfChildren) {
		return mRes.getQuantityString(NUMBER_OF_CHILDREN_PLURAL_ID, numberOfChildren, numberOfChildren);
	}

	public String adultPickerStringPlural(int numberOfAdults) {
		return mRes.getQuantityString(NUMBER_OF_ADULTS_PLURAL_ID, numberOfAdults, numberOfAdults);
	}

	public SimpleNumberPicker adultPicker() {
		return (SimpleNumberPicker) getView(ADULT_PICKER_VIEW_ID);
	}

	public SimpleNumberPicker childrenPicker() {
		return (SimpleNumberPicker) getView(CHILD_PICKER_VIEW_ID);
	}

	public TextView pickerLowerTextView(View picker) {
		return (TextView) picker.findViewById(LOWER_TEXT_VIEW_ID);
	}

	public TextView pickerCurrentTextView(View picker) {
		return (TextView) picker.findViewById(CURRENT_TEXT_VIEW_ID);
	}

	public TextView pickerHigherTextView(View picker) {
		return (TextView) picker.findViewById(HIGHER_TEXT_VIEW_ID);
	}

	// Object interaction

	public void clickOnSearchButton() {
		clickOnView(searchButton());
	}

	public void clickIncrementChildrenButton() {
		clickOnView(incrementChildrenButton());
	}

	public void clickDecrementChildrenButton() {
		clickOnView(decrementChildrenButton());
	}

	public void clickIncrementAdultsButton() {
		clickOnView(incrementAdultsButton());
	}

	public void clickDecrementAdultsButton() {
		clickOnView(decrementAdultsButton());
	}

}
