package com.expedia.bookings.test.ui.tablet.tests.localization;

import org.joda.time.LocalDate;

import com.expedia.bookings.test.ui.tablet.pagemodels.Checkout;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;
import com.expedia.bookings.test.ui.tablet.pagemodels.Launch;
import com.expedia.bookings.test.ui.tablet.pagemodels.Results;
import com.expedia.bookings.test.ui.tablet.pagemodels.Search;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.TabletTestCase;
import com.expedia.bookings.data.pos.PointOfSaleId;


/**
 * Created by dmadan on 9/17/14.
 */
public class BookingInfoLocalizationTest extends TabletTestCase {

	public void goToCheckout(String countryCode, String country) {
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("London, England, UK");
		Search.clickSuggestionAtPosition(1);

		Search.clickOriginButton();
		Search.typeInOriginEditText("Detroit, MI");
		Search.clickSuggestionAtPosition(1);
		Search.clickSelectFlightDates();
		LocalDate startDate = LocalDate.now().plusDays(46);
		Search.clickDate(startDate, null);
		Search.clickSearchPopupDone();
		Results.swipeUpFlightList();
		Results.clickFlightAtIndex(1);
		Results.clickAddFlight();
		Results.clickBookFlight();

		Checkout.clickOnEmptyTravelerDetails();
		Common.closeSoftKeyboard(Checkout.firstName());

		//test: phone number prefix should be localized
		EspressoUtils.assertContains(Checkout.phoneCountryCodeText(), countryCode);

		//test: passport country should be localized
		EspressoUtils.assertContains(Checkout.passportCountryText(), country);
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
