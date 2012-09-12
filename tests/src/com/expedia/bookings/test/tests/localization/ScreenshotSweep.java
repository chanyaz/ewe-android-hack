// POS/Locale Screenshot Sweep
// Made for Expedia Hotels Android App.
// Kevin Carpenter
// Some code derived from Daniel Lew's LocalizationTests.java

package com.expedia.bookings.test.tests.localization;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import junit.framework.AssertionFailedError;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.EditText;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchActivity;
import com.jayway.android.robotium.solo.Solo;

public class ScreenshotSweep extends ActivityInstrumentationTestCase2<SearchActivity> {

	public ScreenshotSweep() { //Default constructor
		super("com.expedia.bookings", SearchActivity.class);
	}

	private static final String PACKAGE_NAME = "com.expedia.bookings";
	private static final String TAG = "SearchTest";

	private Solo mSolo;

	private Resources mRes;
	DisplayMetrics mMetric;
	private boolean mFailed;
	private int mScreenShotCount;

	protected void setUp() throws Exception {
		mScreenShotCount = 1;
		super.setUp();
		mSolo = new Solo(getInstrumentation(), getActivity());
		//Log.configureLogging("ExpediaBookings", true);

		mRes = getActivity().getBaseContext().getResources();
		mMetric = mRes.getDisplayMetrics();
	}

	////////////////////////////////////////////////////////////////
	// Static Locale Data
	public static final String[] LOCALES = {
			"en_UK", "fr_CA", "en_HK",
			"zh_HK", "es_AR", "en_AU",
			"de_AT", "fr_BE", "nl_BE",
			"pt_BR", "en_CA", "da_DK",
			"fr_FR", "de_DE", "en_IN",
			"id_ID", "en_IE", "it_IT",
			"ja_JP", "es_MX", "en_MY",
			"nl_NL", "en_NZ", "nb_NO",
			"en_SG", "en_PH", "ko_KR",
			"es_ES", "sv_SE", "zh_TW",
			"en_US", "th_TH", "vi_VN"
	};

	public static Locale[] TEST_LOCALES = new Locale[] {
			new Locale("es", "AR"),
			new Locale("de", "AT"),
			new Locale("en", "AU"),
			new Locale("fr", "BE"),
			new Locale("nl", "BE"),
			new Locale("pt", "BR"),
			new Locale("en", "CA"),
			new Locale("fr", "CA"),
			new Locale("de", "DE"),
			new Locale("da", "DK"),
			new Locale("es", "ES"),
			new Locale("fr", "FR"),
			new Locale("en", "HK"),
			new Locale("zh", "HK"),
			new Locale("id", "ID"),
			new Locale("en", "IE"),
			new Locale("en", "IN"),
			new Locale("it", "IT"),
			new Locale("ja", "JP"),
			new Locale("ko", "KR"),
			new Locale("es", "MX"),
			new Locale("ms", "MY"),
			new Locale("nl", "NL"),
			new Locale("nb", "NO"),
			new Locale("en", "NZ"),
			new Locale("en", "PH"),
			new Locale("sv", "SE"),
			new Locale("th", "TH"),
			new Locale("en", "TW"),
			new Locale("zh", "TW"),
			new Locale("en", "UK"),
			new Locale("en", "US"),
			new Locale("vi", "VN")
	};

	public static final Map<Locale, Integer> LOCALE_TO_COUNTRY = new HashMap<Locale, Integer>();
	static {
		LOCALE_TO_COUNTRY.put(TEST_LOCALES[0], R.string.country_ar);
		LOCALE_TO_COUNTRY.put(TEST_LOCALES[1], R.string.country_at);
		LOCALE_TO_COUNTRY.put(TEST_LOCALES[2], R.string.country_au);
		LOCALE_TO_COUNTRY.put(TEST_LOCALES[3], R.string.country_be);
		LOCALE_TO_COUNTRY.put(TEST_LOCALES[4], R.string.country_be);
		LOCALE_TO_COUNTRY.put(TEST_LOCALES[5], R.string.country_br);
		LOCALE_TO_COUNTRY.put(TEST_LOCALES[6], R.string.country_ca);
		LOCALE_TO_COUNTRY.put(TEST_LOCALES[7], R.string.country_ca);
		LOCALE_TO_COUNTRY.put(TEST_LOCALES[8], R.string.country_de);
		LOCALE_TO_COUNTRY.put(TEST_LOCALES[9], R.string.country_dk);
		LOCALE_TO_COUNTRY.put(TEST_LOCALES[10], R.string.country_es);
		LOCALE_TO_COUNTRY.put(TEST_LOCALES[11], R.string.country_fr);
		LOCALE_TO_COUNTRY.put(TEST_LOCALES[12], R.string.country_hk);
		LOCALE_TO_COUNTRY.put(TEST_LOCALES[13], R.string.country_hk);
		LOCALE_TO_COUNTRY.put(TEST_LOCALES[14], R.string.country_id);
		LOCALE_TO_COUNTRY.put(TEST_LOCALES[15], R.string.country_ie);
		LOCALE_TO_COUNTRY.put(TEST_LOCALES[16], R.string.country_in);
		LOCALE_TO_COUNTRY.put(TEST_LOCALES[17], R.string.country_it);
		LOCALE_TO_COUNTRY.put(TEST_LOCALES[18], R.string.country_jp);
		LOCALE_TO_COUNTRY.put(TEST_LOCALES[19], R.string.country_kr);
		LOCALE_TO_COUNTRY.put(TEST_LOCALES[20], R.string.country_mx);
		LOCALE_TO_COUNTRY.put(TEST_LOCALES[21], R.string.country_my);
		LOCALE_TO_COUNTRY.put(TEST_LOCALES[22], R.string.country_nl);
		LOCALE_TO_COUNTRY.put(TEST_LOCALES[23], R.string.country_no);
		LOCALE_TO_COUNTRY.put(TEST_LOCALES[24], R.string.country_nz);
		LOCALE_TO_COUNTRY.put(TEST_LOCALES[25], R.string.country_ph);
		LOCALE_TO_COUNTRY.put(TEST_LOCALES[26], R.string.country_se);
		LOCALE_TO_COUNTRY.put(TEST_LOCALES[27], R.string.country_th);
		LOCALE_TO_COUNTRY.put(TEST_LOCALES[28], R.string.country_tw);
		LOCALE_TO_COUNTRY.put(TEST_LOCALES[29], R.string.country_tw);
		LOCALE_TO_COUNTRY.put(TEST_LOCALES[30], R.string.country_gb);
		LOCALE_TO_COUNTRY.put(TEST_LOCALES[31], R.string.country_us);
		LOCALE_TO_COUNTRY.put(TEST_LOCALES[32], R.string.country_vn);
	}

	////////////////////////////////////////////////////////////////
	// Setting Locale

	private void setLocale(Locale locale) {
		Configuration config = mRes.getConfiguration(); //get current configuration
		config.locale = locale; //set to locale specified
		Locale.setDefault(locale);
		mRes.updateConfiguration(config, mMetric);

	}

	////////////////////////////////////////////////////////////////
	// Helpful Methods

	public String getStringFromR(int idFromR) {
		String RString = getActivity().getApplication().getResources().getString(idFromR);
		return RString;
	}

	private static void enterLog(String logText) {
		Log.d(TAG, "Robotium: " + logText);
	}

	public void delay(int time) { //Enter time in seconds
		time = time * 1000;
		mSolo.sleep(time);
	}

	public void delay() { //Defaults to 3 seconds
		mSolo.sleep(3000);
	}

	private void screenshot(String fileName) { //screenshot is saved to device SD card.
		String currentLocale = mRes.getConfiguration().locale.toString();

		mSolo.takeScreenshot(currentLocale + " " + String.format("%02d", mScreenShotCount) + " " + fileName);
		mScreenShotCount++;
	}

	////////////////////////////////////////////////////////////////
	// Changing Settings

	public void changePOS(Locale locale) throws Exception {
		enterLog("Changing POS");

		mSolo.pressMenuItem(0); // e.g. "Settings"

		String countryHeader = getStringFromR(R.string.preference_point_of_sale_title);
		mSolo.clickOnText(countryHeader);

		String countrySelection = getStringFromR(LOCALE_TO_COUNTRY.get(locale));
		Log.d(TAG, "Our countrySelection is: " + countrySelection);
		delay(1);
		mSolo.clickOnText(countrySelection);
		mSolo.clickOnButton(0);
		delay();
		setLocale(locale);
		mSolo.goBack();

	}

	public void changeAPI(String API) throws Exception {
		enterLog("Changing API Server");
		mSolo.pressMenuItem(0);
		mSolo.clickOnText("Select ");
		mSolo.scrollUpList(0);
		mSolo.clickOnText(API);
		mSolo.goBack();
	}

	////////////////////////////////////////////////////////////////
	// Search Screen Methods

	public void closeBanner() {
		try{
			mSolo.clickOnView(mSolo.getView(R.id.widget_notification_close_btn));
		}
		finally{
			//nothing
		}
	}

	public void selectLocation(String location) throws Exception {
		enterLog("Searching for destination " + location);
		delay(1);
		mSolo.clickOnEditText(0);
		mSolo.clearEditText(0);
		mSolo.typeText(0, location);
		delay(1);
		enterLog("Before clicking search button");
		mSolo.clickInList(1);//Selecting search suggestion results
		enterLog("AFter clicking search button");

		mSolo.waitForActivity("ExpediaBookingApp");
		mSolo.waitForDialogToClose(15000);
		enterLog("Location searched for and results loaded!");
		delay();
		screenshot("Search Results");
		mSolo.scrollDown();
		delay(1);
		screenshot("Results2");
		delay(1);
		mSolo.scrollDown();
		delay(1);
		screenshot("Results3");
	}

	public void filterFor(String filterText) { //filter currently does not work.
		//solo.clickOnText(getStringFromR(R.string.filter_hotels));
		mSolo.clickOnText("FILTER");
		mSolo.enterText(0, filterText);
		delay(1);
		screenshot("Filtering for " + filterText);
		mSolo.goBack();
		delay(1);
		screenshot("After Filtering for " + filterText);
	}

	public void pressSort() {
		delay(1);
		String sortText = "SORT";
		mSolo.clickOnText(sortText);
		//solo.clickOnText(getStringFromR(R.string.sort_description_distance));
		delay(1);
		mSolo.clickOnText(getStringFromR(R.string.sort_description_popular));
		screenshot("Sort by Popular Results");
		delay(1);

		/*Doesn't work when searching for city
		mSolo.clickOnText(sortText);
		delay(1);
		mSolo.clickOnText(getStringFromR(R.string.sort_description_distance));
		screenshot("Sort by Distance Results");
		delay(1);
		 */

		mSolo.clickOnText(sortText);
		delay(1);
		mSolo.clickOnText(getStringFromR(R.string.sort_description_price));
		screenshot("Sort by Price Results");
		delay(1);

		mSolo.clickOnText(sortText);
		delay(1);
		mSolo.clickOnText(getStringFromR(R.string.sort_description_rating));
		screenshot("Sort by Rating Results");
		delay(1);

		mSolo.clickOnText(sortText);
		delay(1);
		mSolo.clickOnText(getStringFromR(R.string.sort_description_deals));
		screenshot("Sort by Deals Results");
		delay(1);

	}

	public void pressCalendar() {
		mSolo.clickOnImageButton(1);
		delay(1);
		screenshot("Calendar");
		mSolo.goBack();
	}

	public void pressGuestPicker() {
		mSolo.clickOnImageButton(0);
		delay(2);
		screenshot("Guest Picker");
		mSolo.clickOnImageButton(2); // Adult +1
		delay(1);
		screenshot("GuestPicker Add Adult");
		delay(1);
		mSolo.clickOnImageButton(5); // Child +1
		delay(1);
		screenshot("GuestPicker Add Child");
		mSolo.goBack();
	}

	public void selectHotel(int hotelIndex) throws Exception {
		enterLog("Picking hotel at index " + hotelIndex);

		mSolo.clickInList(hotelIndex);
		mSolo.waitForActivity("HotelDetailsFragmentActivity");
		delay();
		delay();

		enterLog("Hotel Details have loaded!");
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

	public void pressBookRoom() throws Exception {
		String bookNowString = getStringFromR(R.string.book_now);

		enterLog("Pressing Book Room Button: " + bookNowString);
		mSolo.clickOnButton(0);

		Boolean didItLoad = mSolo.waitForActivity("RoomsAndRatesListActivity", 20000);
		if (didItLoad) {
			enterLog("On Rooms and Rates Screen");
			delay();
		}
		else
			enterLog("Didn't load after 20 seconds.");
	}

	////////////////////////////////////////////////////////////////
	// Rooms & Rates Screens methods

	public void selectRoom(int roomIndex) throws Exception {
		enterLog("About to select room at index " + roomIndex);
		delay();

		screenshot("Rooms and Rates Screen");
		mSolo.clickInList(roomIndex);
		delay();

		mSolo.waitForActivity("ExpediaBookingActivity");
		enterLog("On Booking Screen.");
	}

	////////////////////////////////////////////////////////////////
	// Booking Screen methods

	public void bookingScreenShots() {

		screenshot("Booking Screen 1");
		mSolo.scrollDown();
		delay(1);

		screenshot("Booking Screen 2");
		mSolo.scrollDown();
		delay(1);

		screenshot("Booking Screen 3");
		mSolo.scrollToBottom();
		delay(1);

		screenshot("Bottom of Booking Screen");
		mSolo.scrollToTop();
	}

	public void logIn() {
		String loginButtonText = getStringFromR(R.string.log_in_for_faster_booking);

		if (loginButtonText == null) {
			//The US String is different from all other POS
			//So we must check it make sure we're looking for the right one.
			loginButtonText = getStringFromR(R.string.log_in_to_expedia);
		}

		enterLog("Pressing button: " + loginButtonText);
		mSolo.clickOnText(loginButtonText);
		delay(1);
		screenshot("Login Screen Pre Text Entry");

		mSolo.typeText(0, "qa-ehcc@mobiata.com");
		delay(1);
		mSolo.typeText(1, "3xp3d1acc");
		delay(1);
		screenshot("Login Screen Post Text Entry");

		mSolo.clickOnButton(0); //Log in button.
		delay();
		screenshot("Booking Info Post-Login");
		mSolo.scrollToTop();
	}

	public void enterPhoneNumber(String phoneNumber) throws Exception {
		mSolo.enterText((EditText) mSolo.getView(R.id.telephone_edit_text), phoneNumber);
	}

	public void enterCCV(String CCV) throws Exception {
		mSolo.scrollUp();
		mSolo.clickOnView(mSolo.getView(R.id.security_code_edit_text));
		mSolo.enterText((EditText) mSolo.getView(R.id.security_code_edit_text), "111");
		enterLog("Entered CCV");
	}

	//Frequently, different POS have different requirements as to what info has to be entered
	//after log in. The try-catch blocks eliminate the need to hardcode what information is needed
	//where, based upon the POS/locale that you are in.

	public void pressCheckBox() {
		try {
			mSolo.scrollToBottom();
			mSolo.clickOnCheckBox(0);
		}
		catch (AssertionFailedError e) {
			//Nothing.
		}
	}

	public void enterMissingInfo() {
		try {// Entering Phone Number
			enterPhoneNumber("7342122392");
		}
		catch (Exception e) {
			//Nothing needs to be done.
		}

		try { //Entering First and last names
			EditText firstNameEditText = (EditText) mSolo.getView(R.id.first_name_edit_text);
			EditText lastNameEditText = (EditText) mSolo.getView(R.id.last_name_edit_text);

			mSolo.clearEditText(firstNameEditText);
			mSolo.enterText(firstNameEditText, "JexperCC");

			mSolo.clearEditText(lastNameEditText);
			mSolo.enterText(lastNameEditText, "MobiataTestaverde");
		}
		catch (Exception e) {
			//Nothing.
		}

		try {
			EditText Address1 = (EditText) mSolo.getView(R.id.address1_edit_text);
			EditText City = (EditText) mSolo.getView(R.id.city_edit_text);
			EditText State = (EditText) mSolo.getView(R.id.state_edit_text);
			EditText ZIP = (EditText) mSolo.getView(R.id.postal_code_edit_text);
			EditText CreditCard = (EditText) mSolo.getView(R.id.card_number_edit_text);
			EditText ExpMonth = (EditText) mSolo.getView(R.id.expiration_month_edit_text);
			EditText ExpYear = (EditText) mSolo.getView(R.id.expiration_year_edit_text);

			mSolo.clearEditText(Address1);
			mSolo.enterText(Address1, "1234 Test Blvd");

			mSolo.clearEditText(City);
			mSolo.enterText(City, "Ann Arbor");

			mSolo.clearEditText(State);
			mSolo.enterText(State, "MI");

			mSolo.clearEditText(ZIP);
			mSolo.enterText(ZIP, "48104");

			mSolo.clearEditText(CreditCard);
			mSolo.enterText(CreditCard, "4111111111111111");

			mSolo.clearEditText(ExpMonth);
			mSolo.enterText(ExpMonth, "12");

			mSolo.clearEditText(ExpYear);
			mSolo.enterText(ExpYear, "20");

		}
		catch (Exception e) {
			//Nothing.
		}

		pressCheckBox(); //Check box for terms & conditions occasionally needed.
	}

	public void confirmAndBook() throws Exception {
		delay(5);
		String confirmAndBookText = getStringFromR(R.string.confirm_book);
		mSolo.clickOnText(confirmAndBookText);
		Boolean screenLoaded = mSolo.waitForActivity("ConfirmationFragmentActivity", 60000);

		if (screenLoaded) {
			enterLog("Should be on confirmation screen now.");
			delay();
			screenshot("Confirmation Screen 1");
			mSolo.scrollToBottom();
			delay(1);
			screenshot("Confirmation Screen 2");
			mSolo.scrollToTop();
		}
		else {
			enterLog("Never got to confirmation screen.");
		}
		String newSearchString = getStringFromR(R.string.new_search);

		mSolo.clickOnText(newSearchString);
		enterLog("Back at search!");
		delay(5);
	}

	public void logInAndBook() throws Exception {

		bookingScreenShots();

		logIn();
		delay(1);
		enterMissingInfo();

		enterCCV("111");

		confirmAndBook();
	}

	////////////////////////////////////////////////////////////////
	// Info Screen Methods

	public void captureInfoScreen() {
		mSolo.clickOnMenuItem(getStringFromR(R.string.About));
		delay(2);
		screenshot("Info Screen 1");
		delay(1);
		mSolo.scrollToBottom();
		delay(1);
		screenshot("Info Screen 2");
		delay(1);
		mSolo.goBack();
	}

	//////////////////////////////////////////////////////////////// 
	// Test Driver 

	public void testBooking() throws Exception {
		for (int i = 0; i < TEST_LOCALES.length; i++) {
			enterLog("Starting sweep of " + TEST_LOCALES[i].toString());
			Locale testingLocale = TEST_LOCALES[i];
			setLocale(testingLocale);
			mScreenShotCount = 1;
			closeBanner();

			delay(1);
			changePOS(TEST_LOCALES[i]);
			changeAPI("Integration");
			delay(2);
			setLocale(testingLocale);
			pressCalendar();
			pressGuestPicker();
			selectLocation("New York City");
			filterFor("Westin");
			pressSort();
			selectHotel(2);
			delay();
			pressBookRoom();
			selectRoom(0);
			delay();
			bookingScreenShots();
			logInAndBook();
			captureInfoScreen();

		}
	}

	@Override
	protected void tearDown() throws Exception {
		//Robotium will finish all the activities that have been opened
		enterLog("tearing down...");

		mSolo.finishOpenedActivities();
	}

}