package com.expedia.bookings.test.phone.flights;

import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.FlightTestCase;
import com.expedia.bookings.test.phone.pagemodels.flights.FlightLegScreen;
import com.expedia.bookings.test.phone.pagemodels.flights.FlightsSearchResultsScreen;
import com.expedia.bookings.test.phone.pagemodels.flights.FlightsSearchScreen;

public class FlightTotalPriceTest extends FlightTestCase {

	public void testTotalPriceMessaging() throws Exception {
		FlightsSearchScreen.clickDepartureAirportField();
		FlightsSearchScreen.enterDepartureAirport("SFO");
		FlightsSearchScreen.clickArrivalAirportField();
		FlightsSearchScreen.enterArrivalAirport("LAS");
		FlightsSearchScreen.clickSelectDepartureButton();
		LocalDate startDate = LocalDate.now().plusDays(35);
		FlightsSearchScreen.clickDate(startDate);
		FlightsSearchScreen.clickSearchButton();
		FlightsSearchResultsScreen.clickListItem(1);
		FlightLegScreen.clickSelectFlightButton();
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.trip_total, "$446.20");
	}
}
