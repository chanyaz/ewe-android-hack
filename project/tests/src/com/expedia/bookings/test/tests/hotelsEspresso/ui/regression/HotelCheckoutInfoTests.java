package com.expedia.bookings.test.tests.hotelsEspresso.ui.regression;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import org.joda.time.LocalDate;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Pair;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.PhoneSearchActivity;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelReceiptModel;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.ScreenActions;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsCheckoutScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsDetailsScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsGuestPicker;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsRoomsRatesScreen;
import com.expedia.bookings.test.tests.pageModelsEspresso.hotels.HotelsSearchScreen;
import com.expedia.bookings.test.utils.EspressoUtils;
import com.expedia.bookings.test.utils.HotelsUserData;
import com.expedia.bookings.utils.ClearPrivateDataUtil;
import com.google.android.apps.common.testing.ui.espresso.DataInteraction;
import com.google.android.apps.common.testing.ui.espresso.Espresso;
import com.mobiata.android.util.SettingUtils;

import static com.expedia.bookings.test.utilsEspresso.ViewActions.getRating;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onData;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.anything;

/**
 * Created by dmadan on 5/16/14.
 */
public class HotelCheckoutInfoTests extends ActivityInstrumentationTestCase2<PhoneSearchActivity> {
	public HotelCheckoutInfoTests() {
		super(PhoneSearchActivity.class);
	}

	private static final String TAG = HotelCheckoutInfoTests.class.getSimpleName();
	Context mContext;
	SharedPreferences mPrefs;
	Resources mRes;
	HotelsUserData mUser;

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

	public void testHotelHeaderInfo() throws Exception {
		ScreenActions.enterLog(TAG, "START: HOTEL HEADER INFO TESTS");
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText("Boston, MA");
		HotelsSearchScreen.clickOnGuestsButton();
		HotelsSearchScreen.guestPicker().clickOnSearchButton();

		for (int i = 1; i < 3; i++) {
			HotelsSearchScreen.clickListItem(i);
			HotelsDetailsScreen.clickSelectButton();
			EspressoUtils.getListCount(HotelsRoomsRatesScreen.roomList(), "numberOfRooms", 1);
			int numberOfRooms = mPrefs.getInt("numberOfRooms", 0) - 1;
			EspressoUtils.getValues("hotelName", R.id.name_text_view);
			String hotelName = mPrefs.getString("hotelName", "");
			HotelsRoomsRatesScreen.hotelRatingBar().perform(getRating("starRating"));
			float hotelRating = mPrefs.getFloat("starRating", 0);
			ScreenActions.enterLog(TAG, "Test is looking at hotel with name: " + hotelName);
			for (int j = 0; j < numberOfRooms; j++) {
				DataInteraction rowModel = onData(anything()).inAdapterView(withId(android.R.id.list)).atPosition(j);
				EspressoUtils.getListItemValues(rowModel, R.id.room_description_text_view, "roomName");
				String roomName = mPrefs.getString("roomName", "");
				HotelsRoomsRatesScreen.selectRoomItem(j);
				EspressoUtils.getValues("checkoutHotelName", R.id.title);
				String checkoutHotelName = mPrefs.getString("checkoutHotelName", "");
				HotelsDetailsScreen.ratingBar().perform(getRating("checkoutHotelRating"));
				float checkoutHotelRating = mPrefs.getFloat("checkoutHotelRating", 0);
				EspressoUtils.getValues("checkoutRoomName", R.id.room_type_description_text_view);
				String checkoutRoomName = mPrefs.getString("checkoutRoomName", "");

				assertEquals(hotelName, checkoutHotelName);
				ScreenActions.enterLog(TAG, "Assertion Passed: Hotel name from rooms and rates matches name in hotel details");
				assertEquals(hotelRating, checkoutHotelRating);
				ScreenActions.enterLog(TAG, "Assertion Passed: Hotel rating from rooms and rates matches rating in hotel details");
				assertEquals(roomName, checkoutRoomName);
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
		mUser.setHotelCityToRandomUSCity();
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText("Boston, MA");
		for (int i = 0; i < adultChildNumberPairs.size(); i++) {
			Pair<Integer, Integer> currentPair = adultChildNumberPairs.get(i);
			HotelsSearchScreen.clickOnGuestsButton();
			setGuests(currentPair.first, currentPair.second);
			HotelsSearchScreen.guestPicker().clickOnSearchButton();
			for (int j = 1; j < 3; j++) {
				HotelsSearchScreen.clickListItem(j);
				HotelsDetailsScreen.clickSelectButton();
				EspressoUtils.getListCount(HotelsRoomsRatesScreen.roomList(), "numberOfRooms", 1);
				int numberOfRooms = mPrefs.getInt("numberOfRooms", 0) - 1;
				for (int k = 0; k < numberOfRooms; k++) {
					HotelsRoomsRatesScreen.selectRoomItem(k);
					EspressoUtils.getValues("receiptGuestString", R.id.guests_text);
					String receiptGuestString = mPrefs.getString("receiptGuestString", "");
					int totalNumberOfGuests = currentPair.first + currentPair.second;
					String expectedGuestString = mRes.getQuantityString(R.plurals.number_of_guests, totalNumberOfGuests, totalNumberOfGuests);
					assertEquals(expectedGuestString, receiptGuestString);
					ScreenActions.enterLog(TAG, "Receipt's guest string matched expected guest string.");
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
		int dateOffsets[] = {
			3, 7, 10, 25,
		};
		for (int i = 0; i < dateOffsets.length; i++) {
			int numberOfNights = dateOffsets[i];
			HotelsSearchScreen.clickSearchEditText();
			HotelsSearchScreen.clickToClearSearchEditText();
			HotelsSearchScreen.enterSearchText("Boston, MA");
			Calendar cal = Calendar.getInstance();
			int year = cal.get(cal.YEAR);
			int month = cal.get(cal.MONTH) + 1;
			LocalDate mStartDate = new LocalDate(year, month, 5);
			LocalDate mEndDate = new LocalDate(year, month, 5 + numberOfNights);
			HotelsSearchScreen.clickOnCalendarButton();
			HotelsSearchScreen.clickDate(mStartDate, mEndDate);
			HotelsSearchScreen.clickOnGuestsButton();
			HotelsSearchScreen.guestPicker().clickOnSearchButton();
			ScreenActions.enterLog(TAG, "Testing for hotels for a stay of " + numberOfNights + " nights.");

			for (int j = 1; j < 3; j++) {
				HotelsSearchScreen.clickListItem(j);
				HotelsDetailsScreen.clickSelectButton();
				EspressoUtils.getListCount(HotelsRoomsRatesScreen.roomList(), "numberOfRooms", 1);
				int numberOfRooms = mPrefs.getInt("numberOfRooms", 0) - 1;
				for (int k = 0; k < numberOfRooms; k++) {
					HotelsRoomsRatesScreen.selectRoomItem(k);
					String expectedNightsString = mRes.getQuantityString(R.plurals.number_of_nights, numberOfNights, numberOfNights);
					EspressoUtils.getValues("shownNightsString", R.id.nights_text);
					String shownNightsString = mPrefs.getString("shownNightsString", "");
					assertEquals(expectedNightsString, shownNightsString);
					ScreenActions.enterLog(TAG, "Nights string in hotel receipt matched the number of nights selected.");
					HotelReceiptModel.clickGrandTotalTextView();
					EspressoUtils.assertTrue(shownNightsString);
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
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText("Boston, MA");
		HotelsSearchScreen.clickOnGuestsButton();
		HotelsSearchScreen.guestPicker().clickOnSearchButton();

		for (int j = 1; j < 3; j++) {
			HotelsSearchScreen.clickListItem(j);
			HotelsDetailsScreen.clickSelectButton();
			EspressoUtils.getListCount(HotelsRoomsRatesScreen.roomList(), "numberOfRooms", 1);
			int numberOfRooms = mPrefs.getInt("numberOfRooms", 0) - 1;
			for (int k = 0; k < numberOfRooms; k++) {
				HotelsRoomsRatesScreen.selectRoomItem(k);
				EspressoUtils.getValues("hotelName", R.id.title);
				String hotelName = mPrefs.getString("hotelName", "");
				ScreenActions.enterLog(TAG, "Looking at hotel: " + hotelName);

				EspressoUtils.getValues("nightsString", R.id.nights_text);
				String nightsString = mPrefs.getString("nightsString", "");
				EspressoUtils.getValues("guestsString", R.id.guests_text);
				String guestsString = mPrefs.getString("guestsString", "");
				EspressoUtils.getValues("priceString", R.id.price_text);
				String priceString = mPrefs.getString("priceString", "");
				HotelsCheckoutScreen.clickCheckoutButton();

				EspressoUtils.getValues("nightsString", R.id.nights_text);
				String secondNightsString = mPrefs.getString("nightsString", "");
				EspressoUtils.getValues("guestsString", R.id.guests_text);
				String secondGuestsString = mPrefs.getString("guestsString", "");
				EspressoUtils.getValues("priceString", R.id.price_text);
				String secondPriceString = mPrefs.getString("priceString", "");

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

