package com.expedia.bookings.unit;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.lx.Ticket;
import com.expedia.bookings.utils.LXUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class LXUtilsTest {
	@Test
	public void testGetTotalAmount() {
		Gson gson = new GsonBuilder().create();
		Map<Ticket, Integer> selectedTickets = new LinkedHashMap<>();

		Ticket adultTicket = gson.fromJson(
			"{\"code\": \"Adult\", \"ticketId\": \"90042\", \"name\": \"Adult\", \"restrictionText\": \"13+ years\", \"price\": \"$130\", \"originalPrice\": \"\", \"amount\": \"130\", \"displayName\": null, \"defaultTicketCount\": 2 }",
			Ticket.class);
		adultTicket.money = new Money(adultTicket.amount, "USD");
		Ticket childTicket = gson.fromJson(
			"{\"code\": \"Child\", \"ticketId\": \"90043\", \"name\": \"Child\", \"restrictionText\": \"4-12 years\", \"price\": \"$110\", \"originalPrice\": \"\", \"amount\": \"110\", \"displayName\": null, \"defaultTicketCount\": 0 }",
			Ticket.class);
		childTicket.money = new Money(childTicket.amount, "USD");
		selectedTickets.put(adultTicket, 3);
		selectedTickets.put(childTicket, 1);

		Assert.assertEquals(LXUtils.getTotalAmount(selectedTickets), new Money("500", "USD"));

		selectedTickets.clear();
		Assert.assertEquals(LXUtils.getTotalAmount(selectedTickets), new Money());

		Assert.assertEquals(LXUtils.getTotalAmount(null), new Money());
	}

	@Test
	public void testBestApplicableCategory() {
		final List<String> categoriesEn = new ArrayList<>();

		Assert.assertEquals(LXUtils.bestApplicableCategory(null), "");
		Assert.assertEquals(LXUtils.bestApplicableCategory(categoriesEn), "");

		categoriesEn.add("Category 1");
		categoriesEn.add("Multi-day & Extended Tours");
		categoriesEn.add("Category 3");
		Assert.assertEquals(LXUtils.bestApplicableCategory(categoriesEn), "Multi-day & Extended Tours");

		categoriesEn.add("Theme Parks");
		Assert.assertEquals(LXUtils.bestApplicableCategory(categoriesEn), "Theme Parks");

		categoriesEn.clear();
		categoriesEn.add("Category 1");
		categoriesEn.add("Category 3");
		Assert.assertEquals(LXUtils.bestApplicableCategory(categoriesEn), "Category 1");
	}
}
