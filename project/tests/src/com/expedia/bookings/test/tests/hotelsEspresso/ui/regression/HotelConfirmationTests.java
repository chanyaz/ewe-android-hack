package com.expedia.bookings.test.tests.hotelsEspresso.ui.regression;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import org.hamcrest.Matchers;
import org.joda.time.LocalDate;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;
import android.text.format.DateUtils;
import android.util.Pair;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.BillingAddressScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.CVVEntryScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.CardInfoScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.CommonPaymentMethodScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.LaunchScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.LogInScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.ScreenActions;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsCheckoutScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsDetailsScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsGuestPicker;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsRoomsRatesScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsSearchScreen;
import com.expedia.bookings.test.utils.EspressoUtils;
import com.expedia.bookings.test.utils.HotelsUserData;
import com.expedia.bookings.utils.CalendarUtils;
import com.expedia.bookings.utils.ClearPrivateDataUtil;
import com.mobiata.android.util.SettingUtils;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.matcher.RootMatchers.withDecorView;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;

/**
 * Created by dmadan on 5/13/14.
 */
public class HotelConfirmationTests extends ActivityInstrumentationTestCase2<SearchActivity> {
	public HotelConfirmationTests() {
		super(SearchActivity.class);
	}

	private static final String TAG = HotelConfirmationTests.class.getSimpleName();
	Context mContext;
	SharedPreferences mPrefs;
	Resources mRes;
	HotelsUserData mUser;
	int mNumberOfGuests;
	String mDateRangeString;
	String mHotelName;

	protected void setUp() throws Exception {
		super.setUp();
		mContext = getInstrumentation().getTargetContext();
		mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		mRes = mContext.getResources();
		mUser = new HotelsUserData(getInstrumentation());
		ClearPrivateDataUtil.clear(mContext);
		SettingUtils.save(mContext, R.string.preference_which_api_to_use_key, "Integration");
		SettingUtils.save(mContext, R.id.preference_suppress_hotel_booking_checkbox, "true");
		getActivity();
	}

	private void getToCheckout() throws Exception {
		ArrayList<Pair<Integer, Integer>> guestPairList = generateChildAdultCountPairs();
		Pair<Integer, Integer> pair = guestPairList.get(0);
		mNumberOfGuests = pair.first + pair.second;
		LaunchScreen.launchHotels();

		// Search screen
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		ScreenActions.enterLog(TAG, "Setting hotel search city to: " + "New York, NY");
		ScreenActions.enterLog(TAG, "HERE entering text");
		HotelsSearchScreen.enterSearchText("New York, NY");

		ScreenActions.enterLog(TAG, "HERE clicking suggestion");
		HotelsSearchScreen.clickSuggestion(getActivity(), "New York, NY");
		onView(withText("New York, NY")).inRoot(withDecorView(Matchers.not(is(getActivity().getWindow().getDecorView())))).perform(click());
		Calendar cal = Calendar.getInstance();
		int year = cal.get(cal.YEAR);
		int month = cal.get(cal.MONTH) + 1;
		LocalDate mStartDate = new LocalDate(year, month, 5);
		LocalDate mEndDate = new LocalDate(year, month, 10);
		HotelsSearchScreen.clickOnCalendarButton();
		HotelsSearchScreen.clickDate(mStartDate, mEndDate);
		HotelsSearchScreen.clickOnGuestsButton();
		setGuests(pair.first, pair.second);
		HotelsSearchScreen.guestPicker().clickOnSearchButton();
		HotelsSearchScreen.clickListItem(1);
		HotelsDetailsScreen.clickSelectButton();
		HotelsRoomsRatesScreen.selectRoomItem(1);
	}

	public void testLoggedInBookingConfirmation() throws Exception {
		ScreenActions.enterLog(TAG, "START: Testing confirmation screen after logged-in booking");
		getToCheckout();
		HotelsCheckoutScreen.clickCheckoutButton();
		HotelsCheckoutScreen.clickLogInButton();
		LogInScreen.typeTextEmailEditText(mUser.getLoginEmail());
		LogInScreen.typeTextPasswordEditText(mUser.getLoginPassword());
		LogInScreen.clickOnLoginButton();

		HotelsCheckoutScreen.clickSelectPaymentButton();
		ScreenActions.enterLog(TAG, "Using new credit card");
		try {
			CommonPaymentMethodScreen.clickOnAddNewCardTextView();
		}
		catch (Exception e) {
			ScreenActions.enterLog(TAG, "No Add New Card button. Proceeding anyway.");
		}
		CardInfoScreen.typeTextCreditCardEditText(mUser.getCreditCardNumber());
		BillingAddressScreen.typeTextPostalCode(mUser.getAddressPostalCode());
		CardInfoScreen.typeTextNameOnCardEditText(mUser.getFirstName() + " " + mUser.getLastName());
		CardInfoScreen.clickOnExpirationDateButton();

		ScreenActions.enterLog(TAG, "Incrementing credit card exp. month and year by 1");
		CardInfoScreen.clickMonthUpButton();
		CardInfoScreen.clickYearUpButton();
		CardInfoScreen.clickSetButton();
		CardInfoScreen.clickOnDoneButton();
		CardInfoScreen.clickNoThanksButton();

		EspressoUtils.getValues("HotelName", R.id.title);
		mHotelName = mPrefs.getString("HotelName", "");
		HotelsCheckoutScreen.slideToCheckout();
		CVVEntryScreen.parseAndEnterCVV(mUser.getCCV());
		CVVEntryScreen.clickBookButton();
		verifyConfirmationTexts();
	}

	private void setGuests(int adults, int children) {
		ScreenActions.enterLog(TAG, "Setting adults to: " + adults + " and children to: " + children);
		for (int i = 6; i >= 1; i--) {
			HotelsGuestPicker.decrementAdultsButton();
		}
		for (int i = 4; i >= 0; i--) {
			HotelsGuestPicker.decrementChildrenButton();
		}

		for (int i = 1; i < adults; i++) {
			HotelsGuestPicker.incrementAdultsButton();
		}

		for (int i = 0; i < children; i++) {
			HotelsGuestPicker.incrementChildrenButton();
		}
	}

	private ArrayList<Pair<Integer, Integer>> generateChildAdultCountPairs() {
		ArrayList<Pair<Integer, Integer>> returnableList = new ArrayList<Pair<Integer, Integer>>();
		final int numberOfPairsToGenerate = 3;
		Random rand = new Random();
		for (int i = 0; i < numberOfPairsToGenerate; i++) {
			// Can have a maximum of six guests
			// Can add at most 4 children
			int childCount = rand.nextInt(5);
			// Must have a minimum of 1 adult, thus can only add a maximum of 5 minus the number of children already added
			int adultCount = rand.nextInt(6 - childCount) + 1;
			Pair<Integer, Integer> newPair = new Pair<Integer, Integer>(adultCount, childCount);
			returnableList.add(newPair);
			ScreenActions.enterLog(TAG, "Added pair: " + newPair.first + ", " + newPair.second);
		}
		return returnableList;
	}

	private void verifyConfirmationTexts() {
		HotelSearchParams params = Db.getHotelSearch().getSearchParams();
		int cachedNumberOfGuests = params.getNumAdults() + params.getNumChildren();
		assertEquals(mNumberOfGuests, cachedNumberOfGuests);
		ScreenActions.enterLog(TAG, "no guest  " + mNumberOfGuests + "," + cachedNumberOfGuests);

		String guestString = mRes.getQuantityString(R.plurals.number_of_guests, mNumberOfGuests, mNumberOfGuests);
		mDateRangeString = CalendarUtils.formatDateRange2(getActivity(), params, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH);
		String expectedSummaryString = mRes.getString(R.string.stay_summary_TEMPLATE, guestString, mDateRangeString);
		EspressoUtils.getValues("displayedDetailsString", R.id.stay_summary_text_view);
		String displayedDetailsString = mPrefs.getString("displayedDetailsString", "");
		assertEquals(expectedSummaryString, displayedDetailsString);
		ScreenActions.enterLog(TAG, "summary string " + expectedSummaryString + "," + displayedDetailsString);

		EspressoUtils.getValues("displayedHotelName", R.id.hotel_name_text_view);
		String displayedHotelName = mPrefs.getString("displayedHotelName", "");
		assertEquals(mHotelName, displayedHotelName);
		ScreenActions.enterLog(TAG, "hotelname " + mHotelName + "," + displayedHotelName);

		EspressoUtils.getValues("itineraryConfirmationText", R.id.itinerary_text_view);
		String itineraryConfirmationText = mPrefs.getString("itineraryConfirmationText", "");
		String expectedItineraryNumber = Db.getBookingResponse().getItineraryId();
		String expectedItineraryConfirmationText = mRes.getString(R.string.itinerary_confirmation_TEMPLATE, expectedItineraryNumber);
		assertEquals(expectedItineraryConfirmationText, itineraryConfirmationText);
		ScreenActions.enterLog(TAG, "itin conf " + expectedItineraryConfirmationText + "," + itineraryConfirmationText);

		EspressoUtils.getValues("displayedEmailAddress", R.id.email_text_view);
		String displayedEmailAddress = mPrefs.getString("displayedEmailAddress", "");
		String expectedEmailAddString = mUser.getLoginEmail();
		assertEquals(expectedEmailAddString, displayedEmailAddress);
		ScreenActions.enterLog(TAG, "email " + expectedEmailAddString + "," + displayedEmailAddress);
	}
}
