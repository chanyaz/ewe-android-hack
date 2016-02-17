package com.expedia.bookings.test.phone.hotels;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.HotelTestCase;

public class HotelDetailVipTest extends HotelTestCase {
	public void testVIPHotel() throws Throwable {
		HotelScreen.doGenericSearch();
		HotelScreen.selectHotel("vip_hotel");
		HotelScreen.waitForDetailsLoaded();
		HotelScreen.clickVIPAccess();
		Common.delay(2);
		EspressoUtils.assertViewWithTextIsDisplayed(getActivity().getString(R.string.vip_access_message));
		Common.pressBack();
		HotelScreen.waitForDetailsLoaded();
		EspressoUtils.assertViewIsDisplayed(R.id.vip_access_message);
	}
}
