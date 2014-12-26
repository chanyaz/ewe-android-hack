package com.expedia.bookings.test.ui.phone.tests.hotels;

import java.util.ArrayList;

import org.joda.time.LocalDate;

import android.app.Activity;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.ui.phone.pagemodels.common.SettingsScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsSearchScreen;
import com.expedia.bookings.test.ui.utils.EspressoUtils;
import com.expedia.bookings.test.ui.utils.PhoneTestCase;
import com.google.android.apps.common.testing.ui.espresso.Espresso;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;

/**
 * Created by dmadan on 5/13/14.
 */
public class HotelSearchRegressionTests extends PhoneTestCase {

	private static final String TAG = HotelSearchRegressionTests.class.getName();

	public void selectCalendardates(int start, int end) {
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
		String titleString;
		LaunchScreen.launchHotels();
		selectCalendardates(5, 10);
		for (String hotel : hotelList) {
			HotelsSearchScreen.clickSearchEditText();
			HotelsSearchScreen.clickToClearSearchEditText();
			HotelsSearchScreen.enterSearchText(hotel);
			HotelsSearchScreen.clickSuggestionWithName(getActivity(), hotel);
			titleString = EspressoUtils.getText(R.id.title);
			if (titleString.equals(hotel)) {
				ScreenActions.enterLog(TAG, "testSearchByHotelName passed with: " + hotel);
			}
			Espresso.pressBack();
		}
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
		EspressoUtils.assertViewWithTextIsDisplayed("Unable to determine search location.");
		ScreenActions.enterLog(TAG, "Geocoding error message text was displayed");
		Espresso.pressBack();
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
		EspressoUtils.assertContains(HotelsSearchScreen.searchEditText(),"Statue of Liberty");
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
		HotelsSearchScreen.clickToClearSearchEditText();
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
		selectCalendardates(1, 30);
		HotelsSearchScreen.clickOnGuestsButton();
		HotelsSearchScreen.guestPicker().clickOnSearchButton();
		EspressoUtils.assertViewWithTextIsDisplayed(searchError);
		SettingsScreen.clickOKString();
		Espresso.pressBack();
		Espresso.pressBack();
	}

	public void testThat28DaySearchesWork() {
		LaunchScreen.launchHotels();
		selectCalendardates(1, 28);
		HotelsSearchScreen.clickOnGuestsButton();
		HotelsSearchScreen.guestPicker().clickOnSearchButton();
		HotelsSearchScreen.clickListItem(1);
		ScreenActions.enterLog(TAG, "Reaches hotel search results screen");
		Espresso.pressBack();
		Espresso.pressBack();
	}
}
