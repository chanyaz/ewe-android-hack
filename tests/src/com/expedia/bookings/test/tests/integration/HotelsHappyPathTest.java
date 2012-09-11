package com.expedia.bookings.test.tests.integration;

import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.widget.EditText;
import android.widget.ListView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchActivity;
import com.jayway.android.robotium.solo.Solo;
import com.mobiata.android.widget.CalendarDatePicker;
import com.mobiata.testutils.CalendarTouchUtils;
import com.mobiata.testutils.RobotiumWorkflowUtils;

import java.lang.reflect.Field;

public class HotelsHappyPathTest extends ActivityInstrumentationTestCase2<SearchActivity> {

	private static final String TAG = "SearchTest";
	private Solo mSolo;
	private String environment;
	private boolean testRanThroughCompletion;

	private Field[] mExpediaBookingsRFields;

	public HotelsHappyPathTest() {
		super("com.expedia.bookings", SearchActivity.class);
	}

	protected void setUp() throws Exception {
		mExpediaBookingsRFields = R.id.class.getFields();
		mSolo = new Solo(getInstrumentation(), getActivity());
		testRanThroughCompletion = false;
		Log.d(TAG, "#### BEGIN TEST ####");
		//Db.clear() doesn't seem to do anything
		//Log.d(TAG, "clearing history with Db.clear()");
		//com.expedia.bookings.data.Db.clear();
	}

	private void setEnvironment(String specifiedEnvironment) throws Exception {
		Log.d(TAG, "setEnvironment() setting environment to: " + specifiedEnvironment);
		environment = specifiedEnvironment;
		mSolo.clickOnMenuItem("Settings");
		mSolo.clickOnText("Select API");
		mSolo.clickOnText(specifiedEnvironment);
		if (specifiedEnvironment.equals("Proxy")) {
			mSolo.clickOnText("Address");
			mSolo.clearEditText(0);
			mSolo.typeText(0, "10.0.2.2:3000");
			mSolo.clickOnText("OK");
		}
		mSolo.goBack();
	}

	private String getHotelName() {
		return RobotiumWorkflowUtils.getTextViewValue(mSolo, mExpediaBookingsRFields, R.id.name_text_view);
	}

	private String getHotelFirstRoomPrice() {
		return RobotiumWorkflowUtils.getTextViewValue(mSolo, mExpediaBookingsRFields, R.id.price_text_view);
	}

	private void clickInCityList(int line) {
		Log.d(TAG, "clickInCityList: " + line);
		//		ListView lv = (ListView) mSolo.getView(R.id.search_suggestions_list_view);
		//		mSolo.clickOnView(lv.getChildAt(line));
		//mSolo.sleep(5000);
	}

	private void selectCity() throws Exception {
		Log.d(TAG, "starting selectCity()");
		//typeText seems to be needed by Gingerbread devices
		mSolo.typeText((EditText) mSolo.getView(R.id.search_edit_text), "San Diego");
		//		RobotiumWorkflowUtils.waitForListViewToPopulate(mSolo, R.id.search_suggestions_list_view);
		//mSolo.typeText((EditText) mSolo.getView(R.id.search_edit_text), "Miami");
		clickInCityList(0);
	}

	private void setCalendar() throws Exception {
		Log.d(TAG, "setCalendar()");
		mSolo.clickOnView(mSolo.getView(R.id.dates_button));
		CalendarDatePicker cal = (CalendarDatePicker) mSolo.getView(R.id.dates_date_picker);
		assertNotNull(cal);
		for (int i = 0; i < 3; i++) {
			CalendarTouchUtils.clickNextMonth(mSolo, cal);
		}
		mSolo.clickOnButton(0);
	}

	private void selectHotel() {
		Log.d(TAG, "selectHotel()");
		getHotelFirstRoomPrice();
		getHotelName();
		mSolo.clickInList(0);
	}

	private void selectRoom() {
		Log.d(TAG, "selectRoom()");
		//need some padding here for book now button to get itself together
		mSolo.sleep(5000);
		mSolo.clickOnView(mSolo.getView(R.id.book_now_button));
		getHotelFirstRoomPrice();
		getHotelName();
		mSolo.clickInList(0);
	}

	private void enterBookingInfo() throws Exception {
		Log.d(TAG, "enterBookingInfo()");

		//need some padding here; without it, initial views are null to getView()
		mSolo.sleep(2000);

		getHotelFirstRoomPrice();
		getHotelName();

		RobotiumWorkflowUtils.enterText(mSolo, R.id.first_name_edit_text, "Jimmy");
		RobotiumWorkflowUtils.enterText(mSolo, R.id.last_name_edit_text, "James");
		RobotiumWorkflowUtils.enterText(mSolo, R.id.telephone_edit_text, "4155551212");
		RobotiumWorkflowUtils.enterText(mSolo, R.id.email_edit_text, "numb@nuts.com");
		RobotiumWorkflowUtils.enterText(mSolo, R.id.card_number_edit_text, "4111111111111111");
		RobotiumWorkflowUtils.enterText(mSolo, R.id.expiration_month_edit_text, "11");
		RobotiumWorkflowUtils.enterText(mSolo, R.id.expiration_year_edit_text, "14");
		RobotiumWorkflowUtils.enterText(mSolo, R.id.address1_edit_text, "1 Rincon Hill");
		RobotiumWorkflowUtils.enterText(mSolo, R.id.address2_edit_text, "Apt 4709");
		RobotiumWorkflowUtils.enterText(mSolo, R.id.city_edit_text, "San Francisco");
		//RobotiumWorkflowUtils.enterText(mSolo, R.id.state_edit_text, "CA");
		RobotiumWorkflowUtils.enterText(mSolo, R.id.postal_code_edit_text, "94107");

		//will always need to enter security code, regardless of login state
		RobotiumWorkflowUtils.enterText(mSolo, R.id.security_code_edit_text, "123");

	}

	private void completeBooking() throws Exception {
		Log.d(TAG, "completeBooking()");

		getHotelFirstRoomPrice();
		getHotelName();

		mSolo.clickOnView(mSolo.getView(R.id.confirm_book_button));
		if (environment.equals("Production")) {
			mSolo.clickOnText("OK");
		}
		else {
			RobotiumWorkflowUtils.waitForElement(mSolo, R.id.start_new_search_button, 60);
		}
	}

	private void leaveConfirmationPage() {
		if (environment != "Production") {
			mSolo.clickOnView(mSolo.getView(R.id.start_new_search_button));
		}
		RobotiumWorkflowUtils.waitForElement(mSolo, R.id.search_edit_text, 60);

		//this is the final method of the test; set completion flag
		testRanThroughCompletion = true;
	}

	public void testBooking() throws Exception {

		//hotel name to assert at each page
		String mHotelName;

		//one-time setup + prep
		//setEnvironment("Production");
		setEnvironment("Integration");
		//setEnvironment("Proxy");
		RobotiumWorkflowUtils.clearMenu(mSolo);

		//city
		selectCity();
		setCalendar();
		RobotiumWorkflowUtils.waitForListViewToPopulate(mSolo, mExpediaBookingsRFields);

		//hotel room
		selectHotel();
		mHotelName = getHotelName();
		selectRoom();
		assertEquals(mHotelName, getHotelName());

		//book and check out
		enterBookingInfo();
		assertEquals(mHotelName, getHotelName());
		completeBooking();
		assertEquals(mHotelName, getHotelName());

		//return to first page 
		leaveConfirmationPage();

	}

	@Override
	protected void tearDown() throws Exception {
		Log.d(TAG, "in tearDown()");
		assertTrue(testRanThroughCompletion);
		Log.d(TAG, "sleeping");
		mSolo.sleep(5000);
		//Robotium will finish all the activities that have been opened
		mSolo.finishOpenedActivities();
	}

}