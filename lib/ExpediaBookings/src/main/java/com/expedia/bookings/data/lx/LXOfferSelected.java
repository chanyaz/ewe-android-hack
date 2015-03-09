package com.expedia.bookings.data.lx;

import java.math.BigDecimal;
import java.util.List;

import org.joda.time.DateTime;

public class LXOfferSelected {
	public String activityId;
	public String offerId;
	public DateTime offerDate;
	public List<LXTicketSelected> tickets;
	public BigDecimal amount;
}

