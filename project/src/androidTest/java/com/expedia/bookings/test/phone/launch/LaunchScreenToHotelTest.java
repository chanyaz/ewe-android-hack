package com.expedia.bookings.test.phone.launch;

import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.collections.CollectionLocation;
import com.expedia.bookings.data.hotels.Hotel;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.phone.hotels.HotelScreen;
import com.expedia.bookings.test.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.utils.DateUtils;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class LaunchScreenToHotelTest extends PhoneTestCase {

	public void testHotelLobButton() {
		LaunchScreen.launchHotels();
		// Assert that materials hotels search screen is launched.
		EspressoUtils.assertViewIsDisplayed(R.id.hotel_presenter);
	}

	public void testPopularLocationSelection() {
		CollectionLocation collectionLocation = new CollectionLocation();
		CollectionLocation.Location suggestion = new CollectionLocation.Location();
		String city = "San Francisco";

		suggestion.displayName = city;
		suggestion.shortName = city;
		suggestion.type = HotelSearchParams.SearchType.CITY.toString();

		CollectionLocation.LatLng latLng = new CollectionLocation.LatLng();
		latLng.lat = 0;
		latLng.lng = 0;
		suggestion.latLong = latLng;

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

	public void testSeeMore() throws Throwable {
		CollectionLocation collectionLocation = new CollectionLocation();
		CollectionLocation.Location suggestion = new CollectionLocation.Location();
		suggestion.type = HotelSearchParams.SearchType.MY_LOCATION.toString();

		CollectionLocation.LatLng coordinates = new CollectionLocation.LatLng();
		coordinates.lat = 32.71444d;
		coordinates.lng = -117.16237d;
		suggestion.latLong = coordinates;

		collectionLocation.location = suggestion;
		Events.post(new Events.LaunchCollectionItemSelected(collectionLocation, null, ""));
		// Assert that the results screen is displayed
		HotelScreen.waitForResultsLoaded();
		Common.pressBack();
		// Test that searching still works
		onView(withId(R.id.hotel_location)).perform(click());
		HotelScreen.doGenericSearch();
	}

	public void testPopularHotelSelection() {
		Hotel hotel = new Hotel();
		hotel.hotelId = "happypath";
		hotel.localizedName = "happypath";
		Events.post(new Events.LaunchListItemSelected(hotel));
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

		// Make sure that the launch screen is loaded
		LaunchScreen.hotelLaunchButton().perform(ViewActions.waitForViewToDisplay());

		// Pop the air attach banner and click it
		Events.post(new Events.LaunchAirAttachBannerShow(hotelSearchParams));
		onView(withId(R.id.air_attach_banner)).perform(ViewActions.waitForViewToCompletelyDisplay());
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
