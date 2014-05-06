package com.expedia.bookings.test.tests.flightsEspresso.ui.regression;

import java.util.Calendar;

import org.joda.time.LocalDate;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.LaunchScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.ScreenActions;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.SettingsScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.flights.FlightsSearchScreen;
import com.expedia.bookings.test.utils.EspressoUtils;
import com.expedia.bookings.utils.ClearPrivateDataUtil;
import com.mobiata.android.util.SettingUtils;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.pressBack;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;

/**
 * Created by dmadan on 5/2/14.
 */
public class FlightSearchTests extends ActivityInstrumentationTestCase2<SearchActivity> {
	private static final String TAG = "FlightSearchTests";
	private Context mContext;
	private SharedPreferences mPrefs;
	private Calendar mCal;
	private int mYear;
	private int mMonth;
	private LocalDate mStartDate;
	private LocalDate mEndDate;

	public FlightSearchTests() {
		super(SearchActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		mContext = getInstrumentation().getTargetContext();
		mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		mCal = Calendar.getInstance();
		mYear = mCal.get(mCal.YEAR);
		mMonth = mCal.get(mCal.MONTH) + 1;
		mStartDate = new LocalDate(mYear, mMonth, 5);
		mEndDate = new LocalDate(mYear, mMonth, 1);
		ClearPrivateDataUtil.clear(mContext);
		SettingUtils.save(mContext, R.string.preference_which_api_to_use_key, "Integration");
		SettingUtils.save(mContext, R.id.preference_suppress_flight_booking_checkbox, "true");
		getActivity();
	}

	// Test to check duplicate airport search gives error message
	public void testDuplicateAirportSearchGivesErrorMessage() throws Exception {
		ScreenActions.enterLog(TAG, "START TEST:");
		LaunchScreen.launchFlights();
		FlightsSearchScreen.clickDepartureAirportField();
		FlightsSearchScreen.enterDepartureAirport("SFO");
		FlightsSearchScreen.clickArrivalAirportField();
		FlightsSearchScreen.enterArrivalAirport("SFO");
		FlightsSearchScreen.clickSelectDepartureButton();
		FlightsSearchScreen.clickDate(mStartDate, mEndDate);
		FlightsSearchScreen.clickSearchButton();
		EspressoUtils.assertTrue("Departure and arrival airports must be different.");
		ScreenActions.enterLog(TAG, "Duplicate airport search error message displayed.");
		SettingsScreen.clickOKString();
		pressBack();
		pressBack();
		ScreenActions.enterLog(TAG, "END TEST");
	}

	//Test number of traveler shows correctly in Textview
	public void testGuestButtonTextView() throws Exception {
		ScreenActions.enterLog(TAG, "START TEST:");
		String value = "value";
		LaunchScreen.launchFlights();
		FlightsSearchScreen.clickPassengerSelectionButton();
		for (int i = 1; i <= 6; i++) {
			FlightsSearchScreen.getTravelerNumberText(value);
			String adultQuantity = mPrefs.getString(value, "");
			EspressoUtils.getValues(value, R.id.refinement_info_text_view);
			String adultQuantityTextView = mPrefs.getString(value, "");
			assertEquals(adultQuantity, adultQuantityTextView);
			FlightsSearchScreen.incrementAdultsButton();
		}
		pressBack();
		pressBack();
		ScreenActions.enterLog(TAG, "END TEST");
	}

	//Test clearing selected dates work
	public void testClearingSelectedDates() throws Exception {
		ScreenActions.enterLog(TAG, "START TEST:");
		LaunchScreen.launchFlights();
		FlightsSearchScreen.clickDepartureAirportField();
		FlightsSearchScreen.enterDepartureAirport("SFO");
		FlightsSearchScreen.clickArrivalAirportField();
		FlightsSearchScreen.enterArrivalAirport("LAX");
		FlightsSearchScreen.clickSelectDepartureButton();
		FlightsSearchScreen.clickDate(mStartDate, mEndDate);
		FlightsSearchScreen.clickClearSelectedDatesButton();
		FlightsSearchScreen.checkHint("Select a departure date");
		pressBack();
		pressBack();
		ScreenActions.enterLog(TAG, "END TEST");
	}

	//Test can't search until all data is added.
	public void testTryingToSearchWithMissingInfo() {
		ScreenActions.enterLog(TAG, "START TEST:");
		LaunchScreen.launchFlights();
		FlightsSearchScreen.enterDepartureAirport("SFO");
		try {
			FlightsSearchScreen.clickSearchButton();
		}
		catch (Exception e) {
			ScreenActions.enterLog(TAG, "Wasn't able to click the search button without arrival, as expected.");
		}
		FlightsSearchScreen.enterArrivalAirport("LAX");
		FlightsSearchScreen.clickSelectDepartureButton();
		try {
			FlightsSearchScreen.clickSearchButton();
		}
		catch (Exception e) {
			ScreenActions.enterLog(TAG, "Wasn't able to click the search button without date, as expected.");
		}
		FlightsSearchScreen.clickDate(mStartDate, mEndDate);
		FlightsSearchScreen.searchButton().check(matches(isDisplayed()));
		ScreenActions.enterLog(TAG, "Successfully asserted that the search button is shown.");
		pressBack();
		ScreenActions.enterLog(TAG, "END TEST");
	}

	//Test One way international flights,and getting to search results screen
	public void testOneWayInternationalFlight() throws Exception {
		ScreenActions.enterLog(TAG, "START TEST:");
		LaunchScreen.launchFlights();
		FlightsSearchScreen.enterDepartureAirport("LAX");
		FlightsSearchScreen.enterArrivalAirport("Frankfurt, Germany");
		FlightsSearchScreen.clickSelectDepartureButton();
		FlightsSearchScreen.clickDate(mStartDate, mEndDate);
		FlightsSearchScreen.clickSearchButton();
		pressBack();
		pressBack();
		ScreenActions.enterLog(TAG, "END TEST");
	}

	//Test Round trip international flights,and getting to search results screen
	public void testRoundTripInternationalFlight() throws Exception {
		ScreenActions.enterLog(TAG, "START TEST:");
		LaunchScreen.launchFlights();
		FlightsSearchScreen.enterDepartureAirport("LAX");
		FlightsSearchScreen.enterArrivalAirport("Frankfurt, Germany");
		FlightsSearchScreen.clickSelectDepartureButton();
		mEndDate = new LocalDate(mYear, mMonth, 10);
		FlightsSearchScreen.clickDate(mStartDate, mEndDate);
		FlightsSearchScreen.clickSearchButton();
		pressBack();
		pressBack();
		ScreenActions.enterLog(TAG, "END TEST");
	}
}
