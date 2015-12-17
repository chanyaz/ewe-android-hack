package com.expedia.bookings.test.ui.tablet.tests.flights;

import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.tablet.pagemodels.FlightDetails;
import com.expedia.bookings.test.ui.tablet.pagemodels.Launch;
import com.expedia.bookings.test.ui.tablet.pagemodels.Results;
import com.expedia.bookings.test.ui.tablet.pagemodels.Search;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.TabletTestCase;
import android.support.test.espresso.DataInteraction;

/**
 * Created by dmadan on 6/2/14.
 */
public class FlightDetailsTests extends TabletTestCase {

	public void testFlightDetails() throws Exception {
		// search for a flight that should always be direct
		flightSearch();
		verifyFlightDetails();
	}

	//Helper methods

	private void flightSearch() {
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("Detroit, MI");
		Launch.clickSuggestion("Detroit, MI");
		Search.clickOriginButton();
		Search.typeInOriginEditText("San Francisco, CA");
		Search.clickSuggestion("San Francisco, CA");
		Search.clickSelectFlightDates();
		int randomOffset = 20 + (int) (Math.random() * 100);
		LocalDate startDate = LocalDate.now().plusDays(randomOffset);
		Search.clickDate(startDate, null);
		Search.clickSearchPopupDone();
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
				//assert flight details info on the card matches flight search result info
				EspressoUtils.assertContains(FlightDetails.flightName(), resultsFlightName);
				EspressoUtils.assertContains(FlightDetails.flightTime(), arrivalTime);
				EspressoUtils.assertContains(FlightDetails.flightTime(), departureTime);
				EspressoUtils.assertContains(FlightDetails.flightArrivalTime(), arrivalTime);
				EspressoUtils.assertContains(FlightDetails.flightDepartureTime(), departureTime);
				EspressoUtils.assertContains(FlightDetails.flightPrice(), resultsPriceString);
			}
			catch (Exception e) {
				continue;
			}
		}
	}
}
