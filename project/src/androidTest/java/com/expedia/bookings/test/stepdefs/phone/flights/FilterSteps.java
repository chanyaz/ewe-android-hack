package com.expedia.bookings.test.stepdefs.phone.flights;



import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewAssertion;
import android.support.test.espresso.matcher.ViewMatchers;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.stepdefs.phone.TestUtil;
import com.expedia.bookings.widget.FilterSeekBar;
import com.expedia.bookings.widget.TextView;

import junit.framework.Assert;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.AllOf.allOf;

public class FilterSteps {

	private static String filterResult;

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

	@And("^I select \"(.*?)\" checkbox and isOutBound : (true|false)$")
	public void selectSortAndFilterCheckbox(String optionText, boolean outBound) throws Throwable {
		onView(allOf(withId(R.id.check_box), hasSibling(withText(optionText)), (outBound ? isDescendantOfA(withId(R.id.widget_flight_outbound))
			: isDescendantOfA(withId(R.id.widget_flight_inbound)))))
			.perform(scrollTo(), ViewActions.waitForViewToDisplay(), click());
	}

	@And("^I click on sort and filter screen done button$")
	public void doneBtnClick() throws Throwable {
		onView(allOf(withId(R.id.search_btn), withEffectiveVisibility(VISIBLE))).perform(click());
	}

	@Then("^Validate that after filter applied the number of result changes$")
	public void validateResultsAfterFilter() throws Throwable {
		onView(withId(R.id.dynamic_feedback_counter)).check(getDynamicResultCountOnSortAndFilterScreen());
		onView(allOf(withId(R.id.list_view), isDescendantOfA(withId(R.id.widget_flight_inbound)))).check(TestUtil.assertFlightsResultsListSizeEquals(Integer.parseInt(filterResult)));

	}

	@Then("^Validate Number of Results in Dynamic Feedback TextView changes$")
	public void validateResultsWhileApplyingFilter() throws Throwable {
		onView(withId(R.id.dynamic_feedback_counter)).check(assertResultsCountOnFilterChangesDynamically());

	}

	@Then("^Validate all results are \"(.*?)\"$")
	public void checkResultsForNumberOfStops(String stopsValue) throws Throwable {
		onView(Matchers.allOf(withId(R.id.list_view),(isDescendantOfA(withId(R.id.widget_flight_outbound)) ))).check(
			TestUtil.assertFlightResultsListFor( hasDescendant(
			Matchers.allOf(withId(R.id.flight_duration_text_view), withText(containsString(stopsValue))))));
	}

	@And("^Validate that the Dynamic Feedback TextView is Visible$")
	public void validateDynamicFeedbackTextView() throws Throwable {
		onView(withId(R.id.dynamic_feedback_counter)).check(matches(isDisplayed()));
	}

	@And("^I scroll to Airline Section$")
	public void scrollToAirlineSection() throws Throwable {
		onView(withId(R.id.airlines_label)).perform(scrollTo());
	}

	public static ViewAssertion getDynamicResultCountOnSortAndFilterScreen() {
		return new ViewAssertion() {
			@Override
			public void check(View view, NoMatchingViewException noView) {
				TextView dynamicFeedbackTextView = (TextView) view;
				String text = dynamicFeedbackTextView.getText().toString();
				filterResult = String.valueOf(Integer.parseInt(text.split(" ")[0]));
			}
		};
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


	public static ViewAssertion assertResultsCountOnFilterChangesDynamically() {
		return new ViewAssertion() {
			@Override
			public void check(View view, NoMatchingViewException noView) {
				TextView dynamicFeedbackTextView = (TextView) view;
				String text = dynamicFeedbackTextView.getText().toString();
				String textValue = text.split(" ")[0];
				Assert.assertTrue(textValue != filterResult);
				filterResult = textValue;
			}
		};
	}



}
