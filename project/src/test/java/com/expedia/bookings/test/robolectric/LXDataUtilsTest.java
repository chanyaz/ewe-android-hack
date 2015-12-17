package com.expedia.bookings.test.robolectric;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

import android.content.Context;
import android.net.Uri;

import com.expedia.bookings.data.lx.LXSearchParams;
import com.expedia.bookings.data.lx.LXTicketType;
import com.expedia.bookings.data.lx.Ticket;
import com.expedia.bookings.utils.DateUtils;
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

	@Test
	public void testBuildLXSearchParamsFromDeeplinkSearch() {
		final String expectedURL = "expda://activitySearch?startDate=2015-08-08&location=San+Francisco";
		final String location = "San Francisco";
		final String startDate = DateUtils.localDateToyyyyMMdd(DateUtils.ensureDateIsTodayOrInFuture(DateUtils.yyyyMMddToLocalDate("2015-08-08")));

		LXSearchParams obtainedLxSearchParams = getLxSearchParamsFromDeeplink(expectedURL);

		LXSearchParams expectedLxSearchParams = new LXSearchParams();
		expectedLxSearchParams.location(location).startDate(DateUtils.yyyyMMddToLocalDate(startDate));

		assertEquals(expectedLxSearchParams.location, obtainedLxSearchParams.location);
		assertEquals(expectedLxSearchParams.startDate, obtainedLxSearchParams.startDate);
	}

	@Test
	public void testBuildLXSearchParamsFromDeeplinkSearchWithFilters() {
		final String expectedURL = "expda://activitySearch?startDate=2015-08-08&location=San+Francisco&filters=Private+Transfers|Shared+Transfers";
		final String location = "San Francisco";
		final String startDate = DateUtils.localDateToyyyyMMdd(DateUtils.ensureDateIsTodayOrInFuture(DateUtils.yyyyMMddToLocalDate("2015-08-08")));
		final String filters = "Private Transfers|Shared Transfers";

		LXSearchParams obtainedLxSearchParams = getLxSearchParamsFromDeeplink(expectedURL);

		LXSearchParams expectedLxSearchParams = new LXSearchParams();
		expectedLxSearchParams.filters(filters).location(location).startDate(DateUtils.yyyyMMddToLocalDate(startDate));

		assertEquals(expectedLxSearchParams.location, obtainedLxSearchParams.location);
		assertEquals(expectedLxSearchParams.filters, obtainedLxSearchParams.filters);
		assertEquals(expectedLxSearchParams.startDate, obtainedLxSearchParams.startDate);
	}

	@Test
	public void testBuildLXSearchParamsEmptyURL() {
		// URL with no params
		final String emptyParamsURL = "expda://activitySearch";

		// URL with no date.
		final String missingDateURL = "expda://activitySearch?location=San Francisco";

		LXSearchParams searchParamsFromEmptyParamsURL = getLxSearchParamsFromDeeplink(emptyParamsURL);
		LXSearchParams searchParamsFromMissingDateURL = getLxSearchParamsFromDeeplink(missingDateURL);


		// Default to today's date, in case of an incorrect URL.
		assertEquals(searchParamsFromEmptyParamsURL.startDate, LocalDate.now());
		assertEquals(searchParamsFromMissingDateURL.startDate, LocalDate.now());
	}

	private LXSearchParams getLxSearchParamsFromDeeplink(String expectedURL) {
		Uri data = Uri.parse(expectedURL);
		Set<String> queryData = data.getQueryParameterNames();
		return LXDataUtils.buildLXSearchParamsFromDeeplink(data, queryData);
	}
}
