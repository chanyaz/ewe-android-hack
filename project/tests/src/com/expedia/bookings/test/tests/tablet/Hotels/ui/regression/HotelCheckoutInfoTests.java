package com.expedia.bookings.test.tests.tablet.Hotels.ui.regression;

import java.util.ArrayList;
import java.util.Random;

import org.joda.time.LocalDate;

import android.test.ActivityInstrumentationTestCase2;
import android.util.Pair;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.tests.pageModels.tablet.Checkout;
import com.expedia.bookings.test.tests.pageModels.tablet.Common;
import com.expedia.bookings.test.tests.pageModels.tablet.Launch;
import com.expedia.bookings.test.tests.pageModels.tablet.Results;
import com.expedia.bookings.test.tests.pageModels.tablet.Settings;
import com.expedia.bookings.test.utils.EspressoUtils;

import static com.expedia.bookings.test.tests.pageModels.tablet.Common.pressBack;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.click;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.scrollTo;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;

/**
 * Created by dmadan on 6/13/14.
 */
public class HotelCheckoutInfoTests extends ActivityInstrumentationTestCase2 {

	public HotelCheckoutInfoTests() {
		super(SearchActivity.class);
	}

	private static final String TAG = HotelCheckoutInfoTests.class.getSimpleName();

	@Override
	public void runTest() throws Throwable {
		// These tests are only applicable to tablets
		if (ExpediaBookingApp.useTabletInterface(getInstrumentation().getTargetContext())) {

			Settings.clearPrivateData(getInstrumentation());
			// Point to the mock server
			Settings.setCustomServer(getInstrumentation(), "mocke3.mobiata.com");

			// Espresso will not launch our activity for us, we must launch it via getActivity().
			getActivity();

			super.runTest();
		}
	}

	public void testHotelHeaderInfo() throws Exception {
		Common.enterLog(TAG, "START: HOTEL HEADER INFO TESTS");
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("Detroit, MI");
		Launch.clickSuggestion("Detroit, MI");
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);
		Results.clickDate(startDate, endDate);
		Results.clickSearchNow();
		Results.swipeUpHotelList();

		Results.clickHotelWithName("happy_path");
		String hotelName = EspressoUtils.getText(R.id.hotel_header_hotel_name);
		Results.addHotel().perform(scrollTo());
		String roomName = EspressoUtils.getTextWithSibling(R.id.text_room_description, R.id.text_bed_type);
		String bedType = EspressoUtils.getTextWithSibling(R.id.text_bed_type, R.id.text_room_description);
		Results.clickAddHotel();
		Results.clickBookHotel();

		String checkoutHotelName = EspressoUtils.getText(R.id.name_text_view);
		String checkoutRoomName = EspressoUtils.getText(R.id.primary_title_text_view);
		String checkoutBedType = EspressoUtils.getText(R.id.secondary_title_text_view);

		assertEquals(hotelName, checkoutHotelName);
		assertEquals(roomName, checkoutRoomName);
		assertEquals(bedType, checkoutBedType);
		Checkout.clickGrandTotalTextView();
		Common.checkDisplayed(Checkout.costSummaryText());
		pressBack();
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
		Results.clickDate(startDate, endDate);

		for (int i = 0; i < adultChildNumberPairs.size(); i++) {
			Pair<Integer, Integer> currentPair = adultChildNumberPairs.get(i);
			Results.clickTravelerButton();
			setGuests(currentPair.first, currentPair.second);
			Results.clickSearchNow();
			Results.swipeUpHotelList();
			Results.clickHotelWithName("happy_path");
			Results.clickAddHotel();
			Results.clickBookHotel();

			String receiptGuestString = EspressoUtils.getText(R.id.num_travelers_text_view);
			int totalNumberOfGuests = currentPair.first + currentPair.second;
			String expectedGuestString = getActivity().getResources().getQuantityString(R.plurals.number_of_guests, totalNumberOfGuests, totalNumberOfGuests);
			assertEquals(expectedGuestString, receiptGuestString);
			pressBack();
		}
	}

	public void testHotelNightsNumber() throws Exception {
		Common.enterLog(TAG, "START: HOTEL RECEIPT NIGHTS NUMBER TESTS");
		int dateOffsets[] = {
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
			Results.clickDate(startDate, endDate);
			Results.clickSearchNow();
			Results.swipeUpHotelList();
			Common.enterLog(TAG, "Testing for hotels for a stay of " + numberOfNights + " nights.");

			Results.clickHotelWithName("happy_path");
			String expectedNightsString = getActivity().getResources().getQuantityString(R.plurals.number_of_nights, numberOfNights, numberOfNights);
			Results.clickAddHotel();
			Results.clickBookHotel();
			String shownDateText = EspressoUtils.getText(R.id.dates_text_view);
			assertTrue(shownDateText.contains(expectedNightsString.toLowerCase()));
			Common.enterLog(TAG, "Nights string in hotel receipt matched the number of nights selected.");
			Checkout.clickGrandTotalTextView();
			EspressoUtils.assertTrue(expectedNightsString);
			Common.enterLog(TAG, "Number of nights selected is properly displayed in cost summary fragment.");
			Common.checkDisplayed(Checkout.costSummaryText());
			Common.enterLog(TAG, "Cost summary string is shown in cost summary fragment.");
			pressBack();
			pressBack();
			pressBack();
			pressBack();
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

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		// These tests are only applicable to phones
		if (ExpediaBookingApp.useTabletInterface(getInstrumentation().getTargetContext())) {
			Common.pressBackOutOfApp();
		}
	}
}


