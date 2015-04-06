package com.expedia.bookings.utils;

import java.math.BigDecimal;
import java.util.List;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.lx.Ticket;

public class LXUtils {
	public static Money getTotalAmount(List<Ticket> selectedTickets) {
		Money totalMoney = new Money();

		if (selectedTickets == null || selectedTickets.size() == 0) {
			//Should be invoked with at least 1 selected ticket!
			return totalMoney;
		}

		for (Ticket ticket : selectedTickets) {
			BigDecimal amountDueForTickets = ticket.money.getAmount().multiply(BigDecimal.valueOf(ticket.count));
			totalMoney.setAmount(totalMoney.getAmount().add(amountDueForTickets));
		}

		//Currency code for all tickets is the same!
		String currencyCode = selectedTickets.get(0).money.getCurrency();
		totalMoney.setCurrency(currencyCode);

		return totalMoney;
	}

	public static int getTotalTicketCount(List<Ticket> selectedTickets) {
		int ticketCount = 0;

		if (selectedTickets == null || selectedTickets.size() == 0) {
			//Should be invoked with at least 1 selected ticket!
			return ticketCount;
		}

		for (Ticket ticket : selectedTickets) {
			ticketCount += ticket.count;
		}

		return ticketCount;
	}

	public static String bestApplicableCategory(List<String> activitiesEN) {
		//We are comparing English Category Strings with English Prioritized Strings!
		final String[] prioritizedCategoriesEN = {
			"Hop-on Hop-off",
			"Attractions",
			"Adventures",
			"Tours & Sightseeing",
			"Food & Drink",
			"Show & Sport Tickets",
			"Theme Parks",
			"Private Tours",
			"Cruises & Water Tours",
			"Multi-day & Extended Tours",
		};

		if (activitiesEN == null || activitiesEN.size() == 0) {
			return "";
		}

		//Choose the first category in the prioritized list which exists in the activities list passed
		for (int iCategory = 0; iCategory < prioritizedCategoriesEN.length; iCategory++) {
			if (activitiesEN.contains(prioritizedCategoriesEN[iCategory])) {
				return prioritizedCategoriesEN[iCategory];
			}
		}

		//None from the prioritized list found. Simply return the first category!
		return activitiesEN.iterator().next();
	}
}
