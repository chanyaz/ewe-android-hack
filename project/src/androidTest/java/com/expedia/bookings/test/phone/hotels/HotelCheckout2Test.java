package com.expedia.bookings.test.phone.hotels;

import org.junit.Test;

import android.support.test.espresso.Espresso;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.HotelTestCase;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.pagemodels.common.CheckoutScreen;
import com.expedia.bookings.test.pagemodels.common.LogInScreen;
import com.expedia.bookings.test.pagemodels.common.SearchScreen;
import com.expedia.bookings.test.pagemodels.hotels.HotelCheckoutScreen;
import com.expedia.bookings.test.pagemodels.hotels.HotelInfoSiteScreen;
import com.expedia.bookings.test.pagemodels.hotels.HotelResultsScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.expedia.bookings.test.espresso.CustomMatchers.withContentDescription;

import junit.framework.Assert;

import java.util.concurrent.TimeUnit;

public class HotelCheckout2Test extends HotelTestCase {
	@Test
	public void testCouponIsClearedEachCreateTrip() throws Throwable {
		SearchScreen.doGenericHotelSearch();
		HotelResultsScreen.selectHotel("happypath");
		HotelInfoSiteScreen.bookFirstRoom();

		//a11y test for cost summary
		onView(withId(R.id.cost_summary)).check(matches(withContentDescription("Total with Tax $135.81 Due to Expedia today $0  Cost summary information Button")));

		CheckoutScreen.waitForCheckout();
		CheckoutScreen.clickDone();
		CheckoutScreen.applyCoupon("hotel_coupon_success");
		// Coupon was applied
		CheckoutScreen.scrollView().perform(ViewActions.swipeDown());
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.total_price_with_tax_and_fees, "$114.76");

		// Nav back to rooms and rates
		Espresso.pressBack();
		Espresso.pressBack();
		HotelResultsScreen.selectHotel("happypath");
		HotelInfoSiteScreen.clickStickySelectRoom();
		HotelInfoSiteScreen.bookRoomType("happypath_2_night_stay_0");

		// Pick a different room, should refresh createTrip with a new price
		CheckoutScreen.scrollView().perform(ViewActions.swipeDown());
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.total_price_with_tax_and_fees, "$2,394.88");
	}

	@Test
	public void testTealeafIDClearedAfterSignIn() throws Throwable {
		SearchScreen.doGenericHotelSearch();
		HotelResultsScreen.selectHotel("tealeaf_id");
		Common.delay(1);
		HotelInfoSiteScreen.bookFirstRoom();
		Assert.assertEquals(Db.getTripBucket().getHotelV2().mHotelTripResponse.tealeafTransactionId, "tealeafHotel:tealeaf_id");
		HotelCheckoutScreen.clickSignIn();
		LogInScreen.signIn("qa-ehcc@mobiata.com", "password");
		EspressoUtils.waitForViewNotYetInLayoutToDisplay(CheckoutScreen.toolBarMatcher(), 10, TimeUnit.SECONDS);
		Assert.assertEquals(Db.getTripBucket().getHotelV2().mHotelTripResponse.tealeafTransactionId, "tealeafHotel:tealeaf_id_signed_in");
	}
}
