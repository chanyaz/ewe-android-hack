package com.expedia.bookings.test.ui.phone.tests.cars;

import android.support.test.espresso.ViewInteraction;

import com.expedia.bookings.R;
import com.expedia.bookings.test.component.cars.CarViewModel;
import com.expedia.bookings.test.ui.espresso.RecyclerViewAssertions;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.ui.utils.PhoneTestCase;

import org.joda.time.DateTime;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class CarDetailsTests extends PhoneTestCase {

	private final static String CATEGORY = "Standard";

	public void testCarDetails() throws Throwable {
		screenshot("Launch");
		LaunchScreen.launchCars();

		screenshot("Car_Search");
		final DateTime startDateTime = DateTime.now().withTimeAtStartOfDay();
		final DateTime endDateTime = startDateTime.plusDays(3);
		CarViewModel.pickupLocation().perform(typeText("SFO"));
		CarViewModel.selectPickupLocation(getInstrumentation(), "San Francisco, CA");
		CarViewModel.selectDateButton().perform(click());
		CarViewModel.selectDates(startDateTime.toLocalDate(), endDateTime.toLocalDate());

		screenshot("Car_Search_Params_Entered");
		CarViewModel.searchButton().perform(click());

		screenshot("Car_Search_Results");
		CarViewModel.selectCarCategory(CATEGORY);

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

	private void assertTextAppearsInFirstOffer(String text) {
		ViewInteraction carOfferList = CarViewModel.carOfferList();
		carOfferList.check(RecyclerViewAssertions.assertionOnItemAtPosition(1, hasDescendant(withText(text))));
	}

	private void assertViewWithIdAppearsInFirstOffer(int id) {
		ViewInteraction carOfferList = CarViewModel.carOfferList();
		carOfferList.check(RecyclerViewAssertions.assertionOnItemAtPosition(1, hasDescendant(withId(id))));
	}
}
