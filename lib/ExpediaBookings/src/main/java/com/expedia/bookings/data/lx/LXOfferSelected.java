package com.expedia.bookings.data.lx;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import com.expedia.bookings.utils.ApiDateUtils;
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
		DateTime activityDate = ApiDateUtils
			.yyyyMMddHHmmssToDateTime(offer.availabilityInfoOfSelectedDate.availabilities.valueDate);

		for (Ticket ticket : selectedTickets) {
			if (ticket.count > 0) {
				LXTicketSelected ticketSelected = new LXTicketSelected();
				ticketSelected.ticketId = ticket.ticketId;
				ticketSelected.code = ticket.code;
				if (ticket.prices != null && ticket.prices.get(0).groupPrice != null) { // to check if the offer is VBP per group config
					ticketSelected.count = 1;
					ticketSelected.travelerCount = ticket.count;
				}
				else {
					ticketSelected.count = ticket.count;
				}
				this.tickets.add(ticketSelected);
			}
		}

		this.activityId = activityId;
		this.amount = LXUtils.getTotalAmount(selectedTickets).getAmount().setScale(2).toString();
		this.activityDate = ApiDateUtils.toYYYYMMTddhhmmss(activityDate);
		this.activityItemId = offer.id;
		this.allDayActivity = offer.availabilityInfoOfSelectedDate.availabilities.allDayActivity;
		this.regionId = regionId;
		this.promotionId = promotionId;
	}

	public String getPromotionId() {
		return promotionId;
	}
}
