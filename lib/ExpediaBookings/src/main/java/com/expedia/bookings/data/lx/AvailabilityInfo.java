package com.expedia.bookings.data.lx;

import java.util.ArrayList;
import java.util.List;

public class AvailabilityInfo {
	public ActivityAvailabilities availabilities;
	public List<Ticket> tickets = new ArrayList<>();

	public Ticket getLowestTicket() {
		Ticket lowestTicket = null;
		for (Ticket ticket : tickets) {
			if (lowestTicket == null
				|| ticket.money.getAmount().compareTo(lowestTicket.money.getAmount()) < 0) {
				lowestTicket = ticket;
			}
		}
		return lowestTicket;
	}
}
