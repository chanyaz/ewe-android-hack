package com.expedia.bookings.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

import android.content.Context;

import com.expedia.bookings.test.robolectric.RobolectricRunner;
import com.expedia.bookings.utils.DateFormatUtils;

import static junit.framework.Assert.assertEquals;

@RunWith(RobolectricRunner.class)
public class DateFormatUtilsTest {

	private Context getContext() {
		return RuntimeEnvironment.application;
	}

	@Test
	@RunForBrands(brands = {MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY, MultiBrand.AIRASIAGO,
		MultiBrand.VOYAGES, MultiBrand.WOTIF, MultiBrand.LASTMINUTE, MultiBrand.EBOOKERS})
	public void testyyyyMMddHHToDayDateFormat() {
		String checkInDate = "2018-10-11";
		String checkOutDate = "2018-10-15";

		String obtained = DateFormatUtils.formatPackageDateRange(getContext(), checkInDate, checkOutDate);
		assertEquals(obtained, "Thu Oct 11, 2018 - Mon Oct 15, 2018");
	}

}

