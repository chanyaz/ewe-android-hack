package com.expedia.bookings.test.component.cars;

import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.RecyclerViewActions;

import com.expedia.bookings.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class CarDetailsModel {

	public static ViewInteraction list() {
		return onView(withId(R.id.offer_list));
	}

	public static void selectOffer(int position) {
		list().perform(RecyclerViewActions.actionOnItemAtPosition(position, click()));
	}

}
