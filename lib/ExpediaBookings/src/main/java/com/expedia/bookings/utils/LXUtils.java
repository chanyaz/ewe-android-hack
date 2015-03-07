package com.expedia.bookings.utils;

import java.math.BigDecimal;
import java.util.Map;

import com.expedia.bookings.data.lx.Ticket;

public class LXUtils {
	public static BigDecimal getTotalAmount(Map<Ticket, Integer> selectedTickets) {
		BigDecimal total = BigDecimal.ZERO;

		for (Map.Entry<Ticket, Integer> ticketAndCount : selectedTickets.entrySet()) {
			int ticketCount = ticketAndCount.getValue();
			Ticket ticket = ticketAndCount.getKey();
			total = total.add(ticket.amount.multiply(BigDecimal.valueOf(ticketCount)));
		}

		return total;
	}
}
