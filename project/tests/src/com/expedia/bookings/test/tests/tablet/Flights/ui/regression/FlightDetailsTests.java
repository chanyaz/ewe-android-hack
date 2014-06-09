package com.expedia.bookings.test.tests.tablet.Flights.ui.regression;

import org.joda.time.LocalDate;

import android.content.Context;
import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.tests.pageModels.tablet.Launch;
import com.expedia.bookings.test.tests.pageModels.tablet.Results;
import com.expedia.bookings.test.utils.EspressoUtils;
import com.expedia.bookings.test.utils.HotelsUserData;
import com.expedia.bookings.utils.ClearPrivateDataUtil;
import com.google.android.apps.common.testing.ui.espresso.DataInteraction;
import com.mobiata.android.util.SettingUtils;

/**
 * Created by dmadan on 6/2/14.
 */
public class FlightDetailsTests extends ActivityInstrumentationTestCase2<SearchActivity> {
	public FlightDetailsTests() {
		super(SearchActivity.class);
	}

	private static final String TAG = FlightDetailsTests.class.getSimpleName();
	Context mContext;
	Resources mRes;
	HotelsUserData mUser;

	protected void setUp() throws Exception {
		super.setUp();
		mContext = getInstrumentation().getTargetContext();
		mRes = mContext.getResources();
		mUser = new HotelsUserData(getInstrumentation());
		ClearPrivateDataUtil.clear(mContext);
		SettingUtils.save(mContext, R.string.preference_which_api_to_use_key, "Integration");
		getActivity();
	}

	public void testFlightDetails() throws Exception {
		// search for a flight that should always be direct
		flightSearch();
		verifyFlightDetails();
	}

	//Helper methods

	private void flightSearch() {
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("Los Angeles, CA");
		Launch.clickSuggestion("Los Angeles, CA");
		Results.clickOriginButton();
		Results.typeInOriginEditText("San Francisco, CA");
		Results.clickSuggestion("San Francisco, CA");
		Results.clickSelectFlightDates();
		int randomOffset = 20 + (int) (Math.random() * 100);
		LocalDate startDate = LocalDate.now().plusDays(randomOffset);
		Results.clickDate(startDate, null);
		Results.clickSearchNow();
		Results.swipeUpFlightList();
		Results.clickFlightAtIndex(1);
	}

	// Verifies that flight details info on the card matches the flight search results info

	private void verifyFlightDetails() throws Exception {
		int totalFlights = EspressoUtils.getListCount(Results.flightList());
		for (int j = 1; j < totalFlights - 1; j++) {
			DataInteraction previousRow = Results.flightAtIndex(j);
			//Store flight search results info
			String resultsFlightName = EspressoUtils.getListItemValues(previousRow, R.id.airline_text_view);
			String flightTime = EspressoUtils.getListItemValues(previousRow, R.id.flight_time_text_view);
			String cleanedFlightTime = flightTime.substring(0, flightTime.lastIndexOf("M") + 1);
			String arrivalTime = cleanedFlightTime.substring(cleanedFlightTime.indexOf("o") + 2, cleanedFlightTime.length());
			String departureTime = cleanedFlightTime.substring(0, cleanedFlightTime.indexOf("t") - 1);
			String resultsPriceString = EspressoUtils.getListItemValues(previousRow, R.id.price_text_view);
			//Click on search result
			Results.clickFlightAtIndex(j);
			try {
				//Store flight details info on the card
				String detailsFlightName = EspressoUtils.getText(R.id.airline_and_cities_text_view);
				String detailsFlightTime = EspressoUtils.getText(R.id.details_time_header);
				String detailsArrivalTime = EspressoUtils.getText(R.id.arrival_time_text_view);
				String detailsDepartureTime = EspressoUtils.getText(R.id.departure_time_text_view);
				String detailsHeaderPrice = EspressoUtils.getText(R.id.details_add_trip_button);

				assertTrue(detailsFlightName.contains(resultsFlightName));
				assertEquals(cleanedFlightTime, detailsFlightTime);
				assertEquals(departureTime, detailsDepartureTime);
				assertEquals(arrivalTime, detailsArrivalTime);
				assertTrue(detailsHeaderPrice.contains(resultsPriceString));
			}
			catch (Exception e) {
				continue;
			}
		}
	}
}
