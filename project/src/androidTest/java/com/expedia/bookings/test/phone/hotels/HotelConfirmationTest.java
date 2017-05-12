package com.expedia.bookings.test.phone.hotels;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;

import android.support.test.espresso.matcher.ViewMatchers;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.HotelTestCase;
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel;
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen;
import com.expedia.bookings.utils.DateFormatUtils;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewIsDisplayed;
import static org.hamcrest.Matchers.allOf;

public class HotelConfirmationTest extends HotelTestCase {

	@Test
	public void testConfirmationView() throws Throwable {
		SearchScreen.doGenericHotelSearch();
		HotelScreen.selectHotel();
		HotelScreen.selectRoom();
		HotelScreen.checkout(true);
		CheckoutViewModel.performSlideToPurchase(false);
		HotelScreen.enterCVVAndBook();
		HotelScreen.waitForConfirmationDisplayed();

		DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd");
		String checkinDate = "2014-04-23";
		String checkoutDate = "2014-04-24";

		EspressoUtils.assertViewIsDisplayed(R.id.hotel_confirmation_presenter);
		onView(withId(R.id.hotel_name_view)).check(matches((withText("Layne Hotel"))));

		String expectedDateText = DateFormatUtils
			.formatDateRange(getActivity(), dtf.parseLocalDate(checkinDate), dtf.parseLocalDate(checkoutDate), DateFormatUtils.FLAGS_DATE_ABBREV_MONTH);
		onView(allOf(withId(R.id.check_in_out_dates), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
			.check(matches(withText(expectedDateText)));

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
