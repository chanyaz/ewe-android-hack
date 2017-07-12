package com.expedia.bookings.test.phone.cars;

import org.junit.Test;

import android.support.test.espresso.ViewInteraction;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.CarTestCase;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.RecyclerViewAssertions;
import com.expedia.bookings.test.pagemodels.cars.CarScreen;
import com.expedia.bookings.test.pagemodels.common.SearchScreen;

import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.core.IsNot.not;

public class CarDetailsTest extends CarTestCase {

	private final static String CATEGORY = "Standard";

	@Test
	public void testCarDetails() throws Throwable {
		SearchScreen.doGenericCarSearch();

		CarScreen.selectCarCategory(CATEGORY);

		assertTextAppearsInFirstOffer("4 Passengers");
		assertTextAppearsInFirstOffer("2 Bags");
		assertTextAppearsInFirstOffer("4 Doors");
		assertTextAppearsInFirstOffer("Daily $32");
		assertTextAppearsInFirstOffer("Total $32");
		assertViewWithIdAppearsInFirstOffer(R.id.address_line_one);
		assertViewWithIdAppearsInFirstOffer(R.id.address_line_two);
		assertViewWithIdAppearsInFirstOffer(R.id.car_details);
		assertViewWithIdAppearsInFirstOffer(R.id.vendor);
		assertViewWithIdAppearsInFirstOffer(R.id.transmission);
		assertViewWithIdAppearsInFirstOffer(R.id.reserve_now);
		assertViewWithIdAppearsInFirstOffer(R.id.map_view);
		assertViewWithIdAppearsInFirstOffer(R.id.map_text);
	}

	@Test
	public void testCarDetailsNoPassengersDoorsBags() throws Throwable {
		SearchScreen.doGenericCarSearch();

		CarScreen.selectCarCategory(CATEGORY);

		assertViewNotDisplayedInFirstOffer(R.id.passengers);
		assertViewNotDisplayedInFirstOffer(R.id.bags);
		assertViewNotDisplayedInFirstOffer(R.id.doors);
		assertTextAppearsInFirstOffer("Daily $32");
		assertTextAppearsInFirstOffer("Total $32");
		assertViewWithIdAppearsInFirstOffer(R.id.address_line_one);
		assertViewWithIdAppearsInFirstOffer(R.id.address_line_two);
		assertViewWithIdAppearsInFirstOffer(R.id.car_details);
		assertViewWithIdAppearsInFirstOffer(R.id.vendor);
		assertViewWithIdAppearsInFirstOffer(R.id.transmission);
		assertViewWithIdAppearsInFirstOffer(R.id.reserve_now);
		assertViewWithIdAppearsInFirstOffer(R.id.map_view);
		assertViewWithIdAppearsInFirstOffer(R.id.map_text);
	}

	@Test
	public void testCarDetailsCountInRange() throws Throwable {
		SearchScreen.doGenericCarSearch();

		Common.delay(2);
		CarScreen.searchCategoryFilter().perform(waitForViewToDisplay());
		CarScreen.clickCategoryFilterButton();

		CarScreen.selectCategoryForFilter("Economy");
		CarScreen.clickFilterDone();

		assertTextAppearsInFirstCategory("4-5");
		assertTextAppearsInFirstCategory("2-5");
		assertTextAppearsInFirstCategory("4-6");
		assertTextAppearsInFirstCategory("Daily $32");
		assertTextAppearsInFirstCategory("Total $32");
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
