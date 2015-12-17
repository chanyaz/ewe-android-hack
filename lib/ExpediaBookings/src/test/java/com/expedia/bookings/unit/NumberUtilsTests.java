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
}
