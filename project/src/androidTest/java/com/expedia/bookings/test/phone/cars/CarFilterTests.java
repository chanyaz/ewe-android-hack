package com.expedia.bookings.test.phone.cars;

import org.joda.time.DateTime;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.RecyclerViewAssertions;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;
import com.expedia.bookings.test.espresso.CarTestCase;
import com.expedia.bookings.test.espresso.EspressoUtils;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

public class CarFilterTests extends CarTestCase {

	private final static String CATEGORY = "Standard";

	public void testCarsFilters() throws Throwable {
		final DateTime startDateTime = DateTime.now().withTimeAtStartOfDay();
		final DateTime endDateTime = startDateTime.plusDays(3);
		CarScreen.pickupLocation().perform(typeText("SFO"));
		CarScreen.selectPickupLocation("San Francisco, CA");
		CarScreen.selectDateButton().perform(click());
		CarScreen.selectDates(startDateTime.toLocalDate(), endDateTime.toLocalDate());
		CarScreen.searchButton().perform(click());
		ScreenActions.delay(1);
		int unifilterdCategoryNumber = EspressoUtils.getListCount(CarScreen.carCategoryList());

		screenshot("Car_Search_Results");
		CarScreen.searchFilter().perform(click());
		onView(allOf(withId(R.id.category), withText(CATEGORY))).perform(click());
		screenshot("Category_Filter_Select_Car_Type");
		CarScreen.clickFilterDone();
		screenshot("Filtered_Categories");
		int filterdCategoryNumber = EspressoUtils.getListCount(CarScreen.carCategoryList());

		//number of categories after applying car type filter are different from the original number of categories
		assertNotSame(filterdCategoryNumber, unifilterdCategoryNumber);


		for (int i = 0; i < filterdCategoryNumber; i++) {
			//assert correct category is shown after applying filter
			CarScreen.carCategoryList().check(
				RecyclerViewAssertions.assertionOnItemAtPosition(i, hasDescendant(withText(CATEGORY))));
		}

		CarScreen.selectCarCategory(CATEGORY);
		int unifilterdOffersNumber = EspressoUtils.getListCount(CarScreen.carOfferList());

		screenshot("Car_Details");
		CarScreen.searchFilter().perform(click());
		onView(allOf(withId(R.id.vendor), withText("NoCCRequired"),
			isDescendantOfA(withId(R.id.filter_suppliers)))).perform(click());
		screenshot("Details_Filter_Select_Car_Supplier");
		CarScreen.clickFilterDone();
		int filterdOffersNumber = EspressoUtils.getListCount(CarScreen.carOfferList());
		screenshot("Filtered_Details");

		//number of offers after applying car type filter are different from the original number of offers
		assertNotSame(filterdOffersNumber, unifilterdOffersNumber);

		for (int i = 0; i < filterdOffersNumber; i++) {
			//assert correct offer is shown after applying filter
			CarScreen.carOfferList().check(
				RecyclerViewAssertions.assertionOnItemAtPosition(i, hasDescendant(withText("NoCCRequired"))));
		}

		//when user go back to car categories and filters applied on Details screen are reflected on Categories screen
		Common.pressBack();
		for (int i = 0; i < filterdCategoryNumber; i++) {
			//assert correct category is shown after applying filter
			CarScreen.carCategoryList().check(
				RecyclerViewAssertions.assertionOnItemAtPosition(i, hasDescendant(withText(CATEGORY))));
		}

		CarScreen.selectCarCategory(CATEGORY);
		for (int i = 0; i < filterdOffersNumber; i++) {
			//assert correct offer is shown after applying filter
			CarScreen.carOfferList().check(
				RecyclerViewAssertions.assertionOnItemAtPosition(i, hasDescendant(withText("NoCCRequired"))));
		}

		//Test the filters are correctly updated when user comes back to the categories screen from details screen
		Common.pressBack();
		screenshot("Back_to_Categories");
		CarScreen.searchFilter().perform(click());
		screenshot("Categories_Filters_updated");
		onView(allOf(withId(R.id.vendor_check_box), hasSibling(withText("NoCCRequired")),
			isDescendantOfA(withId(R.id.filter_suppliers)))).check(matches(isChecked()));
		onView(allOf(withId(R.id.category_check_box), hasSibling(withText(CATEGORY)))).check(matches(isChecked()));
	}
}
