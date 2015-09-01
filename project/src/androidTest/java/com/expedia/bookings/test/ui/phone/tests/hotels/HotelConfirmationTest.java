package com.expedia.bookings.test.ui.phone.tests.hotels;

import java.util.ArrayList;
import java.util.Random;

import org.joda.time.LocalDate;

import android.util.Pair;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.test.ui.phone.pagemodels.common.BillingAddressScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CVVEntryScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CardInfoScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CommonPaymentMethodScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LogInScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.ui.phone.pagemodels.common.SettingsScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsCheckoutScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsConfirmationScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsDetailsScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsGuestPicker;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsRoomsRatesScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsSearchScreen;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.HotelsUserData;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import com.expedia.bookings.utils.DateFormatUtils;
import com.expedia.bookings.utils.MockModeShim;
import com.mobiata.mocke3.ExpediaDispatcher;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class HotelConfirmationTest extends PhoneTestCase {

	private static final String TAG = HotelConfirmationTest.class.getSimpleName();
	HotelsUserData mUser;
	int mNumberOfGuests;
	String mDateRangeString;
	String mHotelName;

	private void getToCheckout() throws Exception {
		ArrayList<Pair<Integer, Integer>> guestPairList = generateChildAdultCountPairs();
		Pair<Integer, Integer> pair = guestPairList.get(0);
		mNumberOfGuests = pair.first + pair.second;
		LaunchScreen.launchHotels();

		// Search screen
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText("New York, NY");
		HotelsSearchScreen.clickSuggestionWithName(getActivity(), "New York, NY");
		LocalDate startDate = LocalDate.now().plusDays(58);
		LocalDate endDate = LocalDate.now().plusDays(61);
		HotelsSearchScreen.clickOnCalendarButton();
		HotelsSearchScreen.clickDate(startDate, endDate);
		HotelsSearchScreen.clickOnGuestsButton();
		setGuests(pair.first, pair.second);
		HotelsSearchScreen.guestPicker().clickOnSearchButton();
		HotelsSearchScreen.clickListItem(1);
		Common.pressBack();
		HotelsSearchScreen.clickListItem(1);
		HotelsDetailsScreen.clickSelectButton();

		int numberOfRooms = EspressoUtils.getListCount(HotelsRoomsRatesScreen.roomList()) - 1;
		HotelsRoomsRatesScreen.selectRoomItem(0);
		try {
			SettingsScreen.clickOkString();
			if (numberOfRooms > 1) {
				HotelsRoomsRatesScreen.selectRoomItem(1);
			}
		}
		catch (Exception e) {
			ScreenActions.enterLog(TAG, "No popup");
		}
	}

	public void testLoggedInBookingConfirmation() throws Exception {
		mUser = new HotelsUserData();

		ScreenActions.enterLog(TAG, "START: Testing confirmation screen after logged-in booking");
		getToCheckout();
		HotelsCheckoutScreen.clickCheckoutButton();
		HotelsCheckoutScreen.clickLogInButton();
		LogInScreen.typeTextEmailEditText(mUser.email);
		LogInScreen.typeTextPasswordEditText(mUser.password);
		LogInScreen.clickOnLoginButton();

		HotelsCheckoutScreen.clickSelectPaymentButton();
		ScreenActions.enterLog(TAG, "Using new credit card");
		try {
			CommonPaymentMethodScreen.clickOnAddNewCardTextView();
		}
		catch (Exception e) {
			ScreenActions.enterLog(TAG, "No Add New Card button. Proceeding anyway.");
		}
		CardInfoScreen.typeTextCreditCardEditText(mUser.creditCardNumber);
		BillingAddressScreen.typeTextPostalCode(mUser.zipcode);
		CardInfoScreen.typeTextNameOnCardEditText(mUser.firstName + " " + mUser.lastName);
		CardInfoScreen.clickOnExpirationDateButton();
		ScreenActions.enterLog(TAG, "Incrementing credit card exp. month and year by 1");
		CardInfoScreen.clickMonthUpButton();
		CardInfoScreen.clickYearUpButton();
		CardInfoScreen.clickSetButton();
		CardInfoScreen.clickOnDoneButton();
		CardInfoScreen.clickNoThanksButton();

		mHotelName = EspressoUtils.getText(R.id.title);
		HotelsCheckoutScreen.slideToCheckout();
		CVVEntryScreen.parseAndEnterCVV(mUser.cvv);
		CVVEntryScreen.clickBookButton();
		verifyConfirmationTexts();
		verifyTravelAdTracking();

		// Hitting done takes you to launch (as does back press)
		HotelsConfirmationScreen.doneButton().perform(click());
		EspressoUtils.assertViewIsDisplayed(R.id.launch_toolbar);
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
		HotelSearchParams params = Db.getTripBucket().getHotel().getHotelSearchParams();

		// Guests / dates string
		int cachedNumberOfGuests = params.getNumAdults() + params.getNumChildren();
		assertEquals(mNumberOfGuests, cachedNumberOfGuests);
		String guestString = getActivity().getResources().getQuantityString(R.plurals.number_of_guests, mNumberOfGuests, mNumberOfGuests);
		mDateRangeString = DateFormatUtils.formatRangeDateToDate(getActivity(), params, DateFormatUtils.FLAGS_DATE_ABBREV_MONTH);
		String expectedSummaryString = getActivity().getResources().getString(R.string.stay_summary_TEMPLATE, guestString, mDateRangeString);
		HotelsConfirmationScreen.summaryTextView().check(matches(withText(expectedSummaryString)));

		// Hotel name
		HotelsConfirmationScreen.hotelNameTextView().check(matches(withText(mHotelName)));

		// Itinerary number
		String expectedItineraryNumber = Db.getTripBucket().getHotel().getBookingResponse().getItineraryId();
		String expectedItineraryConfirmationText = getActivity().getResources().getString(R.string.itinerary_confirmation_TEMPLATE, expectedItineraryNumber);
		HotelsConfirmationScreen.itineraryTextView().check(matches(withText(expectedItineraryConfirmationText)));

		// Email address
		String expectedEmailAddString = mUser.email;
		HotelsConfirmationScreen.emailTextView().check(matches(withText(expectedEmailAddString)));

		// Actions are displayed (share, add to calendar, call expedia)
		EspressoUtils.assertViewIsDisplayed(R.id.call_action_text_view);
		EspressoUtils.assertViewIsDisplayed(R.id.share_action_text_view);
		EspressoUtils.assertViewIsDisplayed(R.id.calendar_action_text_view);
	}

	private void verifyTravelAdTracking() {
		ExpediaDispatcher dispatcher = MockModeShim.getDispatcher();
		assertEquals(3, dispatcher.numOfTravelAdRequests("/travel"));
		assertEquals(3, dispatcher.numOfTravelAdRequests("/TravelAdsService/v3/Hotels/TravelAdImpression"));
		assertEquals(2, dispatcher.numOfTravelAdRequests("/TravelAdsService/v3/Hotels/TravelAdClick"));
		assertEquals(1, dispatcher.numOfTravelAdRequests("/ads/hooklogic"));
	}
}
