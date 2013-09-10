package com.expedia.bookings.test.tests.hotels;

import com.expedia.bookings.test.utils.HotelsTestDriver;
import com.expedia.bookings.test.utils.HotelsUserData;

public class HotelsHappyPath {

	private static final String TAG = "Hotels Happy Path";

	public static void execute(HotelsTestDriver driver, HotelsUserData user, int numberOfHotelsToLookAt)
			throws Exception {
		try {
			driver.landscape();
			driver.portrait();

			try {
				driver.sweepstakesScreen().clickNoThanksButton();
			}
			catch (Throwable e) {
				driver.enterLog(TAG, "No sweepstakes activity to interact with!");
			}

			// Settings 
			driver.launchScreen().openMenuDropDown();
			driver.launchScreen().pressSettings();
			driver.settingsScreen().clickToClearPrivateData();
			driver.settingsScreen().clickOKString();
			driver.settingsScreen().clickOKString();
			driver.settingsScreen().setSpoofBookings();
			driver.settingsScreen().goBack();

			driver.launchScreen().openMenuDropDown();
			driver.launchScreen().pressSettings();
			driver.settingsScreen().clickSelectAPIString();
			driver.settingsScreen().scrollUp();
			driver.settingsScreen().clickOnText(user.mServerName);
			if (user.mServerName.equals("Proxy") || user.mServerName.equals("Mock Server")) {
				driver.settingsScreen().clickServerProxyAddressString();
				driver.settingsScreen().clearServerEditText();
				driver.settingsScreen().enterServerText(user.mProxyIP + ":" + user.mProxyPort);
				driver.settingsScreen().clickOKString();
			}
			driver.settingsScreen().goBack();

			// Launch screen
			driver.landscape();
			driver.portrait();
			driver.screenshot("Launch Screen");
			driver.launchScreen().launchHotels();

			// Search screen
			driver.delay(1);
			driver.screenshot("Search Screen");
			driver.landscape();
			driver.portrait();
			driver.hotelsSearchScreen().clickSearchEditText();
			driver.hotelsSearchScreen().clickToClearSearchEditText();
			driver.hotelsSearchScreen().enterSearchText(user.mHotelSearchCity);
			driver.clickInList(1);

			// Calendar
			driver.hotelsSearchScreen().clickOnCalendarButton();
			driver.delay(1);
			driver.screenshot("Calendar fragment");
			driver.landscape();
			driver.portrait();
			driver.hotelsSearchScreen().clickDate(2);

			// Guest Picker
			driver.hotelsSearchScreen().clickOnGuestsButton();
			driver.hotelsSearchScreen().guestPicker().clickIncrementAdultsButton();
			driver.hotelsSearchScreen().guestPicker().clickIncrementChildrenButton();
			driver.screenshot("Guest Picker fragment");
			driver.landscape();
			driver.portrait();
			driver.hotelsSearchScreen().guestPicker().clickOnSearchButton();

			// Sort
			driver.waitForStringToBeGone(driver.hotelsSearchScreen().searchingForHotels());
			driver.hotelsSearchScreen().clickOnSortButton();
			driver.delay(1);
			driver.screenshot("Sort fragment");
			driver.hotelsSearchScreen().sortMenu().clickSortByPopularityButton();

			// Filter
			driver.hotelsSearchScreen().clickOnFilterButton();
			driver.screenshot("Filter fragment");
			driver.landscape();
			driver.portrait();
			driver.goBack();

			// Iterate through hotels
			for (int i = 0; i < numberOfHotelsToLookAt / 4; i++) {
				for (int j = 0; j < 4; j++) {
					driver.waitForStringToBeGone(driver.hotelsSearchScreen().searchingForHotels());
					driver.hotelsSearchScreen().selectHotelFromList(j);
					driver.landscape();
					driver.portrait();
					if (!driver.hotelsDetailsScreen().reviewsTitle().getText().toString()
							.equals(driver.hotelsDetailsScreen().noReviews())) {
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
			driver.hotelsDetailsScreen().clickSelectButton();

			// Rooms and rates
			driver.waitForStringToBeGone(driver.hotelsRoomsRatesScreen().findingAvailableRooms());
			driver.screenshot("Rooms and rates");
			driver.landscape();
			driver.portrait();
			driver.hotelsRoomsRatesScreen().selectRoom(0);
			driver.waitForStringToBeGone(driver.commonCheckout().calculatingTaxesAndFees());

			// Checkout
			driver.commonCheckout().clickCheckoutButton();
			if (user.mLogInForCheckout) {
				// Log in
				driver.commonCheckout().clickLogInButton();
				driver.screenshot("Login Screen");
				driver.logInScreen().typeTextEmailEditText(user.mLoginEmail);
				driver.logInScreen().typeTextPasswordEditText(user.mLoginPassword);
				driver.logInScreen().clickOnLoginButton();
				driver.waitForStringToBeGone(driver.logInScreen().loggingInDialogString());

				// Enter payment as logged in user
				driver.commonCheckout().clickSelectPaymentButton();
				driver.scrollToBottom();
				driver.commonPaymentMethodScreen().clickOnAddNewCardTextView();
				driver.screenshot("Card info");
				driver.landscape();
				driver.portrait();
				driver.cardInfoScreen().typeTextCreditCardEditText(user.mCreditCardNumber);
				driver.billingAddressScreen().typeTextPostalCode(user.mZIPCode);
				driver.cardInfoScreen().typeTextNameOnCardEditText(user.mFirstName + " " + user.mLastName);
				driver.cardInfoScreen().clickOnExpirationDateButton();
				driver.delay(1);
				driver.screenshot("Expiration date fragment");
				driver.landscape();
				driver.portrait();
				driver.cardInfoScreen().clickMonthUpButton();
				driver.cardInfoScreen().clickYearUpButton();
				driver.cardInfoScreen().clickSetButton();
				driver.cardInfoScreen().clickOnDoneButton();
				driver.cardInfoScreen().clickNoThanksButton();
			}
			else {
				// Manually add traveler
				driver.screenshot("Add traveler screen");
				driver.landscape();
				driver.portrait();
				driver.commonCheckout().clickAddTravelerButton();
				driver.selectTravelerScreen().clickEnterInfoManuallyButton();
				driver.screenshot("Traveler information");
				driver.landscape();
				driver.portrait();
				driver.travelerInformationScreen().enterFirstName(user.mFirstName);
				driver.travelerInformationScreen().enterLastName(user.mLastName);
				driver.travelerInformationScreen().enterPhoneNumber(user.mPhoneNumber);
				driver.travelerInformationScreen().clickDoneButton();

				// Select payment as guest user
				driver.commonCheckout().clickSelectPaymentButton();
				driver.screenshot("Card info");
				driver.landscape();
				driver.portrait();
				driver.cardInfoScreen().typeTextCreditCardEditText(user.mCreditCardNumber);
				driver.cardInfoScreen().clickOnExpirationDateButton();
				driver.delay(1);
				driver.screenshot("Credit card expiration fragment");
				driver.landscape();
				driver.portrait();
				driver.cardInfoScreen().clickMonthUpButton();
				driver.cardInfoScreen().clickYearUpButton();
				driver.cardInfoScreen().clickSetButton();
				driver.cardInfoScreen().typeTextPostalCode(user.mZIPCode);
				driver.cardInfoScreen().typeTextNameOnCardEditText(user.mFirstName + " " + user.mLastName);
				driver.cardInfoScreen().typeTextEmailEditText(user.mLoginEmail);
				driver.cardInfoScreen().clickOnDoneButton();
			}

			try {
				driver.commonCheckout().clickCheckoutButton();
			}
			catch (Throwable t) {
				driver.enterLog(TAG, "No checkout button to click.");
			}
			// Slide to checkout
			driver.screenshot("Slide to checkout");
			driver.landscape();
			driver.portrait();
			driver.commonCheckout().slideToCheckout();
			driver.delay();

			// CVV Entry
			driver.screenshot("CVV Entry");
			driver.cvvEntryScreen().parseAndEnterCVV(user.mCCV);
			driver.cvvEntryScreen().clickBookButton();
			driver.delay(1);
			driver.waitForStringToBeGone(driver.cvvEntryScreen().booking());
			driver.delay(1);
			driver.screenshot("Confirmation Screen");
			driver.landscape();
			driver.portrait();
			driver.hotelsConfirmationScreen().clickDoneButton();
			driver.launchScreen().pressShop();
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
