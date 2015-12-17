package com.expedia.bookings.test.phone.launch;

import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.cars.LatLong;
import com.expedia.bookings.data.cars.Suggestion;
import com.expedia.bookings.data.collections.CollectionLocation;
import com.expedia.bookings.data.hotels.Hotel;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.test.espresso.AbacusTestUtils;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import com.expedia.bookings.test.phone.newhotels.HotelScreen;
import com.expedia.bookings.test.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.utils.DateUtils;

public class HotelsABTest extends PhoneTestCase {

	private static final List<String> bucketedTests = Arrays
		.asList(
			"testBucketedHotels",
			"testAirAttachBannerClick",
			"testSeeMore",
			"testPopularHotelSelection",
			"testPopularLocationSelection");

	@Override
	public void runTest() throws Throwable {
		String testMethodName = getClass().getMethod(getName(), (Class[]) null).toString();
		if (isBucketed(testMethodName)) {
			AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppHotelsABTest,
				AbacusUtils.DefaultVariate.BUCKETED.ordinal());
		}
		else {
			AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppHotelsABTest,
				AbacusUtils.DefaultVariate.CONTROL.ordinal());
		}
		super.runTest();
	}

	private boolean isBucketed(String testMethodName) {
		for (String bucketedTest : bucketedTests) {
			if (testMethodName.contains(bucketedTest)) {
				return true;
			}
		}
		return false;
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

	public void testPopularLocationSelection() {
		CollectionLocation collectionLocation = new CollectionLocation();
		Suggestion suggestion = new Suggestion();
		String city = "San Francisco";
		suggestion.displayName = city;
		suggestion.shortName = city;
		suggestion.type = HotelSearchParams.SearchType.CITY.toString();
		suggestion.latLong = new LatLong(0, 0);
		collectionLocation.location = suggestion;
		Events.post(new Events.LaunchCollectionItemSelected(collectionLocation, null, ""));
		// Assert that the results screen is displayed
		HotelScreen.waitForResultsLoaded();
		Common.pressBack();
		// Assert that the search screen is displayed with the correct search params
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.hotel_location, "San Francisco");
		EspressoUtils.assertViewWithTextIsDisplayed("2 Guests");
		LocalDate checkIn = LocalDate.now().plusDays(1);
		LocalDate checkOut = LocalDate.now().plusDays(2);
		String expectedCheckInDate = DateUtils.localDateToMMMd(checkIn);
		String expectedCheckoutDate = DateUtils.localDateToMMMd(checkOut);
		String expected = expectedCheckInDate + " - " + expectedCheckoutDate + " (1 night)";
		EspressoUtils.assertViewWithTextIsDisplayed(expected);
	}

	public void testSeeMore() {
		CollectionLocation collectionLocation = new CollectionLocation();
		Suggestion suggestion = new Suggestion();
		suggestion.type = HotelSearchParams.SearchType.MY_LOCATION.toString();
		suggestion.latLong = new LatLong(32.71444d, -117.16237d);
		collectionLocation.location = suggestion;
		Events.post(new Events.LaunchCollectionItemSelected(collectionLocation, null, ""));
		// Assert that the results screen is displayed
		HotelScreen.waitForResultsLoaded();
	}

	public void testPopularHotelSelection() {
		Hotel hotel = new Hotel();
		hotel.hotelId = "happypath";
		hotel.localizedName = "happypath";
		Events.post(new Events.LaunchListItemSelected(hotel));
		Common.delay(2);
		// Assert that the details screen is displayed
		HotelScreen.waitForDetailsLoaded();
	}


	public void testAirAttachBannerClick() {
		HotelSearchParams hotelSearchParams = new HotelSearchParams();
		LocalDate checkIn = LocalDate.now().plusDays(2);
		LocalDate checkOut = LocalDate.now().plusDays(4);
		hotelSearchParams.setRegionId("1234");
		hotelSearchParams.setSearchType(HotelSearchParams.SearchType.CITY);
		hotelSearchParams.setQuery("San Francisco");
		hotelSearchParams.setNumAdults(2);
		hotelSearchParams.setCheckInDate(checkIn);
		hotelSearchParams.setCheckOutDate(checkOut);
		Events.post(new Events.LaunchAirAttachBannerShow(hotelSearchParams));
		Common.delay(2);
		EspressoUtils.assertViewIsDisplayed(R.id.air_attach_banner);

		LaunchScreen.clickOnAirAttachBanner();
		// Assert that the results screen is displayed
		HotelScreen.waitForResultsLoaded();
		Common.pressBack();
		// Assert that the search screen is displayed with the correct search params
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.hotel_location, "San Francisco");
		EspressoUtils.assertViewWithTextIsDisplayed("2 Guests");
		String expectedCheckInDate = DateUtils.localDateToMMMd(checkIn);
		String expectedCheckoutDate = DateUtils.localDateToMMMd(checkOut);
		String expected = expectedCheckInDate + " - " + expectedCheckoutDate + " (2 nights)";
		EspressoUtils.assertViewWithTextIsDisplayed(expected);

	}

}
