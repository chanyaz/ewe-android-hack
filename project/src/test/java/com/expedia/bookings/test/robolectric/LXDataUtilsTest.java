package com.expedia.bookings.test.robolectric;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

import android.content.Context;

import com.expedia.bookings.data.lx.LXTicketType;
import com.expedia.bookings.data.lx.Ticket;
import com.expedia.bookings.utils.LXDataUtils;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricRunner.class)
public class LXDataUtilsTest {
	private Context getContext() {
		return RuntimeEnvironment.application;
	}

	private Ticket getAdultTicket(int count) {
		Ticket adultTicket = new Ticket();
		adultTicket.code = LXTicketType.Adult;
		adultTicket.count = count;
		return adultTicket;
	}

	private Ticket getChildTicket(int count) {
		Ticket childTicket = new Ticket();
		childTicket.code = LXTicketType.Child;
		childTicket.count = count;
		return childTicket;
	}

	@Test
	public void testTicketsCountSummary() {
		List<Ticket> tickets = new ArrayList<>();

		tickets.add(getAdultTicket(1));
		tickets.add(getChildTicket(1));
		assertEquals(LXDataUtils.ticketsCountSummary(getContext(), tickets), "1 Adult, 1 Child");

		tickets.add(getAdultTicket(1));
		assertEquals(LXDataUtils.ticketsCountSummary(getContext(), tickets), "2 Adults, 1 Child");

		tickets.add(getChildTicket(1));
		assertEquals(LXDataUtils.ticketsCountSummary(getContext(), tickets), "2 Adults, 2 Children");
	}

	@Test
	public void testTicketCountSummary() {
		assertEquals(LXDataUtils.ticketCountSummary(getContext(), LXTicketType.Adult, 0), "");
		assertEquals(LXDataUtils.ticketCountSummary(getContext(), LXTicketType.Adult, 1), "1 Adult");
		assertEquals(LXDataUtils.ticketCountSummary(getContext(), LXTicketType.Adult, 2), "2 Adults");
		assertEquals(LXDataUtils.ticketCountSummary(getContext(), LXTicketType.Adult, 5), "5 Adults");

		assertEquals(LXDataUtils.ticketCountSummary(getContext(), LXTicketType.Child, 0), "");
		assertEquals(LXDataUtils.ticketCountSummary(getContext(), LXTicketType.Child, 1), "1 Child");
		assertEquals(LXDataUtils.ticketCountSummary(getContext(), LXTicketType.Child, 2), "2 Children");
		assertEquals(LXDataUtils.ticketCountSummary(getContext(), LXTicketType.Child, 5), "5 Children");
	}

	@Test
	public void testTicketDisplayName() {
		assertEquals(LXDataUtils.ticketDisplayName(getContext(), LXTicketType.Adult), "Adult");
		assertEquals(LXDataUtils.ticketDisplayName(getContext(), LXTicketType.Child), "Child");
	}

	@Test
	public void testPerTicketTypeDisplayLabel() {
		assertEquals(LXDataUtils.perTicketTypeDisplayLabel(getContext(), LXTicketType.Adult), "per adult");
		assertEquals(LXDataUtils.perTicketTypeDisplayLabel(getContext(), LXTicketType.Child), "per child");
	}
}
