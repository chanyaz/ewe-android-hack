package com.expedia.bookings.data.lx;

import com.expedia.bookings.data.Money;

public class Ticket {
	public String ticketId;
	public LXTicketType code;
	public String restrictionText;
	public String amount;
	public Money money;
	public int defaultTicketCount;
	// Count is manipulated from the ticket picker. But this is send back in create trip/ checkout api response.
	public int count;
}
