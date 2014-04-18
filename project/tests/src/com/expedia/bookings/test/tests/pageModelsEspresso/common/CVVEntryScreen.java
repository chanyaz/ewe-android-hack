package com.expedia.bookings.test.tests.pageModelsEspresso.common;

import java.util.HashMap;
import java.util.Map;

import com.expedia.bookings.R;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;

import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;

/**
 * Created by dmadan on 4/7/14.
 */
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

//Object access

	public static ViewInteraction bookButton() {
		return onView(withId(BOOKING_BUTTON_ID));
	}

	public static ViewInteraction deleteButton() {
		return onView(withId(DELETE_BUTTON_ID));
	}

	public static ViewInteraction booking() {
		return onView(withText(sBookingStringID));
	}

	public static ViewInteraction creditCardNumberInvalid() {
		return onView(withText(R.string.error_invalid_card_number));
	}

	public static ViewInteraction okString() {
		return onView(withText(R.string.ok));
	}

	// Object interaction

	public static void clickNumberButton(int number) {
		int resourceID = INTEGER_TO_RESOURCE.get(number);
		onView(withId(resourceID)).perform(click());
	}

	public static void parseAndEnterCVV(String CVV) {
		char c;
		int n;
		for (int i = 0; i < CVV.length(); i++) {
			c = CVV.charAt(i);
			n = Character.getNumericValue(c);
			clickNumberButton(n);
		}
	}

	public static void clickBookButton() {
		bookButton().perform(click());
	}

	public static void clickDeleteButton() {
		deleteButton().perform(click());
	}

	public static void clickOkButton() {
		okString().perform(click());
	}
}
