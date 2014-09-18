package com.expedia.bookings.test.phone.tests.localization;

import org.joda.time.LocalDate;

import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.test.phone.pagemodels.common.BillingAddressScreen;
import com.expedia.bookings.test.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.phone.pagemodels.flights.FlightLegScreen;
import com.expedia.bookings.test.phone.pagemodels.flights.FlightsCheckoutScreen;
import com.expedia.bookings.test.phone.pagemodels.flights.FlightsSearchResultsScreen;
import com.expedia.bookings.test.phone.pagemodels.flights.FlightsSearchScreen;
import com.expedia.bookings.test.phone.pagemodels.flights.FlightsTravelerInfoScreen;
import com.expedia.bookings.test.tablet.pagemodels.Common;
import com.expedia.bookings.test.utils.EspressoUtils;
import com.expedia.bookings.test.utils.PhoneTestCase;

/**
 * Created by dmadan on 9/16/14.
 */
public class BookingInfoLocalizationTest extends PhoneTestCase {

	public void goToCheckout(String countryCode, String country) {
		LaunchScreen.launchFlights();
		FlightsSearchScreen.enterDepartureAirport("LAX");
		FlightsSearchScreen.enterArrivalAirport("LHR");

		FlightsSearchScreen.clickSelectDepartureButton();
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);
		FlightsSearchScreen.clickDate(startDate, endDate);
		FlightsSearchScreen.clickSearchButton();
		FlightsSearchResultsScreen.clickListItem(1);
		FlightLegScreen.clickSelectFlightButton();
		FlightsSearchResultsScreen.clickListItem(1);
		FlightLegScreen.clickSelectFlightButton();
		FlightsCheckoutScreen.clickCheckoutButton();
		FlightsCheckoutScreen.clickTravelerDetails();

		//test: phone number prefix should be localized
		EspressoUtils.assertContains(FlightsTravelerInfoScreen.phoneCountryCodeText(), countryCode);
		Common.closeSoftKeyboard(FlightsTravelerInfoScreen.phoneNumberEditText());
		Common.pressBack();
		Common.pressBack();
		FlightsCheckoutScreen.clickCheckoutButton();
		FlightsCheckoutScreen.clickSelectPaymentButton();

		//test: passport country should be localized
		EspressoUtils.assertContains(BillingAddressScreen.passportCountryText(), country);
		Common.pressBack();
	}

	public void testBookingInfoUSPOS() throws Throwable {
		setPOS(PointOfSaleId.UNITED_STATES);
		goToCheckout("+1", "United States");
	}

	public void testBookingInfoUKPOS() throws Throwable {
		setPOS(PointOfSaleId.UNITED_KINGDOM);
		goToCheckout("+44", "United Kingdom");
	}

	public void testBookingInfoFrancePOS() throws Throwable {
		setPOS(PointOfSaleId.FRANCE);
		goToCheckout("+33", "France");
	}

	public void testBookingInfoGermanyPOS() throws Throwable {
		setPOS(PointOfSaleId.GERMANY);
		goToCheckout("+49", "Germany");
	}

	public void testBookingInfoBrazilPOS() throws Throwable {
		setPOS(PointOfSaleId.BRAZIL);
		goToCheckout("+55", "Brazil");
	}

	public void testBookingInfoAustraliaPOS() throws Throwable {
		setPOS(PointOfSaleId.AUSTRALIA);
		goToCheckout("+61", "Australia");
	}

	public void testBookingInfoCanadaPOS() throws Throwable {
		setPOS(PointOfSaleId.CANADA);
		goToCheckout("+1", "Canada");
	}
}
