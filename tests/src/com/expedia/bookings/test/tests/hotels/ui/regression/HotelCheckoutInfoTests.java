package com.expedia.bookings.test.tests.hotels.ui.regression;

import java.util.ArrayList;
import java.util.Random;

import android.util.Pair;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.PhoneSearchActivity;
import com.expedia.bookings.test.tests.pageModels.hotels.HotelReceiptModel;
import com.expedia.bookings.test.tests.pageModels.hotels.RoomsAndRatesRow;
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
		mUser.setHotelCityToRandomUSCity();
		mDriver.hotelsSearchScreen().clickSearchEditText();
		mDriver.hotelsSearchScreen().clickToClearSearchEditText();
		mDriver.hotelsSearchScreen().enterSearchText(mUser.getHotelSearchCity());
		mDriver.hotelsSearchScreen().clickOnGuestsButton();
		mDriver.hotelsSearchScreen().guestPicker().clickOnSearchButton();
		mDriver.waitForStringToBeGone(mDriver.hotelsSearchScreen().searchingForHotels());
		for (int i = 0; i < 2; i++) {
			mDriver.hotelsSearchScreen().selectHotelFromList(i);
			mDriver.hotelsDetailsScreen().clickSelectButton();
			int numberOfRooms = mDriver.hotelsRoomsRatesScreen().roomList().getChildCount() - 1;
			String hotelName = mDriver.hotelsRoomsRatesScreen().hotelNameTextView().getText().toString();
			float hotelRating = mDriver.hotelsRoomsRatesScreen().hotelRatingBar().getRating();
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
						assertEquals(hotelRating, checkoutHotelRating);
						assertEquals(roomName, checkoutRoomName);
						mDriver.hotelsCheckoutScreen().hotelReceiptModel().clickGrandTotalTextView();
						assertTrue(mDriver.searchText(mDriver.hotelsCheckoutScreen().hotelReceiptModel()
								.costSummaryString()));
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

	public void testHotelReceiptGuestNumber() throws Exception {
		ArrayList<Pair<Integer, Integer>> adultChildNumberPairs = new ArrayList<Pair<Integer, Integer>>();
		adultChildNumberPairs.add(new Pair<Integer, Integer>(1, 0));
		adultChildNumberPairs.add(new Pair<Integer, Integer>(2, 0));
		adultChildNumberPairs.add(new Pair<Integer, Integer>(3, 0));
		adultChildNumberPairs.add(new Pair<Integer, Integer>(4, 0));
		adultChildNumberPairs.add(new Pair<Integer, Integer>(5, 0));
		adultChildNumberPairs.add(new Pair<Integer, Integer>(6, 0));
		adultChildNumberPairs.add(new Pair<Integer, Integer>(1, 1));
		adultChildNumberPairs.add(new Pair<Integer, Integer>(1, 2));
		adultChildNumberPairs.add(new Pair<Integer, Integer>(1, 3));
		adultChildNumberPairs.add(new Pair<Integer, Integer>(1, 4));
		adultChildNumberPairs.add(new Pair<Integer, Integer>(2, 1));
		adultChildNumberPairs.add(new Pair<Integer, Integer>(2, 2));
		adultChildNumberPairs.add(new Pair<Integer, Integer>(2, 3));
		adultChildNumberPairs.add(new Pair<Integer, Integer>(2, 4));
		adultChildNumberPairs.add(new Pair<Integer, Integer>(3, 1));
		adultChildNumberPairs.add(new Pair<Integer, Integer>(3, 2));
		adultChildNumberPairs.add(new Pair<Integer, Integer>(3, 3));
		adultChildNumberPairs.add(new Pair<Integer, Integer>(4, 1));
		adultChildNumberPairs.add(new Pair<Integer, Integer>(2, 2));

		ArrayList<Pair<Integer, Integer>> testSelection = new ArrayList<Pair<Integer, Integer>>();
		Random random = new Random();
		for (int i = 0; i < 3; i++) {
			int randomPairIndex = random.nextInt(adultChildNumberPairs.size());
			testSelection.add(adultChildNumberPairs.get(randomPairIndex));
		}
		mUser.setHotelCityToRandomUSCity();
		mDriver.hotelsSearchScreen().clickSearchEditText();
		mDriver.hotelsSearchScreen().clickToClearSearchEditText();
		mDriver.hotelsSearchScreen().enterSearchText(mUser.getHotelSearchCity());
		for (int i = 0; i < testSelection.size(); i++) {
			Pair<Integer, Integer> currentPair = testSelection.get(i);
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
	}

	public void testHotelNightsNumber() throws Exception {
		int dateOffsets[] = {
			3, 7, 10, 25,
		};
		for (int i = 0; i < dateOffsets.length; i++) {
			int numberOfNights = dateOffsets[i];
			mUser.setHotelCityToRandomUSCity();
			mDriver.hotelsSearchScreen().clickSearchEditText();
			mDriver.hotelsSearchScreen().clickToClearSearchEditText();
			mDriver.hotelsSearchScreen().enterSearchText(mUser.getHotelSearchCity());
			mDriver.hotelsSearchScreen().clickOnCalendarButton();
			mDriver.delay();
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
							mDriver.hotelsCheckoutScreen().hotelReceiptModel().clickGrandTotalTextView();
							assertTrue(mDriver.searchText(shownNightsString));
							assertTrue(mDriver.searchText(mDriver.hotelsCheckoutScreen().hotelReceiptModel()
									.costSummaryString()));
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
	}

	public void testUIElementsPresent() throws Exception {
		mUser.setHotelCityToRandomUSCity();
		mDriver.hotelsSearchScreen().clickSearchEditText();
		mDriver.hotelsSearchScreen().clickToClearSearchEditText();
		mDriver.hotelsSearchScreen().enterSearchText(mUser.getHotelSearchCity());
		mDriver.hotelsSearchScreen().clickOnGuestsButton();
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
						HotelReceiptModel receiptModel = mDriver.hotelsCheckoutScreen().hotelReceiptModel();
						String nightsString = receiptModel.nightsTextView().getText().toString();
						String guestsString = receiptModel.guestsTextView().getText().toString();
						String priceString = receiptModel.priceTextView().getText().toString();
						mDriver.hotelsCheckoutScreen().clickCheckoutButton();
						mDriver.delay(1);
						String secondNightsString = receiptModel.nightsTextView().getText().toString();
						String secondGuestsString = receiptModel.guestsTextView().getText().toString();
						String secondPriceString = receiptModel.priceTextView().getText().toString();
						assertEquals(nightsString, secondNightsString);
						assertEquals(guestsString, secondGuestsString);
						assertEquals(priceString, secondPriceString);
						mDriver.goBack();
						mDriver.goBack();
						mDriver.delay();
						// coupon dialog pops up.
						// TOS screen
					}
				}
			}
			mDriver.goBack();
			mDriver.delay(1);
			mDriver.goBack();
			mDriver.delay(1);
		}
	}

	// Checkout button

	// After scrolled down, verify: 
	/*
	 * Log in with Expedia button
	 * Buy with Google
	 * Checkout info
	 * guest details
	 * payment details
	 * Enter a coupon or promotion code
	 * By accepting blah blah blah
	 * Rules and restrictions link takes you to TOS Activity
	 */

	private void setGuests(int adults, int children) {
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
