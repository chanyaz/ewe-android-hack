package com.expedia.bookings.test.tests.hotels.ui.regression;

import java.util.ArrayList;
import java.util.Random;

import android.util.Pair;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.PhoneSearchActivity;
import com.expedia.bookings.test.tests.pageModels.hotels.HotelReceiptModel;
import com.expedia.bookings.test.tests.pageModels.hotels.RoomsAndRatesRow;
import com.expedia.bookings.test.utils.CalendarTouchUtils;
import com.expedia.bookings.test.utils.CustomActivityInstrumentationTestCase;
import com.mobiata.android.text.format.Time;

public class HotelCheckoutInfoTests extends CustomActivityInstrumentationTestCase<PhoneSearchActivity> {

	private static final String TAG = HotelCheckoutInfoTests.class.getSimpleName();

	public HotelCheckoutInfoTests() {
		super(PhoneSearchActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testHotelHeaderInfo() throws Exception {
		mDriver.enterLog(TAG, "START: HOTEL HEADER INFO TESTS");
		mUser.setHotelCityToRandomUSCity();
		mDriver.hotelsSearchScreen().clickSearchEditText();
		mDriver.hotelsSearchScreen().clickToClearSearchEditText();
		mDriver.hotelsSearchScreen().enterSearchText(mUser.getHotelSearchCity());
		mDriver.hotelsSearchScreen().clickOnGuestsButton();
		mDriver.hotelsSearchScreen().guestPicker().clickOnSearchButton();
		mDriver.waitForStringToBeGone(mDriver.hotelsSearchScreen().searchingForHotels());
		mDriver.enterLog(TAG, "Searched for hotels in: " + mUser.getHotelSearchCity());
		for (int i = 0; i < 2; i++) {
			mDriver.hotelsSearchScreen().selectHotelFromList(i);
			mDriver.hotelsDetailsScreen().clickSelectButton();
			int numberOfRooms = mDriver.hotelsRoomsRatesScreen().roomList().getChildCount() - 1;
			String hotelName = mDriver.hotelsRoomsRatesScreen().hotelNameTextView().getText().toString();
			float hotelRating = mDriver.hotelsRoomsRatesScreen().hotelRatingBar().getRating();
			mDriver.enterLog(TAG, "Test is looking at hotel with name: " + hotelName);
			for (int j = 0; j < numberOfRooms; j++) {
				if (!handleDialogPopupPresent()) {
					RoomsAndRatesRow rowModel = mDriver.hotelsRoomsRatesScreen().getRowModelAtIndex(j);
					String roomName = rowModel.roomDescriptionTextView().getText().toString();
					mDriver.hotelsRoomsRatesScreen().selectRoom(j);
					mDriver.waitForStringToBeGone(mDriver.hotelsCheckoutScreen().calculatingTaxesAndFees());
					if (!handleDialogPopupPresent()) {
						String checkoutHotelName = mDriver.hotelsCheckoutScreen().hotelNameView().getText().toString();
						float checkoutHotelRating = mDriver.hotelsCheckoutScreen().ratingBar().getRating();
						HotelReceiptModel receiptModel = mDriver.hotelsCheckoutScreen().hotelReceiptModel();
						String checkoutRoomName = receiptModel.roomTypeDescriptionTextView().getText().toString();
						assertEquals(hotelName, checkoutHotelName);
						mDriver.enterLog(TAG,
								"Assertion Passed: Hotel name from rooms and rates matches name in hotel details");
						assertEquals(hotelRating, checkoutHotelRating);
						mDriver.enterLog(TAG,
								"Assertion Passed: Hotel rating from rooms and rates matches rating in hotel details");
						assertEquals(roomName, checkoutRoomName);
						mDriver.enterLog(TAG,
								"Assertion Passed: Room title from rooms and rates matches room title in hotel checkout receipt");
						mDriver.hotelsCheckoutScreen().hotelReceiptModel().clickGrandTotalTextView();
						assertTrue(mDriver.searchText(mDriver.hotelsCheckoutScreen().hotelReceiptModel()
								.costSummaryString()));
						mDriver.enterLog(TAG,
								"Assertion Passed: Cost summary displayed after clicking grand total info button");
						mDriver.goBack();
						mDriver.goBack();
						mDriver.delay();
					}
				}
			}
			mDriver.goBack();
			mDriver.delay(1);
			mDriver.goBack();
			mDriver.delay(1);
		}
		mDriver.enterLog(TAG, "END: HOTEL HEADER INFO TESTS");
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
			mDriver.enterLog(TAG, "Added pair: " + newPair.first + ", " + newPair.second);
		}
		return returnableList;
	}

	public void testHotelReceiptGuestNumber() throws Exception {
		mDriver.enterLog(TAG, "START: HOTEL RECEIPT GUEST NUMBER TESTS");
		ArrayList<Pair<Integer, Integer>> adultChildNumberPairs = generateChildAdultCountPairs();
		mUser.setHotelCityToRandomUSCity();
		mDriver.hotelsSearchScreen().clickSearchEditText();
		mDriver.hotelsSearchScreen().clickToClearSearchEditText();
		mDriver.hotelsSearchScreen().enterSearchText(mUser.getHotelSearchCity());
		mDriver.enterLog(TAG, "Searched for hotels in city: " + mUser.getHotelSearchCity());
		for (int i = 0; i < adultChildNumberPairs.size(); i++) {
			Pair<Integer, Integer> currentPair = adultChildNumberPairs.get(i);
			mDriver.hotelsSearchScreen().clickOnGuestsButton();
			setGuests(currentPair.first, currentPair.second);
			mDriver.hotelsSearchScreen().guestPicker().clickOnSearchButton();
			mDriver.waitForStringToBeGone(mDriver.hotelsSearchScreen().searchingForHotels());
			for (int j = 0; j < 2; j++) {
				mDriver.hotelsSearchScreen().selectHotelFromList(j);
				mDriver.delay();
				mDriver.hotelsDetailsScreen().clickSelectButton();
				int numberOfRooms = mDriver.hotelsRoomsRatesScreen().roomList().getChildCount() - 1;
				for (int k = 0; k < numberOfRooms; k++) {
					if (!handleDialogPopupPresent()) {
						mDriver.hotelsRoomsRatesScreen().selectRoom(k);
						mDriver.waitForStringToBeGone(mDriver.hotelsCheckoutScreen().calculatingTaxesAndFees());
						if (!handleDialogPopupPresent()) {
							String receiptGuestString = mDriver.hotelsCheckoutScreen().hotelReceiptModel()
									.guestsTextView()
									.getText().toString();
							int totalNumberOfGuests = currentPair.first + currentPair.second;
							String expectedGuestString = mRes
									.getQuantityString(R.plurals.number_of_guests, totalNumberOfGuests,
											totalNumberOfGuests);
							assertEquals(expectedGuestString, receiptGuestString);
							mDriver.enterLog(TAG, "Receipt's guest string matched expected guest string.");
							mDriver.goBack();
							mDriver.delay();
						}
					}
				}
				mDriver.goBack();
				mDriver.delay(1);
				mDriver.goBack();
				mDriver.delay(1);
			}
		}
		mDriver.enterLog(TAG, "END: HOTEL RECEIPT GUEST NUMBER TESTS");
	}

	public void testHotelNightsNumber() throws Exception {
		mDriver.enterLog(TAG, "START: HOTEL RECEIPT NIGHTS NUMBER TESTS");
		int dateOffsets[] = {
			3, 7, 10, 25,
		};
		for (int i = 0; i < dateOffsets.length; i++) {
			int numberOfNights = dateOffsets[i];
			mUser.setHotelCityToRandomUSCity();
			mDriver.hotelsSearchScreen().clickSearchEditText();
			mDriver.hotelsSearchScreen().clickToClearSearchEditText();
			mDriver.hotelsSearchScreen().enterSearchText(mUser.getHotelSearchCity());
			mDriver.enterLog(TAG, "Searching for hotels in city: " + mUser.getHotelSearchCity());
			mDriver.hotelsSearchScreen().clickOnCalendarButton();
			mDriver.delay();

			//Always do this just in case.
			CalendarTouchUtils.clickPrevMonth(mDriver, mDriver.hotelsSearchScreen().calendarDatePicker());

			Time now = new Time();
			now.setToNow();
			mDriver.hotelsSearchScreen().clickDate(1);
			mDriver.hotelsSearchScreen().clickDate(0);
			Time offsetTime = now;
			offsetTime.monthDay += numberOfNights;
			offsetTime.normalize(false);
			mDriver.hotelsSearchScreen().clickDate(offsetTime);

			mDriver.hotelsSearchScreen().clickOnGuestsButton();
			mDriver.hotelsSearchScreen().guestPicker().clickOnSearchButton();
			mDriver.waitForStringToBeGone(mDriver.hotelsSearchScreen().searchingForHotels());
			mDriver.enterLog(TAG, "Testing for hotels for a stay of " + numberOfNights + " nights.");

			for (int j = 0; j < 2; j++) {
				mDriver.hotelsSearchScreen().selectHotelFromList(j);
				mDriver.delay();
				mDriver.hotelsDetailsScreen().clickSelectButton();
				int numberOfRooms = mDriver.hotelsRoomsRatesScreen().roomList().getChildCount() - 1;
				for (int k = 0; k < numberOfRooms; k++) {
					if (!handleDialogPopupPresent()) {
						mDriver.hotelsRoomsRatesScreen().selectRoom(k);
						mDriver.waitForStringToBeGone(mDriver.hotelsCheckoutScreen().calculatingTaxesAndFees());
						if (!handleDialogPopupPresent()) {
							String expectedNightsString = mRes.getQuantityString(R.plurals.number_of_nights,
									numberOfNights,
									numberOfNights);
							String shownNightsString = mDriver.hotelsCheckoutScreen().hotelReceiptModel()
									.nightsTextView()
									.getText().toString();
							assertEquals(expectedNightsString, shownNightsString);
							mDriver.enterLog(TAG,
									"Nights string in hotel receipt matched the number of nights selected.");
							mDriver.hotelsCheckoutScreen().hotelReceiptModel().clickGrandTotalTextView();
							assertTrue(mDriver.searchText(shownNightsString));
							mDriver.enterLog(TAG,
									"Number of nights selected is properly displayed in cost summary fragment.");
							assertTrue(mDriver.searchText(mDriver.hotelsCheckoutScreen().hotelReceiptModel()
									.costSummaryString()));
							mDriver.enterLog(TAG, "Cost summary string is shown in cost summary fragment.");
							mDriver.goBack();
							mDriver.goBack();
							mDriver.delay();
						}
					}
				}
				mDriver.goBack();
				mDriver.delay(1);
				mDriver.goBack();
				mDriver.delay(1);
			}
		}
		mDriver.enterLog(TAG, "END: HOTEL RECEIPT NIGHTS NUMBER TESTS");
	}

	public void testUIElementsPresent() throws Exception {
		mDriver.enterLog(TAG, "START: UI ELEMENTS PRESENT TESTS");
		mUser.setHotelCityToRandomUSCity();
		mDriver.hotelsSearchScreen().clickSearchEditText();
		mDriver.hotelsSearchScreen().clickToClearSearchEditText();
		mDriver.hotelsSearchScreen().enterSearchText(mUser.getHotelSearchCity());
		mDriver.hotelsSearchScreen().clickOnGuestsButton();
		mDriver.hotelsSearchScreen().guestPicker().clickOnSearchButton();
		mDriver.waitForStringToBeGone(mDriver.hotelsSearchScreen().searchingForHotels());
		mDriver.enterLog(TAG, "Searched for hotels in city: " + mUser.getHotelSearchCity());

		for (int j = 0; j < 2; j++) {
			mDriver.hotelsSearchScreen().selectHotelFromList(j);
			mDriver.delay();
			mDriver.hotelsDetailsScreen().clickSelectButton();
			int numberOfRooms = mDriver.hotelsRoomsRatesScreen().roomList().getChildCount() - 1;
			for (int k = 0; k < numberOfRooms; k++) {
				if (!handleDialogPopupPresent()) {
					mDriver.hotelsRoomsRatesScreen().selectRoom(k);
					mDriver.waitForStringToBeGone(mDriver.hotelsCheckoutScreen().calculatingTaxesAndFees());
					if (!handleDialogPopupPresent()) {
						HotelReceiptModel receiptModel = mDriver.hotelsCheckoutScreen().hotelReceiptModel();
						String hotelName = mDriver.hotelsCheckoutScreen().hotelNameView().getText().toString();
						mDriver.enterLog(TAG, "Looking at hotel: " + hotelName);
						String nightsString = receiptModel.nightsTextView().getText().toString();
						String guestsString = receiptModel.guestsTextView().getText().toString();
						String priceString = receiptModel.priceTextView().getText().toString();
						mDriver.hotelsCheckoutScreen().clickCheckoutButton();
						mDriver.delay(1);
						String secondNightsString = receiptModel.nightsTextView().getText().toString();
						String secondGuestsString = receiptModel.guestsTextView().getText().toString();
						String secondPriceString = receiptModel.priceTextView().getText().toString();
						assertEquals(nightsString, secondNightsString);
						mDriver.enterLog(TAG, "Nights string remained consistent after checkout scroll down.");
						assertEquals(guestsString, secondGuestsString);
						mDriver.enterLog(TAG, "Guests string remained consistent after checkout scroll down.");
						assertEquals(priceString, secondPriceString);
						mDriver.enterLog(TAG, "Price string remained consistent after checkout scroll down.");
						mDriver.goBack();
						mDriver.goBack();
						mDriver.delay();
					}
				}
			}
			mDriver.goBack();
			mDriver.delay(1);
			mDriver.goBack();
			mDriver.delay(1);
		}
		mDriver.enterLog(TAG, "END: UI ELEMENTS PRESENT TESTS");
	}

	private void setGuests(int adults, int children) {
		mDriver.enterLog(TAG, "Setting adults to: " + adults + " and children to: " + children);
		for (int i = 6; i >= 1; i--) {
			mDriver.hotelsSearchScreen().guestPicker().clickDecrementAdultsButton();
		}
		for (int i = 4; i >= 0; i--) {
			mDriver.hotelsSearchScreen().guestPicker().clickDecrementChildrenButton();
		}

		for (int i = 1; i < adults; i++) {
			mDriver.hotelsSearchScreen().guestPicker().clickIncrementAdultsButton();
		}

		for (int i = 0; i < children; i++) {
			mDriver.hotelsSearchScreen().guestPicker().clickIncrementChildrenButton();
		}
	}

	private boolean handleDialogPopupPresent() {
		String okString = mRes.getString(R.string.ok);
		if (mDriver.searchText(okString, 1, false, true)) {
			mDriver.clickOnText(okString);
			return true;
		}
		return false;
	}

	@Override
	protected void tearDown() throws Exception {
		mDriver.enterLog(TAG, "tearing down...");
		mDriver.finishOpenedActivities();
	}

}
