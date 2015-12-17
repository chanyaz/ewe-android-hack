package com.expedia.bookings.test.ui.tablet.tests.hotels;

import java.util.ArrayList;
import java.util.Random;

import org.joda.time.LocalDate;

import android.util.Pair;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.tablet.pagemodels.Checkout;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;
import com.expedia.bookings.test.ui.tablet.pagemodels.HotelDetails;
import com.expedia.bookings.test.ui.tablet.pagemodels.Launch;
import com.expedia.bookings.test.ui.tablet.pagemodels.Results;
import com.expedia.bookings.test.ui.tablet.pagemodels.Search;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.TabletTestCase;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by dmadan on 6/13/14.
 */
public class HotelCheckoutInfoTests extends TabletTestCase {

	private static final String TAG = HotelCheckoutInfoTests.class.getSimpleName();

	public void testHotelHeaderInfo() throws Exception {
		Common.enterLog(TAG, "START: HOTEL HEADER INFO TESTS");
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("Detroit, MI");
		Launch.clickSuggestion("Detroit, MI");
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);
		Search.clickDate(startDate, endDate);
		Search.clickSearchPopupDone();
		Results.swipeUpHotelList();

		Results.clickHotelAtIndex(1);
		String hotelName = EspressoUtils.getText(R.id.hotel_header_hotel_name);
		HotelDetails.addHotel().perform(scrollTo());
		String roomName = EspressoUtils.getTextWithSibling(R.id.text_room_description, R.id.text_bed_type);
		String bedType = EspressoUtils.getTextWithSibling(R.id.text_bed_type, R.id.text_room_description);
		HotelDetails.clickAddHotel();
		Results.clickBookHotel();

		String checkoutHotelName = EspressoUtils.getText(R.id.name_text_view);
		String checkoutRoomName = EspressoUtils.getText(R.id.room_type_text_view);
		String checkoutBedType = EspressoUtils.getText(R.id.bed_type_text_view);

		assertEquals(hotelName, checkoutHotelName);
		assertEquals(roomName, checkoutRoomName);
		assertEquals(bedType, checkoutBedType);
		Checkout.clickGrandTotalTextView();
		Common.checkDisplayed(Checkout.costSummaryText());
		Common.pressBack();
	}

	public void testHotelReceiptGuestNumber() throws Exception {
		Common.enterLog(TAG, "START: HOTEL RECEIPT GUEST NUMBER TESTS");
		ArrayList<Pair<Integer, Integer>> adultChildNumberPairs = generateChildAdultCountPairs();
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("Detroit, MI");
		Launch.clickSuggestion("Detroit, MI");
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);
		Search.clickDate(startDate, endDate);
		Search.clickSearchPopupDone();

		for (int i = 0; i < adultChildNumberPairs.size(); i++) {
			Pair<Integer, Integer> currentPair = adultChildNumberPairs.get(i);
			Search.clickTravelerButton();
			setGuests(currentPair.first, currentPair.second);
			Search.clickSearchPopupDone();
			Results.swipeUpHotelList();
			Results.clickHotelAtIndex(1);
			HotelDetails.clickAddHotel();
			Results.clickBookHotel();

			String receiptGuestString = EspressoUtils.getText(R.id.num_travelers_text_view);
			int totalNumberOfGuests = currentPair.first + currentPair.second;
			String expectedGuestString = getActivity().getResources().getQuantityString(R.plurals.number_of_guests, totalNumberOfGuests, totalNumberOfGuests);
			assertEquals(expectedGuestString, receiptGuestString);
			Common.pressBack();
		}
	}

	public void testHotelNightsNumber() throws Exception {
		Common.enterLog(TAG, "START: HOTEL RECEIPT NIGHTS NUMBER TESTS");
		int[] dateOffsets = {
			3, 7, 10, 25,
		};

		for (int i = 0; i < dateOffsets.length; i++) {
			int numberOfNights = dateOffsets[i];
			Launch.clickSearchButton();
			Launch.clickDestinationEditText();
			Launch.typeInDestinationEditText("Detroit, MI");
			Launch.clickSuggestion("Detroit, MI");
			LocalDate startDate = LocalDate.now().plusDays(35);
			LocalDate endDate = LocalDate.now().plusDays(35 + numberOfNights);
			Search.clickDate(startDate, endDate);
			Search.clickSearchPopupDone();
			Results.swipeUpHotelList();
			Common.enterLog(TAG, "Testing for hotels for a stay of " + numberOfNights + " nights.");

			Results.clickHotelAtIndex(1);
			String expectedNightsString = getActivity().getResources().getQuantityString(R.plurals.number_of_nights, numberOfNights, numberOfNights);
			HotelDetails.clickAddHotel();
			Results.clickBookHotel();
			Checkout.clickGrandTotalTextView();
			EspressoUtils.assertViewWithTextIsDisplayed(expectedNightsString);
			Common.enterLog(TAG, "Number of nights selected is properly displayed in cost summary fragment.");
			Common.checkDisplayed(Checkout.costSummaryText());
			Common.enterLog(TAG, "Cost summary string is shown in cost summary fragment.");
			Common.pressBack();
			Common.pressBack();
			Common.pressBack();
			Common.pressBack();
		}
	}

	// helper methods

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
			Common.enterLog(TAG, "Added pair: " + newPair.first + ", " + newPair.second);
		}
		return returnableList;
	}

	private void setGuests(int adults, int children) {
		Common.enterLog(TAG, "Setting adults to: " + adults + " and children to: " + children);
		for (int i = 6; i >= 1; i--) {
			onView(withId(R.id.adults_minus)).perform(click());
		}

		for (int i = 4; i >= 0; i--) {
			onView(withId(R.id.children_minus)).perform(click());
		}

		for (int i = 1; i < adults; i++) {
			onView(withId(R.id.adults_plus)).perform(click());
		}

		for (int i = 0; i < children; i++) {
			onView(withId(R.id.children_plus)).perform(click());
		}
	}
}


