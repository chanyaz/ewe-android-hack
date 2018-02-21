package com.expedia.bookings.test.phone.launch;

import java.util.concurrent.TimeUnit;

import org.joda.time.LocalDate;
import org.junit.Test;

import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.matcher.ViewMatchers;

import com.expedia.bookings.R;
import com.expedia.bookings.data.DeprecatedHotelSearchParams;
import com.expedia.bookings.data.collections.CollectionLocation;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import com.expedia.bookings.test.pagemodels.hotels.HotelResultsScreen;
import com.expedia.bookings.test.pagemodels.common.SearchScreen;
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class LaunchScreenToHotelTest extends PhoneTestCase {

	@Test
	public void testPopularLocationSelection() {
		waitForLaunchScreenToDisplay();

		CollectionLocation collectionLocation = new CollectionLocation();
		CollectionLocation.Location suggestion = new CollectionLocation.Location();
		String city = "San Francisco";

		suggestion.displayName = city;
		suggestion.shortName = city;
		suggestion.type = DeprecatedHotelSearchParams.SearchType.CITY.toString();

		CollectionLocation.LatLng latLng = new CollectionLocation.LatLng();
		latLng.lat = 0;
		latLng.lng = 0;
		suggestion.latLong = latLng;

		collectionLocation.location = suggestion;
		Events.post(new Events.LaunchCollectionItemSelected(collectionLocation, null, ""));
		// Assert that the results screen is displayed
		HotelResultsScreen.waitForResultsLoaded(10);
		Common.pressBack();
		// Assert that the search screen is displayed with the correct search params
		SearchScreen.selectDestinationTextView().check(ViewAssertions.matches(ViewMatchers.withText("San Francisco")));
		SearchScreen.selectGuestsButton().check(ViewAssertions.matches(ViewMatchers.withText("2 guests")));
		LocalDate checkIn = LocalDate.now().plusDays(1);
		LocalDate checkOut = LocalDate.now().plusDays(2);
		String expectedCheckInDate = LocaleBasedDateFormatUtils.localDateToMMMd(checkIn);
		String expectedCheckoutDate = LocaleBasedDateFormatUtils.localDateToMMMd(checkOut);
		String expected = expectedCheckInDate + " - " + expectedCheckoutDate + " (1 night)";
		SearchScreen.selectDateButton().check(ViewAssertions.matches(ViewMatchers.withText(expected)));
	}


	@Test
	public void testSeeMore() throws Throwable {
		waitForLaunchScreenToDisplay();

		CollectionLocation collectionLocation = new CollectionLocation();
		CollectionLocation.Location suggestion = new CollectionLocation.Location();
		suggestion.type = DeprecatedHotelSearchParams.SearchType.MY_LOCATION.toString();

		CollectionLocation.LatLng coordinates = new CollectionLocation.LatLng();
		coordinates.lat = 32.71444d;
		coordinates.lng = -117.16237d;
		suggestion.latLong = coordinates;

		collectionLocation.location = suggestion;
		Events.post(new Events.LaunchCollectionItemSelected(collectionLocation, null, ""));
		// Assert that the results screen is displayed
		HotelResultsScreen.waitForResultsLoaded(10);
		Common.pressBack();
		// Test that searching still works
		SearchScreen.searchButton().perform(click());
	}

	private void waitForLaunchScreenToDisplay() {
		EspressoUtils.waitForViewNotYetInLayoutToDisplay(withId(R.id.launch_toolbar), 10, TimeUnit.SECONDS);
	}

}
