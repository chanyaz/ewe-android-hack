package com.expedia.bookings.test.phone.packages;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PackageTestCase;
import com.expedia.bookings.test.phone.newhotels.HotelScreen;

import static android.support.test.espresso.action.ViewActions.click;

public class PackageHotelDetailsTest extends PackageTestCase {

	public void testHideStandaloneHotelPricing() throws Throwable {
		PackageScreen.searchPackage();
		Common.delay(1);

		PackageScreen.hotelBundle().perform(click());
		Common.delay(1);

		HotelScreen.selectHotel("packagehappypath");
		Common.delay(3);

		EspressoUtils.assertViewWithSiblingIsNotDisplayed(R.id.discount_percentage, R.id.promo_text);
		EspressoUtils.assertViewIsNotDisplayed(R.id.hotel_price_container);
		EspressoUtils.assertViewIsDisplayed(R.id.search_dates_info);
	}
}
