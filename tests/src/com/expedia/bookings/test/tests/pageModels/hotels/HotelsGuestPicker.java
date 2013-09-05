package com.expedia.bookings.test.tests.pageModels.hotels;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.test.tests.pageModels.common.ScreenActions;
import com.expedia.bookings.test.utils.TestPreferences;

public class HotelsGuestPicker extends ScreenActions {

	private static int sSearchButtonID = R.id.search_button;

	private static int sIncrementButtonID = R.id.increment;
	private static int sDecrementButtonID = R.id.decrement;

	private static int sAdultsPickerViewID = R.id.adults_number_picker;
	private static int sChildrenPickerViewID = R.id.children_number_picker;

	public HotelsGuestPicker(Instrumentation instrumentation, Activity activity, Resources res,
			TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

	// Object access

	public View searchButton() {
		return getView(sSearchButtonID);
	}

	public View incrementChildrenButton() {
		return getView(sChildrenPickerViewID).findViewById(sIncrementButtonID);
	}

	public View decrementChildrenButton() {
		return getView(sChildrenPickerViewID).findViewById(sDecrementButtonID);
	}

	public View incrementAdultsButton() {
		return getView(sAdultsPickerViewID).findViewById(sIncrementButtonID);
	}

	public View decrementAdultsButton() {
		return getView(sAdultsPickerViewID).findViewById(sDecrementButtonID);
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
