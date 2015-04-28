package com.expedia.bookings.test.ui.phone.tests.cars;

import org.joda.time.DateTime;

import com.expedia.bookings.R;
import com.expedia.bookings.test.component.cars.CarViewModel;
import com.expedia.bookings.test.ui.espresso.RecyclerViewAssertions;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;
import com.expedia.bookings.test.ui.utils.CarTestCase;
import com.expedia.bookings.test.ui.utils.EspressoUtils;

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
		CarViewModel.pickupLocation().perform(typeText("SFO"));
		CarViewModel.selectPickupLocation(getInstrumentation(), "San Francisco, CA");
		CarViewModel.selectDateButton().perform(click());
		CarViewModel.selectDates(startDateTime.toLocalDate(), endDateTime.toLocalDate());
		CarViewModel.searchButton().perform(click());
		ScreenActions.delay(1);
		int unifilterdCategoryNumber = EspressoUtils.getListCount(CarViewModel.carCategoryList());

		screenshot("Car_Search_Results");
		CarViewModel.searchFilter().perform(click());
		onView(allOf(withId(R.id.category), withText(CATEGORY))).perform(click());
		screenshot("Category_Filter_Select_Car_Type");
		CarViewModel.clickFilterDone();
		screenshot("Filtered_Categories");
		int filterdCategoryNumber = EspressoUtils.getListCount(CarViewModel.carCategoryList());

		//number of categories after applying car type filter are different from the original number of categories
		assertNotSame(filterdCategoryNumber, unifilterdCategoryNumber);


		for (int i = 0; i < filterdCategoryNumber; i++) {
			//assert correct category is shown after applying filter
			CarViewModel.carCategoryList().check(
				RecyclerViewAssertions.assertionOnItemAtPosition(i, hasDescendant(withText(CATEGORY))));
		}

		CarViewModel.selectCarCategory(CATEGORY);
		int unifilterdOffersNumber = EspressoUtils.getListCount(CarViewModel.carOfferList());

		screenshot("Car_Details");
		CarViewModel.searchFilter().perform(click());
		onView(allOf(withId(R.id.vendor), withText("NoCCRequired"),
			isDescendantOfA(withId(R.id.filter_suppliers_details)))).perform(click());
		screenshot("Details_Filter_Select_Car_Supplier");
		CarViewModel.clickFilterDone();
		int filterdOffersNumber = EspressoUtils.getListCount(CarViewModel.carOfferList());
		screenshot("Filtered_Details");

		//number of offers after applying car type filter are different from the original number of offers
		assertNotSame(filterdOffersNumber, unifilterdOffersNumber);

		for (int i = 0; i < filterdOffersNumber; i++) {
			//assert correct offer is shown after applying filter
			CarViewModel.carOfferList().check(
				RecyclerViewAssertions.assertionOnItemAtPosition(i, hasDescendant(withText("NoCCRequired"))));
		}

		//when user go back to car categories and filters applied on Details screen are reflected on Categories screen
		Common.pressBack();
		for (int i = 0; i < filterdCategoryNumber; i++) {
			//assert correct category is shown after applying filter
			CarViewModel.carCategoryList().check(
				RecyclerViewAssertions.assertionOnItemAtPosition(i, hasDescendant(withText(CATEGORY))));
		}

		CarViewModel.selectCarCategory(CATEGORY);
		for (int i = 0; i < filterdOffersNumber; i++) {
			//assert correct offer is shown after applying filter
			CarViewModel.carOfferList().check(
				RecyclerViewAssertions.assertionOnItemAtPosition(i, hasDescendant(withText("NoCCRequired"))));
		}

		//Test the filters are correctly updated when user comes back to the categories screen from details screen
		Common.pressBack();
		screenshot("Back_to_Categories");
		CarViewModel.searchFilter().perform(click());
		screenshot("Categories_Filters_updated");
		onView(allOf(withId(R.id.vendor_check_box), hasSibling(withText("NoCCRequired")),
			isDescendantOfA(withId(R.id.filter_suppliers_details)))).check(matches(isChecked()));
		onView(allOf(withId(R.id.category_check_box), hasSibling(withText(CATEGORY)))).check(matches(isChecked()));
	}
}
