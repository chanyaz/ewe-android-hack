package com.expedia.bookings.test.robolectric;

import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.expedia.bookings.data.LXState;
import com.expedia.bookings.data.lx.LxSearchParams;
import com.expedia.bookings.otto.Events;

@RunWith(RobolectricRunner.class)
public class LXStateTest {

	@Test
	public void testSearchParamsAvailable() {
		LocalDate expectedStart = LocalDate.now();
		LocalDate expectedEnd = expectedStart.plusDays(14);
		String expectedLocation = "Test";
		boolean expectedModQualification = true;

		LxSearchParams params = (LxSearchParams) new LxSearchParams.Builder().location(expectedLocation)
			.modQualified(true).startDate(expectedStart).endDate(expectedEnd).build();

		LXState lxState = new LXState();

		Events.post(new Events.LXNewSearchParamsAvailable(params));
		LxSearchParams stateSearchParams = lxState.searchParams;
		Assert.assertEquals(expectedLocation, stateSearchParams.getLocation());
		Assert.assertEquals(expectedStart, stateSearchParams.getActivityStartDate());
		Assert.assertEquals(expectedEnd, stateSearchParams.getActivityEndDate());
		Assert.assertEquals(expectedModQualification, stateSearchParams.getModQualified());
	}

}
