package com.expedia.bookings.test.stepdefs.phone.flights;


import org.hamcrest.Matcher;

import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewAssertion;
import android.support.test.espresso.matcher.ViewMatchers;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.widget.FilterSeekBar;

import junit.framework.Assert;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class FilterSteps {

	@Then("^Validate that default flight duration is set to maximum$")
	public void validateDefaultFlightDuration() throws Throwable {
		onView(withId(R.id.duration_seek_bar)).check(checkSeekBarMaxValue());

	}

	@Then("^Validate scrubber moves by an hour$")
	public void validateScrubberMovesAnHour() throws Throwable {
		onView(withId(R.id.duration_seek_bar)).check(checkSeekBarValueDecreaseBy(1));

	}

	@And("^I move the scrubber by an hour$")
	public void moveScrubberAnHour() throws Throwable {
		onView(withId(R.id.duration_seek_bar)).perform(decreaseProgressByOne());
	}

	public static ViewAssertion checkSeekBarValueDecreaseBy(final int seekValue) {
		return new ViewAssertion() {
			@Override
			public void check(View view, NoMatchingViewException noView) {
				FilterSeekBar seek = (FilterSeekBar) view;
				Assert.assertEquals(seek.getUpperLimit() - seek.getMaxValue(), seekValue);
			}
		};
	}

	public static ViewAssertion checkSeekBarMaxValue() {
		return new ViewAssertion() {
			@Override
			public void check(View view, NoMatchingViewException noView) {
				FilterSeekBar seek = (FilterSeekBar) view;
				Assert.assertEquals(seek.getMaxValue(), seek.getUpperLimit());
			}
		};
	}

	public static ViewAction decreaseProgressByOne() {
		return new ViewAction() {
			@Override
			public void perform(UiController uiController, View view) {
				FilterSeekBar seekBar = (FilterSeekBar) view;
				seekBar.setMaxValue(seekBar.getMaxValue() - 1);
			}

			@Override
			public String getDescription() {
				return "Set a progress on a FilterSeekBar";
			}

			@Override
			public Matcher<View> getConstraints() {
				return ViewMatchers.isAssignableFrom(FilterSeekBar.class);
			}
		};
	}
}
