package com.expedia.bookings.test.robolectric;

import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.expedia.bookings.data.LXState;
import com.expedia.bookings.data.lx.LXSearchParams;
import com.expedia.bookings.otto.Events;

@RunWith(RobolectricRunner.class)
public class LXStateTest {

	@Test
	public void testSearchParamsAvailable() {
		LocalDate expectedStart = LocalDate.now();
		LocalDate expectedEnd = expectedStart.plusDays(14);
		String expectedLocation = "Test";

		LXSearchParams params = new LXSearchParams();
		params.location = expectedLocation;
		params.startDate = expectedStart;
		params.endDate = expectedEnd;

		LXState lxState = new LXState();

		Events.post(new Events.LXNewSearchParamsAvailable(params));
		LXSearchParams stateSearchParams = lxState.searchParams;
		Assert.assertEquals(expectedLocation, stateSearchParams.location);
		Assert.assertEquals(expectedStart, stateSearchParams.startDate);
		Assert.assertEquals(expectedEnd, stateSearchParams.endDate);
	}

}
