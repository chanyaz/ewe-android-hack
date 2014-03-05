package com.expedia.bookings.test.tests.localization;

import java.util.Locale;

import com.expedia.bookings.activity.LaunchActivity;
import com.expedia.bookings.test.utils.CustomActivityInstrumentationTestCase;
import com.expedia.bookings.utils.ClearPrivateDataUtil;
import com.mobiata.android.util.SettingUtils;

public class HotelsMarketingSweep extends CustomActivityInstrumentationTestCase<LaunchActivity> {

	private static final String TAG = HotelsMarketingSweep.class.getSimpleName();

	public HotelsMarketingSweep() {
		super(LaunchActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		mPreferences.setScreenshotPermission(true);
		mPreferences.setRotationPermission(false);
		mUser.setFirstName("John");
		mUser.setLastName("Smith");
		mUser.setAddressLine1("543 Belle Lane");
		mUser.setHotelDestinationToLocaleDefault(Locale.getDefault());
		ClearPrivateDataUtil.clear(mDriver.getCurrentActivity());
		SettingUtils.save(mDriver.getCurrentActivity().getApplicationContext(),
			"suppress_hotel_bookings", true);
	}

	public void testMethod() throws Exception {
		mDriver.delay(15);
		// Launch screen
		mDriver.screenshot("Launch Screen");
		mDriver.delay(2);
		mDriver.screenshot("Launch Screen 2");
		mDriver.delay(2);
		mDriver.screenshot("Launch Screen 3");
		mDriver.delay(2);
		mDriver.screenshot("Launch Screen 4");
		mDriver.enterLog(TAG, "Launching hotels application");
		mDriver.launchScreen().launchHotels();
		mDriver.delay();

		// Hotel search screen
		mDriver.hotelsSearchScreen().clickSearchEditText();
		mDriver.hotelsSearchScreen().clickToClearSearchEditText();
		mDriver.enterLog(TAG, "Setting hotel search city to: " + mUser.getHotelSearchCity());
		mDriver.hotelsSearchScreen().enterSearchText(mUser.getHotelSearchCity());

		mDriver.hotelsSearchScreen().clickOnCalendarButton();
		mDriver.delay(1);
		int dateOffset = 20;
		mDriver.enterLog(TAG, "Selecting date with offset from current date of: " + dateOffset);
		mDriver.hotelsSearchScreen().clickDate(dateOffset);
		mDriver.hotelsSearchScreen().clickDate(dateOffset + 1);
		mDriver.hotelsSearchScreen().clickOnGuestsButton();
		mDriver.hotelsSearchScreen().guestPicker().clickOnSearchButton();
		mDriver.delay();
		if (mDriver.searchText(mDriver.hotelsSearchScreen().didYouMean(), 1, false, true)) {
			mDriver.clickInList(0);
		}

		mDriver.waitForStringToBeGone(mDriver.hotelsSearchScreen().searchingForHotels());
		// Sort
		mDriver.hotelsSearchScreen().clickOnSortButton();
		mDriver.hotelsSearchScreen().sortMenu().clickSortByPopularityString();
		mDriver.hotelsSearchScreen().clickOnFilterButton();
		mDriver.hotelsSearchScreen().filterMenu().clickMediumRatingFilterButton();
		mDriver.goBack();
		mDriver.delay();
		mDriver.screenshot("Hotel Search Results");

		//Select hotel
		mDriver.hotelsSearchScreen().selectHotelFromList(0);
		mDriver.delay(10);
		mDriver.screenshot("Hotel details");
		mDriver.delay(1);
		mDriver.screenshot("Hotel details 2");
		mDriver.hotelsDetailsScreen().clickReviewsTitle();
		mDriver.delay();
		mDriver.screenshot("Reviews");
		mDriver.hotelsReviewsScreen().clickSelectButton();
		mDriver.delay();
		mDriver.screenshot("Hotel rooms and rates");
		mDriver.hotelsRoomsRatesScreen().selectRoom(0);
		mDriver.delay();
		mDriver.waitForStringToBeGone(mDriver.hotelsCheckoutScreen().calculatingTaxesAndFees());

		mDriver.screenshot("Hotels checkout overview");
		mDriver.hotelsCheckoutScreen().clickCheckoutButton();
		mDriver.delay(5);
		mDriver.screenshot("Hotels checkout 2");
		mDriver.delay();

		// Log in
		mDriver.hotelsCheckoutScreen().clickLogInButton();
		mDriver.delay();
		mDriver.hideSoftKeyboard();
		mDriver.delay(1);
		mDriver.screenshot("Log in screen");
		mDriver.goBack();
		mDriver.delay();

		// Open Traveler information
		mDriver.hotelsCheckoutScreen().clickAddTravelerButton();
		mDriver.selectTravelerScreen().clickEnterInfoManuallyButton();
		mDriver.delay();
		mDriver.hideSoftKeyboard();
		mDriver.delay(1);
		mDriver.screenshot("Traveler Details");
		mDriver.travelerInformationScreen().enterFirstName(mUser.getFirstName());
		mDriver.travelerInformationScreen().enterLastName(mUser.getLastName());
		mDriver.travelerInformationScreen().enterPhoneNumber(mUser.getPhoneNumber());
		mDriver.travelerInformationScreen().enterEmailAddress(mUser.getLoginEmail());
		mDriver.travelerInformationScreen().clickDoneButton();

		// Open billing information
		mDriver.hotelsCheckoutScreen().clickSelectPaymentButton();
		mDriver.hideSoftKeyboard();
		mDriver.delay(1);
		mDriver.screenshot("Payment Details");
		mDriver.enterLog(TAG, "Entering credit card with number: " + mUser.getCreditCardNumber());
		mDriver.cardInfoScreen().typeTextCreditCardEditText(mUser.getCreditCardNumber());
		mDriver.cardInfoScreen().typeTextPostalCode(mUser.getAddressPostalCode());
		mDriver.cardInfoScreen().clickOnExpirationDateButton();
		mDriver.enterLog(TAG, "Incrementing credit card exp. month and year by 1");
		mDriver.cardInfoScreen().clickMonthUpButton();
		mDriver.cardInfoScreen().clickYearUpButton();
		mDriver.cardInfoScreen().clickSetButton();
		mDriver.cardInfoScreen().typeTextNameOnCardEditText(mUser.getFirstName() + " " + mUser.getLastName());
		mDriver.cardInfoScreen().clickOnDoneButton();
		mDriver.delay();

		if (mDriver.searchText(mDriver.hotelsCheckoutScreen().acceptString(), 1, false, true)) {
			mDriver.screenshot("Accept TOS");
			mDriver.hotelsCheckoutScreen().clickOnAcceptString();
		}
		mDriver.screenshot("Slide to checkout");
		mDriver.hotelsCheckoutScreen().slideToCheckout();
		mDriver.delay();
		mDriver.screenshot("CVV Entry");
	}


}
