package com.expedia.bookings.data.lx;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import com.expedia.bookings.utils.DateUtils;
import com.expedia.bookings.utils.LXUtils;

public class LXOfferSelected {
	public String activityId;
	public String activityItemId;
	public String activityDate;
	public List<LXTicketSelected> tickets = new ArrayList<>();
	public BigDecimal amount;

	public LXOfferSelected(Offer offer, Map<Ticket, Integer> selectedTickets) {
		DateTime activityDate = DateUtils
			.yyyyMMddHHmmssToDateTime(offer.availabilityInfoOfSelectedDate.availabilities.valueDate);

		for (Map.Entry<Ticket, Integer> ticketAndCount : selectedTickets.entrySet()) {
			LXTicketSelected ticketSelected = new LXTicketSelected();
			int ticketCount = ticketAndCount.getValue();
			Ticket ticket = ticketAndCount.getKey();
			ticketSelected.ticketId = ticket.ticketId;
			ticketSelected.count = ticketCount;
			ticketSelected.code = ticket.code.toString();
			this.tickets.add(ticketSelected);
		}
		this.amount = LXUtils.getTotalAmount(selectedTickets);
		this.activityDate = DateUtils.toYYYYMMTddhhmmss(activityDate);
		this.activityItemId = offer.id;
	}

}
