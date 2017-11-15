package com.expedia.bookings.data.lx;

import java.util.ArrayList;
import java.util.List;

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
	private String regionId;
	private String promotionId;

	public LXOfferSelected(String activityId, Offer offer, List<Ticket> selectedTickets, String regionId, String promotionId) {
		DateTime activityDate = DateUtils
			.yyyyMMddHHmmssToDateTime(offer.availabilityInfoOfSelectedDate.availabilities.valueDate);

		for (Ticket ticket : selectedTickets) {
			if (ticket.count > 0) {
				LXTicketSelected ticketSelected = new LXTicketSelected();
				ticketSelected.ticketId = ticket.ticketId;
				ticketSelected.count = ticket.count;
				ticketSelected.code = ticket.code;
				this.tickets.add(ticketSelected);
			}
		}

		this.activityId = activityId;
		this.amount = LXUtils.getTotalAmount(selectedTickets).getAmount().setScale(2).toString();
		this.activityDate = DateUtils.toYYYYMMTddhhmmss(activityDate);
		this.activityItemId = offer.id;
		this.allDayActivity = offer.availabilityInfoOfSelectedDate.availabilities.allDayActivity;
		this.regionId = regionId;
		this.promotionId = promotionId;
	}
}
