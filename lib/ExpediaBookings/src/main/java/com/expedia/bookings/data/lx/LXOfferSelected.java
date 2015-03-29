package com.expedia.bookings.data.lx;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import com.expedia.bookings.utils.DateUtils;
import com.expedia.bookings.utils.LXUtils;

public class LXOfferSelected {
	private String activityId;
	private String activityItemId;
	private String activityDate;
	private List<LXTicketSelected> tickets = new ArrayList<>();
	private boolean allDayActivity;
	private String amount;

	public LXOfferSelected(String activityId, Offer offer, Map<Ticket, Integer> selectedTickets) {
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

		this.activityId = activityId;
		this.amount = LXUtils.getTotalAmount(selectedTickets).getAmount().setScale(2).toString();
		this.activityDate = DateUtils.toYYYYMMTddhhmmss(activityDate);
		this.activityItemId = offer.id;
		this.allDayActivity = offer.availabilityInfoOfSelectedDate.availabilities.allDayActivity;
	}
}
