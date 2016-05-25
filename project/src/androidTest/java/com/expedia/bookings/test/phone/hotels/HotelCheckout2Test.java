package com.expedia.bookings.test.phone.hotels;

import android.support.test.espresso.Espresso;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.HotelTestCase;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel;
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen;

import junit.framework.Assert;

import static android.support.test.espresso.action.ViewActions.click;

public class HotelCheckout2Test extends HotelTestCase {
	public void testCouponIsClearedEachCreateTrip() throws Throwable {
		SearchScreen.doGenericHotelSearch();
		HotelScreen.selectHotel("happypath");
		HotelScreen.selectRoom();
		CheckoutViewModel.waitForCheckout();
		CheckoutViewModel.clickDone();
		CheckoutViewModel.applyCoupon("hotel_coupon_success");
		// Coupon was applied
		CheckoutViewModel.scrollView().perform(ViewActions.swipeDown());
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.total_price_with_tax_and_fees, "$114.76");

		// Nav back to rooms and rates
		Espresso.pressBack();
		Espresso.pressBack();
		HotelScreen.selectHotel("happypath");
		HotelScreen.selectRoomButton().perform(click());
		HotelScreen.clickRoom("happypath_2_night_stay_0");
		HotelScreen.clickAddRoom();

		// Pick a different room, should refresh createTrip with a new price
		CheckoutViewModel.scrollView().perform(ViewActions.swipeDown());
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.total_price_with_tax_and_fees, "$2,394.88");
	}


	public void testTealeafIDClearedAfterSignIn() throws Throwable {
		SearchScreen.doGenericHotelSearch();
		HotelScreen.selectHotel("tealeaf_id");
		Common.delay(1);
		HotelScreen.selectRoom();
		Assert.assertEquals(Db.getTripBucket().getHotelV2().mHotelTripResponse.tealeafTransactionId, "tealeafHotel:tealeaf_id");
		HotelScreen.clickSignIn();
		HotelScreen.signIn();
		Assert.assertEquals(Db.getTripBucket().getHotelV2().mHotelTripResponse.tealeafTransactionId, "tealeafHotel:tealeaf_id_signed_in");
	}


}
