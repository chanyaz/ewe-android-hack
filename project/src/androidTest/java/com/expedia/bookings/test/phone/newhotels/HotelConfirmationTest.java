package com.expedia.bookings.test.phone.newhotels;

import android.support.test.espresso.matcher.ViewMatchers;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.HotelTestCase;
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewIsDisplayed;
import static com.expedia.bookings.test.phone.newhotels.HotelScreen.doGenericSearch;
import static org.hamcrest.Matchers.allOf;

public class HotelConfirmationTest extends HotelTestCase {

	public void testConfirmationView() throws Throwable {
		doGenericSearch();
		HotelScreen.selectHotel();
		HotelScreen.selectRoom();
		HotelScreen.checkout(true);
		CheckoutViewModel.performSlideToPurchase(false);
		HotelScreen.enterCVVAndBook();
		HotelScreen.waitForConfirmationDisplayed();

		EspressoUtils.assertViewIsDisplayed(R.id.hotel_confirmation_presenter);
		onView(withId(R.id.hotel_name_view)).check(matches((withText("Layne Hotel"))));

		onView(allOf(withId(R.id.check_in_out_dates), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
			.check(matches(withText("Apr 23 – 24, 2014")));

		onView(allOf(withId(R.id.address_line_one), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
			.check(matches(withText("545 Jones St")));
		onView(allOf(withId(R.id.address_line_two), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))).check(
			matches(withText("San Francisco, CA")));
		onView(withId(R.id.itin_text_view)).check(matches((withText("Itinerary #174113329733"))));
		assertViewIsDisplayed(R.id.confirmation_text);
		onView(allOf(withId(R.id.email_text), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
			.check(matches(withText("a@aa.com")));

		onView(allOf(withId(R.id.direction_action_textView), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
			.check(matches(isClickable()));
		onView(allOf(withId(R.id.calendar_action_textView), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
			.check(matches(isClickable()));
		onView(allOf(withId(R.id.call_support_action_textView), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
			.check(matches(isClickable()));

	}

}
