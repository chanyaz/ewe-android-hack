package com.expedia.bookings.utils;

import java.math.BigDecimal;
import java.util.List;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.lx.LXTicketType;
import com.expedia.bookings.data.lx.Ticket;

public class LXUtils {
	public static Money getTotalAmount(List<Ticket> selectedTickets) {
		Money totalMoney = new Money();

		if (CollectionUtils.isEmpty(selectedTickets)) {
			//Should be invoked with at least 1 selected ticket!
			return totalMoney;
		}

		for (Ticket ticket : selectedTickets) {
			BigDecimal amountDueForTickets = BigDecimal.ZERO;
			if (ticket.prices == null || ticket.count == 0) {
				amountDueForTickets = ticket.money.getAmount().multiply(BigDecimal.valueOf(ticket.count));
			}
			else {
				for (Ticket.LxTicketPrices price : ticket.prices) {
					if (ticket.count == price.travellerNum) {
						if (price.groupPrice != null) {
							amountDueForTickets = new BigDecimal(price.groupPrice);
						}
						else  {
							amountDueForTickets = price.money.getAmount().multiply(BigDecimal.valueOf(price.travellerNum));
						}
					}
				}
			}
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

	public static int getTicketTypeCount(List<Ticket> selectedTickets, LXTicketType ticketType) {
		int ticketTypeCount = 0;

		if (CollectionUtils.isEmpty(selectedTickets)) {
			//Should be invoked with at least 1 selected ticket!
			return ticketTypeCount;
		}

		for (Ticket ticket : selectedTickets) {
			if (ticket.code == ticketType) {
				ticketTypeCount += ticket.count;
			}
		}

		return ticketTypeCount;
	}

	public static String whitelistAlphanumericFromCategoryKey(String categoryKey) {
		return categoryKey.replaceAll("[^a-zA-Z0-9]", "");
	}

}
