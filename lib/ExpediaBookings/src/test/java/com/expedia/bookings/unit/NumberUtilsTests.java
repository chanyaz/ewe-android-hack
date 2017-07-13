package com.expedia.bookings.unit;

import org.junit.Assert;
import org.junit.Test;

import com.expedia.bookings.utils.NumberUtils;

public class NumberUtilsTests {
	@Test
	public void testParseDoubleSafe() {
		Double d = NumberUtils.parseDoubleSafe("");
		Assert.assertNull(d);

		d = NumberUtils.parseDoubleSafe("0.005");
		Assert.assertEquals(0.005d, d.doubleValue(), 1e-10);

		d = NumberUtils.parseDoubleSafe("1");
		Assert.assertEquals(1d, d.doubleValue(), 1e-10);

		d = NumberUtils.parseDoubleSafe("garbage");
		Assert.assertNull(d);
	}

	@Test
	public void testRound() {
		float number = 4.52614f;
		Assert.assertEquals(5f, NumberUtils.round(number, 0), 1e-10);
		Assert.assertEquals(4.5f, NumberUtils.round(number, 1), 1e-10);
		Assert.assertEquals(4.53f, NumberUtils.round(number, 2), 1e-10);
		Assert.assertEquals(4.526f, NumberUtils.round(number, 3), 1e-10);
		Assert.assertEquals(4.5261f, NumberUtils.round(number, 4), 1e-10);
	}
}
