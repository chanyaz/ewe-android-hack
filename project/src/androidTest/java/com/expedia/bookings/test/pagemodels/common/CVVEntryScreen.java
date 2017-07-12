package com.expedia.bookings.test.pagemodels.common;

import java.util.HashMap;
import java.util.Map;

import android.support.test.espresso.ViewInteraction;

import com.expedia.bookings.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;

public class CVVEntryScreen {

	private static final Map<Integer, Integer> INTEGER_TO_RESOURCE = new HashMap<Integer, Integer>() {
		{
			put(1, R.id.one_button);
			put(2, R.id.two_button);
			put(3, R.id.three_button);
			put(4, R.id.four_button);
			put(5, R.id.five_button);
			put(6, R.id.six_button);
			put(7, R.id.seven_button);
			put(8, R.id.eight_button);
			put(9, R.id.nine_button);
			put(0, R.id.zero_button);
		}
	};

	public static void waitForCvvScreen() {
		bookButton().perform(waitForViewToDisplay());
	}

	public static ViewInteraction bookButton() {
		return onView(withId(R.id.book_button));
	}

	public static void clickBookButton() {
		bookButton().perform(click());
	}

	private static void clickNumberButton(int number) {
		int resourceID = INTEGER_TO_RESOURCE.get(number);
		onView(withId(resourceID)).perform(click());
	}

	public static void enterCVV(String cvv) {
		char c;
		int n;
		for (int i = 0; i < cvv.length(); i++) {
			c = cvv.charAt(i);
			n = Character.getNumericValue(c);
			clickNumberButton(n);
		}
	}

	public static ViewInteraction cvvSignatureText() {
		return onView(withId(R.id.signature_text_view));
	}
}
