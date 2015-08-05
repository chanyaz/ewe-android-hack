package com.expedia.bookings.test.ui.phone.tests.hotels;

import java.util.ArrayList;
import java.util.Random;

import org.joda.time.LocalDate;

import android.util.Pair;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.SettingsScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelReceiptModel;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsCheckoutScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsDetailsScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsGuestPicker;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsRoomsRatesScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsSearchScreen;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.Espresso;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.anything;

/**
 * Created by dmadan on 5/16/14.
 */
public class HotelCheckoutInfoTests extends PhoneTestCase {

	private static final String TAG = HotelCheckoutInfoTests.class.getSimpleName();


	public void testHotelHeaderInfo() throws Exception {
		int numberOfRooms;
		String hotelName, checkoutHotelName, checkoutRoomName;
		float hotelRating, checkoutHotelRating;
		ScreenActions.enterLog(TAG, "START: HOTEL HEADER INFO TESTS");
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText("Boston, MA");
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);
		HotelsSearchScreen.clickOnCalendarButton();
		HotelsSearchScreen.clickDate(startDate, endDate);
		HotelsSearchScreen.guestPicker().clickOnSearchButton();
		int totalHotels = EspressoUtils.getListChildCount(HotelsSearchScreen.hotelResultsListView());
		for (int i = 1; i < 3; i++) {
			HotelsSearchScreen.clickListItem(i);
			HotelsDetailsScreen.clickSelectButton();

			numberOfRooms = EspressoUtils.getListCount(HotelsRoomsRatesScreen.roomList()) - 1;
			hotelName = EspressoUtils.getText(R.id.name_text_view);
			hotelRating = EspressoUtils.getRatingValue(HotelsRoomsRatesScreen.hotelRatingBar());
			ScreenActions.enterLog(TAG, "Test is looking at hotel with name: " + hotelName);
			for (int j = 0; j < numberOfRooms - 1; j++) {
				DataInteraction rowModel = onData(anything()).inAdapterView(withId(android.R.id.list)).atPosition(j);
				String roomName = EspressoUtils.getListItemValues(rowModel, R.id.room_description_text_view);
				HotelsRoomsRatesScreen.selectRoomItem(j);
				try {
					SettingsScreen.clickOkString();
					try {
						HotelsCheckoutScreen.checkoutButton().check(matches(isDisplayed()));
						ScreenActions.enterLog(TAG, "Great news popup");
					}
					catch (Exception e) {
						ScreenActions.enterLog(TAG, "Rooms sold out popup");
						break;
					}
				}
				catch (Exception e) {
					ScreenActions.enterLog(TAG, "No popup");
				}

				checkoutHotelName = EspressoUtils.getText(R.id.title);
				checkoutHotelRating = EspressoUtils.getRatingValue(HotelsDetailsScreen.ratingBar());
				checkoutRoomName = EspressoUtils.getText(R.id.room_type_description_text_view);

				assertEquals(hotelName, checkoutHotelName);
				ScreenActions.enterLog(TAG, "Assertion Passed: Hotel name from rooms and rates matches name in hotel details");
				assertEquals(hotelRating, checkoutHotelRating);
				ScreenActions.enterLog(TAG, "Assertion Passed: Hotel rating from rooms and rates matches rating in hotel details");
				assertEquals(roomName.replaceAll("\\s+", ""), checkoutRoomName.replaceAll("\\s+", ""));
				ScreenActions.enterLog(TAG, "Assertion Passed: Room title from rooms and rates matches room title in hotel checkout receipt");
				HotelReceiptModel.clickGrandTotalTextView();
				HotelReceiptModel.costSummaryString().check(matches(isDisplayed()));
				ScreenActions.enterLog(TAG, "Assertion Passed: Cost summary displayed after clicking grand total info button");
				Espresso.pressBack();
				Espresso.pressBack();
			}
			Espresso.pressBack();
			Espresso.pressBack();
		}
		ScreenActions.enterLog(TAG, "END: HOTEL HEADER INFO TESTS");
	}

	public ArrayList<Pair<Integer, Integer>> generateChildAdultCountPairs() {
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

	public void testHotelReceiptGuestNumber() throws Exception {
		ScreenActions.enterLog(TAG, "START: HOTEL RECEIPT GUEST NUMBER TESTS");
		ArrayList<Pair<Integer, Integer>> adultChildNumberPairs = generateChildAdultCountPairs();
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText("Boston, MA");
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);
		HotelsSearchScreen.clickOnCalendarButton();
		HotelsSearchScreen.clickDate(startDate, endDate);
		HotelsSearchScreen.guestPicker().clickOnSearchButton();

		for (int i = 0; i < adultChildNumberPairs.size(); i++) {
			Pair<Integer, Integer> currentPair = adultChildNumberPairs.get(i);
			HotelsSearchScreen.clickOnGuestsButton();
			setGuests(currentPair.first, currentPair.second);
			HotelsSearchScreen.guestPicker().clickOnSearchButton();
			for (int j = 1; j < 3; j++) {
				HotelsSearchScreen.clickListItem(j);
				HotelsDetailsScreen.clickSelectButton();
				int numberOfRooms = EspressoUtils.getListCount(HotelsRoomsRatesScreen.roomList()) - 1;
				ScreenActions.enterLog(TAG, "number of rooms:" + numberOfRooms);
				for (int k = 0; k < numberOfRooms; k++) {
					HotelsRoomsRatesScreen.selectRoomItem(k);
					try {
						SettingsScreen.clickOkString();
						try {
							HotelsCheckoutScreen.checkoutButton().check(matches(isDisplayed()));
							ScreenActions.enterLog(TAG, "Great news popup");
						}
						catch (Exception e) {
							ScreenActions.enterLog(TAG, "Rooms sold out popup");
							break;
						}
					}
					catch (Exception e) {
						ScreenActions.enterLog(TAG, "No popup");
					}
					String receiptGuestString = EspressoUtils.getText(R.id.guests_text);
					int totalNumberOfGuests = currentPair.first + currentPair.second;
					String expectedGuestString = getActivity().getResources().getQuantityString(R.plurals.number_of_guests, totalNumberOfGuests, totalNumberOfGuests);
					assertEquals(expectedGuestString, receiptGuestString);
					Espresso.pressBack();
				}
				Espresso.pressBack();
				Espresso.pressBack();
			}
		}
		ScreenActions.enterLog(TAG, "END: HOTEL RECEIPT GUEST NUMBER TESTS");
	}

	public void testHotelNightsNumber() throws Exception {
		ScreenActions.enterLog(TAG, "START: HOTEL RECEIPT NIGHTS NUMBER TESTS");
		int[] dateOffsets = {
			3, 7, 10, 25,
		};
		LaunchScreen.launchHotels();
		for (int i = 0; i < dateOffsets.length; i++) {
			int numberOfNights = dateOffsets[i];
			HotelsSearchScreen.clickSearchEditText();
			HotelsSearchScreen.clickToClearSearchEditText();
			HotelsSearchScreen.enterSearchText("Boston, MA");
			LocalDate startDate = LocalDate.now().plusDays(35);
			LocalDate endDate = LocalDate.now().plusDays(35 + numberOfNights);
			HotelsSearchScreen.clickOnCalendarButton();
			HotelsSearchScreen.clickDate(startDate, endDate);

			HotelsSearchScreen.clickOnGuestsButton();
			HotelsSearchScreen.guestPicker().clickOnSearchButton();
			ScreenActions.enterLog(TAG, "Testing for hotels for a stay of " + numberOfNights + " nights.");

			for (int j = 1; j < 3; j++) {
				HotelsSearchScreen.clickListItem(j);
				HotelsDetailsScreen.clickSelectButton();
				int numberOfRooms = EspressoUtils.getListCount(HotelsRoomsRatesScreen.roomList()) - 1;
				for (int k = 0; k < numberOfRooms; k++) {
					HotelsRoomsRatesScreen.selectRoomItem(k);
					try {
						SettingsScreen.clickOkString();
						try {
							HotelsCheckoutScreen.checkoutButton().check(matches(isDisplayed()));
							ScreenActions.enterLog(TAG, "Great news popup");
						}
						catch (Exception e) {
							ScreenActions.enterLog(TAG, "Rooms sold out popup");
							break;
						}
					}
					catch (Exception e) {
						ScreenActions.enterLog(TAG, "No popup");
					}
					String expectedNightsString = getActivity().getResources().getQuantityString(R.plurals.number_of_nights, numberOfNights, numberOfNights);
					String shownNightsString = EspressoUtils.getText(R.id.nights_text);
					assertEquals(expectedNightsString, shownNightsString);
					ScreenActions.enterLog(TAG, "Nights string in hotel receipt matched the number of nights selected.");
					HotelReceiptModel.clickGrandTotalTextView();
					EspressoUtils.assertViewWithTextIsDisplayed(shownNightsString);
					ScreenActions.enterLog(TAG, "Number of nights selected is properly displayed in cost summary fragment.");
					HotelReceiptModel.costSummaryString().check(matches(isDisplayed()));
					ScreenActions.enterLog(TAG, "Cost summary string is shown in cost summary fragment.");
					Espresso.pressBack();
					Espresso.pressBack();
				}
				Espresso.pressBack();
				Espresso.pressBack();
			}
		}
		ScreenActions.enterLog(TAG, "END: HOTEL RECEIPT NIGHTS NUMBER TESTS");
	}


	public void testUIElementsPresent() throws Exception {
		ScreenActions.enterLog(TAG, "START: UI ELEMENTS PRESENT TESTS");
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText("Boston, MA");
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);
		HotelsSearchScreen.clickOnCalendarButton();
		HotelsSearchScreen.clickDate(startDate, endDate);
		HotelsSearchScreen.guestPicker().clickOnSearchButton();

		for (int j = 1; j < 3; j++) {
			HotelsSearchScreen.clickListItem(j);
			HotelsDetailsScreen.clickSelectButton();
			int numberOfRooms = EspressoUtils.getListCount(HotelsRoomsRatesScreen.roomList()) - 1;
			for (int k = 0; k < numberOfRooms; k++) {
				HotelsRoomsRatesScreen.selectRoomItem(k);
				try {
					SettingsScreen.clickOkString();
					try {
						HotelsCheckoutScreen.checkoutButton().check(matches(isDisplayed()));
						ScreenActions.enterLog(TAG, "Great news popup");
					}
					catch (Exception e) {
						ScreenActions.enterLog(TAG, "Rooms sold out popup");
						break;
					}
				}
				catch (Exception e) {
					ScreenActions.enterLog(TAG, "No popup");
				}
				String hotelName = EspressoUtils.getText(R.id.title);
				ScreenActions.enterLog(TAG, "Looking at hotel: " + hotelName);

				String nightsString = EspressoUtils.getText(R.id.nights_text);
				String guestsString = EspressoUtils.getText(R.id.guests_text);
				String priceString = EspressoUtils.getText(R.id.price_text);
				HotelsCheckoutScreen.clickCheckoutButton();

				String secondNightsString = EspressoUtils.getText(R.id.nights_text);
				String secondGuestsString = EspressoUtils.getText(R.id.guests_text);
				String secondPriceString = EspressoUtils.getText(R.id.price_text);

				assertEquals(nightsString, secondNightsString);
				ScreenActions.enterLog(TAG, "Nights string remained consistent after checkout scroll down.");
				assertEquals(guestsString, secondGuestsString);
				ScreenActions.enterLog(TAG, "Guests string remained consistent after checkout scroll down.");
				assertEquals(priceString, secondPriceString);
				ScreenActions.enterLog(TAG, "Price string remained consistent after checkout scroll down.");
				Espresso.pressBack();
				Espresso.pressBack();
			}
			Espresso.pressBack();
			Espresso.pressBack();
		}
		ScreenActions.enterLog(TAG, "END: UI ELEMENTS PRESENT TESTS");
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
}

