package com.expedia.bookings.test.utils;

import java.util.Arrays;
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
import com.mobiata.testutils.CalendarTouchUtils;

public class HotelsRobotHelper {
	////////////////////////////////////////////////////////////////
	// Static Locale Data
	//TODO make these a different container so cool methods can be used
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
			"en_US", "th_TH", "vi_VN",
			"tl_PH", "zh_CN"
	};

	public static Locale[] AMERICAN_LOCALES = new Locale[] {
			new Locale("es", "AR"),
			new Locale("pt", "BR"),
			new Locale("en", "CA"),
			new Locale("fr", "CA"),
			new Locale("es", "MX"),
			new Locale("en", "US")
	};

	public static Locale[] APAC_LOCALES = new Locale[] {
			new Locale("en", "HK"),
			new Locale("zh", "HK"),
			new Locale("id", "ID"),
			new Locale("en", "IN"),
			new Locale("ja", "JP"),
			new Locale("ko", "KR"),
			new Locale("en", "MY"),
			new Locale("ms", "MY"),
			new Locale("en", "PH"),
			new Locale("en", "SG"),
			new Locale("th", "TH"),
			new Locale("en", "TW"),
			new Locale("zh", "TW"),
			new Locale("vi", "VN"),
			new Locale("tl", "PH"),
			new Locale("zh", "CN")
	};

	public static Locale[] WESTERN_LOCALES = new Locale[] {
			new Locale("de", "AT"),
			new Locale("en", "AU"),
			new Locale("fr", "BE"),
			new Locale("nl", "BE"),
			new Locale("de", "DE"),
			new Locale("da", "DK"),
			new Locale("es", "ES"),
			new Locale("fr", "FR"),
			new Locale("en", "IE"),
			new Locale("it", "IT"),
			new Locale("nl", "NL"),
			new Locale("nb", "NO"),
			new Locale("en", "NZ"),
			new Locale("sv", "SE"),
			new Locale("en", "UK")
	};
	
	public static Locale[] FLIGHTS_LOCALES = new Locale[] {
			AMERICAN_LOCALES[2],
			AMERICAN_LOCALES[3],
			AMERICAN_LOCALES[5]
	};

	public static final Map<Locale, Integer> LOCALE_TO_COUNTRY = new HashMap<Locale, Integer>();
	static {
		LOCALE_TO_COUNTRY.put(AMERICAN_LOCALES[0], R.string.country_ar);
		LOCALE_TO_COUNTRY.put(AMERICAN_LOCALES[1], R.string.country_br);
		LOCALE_TO_COUNTRY.put(AMERICAN_LOCALES[2], R.string.country_ca);
		LOCALE_TO_COUNTRY.put(AMERICAN_LOCALES[3], R.string.country_ca);
		LOCALE_TO_COUNTRY.put(AMERICAN_LOCALES[4], R.string.country_mx);
		LOCALE_TO_COUNTRY.put(AMERICAN_LOCALES[5], R.string.country_us);

		LOCALE_TO_COUNTRY.put(APAC_LOCALES[0], R.string.country_hk);
		LOCALE_TO_COUNTRY.put(APAC_LOCALES[1], R.string.country_hk);
		LOCALE_TO_COUNTRY.put(APAC_LOCALES[2], R.string.country_id);
		LOCALE_TO_COUNTRY.put(APAC_LOCALES[3], R.string.country_in);
		LOCALE_TO_COUNTRY.put(APAC_LOCALES[4], R.string.country_jp);
		LOCALE_TO_COUNTRY.put(APAC_LOCALES[5], R.string.country_kr);
		LOCALE_TO_COUNTRY.put(APAC_LOCALES[6], R.string.country_my);
		LOCALE_TO_COUNTRY.put(APAC_LOCALES[7], R.string.country_my);
		LOCALE_TO_COUNTRY.put(APAC_LOCALES[8], R.string.country_ph);
		LOCALE_TO_COUNTRY.put(APAC_LOCALES[9], R.string.country_sg);
		LOCALE_TO_COUNTRY.put(APAC_LOCALES[10], R.string.country_th);
		LOCALE_TO_COUNTRY.put(APAC_LOCALES[11], R.string.country_tw);
		LOCALE_TO_COUNTRY.put(APAC_LOCALES[12], R.string.country_tw);
		LOCALE_TO_COUNTRY.put(APAC_LOCALES[13], R.string.country_vn);
		LOCALE_TO_COUNTRY.put(APAC_LOCALES[14], R.string.country_ph);
		LOCALE_TO_COUNTRY.put(APAC_LOCALES[15], R.string.country_tw);

		LOCALE_TO_COUNTRY.put(WESTERN_LOCALES[0], R.string.country_at);
		LOCALE_TO_COUNTRY.put(WESTERN_LOCALES[1], R.string.country_au);
		LOCALE_TO_COUNTRY.put(WESTERN_LOCALES[2], R.string.country_be);
		LOCALE_TO_COUNTRY.put(WESTERN_LOCALES[3], R.string.country_be);
		LOCALE_TO_COUNTRY.put(WESTERN_LOCALES[4], R.string.country_de);
		LOCALE_TO_COUNTRY.put(WESTERN_LOCALES[5], R.string.country_dk);
		LOCALE_TO_COUNTRY.put(WESTERN_LOCALES[6], R.string.country_es);
		LOCALE_TO_COUNTRY.put(WESTERN_LOCALES[7], R.string.country_fr);
		LOCALE_TO_COUNTRY.put(WESTERN_LOCALES[8], R.string.country_ie);
		LOCALE_TO_COUNTRY.put(WESTERN_LOCALES[9], R.string.country_it);
		LOCALE_TO_COUNTRY.put(WESTERN_LOCALES[10], R.string.country_nl);
		LOCALE_TO_COUNTRY.put(WESTERN_LOCALES[11], R.string.country_no);
		LOCALE_TO_COUNTRY.put(WESTERN_LOCALES[12], R.string.country_nz);
		LOCALE_TO_COUNTRY.put(WESTERN_LOCALES[13], R.string.country_se);
		LOCALE_TO_COUNTRY.put(WESTERN_LOCALES[14], R.string.country_gb);
	}

	private static final String TAG = "com.expedia.bookings.test";
	private boolean mAllowScreenshots;
	private boolean mAllowOrientationChange;
	private int mScreenShotCount;
	private Solo mSolo;
	private Resources mRes;
	private HotelsUserData mUser; //user info container
	private ScreenshotUtils mScreen;
	private int mScreenWidth;
	private int mScreenHeight;
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
		mScreen = new ScreenshotUtils("Robotium-Screenshots", mSolo);
		mScreenWidth = mRes.getDisplayMetrics().widthPixels;
		mScreenWidth = mRes.getDisplayMetrics().heightPixels;
		
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
	public void launchHotels() {
		mSolo.clickOnText(mRes.getString(R.string.launch_hotel_secondary_text));
	}
	
	public void launchFlights() {
		mSolo.clickOnText(mRes.getString(R.string.launch_flight_secondary_text));
	}

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

	////////////////////////////////////////////////////////////////
	// Changing Settings

	public void changePOS(Locale locale) throws Exception {
		enterLog(TAG, "Changing POS");
		String settingsString = mRes.getString(R.string.Settings);
		//Sometimes the text displayed hasn't matched the string
		//for the update locale
		//This try catch block is a messy fix to be able to do more than one booking
		try {
			mSolo.pressMenuItem(0);
		}
		catch (AssertionFailedError E) {
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
		delay(1);
		mSolo.clickOnButton(1);
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
		delay(2);
		mSolo.clickOnButton(1);
		delay(1);
		mSolo.clickOnButton(0);
		setSpoofBookings();
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
			if (!mSolo.isCheckBoxChecked(2)) {
				mSolo.clickOnCheckBox(2);
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
		enterLog(TAG, "After clicking EDIT TEXT");
		landscape();
		delay(1);
		portrait();
		delay(1);
		mSolo.clearEditText(0);
		enterLog(TAG, "After clearing EDIT TEXT");
		delay(1);
		enterLog(TAG, "BEFORE TYPING TEXT");
		mSolo.typeText(0, location);
		enterLog(TAG, "AFTER TYPING TEXT");
		delay(3);
		enterLog(TAG, "Before clicking search button");
		mSolo.clickInList(2);//Selecting search suggestion results
								//some countries' list don't populate ever
								//might break stuff
		enterLog(TAG, "After clicking search button");

		mSolo.waitForActivity("ExpediaBookingApp"); // Add another wait if this causes instability
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
		if (mRes.getConfiguration().locale != APAC_LOCALES[4]
				&& mRes.getConfiguration().locale != APAC_LOCALES[5]
				&& mRes.getConfiguration().locale != APAC_LOCALES[1]
				&& mRes.getConfiguration().locale != APAC_LOCALES[12]
				&& mRes.getConfiguration().locale != APAC_LOCALES[15]) {
			enterLog(TAG, "Clicking on label: " + filter);
			mSolo.clickOnButton(1);
			landscape();
			portrait();
			delay(5);
			mSolo.enterText(0, filterText);
			delay(1);
			screenshot("Filtering for " + filterText);
			delay(1);
			mSolo.goBack();
		}
	}

	public void pressSort() {
		delay(1);
		String sortText = mRes.getString(R.string.SORT);
		enterLog(TAG, "Clicking on label: " + sortText);

		mSolo.clickOnButton(0);
		landscape();
		portrait();
		if (mAllowOrientationChange)
			mSolo.clickOnButton(0);
		delay(1);
		mSolo.clickOnText(mRes.getString(R.string.sort_description_popular));
		screenshot("Sort fragment");
		delay(1);

		/*Doesn't work when searching for city
		mSolo.clickOnText(sortText);
		delay(solo, 1);
		mSolo.clickOnText(getStringFromR(R.string.sort_description_distance));
		screenshot(mRes, solo, "Sort by Distance Results");
		delay(solo, 1);
		 */

		mSolo.clickOnButton(0);
		delay(1);
		mSolo.clickOnText(mRes.getString(R.string.sort_description_price));
		delay(1);

		mSolo.clickOnButton(0);
		delay(1);
		mSolo.clickOnText(mRes.getString(R.string.sort_description_rating));
		delay(1);

		try {
			mSolo.clickOnButton(0);
			delay(1);
			mSolo.clickOnText(mRes.getString(R.string.sort_description_deals));
			delay(1);
		}
		catch (Exception e) {
			mSolo.goBack();
		}

	}

	public void pressCalendar() {
		mSolo.clickOnImageButton(1);
		delay(1);
		landscape();
		portrait();
		screenshot("Calendar");
		mSolo.goBack();
	}

	public void pressGuestPicker() {
		mSolo.clickOnImageButton(0);
		delay(2);
		landscape();
		portrait();
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
		delay();
		mSolo.scrollToTop();
		mSolo.clickOnView(mSolo.getView(R.id.user_rating_text_view));
		mSolo.waitForDialogToClose(10000);
		screenshot("All reviews");
		delay(1);
		landscape();
		portrait();
		delay();
		mSolo.clickOnText(mRes.getString(R.string.user_review_sort_button_favorable));
		delay(1);
		screenshot("Favorable Reviews.");
		delay(1);
		mSolo.clickOnText(mRes.getString(R.string.user_review_sort_button_critical));
		screenshot("Critical Reviews.");
		delay(1);
		landscape();
		portrait();
		delay();
		mSolo.goBack();
	}

	public void pressBookRoom() {
		String bookNowString = mRes.getString(R.string.SELECT);

		landscape();
		portrait();

		enterLog(TAG, "Pressing Book Room Button: " + bookNowString);
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
		mSolo.scrollToBottom();
		delay(1);

		screenshot("Bottom of Booking Screen");
		mSolo.scrollToTop();
	}

	public void logIn() {
		mSolo.scrollToTop();
		mSolo.scrollDown();

		String loginButtonText = mRes.getString(R.string.login_btn_text);

		if (loginButtonText == null) {
			//The US String is different from all other POS
			//So we must check it make sure we're looking for the right one.
			loginButtonText = mRes.getString(R.string.log_in_to_expedia);
		}
		enterLog(TAG, "Pressing button: " + loginButtonText);
		try {
			mSolo.clickOnText(loginButtonText);
		}
		catch (Error e) {
			delay(5);
			enterLog(TAG, "Scrolling to top to press button again.");
			mSolo.scrollToTop();
			mSolo.clickOnText(mRes.getString(R.string.log_in_for_faster_booking));
		}
		delay(1);
		screenshot("Login Screen Pre Text Entry");
		mSolo.typeText(1, mUser.mLoginEmail); // index 1?

		delay();
		mSolo.typeText((EditText) mSolo.getView(R.id.password_edit_text), mUser.mLoginPassword);
		landscape();
		portrait();
		delay(5);
		try {
			mSolo.clickOnButton(1); //Log in button.
		}
		catch (Error e) {
			delay(5);
			mSolo.clickOnButton(1);
		}
		delay(5);
		mSolo.scrollToTop();
		delay();
		screenshot("Booking Info Post-Login");

	}

	public void enterPhoneNumber() throws Exception {
		mSolo.enterText((EditText) mSolo.getView(R.id.telephone_edit_text), mUser.mPhoneNumber);
	}

	public void enterCCV() throws Exception {
		//mSolo.scrollUp();
		delay(3);
		screenshot("CVV Screen");
		landscape();
		portrait();
		delay(5);
		final EditText CCVview =
				(EditText) mSolo.getCurrentActivity().findViewById(R.id.security_code_edit_text);
		mSolo.clickOnScreen(100, 500); //how to generalize this for all screen sizes?
		delay(1);
		mSolo.clickOnScreen(100, 500);
		delay(1);
		mSolo.clickOnScreen(100, 500);
		delay(1);
		mSolo.clickOnScreen(100, 500);
		delay(1);
		mSolo.clickOnScreen(325, 725);
		enterLog(TAG, "Press Book");
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
		String travelerInfo = mSolo.getString(R.string.enter_traveler_info);
		if (mSolo.searchText(travelerInfo, true)) {
			mSolo.clickOnText(travelerInfo);
			delay(5);
			if (mSolo.searchText("JexperCC", true)) {
				mSolo.clickOnText("JexperCC");
			}
			else {
				//At some point, switch this back to entering
				//new traveler and using that
				//Must wait until this functionality is restored
				delay();
				screenshot("Picking traveler");
				mSolo.clickOnText(mRes.getString(R.string.enter_traveler_info));
				delay(5);
				screenshot("Adding new traveler");
				mSolo.enterText(0, mUser.mFirstName);
				mSolo.enterText(2, mUser.mLastName);
				mSolo.enterText(3, mUser.mPhoneNumber);
				mSolo.clickOnScreen(450, 75);//generalize this
				mSolo.clickOnButton(1);

				delay();
			}
		}
		try {
			mSolo.clickOnText(mSolo.getString(R.string.payment_method));
			delay();
			screenshot("Payment Method");
			landscape();
			portrait();
			delay();
			mSolo.clickOnText(mSolo.getString(R.string.add_new_card));
			delay(1);
			screenshot("Add new card");
			delay(1);
			landscape();
			portrait();
			delay();
			mSolo.clearEditText(0);
			mSolo.enterText(0, mUser.mAddressLine1);

			mSolo.clearEditText(2);
			mSolo.enterText(2, mUser.mCityName);

			mSolo.clearEditText(3);
			mSolo.enterText(3, mUser.mStateCode);

			mSolo.clearEditText(4);
			mSolo.enterText(4, mUser.mZIPCode);
			delay();

			mSolo.clickOnScreen(450, 75);
			delay(1);
			landscape();
			portrait();
			delay(5);
			screenshot("Credit card info.");
			delay(1);
			mSolo.clearEditText(0);
			mSolo.enterText(0, mUser.mCreditCardNumber);
			mSolo.clearEditText(1);
			mSolo.enterText(1, mUser.mFirstName + " " + mUser.mLastName);

			mSolo.clickOnText(mRes.getString(R.string.expiration_date));
			landscape();
			portrait();
			delay(5);
			mSolo.clickOnButton(1);
			delay(2);
			mSolo.clickOnScreen(450, 75);
			delay(1);
			mSolo.clickOnButton(1);
			delay();
			mSolo.scrollUp();

			mSolo.scrollToTop();
			mSolo.clickOnScreen(450, 75);
		}
		catch (Error e) {
			mSolo.scrollToTop();
			mSolo.clickOnScreen(450, 75);
		}

		//pressCheckBox(); //Check box for terms & conditions occasionally needed.
	}

	public void confirmAndBook() throws Exception {

		delay(5);
		screenshot("Slide to checkout.");
		delay();
		mSolo.drag(100, 400, 650, 650, 10);

		delay(5);
		enterCCV();
		//If can't complete the suppressed booking, go back to the launcher.
		if (mSolo.searchText("Sorry, we don't seem to be able", true)) {
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
			if (mSolo.searchText("Hotels in", true)) {
				mSolo.scrollToTop();
				screenshot("Confirmation Screen 1");
				mSolo.scrollDown();
				screenshot("Confirmation Screen 2");
				mSolo.clickOnText("Hotels in");
			}
			else {
				try {
					mSolo.clickOnText(mRes.getString(R.string.NEW_SEARCH));
				}
				catch (AssertionFailedError E) {
					enterLog(TAG, "New Search string not localized: No " + mRes.getString(R.string.new_search));
					mSolo.clickOnText("NEW SEARCH");
				}
				enterLog(TAG, "Back at search!");
				delay(5);
				mSolo.goBack();
			}
		}
	}

	public void logInAndBook() throws Exception {
		bookingScreenShots();

		logIn();
		delay(1);
		enterMissingInfo();

		confirmAndBook();

	}

	//browseRooms goes into approximately numberOfHotels hotels, looks at hotels details
	//if completeABooking is true, it will complete a booking on one of the rooms.
	public void browseRooms(int numberOfHotels, String location, boolean completeABooking) throws Exception {
		enterLog(TAG, "Pre select location");
		selectLocation(location);

		for (int i = 0; i < numberOfHotels / 4; i++) {
			for (int j = 0; j < 4; j++) {
				selectHotel(j);
				checkReviews();
				delay(3);
				mSolo.goBack();
				delay(3);
			}
			mSolo.scrollDown();
		}
		if (completeABooking) {
			selectHotel(0);
			pressBookRoom();
			selectRoom(0);
			logInAndBook();
		}
	}

	////////////////////////////////////////////////////////////////
	// Info Screen Methods

	public void captureInfoScreen() {
		delay();
		mSolo.pressMenuItem(2);
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

	public void flightsHappyPath(String departure, String arrival, boolean doHotelBooking) throws Exception {
		
		clearPrivateData();
		launchFlights();

		//If still on flights confirmation page
		//click to do a new search
		if(mSolo.searchText("Hotels in", true)){
			clickTopRightBtn();
			delay(10);	
		}
		screenshot("Flights Search Screen");
		mSolo.clickOnEditText(0);
		mSolo.enterText(0, departure);
		delay();
		mSolo.clickInList(0);
		delay();

		//Arrival Field
		mSolo.clickOnEditText(1);
		mSolo.enterText(1, arrival);
		delay();
		mSolo.clickInList(0);
		delay();

		//Select Departure
		try{
			mSolo.clickOnText(mRes.getString(R.string.hint_select_departure));
		}catch(Error e){
			
		}
		delay();
		screenshot("Calendar");
		delay();
		CalendarTouchUtils.selectDay(mSolo, 5, R.id.calendar_date_picker);
		delay();
		//Click to search
		mSolo.clickOnView(mSolo.getView(R.id.search_button));
		delay(5);
		screenshot("Loading Flights");
		mSolo.waitForDialogToClose(10000);
		
		//Scroll up and down and
		
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
		mSolo.clickOnText(mRes.getString(R.string.sort_flights));
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
		clickTopRightBtn();
		mSolo.waitForDialogToClose(10000);
		clickTopRightBtn();

		//log in and do a booking
		logInAndBook();
		
		//if hotel booking switch is true, do a hotel booking 
		//in that city
		if(doHotelBooking) {
			launchHotels();
			browseRooms(4, arrival, true);
		}
		

	}
}
