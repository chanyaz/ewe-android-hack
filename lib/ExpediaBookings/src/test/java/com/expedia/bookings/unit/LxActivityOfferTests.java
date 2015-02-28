package com.expedia.bookings.unit;

import org.junit.Test;

import com.expedia.bookings.data.lx.AvailabilityInfo;
import com.expedia.bookings.data.lx.Offer;
import com.expedia.bookings.utils.DateUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import static junit.framework.Assert.assertTrue;


public class LxActivityOfferTests {

	@Test
	public void testIsAvailableOnDate() {
		Gson gson = new GsonBuilder().create();

		final String rawOfferJson = "\t\t{\"id\": \"183619\", \"title\": \"2-Day New York Pass\", \"description\": \"\", \"currencySymbol\": \"$\", \"currencyDisplayedLeft\": true, \"freeCancellation\": true, \"duration\": \"2d\", \"discountPercentage\": null, \"directionality\": \"\", \"availabilityInfo\": [{\"availabilities\": {\"displayDate\": \"Tue, Feb 24\", \"valueDate\": \"2015-02-24 07:30:00\", \"allDayActivity\": false }, \"tickets\": [{\"code\": \"Adult\", \"ticketId\": \"90042\", \"name\": \"Adult\", \"restrictionText\": \"13+ years\", \"price\": \"$130\", \"originalPrice\": \"\", \"amount\": \"130\", \"displayName\": null, \"defaultTicketCount\": 2 }, {\"code\": \"Child\", \"ticketId\": \"90043\", \"name\": \"Child\", \"restrictionText\": \"4-12 years\", \"price\": \"$110\", \"originalPrice\": \"\", \"amount\": \"110\", \"displayName\": null, \"defaultTicketCount\": 0 } ] } ], \"direction\": null }";

		Offer parsedOffer = gson.fromJson(rawOfferJson, Offer.class);

		assertTrue(parsedOffer.isAvailableOnDate(DateUtils.yyyyMMddToLocalDate("2015-02-24")));
		assertTrue(!parsedOffer.isAvailableOnDate(DateUtils.yyyyMMddToLocalDate("2015-02-23")));
		assertTrue(!parsedOffer.isAvailableOnDate(DateUtils.yyyyMMddToLocalDate("2015-02-25")));
	}

	@Test
	public void testGetAvailabilityInfoOnDate() {
		Gson gson = new GsonBuilder().create();

		final String rawOfferJson = "\t\t{\"id\": \"183619\", \"title\": \"2-Day New York Pass\", \"description\": \"\", \"currencySymbol\": \"$\", \"currencyDisplayedLeft\": true, \"freeCancellation\": true, \"duration\": \"2d\", \"discountPercentage\": null, \"directionality\": \"\", \"availabilityInfo\": [{\"availabilities\": {\"displayDate\": \"Tue, Feb 24\", \"valueDate\": \"2015-02-24 07:30:00\", \"allDayActivity\": false }, \"tickets\": [{\"code\": \"Adult\", \"ticketId\": \"90042\", \"name\": \"Adult\", \"restrictionText\": \"13+ years\", \"price\": \"$130\", \"originalPrice\": \"\", \"amount\": \"130\", \"displayName\": null, \"defaultTicketCount\": 2 }, {\"code\": \"Child\", \"ticketId\": \"90043\", \"name\": \"Child\", \"restrictionText\": \"4-12 years\", \"price\": \"$110\", \"originalPrice\": \"\", \"amount\": \"110\", \"displayName\": null, \"defaultTicketCount\": 0 } ] } ], \"direction\": null }";

		Offer parsedOffer = gson.fromJson(rawOfferJson, Offer.class);

		AvailabilityInfo activityAvailabilityInfo = parsedOffer.getAvailabilityInfoOnDate(DateUtils.yyyyMMddToLocalDate("2015-02-24"));
		assertTrue(activityAvailabilityInfo.tickets.size() == 2);

		activityAvailabilityInfo = parsedOffer.getAvailabilityInfoOnDate(DateUtils.yyyyMMddToLocalDate("2015-02-23"));
		assertTrue(activityAvailabilityInfo == null);
	}
}
