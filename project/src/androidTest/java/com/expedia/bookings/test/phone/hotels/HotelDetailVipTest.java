package com.expedia.bookings.test.phone.hotels;

import org.junit.Test;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.HotelTestCase;
import com.expedia.bookings.test.pagemodels.common.SearchScreen;
import com.expedia.bookings.test.pagemodels.hotels.HotelInfoSiteScreen;
import com.expedia.bookings.test.pagemodels.hotels.HotelResultsScreen;


public class HotelDetailVipTest extends HotelTestCase {
	@Test
	public void testVIPHotel() throws Throwable {
		SearchScreen.doGenericHotelSearch();
		HotelResultsScreen.selectHotel("vip_hotel");
		HotelInfoSiteScreen.waitForDetailsLoaded();
		HotelInfoSiteScreen.clickVIPAccess();
		Common.delay(2);
		EspressoUtils.assertViewWithTextIsDisplayed(getActivity().getString(R.string.vip_access_message));
		Common.pressBack();
		HotelInfoSiteScreen.waitForDetailsLoaded();
		EspressoUtils.assertViewIsDisplayed(R.id.vip_access_message_container);
	}
}
