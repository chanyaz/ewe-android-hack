package com.expedia.bookings.test.ui.phone.tests.hotels;

import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CVVEntryScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CardInfoScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CommonPaymentMethodScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LogInScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.ui.phone.pagemodels.common.SettingsScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsCheckoutScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsDetailsScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsGuestPicker;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsRoomsRatesScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsSearchScreen;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.HotelsUserData;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import com.mobiata.android.util.SettingUtils;

import static android.support.test.espresso.action.ViewActions.pressBack;

/**
 * Created by dmadan on 4/22/14.
 */
public class HotelsCheckoutNonMerchant extends PhoneTestCase {

	private static final String TAG = "HotelsCheckoutNonMerchant";

	private HotelsUserData mUser;

	protected void setUp() throws Exception {
		super.setUp();
		mUser = new HotelsUserData();
		SettingUtils.save(getInstrumentation().getTargetContext(), R.string.preference_filter_merchant_properties, true);
	}

	public void testMethod() throws Exception {

		// Launch screen
		ScreenActions.enterLog(TAG, "Launching hotels application");
		LaunchScreen.launchHotels();

		// Search screen
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		ScreenActions.enterLog(TAG, "Setting hotel search city to: " + "Las Vegas, NV");
		HotelsSearchScreen.enterSearchText("New York, NY");

		ScreenActions.enterLog(TAG, "Clicking suggestion");
		HotelsSearchScreen.clickSuggestionWithName(getActivity(), "New York, NY");
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);
		HotelsSearchScreen.clickOnCalendarButton();
		HotelsSearchScreen.clickDate(startDate, endDate);

		// Guest Picker
		HotelsSearchScreen.clickOnGuestsButton();
		HotelsGuestPicker.incrementAdultsButton();
		HotelsSearchScreen.guestPicker().clickOnSearchButton();

		// Sort
		ScreenActions.enterLog(TAG, "Opened sort fragment");
		HotelsSearchScreen.clickOnSortButton();
		ScreenActions.enterLog(TAG, "clicked on sort button");
		HotelsSearchScreen.clickSortByPopularity();
		pressBack();
		HotelsSearchScreen.clickListItem(1);
		HotelsDetailsScreen.clickSelectButton();

		// Rooms and rates
		ScreenActions.enterLog(TAG, "Selecting first room listed for this hotel.");
		HotelsRoomsRatesScreen.selectETPRoomItem(1);
		try {
			SettingsScreen.clickOkString();
		}
		catch (Exception e) {
			ScreenActions.enterLog(TAG, "OK popup");
		}

		// Checkout
		HotelsCheckoutScreen.clickCheckoutButton();

		// Log in
		HotelsCheckoutScreen.clickLogInButton();
		LogInScreen.typeTextEmailEditText(mUser.email);
		LogInScreen.typeTextPasswordEditText(mUser.password);
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

		// Select payment as guest user
		CardInfoScreen.typeTextCreditCardEditText(mUser.creditCardNumber);
		CardInfoScreen.clickOnExpirationDateButton();
		ScreenActions.enterLog(TAG, "Incrementing credit card exp. month and year by 1");
		CardInfoScreen.clickMonthUpButton();
		CardInfoScreen.clickYearUpButton();
		CardInfoScreen.clickSetButton();
		ScreenActions.enterLog(TAG, "Entering postal code: " + mUser.zipcode);
		CardInfoScreen.typeTextPostalCode(mUser.zipcode);
		ScreenActions.enterLog(TAG, "Entering cardholder name: " + mUser.firstName + " " + mUser.lastName);
		CardInfoScreen.typeTextNameOnCardEditText(mUser.firstName + " " + mUser.lastName);
		CardInfoScreen.clickOnDoneButton();
		CardInfoScreen.clickNoThanksButton();

		//Slide to purchase
		ScreenActions.enterLog(TAG, "Sliding to checkout");
		HotelsCheckoutScreen.slideToCheckout();

		// CVV Entry
		ScreenActions.enterLog(TAG, "Entering CCV: " + mUser.cvv);
		CVVEntryScreen.parseAndEnterCVV(mUser.cvv);
		CVVEntryScreen.clickBookButton();
		EspressoUtils.assertViewWithTextIsDisplayed("Booking Complete");
	}


}
