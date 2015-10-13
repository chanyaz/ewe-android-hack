package com.expedia.bookings.test.phone.launch;

import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.test.espresso.AbacusTestUtils;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import com.expedia.bookings.test.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.utils.DateUtils;

public class HotelsABTest extends PhoneTestCase {

	@Override
	public void runTest() throws Throwable {
		String testMethodName = getClass().getMethod(getName(), (Class[]) null).toString();
		if (testMethodName.contains("testBucketedHotels") || testMethodName.contains("testAirAttachBannerClickOnNewHotel")) {
			AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppHotelsABTest,
				AbacusUtils.DefaultVariate.BUCKETED.ordinal());
		}
		else {
			AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppHotelsABTest,
				AbacusUtils.DefaultVariate.CONTROL.ordinal());
		}
		super.runTest();
	}

	public void testControlHotels() {
		LaunchScreen.launchHotels();
		// Assert that old hotels search screen is launched.
		EspressoUtils.assertViewIsDisplayed(R.id.calendar_button_container);
	}

	public void testBucketedHotels() {
		LaunchScreen.launchHotels();
		// Assert that materials hotels search screen is launched.
		EspressoUtils.assertViewIsDisplayed(R.id.hotel_presenter);
	}

	public void testAirAttachBannerClickOnNewHotel() {
		HotelSearchParams hotelSearchParams = new HotelSearchParams();
		LocalDate checkIn = LocalDate.now().plusDays(2);
		LocalDate checkOut = LocalDate.now().plusDays(4);
		hotelSearchParams.setRegionId("1234");
		hotelSearchParams.setQuery("San Francisco");
		hotelSearchParams.setNumAdults(2);
		hotelSearchParams.setCheckInDate(checkIn);
		hotelSearchParams.setCheckOutDate(checkOut);
		Events.post(new Events.LaunchAirAttachBannerShow(hotelSearchParams));
		Common.delay(2);
		EspressoUtils.assertViewIsDisplayed(R.id.air_attach_banner);

		LaunchScreen.clickOnAirAttachBanner();

		EspressoUtils.assertViewWithTextIsDisplayed(R.id.hotel_location_autocomplete, "San Francisco");
		EspressoUtils.assertViewWithTextIsDisplayed("2 Guests");
		String expectedCheckInDate = DateUtils.localDateToMMMd(checkIn);
		String expectedCheckoutDate = DateUtils.localDateToMMMd(checkOut);
		String expected = expectedCheckInDate + " - " + expectedCheckoutDate + " (2 nights)" ;
		EspressoUtils.assertViewWithTextIsDisplayed(expected);

	}

}
