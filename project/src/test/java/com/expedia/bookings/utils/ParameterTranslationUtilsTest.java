package com.expedia.bookings.utils;


import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;

import android.net.Uri;

import com.expedia.bookings.test.robolectric.RobolectricRunner;
import com.expedia.util.ParameterTranslationUtils;

import junit.framework.Assert;

@RunWith(RobolectricRunner.class)
public class ParameterTranslationUtilsTest {

	@Test
	public void testHotelSearchLink() {
		String[] urls = {
			"https://www.expedia.com/mobile/deeplink/Hotel-Search",
			"https://www.expedia.com/mobile/deeplink/Hotel-Search?startDate=12/27/2018",
			"https://www.expedia.com/mobile/deeplink/Hotel-Search?startDate=12/27/2018&endDate=01/03/2019",
			"https://www.expedia.com/mobile/deeplink/Hotel-Search?startDate=12/27/2018&endDate=01/03/2019&adults=3",
			"https://www.expedia.com/mobile/deeplink/Hotel-Search?startDate=12/27/2018&endDate=01/03/2019&adults=3&regionId=6059241",
			"https://www.expedia.com/mobile/deeplink/Hotel-Search?sort=price",
			"https://www.expedia.com/mobile/deeplink/Hotel-Search?sort=guestRating",
			"https://www.expedia.com/mobile/deeplink/Hotel-Search?sort=deals",
			"https://www.expedia.com/mobile/deeplink/Hotel-Search?sort=recommended",
			"https://www.expedia.com/mobile/deeplink/Hotel-Search?sort=mostPopular",
			"https://www.expedia.com/mobile/deeplink/Hotel-Search?dummyKey=dummyValue"
		};

		String[] formattedUrls = {
			"https://www.expedia.com/mobile/deeplink/Hotel-Search",
			"https://www.expedia.com/mobile/deeplink/Hotel-Search?checkInDate=2018-12-27",
			"https://www.expedia.com/mobile/deeplink/Hotel-Search?checkInDate=2018-12-27&checkOutDate=2019-01-03",
			"https://www.expedia.com/mobile/deeplink/Hotel-Search?checkInDate=2018-12-27&checkOutDate=2019-01-03&numAdults=3",
			"https://www.expedia.com/mobile/deeplink/Hotel-Search?checkInDate=2018-12-27&checkOutDate=2019-01-03&numAdults=3&location=ID6059241",
			"https://www.expedia.com/mobile/deeplink/Hotel-Search?sortType=Price",
			"https://www.expedia.com/mobile/deeplink/Hotel-Search?sortType=Rating",
			"https://www.expedia.com/mobile/deeplink/Hotel-Search?sortType=Discounts",
			"https://www.expedia.com/mobile/deeplink/Hotel-Search?sortType=Recommended",
			"https://www.expedia.com/mobile/deeplink/Hotel-Search?sortType=Recommended",
			"https://www.expedia.com/mobile/deeplink/Hotel-Search?dummyKey=dummyValue",
		};

		for (int i = 0; i < urls.length; i++) {
			Uri uriActual = ParameterTranslationUtils.hotelSearchLink(Uri.parse(urls[i]));
			Uri uriExpected = Uri.parse(formattedUrls[i]);
			assertUrisEquivalent(uriExpected, uriActual);
		}
	}

	@Test
	public void testFlightSearchLink() {
		String[] urls = {
			"https://www.expedia.com/mobile/deeplink/Flights-Search",
			"https://www.expedia.com/mobile/deeplink/Flights-Search?trip=oneway&leg1=from:Seattle, WA (SEA-Seattle - Tacoma Intl.)",
			"https://www.expedia.com/mobile/deeplink/Flights-Search?trip=oneway&leg1=from:Seattle, WA (SEA-Seattle - Tacoma Intl.),to:BKK",
			"https://www.expedia.com/mobile/deeplink/Flights-Search?trip=somethingwrong&leg1=from:Seattle, WA (SEA-Seattle - Tacoma Intl.),to:BKK&leg2=from:BKK,to:Seattle, WA (SEA-Seattle - Tacoma Intl.)",

			"https://www.expedia.com/mobile/deeplink/Flights-Search?trip=oneway&leg1=from:Seattle, WA (SEA-Seattle - Tacoma Intl.),to:BKK,departure:09/27/2017TANYT",
			"https://www.expedia.com/mobile/deeplink/Flights-Search?trip=roundtrip&leg1=from:Seattle, WA (SEA-Seattle - Tacoma Intl.),to:BKK,departure:09/27/2017TANYT&leg2=from:BKK,to:Seattle, WA (SEA-Seattle - Tacoma Intl.),departure:10/11/2017TANYT",
			"https://www.expedia.com/mobile/deeplink/Flights-Search?trip=roundtrip&leg1=from:Seattle, WA (SEA-Seattle - Tacoma Intl.),to:BKK,departure:09/27/2017TANYT&leg2=from:BKK,to:Seattle, WA (SEA-Seattle - Tacoma Intl.),departure:10/11/2017TANYT&passengers=children:0,adults:1,seniors:0,infantinlap:Y",
			"https://www.expedia.com/mobile/deeplink/Flights-Search?dummyKey=dummyValue"
		};

		String[] formattedUrls = {
			"https://www.expedia.com/mobile/deeplink/Flights-Search",
			"https://www.expedia.com/mobile/deeplink/Flights-Search",
			"https://www.expedia.com/mobile/deeplink/Flights-Search",
			"https://www.expedia.com/mobile/deeplink/Flights-Search",

			"https://www.expedia.com/mobile/deeplink/Flights-Search?origin=SEA&destination=BKK&departureDate=2017-09-27",
			"https://www.expedia.com/mobile/deeplink/Flights-Search?origin=SEA&destination=BKK&departureDate=2017-09-27&returnDate=2017-10-11",
			"https://www.expedia.com/mobile/deeplink/Flights-Search?origin=SEA&destination=BKK&departureDate=2017-09-27&returnDate=2017-10-11&numAdults=1",
			"https://www.expedia.com/mobile/deeplink/Flights-Search?dummyKey=dummyValue"
		};

		for (int i = 0; i < urls.length; i++) {
			Uri uriActual = ParameterTranslationUtils.flightSearchLink(Uri.parse(urls[i]));
			Uri uriExpected = Uri.parse(formattedUrls[i]);
			assertUrisEquivalent(uriExpected, uriActual);
		}
	}

	@Test
	public void testCarSearchLink() {
		String[] urls = {
			"https://www.expedia.com/mobile/deeplink/carsearch",
			"https://www.expedia.com/mobile/deeplink/carsearch?locn=Bangkok, Thailand (BKK-All Airports)&date1=09/26/2017&date2=10/11/2017&time2=500PM",
			"https://www.expedia.com/mobile/deeplink/carsearch?locn=Bangkok, Thailand (BKK-All Airports)&date1=09/26/2017&date2=10/11/2017&time1=700&time2=500PM",
			"https://www.expedia.com/mobile/deeplink/carsearch?date1=09/26/2017&time1=700PM",
			"https://www.expedia.com/mobile/deeplink/carsearch?date1=09/26/2017&date2=10/11/2017&time1=700PM&time2=500PM",
			"https://www.expedia.com/mobile/deeplink/carsearch?locn=Bangkok, Thailand (BKK-All Airports)&date1=09/26/2017&date2=10/11/2017&time1=700PM&time2=500PM",
			"https://www.expedia.com/mobile/deeplink/carsearch?locn=Bangkok, Thailand (BKK-All Airports)&date1=09/26/2017&date2=10/11/2017&time1=700AM&time2=500PM",
			"https://www.expedia.com/mobile/deeplink/carsearch?dummyKey=dummyValue",
		};

		String[] formattedUrls = {
			"https://www.expedia.com/mobile/deeplink/carsearch",
			"https://www.expedia.com/mobile/deeplink/carsearch?pickupLocation=BKK&dropoffDateTime=2017-10-11T17:00:00",
			"https://www.expedia.com/mobile/deeplink/carsearch?pickupLocation=BKK&dropoffDateTime=2017-10-11T17:00:00",
			"https://www.expedia.com/mobile/deeplink/carsearch?pickupDateTime=2017-09-26T19:00:00",
			"https://www.expedia.com/mobile/deeplink/carsearch?pickupDateTime=2017-09-26T19:00:00&dropoffDateTime=2017-10-11T17:00:00",
			"https://www.expedia.com/mobile/deeplink/carsearch?pickupLocation=BKK&pickupDateTime=2017-09-26T19:00:00&dropoffDateTime=2017-10-11T17:00:00",
			"https://www.expedia.com/mobile/deeplink/carsearch?pickupLocation=BKK&pickupDateTime=2017-09-26T07:00:00&dropoffDateTime=2017-10-11T17:00:00",
			"https://www.expedia.com/mobile/deeplink/carsearch?dummyKey=dummyValue"
		};

		for (int i = 0; i < urls.length; i++) {
			Uri uriActual = ParameterTranslationUtils.carSearchLink(Uri.parse(urls[i]));
			Uri uriExpected = Uri.parse(formattedUrls[i]);
			assertUrisEquivalent(uriExpected, uriActual);
		}
	}

	@Test
	public void testLXSearchLink() {
		String[] urls = {
			"https://www.expedia.com/mobile/deeplink/things-to-do/search",
			"https://www.expedia.com/mobile/deeplink/things-to-do/search?startDate=09/26/2017",
			"https://www.expedia.com/mobile/deeplink/things-to-do/search?location=Bangkok (and vicinity), Thailand&startDate=09/26/2017",
			"https://www.expedia.com/mobile/deeplink/things-to-do/search?location=Bangkok (and vicinity), Thailand&startDate=09/26/2017&endDate=10/02/2017&categories=Nightlife|Cruises%20%26%20Water%20Tours",
			"https://www.expedia.com/mobile/deeplink/things-to-do/search?dummyKey=dummyValue"
		};

		String[] formattedUrls = {
			"https://www.expedia.com/mobile/deeplink/things-to-do/search",
			"https://www.expedia.com/mobile/deeplink/things-to-do/search?startDate=2017-09-26",
			"https://www.expedia.com/mobile/deeplink/things-to-do/search?location=Bangkok (and vicinity)&startDate=2017-09-26",
			"https://www.expedia.com/mobile/deeplink/things-to-do/search?location=Bangkok (and vicinity)&startDate=2017-09-26&endDate=2017-10-02&filters=Nightlife|Cruises%20%26%20Water%20Tours",
			"https://www.expedia.com/mobile/deeplink/things-to-do/search?dummyKey=dummyValue"
		};

		for (int i = 0; i < urls.length; i++) {
			Uri uriActual = ParameterTranslationUtils.lxSearchLink(Uri.parse(urls[i]));
			Uri uriExpected = Uri.parse(formattedUrls[i]);
			assertUrisEquivalent(uriExpected, uriActual);
		}
	}

	private void assertUrisEquivalent(Uri uriExpected, Uri uriActual) {
		Set<String> queryDataActual = uriActual.getQueryParameterNames();
		Set<String> queryDataExpected = uriExpected.getQueryParameterNames();

		Assert.assertEquals(queryDataExpected.size(), queryDataActual.size());
		for (String query: queryDataActual) {
			Assert.assertEquals(uriExpected.getQueryParameter(query),uriActual.getQueryParameter(query));
		}
	}
}
