package com.expedia.bookings.utils;

import java.math.BigDecimal;
import java.util.Map;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.lx.Ticket;

public class LXUtils {
	public static Money getTotalAmount(Map<Ticket, Integer> selectedTickets) {
		Money totalMoney = new Money();

		if (selectedTickets == null || selectedTickets.entrySet().size() == 0) {
			//Should be invoked with at least 1 selected ticket!
			return totalMoney;
		}

		for (Map.Entry<Ticket, Integer> ticketAndCount : selectedTickets.entrySet()) {
			int ticketCount = ticketAndCount.getValue();
			Ticket ticket = ticketAndCount.getKey();
			BigDecimal amountDueForTickets = ticket.money.getAmount().multiply(BigDecimal.valueOf(ticketCount));
			totalMoney.setAmount(totalMoney.getAmount().add(amountDueForTickets));
		}

		//Currency code for all tickets is the same!
		String currencyCode = selectedTickets.keySet().iterator().next().money.getCurrency();
		totalMoney.setCurrency(currencyCode);

		return totalMoney;
	}
}
