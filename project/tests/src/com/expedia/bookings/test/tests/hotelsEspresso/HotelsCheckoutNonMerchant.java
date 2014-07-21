package com.expedia.bookings.test.tests.hotelsEspresso;

import org.joda.time.LocalDate;

import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.CVVEntryScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.CardInfoScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.CommonPaymentMethodScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.LaunchScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.LogInScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.ScreenActions;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.SettingsScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsCheckoutScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsDetailsScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsGuestPicker;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsRoomsRatesScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsSearchScreen;
import com.expedia.bookings.test.utils.EspressoUtils;
import com.expedia.bookings.test.utils.HotelsUserData;
import com.expedia.bookings.utils.ClearPrivateDataUtil;
import com.mobiata.android.util.SettingUtils;

import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.pressBack;

/**
 * Created by dmadan on 4/22/14.
 */
public class HotelsCheckoutNonMerchant extends ActivityInstrumentationTestCase2<SearchActivity> {

	public HotelsCheckoutNonMerchant() {
		super(SearchActivity.class);
	}

	private static final String TAG = "HotelsCheckoutNonMerchant";

	private HotelsUserData mUser;
	SearchActivity mActivity;
	Context mContext;

	protected void setUp() throws Exception {
		super.setUp();
		mUser = new HotelsUserData(getInstrumentation());
		mContext = getInstrumentation().getTargetContext();
		mUser.setHotelCityToRandomUSCity();
		// Disable v2 automatically.
		SettingUtils.save(mContext, "preference_disable_domain_v2_hotel_search", true);
		ClearPrivateDataUtil.clear(mContext);
		SettingUtils.save(mContext, R.string.preference_which_api_to_use_key, "Integration");
		SettingUtils.save(mContext, R.string.preference_filter_merchant_properties, true);
		mActivity = getActivity();
	}

	public void testMethod() throws Exception {

		// Launch screen
		ScreenActions.enterLog(TAG, "Launching hotels application");
		LaunchScreen.launchHotels();

		// Search screen
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		ScreenActions.enterLog(TAG, "Setting hotel search city to: " + "Las Vegas, NV");
		HotelsSearchScreen.enterSearchText("Las Vegas, NV");

		ScreenActions.enterLog(TAG, "Clicking suggestion");
		HotelsSearchScreen.clickSuggestion(getActivity(), "Las Vegas, NV");
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
		HotelsSearchScreen.sortMenu().clickSortByPopularityString();
		pressBack();
		HotelsSearchScreen.clickListItem(1);
		HotelsDetailsScreen.clickSelectButton();

		// Rooms and rates
		ScreenActions.enterLog(TAG, "Selecting first room listed for this hotel.");
		HotelsRoomsRatesScreen.selectRoomItem(0);
		try {
			SettingsScreen.clickOKString();
		}
		catch (Exception e) {
			ScreenActions.enterLog(TAG, "OK popup");
		}

		// Checkout
		HotelsCheckoutScreen.clickCheckoutButton();

		// Log in
		HotelsCheckoutScreen.clickLogInButton();
		LogInScreen.typeTextEmailEditText(mUser.getLoginEmail());
		LogInScreen.typeTextPasswordEditText(mUser.getLoginPassword());
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
		CardInfoScreen.typeTextCreditCardEditText(mUser.getCreditCardNumber());
		CardInfoScreen.clickOnExpirationDateButton();
		ScreenActions.enterLog(TAG, "Incrementing credit card exp. month and year by 1");
		CardInfoScreen.clickMonthUpButton();
		CardInfoScreen.clickYearUpButton();
		CardInfoScreen.clickSetButton();
		ScreenActions.enterLog(TAG, "Entering postal code: " + mUser.getAddressPostalCode());
		CardInfoScreen.typeTextPostalCode(mUser.getAddressPostalCode());
		ScreenActions.enterLog(TAG, "Entering cardholder name: " + mUser.getFirstName() + " " + mUser.getLastName());
		CardInfoScreen.typeTextNameOnCardEditText(mUser.getFirstName() + " " + mUser.getLastName());
		CardInfoScreen.clickOnDoneButton();
		CardInfoScreen.clickNoThanksButton();

		//Slide to purchase
		ScreenActions.enterLog(TAG, "Sliding to checkout");
		HotelsCheckoutScreen.slideToCheckout();

		// CVV Entry
		ScreenActions.enterLog(TAG, "Entering CCV: " + mUser.getCCV());
		CVVEntryScreen.parseAndEnterCVV(mUser.getCCV());
		CVVEntryScreen.clickBookButton();
		EspressoUtils.assertTrue("Booking Complete");
	}
}
