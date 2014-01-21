package com.expedia.bookings.test.tests.hotels.ui.regression;

import java.util.ArrayList;

import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.utils.CustomActivityInstrumentationTestCase;

public class HotelsSearchRegressionTests extends CustomActivityInstrumentationTestCase<SearchActivity> {

	private static final String TAG = HotelsSearchRegressionTests.class.getSimpleName();

	public HotelsSearchRegressionTests() {
		super(SearchActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		mUser.setHotelCityToRandomUSCity();
		mDriver.launchScreen().openMenuDropDown();
		mDriver.launchScreen().pressSettings();
		mDriver.settingsScreen().clickToClearPrivateData();
		mDriver.settingsScreen().clickOKString();
		mDriver.settingsScreen().clickOKString();
		mDriver.goBack();
	}

	public void testChangingLocaleResetsSearchParams() throws Exception {
		// Change POS 
		mDriver.launchScreen().openMenuDropDown();
		mDriver.launchScreen().pressSettings();
		mDriver.settingsScreen().clickCountryString();
		mDriver.clickOnText(getString(R.string.country_ie));
		mDriver.settingsScreen().clickOKString();
		mDriver.goBack();

		// Add a search location and change other search params
		mDriver.launchScreen().launchHotels();
		int initialDateNumber = Integer.parseInt((String) mDriver.hotelsSearchScreen().calendarNumberTextView()
				.getText());
		mDriver.hotelsSearchScreen().clickSearchEditText();
		mDriver.hotelsSearchScreen().clickToClearSearchEditText();
		mDriver.hotelsSearchScreen().enterSearchText("San Francisco, CA");
		mDriver.hotelsSearchScreen().clickOnCalendarButton();
		mDriver.hotelsSearchScreen().clickDate(1);
		mDriver.hotelsSearchScreen().clickOnGuestsButton();
		mDriver.hotelsSearchScreen().guestPicker().clickIncrementAdultsButton();
		mDriver.hotelsSearchScreen().guestPicker().clickOnSearchButton();
		mDriver.waitForStringToBeGone(mDriver.hotelsSearchScreen().searchingForHotels());
		mDriver.goBack();

		// Change POS back to United States
		mDriver.launchScreen().openMenuDropDown();
		mDriver.launchScreen().pressSettings();
		mDriver.settingsScreen().clickCountryString();
		mDriver.delay();
		mDriver.clickOnText(getString(R.string.country_us));
		mDriver.settingsScreen().clickOKString();
		mDriver.delay();
		mDriver.goBack();

		// Verify that search screen info has reverted to fresh state
		mDriver.launchScreen().launchHotels();
		int postPOSChangeDateNumber = Integer.parseInt(mDriver.hotelsSearchScreen().calendarNumberTextView().getText()
				.toString());
		assertEquals(initialDateNumber, postPOSChangeDateNumber);

		String searchEditTextString = mDriver.hotelsSearchScreen().searchEditText().getText()
				.toString();
		assertEquals(getString(R.string.current_location), searchEditTextString);

		int postPOSChangeGuestNumber = Integer.parseInt(mDriver.hotelsSearchScreen().guestNumberTextView().getText()
				.toString());
		assertEquals(1, postPOSChangeGuestNumber);
	}

	public void testNoHotelsAvailableMessage() throws Exception {
		mDriver.launchScreen().launchHotels();
		mDriver.hotelsSearchScreen().clickSearchEditText();
		mDriver.hotelsSearchScreen().clickToClearSearchEditText();
		mDriver.hotelsSearchScreen().enterSearchText("Everglades, FL");
		mDriver.hotelsSearchScreen().clickOnGuestsButton();
		for (int i = 0; i < 6; i++) {
			mDriver.hotelsSearchScreen().guestPicker().clickIncrementAdultsButton();
		}
		mDriver.hotelsSearchScreen().guestPicker().clickOnSearchButton();
		mDriver.waitForStringToBeGone(mDriver.hotelsSearchScreen().searchingForHotels());
		assertTrue(mDriver.searchText(mDriver.hotelsSearchScreen().noHotelsAvailableTonight()));
		assertTrue(mDriver.searchText(mDriver.hotelsSearchScreen().pleaseTryADifferentLocationOrDate()));
		mDriver.goBack();
		mDriver.delay();
		mDriver.launchScreen().launchHotels();
		mDriver.waitForStringToBeGone(mDriver.hotelsSearchScreen().searchingForHotels());
		assertTrue(mDriver.searchText(mDriver.hotelsSearchScreen().noHotelsAvailableTonight()));
		assertTrue(mDriver.searchText(mDriver.hotelsSearchScreen().pleaseTryADifferentLocationOrDate()));
	}

	public void testSearchByHotelName() throws Exception {
		ArrayList<String> hotelList = new ArrayList<String>();
		hotelList.add("Hotel Nikko San Francisco");
		hotelList.add("Hotel Bijou");
		hotelList.add("Hilton Minneapolis");
		hotelList.add("Swissotel Berlin");
		hotelList.add("Pacific Beach Hotel");

		String titleString;
		int passThreshold = 3;
		int passCount = 0;
		mDriver.launchScreen().launchHotels();
		mDriver.hotelsSearchScreen().clickOnCalendarButton();
		mDriver.hotelsSearchScreen().clickDate(3);
		for (String hotel : hotelList) {
			mDriver.hotelsSearchScreen().clickSearchEditText();
			mDriver.hotelsSearchScreen().clickToClearSearchEditText();
			mDriver.hotelsSearchScreen().enterSearchText(hotel);
			mDriver.delay();
			mDriver.clickOnText(hotel);
			mDriver.waitForStringToBeGone(mDriver.hotelsSearchScreen().searchingForHotels());
			if (mDriver.searchText(mDriver.hotelsSearchScreen().roomNoLongerAvailable())) {
				passCount++;
				continue;
			}
			else {
				TextView titleView = mDriver.hotelsDetailsScreen().titleView();
				titleString = (String) titleView.getText();
				if (titleString.equals(hotel)) {
					mDriver.enterLog(TAG, "testSearchByHotelName passed with: " + hotel);
					passCount++;
				}
				mDriver.goBack();
				mDriver.delay(3);
				if (titleView.isShown()) {
					mDriver.goBack();
				}
			}
		}
		mDriver.enterLog(TAG, "passCount: " + passCount);
		assertTrue(passCount >= passThreshold);
	}

	/*
	 * Search edit text and geocoding tests
	 */

	public void testSearchMultipleGeocodeResponses() throws Exception {
		String ambiguousSearchString = "123";
		mDriver.launchScreen().launchHotels();
		mDriver.hotelsSearchScreen().clickSearchEditText();
		mDriver.hotelsSearchScreen().clickToClearSearchEditText();
		mDriver.hotelsSearchScreen().enterSearchText(ambiguousSearchString);
		mDriver.hotelsSearchScreen().clickOnGuestsButton();
		mDriver.hotelsSearchScreen().guestPicker().clickOnSearchButton();
		mDriver.waitForStringToBeGone(mDriver.hotelsSearchScreen().searchingForHotels());
		assertTrue(mDriver.searchText(mDriver.hotelsSearchScreen().didYouMean()));
		mDriver.enterLog(TAG, "'Did you mean' dialog appeared after search for: " + ambiguousSearchString);
		mDriver.goBack();

		ambiguousSearchString = "Clinton";
		mDriver.hotelsSearchScreen().clickSearchEditText();
		mDriver.hotelsSearchScreen().clickToClearSearchEditText();
		mDriver.hotelsSearchScreen().enterSearchText(ambiguousSearchString);
		mDriver.hotelsSearchScreen().clickOnGuestsButton();
		mDriver.hotelsSearchScreen().guestPicker().clickOnSearchButton();
		mDriver.waitForStringToBeGone(mDriver.hotelsSearchScreen().searchingForHotels());
		assertTrue(mDriver.searchText(mDriver.hotelsSearchScreen().didYouMean()));
		mDriver.enterLog(TAG, "'Did you mean' dialog appeared after search for: " + ambiguousSearchString);
	}

	public void testGeocodeResolutionOfCityAbbreviations() throws Exception {
		String cityAbbreviation = "NYC";
		mDriver.launchScreen().launchHotels();
		mDriver.hotelsSearchScreen().clickSearchEditText();
		mDriver.hotelsSearchScreen().clickToClearSearchEditText();
		mDriver.hotelsSearchScreen().enterSearchText(cityAbbreviation);
		mDriver.hotelsSearchScreen().clickOnGuestsButton();
		mDriver.hotelsSearchScreen().guestPicker().clickOnSearchButton();
		mDriver.enterLog(TAG, "Testing geocoding with abbrevation string: " + cityAbbreviation);
		mDriver.waitForStringToBeGone(mDriver.hotelsSearchScreen().searchingForHotels());
		String resolvedCityName = mDriver.hotelsSearchScreen().searchEditText().getText().toString();
		mDriver.enterLog(TAG, "Geocoding resolved abbreviation string to: " + resolvedCityName);
		assertTrue(resolvedCityName.equals("New York, NY"));
		mDriver.goBack();

		cityAbbreviation = "SF";
		mDriver.launchScreen().launchHotels();
		mDriver.hotelsSearchScreen().clickSearchEditText();
		mDriver.hotelsSearchScreen().clickToClearSearchEditText();
		mDriver.enterLog(TAG, "Testing geocoding with nonsense string: " + cityAbbreviation);
		mDriver.hotelsSearchScreen().enterSearchText(cityAbbreviation);
		mDriver.hotelsSearchScreen().clickOnGuestsButton();
		mDriver.hotelsSearchScreen().guestPicker().clickOnSearchButton();
		mDriver.waitForStringToBeGone(mDriver.hotelsSearchScreen().searchingForHotels());
		resolvedCityName = mDriver.hotelsSearchScreen().searchEditText().getText().toString();
		mDriver.enterLog(TAG, "Geocoding resolved abbreviation string to: " + resolvedCityName);
		assertTrue(resolvedCityName.equals("San Francisco, CA"));
	}

	public void testGeocodeFailureMessage() throws Exception {
		String nonsense = "hioeawh3aw09y4wajioyanwhalwqqqkeviniscool";
		mDriver.enterLog(TAG, "Testing geocoding with nonsense string: " + nonsense);
		mDriver.launchScreen().launchHotels();
		mDriver.hotelsSearchScreen().clickSearchEditText();
		mDriver.hotelsSearchScreen().clickToClearSearchEditText();
		mDriver.hotelsSearchScreen().enterSearchText(nonsense);
		mDriver.hotelsSearchScreen().clickOnGuestsButton();
		mDriver.hotelsSearchScreen().guestPicker().clickOnSearchButton();
		mDriver.waitForStringToBeGone(mDriver.hotelsSearchScreen().searchingForHotels());
		assertTrue(mDriver.searchText(mDriver.hotelsSearchScreen().unableToDetermineSearchLocation()));
		mDriver.enterLog(TAG, "Geocoding error message text was displayed");
	}

	public void testPointOfInterestGeocoding() throws Exception {
		String pointOfInterest = "Statue of Liberty";
		mDriver.enterLog(TAG, "Testing geocoding with POI string: " + pointOfInterest);
		mDriver.launchScreen().launchHotels();
		mDriver.hotelsSearchScreen().clickSearchEditText();
		mDriver.hotelsSearchScreen().clickToClearSearchEditText();
		mDriver.hotelsSearchScreen().enterSearchText(pointOfInterest);
		mDriver.hotelsSearchScreen().clickOnGuestsButton();
		mDriver.hotelsSearchScreen().guestPicker().clickOnSearchButton();
		mDriver.waitForStringToBeGone(mDriver.hotelsSearchScreen().searchingForHotels());
		String resolvedPOIName = mDriver.hotelsSearchScreen().searchEditText().getText().toString();
		mDriver.enterLog(TAG, "Geocoding resolved POI string to: " + resolvedPOIName);
		assertTrue(resolvedPOIName.equals("Statue of Liberty National Monument, New York, NY 10004"));
	}

	public void testPostalCodeGeocoding() throws Exception {
		String postalCode = "94104";
		mDriver.enterLog(TAG, "Testing geocoding with postal code string: " + postalCode);
		mDriver.launchScreen().launchHotels();
		mDriver.hotelsSearchScreen().clickSearchEditText();
		mDriver.hotelsSearchScreen().clickToClearSearchEditText();
		mDriver.hotelsSearchScreen().enterSearchText(postalCode);
		mDriver.hotelsSearchScreen().clickOnGuestsButton();
		mDriver.hotelsSearchScreen().guestPicker().clickOnSearchButton();
		mDriver.waitForStringToBeGone(mDriver.hotelsSearchScreen().searchingForHotels());
		String resolvedPostalCodeString = mDriver.hotelsSearchScreen().searchEditText().getText().toString();
		mDriver.enterLog(TAG, "Geocoding resolved postal code string to: " + resolvedPostalCodeString);
		assertTrue(resolvedPostalCodeString.equals("San Francisco, CA 94104"));
	}

	public void testCachingOfPreviousSearches() throws Exception {
		String initialSearch = "Belleville, MI";
		mDriver.launchScreen().launchHotels();
		mDriver.hotelsSearchScreen().clickSearchEditText();
		mDriver.hotelsSearchScreen().clickToClearSearchEditText();
		mDriver.hotelsSearchScreen().enterSearchText(initialSearch);
		mDriver.hotelsSearchScreen().clickOnGuestsButton();
		mDriver.hotelsSearchScreen().guestPicker().clickOnSearchButton();
		mDriver.waitForStringToBeGone(mDriver.hotelsSearchScreen().searchingForHotels());
		String resolvedSearchString = mDriver.hotelsSearchScreen().searchEditText().getText().toString();
		mDriver.enterLog(TAG, initialSearch + " resolved to: " + resolvedSearchString);

		mDriver.hotelsSearchScreen().clickSearchEditText();
		mDriver.hotelsSearchScreen().clickToClearSearchEditText();
		mDriver.delay();
		assertTrue(mDriver.searchText(initialSearch));
	}

	@Override
	protected void tearDown() throws Exception {
		mDriver.enterLog(TAG, "tearing down...");
		mDriver.finishOpenedActivities();
	}

}
