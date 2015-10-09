package com.expedia.bookings.test.ui.phone.tests.hotels;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsCheckoutScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsDetailsScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsRoomsRatesScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsSearchScreen;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PhoneTestCase;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class HotelCouponErrorTest extends PhoneTestCase {

	private static final String TAG = HotelCouponErrorTest.class.getSimpleName();

	public void testHotelCouponErrors() throws Exception {
		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		ScreenActions.enterLog(TAG, "Setting hotel search city to: " + "New York, NY");
		HotelsSearchScreen.enterSearchText("New York, NY");
		HotelsSearchScreen.clickSuggestionWithName(getActivity(), "New York, NY");
		HotelsSearchScreen.clickListItem(1);
		HotelsDetailsScreen.clickSelectButton();
		try {
			onView(withText("OK")).perform(click());
		}
		catch (Exception e) {
			ScreenActions.enterLog(TAG, "Not available right now");
		}
		ScreenActions.enterLog(TAG, "Selecting first room listed for this hotel.");
		HotelsRoomsRatesScreen.selectRoomItem(0);
		HotelsCheckoutScreen.clickCheckoutButton();

		verifyCouponErrors();
	}

	private void verifyCouponErrors() {
		HotelsCheckoutScreen.couponButton().perform(click());
		onView(withId(R.id.coupon_edit_text)).perform(typeText("hotel_coupon_errors_duplicate"));
		onView(withId(android.R.id.button1)).perform(click());
		EspressoUtils.assertViewWithTextIsDisplayed(android.R.id.message, "Sorry, but this coupon code has already been used.");
		onView(withId(android.R.id.button3)).perform(click());

		HotelsCheckoutScreen.couponButton().perform(click());
		onView(withId(R.id.coupon_edit_text)).perform(typeText("hotel_coupon_errors_expired"));
		onView(withId(android.R.id.button1)).perform(click());
		EspressoUtils.assertViewWithTextIsDisplayed(android.R.id.message, "Sorry, but this coupon has expired.");
		onView(withId(android.R.id.button3)).perform(click());

		HotelsCheckoutScreen.couponButton().perform(click());
		onView(withId(R.id.coupon_edit_text)).perform(typeText("hotel_coupon_errors_exceeded_earn_limit"));
		onView(withId(android.R.id.button1)).perform(click());
		EspressoUtils.assertViewWithTextIsDisplayed(android.R.id.message,
			"Sorry, this coupon has been used too many times and can no longer be applied.");
		onView(withId(android.R.id.button3)).perform(click());

		HotelsCheckoutScreen.couponButton().perform(click());
		onView(withId(R.id.coupon_edit_text)).perform(typeText("hotel_coupon_errors_fallback"));
		onView(withId(android.R.id.button1)).perform(click());
		EspressoUtils.assertViewWithTextIsDisplayed(android.R.id.message,
			"Sorry, but we're having a problem. Please try again.");
		onView(withId(android.R.id.button3)).perform(click());

		HotelsCheckoutScreen.couponButton().perform(click());
		onView(withId(R.id.coupon_edit_text)).perform(typeText("hotel_coupon_errors_hotel_excluded"));
		onView(withId(android.R.id.button1)).perform(click());
		EspressoUtils.assertViewWithTextIsDisplayed(android.R.id.message,
			"Sorry, but this hotel doesn't accept coupons.");
		onView(withId(android.R.id.button3)).perform(click());

		HotelsCheckoutScreen.couponButton().perform(click());
		onView(withId(R.id.coupon_edit_text)).perform(typeText("hotel_coupon_errors_invalid_hotel"));
		onView(withId(android.R.id.button1)).perform(click());
		EspressoUtils.assertViewWithTextIsDisplayed(android.R.id.message,
			"Sorry, but the coupon cannot be applied to this hotel.");
		onView(withId(android.R.id.button3)).perform(click());

		HotelsCheckoutScreen.couponButton().perform(click());
		onView(withId(R.id.coupon_edit_text)).perform(typeText("hotel_coupon_errors_invalid_product"));
		onView(withId(android.R.id.button1)).perform(click());
		EspressoUtils.assertViewWithTextIsDisplayed(android.R.id.message,
			"Sorry, but this hotel doesn't accept coupons.");
		onView(withId(android.R.id.button3)).perform(click());

		HotelsCheckoutScreen.couponButton().perform(click());
		onView(withId(R.id.coupon_edit_text)).perform(typeText("hotel_coupon_errors_invalid_region"));
		onView(withId(android.R.id.button1)).perform(click());
		EspressoUtils.assertViewWithTextIsDisplayed(android.R.id.message,
			"Sorry, but this coupon code is for a different country.");
		onView(withId(android.R.id.button3)).perform(click());

		HotelsCheckoutScreen.couponButton().perform(click());
		onView(withId(R.id.coupon_edit_text)).perform(typeText("hotel_coupon_errors_invalid_stay_dates"));
		onView(withId(android.R.id.button1)).perform(click());
		EspressoUtils.assertViewWithTextIsDisplayed(android.R.id.message,
			"Sorry, but the coupon cannot be applied for these travel dates.");
		onView(withId(android.R.id.button3)).perform(click());

		HotelsCheckoutScreen.couponButton().perform(click());
		onView(withId(R.id.coupon_edit_text)).perform(typeText("hotel_coupon_errors_invalid_travel_dates"));
		onView(withId(android.R.id.button1)).perform(click());
		EspressoUtils.assertViewWithTextIsDisplayed(android.R.id.message,
			"Sorry, but this coupon isn't valid for these dates.");
		onView(withId(android.R.id.button3)).perform(click());

		HotelsCheckoutScreen.couponButton().perform(click());
		onView(withId(R.id.coupon_edit_text)).perform(typeText("hotel_coupon_errors_min_purchase_amount_not_met"));
		onView(withId(android.R.id.button1)).perform(click());
		EspressoUtils.assertViewWithTextIsDisplayed(android.R.id.message,
			"Sorry, but this doesn't meet the minimum cost requirement.");
		onView(withId(android.R.id.button3)).perform(click());

		HotelsCheckoutScreen.couponButton().perform(click());
		onView(withId(R.id.coupon_edit_text)).perform(typeText("hotel_coupon_errors_not_redeemed"));
		onView(withId(android.R.id.button1)).perform(click());
		EspressoUtils.assertViewWithTextIsDisplayed(android.R.id.message,
			"Sorry, but we're having a problem. Please try again.");
		onView(withId(android.R.id.button3)).perform(click());

		HotelsCheckoutScreen.couponButton().perform(click());
		onView(withId(R.id.coupon_edit_text)).perform(typeText("hotel_coupon_errors_price_change"));
		onView(withId(android.R.id.button1)).perform(click());
		EspressoUtils.assertViewWithTextIsDisplayed(android.R.id.message,
			"The booking cost changed, so we didn't apply the coupon. Please review and try again.");
		onView(withId(android.R.id.button3)).perform(click());

		HotelsCheckoutScreen.couponButton().perform(click());
		onView(withId(R.id.coupon_edit_text)).perform(typeText("hotel_coupon_errors_service_down"));
		onView(withId(android.R.id.button1)).perform(click());
		EspressoUtils.assertViewWithTextIsDisplayed(android.R.id.message,
			"Sorry, but we cannot process coupons currently. Please try again.");
		onView(withId(android.R.id.button3)).perform(click());

		HotelsCheckoutScreen.couponButton().perform(click());
		onView(withId(R.id.coupon_edit_text)).perform(typeText("hotel_coupon_errors_unrecognized"));
		onView(withId(android.R.id.button1)).perform(click());
		EspressoUtils.assertViewWithTextIsDisplayed(android.R.id.message,
			"Sorry, but this isn't a valid coupon code.");
		onView(withId(android.R.id.button3)).perform(click());
	}
}
