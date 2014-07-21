package com.expedia.bookings.test.tests.hotelsEspresso;

import java.util.Calendar;

import org.joda.time.LocalDate;

import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.CVVEntryScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.CardInfoScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.CommonTravelerInformationScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.LaunchScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.ScreenActions;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.SettingsScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsCheckoutScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsConfirmationScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsDetailsScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsGuestPicker;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsRoomsRatesScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsSearchScreen;
import com.expedia.bookings.test.utils.HotelsUserData;
import com.google.android.apps.common.testing.ui.espresso.Espresso;

import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.pressBack;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;

/**
 * Created by dmadan on 4/11/14.
 */
public class HotelsHappyPathE {

	private static final String TAG = HotelsHappyPathE.class.getSimpleName();

	public static void execute(HotelsUserData user, SearchActivity activity) throws Exception {

		//Settings
		LaunchScreen.openMenuDropDown();
		LaunchScreen.pressSettings();

		SettingsScreen.clickStubConfigPage();
		SettingsScreen.clickHotelCheckoutScenario();
		Espresso.pressBack();

		// Launch screen
		ScreenActions.enterLog(TAG, "Launching hotels application");
		LaunchScreen.launchHotels();

		// Search screen
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		ScreenActions.enterLog(TAG, "Setting hotel search city to: " + "New York, NY");
		HotelsSearchScreen.enterSearchText("New York, NY");

		ScreenActions.enterLog(TAG, "Clicking suggestion");
		HotelsSearchScreen.clickSuggestion(activity, "New York, NY");
		Calendar cal = Calendar.getInstance();
		int year = cal.get(cal.YEAR);
		int month = cal.get(cal.MONTH) + 1;
		LocalDate mStartDate = new LocalDate(year, month, 5);
		LocalDate mEndDate = new LocalDate(year, month, 10);
		HotelsSearchScreen.clickOnCalendarButton();
		HotelsSearchScreen.clickDate(mStartDate, mEndDate);

		// Guest Picker
		HotelsSearchScreen.clickOnGuestsButton();
		HotelsGuestPicker.incrementAdultsButton();
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
		HotelsSearchScreen.clickListItem(1);
		HotelsDetailsScreen.clickSelectButton();

		// Rooms and rates
		ScreenActions.enterLog(TAG, "Selecting first room listed for this hotel.");
		HotelsRoomsRatesScreen.selectRoomItem(0);
		try {
			SettingsScreen.clickOKString();
		}
		catch (Exception e) {
			ScreenActions.enterLog(TAG, "No popup.");
		}

		// Checkout
		HotelsCheckoutScreen.clickCheckoutButton();

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
		CardInfoScreen.clickOnDoneButton();

		// Manually add traveler
		onView(withText("Guest details")).perform(click());
		ScreenActions.enterLog(TAG, "Manually entering traveler information");
		HotelsCheckoutScreen.clickAddTravelerButton();
		ScreenActions.enterLog(TAG, "Entering first name: " + user.getFirstName());
		CommonTravelerInformationScreen.enterFirstName(user.getFirstName());
		ScreenActions.enterLog(TAG, "Entering last name: " + user.getLastName());
		CommonTravelerInformationScreen.enterLastName(user.getLastName());
		ScreenActions.enterLog(TAG, "Entering phone number: " + user.getPhoneNumber());
		CommonTravelerInformationScreen.enterPhoneNumber(user.getPhoneNumber());
		CommonTravelerInformationScreen.enterEmailAddress(user.getLoginEmail());
		CommonTravelerInformationScreen.clickDoneButton();

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

