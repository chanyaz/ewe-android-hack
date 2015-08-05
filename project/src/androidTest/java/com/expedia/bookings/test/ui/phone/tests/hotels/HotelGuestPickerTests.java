package com.expedia.bookings.test.ui.phone.tests.hotels;

import org.joda.time.LocalDate;

import android.support.test.espresso.Espresso;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CVVEntryScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CardInfoScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CommonTravelerInformationScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsCheckoutScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsConfirmationScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsDetailsScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsGuestPicker;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsRoomsRatesScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsSearchScreen;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import com.expedia.bookings.utils.DateFormatUtils;

import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

/**
 * Created by dmadan on 5/22/14.
 */
public class HotelGuestPickerTests extends PhoneTestCase {

	// verify that the guest number picker's text views
	// show the expected text when children and adults
	// are incremented and decremented
	public void testPickerTextViews() {
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickOnGuestsButton();

		String lowerTextChildView = HotelsGuestPicker
			.getGuestTextViewValue(R.id.text_lower, R.id.children_number_picker);
		String lowerTextAdultView = HotelsGuestPicker.getGuestTextViewValue(R.id.text_lower, R.id.adults_number_picker);
		assertEquals(lowerTextChildView, "");
		assertEquals(lowerTextAdultView, "");

		int adultCount = 1;
		int childCount = 0;
		final int adultMax = 6;
		final int childMax = 4;

		String adultCurrentTextViewValue;
		String adultHigherTextViewValue;
		String adultLowerTextViewValue;

		for (int i = 1; i < adultMax; i++) {
			adultCurrentTextViewValue = HotelsGuestPicker
				.getGuestTextViewValue(R.id.text_current, R.id.adults_number_picker);
			assertEquals(HotelsGuestPicker.adultPickerStringPlural(adultCount, getActivity().getResources()),
				adultCurrentTextViewValue);

			adultHigherTextViewValue = HotelsGuestPicker
				.getGuestTextViewValue(R.id.text_higher, R.id.adults_number_picker);
			assertEquals(HotelsGuestPicker.adultPickerStringPlural(adultCount + 1, getActivity().getResources()),
				adultHigherTextViewValue);

			HotelsGuestPicker.incrementAdultsButton();
			adultCount++;

			adultLowerTextViewValue = HotelsGuestPicker
				.getGuestTextViewValue(R.id.text_lower, R.id.adults_number_picker);
			assertEquals(HotelsGuestPicker.adultPickerStringPlural(adultCount - 1, getActivity().getResources()),
				adultLowerTextViewValue);
		}

		for (int i = 6; i > 0; i--) {
			HotelsGuestPicker.decrementAdultsButton();
		}

		String childCurrentTextViewValue;
		String childHigherTextViewValue;
		String childLowerTextViewValue;

		for (int i = 0; i < childMax; i++) {
			childHigherTextViewValue = HotelsGuestPicker
				.getGuestTextViewValue(R.id.text_higher, R.id.children_number_picker);
			assertEquals(HotelsGuestPicker.childPickerStringPlural(childCount + 1, getActivity().getResources()),
				childHigherTextViewValue);

			childCurrentTextViewValue = HotelsGuestPicker
				.getGuestTextViewValue(R.id.text_current, R.id.children_number_picker);
			assertEquals(HotelsGuestPicker.childPickerStringPlural(childCount, getActivity().getResources()),
				childCurrentTextViewValue);

			HotelsGuestPicker.incrementChildrenButton();
			childCount++;

			childLowerTextViewValue = HotelsGuestPicker
				.getGuestTextViewValue(R.id.text_lower, R.id.children_number_picker);
			assertEquals(HotelsGuestPicker.childPickerStringPlural(childCount - 1, getActivity().getResources()),
				childLowerTextViewValue);
		}
	}

	public void testChildSelectorAppearing() {
		final int childMax = 4;
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickOnGuestsButton();
		assertFalse(
			withText(HotelsGuestPicker.selectChildAgePlural(1, getActivity().getResources())).matches(isDisplayed()));

		for (int i = 1; i <= childMax; i++) {
			HotelsGuestPicker.incrementChildrenButton();
			assertFalse(withText(HotelsGuestPicker.selectChildAgePlural(i, getActivity().getResources()))
				.matches(isDisplayed()));
		}
		Espresso.pressBack();
		Espresso.pressBack();
	}

	// Guest picker should allow total of 6 people
	public void testMaxGuestsWithAllAdults() {
		// All adults selected. Child inc/dec buttons should be disabled
		int adultCount = 6;
		int childCount = 0;
		String adultCurrentTextViewValue;
		String childCurrentTextViewValue;

		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickOnGuestsButton();
		for (int i = 1; i < adultCount; i++) {
			HotelsGuestPicker.incrementAdultsButton();
		}

		adultCurrentTextViewValue = HotelsGuestPicker
			.getGuestTextViewValue(R.id.text_current, HotelsGuestPicker.ADULT_PICKER_VIEW_ID);
		assertEquals(HotelsGuestPicker.adultPickerStringPlural(adultCount, getActivity().getResources()),
			adultCurrentTextViewValue);

		childCurrentTextViewValue = HotelsGuestPicker
			.getGuestTextViewValue(R.id.text_current, HotelsGuestPicker.CHILD_PICKER_VIEW_ID);
		assertEquals(HotelsGuestPicker.childPickerStringPlural(childCount, getActivity().getResources()),
			childCurrentTextViewValue);
		HotelsGuestPicker.guestsIndicatorTextMatches(String.valueOf(adultCount));

		HotelsGuestPicker.childPickerIncrementIsDisabled();
		HotelsGuestPicker.childPickerDecrementIsDisabled();

		HotelsGuestPicker.adultPickerIncrementIsDisabled();
	}

	public void testMaxGuestsWithAdultAndChildrenSelected() {
		int adultCount = 4;
		int childCount = 2;
		String adultCurrentTextViewValue;
		String childCurrentTextViewValue;

		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickOnGuestsButton();
		for (int i = 1; i < adultCount; i++) {
			HotelsGuestPicker.incrementAdultsButton();
		}
		for (int i = 0; i < childCount; i++) {
			HotelsGuestPicker.incrementChildrenButton();
		}
		adultCurrentTextViewValue = HotelsGuestPicker
			.getGuestTextViewValue(R.id.text_current, HotelsGuestPicker.ADULT_PICKER_VIEW_ID);
		assertEquals(HotelsGuestPicker.adultPickerStringPlural(adultCount, getActivity().getResources()),
			adultCurrentTextViewValue);

		childCurrentTextViewValue = HotelsGuestPicker
			.getGuestTextViewValue(R.id.text_current, HotelsGuestPicker.CHILD_PICKER_VIEW_ID);
		assertEquals(HotelsGuestPicker.childPickerStringPlural(childCount, getActivity().getResources()),
			childCurrentTextViewValue);

		HotelsGuestPicker.childPickerIncrementIsDisabled();
		HotelsGuestPicker.adultPickerIncrementIsDisabled();
	}

	public void testMinAdultGuests() {
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickOnGuestsButton();

		HotelsGuestPicker.guestsIndicatorTextMatches("1");

		HotelsGuestPicker.incrementAdultsButton();
		HotelsGuestPicker.guestsIndicatorTextMatches("2");

		HotelsGuestPicker.decrementAdultsButton();
		HotelsGuestPicker.adultPickerDecrementIsDisabled();
	}

	// Test guest picker is closed on click of search
	public void testGuestPickerClosesOnSearch() {
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickOnGuestsButton();

		HotelsGuestPicker.clickOnSearchButton();
		HotelsGuestPicker.guestLayout().check(matches(not(isDisplayed())));
	}

	public void testGuestPickerDisplayWithIncrementDecrement() {
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickOnGuestsButton();

		HotelsGuestPicker.guestsIndicatorTextMatches("1");

		HotelsGuestPicker.incrementAdultsButton();
		HotelsGuestPicker.guestsIndicatorTextMatches("2");

		HotelsGuestPicker.incrementAdultsButton();
		HotelsGuestPicker.guestsIndicatorTextMatches("3");

		HotelsGuestPicker.decrementAdultsButton();
		HotelsGuestPicker.guestsIndicatorTextMatches("2");

		HotelsGuestPicker.incrementChildrenButton();
		HotelsGuestPicker.guestsIndicatorTextMatches("3");

		HotelsGuestPicker.incrementChildrenButton();
		HotelsGuestPicker.guestsIndicatorTextMatches("4");

		HotelsGuestPicker.decrementChildrenButton();
		HotelsGuestPicker.guestsIndicatorTextMatches("3");
	}

	public void testMaxChildrenAllowed() {
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickOnGuestsButton();

		int maxChildrenAllowed = 4;
		for (int i = 0; i < maxChildrenAllowed; i++) {
			HotelsGuestPicker.incrementChildrenButton();
		}

		HotelsGuestPicker.guestsIndicatorTextMatches("5");
		HotelsGuestPicker.childPickerIncrementIsDisabled();
	}

	public void testGuestsSelectedAreReflectedOnOverviewAndCheckout() {
		int numberOfGuests = 1; //Default
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText("New York, NY");
		HotelsSearchScreen.clickSuggestionWithName(getActivity(), "New York, NY");
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);
		HotelsSearchScreen.clickOnCalendarButton();
		HotelsSearchScreen.clickDate(startDate, endDate);

		HotelsSearchScreen.clickOnGuestsButton();
		HotelsGuestPicker.incrementAdultsButton();
		HotelsGuestPicker.incrementAdultsButton();
		numberOfGuests += 2;
		HotelsSearchScreen.guestPicker().clickOnSearchButton();

		HotelsSearchScreen.clickHotelWithName("happypath");
		HotelsDetailsScreen.clickSelectButton();
		HotelsRoomsRatesScreen.selectRoomItem(0);

		HotelsCheckoutScreen.guestCountView().check(matches(withText(getActivity().getResources()
			.getQuantityString(R.plurals.number_of_guests, numberOfGuests, numberOfGuests))));

		HotelsCheckoutScreen.clickCheckoutButton();

		HotelsCheckoutScreen.clickGuestDetails();
		CommonTravelerInformationScreen.enterFirstName("Mobiata");
		CommonTravelerInformationScreen.enterLastName("Auto");
		CommonTravelerInformationScreen.enterPhoneNumber("1112223333");
		CommonTravelerInformationScreen.enterEmailAddress("mobiataauto@gmail.com");
		CommonTravelerInformationScreen.clickDoneButton();

		HotelsCheckoutScreen.clickSelectPaymentButton();
		CardInfoScreen.typeTextCreditCardEditText("4111111111111111");
		Common.closeSoftKeyboard(CardInfoScreen.creditCardNumberEditText());
		CardInfoScreen.clickOnExpirationDateButton();
		CardInfoScreen.clickMonthUpButton();
		CardInfoScreen.clickYearUpButton();
		CardInfoScreen.clickSetButton();
		CardInfoScreen.typeTextPostalCode("94015");
		CardInfoScreen.typeTextNameOnCardEditText("Mobiata Auto");
		CardInfoScreen.clickOnDoneButton();

		HotelsCheckoutScreen.slideToCheckout();
		CVVEntryScreen.parseAndEnterCVV("111");
		CVVEntryScreen.clickBookButton();

		HotelSearchParams params = Db.getTripBucket().getHotel().getHotelSearchParams();
		String dateRangeString = DateFormatUtils.formatRangeDateToDate(getActivity(), params,
			DateFormatUtils.FLAGS_DATE_ABBREV_MONTH);
		String guestString = getActivity().getResources()
			.getQuantityString(R.plurals.number_of_guests, numberOfGuests, numberOfGuests);
		String expectedSummaryString = getActivity().getResources()
			.getString(R.string.stay_summary_TEMPLATE, guestString, dateRangeString);
		HotelsConfirmationScreen.summaryTextView().check(matches(withText(expectedSummaryString)));
	}
}
