package com.expedia.bookings.test.tests.hotelsEspresso.ui.regression;

import java.util.ArrayList;
import java.util.Random;

import org.joda.time.LocalDate;

import android.text.format.DateUtils;
import android.util.Pair;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.BillingAddressScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.CVVEntryScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.CardInfoScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.CommonPaymentMethodScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.LaunchScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.LogInScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.ScreenActions;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.SettingsScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsCheckoutScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsConfirmationScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsDetailsScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsGuestPicker;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsRoomsRatesScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsSearchScreen;
import com.expedia.bookings.test.utils.EspressoUtils;
import com.expedia.bookings.test.utils.HotelsUserData;
import com.expedia.bookings.test.utils.PhoneTestCase;
import com.expedia.bookings.utils.CalendarUtils;

import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;

/**
 * Created by dmadan on 5/13/14.
 */
public class HotelConfirmationTests extends PhoneTestCase {

	private static final String TAG = HotelConfirmationTests.class.getSimpleName();
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
		HotelsSearchScreen.enterSearchText("Boston, MA");
		HotelsSearchScreen.clickSuggestion(getActivity(), "Boston, MA");
		LocalDate startDate = LocalDate.now().plusDays(58);
		LocalDate endDate = LocalDate.now().plusDays(61);
		HotelsSearchScreen.clickOnCalendarButton();
		HotelsSearchScreen.clickDate(startDate, endDate);
		HotelsSearchScreen.clickOnGuestsButton();
		setGuests(pair.first, pair.second);
		HotelsSearchScreen.guestPicker().clickOnSearchButton();
		HotelsSearchScreen.clickListItem(1);
		HotelsDetailsScreen.clickSelectButton();

		int numberOfRooms = EspressoUtils.getListCount(HotelsRoomsRatesScreen.roomList()) - 1;
		HotelsRoomsRatesScreen.selectRoomItem(0);
		try {
			SettingsScreen.clickOKString();
			if (numberOfRooms > 1) {
				HotelsRoomsRatesScreen.selectRoomItem(1);
			}
		}
		catch (Exception e) {
			ScreenActions.enterLog(TAG, "No popup");
		}
	}

	public void testLoggedInBookingConfirmation() throws Exception {
		mUser = new HotelsUserData(getInstrumentation());

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

		mHotelName = EspressoUtils.getText(R.id.title);
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

		String guestString = getActivity().getResources().getQuantityString(R.plurals.number_of_guests, mNumberOfGuests, mNumberOfGuests);
		mDateRangeString = CalendarUtils.formatDateRange2(getActivity(), params, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH);
		String expectedSummaryString = getActivity().getResources().getString(R.string.stay_summary_TEMPLATE, guestString, mDateRangeString);
		HotelsConfirmationScreen.summaryTextView().check(matches(withText(expectedSummaryString)));

		HotelsConfirmationScreen.hotelNameTextView().check(matches(withText(mHotelName)));

		String expectedItineraryNumber = Db.getBookingResponse().getItineraryId();
		String expectedItineraryConfirmationText = getActivity().getResources().getString(R.string.itinerary_confirmation_TEMPLATE, expectedItineraryNumber);
		HotelsConfirmationScreen.itineraryTextView().check(matches(withText(expectedItineraryConfirmationText)));

		String expectedEmailAddString = mUser.getLoginEmail();
		HotelsConfirmationScreen.emailTextView().check(matches(withText(expectedEmailAddString)));
	}
}
