package com.expedia.bookings.test.tablet.tests.hotels;

import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.test.tablet.pagemodels.Checkout;
import com.expedia.bookings.test.tablet.pagemodels.Common;
import com.expedia.bookings.test.tablet.pagemodels.Confirmation;
import com.expedia.bookings.test.tablet.pagemodels.HotelDetails;
import com.expedia.bookings.test.tablet.pagemodels.Launch;
import com.expedia.bookings.test.tablet.pagemodels.Results;
import com.expedia.bookings.test.tablet.pagemodels.Search;
import com.expedia.bookings.test.utils.EspressoUtils;
import com.expedia.bookings.test.utils.TabletTestCase;

/**
 * Created by dmadan on 7/14/14.
 */
public class HotelConfirmationTests extends TabletTestCase {

	String mDateRange;

	private void getToCheckout() throws Exception {
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("Detroit, MI");
		Launch.clickSuggestion("Detroit, MI");
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);
		Search.clickDate(startDate, endDate);
		mDateRange = EspressoUtils.getText(R.id.calendar_btn);
		Search.clickSearchPopupDone();

		Results.swipeUpHotelList();
		Results.clickHotelWithName("happy_path_Hotel_Orchard");
		HotelDetails.clickAddHotel();
		Results.clickBookHotel();
	}

	//Testing confirmation screen
	public void testConfirmation() throws Exception {
		getToCheckout();
		Checkout.clickOnTravelerDetails();
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
		Checkout.enterPostalCode("95104");
		Common.closeSoftKeyboard(Checkout.postalCode());
		Checkout.clickOnDone();

		Checkout.slideToPurchase();
		Checkout.enterCvv("111");
		Checkout.clickBookButton();
		verifyConfirmationTexts();

		Checkout.clickDoneBooking();
	}

	private void verifyConfirmationTexts() {
		//verify hotel name
		EspressoUtils.assertContains(Confirmation.confirmationSummary(), "happy_path_Hotel_Orchard");

		//verify date range
		EspressoUtils.assertContains(Confirmation.confirmationSummary(), mDateRange);

		//verify itinerary #
		String expectedItineraryNumber = Db.getHotelBookingResponse().getItineraryId();
		EspressoUtils.assertContains(Confirmation.confirmationItinerary(), expectedItineraryNumber);

		//verify login email
		EspressoUtils.assertContains(Confirmation.confirmationItinerary(), "aaa@aaa.com");
	}
}
