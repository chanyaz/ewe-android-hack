package com.expedia.bookings.test.component.cars;

import org.joda.time.LocalDate;

import android.app.Activity;
import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.PlaygroundActivity;
import com.expedia.bookings.test.ui.espresso.TabletViewActions;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.Visibility;

public class CarSearchParamsTests extends ActivityInstrumentationTestCase2 {

	public CarSearchParamsTests() {
		super(PlaygroundActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Context context = getInstrumentation().getTargetContext();
		setActivityIntent(PlaygroundActivity.createIntent(context, R.layout.widget_car_search_params));
		getActivity();
	}

	public void testSelectingPickupTime() throws Throwable {
		onView(withId(R.id.calendar)).check(matches(withEffectiveVisibility(Visibility.INVISIBLE)));
		onView(withId(R.id.pickup_datetime)).perform(click());
		onView(withId(R.id.calendar)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)));

		onView(withId(R.id.change_time)).check(matches(withEffectiveVisibility(Visibility.INVISIBLE)));
		onView(withId(R.id.calendar)).perform(TabletViewActions.clickDates(LocalDate.now(), null));
		onView(withId(R.id.change_time)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		//Common.pressBackOutOfApp();
	}
}
