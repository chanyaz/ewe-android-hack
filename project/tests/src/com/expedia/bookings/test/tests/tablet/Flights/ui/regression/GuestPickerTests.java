package com.expedia.bookings.test.tests.tablet.Flights.ui.regression;

import org.joda.time.LocalDate;

import com.expedia.bookings.test.tests.pageModels.tablet.Common;
import com.expedia.bookings.test.tests.pageModels.tablet.Launch;
import com.expedia.bookings.test.tests.pageModels.tablet.Search;
import com.expedia.bookings.test.utils.EspressoUtils;
import com.expedia.bookings.test.utils.TabletTestCase;

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
		Launch.typeInDestinationEditText("San Francisco, CA");
		Launch.clickSuggestion("San Francisco, CA");
		Common.pressBack();
		Search.clickOriginButton();
		Search.typeInOriginEditText("Detroit, MI");
		Search.clickSuggestion("Detroit, MI");
		Search.clickSelectFlightDates();
		int randomOffset = 20 + (int) (Math.random() * 100);
		LocalDate startDate = LocalDate.now().plusDays(randomOffset);
		Search.clickDate(startDate, null);
		Search.clickSearchPopupDone();

		Search.clickTravelerButton();

		//assert initial adult and child counts text view
		EspressoUtils.assertContains(Search.adultCountText(), Search.adultPickerStringPlural(1, getInstrumentation()));
		EspressoUtils.assertContains(Search.childCountText(), Search.childPickerStringPlural(0, getInstrumentation()));

		int adultCount = 1;
		int childCount = 0;
		final int adultMax = 6;
		final int childMax = 4;

		for (int i = 1; i < adultMax; i++) {
			EspressoUtils.assertContains(Search.adultCountText(), Search.adultPickerStringPlural(adultCount, getInstrumentation()));
			Search.incrementAdultButton();
			adultCount++;
		}

		for (int i = 6; i > 1; i--) {
			Search.decrementAdultButton();
			adultCount--;
			EspressoUtils.assertContains(Search.adultCountText(), Search.adultPickerStringPlural(adultCount, getInstrumentation()));
		}

		for (int i = 0; i < childMax; i++) {
			EspressoUtils.assertContains(Search.childCountText(), Search.childPickerStringPlural(childCount, getInstrumentation()));
			Search.incrementChildButton();
			childCount++;
		}
	}

	public void testLapInfantAlret() {
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("San Francisco, CA");
		Launch.clickSuggestion("San Francisco, CA");
		Search.clickTravelerButton();
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
}
