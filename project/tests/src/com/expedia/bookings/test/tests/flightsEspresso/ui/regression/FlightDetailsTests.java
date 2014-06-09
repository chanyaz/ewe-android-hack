package com.expedia.bookings.test.tests.flightsEspresso.ui.regression;

import java.util.Calendar;

import org.joda.time.LocalDate;

import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.LaunchScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.ScreenActions;
import com.expedia.bookings.test.tests.pageModelsEspresso.flights.FlightLegScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.flights.FlightsSearchResultsScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.flights.FlightsSearchScreen;
import com.expedia.bookings.test.utils.EspressoUtils;
import com.expedia.bookings.utils.ClearPrivateDataUtil;
import com.google.android.apps.common.testing.ui.espresso.DataInteraction;
import com.mobiata.android.util.SettingUtils;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.pressBack;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;

/**
 * Created by dmadan on 5/1/14.
 */
public class FlightDetailsTests extends ActivityInstrumentationTestCase2<SearchActivity> {
	public FlightDetailsTests() {
		super(SearchActivity.class);
	}

	private static final String TAG = FlightDetailsTests.class.getName();

	Context mContext;

	protected void setUp() throws Exception {
		super.setUp();
		mContext = getInstrumentation().getTargetContext();
		ClearPrivateDataUtil.clear(mContext);
		SettingUtils.save(mContext, R.string.preference_which_api_to_use_key, "Integration");
		SettingUtils.save(mContext, R.id.preference_suppress_flight_booking_checkbox, "true");
		getActivity();
	}

	public void testFlightDetails() throws Exception {
		// search for a flight that should always be direct
		flightSearch();
		verifyFlightDetails();
	}

	//Helper methods

	private void checkBaggageFeeInfoButton() {
		FlightLegScreen.clickBaggageInfoView();
		EspressoUtils.assertTrue("Baggage Fees");
		pressBack();
	}

	private void flightSearch() {
		ScreenActions.enterLog(TAG, "Launching flights application");
		LaunchScreen.launchFlights();
		FlightsSearchScreen.enterDepartureAirport("SFO");
		FlightsSearchScreen.enterArrivalAirport("LAX");
		FlightsSearchScreen.clickSelectDepartureButton();
		Calendar cal = Calendar.getInstance();
		int year = cal.get(cal.YEAR);
		int month = cal.get(cal.MONTH) + 1;
		LocalDate mStartDate = new LocalDate(year, month, 5);
		LocalDate mEndDate = new LocalDate(year, month, 1);
		FlightsSearchScreen.clickDate(mStartDate, mEndDate);
		FlightsSearchScreen.clickSearchButton();
		FlightsSearchResultsScreen.clickSortFlightsButton();
		FlightsSearchResultsScreen.clickToSortByDuration();
	}

	// Verifies that flight details info on the card matches the flight search results info

	private void verifyFlightDetails() throws Exception {
		DataInteraction searchResultRow = FlightsSearchResultsScreen.listItem().atPosition(1);

		//Store flight search results info
		String resultsFlightName = EspressoUtils.getListItemValues(searchResultRow, R.id.airline_text_view);
		String resultsDepartureTime = EspressoUtils.getListItemValues(searchResultRow, R.id.departure_time_text_view);
		String resultsArrivalTime = EspressoUtils.getListItemValues(searchResultRow, R.id.arrival_time_text_view);
		String resultsPriceString = EspressoUtils.getListItemValues(searchResultRow, R.id.price_text_view);

		//Click on search result
		searchResultRow.perform(click());

		//Store flight details info on the card
		String detailsFlightName = EspressoUtils.getText(R.id.airline_text_view);
		String detailsDepartureTime = EspressoUtils.getText(R.id.departure_time_text_view);
		String detailsArrivalTime = EspressoUtils.getText(R.id.arrival_time_text_view);
		String detailsHeaderPrice = EspressoUtils.getText(R.id.right_text_view);
		String detailsString = EspressoUtils.getText(R.id.details_text_view);
		String cardDurationString = detailsString.substring(0, detailsString.indexOf(' ', detailsString.indexOf(' ')));
		String headerDurationString = EspressoUtils.getText(R.id.left_text_view).substring(0, detailsString.indexOf(' ', detailsString.indexOf(' ')));

		assertTrue(detailsFlightName.contains(resultsFlightName));
		assertEquals(resultsDepartureTime, detailsDepartureTime);
		assertEquals(resultsArrivalTime, detailsArrivalTime);
		assertTrue(detailsHeaderPrice.contains(resultsPriceString));
		assertEquals(cardDurationString, headerDurationString);

		// Test baggage fee info button
		checkBaggageFeeInfoButton();

		FlightLegScreen.clickCancelButton();
		pressBack();
		pressBack();
	}
}


