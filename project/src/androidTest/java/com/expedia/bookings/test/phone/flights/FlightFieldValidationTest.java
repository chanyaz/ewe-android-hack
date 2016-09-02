package com.expedia.bookings.test.phone.flights;

import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.FlightTestCase;
import com.expedia.bookings.test.phone.pagemodels.common.BillingAddressScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CVVEntryScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CardInfoScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CommonCheckoutScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CommonTravelerInformationScreen;
import com.expedia.bookings.test.phone.pagemodels.common.ConfirmationScreen;
import com.expedia.bookings.test.phone.pagemodels.flights.FlightLegScreen;
import com.expedia.bookings.test.phone.pagemodels.flights.FlightsSearchResultsScreen;
import com.expedia.bookings.test.phone.pagemodels.flights.FlightsSearchScreen;
import com.expedia.bookings.test.phone.pagemodels.flights.FlightsTravelerInfoScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.expedia.bookings.test.espresso.ViewActions.setText;
import static com.expedia.bookings.test.phone.pagemodels.common.CommonCheckoutScreen.clickCheckoutButton;
import static com.expedia.bookings.test.phone.pagemodels.common.CommonCheckoutScreen.clickNewPaymentCard;
import static com.expedia.bookings.test.phone.pagemodels.common.CommonCheckoutScreen.clickSelectPaymentButton;
import static com.expedia.bookings.test.phone.pagemodels.common.CommonCheckoutScreen.clickTravelerDetails;
import static com.expedia.bookings.test.phone.pagemodels.common.CommonCheckoutScreen.slideToCheckout;

public class FlightFieldValidationTest extends FlightTestCase {

	/*
 	* #373 eb_tp test plan
 	*/

	public void testFieldValidation() throws Throwable {
		FlightsSearchScreen.enterDepartureAirport("LAX");
		FlightsSearchScreen.enterArrivalAirport("SFO");
		FlightsSearchScreen.clickSelectDepartureButton();
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(36);
		FlightsSearchScreen.clickDate(startDate, endDate);
		FlightsSearchScreen.clickSearchButton();
		FlightsSearchResultsScreen.clickListItem(1);
		FlightLegScreen.clickSelectFlightButton();
		FlightsSearchResultsScreen.clickListItem(1);
		FlightLegScreen.clickSelectFlightButton();
		clickCheckoutButton();

		clickTravelerDetails();

		//test field validation for multibyte character
		onView(withId(R.id.edit_first_name)).perform(setText("ш"));
		assertPopup();
		onView(withId(R.id.edit_last_name)).perform(setText("ш"));
		assertPopup();

		FlightsTravelerInfoScreen.enterFirstName("Mobiata");
		FlightsTravelerInfoScreen.enterLastName("Auto");
		FlightsTravelerInfoScreen.enterPhoneNumber("1112223333");
		Common.closeSoftKeyboard(FlightsTravelerInfoScreen.phoneNumberEditText());
		FlightsTravelerInfoScreen.clickBirthDateButton();
		try {
			FlightsTravelerInfoScreen.clickSetButton();
		}
		catch (Exception e) {
			CommonTravelerInformationScreen.clickDoneString();
		}
		BillingAddressScreen.clickNextButton();
		FlightsTravelerInfoScreen.selectGender("Male");
		FlightsTravelerInfoScreen.clickDoneButton();
		Common.pressBack();
		clickCheckoutButton();
		clickSelectPaymentButton();
		try {
			clickNewPaymentCard();
		}
		catch (Exception e) {
			// No add new card option
		}
		try {
			//test field validation for multibyte character
			onView(withId(R.id.edit_address_line_one)).perform(setText("ш"));
			assertPopup();
			onView(withId(R.id.edit_address_city)).perform(setText("ш"));
			assertPopup();
			onView(withId(R.id.edit_address_postal_code)).perform(setText("ш"));
			assertPopup();

			BillingAddressScreen.typeTextAddressLineOne("123 California Street");
			BillingAddressScreen.typeTextCity("San Francisco");
			BillingAddressScreen.typeTextState("CA");
			BillingAddressScreen.typeTextPostalCode("94105");
			BillingAddressScreen.clickNextButton();
		}
		catch (Exception e) {
			//Billing address not needed
		}
		CardInfoScreen.typeTextCreditCardEditText("4111111111111111");
		Common.closeSoftKeyboard(CardInfoScreen.creditCardNumberEditText());
		CardInfoScreen.clickOnExpirationDateButton();
		CardInfoScreen.clickMonthUpButton();
		CardInfoScreen.clickYearUpButton();
		CardInfoScreen.clickSetButton();

		//test field validation for multibyte character
		onView(withId(R.id.edit_name_on_card)).perform(setText("ш"));
		assertPopup();
		onView(withId(R.id.edit_email_address)).perform(setText("ш"));
		assertPopup();

		CardInfoScreen.typeTextNameOnCardEditText("Mobiata Auto");
		CardInfoScreen.typeTextEmailEditText("mobiataauto@gmail.com");
		CardInfoScreen.clickOnDoneButton();
		try {
			CommonCheckoutScreen.clickIAcceptButton();
		}
		catch (Exception e) {
			//No I accept
		}
		slideToCheckout();
		CVVEntryScreen.enterCVV("111");
		CVVEntryScreen.clickBookButton();
		ConfirmationScreen.clickDoneButton();
	}

	private void assertPopup() {
		EspressoUtils.assertViewWithTextIsDisplayed(mRes.getString(R.string.please_use_the_roman_alphabet));
		Common.clickOkString();
	}
}
