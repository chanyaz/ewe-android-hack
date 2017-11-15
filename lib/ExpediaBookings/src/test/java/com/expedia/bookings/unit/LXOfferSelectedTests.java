package com.expedia.bookings.unit;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Test;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.lx.LXOfferSelected;
import com.expedia.bookings.data.lx.Offer;
import com.expedia.bookings.data.lx.Ticket;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import static junit.framework.Assert.assertEquals;

public class LXOfferSelectedTests {

	@Test
	public void testLXOfferSelectedSerialization() {
		//Serialize Nulls so that any additional fields added to LXOfferSelected are caught and this test fails!
		Gson gson = new GsonBuilder().serializeNulls().create();

		Offer offer = gson.fromJson("{\"id\": \"183619\", \"title\": \"2-Day New York Pass\", \"description\": \"\", \"currencySymbol\": \"$\", \"currencyDisplayedLeft\": true, \"freeCancellation\": true, \"duration\": \"2d\", \"discountPercentage\": null, \"directionality\": \"\", \"availabilityInfo\": [{\"availabilities\": {\"displayDate\": \"Tue, Feb 24\", \"valueDate\": \"2015-02-24 07:30:00\", \"allDayActivity\": false }, \"tickets\": [{\"code\": \"Adult\", \"ticketId\": \"90042\", \"name\": \"Adult\", \"restrictionText\": \"13+ years\", \"price\": \"$130\", \"originalPrice\": \"\", \"amount\": \"130\", \"displayName\": null, \"defaultTicketCount\": 2 }, {\"code\": \"Child\", \"ticketId\": \"90043\", \"name\": \"Child\", \"restrictionText\": \"4-12 years\", \"price\": \"$110\", \"originalPrice\": \"\", \"amount\": \"110\", \"displayName\": null, \"defaultTicketCount\": 0 } ] } ], \"direction\": null }", Offer.class);
		offer.updateAvailabilityInfoOfSelectedDate(new LocalDate(2015, 2, 24));

		List<Ticket> tickets = new ArrayList<>();
		Ticket ticket = offer.availabilityInfoOfSelectedDate.tickets.get(0);
		ticket.money = new Money(ticket.amount, "USD");
		ticket.count = 2;
		tickets.add(ticket);

		LXOfferSelected a = new LXOfferSelected("123456", offer, tickets, "region123", "2");

		String expectedSerializedJson = gson.toJson(a);
		assertEquals(expectedSerializedJson, "{\"activityId\":\"123456\",\"activityItemId\":\"183619\",\"activityDate\":\"2015-02-24T07:30:00\",\"tickets\":[{\"ticketId\":\"90042\",\"code\":\"Adult\",\"count\":2}],\"allDayActivity\":false,\"amount\":\"260.00\",\"regionId\":\"region123\",\"promotionId\":\"2\"}");
	}
}
