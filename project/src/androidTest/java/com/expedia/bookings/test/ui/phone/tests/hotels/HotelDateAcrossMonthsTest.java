package com.expedia.bookings.test.ui.phone.tests.hotels;

import java.util.Calendar;

import org.joda.time.LocalDate;

import android.text.format.DateUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.phone.pagemodels.common.BillingAddressScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CVVEntryScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CardInfoScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CommonPaymentMethodScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LogInScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelReceiptModel;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsCheckoutScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsConfirmationScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsDetailsScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsRoomsRatesScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsSearchScreen;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.HotelsUserData;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import com.expedia.bookings.utils.JodaUtils;

import android.support.test.espresso.ViewInteraction;

/*
card link : https://expedia.mingle.thoughtworks.com/projects/eb_tp/cards/184?
card description :Description
When I select the dates 12/31-1/2, or dates that start in one month and end in the next and perform a search And I select any hotel from the results list And I select any room type from Rooms & Rates
Then the Check In and Check Out dates match what was selected on the calendar on Booking Overview/Booking Information screen and on the Confirmation screen
Then The length of stay matches the number of nights I selected on the calendar.
Then A room rate will appear for each night selected on the calendar if the nightly breakdown is displayed.
 */


public class HotelDateAcrossMonthsTest extends PhoneTestCase {

	private LocalDate mStartDate, mEndDate;
	HotelsUserData mUser;

	public void testStayDaysOnCheckoutPage() throws Exception {
		initialSearch();
		selectFirstHotel();
		selectFirstType();
		//test
		ViewInteraction nightView = HotelReceiptModel.nightsTextView();
		EspressoUtils.assertContains(nightView, "2 Nights");
	}

	public void testStayDaysOnCostSummaryPage() throws Exception {
		initialSearch();
		selectFirstHotel();
		selectFirstType();

		HotelReceiptModel.clickGrandTotalTextView();
		EspressoUtils.assertViewWithTextIsDisplayed("2 Nights");

		//test
		try {
			EspressoUtils.assertViewWithTextIsDisplayed(getFromattedDate(0));
		}
		catch (Exception e) {
			EspressoUtils.assertViewWithTextIsDisplayed(getFromattedDateWithYear(0));
		}

		try {
			EspressoUtils.assertViewWithTextIsDisplayed(getFromattedDate(1));
		}
		catch (Exception e) {
			EspressoUtils.assertViewWithTextIsDisplayed(getFromattedDateWithYear(1));
		}
	}

	public void testHotelStayDaysOnConfirmationScreen() throws Exception {

		initialSearch();
		selectFirstHotel();
		selectFirstType();
		bookHotel();

		//create the expected string
		CharSequence from = JodaUtils.formatLocalDate(getActivity(), mStartDate, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH);
		CharSequence to = JodaUtils.formatLocalDate(getActivity(), mEndDate, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH);
		String expectedStayString = getInstrumentation().getTargetContext().getResources().getString(R.string.date_range_TEMPLATE, from, to);

		//test
		EspressoUtils.assertContains(HotelsConfirmationScreen.summaryTextView(), expectedStayString);
	}

	private void bookHotel() throws Exception {
		HotelsCheckoutScreen.clickCheckoutButton();
		HotelsCheckoutScreen.clickLogInButton();
		LogInScreen.typeTextEmailEditText(mUser.email);
		LogInScreen.typeTextPasswordEditText(mUser.password);
		LogInScreen.clickOnLoginButton();
		HotelsCheckoutScreen.clickSelectPaymentButton();
		try {
			CommonPaymentMethodScreen.clickOnAddNewCardTextView();
		}
		catch (Exception e) {
			//ignore
		}
		CardInfoScreen.typeTextCreditCardEditText(mUser.creditCardNumber);
		BillingAddressScreen.typeTextPostalCode(mUser.zipcode);
		CardInfoScreen.typeTextNameOnCardEditText(mUser.firstName + " " + mUser.lastName);
		CardInfoScreen.clickOnExpirationDateButton();
		CardInfoScreen.clickMonthUpButton();
		CardInfoScreen.clickYearUpButton();
		CardInfoScreen.clickSetButton();
		CardInfoScreen.clickOnDoneButton();
		CardInfoScreen.clickNoThanksButton();
		HotelsCheckoutScreen.slideToCheckout();
		CVVEntryScreen.parseAndEnterCVV(mUser.cvv);
		CVVEntryScreen.clickBookButton();
	}

	/*
	When I select the dates 12/31-1/2, or dates that start in one month and end in the next and perform a search

	 */
	private void initialSearch() throws Exception {
		mUser = new HotelsUserData();
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.enterSearchText("New York, NY");
		HotelsSearchScreen.clickSuggestionWithName(getActivity(), "New York, NY");
		//getting the last day of the current month as Start Date
		Calendar cal = Calendar.getInstance();
		int year = cal.get(cal.YEAR);
		int currentMonth = cal.get(cal.MONTH) + 1;
		int lastDayOfTheCurrentMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		mStartDate = new LocalDate(year, currentMonth, lastDayOfTheCurrentMonth);
		//next month with 2 days in advance
		mEndDate = mStartDate.plusDays(2);
		//Search with last day of current month to the next 2 days of next month.
		HotelsSearchScreen.clickOnCalendarButton();
		HotelsSearchScreen.clickDate(mStartDate, mEndDate);
		HotelsSearchScreen.clickOnGuestsButton();
		HotelsSearchScreen.guestPicker().clickOnSearchButton();
	}

	// And I select the first hotel from the results list
	private void selectFirstHotel() throws Exception {
		try {
			HotelsSearchScreen.clickHotelWithName("happypath");
		}
		catch (Exception e) {
			HotelsSearchScreen.clickListItem(1);
		}
		HotelsDetailsScreen.clickSelectButton();
	}

	//And I select the first type from Rooms & Rates
	private void selectFirstType() throws Exception {
		HotelsRoomsRatesScreen.selectRoomItem(1);
	}

	private String getFromattedDate(int daysInAdvance) {
		return mStartDate.plusDays(daysInAdvance).toString("M/d");
	}

	private String getFromattedDateWithYear(int daysInAdvance) {
		return mStartDate.plusDays(daysInAdvance).toString("M/d/y");
	}
}
