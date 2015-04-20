package com.expedia.bookings.utils;

import java.math.BigDecimal;
import java.util.List;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.lx.Ticket;

public class LXUtils {
	public static Money getTotalAmount(List<Ticket> selectedTickets) {
		Money totalMoney = new Money();

		if (CollectionUtils.isEmpty(selectedTickets)) {
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

		if (CollectionUtils.isEmpty(selectedTickets)) {
			//Should be invoked with at least 1 selected ticket!
			return ticketCount;
		}

		for (Ticket ticket : selectedTickets) {
			ticketCount += ticket.count;
		}

		return ticketCount;
	}
}
