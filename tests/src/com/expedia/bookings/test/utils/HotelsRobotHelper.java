package com.expedia.bookings.test.utils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import junit.framework.AssertionFailedError;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Log;
import android.widget.EditText;

import com.expedia.bookings.R;
import com.jayway.android.robotium.solo.Solo;

public class HotelsRobotHelper {
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
	private static final String TAG = "com.expedia.bookings.test";
	private boolean mAllowScreenshots;
	private boolean mAllowOrientationChange;
	private int mScreenShotCount;
	private Solo mSolo;
	private Resources mRes;
	private HotelsUserData mUser; //user info container

	//Defaults are set, including the default user booking info
	//which is set to the qa-ehcc@mobiata.com account's info
	public HotelsRobotHelper(Solo solo, Resources res) {
		this(solo, res, new HotelsUserData());
	}

	//Constructor for user created book user container
	public HotelsRobotHelper(Solo solo, Resources res, HotelsUserData customUser) {
		mAllowScreenshots = true;
		mAllowOrientationChange = true;
		mScreenShotCount = 1;
		mSolo = solo;
		mRes = res;
		mUser = customUser;
	}

	////////////////////////////////////////////////////////////////
	// Setting Locale

	public void setLocale(Locale locale) {
		Configuration config = mRes.getConfiguration(); //get current configuration
		config.locale = locale; //set to locale specified
		Locale.setDefault(locale);
		mRes.updateConfiguration(config, mRes.getDisplayMetrics());

	}

	////////////////////////////////////////////////////////////////
	// Helpful Methods

	public void enterLog(String TAG, String logText) {
		Log.d(TAG, "Robotium: " + logText);
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

	public void setScreenshotCount(int count) {
		mScreenShotCount = count;
	}

	public void screenshot(String fileName) { //screenshot is saved to device SD card.
		if (mAllowScreenshots) {
			String currentLocale = mRes.getConfiguration().locale.toString();

			mSolo.takeScreenshot(currentLocale + " " + String.format("%02d", mScreenShotCount) + " " + fileName);
			mScreenShotCount++;
		}
	}

	public void landscape() {
		if (mAllowOrientationChange) {
			delay(1);
			mSolo.setActivityOrientation(Solo.LANDSCAPE);
			delay(1);
		}
	}

	public void portrait() {
		if (mAllowOrientationChange) {
			delay(1);
			mSolo.setActivityOrientation(Solo.PORTRAIT);
			delay(1);
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
		try{
			mSolo.clickOnMenuItem(settingsString, false);
		}catch(AssertionFailedError E){
			mSolo.goBack();
			delay();
			mSolo.pressMenuItem(1);
			delay();
			mSolo.goBack();
			mSolo.pressMenuItem(0);
		}
		
		String countryHeader = mRes.getString(R.string.preference_point_of_sale_title);
		mSolo.clickOnText(countryHeader);

		String countrySelection = mRes.getString(LOCALE_TO_COUNTRY.get(locale));
		Log.d(TAG, "Our countrySelection is: " + countrySelection);
		delay(1);
		mSolo.clickOnText(countrySelection);
		mSolo.clickOnButton(0);
		delay(1);
		mSolo.clickOnButton(0);
		setSpoofBookings();
		mSolo.goBack();
		delay();
		setLocale(locale);

	}

	public void clearPrivateData() {
		mSolo.pressMenuItem(0);
		landscape();
		portrait();
		String clearPrivateData = mRes.getString(R.string.clear_private_data);
		mSolo.clickOnText(clearPrivateData);
		mSolo.clickOnButton(0);
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
		try {
			if (!mSolo.isCheckBoxChecked(0)) {
				mSolo.clickOnCheckBox(0);
			}

		}
		catch (Exception E) {
			//nothing
		}

	}

	////////////////////////////////////////////////////////////////
	// Search Screen Methods

	public void closeBanner() {
		try {
			mSolo.clickOnView(mSolo.getView(R.id.widget_notification_close_btn));
		}
		catch (AssertionFailedError AFE) {
			//nothing
		}
		catch (Exception E) {
			//nothing 
		}
	}

	public void selectLocation(String location) throws Exception {
		enterLog(TAG, "Searching for destination " + location);
		delay(5);
		mSolo.clickOnEditText(0);
		delay(1);
		mSolo.clearEditText(0);
		delay(1);
		mSolo.typeText(0, location);
		delay(3);
		enterLog(TAG, "Before clicking search button");
		mSolo.clickInList(1);//Selecting search suggestion results
		enterLog(TAG, "After clicking search button");

		mSolo.waitForActivity("ExpediaBookingApp");
		mSolo.waitForDialogToClose(15000);
		enterLog(TAG, "Location searched for and results loaded!");
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

		String filter = mRes.getString(R.string.FILTER);
		//Korea and Japan do not support filtering because 
		//most hotel names are in their respective languages' characters
		if(mRes.getConfiguration().locale != TEST_LOCALES[18] && mRes.getConfiguration().locale != TEST_LOCALES[19]){
			enterLog(TAG, "Clicking on label: " + filter);
			mSolo.clickOnText(filter);
			mSolo.enterText(0, filterText);
			delay(1);
			screenshot("Filtering for " + filterText);
			mSolo.goBack();
			delay(1);
			screenshot("After Filtering for " + filterText);
		}
	}

	public void pressSort() {
		delay(1);
		String sortText = mRes.getString(R.string.SORT);
		enterLog(TAG, "Clicking on label: " + sortText);
		mSolo.clickOnText(sortText);
		//solo.clickOnText(getStringFromR(R.string.sort_description_distance));
		delay(1);
		mSolo.clickOnText(mRes.getString(R.string.sort_description_popular));
		screenshot("Sort by Popular Results");
		delay(1);

		/*Doesn't work when searching for city
		mSolo.clickOnText(sortText);
		delay(solo, 1);
		mSolo.clickOnText(getStringFromR(R.string.sort_description_distance));
		screenshot(mRes, solo, "Sort by Distance Results");
		delay(solo, 1);
		 */

		mSolo.clickOnText(sortText);
		delay(1);
		mSolo.clickOnText(mRes.getString(R.string.sort_description_price));
		screenshot("Sort by Price Results");
		delay(1);

		mSolo.clickOnText(sortText);
		delay(1);
		mSolo.clickOnText(mRes.getString(R.string.sort_description_rating));
		screenshot("Sort by Rating Results");
		delay(1);

		try {
			mSolo.clickOnText(sortText);
			delay(1);
			mSolo.clickOnText(mRes.getString(R.string.sort_description_deals));
			screenshot("Sort by Deals Results");
			delay(1);
		}
		catch (Exception e) {
			mSolo.goBack();
		}

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
		enterLog(TAG, "Picking hotel at index " + hotelIndex);

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

	public void pressBookRoom() {
		String bookNowString = mRes.getString(R.string.SELECT);

		enterLog(TAG, "Pressing Book Room Button: " + bookNowString);
		mSolo.clickOnText(bookNowString);

		Boolean didItLoad = mSolo.waitForActivity("RoomsAndRatesListActivity", 20000);
		if (didItLoad) {
			enterLog(TAG, "On Rooms and Rates Screen");
			delay();
		}
		else{
			enterLog(TAG, "Didn't load after 20 seconds.");
			delay(5); 
		}
	}

	////////////////////////////////////////////////////////////////
	// Rooms & Rates Screens methods

	public void selectRoom(int roomIndex) throws Exception {
		enterLog(TAG, "About to select room at index " + roomIndex);
		delay();

		screenshot("Rooms and Rates Screen");
		mSolo.clickInList(roomIndex);
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
		mSolo.scrollDown();
		delay(1);

		screenshot("Booking Screen 3");
		mSolo.scrollToBottom();
		delay(1);

		screenshot("Bottom of Booking Screen");
		mSolo.scrollToTop();
	}

	public void logIn(Resources mRes) {
		String loginButtonText = mRes.getString(R.string.log_in_for_faster_booking);

		if (loginButtonText == null) {
			//The US String is different from all other POS
			//So we must check it make sure we're looking for the right one.
			loginButtonText = mRes.getString(R.string.log_in_to_expedia);
		}

		enterLog(TAG, "Pressing button: " + loginButtonText);
		mSolo.clickOnText(loginButtonText);
		delay(1);
		screenshot("Login Screen Pre Text Entry");

		mSolo.typeText(0, mUser.mLoginEmail);

		landscape();
		portrait();
		delay();
		mSolo.typeText(1, mUser.mLoginPassword);
		landscape();
		portrait();

		delay(1);

		screenshot("Login Screen Post Text Entry");

		mSolo.clickOnButton(0); //Log in button.
		delay();
		screenshot("Booking Info Post-Login");
		mSolo.scrollToTop();
	}

	public void enterPhoneNumber() throws Exception {
		mSolo.enterText((EditText) mSolo.getView(R.id.telephone_edit_text), mUser.mPhoneNumber);
	}

	public void enterCCV() throws Exception {
		mSolo.scrollUp();
		mSolo.clickOnView(mSolo.getView(R.id.security_code_edit_text));
		mSolo.enterText((EditText) mSolo.getView(R.id.security_code_edit_text), mUser.mCCV);
		enterLog(TAG, "Entered CCV");
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
			enterPhoneNumber();
		}
		catch (Exception e) {
			//Nothing needs to be done.
		}

		try { //Entering First and last names
			EditText firstNameEditText = (EditText) mSolo.getView(R.id.first_name_edit_text);
			EditText lastNameEditText = (EditText) mSolo.getView(R.id.last_name_edit_text);

			mSolo.clearEditText(firstNameEditText);
			mSolo.enterText(firstNameEditText, mUser.mFirstName);

			mSolo.clearEditText(lastNameEditText);
			mSolo.enterText(lastNameEditText, mUser.mLastName);
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
			mSolo.enterText(Address1, mUser.mAddressLine1);

			mSolo.clearEditText(City);
			mSolo.enterText(City, mUser.mCityName);

			mSolo.clearEditText(State);
			mSolo.enterText(State, mUser.mStateCode);

			mSolo.clearEditText(ZIP);
			mSolo.enterText(ZIP, mUser.mZIPCode);

			mSolo.clearEditText(CreditCard);
			mSolo.enterText(CreditCard, mUser.mCreditCardNumber);

			mSolo.clearEditText(ExpMonth);
			mSolo.enterText(ExpMonth, mUser.mCardExpMonth);

			mSolo.clearEditText(ExpYear);
			mSolo.enterText(ExpYear, mUser.mCardExpYear);

		}
		catch (Exception e) {
			//Nothing.
		}

		pressCheckBox(); //Check box for terms & conditions occasionally needed.
	}

	public void confirmAndBook() throws Exception {
		delay(5);
		mSolo.clickOnButton(0);
		Boolean screenLoaded = mSolo.waitForActivity("ConfirmationFragmentActivity", 60000);

		if (screenLoaded) {
			enterLog(TAG, "Should be on confirmation screen now.");
			delay();
			screenshot("Confirmation Screen 1");
			landscape();
			delay(1);
			portrait();
			mSolo.scrollToBottom();
			delay(1);
			screenshot("Confirmation Screen 2");
			mSolo.scrollToTop();
		}
		else {
			enterLog(TAG, "Never got to confirmation screen.");
		}
		try {
			mSolo.clickOnText(mRes.getString(R.string.new_search));
		}
		catch (AssertionFailedError E) {
			enterLog(TAG, "New Search string not localized.");
			mSolo.clickOnText("NEW SEARCH");
		}
		enterLog(TAG, "Back at search!");
		delay(5);
	}

	public void logInAndBook() throws Exception {

		bookingScreenShots();

		logIn(mRes);
		delay(1);
		enterMissingInfo();

		enterCCV();

		confirmAndBook();
	}

	////////////////////////////////////////////////////////////////
	// Info Screen Methods

	public void captureInfoScreen() {
		mSolo.clickOnMenuItem(mRes.getString(R.string.About));
		delay(2);
		screenshot("Info Screen 1");
		delay(1);
		mSolo.scrollToBottom();
		delay(1);
		screenshot("Info Screen 2");
		delay(1);
		mSolo.goBack();
	}

}
