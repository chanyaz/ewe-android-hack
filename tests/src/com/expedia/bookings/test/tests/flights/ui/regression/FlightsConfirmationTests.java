package com.expedia.bookings.test.tests.flights.ui.regression;

import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.test.tests.pageModels.flights.FlightsSearchResultRow;
import com.expedia.bookings.test.utils.CustomActivityInstrumentationTestCase;
import com.expedia.bookings.test.utils.FlightsTestDriver;

public class FlightsConfirmationTests extends CustomActivityInstrumentationTestCase<SearchActivity> {

	private static final String TAG = FlightsConfirmationTests.class.getSimpleName();

	FlightsTestDriver mDriver;

	String mEnvironment;
	String mAirlineCarrier;
	String mTakeOffTime;
	String mArrivalTime;

	public FlightsConfirmationTests() {
		super(SearchActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		mDriver = new FlightsTestDriver(getInstrumentation(), getActivity(), mRes, mPreferences);
		mUser.setAirportsToRandomUSAirports();
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

	public void testGuestBookingConfirmation() throws Exception {
		mDriver.enterLog(TAG, "START TEST: Testing confirmation screen for guest flight booking");
		getToCheckout();
		mDriver.flightsCheckoutScreen().clickTravelerDetailsButton();
		mDriver.travelerInformationScreen().enterFirstName(mUser.getFirstName());
		mDriver.travelerInformationScreen().enterLastName(mUser.getLastName());
		mDriver.travelerInformationScreen().enterPhoneNumber(mUser.getPhoneNumber());
		mDriver.travelerInformationScreen().clickBirthDateButton();
		if (mDriver.searchText(mDriver.travelerInformationScreen().set())) {
			mDriver.travelerInformationScreen().clickSetButton();
		}
		else {
			mDriver.travelerInformationScreen().clickDoneString();
		}
		mDriver.travelerInformationScreen().clickNextButton();
		mDriver.travelerInformationScreen().clickDoneButton();

		enterPaymentInfo();

		mDriver.enterLog(TAG, "Sliding to checkout");
		mDriver.flightsCheckoutScreen().slideToCheckout();
		mDriver.delay();

		mDriver.enterLog(TAG, "Entering CCV: " + mUser.getCCV());
		mDriver.cvvEntryScreen().parseAndEnterCVV(mUser.getCCV());
		mDriver.cvvEntryScreen().clickBookButton();
		mDriver.delay(1);
		mDriver.waitForStringToBeGone(mDriver.cvvEntryScreen().booking());

		assertTrue(mDriver.searchText(mAirlineCarrier));
		assertTrue(mDriver.searchText(mArrivalTime));
		assertTrue(mDriver.searchText(mTakeOffTime));
		mDriver.flightsConfirmationScreen().clickDoneButton();
		mDriver.tripsScreen().swipeToLaunchScreen();
	}

	public void testLoggedInBookingConfirmation() throws Exception {
		mDriver.enterLog(TAG, "START TEST: Testing confirmation screen for logged in flight booking");
		getToCheckout();
		mDriver.flightsCheckoutScreen().clickLogInButton();
		mDriver.logInScreen().typeTextEmailEditText(mUser.getLoginEmail());
		mDriver.logInScreen().typeTextPasswordEditText(mUser.getLoginPassword());
		mDriver.logInScreen().clickOnLoginButton();
		mDriver.waitForStringToBeGone(mDriver.logInScreen().loggingInDialogString());

		enterPaymentInfo();

		if (mDriver.searchText(mDriver.flightsCheckoutScreen().addTravelerString(), 1, false, true)) {
			mDriver.flightsCheckoutScreen().clickAddTravelerString();
			mDriver.travelerInformationScreen().clickEnterANewTraveler();
			mDriver.travelerInformationScreen().enterLastName(mUser.getLastName());
			mDriver.enterLog(TAG, "Entering last name: " + mUser.getLastName());
			mDriver.travelerInformationScreen().enterFirstName(mUser.getFirstName());
			mDriver.enterLog(TAG, "Entering first name: " + mUser.getFirstName());
			mDriver.travelerInformationScreen().enterPhoneNumber(mUser.getPhoneNumber());
			mDriver.enterLog(TAG, "Entering phone number: " + mUser.getPhoneNumber());
			mDriver.travelerInformationScreen().clickBirthDateButton();
			mDriver.travelerInformationScreen().clickDoneString();
			mDriver.billingAddressScreen().clickNextButton();
			mDriver.travelerInformationScreen().clickDoneButton();
			mDriver.cardInfoScreen().clickNoThanksButton();
		}

		mDriver.enterLog(TAG, "Sliding to checkout");
		mDriver.flightsCheckoutScreen().slideToCheckout();
		mDriver.delay();

		mDriver.enterLog(TAG, "Entering CCV: " + mUser.getCCV());
		mDriver.cvvEntryScreen().parseAndEnterCVV(mUser.getCCV());
		mDriver.cvvEntryScreen().clickBookButton();
		mDriver.delay(1);
		mDriver.waitForStringToBeGone(mDriver.cvvEntryScreen().booking());

		assertTrue(mDriver.searchText(mAirlineCarrier));
		assertTrue(mDriver.searchText(mArrivalTime));
		assertTrue(mDriver.searchText(mTakeOffTime));
		mDriver.flightsConfirmationScreen().clickDoneButton();
		mDriver.tripsScreen().swipeToLaunchScreen();
	}

	// Helper methods

	private void enterPaymentInfo() {
		mDriver.flightsCheckoutScreen().clickSelectPaymentButton();

		try {
			mDriver.commonPaymentMethodScreen().clickOnAddNewCardTextView();
		}
		catch (Error e) {
			mDriver.enterLog(TAG, "No Add New Card button. Proceeding anyway.");
		}

		if (PointOfSale.getPointOfSale().requiresBillingAddressFlights()) {
			mDriver.screenshot("Address");

			mDriver.enterLog(TAG, "Entering address line 1: " + mUser.getAddressLine1());
			mDriver.billingAddressScreen().typeTextAddressLineOne(mUser.getAddressLine1());
			mDriver.enterLog(TAG, "Entering address city: " + mUser.getAddressCity());
			mDriver.billingAddressScreen().typeTextCity(mUser.getAddressCity());
			mDriver.enterLog(TAG, "Entering address state code: " + mUser.getAddressStateCode());
			mDriver.billingAddressScreen().typeTextState(mUser.getAddressStateCode());
			mDriver.enterLog(TAG, "Entering postal code: " + mUser.getAddressPostalCode());
			mDriver.billingAddressScreen().typeTextPostalCode(mUser.getAddressPostalCode());
			mDriver.billingAddressScreen().clickNextButton();
		}

		mDriver.enterLog(TAG, "Using new credit card");
		mDriver.screenshot("Card info");
		mDriver.enterLog(TAG, "Entering credit card with number: " + mUser.getCreditCardNumber());
		mDriver.cardInfoScreen().typeTextCreditCardEditText(mUser.getCreditCardNumber());
		mDriver.cardInfoScreen().clickOnExpirationDateButton();
		mDriver.enterLog(TAG, "Incrementing credit card exp. month and year by 1");
		mDriver.cardInfoScreen().clickMonthUpButton();
		mDriver.cardInfoScreen().clickYearUpButton();
		mDriver.cardInfoScreen().clickSetButton();

		if (mDriver.cardInfoScreen().emailEditText().isShown()) {
			mDriver.cardInfoScreen().typeTextEmailEditText(mUser.getLoginEmail());
		}
		if (mDriver.cardInfoScreen().nameOnCardEditText().isShown()) {
			mDriver.cardInfoScreen().typeTextNameOnCardEditText(mUser.getFirstName() + " " + mUser.getLastName());
		}

		mDriver.cardInfoScreen().clickOnDoneButton();
		if (mDriver.searchText(mDriver.cardInfoScreen().noThanksButtonString())) {
			mDriver.cardInfoScreen().clickNoThanksButton();
		}
		mDriver.delay(1);
	}

	private void getToCheckout() throws Exception {
		mDriver.launchScreen().openMenuDropDown();
		mDriver.launchScreen().pressSettings();
		mDriver.settingsScreen().setSpoofBookings();
		mDriver.goBack();

		mDriver.launchScreen().launchFlights();
		mDriver.flightsSearchScreen().enterDepartureAirport(mUser.getDepartureAirport());
		mDriver.flightsSearchScreen().enterArrivalAirport(mUser.getArrivalAirport());
		mDriver.flightsSearchScreen().clickSelectDepartureButton();
		int dateOffset = 1;
		mDriver.enterLog(TAG, "Selecting date with offset from current date: " + dateOffset);
		mDriver.flightsSearchScreen().clickDate(dateOffset);
		mDriver.enterLog(TAG, "Click search button");
		mDriver.flightsSearchScreen().clickSearchButton();
		mDriver.waitForStringToBeGone(mDriver.flightsSearchLoading().getLoadingFlightsString());

		int childIndex = 1;
		FlightsSearchResultRow row = mDriver.flightsSearchResultsScreen().getSearchResultModelFromView(
				mDriver.flightsSearchResultsScreen().searchResultListView().getChildAt(childIndex + 1));
		mAirlineCarrier = row.getAirlineTextView().getText().toString();
		mTakeOffTime = row.getDepartureTimeTextView().getText().toString();
		mArrivalTime = row.getArrivalTimeTextView().getText().toString();

		mDriver.flightsSearchResultsScreen().selectFlightFromList(childIndex);
		mDriver.delay(1);
		mDriver.flightLegScreen().clickSelectFlightButton();
		mDriver.delay(1);
		mDriver.waitForStringToBeGone(mDriver.flightLegScreen().checkingForPriceChangesString());
		mDriver.flightsCheckoutScreen().clickCheckoutButton();
	}

	@Override
	protected void tearDown() throws Exception {
		mDriver.enterLog(TAG, "tearing down...");
		mDriver.finishOpenedActivities();
	}
}
