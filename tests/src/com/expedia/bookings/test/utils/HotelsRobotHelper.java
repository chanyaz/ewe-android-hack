package com.expedia.bookings.test.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import junit.framework.AssertionFailedError;
import android.content.res.Resources;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

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
	private EventTrackingUtils mFileWriter;
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

			mScreen.screenshot(currentLocale + " " + String.format("%02d", mScreenShotCount) + " " + fileName);
			mScreenShotCount++;
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

	public void clickTopRightBtn() {
		int w = 479;
		int h = 46;
		mSolo.clickOnScreen(w, h);
	}

	public void createFileWriter() {
		mFileWriter = new EventTrackingUtils();
		mWriteEventsToFile = true;
		mFileWriter.addLineToFile("Install Event within a couple of minutes",
				true);
	}

	public void closeFileWriter() {
		mFileWriter.closeFileWriter();
	}

	public void flushFileWriter() {
		mFileWriter.flushFileWriter();
	}

	//Can't use before instantiating mFileWriter!
	// Reading any instructions from a text file
	// Expected file format:
	// Line 1: Device information
	// Other lines: any other info
	public void readInstructionsToOutFile(String inputFile) {
		BufferedReader fileIn;
		try {
			fileIn = new BufferedReader(new FileReader(inputFile));
			String deviceName = fileIn.readLine();
			mFileWriter.addLineToFile("Device: " + deviceName, false);
			String fileLine;
			while ((fileLine = fileIn.readLine()) != null) {
				mFileWriter.addLineToFile(fileLine, false);
			}
			fileIn.close();
		}
		catch (FileNotFoundException e) {
			Log.e(TAG, "FileNotFoundException", e);
		}
		catch (IOException e) {
			Log.e(TAG, "IOException", e);
		}
		finally {
			closeFileWriter();
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
		mLocaleUtils.setLocale(locale);

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
			delay(1);
			screenshot("Results2");
			delay(1);
			mSolo.scrollDown();
			delay(1);
			screenshot("Results3");
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

		if (mWriteEventsToFile) {
			mFileWriter.addLineToFile("POS: " + currentPOS, false);
		}

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

		mSolo.waitForActivity("ExpediaBookingApp"); // Add another wait if this causes instability
		enterLog(TAG, "Location searched for and results loaded!");

		delay();
		hotelListScreenshots();
	}

	public void filterFor(String filterText) { //filter currently does not work.
		//solo.clickOnText(getStringFromR(R.string.filter_hotels));
		View filterButton = mSolo.getView(R.id.menu_select_filter);
		//Korea and Japan do not support filtering because 
		//most hotel names are in their respective languages' characters
		if (mRes.getConfiguration().locale != mLocaleUtils.APAC_LOCALES[4]
				&& mRes.getConfiguration().locale != mLocaleUtils.APAC_LOCALES[5]
				&& mRes.getConfiguration().locale != mLocaleUtils.APAC_LOCALES[1]
				&& mRes.getConfiguration().locale != mLocaleUtils.APAC_LOCALES[12]
				&& mRes.getConfiguration().locale != mLocaleUtils.APAC_LOCALES[15]) {
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
		}
		catch (Error e) {
			enterLog(TAG, "No reviews for hotel selected");
		}

		mSolo.goBack();
	}

	public void pressBookRoom() {

		mSolo.clickOnButton(0);
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
		catch (Error noRoomsListed) {
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

		screenshot("Booking Screen 2");
		mSolo.scrollToBottom();
		delay(1);

		screenshot("Bottom of Booking Screen");
		mSolo.scrollToTop();
	}

	public void logIn() {
		enterLog(TAG, "Beginning log-in sequence.");
		mSolo.scrollToTop();
		mSolo.clickOnText(mRes.getString(R.string.checkout_btn));
		String log_in_for_faster_booking = mRes.getString(R.string.log_in_for_faster_booking);
		String log_in_with_expedia = mRes.getString(R.string.Log_in_with_Expedia);
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

		// Log log in event for ad tracking
		if (mWriteEventsToFile) {
			mFileWriter.addLineToFile("Log in event at", true);
		}

		delay(5);
		mSolo.scrollToTop();
		delay();
		screenshot("Booking Info Post-Login");

	}

	public void enterPhoneNumber() throws Exception {
		enterLog(TAG, "Booking: Entering phone number");
		mSolo.enterText((EditText) mSolo.getView(R.id.telephone_edit_text), mUser.mPhoneNumber);
	}

	public void enterCCV() throws Exception {
		enterLog(TAG, "Booking: Entering CVV");
		//mSolo.scrollUp();
		delay(3);
		screenshot("CVV Screen");
		landscape();
		portrait();
		delay(5);
		final EditText CCVview =
				(EditText) mSolo.getCurrentActivity().findViewById(R.id.security_code_edit_text);

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

		mSolo.enterText((EditText)
				mSolo.getCurrentActivity().findViewById(R.id.edit_first_name),
				mUser.mFirstName);
		mSolo.enterText((EditText)
				mSolo.getCurrentActivity().findViewById(R.id.edit_last_name),
				mUser.mLastName);
		mSolo.enterText((EditText)
				mSolo.getCurrentActivity().findViewById(R.id.edit_phone_number),
				mUser.mPhoneNumber);

		landscape();
		portrait();

		delay();
		mSolo.clickOnText(mRes.getString(R.string.button_done));
		//mSolo.clickOnButton(1);

		delay();
	}

	public void enterMissingInfo() {
		enterLog(TAG, "Booking: entering traveler info.");
		String travelerInfo = mSolo.getString(R.string.enter_traveler_info);

		if (mSolo.searchText(travelerInfo, true)) {
			mSolo.clickOnText(travelerInfo);
			enterNewTraveler();
		}

		String select_payment_sentence_case = mSolo.getString(R.string.select_payment_sentence_case);

		if (mSolo.searchText(select_payment_sentence_case, true)) {
			mSolo.clickOnText(select_payment_sentence_case);
		}
		else {
			String select_payment = mSolo.getString(R.string.select_payment);
			mSolo.clickOnText(select_payment);
		}

		delay();
		screenshot("Select payment");

		landscape();
		portrait();
		delay();

		mSolo.clickOnText(mSolo.getString(R.string.add_new_card));
		delay(1);
		screenshot("Add new card");
		delay(1);

		landscape();
		portrait();
		delay(5);
		screenshot("Credit card info.");

		if (mSolo.searchText(mRes.getString(R.string.billing_address))) {
			inputBillingAddress();
		}
		inputCCBillingInfo();

	}

	public void inputBillingAddress() {
		enterLog(TAG, "Booking: entering billing address.");

		mSolo.enterText((EditText) mSolo.getView(R.id.edit_address_line_one),
				mUser.mAddressLine1);

		mSolo.enterText((EditText) mSolo.getView(R.id.edit_address_city),
				mUser.mCityName);

		mSolo.enterText((EditText) mSolo.getView(R.id.edit_address_state),
				mUser.mStateCode);

		mSolo.enterText((EditText) mSolo.getView(R.id.edit_address_postal_code),
				mUser.mZIPCode);

		mSolo.clickOnText(mRes.getString(R.string.next));

	}

	public void inputCCBillingInfo() {
		enterLog(TAG, "Booking: entering billing credit card information.");

		// Enter Credit Card Number
		mSolo.enterText((EditText) mSolo.getView(R.id.edit_creditcard_number),
				mUser.mCreditCardNumber);

		// Enter Cardholder's name
		mSolo.typeText((EditText) mSolo.getView(R.id.edit_name_on_card),
				mUser.mFirstName + " " + mUser.mLastName);

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

	public void confirmAndBook(boolean assertPostCVVPopUp) throws Exception {
		enterLog(TAG, "Booking: About to slide to accept.");
		delay(5);
		screenshot("Slide to checkout.");
		delay();
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
		landscape();
		portrait();
		delay();
		mSolo.scrollToBottom();

		View sliderStart = mSolo.getView(R.id.slider_image);
		int[] startLocation = new int[2];
		sliderStart.getLocationOnScreen(startLocation);

		View sliderEnd = mSolo.getView(R.id.destination_image);
		int[] endLocation = new int[2];
		sliderEnd.getLocationOnScreen(endLocation);

		enterLog(TAG, "Booking: Slide X from: " + startLocation[0] + " to " + endLocation[0] + ".");
		enterLog(TAG, "Booking: Slide Y from: " + startLocation[1] + " to " + endLocation[1] + ".");
		delay();

		mSolo.drag(startLocation[0], mScreenWidth - 5, startLocation[1] + 25, endLocation[1] + 20, 10);

		delay(5);
		enterCCV();

		if (mSolo.searchText("Sorry, we don't seem to be able", true)) {
			//If asserting post-cvv entry popup, assert that
			// leaving pop up takes you back to CC entry view
			if (assertPostCVVPopUp) {
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
			Boolean screenLoaded = mSolo.waitForActivity("ConfirmationFragmentActivity");

			if (screenLoaded) {
				enterLog(TAG, "Booking: Should be on confirmation screen now.");
				delay();
				screenshot("Confirmation Screen 1");
				landscape();
				delay(1);
				portrait();
				mSolo.scrollToBottom();
				delay(1);
				screenshot("Confirmation Screen 2");
				mSolo.scrollToTop();

				// Log booking event for ad tracking
				if (mWriteEventsToFile) {
					mFileWriter.addLineToFile("BOOKING EVENT", true);
				}
			}
			else {
				enterLog(TAG, "Booking: Never got to confirmation screen.");
			}
			if (!mSolo.searchText(mRes.getString(R.string.total_cost), true)) {
				delay();
				mSolo.scrollToTop();
				screenshot("Confirmation Screen 1");
				mSolo.scrollDown();
				screenshot("Confirmation Screen 2");
				mSolo.clickOnActionBarItem(R.drawable.ic_action_bar_magnifying_glass);
			}
			else {
				mSolo.clickOnText(mRes.getString(R.string.NEW_SEARCH));
				enterLog(TAG, "Booking: Going back to launcher.");
				delay();
				mSolo.goBack();
			}
		}
	}

	public void logInAndBook() throws Exception {
		bookingScreenShots();

		logIn();
		delay(1);
		enterMissingInfo();

		confirmAndBook(false);

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
		if (completeABooking) {
			selectHotel(2);
			pressBookRoom();
			selectRoom(0);
			logInAndBook();
		}

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
		delay(2);
		portrait();
		delay(2);
		screenshot("Info Screen 1");
		delay(1);
		mSolo.scrollToBottom();
		delay(1);
		screenshot("Info Screen 2");
		delay(1);
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

	public void flightsHappyPath(String departure, String arrival, int bookingDateOffset, boolean doHotelBooking)
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
		mSolo.clickInList(2);

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
		logInAndBook();

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
