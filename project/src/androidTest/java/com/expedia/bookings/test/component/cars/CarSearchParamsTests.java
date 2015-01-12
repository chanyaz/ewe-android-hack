package com.expedia.bookings.test.component.cars;

import org.joda.time.LocalDate;

import android.content.Context;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.matcher.ViewMatchers;
import android.test.ActivityInstrumentationTestCase2;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.PlaygroundActivity;
import com.expedia.bookings.test.ui.espresso.TabletViewActions;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.Visibility;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class CarSearchParamsTests extends ActivityInstrumentationTestCase2 {

	public CarSearchParamsTests() {
		super(PlaygroundActivity.class);
	}

	class CarSearchWidget {
		public ViewInteraction dropOffButton() {
			return onView(withId(R.id.dropoff_datetime));
		}

		public ViewInteraction pickUpButton() {
			return onView(withId(R.id.pickup_datetime));
		}

		public ViewInteraction calendarContainer() {
			return onView(withId(R.id.calendar_container));
		}

		public ViewInteraction calendar() {
			return onView(withId(R.id.calendar));
		}

		public ViewInteraction calendarActionButton() {
			return onView(withId(R.id.calendar_action_button));
		}

		public ViewInteraction changeTime() {
			return onView(withId(R.id.change_time));
		}

		public ViewInteraction timeContainer() {
			return onView(withId(R.id.time_container));
		}

		public ViewInteraction timePicker() {
			return onView(withId(R.id.time_picker));
		}

		public ViewInteraction timeConfirm() {
			return onView(withId(R.id.time_confirm_btn));
		}
	}

	CarSearchWidget mModel;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Context context = getInstrumentation().getTargetContext();
		mModel = new CarSearchWidget();
		setActivityIntent(PlaygroundActivity.createIntent(context, R.layout.widget_car_search_params));
		getActivity();
	}

	public void testSelectingPickupTime() throws Throwable {
		mModel.dropOffButton().check(matches(withEffectiveVisibility(ViewMatchers.Visibility.INVISIBLE)));
		mModel.calendar().check(matches(withEffectiveVisibility(ViewMatchers.Visibility.INVISIBLE)));
		mModel.pickUpButton().perform(click());
		mModel.calendar().check(matches(withEffectiveVisibility(Visibility.VISIBLE)));

		mModel.changeTime().check(matches(withEffectiveVisibility(Visibility.INVISIBLE)));
		mModel.calendar().perform(TabletViewActions.clickDates(LocalDate.now(), null));
		mModel.changeTime().check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
	}

	public void testCalendarActionText() throws Throwable {
		mModel.pickUpButton().perform(click());
		mModel.calendar().check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
		mModel.calendarActionButton().check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
		mModel.calendarActionButton().check(matches(withText(R.string.next)));

		mModel.dropOffButton().perform(click());
		mModel.calendarActionButton().check(matches(withText(R.string.search)));
	}

	public void testTimePicker() throws Throwable {
		mModel.pickUpButton().perform(click());
		mModel.calendar().perform(TabletViewActions.clickDates(LocalDate.now(), null));
		mModel.timeContainer().check(matches(withEffectiveVisibility(Visibility.INVISIBLE)));
		mModel.changeTime().perform(click());
		mModel.timeContainer().check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
		mModel.timeConfirm().perform(click());
		mModel.timeContainer().check(matches(withEffectiveVisibility(Visibility.INVISIBLE)));
		mModel.calendarContainer().check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
	}

	@Override
	protected void tearDown() throws Exception {
		Common.pressBackOutOfApp();
		super.tearDown();
	}
}
