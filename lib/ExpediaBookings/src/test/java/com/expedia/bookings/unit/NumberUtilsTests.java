package com.expedia.bookings.unit;

import java.math.BigDecimal;
import java.util.Locale;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import com.expedia.bookings.utils.NumberUtils;

public class NumberUtilsTests {
	@Rule
	public ExpectedException expectedEx = ExpectedException.none();

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

	@Test
	public void testLocaleBasedFormattedNumber() {
		setLocale("en", "US");
		Assert.assertEquals("4.2", NumberUtils.localeBasedFormattedNumber(4.2));
		Assert.assertEquals("3.6", NumberUtils.localeBasedFormattedNumber(3.6));

		setLocale("de", "DE");
		Assert.assertEquals("4,2", NumberUtils.localeBasedFormattedNumber(4.2));

		setLocale("zh", "CN");
		Assert.assertEquals("4.2", NumberUtils.localeBasedFormattedNumber(4.2));

		setLocale("es", "MX");
		Assert.assertEquals("4.2", NumberUtils.localeBasedFormattedNumber(4.2));
		setLocale("en", "US");
	}

	private void setLocale(String lang, String region) {
		Locale.setDefault(new Locale(lang, region));
	}

	@Test
	public void testGetPercentagePaidWithPointsForOmniture() {
		Assert.assertEquals(10, NumberUtils.getPercentagePaidWithPointsForOmniture(BigDecimal.ONE, BigDecimal.TEN));

		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("Total cannot be zero while calculating percentage");
		NumberUtils.getPercentagePaidWithPointsForOmniture(BigDecimal.ONE, BigDecimal.ZERO);
	}
}
