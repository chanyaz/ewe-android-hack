package com.expedia.bookings.test.tests.hotelsEspresso;

import java.util.Calendar;

import org.joda.time.LocalDate;

import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.BillingAddressScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.CVVEntryScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.CardInfoScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.CommonPaymentMethodScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.CommonSelectTravelerScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.CommonTravelerInformationScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.LaunchScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.LogInScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.ScreenActions;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsCheckoutScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsConfirmationScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsDetailsScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsRoomsRatesScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsSearchScreen;
import com.expedia.bookings.test.utils.HotelsUserData;

import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.pressBack;
import static com.google.android.apps.common.testing.ui.espresso.matcher.RootMatchers.withDecorView;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * Created by dmadan on 4/11/14.
 */
public class HotelsHappyPathE {

	private static final String TAG = HotelsHappyPathE.class.getSimpleName();

	public static void execute(HotelsUserData user, int numberOfHotelsToLookAt, SearchActivity s) throws Exception {

		// Launch screen
		ScreenActions.enterLog(TAG, "Launching hotels application");
		LaunchScreen.launchHotels();

		// Search screen
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		ScreenActions.enterLog(TAG, "Setting hotel search city to: " + "New York, NY");
		ScreenActions.enterLog(TAG, "HERE entering text");
		HotelsSearchScreen.enterSearchText("New York,NY");

		ScreenActions.enterLog(TAG, "HERE clicking suggestion");
		onView(withText("New York, NY")).inRoot(withDecorView(not(is(s.getWindow().getDecorView())))).perform(click());
		Calendar cal = Calendar.getInstance();
		int year = cal.get(cal.YEAR);
		int month = cal.get(cal.MONTH) + 1;
		int date = cal.get(cal.DATE);
		LocalDate mStartDate = new LocalDate(year, month, date + 3);
		LocalDate mEndDate = new LocalDate(year, month, date + 5);
		HotelsSearchScreen.clickOnCalendarButton();

		HotelsSearchScreen.clickDate(mStartDate, mEndDate);

		// Guest Picker
		HotelsSearchScreen.clickOnGuestsButton();
		ScreenActions.enterLog(TAG, "Incrementing number of adults and number of children by 1");
		HotelsSearchScreen.guestPicker().clickIncrementAdultsButton();
		HotelsSearchScreen.guestPicker().clickIncrementChildrenButton();
		ScreenActions.enterLog(TAG, "Incremented number of adults and number of children by 1");
		HotelsSearchScreen.guestPicker().clickOnSearchButton();

		// Sort
		ScreenActions.enterLog(TAG, "Hotel search results loaded");
		ScreenActions.enterLog(TAG, "Opened sort fragment");
		HotelsSearchScreen.clickOnSortButton();
		ScreenActions.enterLog(TAG, "clicked on sort button");

		HotelsSearchScreen.sortMenu().clickSortByPopularityString();
		ScreenActions.enterLog(TAG, "Popularity string clicked");
		pressBack();
		ScreenActions.enterLog(TAG, "Back button pressed");
		HotelsSearchScreen.selectHotelFromList();
		HotelsDetailsScreen.clickSelectButton();

		// Rooms and rates
		ScreenActions.enterLog(TAG, "Selecting first room listed for this hotel.");
		HotelsRoomsRatesScreen.selectRoom();

		// Checkout
		HotelsCheckoutScreen.clickCheckoutButton();
		if (user.getLogInForCheckout()) {
			ScreenActions.enterLog(TAG, "Logging in for this booking using email " + user.getLoginEmail());
			// Log in
			HotelsCheckoutScreen.clickLogInButton();
			LogInScreen.typeTextEmailEditText(user.getLoginEmail());
			LogInScreen.typeTextPasswordEditText(user.getLoginPassword());
			LogInScreen.clickOnLoginButton();

			// Enter payment as logged in user
			HotelsCheckoutScreen.clickSelectPaymentButton();
			ScreenActions.enterLog(TAG, "Using new credit card");
			try {
				CommonPaymentMethodScreen.clickOnAddNewCardTextView();
			}
			catch (Exception e) {
				ScreenActions.enterLog(TAG, "No Add New Card button. Proceeding anyway.");
			}

			ScreenActions.enterLog(TAG, "Entering credit card with number: " + user.getCreditCardNumber());
			CardInfoScreen.typeTextCreditCardEditText(user.getCreditCardNumber());
			ScreenActions.enterLog(TAG, "Entering postal code: " + user.getAddressPostalCode());
			BillingAddressScreen.typeTextPostalCode(user.getAddressPostalCode());
			ScreenActions.enterLog(TAG, "Entering cardholder name: " + user.getFirstName() + " " + user.getLastName());
			CardInfoScreen.typeTextNameOnCardEditText(user.getFirstName() + " " + user.getLastName());
			CardInfoScreen.clickOnExpirationDateButton();

			ScreenActions.enterLog(TAG, "Incrementing credit card exp. month and year by 1");
			CardInfoScreen.clickMonthUpButton();
			CardInfoScreen.clickYearUpButton();
			CardInfoScreen.clickSetButton();
			CardInfoScreen.clickOnDoneButton();
			CardInfoScreen.clickNoThanksButton();
		}
		else {
			// Manually add traveler
			ScreenActions.enterLog(TAG, "Manually entering traveler information");
			HotelsCheckoutScreen.clickAddTravelerButton();
			CommonSelectTravelerScreen.clickEnterInfoManuallyButton();
			ScreenActions.enterLog(TAG, "Entering first name: " + user.getFirstName());
			CommonTravelerInformationScreen.enterFirstName(user.getFirstName());
			ScreenActions.enterLog(TAG, "Entering last name: " + user.getLastName());
			CommonTravelerInformationScreen.enterLastName(user.getLastName());
			ScreenActions.enterLog(TAG, "Entering phone number: " + user.getPhoneNumber());
			CommonTravelerInformationScreen.enterPhoneNumber(user.getPhoneNumber());
			CommonTravelerInformationScreen.clickDoneButton();

			// Select payment as guest user
			HotelsCheckoutScreen.clickSelectPaymentButton();
			CardInfoScreen.typeTextCreditCardEditText(user.getCreditCardNumber());
			CardInfoScreen.clickOnExpirationDateButton();
			ScreenActions.enterLog(TAG, "Incrementing credit card exp. month and year by 1");
			CardInfoScreen.clickMonthUpButton();
			CardInfoScreen.clickYearUpButton();
			CardInfoScreen.clickSetButton();
			ScreenActions.enterLog(TAG, "Entering postal code: " + user.getAddressPostalCode());
			CardInfoScreen.typeTextPostalCode(user.getAddressPostalCode());
			ScreenActions.enterLog(TAG, "Entering cardholder name: " + user.getFirstName() + " " + user.getLastName());
			CardInfoScreen.typeTextNameOnCardEditText(user.getFirstName() + " " + user.getLastName());
			ScreenActions.enterLog(TAG, "Entering user email address: " + user.getLoginEmail());
			CardInfoScreen.typeTextEmailEditText(user.getLoginEmail());
			CardInfoScreen.clickOnDoneButton();
		}

		//Slide to purchase
		ScreenActions.enterLog(TAG, "Sliding to checkout");
		HotelsCheckoutScreen.slideToCheckout();
		ScreenActions.enterLog(TAG, "Checked out");

		// CVV Entry
		ScreenActions.enterLog(TAG, "Entering CCV: " + user.getCCV());
		CVVEntryScreen.parseAndEnterCVV(user.getCCV());
		CVVEntryScreen.clickBookButton();

		ScreenActions.enterLog(TAG, "Going back to launch screen.");
		HotelsConfirmationScreen.clickDoneButton();
		ScreenActions.enterLog(TAG, "Clicking shop tab");
		LaunchScreen.pressShop();
	}
}

