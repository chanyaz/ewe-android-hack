package com.expedia.bookings.test.tests.hotelsEspresso.ui.regression;

import java.util.ArrayList;
import java.util.Calendar;

import org.joda.time.LocalDate;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.LaunchScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.ScreenActions;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.SettingsScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsSearchScreen;
import com.expedia.bookings.test.utils.EspressoUtils;
import com.expedia.bookings.utils.ClearPrivateDataUtil;
import com.google.android.apps.common.testing.ui.espresso.Espresso;
import com.mobiata.android.util.SettingUtils;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;

/**
 * Created by dmadan on 5/13/14.
 */
public class HotelSearchRegressionTests extends ActivityInstrumentationTestCase2<SearchActivity> {
	public HotelSearchRegressionTests() {
		super(SearchActivity.class);
	}

	private static final String TAG = HotelSearchRegressionTests.class.getName();

	Context mContext;
	SharedPreferences mPrefs;
	Resources mRes;

	protected void setUp() throws Exception {
		super.setUp();
		mContext = getInstrumentation().getTargetContext();
		mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		mRes = mContext.getResources();
		ClearPrivateDataUtil.clear(mContext);
		SettingUtils.save(mContext, R.string.preference_which_api_to_use_key, "Integration");
		getActivity();
	}

	public void selectCalendardates(int start, int end) {
		Calendar cal = Calendar.getInstance();
		int year = cal.get(cal.YEAR);
		int month = cal.get(cal.MONTH) + 1;
		LocalDate mStartDate = new LocalDate(year, month, start);
		LocalDate mEndDate = new LocalDate(year, month, end);
		HotelsSearchScreen.clickOnCalendarButton();
		HotelsSearchScreen.clickDate(mStartDate, mEndDate);
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
			HotelsSearchScreen.clickSuggestion(getActivity(), hotel);
			EspressoUtils.getValues("titleString", R.id.title);
			titleString = mPrefs.getString("titleString", "");
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
		EspressoUtils.assertTrue("Did you mean…");
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
		EspressoUtils.getValues("resolvedCityName", R.id.search_edit_text);
		String resolvedCityName = mPrefs.getString("resolvedCityName", "");
		ScreenActions.enterLog(TAG, "Geocoding resolved abbreviation string to: " + resolvedCityName);
		assertTrue(resolvedCityName.equals("New York, NY"));
		Espresso.pressBack();

		cityAbbreviation = "SF";
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText(cityAbbreviation);
		HotelsSearchScreen.clickOnGuestsButton();
		HotelsSearchScreen.guestPicker().clickOnSearchButton();
		ScreenActions.enterLog(TAG, "Testing geocoding with abbrevation string: " + cityAbbreviation);
		EspressoUtils.getValues("resolvedCityName", R.id.search_edit_text);
		resolvedCityName = mPrefs.getString("resolvedCityName", "");
		ScreenActions.enterLog(TAG, "Geocoding resolved abbreviation string to: " + resolvedCityName);
		assertTrue(resolvedCityName.equals("San Francisco, CA"));
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
		EspressoUtils.assertTrue("Unable to determine search location.");
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
		EspressoUtils.getValues("resolvedPOIName", R.id.search_edit_text);
		String resolvedPOIName = mPrefs.getString("resolvedPOIName", "");
		ScreenActions.enterLog(TAG, "Geocoding resolved POI string to: " + resolvedPOIName);
		assertTrue(resolvedPOIName.equals("Statue of Liberty National Monument, New York, NY 10004"));
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
		EspressoUtils.getValues("resolvedPostalCodeString", R.id.search_edit_text);
		String resolvedPostalCodeString = mPrefs.getString("resolvedPostalCodeString", "");
		assertTrue(resolvedPostalCodeString.equals("San Francisco, CA 94104"));
		Espresso.pressBack();
	}

	public void testCachingOfPreviousSearches() throws Exception {
		String initialSearch = "Belleville, MI";
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText(initialSearch);
		HotelsSearchScreen.clickOnGuestsButton();
		HotelsSearchScreen.guestPicker().clickOnSearchButton();
		EspressoUtils.getValues("resolvedSearchString", R.id.search_edit_text);
		String resolvedSearchString = mPrefs.getString("resolvedSearchString", "");
		ScreenActions.enterLog(TAG, initialSearch + " resolved to: " + resolvedSearchString);
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		try {
			HotelsSearchScreen.clickSuggestion(getActivity(), resolvedSearchString);
		}
		catch (Exception e) {
			HotelsSearchScreen.clickSuggestion(getActivity(), initialSearch);
		}
		Espresso.pressBack();
	}

	public void testNoSearchesLongerThan28Days() {
		String searchError = "We're sorry, but we are unable to search for hotel stays longer than 28 days.";
		LaunchScreen.launchHotels();
		selectCalendardates(1, 30);
		HotelsSearchScreen.clickOnGuestsButton();
		HotelsSearchScreen.guestPicker().clickOnSearchButton();
		EspressoUtils.assertTrue(searchError);
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
