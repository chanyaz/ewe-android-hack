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

	private static int sOneButtonID = R.id.one_button;
	private static int sTwoButtonID = R.id.two_button;
	private static int sThreeButtonID = R.id.three_button;
	private static int sFourButtonID = R.id.four_button;
	private static int sFiveButtonID = R.id.five_button;
	private static int sSixButtonID = R.id.six_button;
	private static int sSevenButtonID = R.id.seven_button;
	private static int sEightButtonID = R.id.eight_button;
	private static int sNineButtonID = R.id.nine_button;
	private static int sZeroButtonID = R.id.zero_button;
	private static int sBookButtonID = R.id.book_button;

	private static int sBookingStringID = R.string.booking_loading;

	private static final Map<Integer, Integer> INTEGER_TO_RESOURCE = new HashMap<Integer, Integer>() {
		{
			put(1, sOneButtonID);
			put(2, sTwoButtonID);
			put(3, sThreeButtonID);
			put(4, sFourButtonID);
			put(5, sFiveButtonID);
			put(6, sSixButtonID);
			put(7, sSevenButtonID);
			put(8, sEightButtonID);
			put(9, sNineButtonID);
			put(0, sZeroButtonID);
		}
	};

	public CVVEntryScreen(Instrumentation instrumentation, Activity activity, Resources res,
			TestPreferences preferences) {
		super(instrumentation, activity, res, preferences);
	}

	//Object access

	public View bookButton() {
		return getView(sBookButtonID);
	}

	public String booking() {
		return getString(sBookingStringID);
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
}
