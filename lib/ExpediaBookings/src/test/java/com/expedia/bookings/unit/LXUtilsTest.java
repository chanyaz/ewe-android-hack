package com.expedia.bookings.unit;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.lx.LXTicketType;
import com.expedia.bookings.data.lx.Ticket;
import com.expedia.bookings.utils.LXUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class LXUtilsTest {
	@Test
	public void testGetTotalAmount() {
		Gson gson = new GsonBuilder().create();
		List<Ticket> selectedTickets = getSelectedTickets(gson);

		Assert.assertEquals(LXUtils.getTotalAmount(selectedTickets), new Money("500", "USD"));

		selectedTickets.clear();
		Assert.assertEquals(LXUtils.getTotalAmount(selectedTickets), new Money());

		Assert.assertEquals(LXUtils.getTotalAmount(null), new Money());
	}

	@Test
	public void testGetTicketTypeCount() {
		Gson gson = new GsonBuilder().create();
		List<Ticket> selectedTickets = getSelectedTickets(gson);

		Assert.assertEquals(LXUtils.getTicketTypeCount(selectedTickets, LXTicketType.Child), 1);
		Assert.assertEquals(LXUtils.getTicketTypeCount(selectedTickets, LXTicketType.Adult), 3);
	}

	private List<Ticket> getSelectedTickets(Gson gson) {
		List<Ticket> selectedTickets = new ArrayList<>();

		Ticket adultTicket = gson.fromJson(
			"{\"code\": \"Adult\",\"count\": \"3\", \"ticketId\": \"90042\", \"name\": \"Adult\", \"restrictionText\": \"13+ years\", \"price\": \"$130\", \"originalPrice\": \"\", \"amount\": \"130\", \"displayName\": null, \"defaultTicketCount\": 2 }",
			Ticket.class);
		adultTicket.money = new Money(adultTicket.amount, "USD");
		Ticket childTicket = gson.fromJson(
			"{\"code\": \"Child\",\"count\": \"1\", \"ticketId\": \"90043\", \"name\": \"Child\", \"restrictionText\": \"4-12 years\", \"price\": \"$110\", \"originalPrice\": \"\", \"amount\": \"110\", \"displayName\": null, \"defaultTicketCount\": 0 }",
			Ticket.class);
		childTicket.money = new Money(childTicket.amount, "USD");
		selectedTickets.add(adultTicket);
		selectedTickets.add(childTicket);
		return selectedTickets;
	}

	@Test
	public void testWhitelistAlphanumericFromCategoryKey() {
		final String expectedStringFirst = "HoponHopoff";
		final String expectedStringSecond = "WalkingBikeTours";

		final String actualStringFirst = "Hop-on Hop-off";
		final String actualStringSecond = "Walking & Bike Tours";

		Assert.assertEquals(expectedStringFirst, LXUtils.whitelistAlphanumericFromCategoryKey(actualStringFirst));
		Assert.assertEquals(expectedStringSecond, LXUtils.whitelistAlphanumericFromCategoryKey(actualStringSecond));
	}
}
