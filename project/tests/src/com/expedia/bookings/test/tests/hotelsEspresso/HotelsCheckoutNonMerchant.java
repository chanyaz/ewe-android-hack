package com.expedia.bookings.test.tests.hotelsEspresso;

import java.util.Calendar;

import org.joda.time.LocalDate;

import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.CVVEntryScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.CardInfoScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.CommonTravelerInformationScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.LaunchScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.ScreenActions;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsCheckoutScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsConfirmationScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsDetailsScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsGuestPicker;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsSearchScreen;
import com.expedia.bookings.test.utils.HotelsUserData;
import com.expedia.bookings.utils.ClearPrivateDataUtil;
import com.mobiata.android.util.SettingUtils;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.pressBack;
import static com.google.android.apps.common.testing.ui.espresso.matcher.RootMatchers.withDecorView;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

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
		ScreenActions.enterLog(TAG, "HERE entering text");
		HotelsSearchScreen.enterSearchText("Las Vegas, NV");

		ScreenActions.enterLog(TAG, "HERE clicking suggestion");
		onView(withText("Las Vegas, NV")).inRoot(withDecorView(not(is(mActivity.getWindow().getDecorView())))).perform(click());
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
		ScreenActions.enterLog(TAG, "Opened sort fragment");
		HotelsSearchScreen.clickOnSortButton();
		ScreenActions.enterLog(TAG, "clicked on sort button");
		HotelsSearchScreen.sortMenu().clickSortByPopularityString();
		pressBack();
		onView(withText("Las Vegas Hostel")).perform(click());
		HotelsDetailsScreen.clickSelectButton();

		// Rooms and rates
		ScreenActions.enterLog(TAG, "Selecting first room listed for this hotel.");
		onView(withText("Standard Rate Private Room With 1 King Size Bed And Bathroom Rates Are For Both Single And Double Occupancy And Include Wireless Internet,")).perform(click());

		// Checkout
		HotelsCheckoutScreen.clickCheckoutButton();

		// Select payment as guest user
		HotelsCheckoutScreen.clickSelectPaymentButton();
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

		// Manually add traveler
		onView(withText("Guest details")).perform(click());
		HotelsCheckoutScreen.clickAddTravelerButton();
		ScreenActions.enterLog(TAG, "Entering first name: " + mUser.getFirstName());
		CommonTravelerInformationScreen.enterFirstName(mUser.getFirstName());
		ScreenActions.enterLog(TAG, "Entering last name: " + mUser.getLastName());
		CommonTravelerInformationScreen.enterLastName(mUser.getLastName());
		ScreenActions.enterLog(TAG, "Entering phone number: " + mUser.getPhoneNumber());
		CommonTravelerInformationScreen.enterPhoneNumber(mUser.getPhoneNumber());
		CommonTravelerInformationScreen.enterEmailAddress(mUser.getLoginEmail());
		CommonTravelerInformationScreen.clickDoneButton();

		//Slide to purchase
		ScreenActions.enterLog(TAG, "Sliding to checkout");
		HotelsCheckoutScreen.slideToCheckout();

		// CVV Entry
		ScreenActions.enterLog(TAG, "Entering CCV: " + mUser.getCCV());
		CVVEntryScreen.parseAndEnterCVV(mUser.getCCV());
		CVVEntryScreen.clickBookButton();
		HotelsConfirmationScreen.clickDoneButton();
		ScreenActions.enterLog(TAG, "Clicking shop tab");
		LaunchScreen.pressShop();
	}
}
