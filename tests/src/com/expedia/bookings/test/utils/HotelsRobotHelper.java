package com.expedia.bookings.test.utils;

import java.util.ArrayList;
import java.util.Locale;

import junit.framework.AssertionFailedError;
import ErrorsAndExceptions.IntegrationFailureError;
import android.content.res.Resources;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.jayway.android.robotium.solo.Solo;
import com.mobiata.testutils.CalendarTouchUtils;

public class HotelsRobotHelper {
	////////////////////////////////////////////////////////////////
	// Static Locale Data
	//TODO make these a different container so cool methods can be used

	private static final String TAG = "com.expedia.bookings.test";
	private boolean mAllowScreenshots;
	private boolean mAllowOrientationChange;
	private boolean mWriteEventsToFile;
	private int mScreenShotCount;
	private Solo mSolo;
	private Resources mRes;
	private HotelsUserData mUser; //user info container
	private ScreenshotUtils mScreen;
	private int mScreenWidth;
	private int mScreenHeight;
	public UserLocaleUtils mLocaleUtils;

	private static final String mScreenshotDirectory = "Robotium-Screenshots";

	//Defaults are set, including the default user booking info
	//which is set to the qa-ehcc@mobiata.com account's info
	public HotelsRobotHelper(Solo solo, Resources res) {
		this(solo, res, new HotelsUserData());
	}

	//Constructor for user created book user container
	public HotelsRobotHelper(Solo solo, Resources res, HotelsUserData customUser) {
		mAllowScreenshots = false;
		mAllowOrientationChange = false;
		mWriteEventsToFile = false;
		mScreenShotCount = 1;

		mSolo = solo;
		mRes = res;
		mUser = customUser;
		mLocaleUtils = new UserLocaleUtils(res);

		mScreen = new ScreenshotUtils(mScreenshotDirectory, mSolo);
		mScreenWidth = mRes.getDisplayMetrics().widthPixels;
		mScreenWidth = mRes.getDisplayMetrics().heightPixels;
	}

	////////////////////////////////////////////////////////////////
	// Helpful Methods
	public void launchHotels() {
		mSolo.clickOnView(mSolo.getView(R.id.hotels_button));
	}

	public void launchFlights() {
		mSolo.clickOnView(mSolo.getView(R.id.flights_button));
	}

	public void enterLog(String TAG, String logText) {
		Log.v(TAG, "Robotium: " + logText);
	}

	public void delay(int time) { //Enter time in seconds
		time = time * 1000;
		mSolo.sleep(time);
	}

	public void delay() { //Defaults to 3 seconds
		mSolo.sleep(3000);
	}

	public void setAllowScreenshots(Boolean allowed) {
		mAllowScreenshots = allowed;
	}

	public void setAllowOrientationChange(Boolean allowed) {
		mAllowOrientationChange = allowed;
	}

	public void setWriteEventsToFile(Boolean allowed) {
		mWriteEventsToFile = allowed;
	}

	public void setScreenshotCount(int count) {
		mScreenShotCount = count;
	}

	public void screenshot(String fileName) { //screenshot is saved to device SD card.
		if (mAllowScreenshots) {
			String currentLocale = mRes.getConfiguration().locale.toString();
			enterLog(TAG, "Taking screenshot: " + fileName);
			mScreen.screenshot(currentLocale + " " + String.format("%02d", mScreenShotCount) + " " + fileName);
			mScreenShotCount++;
		}
	}

	// Log failure upon catching Throwable, and create and store screenshot
	// Maintain mAllowScreenshots state from before screenshot is taken
	public void takeScreenshotUponFailure(Throwable e, String testName) {
		Log.e(TAG, "Taking screenshot due to " + e.getClass().getName(), e);
		final boolean currentSSPermission = mAllowScreenshots;
		if (!currentSSPermission) {
			mAllowScreenshots = true;
		}
		screenshot(testName + "-FAILURE");
		if (!currentSSPermission) {
			mAllowScreenshots = false;
		}
	}

	public void landscape() {
		if (mAllowOrientationChange) {
			delay(2);
			mSolo.setActivityOrientation(Solo.LANDSCAPE);
			delay(2);
		}
	}

	public void portrait() {
		if (mAllowOrientationChange) {
			delay(2);
			mSolo.setActivityOrientation(Solo.PORTRAIT);
			delay(2);
		}
	}

	////////////////////////////////////////////////////////////////
	// Changing Settings

	public void changePOS(Locale locale) throws Exception {
		enterLog(TAG, "Changing POS");
		String settingsString = mRes.getString(R.string.Settings);
		//Sometimes the text displayed hasn't matched the string
		//for the update locale
		//This try catch block is a messy fix to be able to do more than one booking
		delay();
		try {
			delay();
			mSolo.pressMenuItem(0);
		}
		catch (Error E) {
			enterLog(TAG, "Menu not there. Going back to try to refresh it.");
			mSolo.goBack();
			delay();
			mSolo.pressMenuItem(1);
			delay();
			mSolo.goBack();
			mSolo.pressMenuItem(0);
		}

		String countryHeader = mRes.getString(R.string.preference_point_of_sale_title);
		mSolo.clickOnText(countryHeader);

		String countrySelection = mRes.getString(mLocaleUtils.LOCALE_TO_COUNTRY.get(locale));
		Log.d(TAG, "Our countrySelection is: " + countrySelection);
		delay(1);
		mSolo.clickOnText(countrySelection);
		delay(1);
		mSolo.clickOnButton(1);
		delay(1);
		mSolo.clickOnButton(0);
		mSolo.goBack();
		delay();
	}

	public void clearPrivateData() {
		mSolo.pressMenuItem(0);
		delay();
		if (!mSolo.searchText("Select API")) {
			mSolo.pressMenuItem(0);
		}
		landscape();
		portrait();
		String clearPrivateData = mRes.getString(R.string.clear_private_data);
		mSolo.clickOnText(clearPrivateData);
		delay(2);
		mSolo.clickOnButton(1);
		delay(1);
		mSolo.clickOnButton(0);
		mSolo.goBack();
	}

	public void changeAPI(String API) throws Exception {
		enterLog(TAG, "Changing API Server");
		mSolo.pressMenuItem(0);
		mSolo.clickOnText("Select ");
		mSolo.scrollUpList(0);
		mSolo.clickOnText(API);
		mSolo.goBack();
	}

	public void setSpoofBookings() {
		boolean spoofBookingsDone = false;
		boolean suppressFlightsDone = false;
		mSolo.pressMenuItem(0);
		delay(5);
		mSolo.scrollDown();
		ArrayList<View> a = mSolo.getCurrentViews();
		for (int i = 0; i < a.size(); i++) {
			Log.v("!!!", "!!! " + a.toString());
			if (spoofBookingsDone && suppressFlightsDone) {
				break;
			}
			View currentView = a.get(i);
			if (currentView instanceof CheckBox) {
				CheckBox currentCheckBox = (CheckBox) currentView;
				if (currentCheckBox.getId() == R.id.preference_spoof_booking_checkbox) {
					if (!currentCheckBox.isChecked()) {
						mSolo.clickOnText("Spoof hotel bookings");
					}
					spoofBookingsDone = true;
				}
				else if (currentCheckBox.getId() == R.id.preference_suppress_flight_booking_checkbox) {
					if (!currentCheckBox.isChecked()) {
						mSolo.clickOnText("Suppress Flight Bookings");
					}
					suppressFlightsDone = true;
				}
			}
		}
		mSolo.goBack();
	}

	////////////////////////////////////////////////////////////////
	// Search Screen Methods

	public void hotelListScreenshots() {
		if (mAllowScreenshots) {
			screenshot("Search Results");
			mSolo.scrollDown();
		}
	}

	public void selectLocation(String location) throws Exception {
		enterLog(TAG, "Searching for destination " + location);
		delay(5);
		mSolo.clickOnEditText(0);
		enterLog(TAG, "Click selection location EditText");
		delay(1);
		mSolo.clearEditText(0);
		enterLog(TAG, "Post-clearing of EditText");
		delay(1);
		enterLog(TAG, "Before typing location into location EditText");
		mSolo.typeText(0, location);
		enterLog(TAG, "After typing location into location EditText");
		delay(3);
		enterLog(TAG, "Before initiating location ");

		//If keeping track of events, write current locale/POS to file
		String currentPOS = mRes.getConfiguration().locale.toString();
		Log.d(TAG, "Current POS/Locale: " + currentPOS);

		landscape();
		portrait();
		delay();
		if (mAllowOrientationChange) {
			delay();
			mSolo.clickOnEditText(0);
			mSolo.clickInList(2);
		}
		else {
			mSolo.clickInList(1); //Selecting search suggestion results
		} //some countries' list don't populate ever
			//might break stuff
		enterLog(TAG, "After clicking search button");

		String activityString = "ExpediaBookingApp";
		int count = 0;
		while (count < 5 && !mSolo.waitForActivity(activityString)) {
			delay(5);
			count++;
		}
		if (mSolo.waitForActivity(activityString)) {
			enterLog(TAG, "Location searched for and results loaded!");
		}
		else {
			Log.e(TAG, "Robotium: Never got hotel search results.");
		}

		delay();
		hotelListScreenshots();
	}

	public void filterFor(String filterText) { //filter currently does not work.
		//solo.clickOnText(getStringFromR(R.string.filter_hotels));
		View filterButton = mSolo.getView(R.id.menu_select_filter);

		//Filtering Asian languages with English characters often
		// leads to no hotels being found, so we don't filter on those locales.

		Locale currentLocale = mRes.getConfiguration().locale;
		Log.d(TAG, "!!!!! " + currentLocale.toString());
		if (!currentLocale.equals(mLocaleUtils.APAC_LOCALES[4]) //Japan
				&& !currentLocale.equals(mLocaleUtils.APAC_LOCALES[5]) //Korea
				&& !currentLocale.equals(mLocaleUtils.APAC_LOCALES[1]) //Chinese-Hong Kong
				&& !currentLocale.equals(mLocaleUtils.APAC_LOCALES[12]) //Chinese-Taiwan
				&& !currentLocale.equals(mLocaleUtils.APAC_LOCALES[15]) //China 
		) {
			enterLog(TAG, "Clicking on Filter label");

			try {
				mSolo.clickOnView(filterButton);
			}
			catch (Error e) {
				mSolo.clickOnText(mRes.getString(R.string.filter));
			}

			landscape();
			portrait();
			delay();

			delay(5);
			mSolo.enterText(0, filterText);

			delay(1);
			screenshot("Filtering hotels for " + filterText);
			delay(1);
			mSolo.goBack();
		}
	}

	public void pressSort() {
		delay(1);
		View sortButton = mSolo.getView(R.id.menu_select_sort);
		enterLog(TAG, "Clicking on sort label: ");
		delay();
		try {
			mSolo.clickOnView(sortButton);
		}
		catch (Error e) {
			mSolo.clickOnText(mRes.getString(R.string.sort));
		}
		landscape();
		portrait();
		if (!mAllowOrientationChange) {
			mSolo.goBack();
		}
	}

	public void pressCalendar() {
		mSolo.clickOnImageButton(1);
		delay(1);
		landscape();
		portrait();
		delay();
		screenshot("Calendar");
	}

	public void pressGuestPicker() {
		mSolo.clickOnImageButton(0);
		delay(2);
		landscape();
		portrait();
		delay(1);
		if (mAllowOrientationChange) {
			mSolo.clickOnImageButton(0);
		}
		screenshot("Guest Picker");

	}

	public void selectHotel(int hotelIndex) throws Exception {
		enterLog(TAG, "Picking hotel at index " + hotelIndex);

		landscape();
		portrait();

		mSolo.clickInList(hotelIndex);
		mSolo.waitForActivity("HotelDetailsFragmentActivity");
		delay();
		delay();

		enterLog(TAG, "Hotel Details have loaded!");
		screenshot("Hotel Details Screen");

		mSolo.scrollDown();
		screenshot("Hotel Details 2");

		mSolo.scrollDown();
		screenshot("Hotel Details 3");

		mSolo.scrollToBottom();
		screenshot("Bottom of Hotel Details");

	}

	////////////////////////////////////////////////////////////////
	// Hotel Info Screen Methods
	public void checkReviews() {
		enterLog(TAG, "About to go to Reviews view.");
		delay();
		mSolo.scrollToTop();
		View user_rating_text_view =
				mSolo.getCurrentActivity().findViewById(R.id.user_rating_text_view);

		try {
			mSolo.clickOnView(user_rating_text_view);
			mSolo.waitForDialogToClose(10000);

			screenshot("All reviews");

			delay(1);
			landscape();
			portrait();
			delay();
			mSolo.goBack();
		}
		catch (Error e) {
			enterLog(TAG, "No reviews for hotel selected");
		}
	}

	public void pressBookRoom() {

		mSolo.clickOnButton(0);
		//Wait for rooms and rates to appear!
		Boolean didItLoad = mSolo.waitForActivity("RoomsAndRatesListActivity", 20000);
		if (didItLoad) {
			enterLog(TAG, "On Rooms and Rates Screen");
			delay();
		}
		else {
			enterLog(TAG, "Didn't load after 20 seconds.");
			delay(5);
		}
	}

	////////////////////////////////////////////////////////////////
	// Rooms & Rates Screens methods

	public void selectRoom(int roomIndex) throws Exception {
		enterLog(TAG, "About to select room at index " + roomIndex);
		delay();
		landscape();
		portrait();
		mSolo.waitForDialogToClose(10000);
		screenshot("Rooms and Rates Screen");
		try {
			mSolo.clickInList(roomIndex);
		}
		//select new hotel if current hotel as no rooms
		catch (Error noRoomsListed) {
			enterLog(TAG, "No rooms at this hotel. Going to a new one.");
			mSolo.goBack();
			delay();
			mSolo.goBack();
			delay();
			mSolo.scrollDown();
			selectHotel(2);
			pressBookRoom();
			selectRoom(0);
		}
		delay();

		mSolo.waitForActivity("ExpediaBookingActivity");
		enterLog(TAG, "On Booking Screen.");
	}

	////////////////////////////////////////////////////////////////
	// Booking Screen methods

	public void bookingScreenShots() {

		screenshot("Booking Screen 1");
		mSolo.scrollDown();
		delay(1);

		screenshot("Bottom of Booking Screen");
		mSolo.scrollToTop();
	}

	public void logIn() {

		enterLog(TAG, "Beginning log-in sequence.");

		mSolo.scrollToTop();

		String log_in_for_faster_booking = mRes.getString(R.string.log_in_for_faster_booking);
		String log_in_with_expedia = mRes.getString(R.string.Log_in_with_Expedia);
		// Try clicking login button
		try {
			if (mSolo.searchText(log_in_for_faster_booking)) {
				enterLog(TAG, "Log in: Clicking " + log_in_for_faster_booking);
				mSolo.clickOnText(log_in_for_faster_booking);
			}
			else {
				enterLog(TAG, "Log in: Clicking " + log_in_with_expedia);
				mSolo.clickOnText(log_in_with_expedia);
			}
		}
		// A failure probably means that the room isn't available
		// so go back and grab a new hotel
		catch (AssertionFailedError e) {
			enterLog(TAG, "Failure clicking log in button");
			delay(5);

			if (mSolo.searchText(mSolo.getCurrentActivity().getString(
					R.string.e3_error_checkout_hotel_room_unavailable))) {
				enterLog(TAG, "Log in: E3 Error - room no longer available. Backing out and trying again.");
				mSolo.clickOnButton(0);
				delay();
				mSolo.goBack();
				mSolo.goBack();
				mSolo.scrollDown();
				mSolo.clickInList(1);
				delay();
				pressBookRoom();
				mSolo.clickInList(0);
				delay();
				mSolo.clickOnText(mRes.getString(R.string.checkout_btn));
				if (mSolo.searchText(log_in_for_faster_booking)) {
					mSolo.clickOnText(log_in_for_faster_booking);
				}
				else {
					mSolo.clickOnText(log_in_with_expedia);
				}
			}
			else {
				delay();
				enterLog(TAG, "Log in: If all else fails, try the button again");
				if (mSolo.searchText(log_in_for_faster_booking)) {
					mSolo.clickOnText(log_in_for_faster_booking);
				}
				else {
					mSolo.clickOnText(log_in_with_expedia);
				}
			}
		}

		delay();

		screenshot("Login Screen Pre Text Entry");
		mSolo.typeText(0, mUser.mLoginEmail);

		delay();

		mSolo.typeText((EditText) mSolo.getView(R.id.password_edit_text), mUser.mLoginPassword);

		landscape();
		delay();
		portrait();
		delay(5);

		//Ensure that the keyboard isn't covering the log in button
		String expediaAccount = mRes.getString(R.string.expedia_account);
		if (mSolo.searchText(expediaAccount) && !mSolo.searchText(log_in_with_expedia, true)) {
			mSolo.goBack();
		}
		mSolo.clickOnText(log_in_with_expedia);

		delay(5);
		mSolo.scrollToTop();
		delay();
		screenshot("Booking Info Post-Login");

	}

	public void enterCCV() throws Exception {
		enterLog(TAG, "Booking: Entering CVV");
		//mSolo.scrollUp();
		delay(3);
		screenshot("CVV Screen");
		landscape();
		portrait();
		delay(5);

		View one = mSolo.getView(R.id.one_button);
		mSolo.clickOnView(one);
		delay(1);
		mSolo.clickOnView(one);
		delay(1);
		mSolo.clickOnView(one);
		delay(1);
		mSolo.clickOnView(one);
		delay(1);
		mSolo.clickOnView(mSolo.getView(R.id.book_button));
		enterLog(TAG, "Press Book");
		enterLog(TAG, "Entered CCV");
	}

	//Frequently, different POS have different requirements as to what info has to be entered
	//after log in. The try-catch blocks eliminate the need to hardcode what information is needed
	//where, based upon the POS/locale that you are in.

	public void enterNewTraveler() {
		enterLog(TAG, "Booking: Entering a new traveler.");
		delay();
		screenshot("Picking traveler");
		landscape();
		delay();
		portrait();
		delay(5);
		screenshot("Adding new traveler");

		mSolo.clickOnView(mSolo.getView(R.id.enter_info_manually_button));
		delay();
		mSolo.enterText((EditText)
				mSolo.getCurrentActivity().findViewById(R.id.edit_first_name),
				mUser.mFirstName);
		delay(1);
		mSolo.enterText((EditText)
				mSolo.getCurrentActivity().findViewById(R.id.edit_last_name),
				mUser.mLastName);
		delay();
		mSolo.enterText((EditText)
				mSolo.getCurrentActivity().findViewById(R.id.edit_phone_number),
				mUser.mPhoneNumber);

		boolean goThroughFlightsFlow = false;
		View birthDateTextButton = null;
		try {
			birthDateTextButton = mSolo.getView(R.id.edit_birth_date_text_btn);
			if (birthDateTextButton.isShown()) {
				goThroughFlightsFlow = true;
			}
		}
		catch (Error e) {
			Log.e(TAG, "Birthdate text button not here.", e);
		}

		if (goThroughFlightsFlow) {

			mSolo.clickOnView(birthDateTextButton);
			String done = mRes.getString(R.string.done);
			if (mSolo.searchText(done)) {
				mSolo.clickOnText(done);
			}
			else {
				String setButton = mRes.getString(R.string.btn_set);
				mSolo.clickOnText(setButton);
			}

			landscape();
			portrait();

			mSolo.clickOnText(mRes.getString(R.string.next));

		}

		mSolo.clickOnText(mRes.getString(R.string.button_done));

		if (mSolo.searchText(mRes.getString(R.string.save_traveler))) {
			mSolo.clickOnText(mRes.getString(R.string.no_thanks));
		}

	}

	public void enterMissingInfo(boolean addNewCC) {
		enterLog(TAG, "Booking: entering traveler info.");

		String addTraveler = mSolo.getString(R.string.add_traveler);

		if (mSolo.searchText(addTraveler, true)) {
			try {
				mSolo.clickOnView(mSolo.getView(R.id.traveler_info_btn));
			}
			catch (Error e) {
				Log.e(TAG, "Failed to click view. Falling back to string: " + addTraveler);
				mSolo.clickOnText(addTraveler);
			}
			enterNewTraveler();
		}

		if (addNewCC) {

			mSolo.scrollToBottom();

			mSolo.clickOnView(mSolo.getView(R.id.payment_info_btn));
			delay();
			screenshot("Select payment");

			delay();

			mSolo.clickOnText(mSolo.getString(R.string.add_new_card));
			delay(1);
			screenshot("Add new card");
			delay(1);

			delay(5);
			screenshot("Credit card info.");

			if (mSolo.searchText(mRes.getString(R.string.billing_address))) {
				inputBillingAddress();
			}
			inputCCBillingInfo();
		}

	}

	public void inputBillingAddress() {
		enterLog(TAG, "Booking: entering billing address.");

		//Enter billing street address
		mSolo.enterText((EditText) mSolo.getView(R.id.edit_address_line_one),
				mUser.mAddressLine1);

		//Enter billing address city
		mSolo.enterText((EditText) mSolo.getView(R.id.edit_address_city),
				mUser.mCityName);

		//Enter billing address state
		mSolo.enterText((EditText) mSolo.getView(R.id.edit_address_state),
				mUser.mStateCode);

		//Enter billing address postal code
		mSolo.enterText((EditText) mSolo.getView(R.id.edit_address_postal_code),
				mUser.mZIPCode);

		//Press "Next" to continue
		mSolo.clickOnText(mRes.getString(R.string.next));
		landscape();
		portrait();

	}

	public void inputCCBillingInfo() {
		enterLog(TAG, "Booking: entering billing credit card information.");

		landscape();
		delay(1);
		portrait();

		// Enter Credit Card Number
		mSolo.enterText((EditText) mSolo.getView(R.id.edit_creditcard_number),
				mUser.mCreditCardNumber);

		// Enter Cardholder's name
		mSolo.typeText((EditText) mSolo.getView(R.id.edit_name_on_card),
				mUser.mFirstName + " " + mUser.mLastName);

		// Try to enter postal code
		try {
			mSolo.typeText((EditText) mSolo.getView(R.id.edit_address_postal_code),
					mUser.mZIPCode);
		}
		catch (Error e) {
			enterLog(TAG, "No postal code edit text found.");
		}

		// Pick generic date
		mSolo.clickOnText(mRes.getString(R.string.expiration_date));
		mSolo.clickOnButton(1);

		// Press done to enter this data
		mSolo.clickOnText(mRes.getString(R.string.button_done));

		// Do not save this card info
		mSolo.clickOnText(mRes.getString(R.string.no_thanks));
	}

	public void confirmAndBook(boolean assertPostCCVPopUp) throws Exception {
		enterLog(TAG, "Booking: About to slide to accept.");

		try {
			mSolo.clickOnText(mRes.getString(R.string.checkout_btn));
		}
		catch (Error e) {
			enterLog(TAG, "Checkout button not there. Try to move on without it.");
		}

		try {
			mSolo.clickOnText(mRes.getString(R.string.I_Accept));
		}
		catch (Error e) {
			enterLog(TAG, "There is no 'I accept' button on this POS");
		}

		delay();
		mSolo.scrollToBottom();

		// Slide to checkout automation
		// gets location of start point and end point, slides 
		View sliderStart = mSolo.getView(R.id.slider_image);
		int[] startLocation = new int[2];
		sliderStart.getLocationOnScreen(startLocation);

		View sliderEnd = mSolo.getView(R.id.destination_image);
		int[] endLocation = new int[2];
		sliderEnd.getLocationOnScreen(endLocation);

		delay();
		screenshot("Slide to checkout");
		delay();

		enterLog(TAG, "Booking: Slide X from: " + startLocation[0] + " to " + endLocation[0] + ".");
		enterLog(TAG, "Booking: Slide Y from: " + startLocation[1] + " to " + endLocation[1] + ".");
		delay();

		mSolo.drag(startLocation[0], mScreenWidth - 5, startLocation[1] + 50, endLocation[1] + 50, 10);
		delay(1);

		String legal_information = mRes.getString(R.string.legal_information);
		if (mSolo.searchText(legal_information, true)) {
			enterLog(TAG, "Got to legal info screen by mistake. Going back and sliding to checkout.");
			mSolo.goBack();
			delay(1);
			mSolo.drag(startLocation[0], mScreenWidth - 5, startLocation[1] + 50, endLocation[1] + 50, 10);
		}

		delay(5);

		//ENTER CCV HERE!
		enterCCV();

		postBookingHandling(assertPostCCVPopUp);

	}

	public void postBookingHandling(boolean assertPostCCVPopUp) throws Exception {

		// If booking error appears, either throw exception
		// or try to enter CVV again.
		if (mSolo.searchText("Sorry, we don't seem to be able", true)) {

			//If asserting post-cvv entry popup, assert that
			// leaving pop up takes you back to CC entry view
			if (assertPostCCVPopUp) {
				mSolo.clickOnButton(0);
				delay();
				if (!mSolo.searchText(mSolo.getCurrentActivity().getString(R.string.card_info))) {
					Exception exception = new Exception();
					throw exception;
				}
			}
			//If can't complete the suppressed booking, go back to the launcher.
			mSolo.clickOnButton(0);
			mSolo.goBack();
			mSolo.goBack();
			mSolo.goBack();
			mSolo.goBack();
			mSolo.goBack();
		}
		else {
			//Take pictures of confirmation screen and 
			//go back to launcher
			Boolean screenLoaded = mSolo.waitForActivity("HotelConfirmationActivity");

			if (screenLoaded) {
				enterLog(TAG, "Booking: Should be on confirmation screen now.");
				delay();

				// Get and log itinerary number
				TextView itineraryTextView = (TextView) mSolo.getView(R.id.itinerary_text_view);
				String itineraryNumber = (String) itineraryTextView.getText();
				Log.d(TAG, "Robotium: Itinerary number is " + itineraryNumber);

				screenshot("Confirmation Screen 1");
				landscape();
				delay(1);
				portrait();
				mSolo.scrollToBottom();
				delay(1);
				screenshot("Confirmation Screen 2");
				mSolo.scrollToTop();
				try {
					mSolo.clickOnText(mRes.getString(R.string.button_done));
				}
				catch (Error e) {
					Log.e(TAG, "Can't press 'done' on confirmation screen.", e);
				}
				delay();
				try {
					String shop = mRes.getString(R.string.shop);
					String uppercaseShop = mRes.getString(R.string.shop).toUpperCase();
					if (mSolo.searchText(shop, true)) {
						mSolo.clickOnText(shop);
					}
					else if (mSolo.searchText(uppercaseShop, true)) {
						mSolo.clickOnText(uppercaseShop);
					}
					else if (mSolo.searchText("SHOP", true)) {
						mSolo.clickOnText("SHOP");
						Log.d(TAG, "Something wrong with loc. Clicking 'SHOP'");
					}
					else {
						mSolo.clickOnText("Shop");
						Log.d(TAG, "Something wrong with loc. Clicking 'Shop'");
					}
				}
				catch (Error e) {
					Log.e(TAG, "No 'shop' or 'SHOP' able to be clicked. Likely on tablet", e);
				}
			}
			else {
				enterLog(TAG, "Booking: Never got to confirmation screen.");
			}
		}
	}

	public void logInAndBook(boolean addNewCC, boolean completeBooking) throws Exception {
		bookingScreenShots();

		logIn();
		delay(1);
		enterMissingInfo(addNewCC);
		if (completeBooking) {
			confirmAndBook(false);
		}

	}

	//browseRooms goes into approximately numberOfHotels hotels, looks at hotels details
	//if completeABooking is true, it will complete a booking on one of the rooms.
	public void browseRooms(int numberOfHotels, String location, boolean completeABooking) throws Exception {
		enterLog(TAG, "Pre select location");
		selectLocation(location);

		for (int i = 0; i < numberOfHotels / 4; i++) {
			for (int j = 2; j < 6; j++) {
				selectHotel(j);
				checkReviews();
				delay(3);
				mSolo.goBack();
				delay(3);
			}
			mSolo.scrollDown();
		}
		selectHotel(2);
		pressBookRoom();
		selectRoom(0);
		logInAndBook(true, completeABooking);

	}

	public void checkItin(boolean useMockProxy) {

		clearPrivateData();
		if (useMockProxy) {
			mSolo.clickOnMenuItem(mRes.getString(R.string.Settings));
			mSolo.clickOnText("Select API");
			mSolo.clickOnText("Proxy");
			mSolo.clickOnText("Server/Proxy Address");
			mSolo.clearEditText(0);
			mSolo.enterText(0, "192.168.1.60:3000");
			mSolo.clickOnText("OK");
			mSolo.clickOnText("Stub Configuration Page");
			mSolo.waitForDialogToClose(15000);
			mSolo.clickInList(2);
			mSolo.waitForDialogToClose(15000);
			mSolo.goBack();
		}

		mSolo.clickOnText(mRes.getString(R.string.itinerary));
		delay(5);
		if (mSolo.searchText(mRes.getString(R.string.refresh), true)) {
			screenshot("Itinerary screen post-login");
			mSolo.clickOnMenuItem(mRes.getString(R.string.log_out));
			mSolo.clickOnText(mRes.getString(R.string.log_out));
			delay(5);
			screenshot("Itinerary screen pre-login");
		}
		else {
			screenshot("Itinerary screen pre-login");
			mSolo.clickOnButton(0);
			mSolo.typeText(0, mUser.mLoginEmail);

			delay();

			mSolo.typeText((EditText) mSolo.getView(R.id.password_edit_text), mUser.mLoginPassword);

			landscape();
			delay();
			portrait();
			delay(5);

			mSolo.clickOnButton(0);
			mSolo.waitForDialogToClose(30000);
			delay();
			screenshot("Itinerary screen with cards.");
			delay();
			mSolo.clickInList(1);
			delay();
			screenshot("Hotel card 1");
			mSolo.scrollToBottom();
			delay();
			screenshot("Hotel card 2");
			mSolo.goBack();

			delay();
			mSolo.clickInList(2);
			delay();
			screenshot("Flight card 1");
			delay();
			mSolo.scrollToBottom();
			screenshot("Flight card 2");
			delay();

			mSolo.goBack();
			mSolo.goBack();
		}

	}

	////////////////////////////////////////////////////////////////
	// Info Screen Methods

	public void captureInfoScreen() {
		delay();
		try {
			mSolo.clickOnMenuItem(mRes.getString(R.string.About));
		}
		catch (Error e) {
			enterLog(TAG, "Not at the launch screen. Trying to go back.");
			mSolo.goBack();
			delay();
			mSolo.clickOnMenuItem(mRes.getString(R.string.About));
		}
		landscape();
		portrait();

		delay(1);
		screenshot("Info Screen 1");

		mSolo.scrollToBottom();

		delay(1);
		screenshot("Info Screen 2");
		mSolo.goBack();
	}

	public void checkFlightsScreen() {
		launchFlights();
		delay(5);
		screenshot("Flights screen");
		mSolo.goBack();
	}

	////////////////////////////////////////////////////////////////
	// Flights

	public void enterDepartureAndArrivalAirports(String departure, String arrival) {
		EditText editDeparture = (EditText) mSolo.getView(R.id.departure_airport_edit_text);
		EditText editArrival = (EditText) mSolo.getView(R.id.arrival_airport_edit_text);

		mSolo.clickOnView((View) editDeparture);
		mSolo.enterText(editDeparture, departure);
		delay();

		mSolo.clickOnView((View) editArrival);
		mSolo.enterText(editArrival, arrival);
		delay();
	}

	public void flightsHappyPath(String departure, String arrival, int bookingDateOffset,
			boolean completeFlightBooking, boolean doHotelBooking)
			throws Exception, IntegrationFailureError {

		landscape();
		portrait();
		delay();

		clearPrivateData();
		launchFlights();
		delay();

		//If still on flights confirmation page
		//click to do a new search
		if (mSolo.searchText("qa-ehcc@mobiata.com", true)) {
			mSolo.clickOnImageButton(0);
			delay(10);
		}

		screenshot("Flights Search Screen");

		delay(5);

		enterDepartureAndArrivalAirports(departure, arrival);

		//Select Departure
		try {
			mSolo.clickOnText(mRes.getString(R.string.hint_select_departure));
		}
		catch (Error e) {
			enterLog(TAG, "Select departure text not there.");
		}

		landscape();
		portrait();

		delay();
		screenshot("Calendar");
		delay();
		CalendarTouchUtils.selectDay(mSolo, bookingDateOffset, R.id.calendar_date_picker);
		delay();

		//Click to search
		mSolo.clickOnView(mSolo.getView(R.id.search_button));
		delay(5);
		screenshot("Loading Flights");
		mSolo.waitForDialogToClose(10000);

		while (mSolo.searchText(mRes.getString(R.string.loading_flights))
				|| mSolo.searchText(mRes.getString(R.string.searching))) {
			delay(5);
			if (mSolo.searchText(mRes.getString(R.string.error_server))) {
				throw new IntegrationFailureError("INTEGRATION FAILURE: Unable to get flights search results");
			}
		}

		//Scroll up and down
		landscape();
		delay();
		screenshot("Search results");
		mSolo.scrollDown();
		delay();
		mSolo.scrollToBottom();
		delay();
		screenshot("Search results - bottom");
		portrait();
		delay();
		mSolo.scrollToTop();
		delay();

		if (mSolo.searchText(mRes.getString(R.string.error_server))) {
			throw new IntegrationFailureError("INTEGRATION FAILURE: Unable to get flights search results");
		}

		try {
			mSolo.clickOnText(mRes.getString(R.string.sort_flights).toUpperCase());
		}
		catch (Error e) {
			mSolo.clickOnText(mRes.getString(R.string.sort_flights));
		}

		screenshot("Sort fragment");
		mSolo.goBack();
		delay();

		//Select top flight in list.
		try {
			mSolo.clickInList(2, 2);
		}
		catch (AssertionFailedError e) {
			Log.e(TAG, "On older phone, so we must try to click in listview differently", e);
			mSolo.clickInList(2);
		}

		//Confirm flight selection
		//and advance to booking
		mSolo.waitForDialogToClose(10000);
		screenshot("Booking screen.");
		mSolo.scrollDown();
		delay();
		mSolo.scrollUp();
		delay(5);
		mSolo.clickOnText(mRes.getString(R.string.select_flight));
		mSolo.waitForDialogToClose(10000);
		if (mSolo.searchText(mRes.getString(R.string.error_server))) {
			throw new IntegrationFailureError("INTEGRATION FAILURE: Failed when checking for new price");
		}
		mSolo.clickOnText(mRes.getString(R.string.checkout_btn));

		//log in and do a booking
		logInAndBook(true, completeFlightBooking);

		//if hotel booking switch is true, do a hotel booking 
		//in that city
		if (doHotelBooking) {
			launchHotels();
			browseRooms(4, arrival, true);
		}
		else {
			mSolo.goBack();
		}

	}
}
