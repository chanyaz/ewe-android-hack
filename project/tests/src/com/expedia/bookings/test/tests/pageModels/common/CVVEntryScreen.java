package com.expedia.bookings.test.tests.pageModels.common;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.res.Resources;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.test.utils.TestPreferences;

public class CVVEntryScreen extends ScreenActions {

	private static final int ONE_BUTTON_ID = R.id.one_button;
	private static final int TWO_BUTTON_ID = R.id.two_button;
	private static final int THREE_BUTTON_ID = R.id.three_button;
	private static final int FOUR_BUTTON_ID = R.id.four_button;
	private static final int FIVE_BUTTON_ID = R.id.five_button;
	private static final int SIX_BUTTON_ID = R.id.six_button;
	private static final int SEVEN_BUTTON_ID = R.id.seven_button;
	private static final int EIGHT_BUTTON_ID = R.id.eight_button;
	private static final int NINE_BUTTON_ID = R.id.nine_button;
	private static final int ZERO_BUTTON_ID = R.id.zero_button;
	private static final int BOOKING_BUTTON_ID = R.id.book_button;
	private static final int DELETE_BUTTON_ID = R.id.delete_button;
	private static final int INVALID_SECURITY_CODE_STRING_ID = R.string.invalid_security_code;

	private static final int sBookingStringID = R.string.booking_loading;

	private static final Map<Integer, Integer> INTEGER_TO_RESOURCE = new HashMap<Integer, Integer>() {
		{
			put(1, ONE_BUTTON_ID);
			put(2, TWO_BUTTON_ID);
			put(3, THREE_BUTTON_ID);
			put(4, FOUR_BUTTON_ID);
			put(5, FIVE_BUTTON_ID);
			put(6, SIX_BUTTON_ID);
			put(7, SEVEN_BUTTON_ID);
			put(8, EIGHT_BUTTON_ID);
			put(9, NINE_BUTTON_ID);
			put(0, ZERO_BUTTON_ID);
		}
	};

	public CVVEntryScreen(Instrumentation instrumentation, Activity activity, Resources res,
			TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

	//Object access

	public View bookButton() {
		return getView(BOOKING_BUTTON_ID);
	}

	public View deleteButton() {
		return getView(DELETE_BUTTON_ID);
	}

	public String booking() {
		return getString(sBookingStringID);
	}

	public String invalidSecurityCode() {
		return getString(INVALID_SECURITY_CODE_STRING_ID);
	}

	// Object interaction

	public void clickNumberButton(int number) {
		int resourceID = INTEGER_TO_RESOURCE.get(number);
		clickOnView(getView(resourceID));
	}

	public void parseAndEnterCVV(String CVV) {
		char c;
		int n;
		for (int i = 0; i < CVV.length(); i++) {
			c = CVV.charAt(i);
			n = Character.getNumericValue(c);
			clickNumberButton(n);
		}
	}

	public void clickBookButton() {
		clickOnView(bookButton());
	}

	public void clickDeleteButton() {
		clickOnView(deleteButton());
	}
}
