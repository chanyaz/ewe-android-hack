package com.expedia.bookings.unit;

import org.joda.time.LocalDate;
import org.junit.Test;

import com.expedia.bookings.data.lx.LXSearchParams;
import com.expedia.bookings.data.lx.LXSearchParamsBuilder;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

public class LXSearchParamsBuilderTests {

	@Test
	public void testLocationFilled() {
		String location = "New York";
		LXSearchParamsBuilder paramsBuilder = new LXSearchParamsBuilder();
		paramsBuilder.location(location);
		LXSearchParams params = paramsBuilder.build();

		assertEquals(location, params.location);
	}

	@Test
	public void testStartAndEndDates() {
		LXSearchParamsBuilder paramsBuilder = new LXSearchParamsBuilder();
		LocalDate start = LocalDate.now();
		LocalDate end = LocalDate.now().plusDays(14);
		paramsBuilder.startDate(start);
		paramsBuilder.endDate(end);

		LXSearchParams params = paramsBuilder.build();
		assertEquals(start, params.startDate);
		assertEquals(end, params.endDate);
	}

	@Test
	public void testEmptyEndDate() {
		LXSearchParamsBuilder paramsBuilder = new LXSearchParamsBuilder();
		paramsBuilder.location("SFO");
		paramsBuilder.startDate(LocalDate.now());

		LXSearchParams params = new LXSearchParams();
		assertNull(params.endDate);
	}
}
