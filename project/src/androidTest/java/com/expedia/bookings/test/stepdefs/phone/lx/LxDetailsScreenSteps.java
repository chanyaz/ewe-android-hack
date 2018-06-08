package com.expedia.bookings.test.stepdefs.phone.lx;

import android.support.test.espresso.contrib.RecyclerViewActions;

import com.expedia.bookings.R;
import com.expedia.bookings.test.pagemodels.lx.LXInfositeScreen;
import com.expedia.bookings.test.pagemodels.lx.LXScreen;
import com.expedia.bookings.test.stepdefs.phone.TestUtil;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.CustomMatchers.isEmpty;
import static com.expedia.bookings.test.espresso.CustomMatchers.withIndex;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.AllOf.allOf;


public class LxDetailsScreenSteps {

	private LXInfositeScreen.LxOffer lxOffer = null;

	@Then("^I select (\\d+) Activity$")
	public void clickOnActivity(final int postion)
		throws Throwable {
		int indexPosition = postion - 1;
		LXScreen.searchList().perform(RecyclerViewActions.actionOnItemAtPosition(indexPosition, click()));
	}

	@And("^I wait for activity detail to load$")
	public void waitForResultsToLoad() throws Throwable {
		LXInfositeScreen.waitForActivityDetailDisplayed();
	}

	@Then("^validate activity name \"(.*?)\" is same as user selected on ASR$")
	public void matchActivityName(String key) throws Throwable {
		validateActivityName(TestUtil.storeDataAtRuntime.get(key));
	}

	@Then("^validate date range \"(.*?)\" is same as user selected on ASR$")
	public void matchDateRange(String key) throws Throwable {
		validateDateRange(TestUtil.storeDataAtRuntime.get(key));
	}

	@Then("^Validate that highlights is displayed")
	public void validateHighlithtsVisibility() throws Throwable {
		onView(allOf(withId(R.id.section_title), withText(
			R.string.highlights_activity_details))).perform(scrollTo()).check(matches(
			isDisplayed()));
	}

	@Then("^Validate that description is not empty")
	public void validateDescriptionVisibility() throws Throwable {
		onView(allOf(withId(R.id.section_content), hasSibling(allOf(withId(R.id.section_title),
			withText(R.string.description_activity_details)))))
			.check(matches(not(isEmpty())));
	}

	@Then("^Validate that location is not empty")
	public void validateLocationVisibility() throws Throwable {
		onView(allOf(withId(R.id.section_content), hasSibling(allOf(withId(R.id.section_title),
			withText(R.string.location_activity_details)))))
			.check(matches(not(isEmpty())));
	}

	@Then("^Validate that cancellation policy is not empty")
	public void validateCancellationPolicyVisibility() throws Throwable {
		onView(allOf(withId(R.id.section_content), hasSibling(allOf(withId(R.id.section_title),
			withText(R.string.cancellation_policy)))))
			.check(matches(not(isEmpty())));
	}

	@Then("^I select activity (\\d+) offer$")
	public void selectOffer(final int position) throws Throwable {
		getLxOffer().selectActivityOffer(position);
	}

	@Then("^I trigger Book Now button$")
	public void clickBookNowButton() throws Throwable {
		getLxOffer().clickBookNowButton();
	}

	private LXInfositeScreen.LxOffer getLxOffer() {
		if (lxOffer == null) {
			lxOffer = new LXInfositeScreen.LxOffer();
		}
		return lxOffer;
	}

	private void validateActivityName(String name) {
		onView(allOf(withId(R.id.toolbar_detail_text), withText(name))).check(matches(isDisplayed()));
	}

	private void validateDateRange(String name) {
		onView(withIndex(withId(R.id.toolbar_subtitle_text), 0)).check(matches(withText(containsString(name))));
	}
}
