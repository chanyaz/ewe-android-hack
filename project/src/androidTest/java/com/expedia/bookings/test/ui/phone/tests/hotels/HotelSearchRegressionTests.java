package com.expedia.bookings.test.ui.phone.tests.hotels;

import java.util.ArrayList;

import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.ui.phone.pagemodels.common.SettingsScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsGuestPicker;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsSearchScreen;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PhoneTestCase;

import android.app.Activity;
import android.support.test.espresso.Espresso;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

/**
 * Created by dmadan on 5/13/14.
 */
public class HotelSearchRegressionTests extends PhoneTestCase {

	/*
	* #190 eb_tp test plan
	 */
	private static final String TAG = HotelSearchRegressionTests.class.getName();

	public void selectCalendarDates(int start, int end) {
		LocalDate startDate = LocalDate.now().plusDays(start);
		LocalDate endDate = LocalDate.now().plusDays(end);
		HotelsSearchScreen.clickOnCalendarButton();
		HotelsSearchScreen.clickDate(startDate, endDate);
	}

	public void testSearchByHotelName() throws Exception {
		ArrayList<String> hotelList = new ArrayList<String>();
		hotelList.add("Hotel Nikko San Francisco");
		hotelList.add("Hilton Minneapolis");
		hotelList.add("Swissotel Berlin");
		LaunchScreen.launchHotels();
		selectCalendarDates(5, 10);
		for (String hotel : hotelList) {
			HotelsSearchScreen.clickSearchEditText();
			HotelsSearchScreen.clickToClearSearchEditText();
			HotelsSearchScreen.enterSearchText(hotel);
			HotelsSearchScreen.clickSuggestionWithName(getActivity(), hotel);
			onView(withId(R.id.title)).check(matches(withText(hotel)));
			Espresso.pressBack();
		}
		Espresso.pressBack();
	}

	public void testSearchByCity() throws Exception {
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText("Ann Arbor");
		selectCalendarDates(5, 10);
		HotelsSearchScreen.clickSearchButton();
		HotelsSearchScreen.hotelListItem().atPosition(1).onChildView(withId(R.id.proximity_text_view)).check(matches(withText("Ann Arbor")));
		Espresso.pressBack();
	}

	public void testSearchByCommonCityName() throws Exception {
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText("Clinton");
		selectCalendarDates(5, 10);
		HotelsSearchScreen.clickSearchButton();
		EspressoUtils.assertViewWithTextIsDisplayed(mRes.getString(R.string.ChooseLocation));
		Espresso.pressBack();
	}

		/*
		 * Search edit text and geocoding tests
		 */

	public void testSearchMultipleGeocodeResponses() throws Exception {
		String ambiguousSearchString = "123";
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText(ambiguousSearchString);
		HotelsSearchScreen.clickOnGuestsButton();
		HotelsSearchScreen.guestPicker().clickOnSearchButton();
		EspressoUtils.assertViewWithTextIsDisplayed(mRes.getString(R.string.ChooseLocation));
		ScreenActions.enterLog(TAG, "Did you mean' dialog appeared after search for: " + ambiguousSearchString);
		onView(withText("Cancel")).perform(click());
		ScreenActions.enterLog(TAG, "clicked cancel");
		Espresso.pressBack();
	}

	public void testGeocodeResolutionOfCityAbbreviations() throws Exception {
		String cityAbbreviation = "NYC";
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText(cityAbbreviation);
		HotelsSearchScreen.clickOnGuestsButton();
		HotelsSearchScreen.guestPicker().clickOnSearchButton();
		ScreenActions.enterLog(TAG, "Testing geocoding with abbrevation string: " + cityAbbreviation);
		HotelsSearchScreen.searchEditText().check(matches(withText("New York, NY")));
		Espresso.pressBack();

		cityAbbreviation = "SF";
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText(cityAbbreviation);
		HotelsSearchScreen.clickOnGuestsButton();
		HotelsSearchScreen.guestPicker().clickOnSearchButton();
		ScreenActions.enterLog(TAG, "Testing geocoding with abbrevation string: " + cityAbbreviation);
		HotelsSearchScreen.searchEditText().check(matches(withText("San Francisco, CA")));
		Espresso.pressBack();
	}

	public void testGeocodeFailureMessage() throws Exception {
		String nonsense = "hioeawh3aw09y4wajioyanwhalwqqqkeviniscool";
		ScreenActions.enterLog(TAG, "Testing geocoding with nonsense string: " + nonsense);
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText(nonsense);
		HotelsSearchScreen.clickOnGuestsButton();
		HotelsSearchScreen.guestPicker().clickOnSearchButton();
		EspressoUtils.assertViewWithTextIsDisplayed(mRes.getString(R.string.geolocation_failed));
		ScreenActions.enterLog(TAG, "Geocoding error message text was displayed");
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText("Chargoggagoggmanchauggagoggchaubunagungamaugg");
		HotelsSearchScreen.clickOnGuestsButton();
		HotelsSearchScreen.guestPicker().clickOnSearchButton();
		HotelsSearchScreen.clickListItem(1);
		ScreenActions.enterLog(TAG, "Search for the longest place name in the United States works");
	}

	public void testPointOfInterestGeocoding() throws Exception {
		String pointOfInterest = "Statue of Liberty";
		ScreenActions.enterLog(TAG, "Testing geocoding with POI string: " + pointOfInterest);
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText(pointOfInterest);
		HotelsSearchScreen.clickOnGuestsButton();
		HotelsSearchScreen.guestPicker().clickOnSearchButton();
		EspressoUtils.assertContains(HotelsSearchScreen.searchEditText(), "Statue of Liberty");
		Espresso.pressBack();
	}

	public void testPostalCodeGeocoding() throws Exception {
		String postalCode = "94104";
		ScreenActions.enterLog(TAG, "Testing geocoding with postal code string: " + postalCode);
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText(postalCode);
		HotelsSearchScreen.clickOnGuestsButton();
		HotelsSearchScreen.guestPicker().clickOnSearchButton();
		HotelsSearchScreen.searchEditText().check(matches(withText("San Francisco, CA 94104")));
		Espresso.pressBack();
	}

	public void testCachingOfPreviousSearches() throws Exception {
		Activity activity = getActivity();
		String initialSearch = "Belleville, MI";
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText(initialSearch);
		HotelsSearchScreen.clickOnGuestsButton();
		HotelsSearchScreen.guestPicker().clickOnSearchButton();
		String resolvedSearchString = EspressoUtils.getText(R.id.search_edit_text);
		ScreenActions.enterLog(TAG, initialSearch + " resolved to: " + resolvedSearchString);
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.searchEditText().perform(clearText());
		try {
			HotelsSearchScreen.clickSuggestionWithName(activity, resolvedSearchString);
		}
		catch (Exception e) {
			HotelsSearchScreen.clickSuggestionWithName(activity, initialSearch);
		}
		Espresso.pressBack();
	}

	public void testNoSearchesLongerThan28Days() {
		String searchError = "We're sorry, but we are unable to search for hotel stays longer than 28 days.";
		LaunchScreen.launchHotels();
		selectCalendarDates(1, 30);
		HotelsSearchScreen.clickOnGuestsButton();
		HotelsSearchScreen.guestPicker().clickOnSearchButton();
		EspressoUtils.assertViewWithTextIsDisplayed(searchError);
		SettingsScreen.clickOkString();
	}

	public void testThat28DaySearchesWork() {
		LaunchScreen.launchHotels();
		selectCalendarDates(1, 28);
		HotelsSearchScreen.clickOnGuestsButton();
		HotelsSearchScreen.guestPicker().clickOnSearchButton();
		HotelsSearchScreen.clickListItem(1);
		ScreenActions.enterLog(TAG, "Reaches hotel search results screen");
	}

	public void testSearchSpecificLocationInPOS() throws Throwable {
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText("London");
		HotelsSearchScreen.clickSuggestionAtIndex(getActivity(), 0);
		selectCalendarDates(5, 10);
		HotelsSearchScreen.clickSearchButton();
		HotelsSearchScreen.clickListItem(1);
		Espresso.pressBack();
		Espresso.pressBack();

		setPOS(PointOfSaleId.UNITED_KINGDOM);
		LaunchScreen.launchHotels();
		EspressoUtils.assertContains(HotelsSearchScreen.searchEditText(), mRes.getString(R.string.current_location));
		ScreenActions.enterLog(TAG, "New search should commence in the proper POS and search location should change to Current Location.");
		setPOS(PointOfSaleId.UNITED_STATES);
	}

	public void testSearchHotelInBourbonnais() {
		final int adultMax = 6;

		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText("Bourbonnais, IL");
		HotelsSearchScreen.clickSuggestionWithName(getActivity(), "Bourbonnais, IL");
		HotelsSearchScreen.clickOnGuestsButton();
		for (int i = 1; i < adultMax; i++) {
			HotelsGuestPicker.incrementAdultsButton();
		}
		HotelsSearchScreen.guestPicker().clickOnSearchButton();

		EspressoUtils.assertContains(onView(withId(R.id.search_progress_text_view)), mRes.getString(R.string.no_hotels_availiable_tonight));
		Common.pressBack();
		LaunchScreen.launchHotels();
		onView(withId(R.id.search_progress_text_view)).check(matches(not(withText(containsString(mRes.getString(R.string.no_filter_results))))));
		EspressoUtils.assertContains(onView(withId(R.id.search_progress_text_view)), mRes.getString(R.string.no_hotels_availiable_tonight));
	}

	public void testNoHotelsLocationSearch() {
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText("Alaska");
		selectCalendarDates(5, 10);
		HotelsSearchScreen.clickSearchButton();
		EspressoUtils.assertContains(onView(withId(R.id.search_progress_text_view)), mRes.getString(R.string.no_hotels_availiable));
		EspressoUtils.assertContains(onView(withId(R.id.search_progress_text_view)), mRes.getString(R.string.please_try_a_different_location_or_date));
		Espresso.pressBack();
	}
}
