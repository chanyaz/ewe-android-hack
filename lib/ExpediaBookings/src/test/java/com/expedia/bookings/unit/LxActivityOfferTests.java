package com.expedia.bookings.unit;

import org.junit.Test;

import com.expedia.bookings.data.lx.AvailabilityInfo;
import com.expedia.bookings.data.lx.Offer;
import com.expedia.bookings.data.lx.OffersDetail;
import com.expedia.bookings.utils.ApiDateUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import static junit.framework.Assert.assertTrue;


public class LxActivityOfferTests {

	@Test
	public void testIsAvailableOnDate() {
		Gson gson = new GsonBuilder().create();

		final String rawOfferDetailsJson = "{\"offers\":[{\"id\":\"166367\",\"title\":\"8: 30AMEnglishCommentaryTour\",\"description\":\"\",\"currencySymbol\":\"$\",\"currencyDisplayedLeft\":true,\"freeCancellation\":true,\"duration\":\"10h\",\"durationInMillis\":36000000,\"discountPercentage\":null,\"directionality\":\"\",\"availabilityInfo\":[{\"availabilities\":{\"displayDate\":\"Wed,Mar18\",\"valueDate\":\"2015-03-18 08:30:00\",\"allDayActivity\":false},\"tickets\":[{\"code\":\"Adult\",\"ticketId\":\"76684\",\"name\":\"Adult\",\"restrictionText\":\"9+years\",\"restriction\":{\"type\":\"Age-Years\",\"max\":255,\"min\":9},\"price\":\"$151.81\",\"originalPrice\":\"\",\"amount\":\"151.81\",\"displayName\":null,\"defaultTicketCount\":2},{\"code\":\"Child\",\"ticketId\":\"76685\",\"name\":\"Child\",\"restrictionText\":\"3-8years\",\"restriction\":{\"type\":\"Age-Years\",\"max\":8,\"min\":3},\"price\":\"$121.45\",\"originalPrice\":\"\",\"amount\":\"121.45\",\"displayName\":null,\"defaultTicketCount\":0}]},{\"availabilities\":{\"displayDate\":\"Wed,Apr1\",\"valueDate\":\"2015-04-01 08:30:00\",\"allDayActivity\":false},\"tickets\":[{\"code\":\"Adult\",\"ticketId\":\"76684\",\"name\":\"Adult\",\"restrictionText\":\"9+years\",\"restriction\":{\"type\":\"Age-Years\",\"max\":255,\"min\":9},\"price\":\"$151.81\",\"originalPrice\":\"\",\"amount\":\"151.81\",\"displayName\":null,\"defaultTicketCount\":2},{\"code\":\"Child\",\"ticketId\":\"76685\",\"name\":\"Child\",\"restrictionText\":\"3-8years\",\"restriction\":{\"type\":\"Age-Years\",\"max\":8,\"min\":3},\"price\":\"$121.45\",\"originalPrice\":\"\",\"amount\":\"121.45\",\"displayName\":null,\"defaultTicketCount\":0}]}],\"direction\":null},{\"id\":\"166372\",\"title\":\"8: 30AMSpanishCommentaryTour\",\"description\":\"\",\"currencySymbol\":\"$\",\"currencyDisplayedLeft\":true,\"freeCancellation\":true,\"duration\":null,\"durationInMillis\":0,\"discountPercentage\":null,\"directionality\":\"\",\"availabilityInfo\":[{\"availabilities\":{\"displayDate\":\"Wed,Mar18\",\"valueDate\":\"2015-03-18 08:30:00\",\"allDayActivity\":false},\"tickets\":[{\"code\":\"Adult\",\"ticketId\":\"76703\",\"name\":\"Adult\",\"restrictionText\":\"9+years\",\"restriction\":{\"type\":\"Age-Years\",\"max\":255,\"min\":9},\"price\":\"$151.81\",\"originalPrice\":\"\",\"amount\":\"151.81\",\"displayName\":null,\"defaultTicketCount\":2},{\"code\":\"Child\",\"ticketId\":\"76704\",\"name\":\"Child\",\"restrictionText\":\"3-8years\",\"restriction\":{\"type\":\"Age-Years\",\"max\":8,\"min\":3},\"price\":\"$121.45\",\"originalPrice\":\"\",\"amount\":\"121.45\",\"displayName\":null,\"defaultTicketCount\":0}]},{\"availabilities\":{\"displayDate\":\"Wed,Apr1\",\"valueDate\":\"2015-04-01 08:30:00\",\"allDayActivity\":false},\"tickets\":[{\"code\":\"Adult\",\"ticketId\":\"76703\",\"name\":\"Adult\",\"restrictionText\":\"9+years\",\"restriction\":{\"type\":\"Age-Years\",\"max\":255,\"min\":9},\"price\":\"$151.81\",\"originalPrice\":\"\",\"amount\":\"151.81\",\"displayName\":null,\"defaultTicketCount\":2},{\"code\":\"Child\",\"ticketId\":\"76704\",\"name\":\"Child\",\"restrictionText\":\"3-8years\",\"restriction\":{\"type\":\"Age-Years\",\"max\":8,\"min\":3},\"price\":\"$121.45\",\"originalPrice\":\"\",\"amount\":\"121.45\",\"displayName\":null,\"defaultTicketCount\":0}]}],\"direction\":null}],\"priceFootnote\":\"*Taxesincluded\",\"sameDateSearch\":false}";

		OffersDetail parsedOffer = gson.fromJson(rawOfferDetailsJson, OffersDetail.class);

		assertTrue(parsedOffer.isAvailableOnDate(ApiDateUtils.yyyyMMddToLocalDate("2015-04-01")));
		assertTrue(!parsedOffer.isAvailableOnDate(ApiDateUtils.yyyyMMddToLocalDate("2015-02-23")));
		assertTrue(!parsedOffer.isAvailableOnDate(ApiDateUtils.yyyyMMddToLocalDate("2015-02-25")));
	}

	@Test
	public void testGetAvailabilityInfoOnDate() {
		Gson gson = new GsonBuilder().create();

		final String rawOfferJson = "\t\t{\"id\": \"183619\", \"title\": \"2-Day New York Pass\", \"description\": \"\", \"currencySymbol\": \"$\", \"currencyDisplayedLeft\": true, \"freeCancellation\": true, \"duration\": \"2d\", \"discountPercentage\": null, \"directionality\": \"\", \"availabilityInfo\": [{\"availabilities\": {\"displayDate\": \"Tue, Feb 24\", \"valueDate\": \"2015-02-24 07:30:00\", \"allDayActivity\": false }, \"tickets\": [{\"code\": \"Adult\", \"ticketId\": \"90042\", \"name\": \"Adult\", \"restrictionText\": \"13+ years\", \"price\": \"$130\", \"originalPrice\": \"\", \"amount\": \"130\", \"displayName\": null, \"defaultTicketCount\": 2 }, {\"code\": \"Child\", \"ticketId\": \"90043\", \"name\": \"Child\", \"restrictionText\": \"4-12 years\", \"price\": \"$110\", \"originalPrice\": \"\", \"amount\": \"110\", \"displayName\": null, \"defaultTicketCount\": 0 } ] } ], \"direction\": null }";

		Offer parsedOffer = gson.fromJson(rawOfferJson, Offer.class);

		AvailabilityInfo activityAvailabilityInfo = parsedOffer.updateAvailabilityInfoOfSelectedDate(ApiDateUtils.yyyyMMddToLocalDate("2015-02-24"));
		assertTrue(activityAvailabilityInfo.tickets.size() == 2);

		activityAvailabilityInfo = parsedOffer.updateAvailabilityInfoOfSelectedDate(ApiDateUtils.yyyyMMddToLocalDate("2015-02-23"));
		assertTrue(activityAvailabilityInfo == null);
	}
}
