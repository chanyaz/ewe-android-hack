package com.expedia.bookings.test.phone.cars;

import android.support.test.espresso.ViewInteraction;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.RecyclerViewAssertions;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.espresso.PhoneTestCase;

import org.joda.time.DateTime;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.core.IsNot.not;

public class CarDetailsTests extends PhoneTestCase {

	private final static String CATEGORY = "Standard";

	public void testCarDetails() throws Throwable {
		screenshot("Launch");
		LaunchScreen.launchCars();

		screenshot("Car_Search");
		enterSearchParams("SFO", "San Francisco, CA");

		screenshot("Car_Search_Params_Entered");
		CarScreen.searchButton().perform(click());

		screenshot("Car_Search_Results");
		CarScreen.selectCarCategory(CATEGORY);

		assertTextAppearsInFirstOffer("4 Passengers");
		assertTextAppearsInFirstOffer("2 Bags");
		assertTextAppearsInFirstOffer("4 Doors");
		assertTextAppearsInFirstOffer("Daily $32");
		assertTextAppearsInFirstOffer("Total $32");
		assertViewWithIdAppearsInFirstOffer(R.id.address);
		assertViewWithIdAppearsInFirstOffer(R.id.car_details);
		assertViewWithIdAppearsInFirstOffer(R.id.vendor);
		assertViewWithIdAppearsInFirstOffer(R.id.transmission);
		assertViewWithIdAppearsInFirstOffer(R.id.reserve_now);
		assertViewWithIdAppearsInFirstOffer(R.id.map_view);
		assertViewWithIdAppearsInFirstOffer(R.id.map_text);
	}

	public void testCarDetailsNoPassengersDoorsBags() throws Throwable {
		screenshot("Launch");
		LaunchScreen.launchCars();

		screenshot("Car_Search");
		enterSearchParams("SFO", "San Francisco, CA");

		screenshot("Car_Search_Params_Entered");
		CarScreen.searchButton().perform(click());

		screenshot("Car_Search_Results");
		CarScreen.selectCarCategory(CATEGORY);

		assertViewNotDisplayedInFirstOffer(R.id.passengers);
		assertViewNotDisplayedInFirstOffer(R.id.bags);
		assertViewNotDisplayedInFirstOffer(R.id.doors);
		assertTextAppearsInFirstOffer("Daily $32");
		assertTextAppearsInFirstOffer("Total $32");
		assertViewWithIdAppearsInFirstOffer(R.id.address);
		assertViewWithIdAppearsInFirstOffer(R.id.car_details);
		assertViewWithIdAppearsInFirstOffer(R.id.vendor);
		assertViewWithIdAppearsInFirstOffer(R.id.transmission);
		assertViewWithIdAppearsInFirstOffer(R.id.reserve_now);
		assertViewWithIdAppearsInFirstOffer(R.id.map_view);
		assertViewWithIdAppearsInFirstOffer(R.id.map_text);
	}

	public void testCarDetailsCountInRange() throws Throwable {
		screenshot("Launch");
		LaunchScreen.launchCars();

		screenshot("Car_Search");
		enterSearchParams("SFO", "San Francisco, CA");

		screenshot("Car_Search_Params_Entered");
		CarScreen.searchButton().perform(click());

		screenshot("Car_Search_Results");
		CarScreen.clickFilterButton();

		CarScreen.selectCategoryForFilter("Economy");
		CarScreen.clickFilterDone();

		assertTextAppearsInFirstCategory("4-5");
		assertTextAppearsInFirstCategory("2-5");
		assertTextAppearsInFirstCategory("4-6");
		assertTextAppearsInFirstCategory("Daily $32");
		assertTextAppearsInFirstCategory("Total $32");
	}


	private void enterSearchParams(String searchQuery, String location) throws Throwable {
		final DateTime startDateTime = DateTime.now().withTimeAtStartOfDay();
		final DateTime endDateTime = startDateTime.plusDays(3);
		CarScreen.pickupLocation().perform(typeText(searchQuery));
		CarScreen.selectPickupLocation(location);
		CarScreen.selectDateButton().perform(click());
		CarScreen.selectDates(startDateTime.toLocalDate(), endDateTime.toLocalDate());
	}

	private void assertViewNotDisplayedInFirstOffer(int id) {
		ViewInteraction carOfferList = CarScreen.carOfferList();
		carOfferList.check(
			RecyclerViewAssertions.assertionOnItemAtPosition(1, hasDescendant(allOf(withId(id), not(isDisplayed())))));
	}

	private void assertTextAppearsInFirstOffer(String text) {
		ViewInteraction carOfferList = CarScreen.carOfferList();
		carOfferList.check(RecyclerViewAssertions.assertionOnItemAtPosition(1, hasDescendant(withText(text))));
	}
	private void assertTextAppearsInFirstCategory(String text) {
		ViewInteraction carCategoryList = CarScreen.carCategoryList();
		carCategoryList.check(RecyclerViewAssertions.assertionOnItemAtPosition(0, hasDescendant(withText(text))));
	}

	private void assertViewWithIdAppearsInFirstOffer(int id) {
		ViewInteraction carOfferList = CarScreen.carOfferList();
		carOfferList.check(RecyclerViewAssertions.assertionOnItemAtPosition(1, hasDescendant(withId(id))));
	}
}
