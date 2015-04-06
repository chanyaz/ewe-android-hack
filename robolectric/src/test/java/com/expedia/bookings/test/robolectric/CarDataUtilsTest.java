package com.expedia.bookings.test.robolectric;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import com.expedia.bookings.utils.CarDataUtils;
import com.expedia.bookings.utils.Interval;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
public class CarDataUtilsTest {

	@Test
	public void testGetInfoCount() {
		Interval interval = new Interval();
		String s = CarDataUtils.getInfoCount(interval);
		assertNull(s);

		interval.addIgnoreZero(0);
		s = CarDataUtils.getInfoCount(interval);
		assertNull(s);

		// 1, 1 maps to "1"
		interval.addIgnoreZero(1);
		s = CarDataUtils.getInfoCount(interval);
		assertEquals("1", s);

		// 1, 4 maps to "1-4"
		interval.addIgnoreZero(4);
		s = CarDataUtils.getInfoCount(interval);
		assertEquals("1-4", s);
	}
}
