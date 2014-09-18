package com.expedia.bookings.test.phone.tests.hotels;

import com.expedia.bookings.R;
import com.expedia.bookings.test.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.phone.pagemodels.hotels.HotelsGuestPicker;
import com.expedia.bookings.test.phone.pagemodels.hotels.HotelsSearchScreen;
import com.expedia.bookings.test.utils.PhoneTestCase;
import com.google.android.apps.common.testing.ui.espresso.Espresso;

import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayed;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;

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

		String lowerTextChildView = HotelsGuestPicker.getGuestTextViewValue(R.id.text_lower, R.id.children_number_picker);
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
			adultCurrentTextViewValue = HotelsGuestPicker.getGuestTextViewValue(R.id.text_current, R.id.adults_number_picker);
			assertEquals(HotelsGuestPicker.adultPickerStringPlural(adultCount, getActivity().getResources()), adultCurrentTextViewValue);

			adultHigherTextViewValue = HotelsGuestPicker.getGuestTextViewValue(R.id.text_higher, R.id.adults_number_picker);
			assertEquals(HotelsGuestPicker.adultPickerStringPlural(adultCount + 1, getActivity().getResources()), adultHigherTextViewValue);

			HotelsGuestPicker.incrementAdultsButton();
			adultCount++;

			adultLowerTextViewValue = HotelsGuestPicker.getGuestTextViewValue(R.id.text_lower, R.id.adults_number_picker);
			assertEquals(HotelsGuestPicker.adultPickerStringPlural(adultCount - 1, getActivity().getResources()), adultLowerTextViewValue);
		}

		for (int i = 6; i > 0; i--) {
			HotelsGuestPicker.decrementAdultsButton();
		}

		String childCurrentTextViewValue;
		String childHigherTextViewValue;
		String childLowerTextViewValue;

		for (int i = 0; i < childMax; i++) {
			childHigherTextViewValue = HotelsGuestPicker.getGuestTextViewValue(R.id.text_higher, R.id.children_number_picker);
			assertEquals(HotelsGuestPicker.childPickerStringPlural(childCount + 1, getActivity().getResources()), childHigherTextViewValue);

			childCurrentTextViewValue = HotelsGuestPicker.getGuestTextViewValue(R.id.text_current, R.id.children_number_picker);
			assertEquals(HotelsGuestPicker.childPickerStringPlural(childCount, getActivity().getResources()), childCurrentTextViewValue);

			HotelsGuestPicker.incrementChildrenButton();
			childCount++;

			childLowerTextViewValue = HotelsGuestPicker.getGuestTextViewValue(R.id.text_lower, R.id.children_number_picker);
			assertEquals(HotelsGuestPicker.childPickerStringPlural(childCount - 1, getActivity().getResources()), childLowerTextViewValue);
		}
	}

	public void testChildSelectorAppearing() {
		final int childMax = 4;
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickOnGuestsButton();
		assertFalse(withText(HotelsGuestPicker.selectChildAgePlural(1, getActivity().getResources())).matches(isDisplayed()));

		for (int i = 1; i <= childMax; i++) {
			HotelsGuestPicker.incrementChildrenButton();
			assertFalse(withText(HotelsGuestPicker.selectChildAgePlural(i, getActivity().getResources())).matches(isDisplayed()));
		}
		Espresso.pressBack();
		Espresso.pressBack();
	}
}
