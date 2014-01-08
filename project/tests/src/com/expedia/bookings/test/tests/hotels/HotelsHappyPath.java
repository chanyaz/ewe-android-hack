package com.expedia.bookings.test.tests.hotels;

import com.expedia.bookings.test.utils.HotelsTestDriver;
import com.expedia.bookings.test.utils.HotelsUserData;

public class HotelsHappyPath {

	private static final String TAG = HotelsHappyPath.class.getSimpleName();

	public static void execute(HotelsTestDriver driver, HotelsUserData user, int numberOfHotelsToLookAt)
			throws Exception {
		try {

			driver.delay();
			driver.launchScreen().openMenuDropDown();
			try {
				driver.launchScreen().pressSettings();
			}
			catch (Error e) {
				driver.clickInList(0);
			}
			driver.settingsScreen().clickToClearPrivateData();
			if (driver.searchText(driver.settingsScreen().OKString())) {
				driver.settingsScreen().clickOKString();
			}
			else if (driver.searchText(driver.settingsScreen().AcceptString())) {
				driver.settingsScreen().clickAcceptString();
			}
			else {
				driver.clickOnText("OK");
			}
			driver.delay();
			if (driver.searchText(driver.settingsScreen().OKString())) {
				driver.settingsScreen().clickOKString();
			}
			else if (driver.searchText(driver.settingsScreen().AcceptString())) {
				driver.settingsScreen().clickAcceptString();
			}
			else {
				driver.clickOnText("OK");
			}
			driver.settingsScreen().setSpoofBookings();
			driver.settingsScreen().goBack();
			driver.enterLog(TAG, "Cleared private data and set spoof/suppress bookings");

			driver.launchScreen().openMenuDropDown();
			driver.launchScreen().pressSettings();
			driver.settingsScreen().clickSelectAPIString();
			driver.settingsScreen().scrollUp();
			driver.settingsScreen().clickOnText(user.getBookingServer());
			driver.enterLog(TAG, "Set booking server to: " + user.getBookingServer());
			if (user.getBookingServer().equals("Proxy") || user.getBookingServer().equals("Mock Server")) {
				driver.settingsScreen().clickServerProxyAddressString();
				driver.settingsScreen().clearServerEditText();
				driver.settingsScreen().enterServerText(user.getServerIP() + ":" + user.getServerPort());
				driver.settingsScreen().clickOKString();
			}
			driver.settingsScreen().goBack();

			// Launch screen
			driver.screenshot("Launch Screen");
			driver.enterLog(TAG, "Launching hotels application");
			driver.launchScreen().launchHotels();

			// Search screen
			driver.delay(1);
			driver.screenshot("Search Screen");

			driver.landscape();
			driver.portrait();
			driver.hotelsSearchScreen().clickSearchEditText();
			driver.hotelsSearchScreen().clickToClearSearchEditText();
			driver.enterLog(TAG, "Setting hotel search city to: " + user.getHotelSearchCity());
			driver.hotelsSearchScreen().enterSearchText(user.getHotelSearchCity());
			driver.clickInList(1);

			// Calendar
			driver.hotelsSearchScreen().clickOnCalendarButton();
			driver.delay(1);
			driver.screenshot("Calendar fragment");
			driver.landscape();
			driver.portrait();
			int dateOffset = 2;
			driver.enterLog(TAG, "Selecting date with offset from current date of: " + dateOffset);
			driver.hotelsSearchScreen().clickDate(dateOffset);

			// Guest Picker
			driver.hotelsSearchScreen().clickOnGuestsButton();
			driver.enterLog(TAG, "Incrementing number of adults and number of children by 1");
			driver.hotelsSearchScreen().guestPicker().clickIncrementAdultsButton();
			driver.hotelsSearchScreen().guestPicker().clickIncrementChildrenButton();
			driver.screenshot("Guest Picker fragment");
			driver.landscape();
			driver.portrait();
			driver.hotelsSearchScreen().guestPicker().clickOnSearchButton();

			// Sort
			driver.waitForStringToBeGone(driver.hotelsSearchScreen().searchingForHotels());
			driver.delay();
			driver.enterLog(TAG, "Hotel search results loaded");
			driver.hotelsSearchScreen().clickOnSortButton();
			driver.delay(1);
			driver.screenshot("Sort fragment");
			driver.enterLog(TAG, "Opened sort fragment");
			if (driver.searchText(driver.hotelsSearchScreen().sortMenu().getSortByPopularityString())) {
				driver.hotelsSearchScreen().sortMenu().clickSortByPopularityString();
			}
			else {
				driver.hotelsSearchScreen().clickOnSortButton();
				driver.delay(1);
				driver.hotelsSearchScreen().sortMenu().clickSortByPopularityString();
			}

			// Filter
			driver.hotelsSearchScreen().clickOnFilterButton();
			driver.enterLog(TAG, "Opened filter fragment");
			driver.screenshot("Filter fragment");
			driver.landscape();
			driver.portrait();
			driver.goBack();

			// Iterate through hotels
			driver.enterLog(TAG, "Starting hotel iteration. Will iterate through: " + numberOfHotelsToLookAt
					+ " hotels");
			for (int i = 0; i < numberOfHotelsToLookAt / 4; i++) {
				for (int j = 0; j < 4; j++) {
					driver.waitForStringToBeGone(driver.hotelsSearchScreen().searchingForHotels());
					driver.hotelsSearchScreen().selectHotelFromList(j);
					driver.landscape();
					driver.portrait();
					String hotelName = driver.hotelsDetailsScreen().titleView().getText().toString();
					driver.enterLog(TAG, "Selected hotel: " + hotelName);
					if (!driver.hotelsDetailsScreen().reviewsTitle().getText().toString()
							.equals(driver.hotelsDetailsScreen().noReviews())) {
						driver.enterLog(TAG, "Looking at hotel's reviews.");
						driver.hotelsDetailsScreen().clickReviewsTitle();
						driver.waitForStringToBeGone(driver.hotelsReviewsScreen().loadingUserReviews());
						driver.screenshot("Reviews screen");
						driver.hotelsReviewsScreen().clickCriticalTab();
						driver.hotelsReviewsScreen().clickRecentTab();
						driver.hotelsReviewsScreen().clickFavorableTab();
						driver.hotelsReviewsScreen().clickBackButton();
						driver.delay(1);
					}
					driver.goBack();
				}
				driver.scrollDown();
			}

			driver.hotelsSearchScreen().selectHotelFromList(0);
			driver.delay(1);
			driver.screenshot("Hotel Details");
			String hotelName = driver.hotelsDetailsScreen().titleView().getText().toString();
			driver.enterLog(TAG, "Reselecting hotel: " + hotelName);
			driver.hotelsDetailsScreen().clickSelectButton();

			// Rooms and rates
			driver.waitForStringToBeGone(driver.hotelsRoomsRatesScreen().findingAvailableRooms());
			driver.enterLog(TAG, "On rooms and rates screen for hotel: " + hotelName);
			driver.screenshot("Rooms and rates");
			driver.landscape();
			driver.portrait();
			driver.enterLog(TAG, "Selecting first room listed for this hotel.");
			driver.hotelsRoomsRatesScreen().selectRoom(0);
			driver.delay();
			driver.waitForStringToBeGone(driver.hotelsCheckoutScreen().calculatingTaxesAndFees());

			// Checkout
			driver.hotelsCheckoutScreen().clickCheckoutButton();
			driver.delay();
			if (user.getLogInForCheckout()) {
				driver.enterLog(TAG, "Logging in for this booking using email " + user.getLoginEmail());
				// Log in
				driver.hotelsCheckoutScreen().clickLogInButton();
				driver.screenshot("Login Screen");
				driver.logInScreen().typeTextEmailEditText(user.getLoginEmail());
				driver.logInScreen().typeTextPasswordEditText(user.getLoginPassword());
				driver.logInScreen().clickOnLoginButton();
				driver.waitForStringToBeGone(driver.logInScreen().loggingInDialogString());

				// Enter payment as logged in user
				driver.hotelsCheckoutScreen().clickSelectPaymentButton();
				driver.scrollToBottom();
				driver.enterLog(TAG, "Using new credit card");
				try {
					driver.commonPaymentMethodScreen().clickOnAddNewCardTextView();
				}
				catch (Exception e) {
					driver.enterLog(TAG, "No Add New Card button. Proceeding anyway.");
				}
				driver.screenshot("Card info");
				driver.landscape();
				driver.portrait();
				driver.enterLog(TAG, "Entering credit card with number: " + user.getCreditCardNumber());
				driver.cardInfoScreen().typeTextCreditCardEditText(user.getCreditCardNumber());
				driver.enterLog(TAG, "Entering postal code: " + user.getAddressPostalCode());
				driver.billingAddressScreen().typeTextPostalCode(user.getAddressPostalCode());
				driver.enterLog(TAG, "Entering cardholder name: " + user.getFirstName() + " " + user.getLastName());
				driver.cardInfoScreen().typeTextNameOnCardEditText(user.getFirstName() + " " + user.getLastName());
				driver.cardInfoScreen().clickOnExpirationDateButton();
				driver.delay(1);
				driver.screenshot("Expiration date fragment");
				driver.landscape();
				driver.portrait();
				driver.enterLog(TAG, "Incrementing credit card exp. month and year by 1");
				driver.cardInfoScreen().clickMonthUpButton();
				driver.cardInfoScreen().clickYearUpButton();
				driver.cardInfoScreen().clickSetButton();
				driver.cardInfoScreen().clickOnDoneButton();
				driver.cardInfoScreen().clickNoThanksButton();
			}
			else {
				// Manually add traveler
				driver.enterLog(TAG, "Manually entering traveler information");
				driver.screenshot("Add traveler screen");
				driver.landscape();
				driver.portrait();
				driver.hotelsCheckoutScreen().clickAddTravelerButton();
				driver.selectTravelerScreen().clickEnterInfoManuallyButton();
				driver.screenshot("Traveler information");
				driver.landscape();
				driver.portrait();
				driver.enterLog(TAG, "Entering first name: " + user.getFirstName());
				driver.travelerInformationScreen().enterFirstName(user.getFirstName());
				driver.enterLog(TAG, "Entering last name: " + user.getLastName());
				driver.travelerInformationScreen().enterLastName(user.getLastName());
				driver.enterLog(TAG, "Entering phone number: " + user.getPhoneNumber());
				driver.travelerInformationScreen().enterPhoneNumber(user.getPhoneNumber());
				driver.travelerInformationScreen().clickDoneButton();

				// Select payment as guest user
				driver.hotelsCheckoutScreen().clickSelectPaymentButton();
				driver.screenshot("Card info");
				driver.landscape();
				driver.portrait();
				driver.cardInfoScreen().typeTextCreditCardEditText(user.getCreditCardNumber());
				driver.cardInfoScreen().clickOnExpirationDateButton();
				driver.delay(1);
				driver.screenshot("Credit card expiration fragment");
				driver.landscape();
				driver.portrait();
				driver.enterLog(TAG, "Incrementing credit card exp. month and year by 1");
				driver.cardInfoScreen().clickMonthUpButton();
				driver.cardInfoScreen().clickYearUpButton();
				driver.cardInfoScreen().clickSetButton();
				driver.enterLog(TAG, "Entering postal code: " + user.getAddressPostalCode());
				driver.cardInfoScreen().typeTextPostalCode(user.getAddressPostalCode());
				driver.enterLog(TAG, "Entering cardholder name: " + user.getFirstName() + " " + user.getLastName());
				driver.cardInfoScreen().typeTextNameOnCardEditText(user.getFirstName() + " " + user.getLastName());
				driver.enterLog(TAG, "Entering user email address: " + user.getLoginEmail());
				driver.cardInfoScreen().typeTextEmailEditText(user.getLoginEmail());
				driver.cardInfoScreen().clickOnDoneButton();
			}

			try {
				driver.hotelsCheckoutScreen().clickCheckoutButton();
			}
			catch (Throwable t) {
				driver.enterLog(TAG, "No checkout button to click.");
			}

			driver.landscape();
			driver.portrait();
			if (driver.searchText(driver.hotelsCheckoutScreen().acceptString(), 1, false, true)) {
				driver.hotelsCheckoutScreen().clickOnAcceptString();
			}
			driver.screenshot("Slide to checkout");
			driver.enterLog(TAG, "Sliding to checkout");
			driver.hotelsCheckoutScreen().slideToCheckout();
			driver.delay();

			// CVV Entry
			driver.screenshot("CVV Entry");
			driver.enterLog(TAG, "Entering CCV: " + user.getCCV());
			driver.cvvEntryScreen().parseAndEnterCVV(user.getCCV());
			driver.cvvEntryScreen().clickBookButton();
			driver.delay(1);
			driver.waitForStringToBeGone(driver.cvvEntryScreen().booking());
			driver.delay(1);
			driver.screenshot("Confirmation Screen");
			driver.landscape();
			driver.portrait();
			driver.enterLog(TAG, "Going back to launch screen.");
			driver.hotelsConfirmationScreen().clickDoneButton();
			driver.enterLog(TAG, "Clicking shop tab");
			driver.tripsScreen().swipeToLaunchScreen();
		}
		catch (Error e) {
			driver.getScreenShotUtility().screenshot(TAG + "-FAILURE");
			throw e;
		}
		catch (Exception e) {
			driver.getScreenShotUtility().screenshot(TAG + "-FAILURE");
			throw e;
		}
	}
}
