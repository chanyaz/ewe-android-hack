package com.expedia.bookings.unit;

import org.joda.time.LocalDate;
import org.junit.Test;

import com.expedia.bookings.data.lx.LXSearchParams;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

public class LXSearchParamsTests {

	@Test
	public void testLocationFilled() {
		String location = "New York";
		LXSearchParams searchParams = new LXSearchParams().location(location);
		assertEquals(location, searchParams.location);
	}

	@Test
	public void testStartAndEndDates() {
		LXSearchParams searchParams = new LXSearchParams().startDate(LocalDate.now()).endDate(
			LocalDate.now().plusDays(14));

		assertEquals(LocalDate.now(), searchParams.startDate);
		assertEquals(LocalDate.now().plusDays(14), searchParams.endDate);
	}

	@Test
	public void testEmptyEndDate() {
		LXSearchParams params = new LXSearchParams();
		assertNull(params.endDate);
	}
}
