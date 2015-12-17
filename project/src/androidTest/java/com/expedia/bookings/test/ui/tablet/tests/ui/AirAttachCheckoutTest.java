package com.expedia.bookings.test.ui.tablet.tests.ui;

import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.tablet.pagemodels.Checkout;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;
import com.expedia.bookings.test.ui.tablet.pagemodels.HotelDetails;
import com.expedia.bookings.test.ui.tablet.pagemodels.Launch;
import com.expedia.bookings.test.ui.tablet.pagemodels.Results;
import com.expedia.bookings.test.ui.tablet.pagemodels.Search;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.TabletTestCase;

/**
 * Created by dmadan on 12/11/14.
 */
public class AirAttachCheckoutTest extends TabletTestCase {
	public void testBookAirAttach() throws Throwable {
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("Detroit, MI");
		Launch.clickSuggestion("Detroit, MI");

		Search.clickOriginButton();
		Search.typeInOriginEditText("San Francisco, CA");
		Search.clickSuggestion("San Francisco, CA");
		Search.clickSelectFlightDates();
		LocalDate startDate = LocalDate.now().plusDays(35);
		Search.clickDate(startDate, null);
		Search.clickSearchPopupDone();

		// Add Hotel to trip bucket
		Results.swipeUpHotelList();
		Results.clickHotelWithName("air_attached_hotel");
		HotelDetails.clickAddHotel();

		// Add Flight to trip bucket
		Results.swipeUpFlightList();
		Results.clickFlightAtIndex(7);
		Results.clickAddFlight();

		Results.clickBookFlight();
		Checkout.clickOnEmptyTravelerDetails();
		Checkout.enterFirstName("Mobiata");
		Checkout.enterLastName("Auto");
		Checkout.enterPhoneNumber("1112223333");
		Checkout.enterEmailAddress("aaa@aaa.com");
		Common.closeSoftKeyboard(Checkout.emailAddress());
		Checkout.enterDateOfBirth(1970, 1, 1);
		Checkout.clickOnDone();

		Checkout.clickOnEnterPaymentInformation();
		Checkout.enterCreditCardNumber("4111111111111111");
		Common.closeSoftKeyboard(Checkout.creditCardNumber());
		Checkout.setExpirationDate(2020, 12);
		Checkout.enterNameOnCard("Mobiata Auto");
		Checkout.enterAddress1("123 Main St.");
		Checkout.enterAddress2("Apt. 1");
		Checkout.enterCity("Madison");
		Checkout.enterState("WI");
		Checkout.enterPostalCode("53704");
		Common.closeSoftKeyboard(Checkout.firstName());
		Checkout.clickOnDone();

		//price of hotel in the trip bucket before completing flight checkout
		String tripBucketHotelPrice = EspressoUtils.getTextWithSibling(R.id.trip_bucket_price_text, R.id.book_button_text);

		Checkout.slideToPurchase();
		Checkout.enterCvv("111");
		Checkout.clickBookButton();

		//price of hotel in the trip bucket after completing flight checkout
		String tripBucketHotelPriceWithDiscount = EspressoUtils.getTextWithSibling(R.id.trip_bucket_price_text, R.id.book_button_text);

		EspressoUtils.assertViewIsDisplayed(R.id.air_attach_text_view);
		//air attach text view: " Because you booked a flight" is displayed

		EspressoUtils.assertViewIsDisplayed(R.id.air_attach_savings_text_view);
		//air attach savings text view is displayed

		EspressoUtils.assertViewIsDisplayed(R.id.air_attach_expires_text_view);
		//air attach expires text view is displayed

		//air attach expiration date is displayed
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.air_attach_expires_text_view, "Offer expires in");
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.air_attach_expiration_date_text_view, "10 days");

		assertFalse(tripBucketHotelPrice.equals(tripBucketHotelPriceWithDiscount));
		//Successfully asserted hotel trip bucket price before and after flight confirmation are different

		Checkout.clickBookNextItem();

		Checkout.slideToPurchase();
		Checkout.enterCvv("111");
		Checkout.clickBookButton();
	}
}
