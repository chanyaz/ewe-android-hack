package com.expedia.bookings.data.lx;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.utils.DateUtils;
import com.expedia.bookings.utils.LXUtils;

public class LXOfferSelected {
	public String activityId;
	public String activityItemId;
	public String activityDate;
	public List<LXTicketSelected> tickets = new ArrayList<>();
	public Money money;
	public boolean allDayActivity;

	public LXOfferSelected(Offer offer, Map<Ticket, Integer> selectedTickets) {
		DateTime activityDate = DateUtils
			.yyyyMMddHHmmssToDateTime(offer.availabilityInfoOfSelectedDate.availabilities.valueDate);

		for (Map.Entry<Ticket, Integer> ticketAndCount : selectedTickets.entrySet()) {
			int ticketCount = ticketAndCount.getValue();
			Ticket ticket = ticketAndCount.getKey();
			if (ticketCount > 0) {
				LXTicketSelected ticketSelected = new LXTicketSelected();
				ticketSelected.ticketId = ticket.ticketId;
				ticketSelected.count = ticketCount;
				ticketSelected.code = ticket.code;
				this.tickets.add(ticketSelected);
			}
		}
		this.money = LXUtils.getTotalAmount(selectedTickets);
		this.activityDate = DateUtils.toYYYYMMTddhhmmss(activityDate);
		this.activityItemId = offer.id;
		this.allDayActivity = offer.availabilityInfoOfSelectedDate.availabilities.allDayActivity;
	}
}
