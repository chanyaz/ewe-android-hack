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
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.lx.LxSearchParams;
import com.expedia.bookings.data.lx.LXTicketType;
import com.expedia.bookings.data.lx.SearchType;
import com.expedia.bookings.data.lx.Ticket;
import com.expedia.bookings.utils.DateUtils;
import com.expedia.bookings.utils.LXDataUtils;
import com.expedia.bookings.utils.Strings;

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
		final String startDate = DateUtils
			.localDateToyyyyMMdd(DateUtils.ensureDateIsTodayOrInFuture(DateUtils.yyyyMMddToLocalDate("2015-08-08")));

		LxSearchParams obtainedLxSearchParams = getLxSearchParamsFromDeeplink(expectedURL);

		LxSearchParams expectedLxSearchParams = (LxSearchParams) new LxSearchParams.Builder().location(location)
			.startDate(DateUtils.yyyyMMddToLocalDate(startDate)).endDate(DateUtils.yyyyMMddToLocalDate(startDate).plusDays(14)).build();

		assertEquals(expectedLxSearchParams.getLocation(), obtainedLxSearchParams.getLocation());
		assertEquals(expectedLxSearchParams.getActivityStartDate(), obtainedLxSearchParams.getActivityStartDate());
	}

	@Test
	public void testBuildLXSearchParamsFromDeeplinkSearchWithFilters() {
		final String expectedURL = "expda://activitySearch?startDate=2015-08-08&location=San+Francisco&filters=Private+Transfers|Shared+Transfers";
		final String location = "San Francisco";
		final String startDate = DateUtils
			.localDateToyyyyMMdd(DateUtils.ensureDateIsTodayOrInFuture(DateUtils.yyyyMMddToLocalDate("2015-08-08")));
		final String filters = "Private Transfers|Shared Transfers";

		LxSearchParams obtainedLxSearchParams = getLxSearchParamsFromDeeplink(expectedURL);

		LxSearchParams expectedLxSearchParams = (LxSearchParams) new LxSearchParams.Builder().filters(filters).location(location)
			.startDate(DateUtils.yyyyMMddToLocalDate(startDate)).endDate(DateUtils.yyyyMMddToLocalDate(startDate).plusDays(14)).build();

		assertEquals(expectedLxSearchParams.getLocation(), obtainedLxSearchParams.getLocation());
		assertEquals(expectedLxSearchParams.getFilters(), obtainedLxSearchParams.getFilters());
		assertEquals(expectedLxSearchParams.getActivityStartDate(), obtainedLxSearchParams.getActivityStartDate());
	}

	@Test
	public void testBuildLXSearchParamsEmptyURL() {
		// URL with no params
		final String emptyParamsURL = "expda://activitySearch";

		// URL with no date.
		final String missingDateURL = "expda://activitySearch?location=San Francisco";

		LxSearchParams searchParamsFromEmptyParamsURL = getLxSearchParamsFromDeeplink(emptyParamsURL);
		LxSearchParams searchParamsFromMissingDateURL = getLxSearchParamsFromDeeplink(missingDateURL);


		// Default to today's date, in case of an incorrect URL.
		assertEquals(searchParamsFromEmptyParamsURL.getActivityStartDate(), LocalDate.now());
		assertEquals(searchParamsFromMissingDateURL.getActivityStartDate(), LocalDate.now());
	}

	@Test
	public void lxSearchParamsFromHotelParams() {
		LocalDate checkinDate = new LocalDate();
		Location location = new Location();
		location.setCity("San francisco");
		location.setStateCode("SFO");
		LocalDate checkoutDate = new LocalDate().plusDays(14);
		String locationText = getContext().getResources().getString(
			R.string.lx_destination_TEMPLATE, location.getCity(),
			Strings.isEmpty(location.getStateCode()) ? location.getCountryCode() : location.getStateCode());
		// Expected params.
		LxSearchParams expectedSearchParams = (LxSearchParams) new LxSearchParams.Builder()
			.searchType(SearchType.EXPLICIT_SEARCH).location(locationText).startDate(checkinDate).endDate(checkoutDate)
			.build();

		LxSearchParams obtainedSearchParams = LXDataUtils.fromHotelParams(getContext(), checkinDate, location);

		assertEquals(expectedSearchParams.getLocation(), obtainedSearchParams.getLocation());
		assertEquals(expectedSearchParams.getSearchType(), obtainedSearchParams.getSearchType());
		assertEquals(expectedSearchParams.getActivityStartDate(), obtainedSearchParams.getActivityStartDate());
		assertEquals(expectedSearchParams.getActivityEndDate(), obtainedSearchParams.getActivityEndDate());

	}

	@Test
	public void getToolbarSearchDateTextTest() {
		LocalDate startDate = new LocalDate(2016, 4, 23);
		LocalDate endDate = new LocalDate(2016, 4, 23).plusDays(2);
		LxSearchParams searchParams = (LxSearchParams) new LxSearchParams.Builder().location("NewYork").startDate(startDate)
			.endDate(endDate).build();

		String toolbarSearchDateText = LXDataUtils.getToolbarSearchDateText(getContext(), searchParams, false);
		assertEquals(toolbarSearchDateText, "Apr 23 - Apr 25");

		String toolbarSearchDateTextContDesc = LXDataUtils.getToolbarSearchDateText(getContext(), searchParams, true);
		assertEquals(toolbarSearchDateTextContDesc, "Apr 23 to Apr 25");
	}

	private LxSearchParams getLxSearchParamsFromDeeplink(String expectedURL) {
		Uri data = Uri.parse(expectedURL);
		Set<String> queryData = data.getQueryParameterNames();
		return LXDataUtils.buildLXSearchParamsFromDeeplink(data, queryData);
	}

	@Test
	public void bindPriceAndTicketTypeTest() {
		TextView activityPrice = new TextView(getContext());
		TextView fromPriceTicketType = new TextView(getContext());
		LXDataUtils.bindPriceAndTicketType(getContext(), LXTicketType.Traveler, new Money("180", "USD"),
			new Money("0", "USD"), activityPrice, fromPriceTicketType);

		assertEquals(activityPrice.getText(), "$180");
		assertEquals(activityPrice.getContentDescription(), "Price is $180");
		assertEquals(fromPriceTicketType.getText(), "per traveler");

		LXDataUtils.bindPriceAndTicketType(getContext(), LXTicketType.Traveler, new Money("180", "USD"),
			new Money("220", "USD"), activityPrice, fromPriceTicketType);

		assertEquals(activityPrice.getText(), "$180");
		assertEquals(activityPrice.getContentDescription(), "Price is $180. Price before discount was $220");
		assertEquals(fromPriceTicketType.getText(), "per traveler");
	}
}
