package com.expedia.bookings.test.ui.tablet.tests.hotels;

import java.util.Calendar;

import org.joda.time.LocalDate;

import android.text.format.DateUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.tablet.pagemodels.Checkout;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;
import com.expedia.bookings.test.ui.tablet.pagemodels.Confirmation;
import com.expedia.bookings.test.ui.tablet.pagemodels.HotelDetails;
import com.expedia.bookings.test.ui.tablet.pagemodels.Launch;
import com.expedia.bookings.test.ui.tablet.pagemodels.Results;
import com.expedia.bookings.test.ui.tablet.pagemodels.Search;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.TabletTestCase;
import com.expedia.bookings.utils.JodaUtils;

/**
 * Created by dmadan on 8/28/14.
 */
public class HotelBookingAcrossMonths extends TabletTestCase {

	private LocalDate mStartDate, mEndDate;
	private String mCalendarDuration;

	/*
	* Test: Hotel Booking dates matches on Trip Bucket, Overview Information screen,
	* Confirmation screen and on the Cost Summary
	*/

	public void testStayDaysOnTripBucket() {
		initialSearch();
		EspressoUtils.assertContains(Search.tripBucketDuration(), mCalendarDuration);
	}

	public void testStayDaysOnTripOverview() {
		initialSearch();
		Results.clickBookHotel();

		String from = JodaUtils.formatLocalDate(getActivity(), mStartDate, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH);
		String to = JodaUtils.formatLocalDate(getActivity(), mEndDate, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH);

		EspressoUtils.assertContains(Checkout.tripDateRange(), from);
		EspressoUtils.assertContains(Checkout.tripDateRange(), to);
	}

	public void testStayDaysOnTripConfirmation() {
		initialSearch();
		Results.clickBookHotel();
		Checkout.clickOnEmptyTravelerDetails();
		Common.closeSoftKeyboard(Checkout.firstName());
		Checkout.enterFirstName("Mobiata");
		Checkout.enterLastName("Auto");
		Checkout.enterPhoneNumber("1112223333");
		Checkout.enterEmailAddress("aaa@aaa.com");
		Common.closeSoftKeyboard(Checkout.emailAddress());
		Checkout.clickOnDone();

		Checkout.clickOnEnterPaymentInformation();
		Checkout.enterNameOnCard("Mobiata Auto");
		Checkout.enterCreditCardNumber("4111111111111111");
		Common.closeSoftKeyboard(Checkout.creditCardNumber());
		Checkout.setExpirationDate(2020, 12);
		Checkout.enterPostalCode("95104");
		Common.closeSoftKeyboard(Checkout.postalCode());
		Checkout.clickOnDone();

		Checkout.slideToPurchase();
		Checkout.enterCvv("111");
		Checkout.clickBookButton();
		EspressoUtils.assertContains(Confirmation.confirmationSummary(), mCalendarDuration);
	}

	public void testStayDaysOnCostSummaryPage() throws Exception {
		initialSearch();
		Results.clickBookHotel();

		Checkout.clickGrandTotalTextView();
		EspressoUtils.assertViewWithTextIsDisplayed("2 Nights");

		//test room rate will appear for each night on the calendar
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

	//helper methods
	private void initialSearch() {
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("Detroit, MI");
		Launch.clickSuggestionAtPosition(1);
		Calendar cal = Calendar.getInstance();
		int year = cal.get(cal.YEAR);
		int currentMonth = cal.get(cal.MONTH) + 1;
		int lastDayOfTheCurrentMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		mStartDate = new LocalDate(year, currentMonth, lastDayOfTheCurrentMonth);

		//next month with 2 days in advance
		mEndDate = mStartDate.plusDays(2);
		Search.clickDate(mStartDate, mEndDate);
		Search.clickSearchPopupDone();
		mCalendarDuration = EspressoUtils.getText(R.id.calendar_btn);
		Results.swipeUpHotelList();
		try {
			Results.clickHotelWithName("happypath");
		}
		catch (Exception e) {
			Results.clickHotelAtIndex(1);
		}
		HotelDetails.clickSelectHotelWithRoomDescription("happypath_2_night_stay_0");
		HotelDetails.clickAddHotel();
	}

	private String getFromattedDate(int daysInAdvance) {
		return mStartDate.plusDays(daysInAdvance).toString("M/d");
	}

	private String getFromattedDateWithYear(int daysInAdvance) {
		return mStartDate.plusDays(daysInAdvance).toString("M/d/y");
	}

}
