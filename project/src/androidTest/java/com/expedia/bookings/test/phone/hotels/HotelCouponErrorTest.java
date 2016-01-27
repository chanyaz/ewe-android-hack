package com.expedia.bookings.test.phone.hotels;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import android.support.test.espresso.Espresso;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import com.expedia.bookings.test.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.phone.pagemodels.hotels.HotelsCheckoutScreen;
import com.expedia.bookings.test.phone.pagemodels.hotels.HotelsDetailsScreen;
import com.expedia.bookings.test.phone.pagemodels.hotels.HotelsRoomsRatesScreen;
import com.expedia.bookings.test.phone.pagemodels.hotels.HotelsSearchScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.ViewActions.waitFor;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;

public class HotelCouponErrorTest extends PhoneTestCase {

	public void testHotelCouponErrors() throws Exception {
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText("SFO");
		HotelsSearchScreen.clickSuggestionWithName(getActivity(), "San Francisco, CA (SFO-San Francisco Intl.)");
		HotelsSearchScreen.clickListItem(1);
		HotelsDetailsScreen.clickSelectButton();
		HotelsRoomsRatesScreen.selectRoomItem(0);
		HotelsCheckoutScreen.clickCheckoutButton();

		ArrayList<TestData> tests = new ArrayList<>();
		tests.add(new TestData("hotel_coupon_errors_duplicate", "Sorry, but this coupon code has already been used."));
		tests.add(new TestData("hotel_coupon_errors_expired", "Sorry, but this coupon has expired."));
		tests.add(new TestData("hotel_coupon_errors_exceeded_earn_limit", "Sorry, this coupon has been used too many times and can no longer be applied."));
		tests.add(new TestData("hotel_coupon_errors_fallback", "Sorry, but we're having a problem. Please try again."));
		tests.add(new TestData("hotel_coupon_errors_hotel_excluded", "Sorry, but this hotel doesn't accept coupons."));
		tests.add(new TestData("hotel_coupon_errors_invalid_hotel", "Sorry, but the coupon cannot be applied to this hotel."));
		tests.add(new TestData("hotel_coupon_errors_invalid_product", "Sorry, but this hotel doesn't accept coupons."));
		tests.add(new TestData("hotel_coupon_errors_invalid_region", "Sorry, but this coupon code is for a different country."));
		tests.add(new TestData("hotel_coupon_errors_invalid_stay_dates", "Sorry, but the coupon cannot be applied for these travel dates."));
		tests.add(new TestData("hotel_coupon_errors_invalid_travel_dates", "Sorry, but this coupon isn't valid for these dates."));
		tests.add(new TestData("hotel_coupon_errors_min_purchase_amount_not_met", "Sorry, but this doesn't meet the minimum cost requirement."));
		tests.add(new TestData("hotel_coupon_errors_not_redeemed", "Sorry, but we're having a problem. Please try again."));
		tests.add(new TestData("hotel_coupon_errors_price_change", "The booking cost changed, so we didn't apply the coupon. Please review and try again."));
		tests.add(new TestData("hotel_coupon_errors_service_down", "Sorry, but we cannot process coupons currently. Please try again."));
		tests.add(new TestData("hotel_coupon_errors_unrecognized", "Sorry, but this isn't a known coupon code."));
		tests.add(new TestData("hotel_coupon_errors_not_active", "Sorry, but this coupon has expired."));
		tests.add(new TestData("hotel_coupon_errors_not_exists", "Sorry, but this isn't a known coupon code."));
		tests.add(new TestData("hotel_coupon_errors_not_configured", "Sorry, but this isn't a known coupon code."));
		tests.add(new TestData("hotel_coupon_errors_product_missing", "Sorry, but the coupon cannot be applied to this booking."));

		for (TestData data : tests) {
			HotelsCheckoutScreen.couponButton().perform(waitFor(isEnabled(), 5, TimeUnit.SECONDS), click());

			// Coupon Dialog
			Common.delay(1);
			onView(withId(android.R.id.button1)).check(matches(not(isEnabled())));
			onView(withId(R.id.coupon_edit_text)).check(matches(withText(isEmptyOrNullString())));
			onView(withId(R.id.coupon_edit_text)).perform(typeText(data.coupon));
			Espresso.closeSoftKeyboard();
			Common.delay(2);
			onView(withId(android.R.id.button1)).check(matches(isEnabled()));
			onView(withId(android.R.id.button1)).perform(click());

			// Error dialog
			EspressoUtils.assertViewWithTextIsDisplayed(android.R.id.message, data.expected);
			onView(withId(android.R.id.button3)).perform(click());
		}
	}

	private static class TestData {
		public String coupon;
		public String expected;

		public TestData(String coupon, String expected) {
			this.coupon = coupon;
			this.expected = expected;
		}
	}

}
