package com.expedia.bookings.unit;

import java.math.BigDecimal;
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
			"{\"code\": \"Adult\",\"count\": \"3\", \"ticketId\": \"90042\", \"name\": \"Adult\", \"restrictionText\": \"13+ years\", \"price\": \"$130\", \"originalPrice\": \"$145\", \"amount\": \"130\", \"originalAmount\": \"145\", \"displayName\": null, \"defaultTicketCount\": 2 }",
			Ticket.class);
		adultTicket.money = new Money(adultTicket.amount, "USD");
		adultTicket.originalPriceMoney = new Money(adultTicket.originalAmount, "USD");
		Ticket childTicket = gson.fromJson(
			"{\"code\": \"Child\",\"count\": \"1\", \"ticketId\": \"90043\", \"name\": \"Child\", \"restrictionText\": \"4-12 years\", \"price\": \"$110\", \"originalPrice\": \"$120\", \"amount\": \"110\", \"originalAmount\": \"120\", \"displayName\": null, \"defaultTicketCount\": 0 }",
			Ticket.class);
		childTicket.money = new Money(childTicket.amount, "USD");
		childTicket.originalPriceMoney = new Money(childTicket.originalAmount, "USD");
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

	private List<Ticket> getSelectedTicketsWithPrices(Gson gson) {
		List<Ticket> selectedTickets = new ArrayList<>();

		Ticket adultTicket = gson.fromJson(
				"{\"code\": \"Adult\",\"count\": \"3 \", \"ticketId\": \"90042\", \"name\": \"Adult\", \"restrictionText\": \"13+ years\", \"price\": \"$130\", \"originalPrice\": \"$145\", \"amount\": \"130\", \"originalAmount\": \"145\", \"displayName\": null, \"defaultTicketCount\": 2, \"prices\": [{\"originalPrice\": \"$145\", \"travellerNum\": 3, \"amount\": \"125\", \"originalAmount\": \"145\", \"price\": \"$125\"}] }",
				Ticket.class);
		adultTicket.money = new Money(adultTicket.amount, "USD");
		adultTicket.originalPriceMoney = new Money(adultTicket.originalAmount, "USD");

		List<Ticket.LxTicketPrices> adultPrices = new ArrayList<>();
		Ticket.LxTicketPrices adultPrice = adultTicket.prices.get(0);
		adultPrice.money = new Money(adultPrice.amount, "USD");
		adultPrice.originalPriceMoney = new Money(adultPrice.originalAmount, "USD");
		adultPrices.add(adultPrice);
		adultTicket.prices = adultPrices;

		Ticket childTicket = gson.fromJson(
				"{\"code\": \"Child\",\"count\": \"0\", \"ticketId\": \"90043\", \"name\": \"Child\", \"restrictionText\": \"4-12 years\", \"price\": \"$110\", \"originalPrice\": \"$120\", \"amount\": \"110\", \"originalAmount\": \"120\", \"displayName\": null, \"defaultTicketCount\": 0, \"prices\": [{\"originalPrice\": \"$120\", \"travellerNum\": 3, \"amount\": \"100\", \"originalAmount\": \"120\", \"price\": \"$100\"}] }",
				Ticket.class);
		childTicket.money = new Money(childTicket.amount, "USD");
		childTicket.originalPriceMoney = new Money(childTicket.originalAmount, "USD");

		List<Ticket.LxTicketPrices> childPrices = new ArrayList<>();
		Ticket.LxTicketPrices childPrice = childTicket.prices.get(0);
		childPrice.money = new Money(childPrice.amount, "USD");
		childPrice.originalPriceMoney = new Money(childPrice.originalAmount, "USD");
		childPrices.add(childPrice);
		childTicket.prices = childPrices;

		selectedTickets.add(adultTicket);
		selectedTickets.add(childTicket);
		return selectedTickets;
	}

	@Test
	public void testGetTotalOriginalAmount() {
		Gson gson = new GsonBuilder().create();
		List<Ticket> selectedTickets = getSelectedTickets(gson);

		Assert.assertEquals(new Money("555", "USD"), LXUtils.getTotalOriginalAmount(selectedTickets));

		selectedTickets.clear();
		selectedTickets = getSelectedTicketsWithPrices(gson);
		Assert.assertEquals(new Money("435", "USD"), LXUtils.getTotalOriginalAmount(selectedTickets));

		selectedTickets.clear();
		Assert.assertEquals(new Money(), LXUtils.getTotalAmount(selectedTickets));

		Assert.assertEquals(new Money(), LXUtils.getTotalAmount(null));
	}

	@Test
	public void testGetDiscountPercentValue() {
		Assert.assertEquals(17, LXUtils.getDiscountPercentValue(BigDecimal.valueOf(120), BigDecimal.valueOf(145)));
		Assert.assertEquals(7, LXUtils.getDiscountPercentValue(BigDecimal.valueOf(135), BigDecimal.valueOf(145)));
		Assert.assertEquals(0, LXUtils.getDiscountPercentValue(BigDecimal.valueOf(120), BigDecimal.valueOf(0)));
	}
}
