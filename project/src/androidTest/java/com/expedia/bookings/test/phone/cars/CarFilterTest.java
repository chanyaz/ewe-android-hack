package com.expedia.bookings.test.phone.cars;

import org.junit.Test;

import android.support.test.espresso.contrib.RecyclerViewActions;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.CarTestCase;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.RecyclerViewAssertions;
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.core.AnyOf.anyOf;

public class CarFilterTest extends CarTestCase {

	private final static String CATEGORY = "Standard";

	@Test
	public void testCarsFilters() throws Throwable {
		SearchScreen.doGenericCarSearch();
		Common.delay(1);
		int unifilterdCategoryNumber = EspressoUtils.getListCount(CarScreen.carCategoryList());

		screenshot("Car_Search_Results");
		CarScreen.searchCategoryFilter().perform(click());
		CarScreen.selectCategoryFilter(CATEGORY);
		screenshot("Category_Filter_Select_Car_Type");
		CarScreen.clickFilterDone();
		screenshot("Filtered_Categories");
		int filterdCategoryNumber = EspressoUtils.getListCount(CarScreen.carCategoryList());

		//number of categories after applying car type filter are different from the original number of categories
		assertNotSame(filterdCategoryNumber, unifilterdCategoryNumber);
		assertCorrectCategoryShownAfterApplyingFilters(filterdCategoryNumber, CATEGORY);

		CarScreen.selectCarCategory(CATEGORY);
		int unifilterdOffersNumber = EspressoUtils.getListCount(CarScreen.carOfferList());

		screenshot("Car_Details");
		CarScreen.searchFilter().perform(click());
		CarScreen.selectSupplierFilter("NoCCRequired");
		screenshot("Details_Filter_Select_Car_Supplier");
		CarScreen.clickFilterDone();
		int filterdOffersNumber = EspressoUtils.getListCount(CarScreen.carOfferList());
		screenshot("Filtered_Details");

		//number of offers after applying car type filter are different from the original number of offers
		assertNotSame(filterdOffersNumber, unifilterdOffersNumber);
		assertCorrectOfferShownAfterApplyingFilters(filterdOffersNumber, "NoCCRequired");

		//when user go back to car categories and filters applied on Details screen are reflected on Categories screen
		Common.pressBack();
		assertCorrectCategoryShownAfterApplyingFilters(filterdCategoryNumber, CATEGORY);
		Common.delay(1);

		CarScreen.selectCarCategory(CATEGORY);
		assertCorrectOfferShownAfterApplyingFilters(filterdOffersNumber, "NoCCRequired");

		//Test the filters are correctly updated when user comes back to the categories screen from details screen
		Common.pressBack();
		screenshot("Back_to_Categories");
		CarScreen.searchCategoryFilter().perform(click());
		screenshot("Categories_Filters_updated");
		onView(allOf(withId(R.id.vendor_check_box), hasSibling(withText("NoCCRequired")),
			isDescendantOfA(withId(R.id.filter_suppliers)))).check(matches(isChecked()));
		onView(allOf(withId(R.id.category_check_box), hasSibling(withText(CATEGORY)))).check(matches(isChecked()));
	}

	@Test
	public void testFilterByMultipleCategoriesOnSRP() throws Throwable {
		SearchScreen.doGenericCarSearch();

		Common.delay(1);

		int unifilterdCategoryNumber = EspressoUtils.getListCount(CarScreen.carCategoryList());
		screenshot("Car_Search_Results");

		CarScreen.searchCategoryFilter().perform(click());
		CarScreen.selectCategoryFilter(CATEGORY);
		CarScreen.selectCategoryFilter("Economy");
		screenshot("Category_Filter_Select_Car_Type");
		CarScreen.clickFilterDone();
		screenshot("Filtered_Categories");

		int filterdCategoryNumber = EspressoUtils.getListCount(CarScreen.carCategoryList());

		assertNotSame(filterdCategoryNumber, unifilterdCategoryNumber);
		assertCorrectCategoryShownAfterApplyingFilters(filterdCategoryNumber, CATEGORY, "Economy");

	}

	@Test
	public void testFilterBySupplierOnSRP() throws Throwable {
		SearchScreen.doGenericCarSearch();

		Common.delay(1);

		CarScreen.searchCategoryFilter().perform(click());
		CarScreen.selectSupplierFilter("NoCCRequired");
		CarScreen.clickFilterDone();
		screenshot("Filtered_categories");

		int filteredCategoryNumber = EspressoUtils.getListCount(CarScreen.carCategoryList());
		assertSupplierInsideFilteredCategories(filteredCategoryNumber, "NoCCRequired");
	}

	@Test
	public void testFilterByAirConditioningOnSRP() throws Throwable {
		SearchScreen.doGenericCarSearch();

		int unifilterdCategoryNumber = EspressoUtils.getListCount(CarScreen.carCategoryList());
		screenshot("Car_Search_Results");
		int unfilteredOfferNumber = countOffers(unifilterdCategoryNumber);

		Common.delay(1);

		CarScreen.searchCategoryFilter().perform(click());
		onView(withId(R.id.ac_filter_checkbox)).perform(click());

		CarScreen.clickFilterDone();
		screenshot("Filtered_Categories");
		int filterdCategoryNumber = EspressoUtils.getListCount(CarScreen.carCategoryList());
		int filteredOfferNumber = countOffers(filterdCategoryNumber);
		assertNotSame(unfilteredOfferNumber, filteredOfferNumber);

	}

	@Test
	public void testFilterByUnlimitedMileageOnSRP() throws Throwable {
		SearchScreen.doGenericCarSearch();

		int unifilterdCategoryNumber = EspressoUtils.getListCount(CarScreen.carCategoryList());
		screenshot("Car_Search_Results");
		int unfilteredOfferNumber = countOffers(unifilterdCategoryNumber);

		Common.delay(1);

		CarScreen.searchCategoryFilter().perform(click());
		onView(withId(R.id.unlimited_mileage_filter_checkbox)).perform(click());

		CarScreen.clickFilterDone();
		screenshot("Filtered_Categories");
		int filterdCategoryNumber = EspressoUtils.getListCount(CarScreen.carCategoryList());
		int filteredOfferNumber = countOffers(filterdCategoryNumber);
		assertNotSame(filteredOfferNumber, unfilteredOfferNumber);
	}

	@Test
	public void testFilterByTransmissionTypeOnSRP() throws Throwable {
		SearchScreen.doGenericCarSearch();

		int unifilterdCategoryNumber = EspressoUtils.getListCount(CarScreen.carCategoryList());
		screenshot("Car_Search_Results");
		int unfilteredOfferNumber = countOffers(unifilterdCategoryNumber);

		Common.delay(1);

		CarScreen.searchCategoryFilter().perform(click());
		onView(withId(R.id.transmission_filter_manual)).perform(click());

		CarScreen.clickFilterDone();
		screenshot("Filtered_Categories");
		int filterdCategoryNumber = EspressoUtils.getListCount(CarScreen.carCategoryList());
		assertNotSame(filterdCategoryNumber, unifilterdCategoryNumber);

		CarScreen.searchCategoryFilter().perform(click());
		onView(withId(R.id.transmission_filter_automatic)).perform(click());

		CarScreen.clickFilterDone();
		screenshot("Filtered_Categories");
		int filteredByAutoTransmissionCategories = EspressoUtils.getListCount(CarScreen.carCategoryList());
		int filteredOfferNumber = countOffers(filteredByAutoTransmissionCategories);
		assertNotSame(filteredOfferNumber, unfilteredOfferNumber);
	}

	private void assertCorrectOfferShownAfterApplyingFilters(int numberOfResults, String offerName) {
		for (int i = 0; i < numberOfResults; i++) {
			CarScreen.carOfferList().check(
				RecyclerViewAssertions.assertionOnItemAtPosition(i, hasDescendant(withText(offerName))));
		}

	}

	private void assertCorrectCategoryShownAfterApplyingFilters(int numberOfResults, String categoryName) {
		for (int i = 0; i < numberOfResults; i++) {
			CarScreen.carCategoryList().check(
				RecyclerViewAssertions
					.assertionOnItemAtPosition(i, hasDescendant(withText(containsString(categoryName)))));
		}
	}

	private void assertCorrectCategoryShownAfterApplyingFilters(int numberOfResults, String firstCategoryName,
		String secondCategoryName) {
		for (int i = 0; i < numberOfResults; i++) {
			CarScreen.carCategoryList().check(
				RecyclerViewAssertions.assertionOnItemAtPosition(i, hasDescendant(
					anyOf(withText(containsString(firstCategoryName)), withText(containsString(secondCategoryName))))));
		}
	}

	private void assertSupplierInsideFilteredCategories(int numberOfCategories, String supplierName) {
		for (int i = 0; i < numberOfCategories; i++) {
			CarScreen.carCategoryList().perform(RecyclerViewActions.actionOnItemAtPosition(i, click()));
			Common.delay(1);

			int numberOfOffers = EspressoUtils.getListCount(CarScreen.carOfferList());

			for (int j = 0; j < numberOfOffers; j++) {
				CarScreen.carOfferList()
					.check(RecyclerViewAssertions.assertionOnItemAtPosition(j, hasDescendant(withText(supplierName))));
			}
		}
	}

	private int countOffers(int numberOfCategories) {
		int numberOfOffers = 0;
		for (int i = 0; i < numberOfCategories; i++) {
			Common.delay(1);
			CarScreen.carCategoryList().perform(RecyclerViewActions.actionOnItemAtPosition(i, click()));
			Common.delay(1);

			numberOfOffers = numberOfOffers + EspressoUtils.getListCount(CarScreen.carOfferList());
			Common.pressBack();

		}
		return numberOfOffers;

	}
}
