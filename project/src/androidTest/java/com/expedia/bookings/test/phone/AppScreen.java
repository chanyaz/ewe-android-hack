package com.expedia.bookings.test.phone;

import java.util.concurrent.TimeUnit;

import org.hamcrest.Matcher;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.ViewInteraction;
import android.view.View;

import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static com.expedia.bookings.test.espresso.ViewActions.waitFor;
import static com.expedia.bookings.test.phone.lx.LXScreen.recyclerView;
import static org.hamcrest.Matchers.allOf;

public class AppScreen {

	public static ViewInteraction recyclerItemView(Matcher<View> identifyingMatcher, int recyclerViewId) {
		Matcher<View> itemView = allOf(withParent(recyclerView(recyclerViewId)),
			withChild(identifyingMatcher));
		return Espresso.onView(itemView);
	}

	public static void waitForViewToDisplay(ViewInteraction viewInteraction) {
		viewInteraction.perform(waitFor(isDisplayed(), 10, TimeUnit.SECONDS));
	}
}
