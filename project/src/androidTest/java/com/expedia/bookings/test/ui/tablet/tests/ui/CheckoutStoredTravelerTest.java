package com.expedia.bookings.test.ui.tablet.tests.ui;

import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.tablet.pagemodels.Checkout;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;
import com.expedia.bookings.test.ui.tablet.pagemodels.HotelDetails;
import com.expedia.bookings.test.ui.tablet.pagemodels.Launch;
import com.expedia.bookings.test.ui.tablet.pagemodels.LogIn;
import com.expedia.bookings.test.ui.tablet.pagemodels.Results;
import com.expedia.bookings.test.ui.tablet.pagemodels.Search;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.HotelsUserData;
import com.expedia.bookings.test.espresso.TabletTestCase;

public class CheckoutStoredTravelerTest extends TabletTestCase {

	HotelsUserData mUser;
	private String travelerFirstName = "Expedia";
	private String travelerMiddleName = "Automation";
	private String travelerLastName = "First";
	private String travelerFullName = "Expedia Automation First";
	private String travelerPhone = "1-234-567-890";
	private String travelerDOB = "Born Jan 27, 1991";
	private String travelerSex = "Male";

	private void doSearch() {
		mUser = new HotelsUserData();
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("Detroit, MI");
		Launch.clickSuggestion("Detroit, MI");
		Search.clickOriginButton();
		Search.typeInOriginEditText("San Francisco, CA");
		Search.clickSuggestion("San Francisco, CA");
		Search.clickSelectFlightDates();
		int randomOffset = 20 + (int) (Math.random() * 100);
		LocalDate startDate = LocalDate.now().plusDays(randomOffset);
		Search.clickDate(startDate, null);
		Search.clickSearchPopupDone();
	}

	private void addHotelFromResults() {
		Results.swipeUpHotelList();
		Results.clickHotelAtIndex(1);
		HotelDetails.clickAddHotel();
	}

	private void addFlightFromResults() {
		Results.swipeUpFlightList();
		Results.clickFlightAtIndex(1);
		Results.clickAddFlight();
	}

	private void validateTravelerForFlight() {
		validateTravelerForHotel();
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.edit_birth_date_text_btn, travelerDOB);
		EspressoUtils.assertTextWithChildrenIsDisplayed(R.id.edit_gender_spinner, travelerSex);
	}

	private void validateTravelerForHotel() {
		Checkout.clickOnTravelerDetails();
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.edit_first_name, travelerFirstName);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.edit_middle_name, travelerMiddleName);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.edit_last_name, travelerLastName);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.edit_phone_number, travelerPhone);
	}

	private void doLogin() throws Throwable {
		Checkout.clickLoginButton();
		LogIn.enterUserName(mUser.email);
		LogIn.enterPassword(mUser.password);
		screenshot("Login_Info_Entered");
		LogIn.clickLoginExpediaButton();
		EspressoUtils.assertViewWithTextIsDisplayed(mUser.email);
		screenshot("Login_Successful");
	}

	private void addFlightHotelGoToCheckoutAndLogin() throws Throwable {
		// Test setup
		doSearch();

		addFlightFromResults();
		addHotelFromResults();
		screenshot("Both_Flight_Hotel_Bucket");

		Results.clickBookFlight();
		screenshot("Choose_Book_Flight");
		Results.clickBookHotel();
		screenshot("Choose_Book_Hotel");

		doLogin();
	}

	private void addFlightGoToCheckoutAndLogin() throws Throwable {
		// Test setup
		doSearch();

		addFlightFromResults();
		screenshot("Flight_In_Bucket");

		Results.clickBookFlight();
		screenshot("Choose_Book_Flight");

		doLogin();
	}

	private void addHotelGoToCheckoutAndLogin() throws Throwable {
		// Test setup
		doSearch();

		addHotelFromResults();
		screenshot("Hotel_In_Bucket");

		Results.clickBookHotel();
		screenshot("Choose_Book_Hotel");

		doLogin();
	}

	private void enterNewTravelerCommonFields() {
		Checkout.enterFirstName("Mobiata");
		Checkout.enterLastName("Auto");
		Checkout.enterPhoneNumber("1112223333");
	}

	private void enterNewTravelerDetailsHotel() throws Throwable {
		enterNewTravelerCommonFields();
		Common.closeSoftKeyboard(Checkout.phoneNumber());
		screenshot("Hotel_Checkout_Traveler_Details_Entered");
		Checkout.clickOnDone();

		EspressoUtils.assertViewWithTextIsDisplayed(R.id.display_full_name, "Mobiata Auto");
		screenshot("Checkout_New_Traveler_Entered");
	}

	private void enterNewTravelerDetailsFlight() throws Throwable {
		enterNewTravelerCommonFields();
		Common.closeSoftKeyboard(Checkout.phoneNumber());
		Checkout.enterDateOfBirth(1970, 1, 1);
		screenshot("CheckoutForm_Traveler_Entered");
		Checkout.clickOnDone();

		//error popup
		EspressoUtils.assertViewWithTextIsDisplayed(String.format(mRes.getString(R.string.save_traveler_message_TEMPLATE), "Mobiata Auto"));
		Checkout.clickNegativeButton();

		EspressoUtils.assertViewWithTextIsDisplayed(R.id.display_full_name, "Mobiata Auto");
		screenshot("Checkout_New_Traveler_Entered");
	}

	public void selectStoredTravelerFromList(String travelerName) throws Throwable {
		Checkout.clickOnStoredTravelerSpinnerButton();
		Checkout.selectStoredTraveler(getInstrumentation(), travelerName);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.display_full_name, travelerName);
		screenshot("CheckoutOverview_Stored_Traveler_Selected");
	}

	public void selectAddNewTravelerFromList() throws Throwable {
		Checkout.clickOnStoredTravelerSpinnerButton();
		Checkout.selectStoredTraveler(getInstrumentation(), "Add New Traveler");
	}

	public void testFlightLoginStoredTravelerOperations() throws Throwable {
		addFlightGoToCheckoutAndLogin();
		selectStoredTravelerFromList(travelerFullName);
		validateTravelerForFlight();
		screenshot("Stored_Traveler_Detail_Present");
		Common.pressBack();
	}

	public void testHotelLoginStoredTravelerOperations() throws Throwable {
		addHotelGoToCheckoutAndLogin();
		selectStoredTravelerFromList(travelerFullName);
		validateTravelerForHotel();
		screenshot("Stored_Traveler_Detail_Present");
		Common.pressBack();
	}

	public void testFlightHotelLoginStoredTravelerOperations() throws Throwable {
		addFlightHotelGoToCheckoutAndLogin();
		selectStoredTravelerFromList(travelerFullName);
		validateTravelerForHotel();
		Common.pressBack();

		Results.clickBookFlight();
		selectStoredTravelerFromList("Mock Web Server");
		selectStoredTravelerFromList(travelerFullName);
		validateTravelerForFlight();
		screenshot("Stored_Traveler_Detail_Present");
		Common.pressBack();
	}

	public void testAddNewTravelerHotelCheckout() throws Throwable {
		addHotelGoToCheckoutAndLogin();

		selectAddNewTravelerFromList();
		enterNewTravelerDetailsHotel();
	}

	public void testAddNewTravelerFlightCheckout() throws Throwable {
		addFlightGoToCheckoutAndLogin();

		selectAddNewTravelerFromList();
		enterNewTravelerDetailsFlight();
	}
}
