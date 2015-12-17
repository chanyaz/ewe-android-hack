package com.expedia.bookings.test.ui.tablet.tests.flights;

import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.tablet.pagemodels.Checkout;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;
import com.expedia.bookings.test.ui.tablet.pagemodels.HotelDetails;
import com.expedia.bookings.test.ui.tablet.pagemodels.Launch;
import com.expedia.bookings.test.ui.tablet.pagemodels.Results;
import com.expedia.bookings.test.ui.tablet.pagemodels.Search;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.TabletTestCase;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

/**
 * Created by dmadan on 7/21/14.
 */
public class GuestPickerTests extends TabletTestCase {

	// verify that the guest number picker's text views
	// show the expected text when children and adults
	// are incremented and decremented
	public void testPickerTextViews() {
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

		Search.clickTravelerButton();

		//assert initial adult and child counts text view
		checkAdultCountText(1);
		checkChildCountText(0);

		int adultCount = 1;
		int childCount = 0;
		final int adultMax = 6;
		final int childMax = 4;

		for (int i = 1; i < adultMax; i++) {
			checkAdultCountText(adultCount);
			Search.incrementAdultButton();
			adultCount++;
		}

		for (int i = 6; i > 1; i--) {
			Search.decrementAdultButton();
			adultCount--;
			checkAdultCountText(adultCount);
		}

		for (int i = 0; i < childMax; i++) {
			checkChildCountText(childCount);
			Search.incrementChildButton();
			childCount++;
		}
		Common.pressBack();
	}

	public void testLapInfantAlret() {
		goToSearchScreen();
		Search.incrementChildButton();
		Search.incrementChildButton();
		Search.clickChild1Spinner();
		Search.selectChildTravelerAgeAt(0, getActivity());
		Search.clickChild2Spinner();
		Search.selectChildTravelerAgeAt(0, getActivity());

		//Check for lap infant alert
		Common.checkDisplayed(Search.lapInfantAlert());
		Search.decrementChildButton();
		Search.decrementChildButton();
		Search.clickSearchPopupDone();
	}

	public void testMaxGuestsWithAllAdults() {
		int adultCount = 6;
		int childCount = 0;
		goToSearchScreen();
		for (int i = 1; i < adultCount; i++) {
			Search.incrementAdultButton();
		}
		checkAdultCountText(adultCount);
		checkChildCountText(childCount);
	}

	public void testMaxGuestsWithAdultAndChildrenSelected() {
		int adultCount = 4;
		int childCount = 2;
		goToSearchScreen();
		for (int i = 1; i < adultCount; i++) {
			Search.incrementAdultButton();
		}
		for (int i = 0; i < childCount; i++) {
			Search.incrementChildButton();
		}
		checkAdultCountText(adultCount);
		checkChildCountText(childCount);
	}

	public void testMinAdultGuests() {
		int adultCount = 1;
		goToSearchScreen();
		checkAdultCountText(adultCount);

		Search.incrementAdultButton();
		adultCount++;
		checkAdultCountText(adultCount);

		Search.decrementAdultButton();
		adultCount--;
		checkAdultCountText(adultCount);

		Search.decrementAdultButton();
		checkAdultCountText(adultCount);
	}

	public void testGuestPickerClosesOnDone() {
		goToSearchScreen();
		Search.clickSearchPopupDone();
		onView(withId(R.id.guest_picker)).check(matches(not(isDisplayed())));
	}

	public void testGuestPickerDisplayWithIncrementDecrement() {
		goToSearchScreen();
		checkAdultCountText(1);

		Search.incrementAdultButton();
		checkAdultCountText(2);
		Search.incrementAdultButton();
		checkAdultCountText(3);
		Search.decrementAdultButton();
		checkAdultCountText(2);

		Search.incrementChildButton();
		checkChildCountText(1);
		Search.incrementChildButton();
		checkChildCountText(2);
		Search.decrementChildButton();
		checkChildCountText(1);
	}

	public void testMaxChildrenAllowed() {
		goToSearchScreen();
		int maxChildrenAllowed = 4;
		for (int i = 0; i < maxChildrenAllowed; i++) {
			Search.incrementChildButton();
		}
		checkAdultCountText(1);
		checkChildCountText(4);
	}

	public void testGuestsSelectedAreReflectedOnOverviewAndCheckout() {
		int numberOfGuests = 1;
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("Detroit, MI");
		Launch.clickSuggestion("Detroit, MI");

		Search.clickTravelerButton();
		Search.incrementAdultButton();
		Search.incrementAdultButton();
		numberOfGuests += 2;
		Search.clickSearchPopupDone();

		Results.swipeUpHotelList();
		Results.clickHotelWithName("happypath");
		HotelDetails.clickAddHotel();
		Results.clickBookHotel();

		// Test guest count on overview screen
		onView(withId(R.id.num_travelers_text_view)).check(matches(withText(
			getActivity().getResources()
				.getQuantityString(R.plurals.number_of_guests, numberOfGuests, numberOfGuests))));

		Checkout.clickOnEmptyTravelerDetails();
		Checkout.enterFirstName("Mobiata");
		Checkout.enterLastName("Auto");
		Checkout.enterPhoneNumber("1112223333");
		Checkout.enterEmailAddress("aaa@aaa.com");
		Common.closeSoftKeyboard(Checkout.firstName());
		Checkout.clickOnDone();

		Checkout.clickOnEnterPaymentInformation();
		Checkout.enterCreditCardNumber("4111111111111111");
		Common.closeSoftKeyboard(Checkout.creditCardNumber());
		Checkout.setExpirationDate(2020, 12);
		Checkout.enterNameOnCard("Mobiata Auto");
		Checkout.enterPostalCode("95104");
		Common.closeSoftKeyboard(Checkout.postalCode());
		Checkout.clickOnDone();

		Checkout.slideToPurchase();
		Checkout.enterCvv("111");
		Checkout.clickBookButton();

		// Test guest count on confirmation screen num_travelers_text_view
		onView(withId(R.id.num_travelers_text_view)).check(matches(withText(getActivity().getResources()
			.getQuantityString(R.plurals.number_of_guests, numberOfGuests, numberOfGuests))));
	}

	private void goToSearchScreen() {
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("San Francisco, CA");
		Launch.clickSuggestion("San Francisco, CA");
		Search.clickTravelerButton();
	}

	private void checkAdultCountText(int count) {
		EspressoUtils
			.assertContains(Search.adultCountText(), Search.adultPickerStringPlural(count, getInstrumentation()));
	}

	private void checkChildCountText(int count) {
		EspressoUtils.assertContains(Search.childCountText(), Search.childPickerStringPlural(count,
			getInstrumentation()));
	}
}
