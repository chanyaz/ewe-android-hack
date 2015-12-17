package com.expedia.bookings.test.ui.tablet.tests.localization;

import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.test.ui.tablet.pagemodels.Checkout;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;
import com.expedia.bookings.test.ui.tablet.pagemodels.HotelDetails;
import com.expedia.bookings.test.ui.tablet.pagemodels.Launch;
import com.expedia.bookings.test.ui.tablet.pagemodels.Results;
import com.expedia.bookings.test.ui.tablet.pagemodels.Search;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.TabletTestCase;

/**
 * Created by dmadan on 8/25/14.
 */
public class ZipCodeValidationTest extends TabletTestCase {

	/*
	 *  POS test: Zip code is required for guest hotel bookings in CA, UK, US
	 *  This test needs to be run against Production, so make sure
	 *  that your config.json has the server name variable as Production
	 */

	public void runZipCodeValidationTest() {
		//error pops up on hotel checkout without Zip code
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("Detroit, MI");
		Launch.clickSuggestionAtPosition(1);
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);
		Search.clickDate(startDate, endDate);
		Search.clickSearchPopupDone();

		Results.swipeUpHotelList();
		Results.clickHotelAtIndex(1);
		HotelDetails.clickAddHotel();
		Results.clickBookButton();

		Checkout.clickOnEmptyTravelerDetails();
		Common.closeSoftKeyboard(Checkout.firstName());
		Checkout.enterFirstName("Mobiata");
		Checkout.enterLastName("Auto");
		Checkout.enterPhoneNumber("1112223333");
		Checkout.enterEmailAddress("aaa@aaa.com");
		Common.closeSoftKeyboard(Checkout.emailAddress());
		Checkout.clickOnDone();

		Checkout.clickOnEnterPaymentInformation();
		Checkout.enterNameOnCard("Mobiata Auto");
		Checkout.enterCreditCardNumber("4111111111111111");
		Common.closeSoftKeyboard(Checkout.creditCardNumber());
		Checkout.setExpirationDate(2020, 12);
		Common.closeSoftKeyboard(Checkout.postalCode());
		Checkout.clickOnDone();
		try {
			Checkout.clickIAcceptButton();
		}
		catch (Exception e) {
			//no I accept button
		}

		Checkout.slideToPurchase();
		Checkout.enterCvv("111");
		Checkout.clickBookButton();

		//error popup
		EspressoUtils.assertViewWithTextIsDisplayed(mRes.getString(R.string.invalid_postal_code));
		Checkout.clickOKButton();

		// complete checkout after error popup
		Checkout.enterPostalCode("95104");
		Common.closeSoftKeyboard(Checkout.postalCode());
		Checkout.clickOnDone();

		Checkout.slideToPurchase();
		Checkout.enterCvv("111");
		Checkout.clickBookButton();
	}

	public void testUSValidation() throws Throwable {
		setPOS(PointOfSaleId.UNITED_STATES);
		runZipCodeValidationTest();
	}

	public void testUKValidation() throws Throwable {
		setPOS(PointOfSaleId.UNITED_KINGDOM);
		runZipCodeValidationTest();
	}

	public void testCAValidation() throws Throwable {
		setPOS(PointOfSaleId.CANADA);
		runZipCodeValidationTest();
	}

	public void testAusValidation() throws Throwable {
		setPOS(PointOfSaleId.AUSTRALIA);
		//hotel checkout without Zip code works for Australia POS
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("Detroit, MI");
		Launch.clickSuggestionAtPosition(1);
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);
		Search.clickDate(startDate, endDate);
		Search.clickSearchPopupDone();

		Results.swipeUpHotelList();
		Results.clickHotelAtIndex(1);
		HotelDetails.clickAddHotel();
		Results.clickBookButton();

		Checkout.clickOnEmptyTravelerDetails();
		Common.closeSoftKeyboard(Checkout.firstName());
		Checkout.enterFirstName("Mobiata");
		Checkout.enterLastName("Auto");
		Checkout.enterPhoneNumber("1112223333");
		Checkout.enterEmailAddress("aaa@aaa.com");
		Common.closeSoftKeyboard(Checkout.emailAddress());
		Checkout.clickOnDone();

		Checkout.clickOnEnterPaymentInformation();
		Checkout.enterNameOnCard("Mobiata Auto");
		Checkout.enterCreditCardNumber("4111111111111111");
		Common.closeSoftKeyboard(Checkout.creditCardNumber());
		Checkout.setExpirationDate(2020, 12);
		Common.closeSoftKeyboard(Checkout.postalCode());
		Checkout.clickOnDone();
		try {
			Checkout.clickIAcceptButton();
		}
		catch (Exception e) {
			//no I accept button
		}

		Checkout.slideToPurchase();
		Checkout.enterCvv("111");
		Checkout.clickBookButton();
	}
}
