package com.expedia.bookings.test.tests.hotels.ui.regression;

import java.util.ArrayList;
import java.util.Random;

import android.text.format.DateUtils;
import android.util.Pair;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.test.utils.ConfigFileUtils;
import com.expedia.bookings.test.utils.CustomActivityInstrumentationTestCase;
import com.expedia.bookings.utils.CalendarUtils;

public class HotelConfirmationTests extends CustomActivityInstrumentationTestCase<SearchActivity> {

	private static final String TAG = HotelConfirmationTests.class.getSimpleName();
	ConfigFileUtils mConfigFileUtils;
	String mEnvironment;
	int mNumberOfGuests;
	String mDateRangeString;
	String mHotelName;

	public HotelConfirmationTests() {
		super(SearchActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	private void getToCheckout() throws Exception {
		ArrayList<Pair<Integer, Integer>> guestPairList = generateChildAdultCountPairs();
		Pair<Integer, Integer> pair = guestPairList.get(0);
		mNumberOfGuests = pair.first + pair.second;

		mDriver.launchScreen().openMenuDropDown();
		mDriver.launchScreen().pressSettings();
		mDriver.settingsScreen().setSpoofBookings();
		mDriver.goBack();
		mDriver.launchScreen().launchHotels();

		mUser.setHotelCityToRandomUSCity();
		mDriver.hotelsSearchScreen().clickSearchEditText();
		mDriver.hotelsSearchScreen().clickToClearSearchEditText();
		mDriver.enterLog(TAG, "Hotel search city is: " + mUser.getHotelSearchCity());
		mDriver.hotelsSearchScreen().enterSearchText(mUser.getHotelSearchCity());

		mDriver.hotelsSearchScreen().clickOnGuestsButton();
		setGuests(pair.first, pair.second);
		mDriver.hotelsSearchScreen().guestPicker().clickOnSearchButton();
		mDriver.waitForStringToBeGone(mDriver.hotelsSearchScreen().searchingForHotels());
		mDriver.hotelsSearchScreen().selectHotelFromList(1);
		mDriver.delay();
		mDriver.hotelsDetailsScreen().clickSelectButton();
		mDriver.waitForStringToBeGone(mDriver.hotelsRoomsRatesScreen().findingAvailableRooms());
		mDriver.hotelsRoomsRatesScreen().selectRoom(0);
		mDriver.waitForStringToBeGone(mDriver.hotelsCheckoutScreen().calculatingTaxesAndFees());
	}

	public void testLoggedInBookingConfirmation() throws Exception {
		mDriver.enterLog(TAG, "START: Testing confirmation screen after logged-in booking");
		getToCheckout();
		if (!mDriver.handleDialogPopupPresent()) {
			mDriver.enterLog(TAG, "Hotel name is: "
					+ mDriver.hotelsCheckoutScreen().hotelNameView().getText().toString());
			mDriver.hotelsCheckoutScreen().clickCheckoutButton();
			mDriver.hotelsCheckoutScreen().clickLogInButton();
			mDriver.logInScreen().typeTextEmailEditText(mUser.getLoginEmail());
			mDriver.logInScreen().typeTextPasswordEditText(mUser.getLoginPassword());
			mDriver.logInScreen().clickOnLoginButton();
			mDriver.waitForStringToBeGone(mDriver.logInScreen().loggingInDialogString());
			mDriver.hotelsCheckoutScreen().clickSelectPaymentButton();
			try {
				mDriver.commonPaymentMethodScreen().clickOnAddNewCardTextView();
			}
			catch (Exception e) {
				mDriver.enterLog(TAG, "No Add New Card button. Proceeding anyway.");
			}

			mDriver.enterLog(TAG, "Entering credit card with number: " + mUser.getCreditCardNumber());
			mDriver.cardInfoScreen().typeTextCreditCardEditText(mUser.getCreditCardNumber());
			mDriver.enterLog(TAG, "Entering postal code: " + mUser.getAddressPostalCode());
			mDriver.billingAddressScreen().typeTextPostalCode(mUser.getAddressPostalCode());
			mDriver.enterLog(TAG, "Entering cardholder name: " + mUser.getFirstName() + " " + mUser.getLastName());
			mDriver.cardInfoScreen().typeTextNameOnCardEditText(mUser.getFirstName() + " " + mUser.getLastName());
			mDriver.cardInfoScreen().clickOnExpirationDateButton();
			mDriver.delay(1);

			mDriver.enterLog(TAG, "Incrementing credit card exp. month and year by 1");
			mDriver.cardInfoScreen().clickMonthUpButton();
			mDriver.cardInfoScreen().clickYearUpButton();
			mDriver.cardInfoScreen().clickSetButton();
			mDriver.cardInfoScreen().clickOnDoneButton();
			mDriver.cardInfoScreen().clickNoThanksButton();
			mDriver.delay();
			mHotelName = mDriver.hotelsCheckoutScreen().hotelNameView().getText().toString();

			mDriver.enterLog(TAG, "Sliding to checkout");
			mDriver.hotelsCheckoutScreen().slideToCheckout();
			mDriver.delay();

			mDriver.enterLog(TAG, "Entering CCV: " + mUser.getCCV());
			mDriver.cvvEntryScreen().parseAndEnterCVV(mUser.getCCV());
			mDriver.cvvEntryScreen().clickBookButton();
			mDriver.delay(1);
			mDriver.waitForStringToBeGone(mDriver.cvvEntryScreen().booking());

			verifyConfirmationTexts();

			mDriver.hotelsConfirmationScreen().clickDoneButton();
			mDriver.delay();
			mDriver.tripsScreen().swipeToLaunchScreen();
			mDriver.launchScreen().openMenuDropDown();
			mDriver.launchScreen().pressSettings();
			mDriver.settingsScreen().clickToClearPrivateData();
			if (mDriver.searchText(mDriver.settingsScreen().OKString())) {
				mDriver.settingsScreen().clickOKString();
			}
			else if (mDriver.searchText(mDriver.settingsScreen().AcceptString())) {
				mDriver.settingsScreen().clickAcceptString();
			}
			else {
				mDriver.clickOnText("OK");
			}
			mDriver.delay();
			if (mDriver.searchText(mDriver.settingsScreen().OKString())) {
				mDriver.settingsScreen().clickOKString();
			}
			else if (mDriver.searchText(mDriver.settingsScreen().AcceptString())) {
				mDriver.settingsScreen().clickAcceptString();
			}
			else {
				mDriver.clickOnText("OK");
			}
			mDriver.goBack();
		}
	}

	public void testGuestBookingConfirmation() throws Exception {
		mDriver.enterLog(TAG, "START: Testing confirmation screen after guest booking");
		getToCheckout();
		if (!mDriver.handleDialogPopupPresent()) {
			mDriver.enterLog(TAG, "Hotel name is: "
					+ mDriver.hotelsCheckoutScreen().hotelNameView().getText().toString());
			mDriver.hotelsCheckoutScreen().clickCheckoutButton();

			mDriver.hotelsCheckoutScreen().clickAddTravelerButton();
			mDriver.travelerInformationScreen().clickEnterANewTraveler();
			mDriver.travelerInformationScreen().enterFirstName(mUser.getFirstName());
			mDriver.travelerInformationScreen().enterLastName(mUser.getLastName());
			mDriver.travelerInformationScreen().enterPhoneNumber(mUser.getPhoneNumber());
			mDriver.travelerInformationScreen().enterEmailAddress(mUser.getLoginEmail());
			mDriver.travelerInformationScreen().clickDoneButton();

			mDriver.hotelsCheckoutScreen().clickSelectPaymentButton();
			try {
				mDriver.commonPaymentMethodScreen().clickOnAddNewCardTextView();
			}
			catch (Error e) {
				mDriver.enterLog(TAG, "No Add New Card button. Proceeding anyway.");
			}
			mDriver.enterLog(TAG, "Entering credit card with number: " + mUser.getCreditCardNumber());
			mDriver.cardInfoScreen().typeTextCreditCardEditText(mUser.getCreditCardNumber());
			mDriver.enterLog(TAG, "Entering postal code: " + mUser.getAddressPostalCode());
			mDriver.billingAddressScreen().typeTextPostalCode(mUser.getAddressPostalCode());
			mDriver.enterLog(TAG, "Entering cardholder name: " + mUser.getFirstName() + " " + mUser.getLastName());
			mDriver.cardInfoScreen().typeTextNameOnCardEditText(mUser.getFirstName() + " " + mUser.getLastName());
			mDriver.cardInfoScreen().clickOnExpirationDateButton();
			mDriver.delay(1);

			mDriver.enterLog(TAG, "Incrementing credit card exp. month and year by 1");
			mDriver.cardInfoScreen().clickMonthUpButton();
			mDriver.cardInfoScreen().clickYearUpButton();
			mDriver.cardInfoScreen().clickSetButton();
			mDriver.cardInfoScreen().clickOnDoneButton();
			mDriver.delay();
			mHotelName = mDriver.hotelsCheckoutScreen().hotelNameView().getText().toString();

			mDriver.enterLog(TAG, "Sliding to checkout");
			mDriver.hotelsCheckoutScreen().slideToCheckout();
			mDriver.delay();

			mDriver.enterLog(TAG, "Entering CCV: " + mUser.getCCV());
			mDriver.cvvEntryScreen().parseAndEnterCVV(mUser.getCCV());
			mDriver.cvvEntryScreen().clickBookButton();
			mDriver.delay(1);
			mDriver.waitForStringToBeGone(mDriver.cvvEntryScreen().booking());

			verifyConfirmationTexts();

			mDriver.hotelsConfirmationScreen().clickDoneButton();
			mDriver.delay();
			mDriver.tripsScreen().swipeToLaunchScreen();
			mDriver.launchScreen().openMenuDropDown();
			mDriver.launchScreen().pressSettings();
			mDriver.settingsScreen().clickToClearPrivateData();
			if (mDriver.searchText(mDriver.settingsScreen().OKString())) {
				mDriver.settingsScreen().clickOKString();
			}
			else if (mDriver.searchText(mDriver.settingsScreen().AcceptString())) {
				mDriver.settingsScreen().clickAcceptString();
			}
			else {
				mDriver.clickOnText("OK");
			}
			mDriver.delay();
			if (mDriver.searchText(mDriver.settingsScreen().OKString())) {
				mDriver.settingsScreen().clickOKString();
			}
			else if (mDriver.searchText(mDriver.settingsScreen().AcceptString())) {
				mDriver.settingsScreen().clickAcceptString();
			}
			else {
				mDriver.clickOnText("OK");
			}
			mDriver.goBack();
		}
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
			mDriver.enterLog(TAG, "Added pair: " + newPair.first + ", " + newPair.second);
		}
		return returnableList;
	}

	private void verifyConfirmationTexts() {
		HotelSearchParams params = Db.getHotelSearch().getSearchParams();
		int cachedNumberOfGuests = params.getNumAdults() + params.getNumChildren();
		assertEquals(mNumberOfGuests, cachedNumberOfGuests);
		String guestString = mRes.getQuantityString(R.plurals.number_of_guests, mNumberOfGuests, mNumberOfGuests);
		mDateRangeString = CalendarUtils.formatDateRange2(getActivity(), params, DateUtils.FORMAT_SHOW_DATE
				| DateUtils.FORMAT_ABBREV_MONTH);
		String expectedSummaryString = getString(R.string.stay_summary_TEMPLATE, guestString, mDateRangeString);
		String displayedDetailsString = mDriver.hotelsConfirmationScreen().summaryTextView().getText().toString();
		assertEquals(expectedSummaryString, displayedDetailsString);

		String displayedHotelName = mDriver.hotelsConfirmationScreen().hotelNameTextView().getText().toString();
		assertEquals(mHotelName, displayedHotelName);

		String itineraryConfirmationText = mDriver.hotelsConfirmationScreen().itineraryTextView().getText().toString();
        String expectedItineraryNumber = Db.getBookingResponse().getItineraryId();
		String expectedItineraryConfirmationText = getString(R.string.itinerary_confirmation_TEMPLATE, expectedItineraryNumber);
		assertEquals(expectedItineraryConfirmationText, itineraryConfirmationText);

		String displayedEmailAddress = mDriver.hotelsConfirmationScreen().emailTextView().getText().toString();
		String expectedEmailAddString = mUser.getLoginEmail();
		assertEquals(expectedEmailAddString, displayedEmailAddress);
	}

	@Override
	protected void tearDown() throws Exception {
		mDriver.enterLog(TAG, "tearing down...");
		mDriver.finishOpenedActivities();
	}

}
