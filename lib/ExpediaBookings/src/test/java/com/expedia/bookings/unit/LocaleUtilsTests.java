package com.expedia.bookings.unit;

import org.junit.Test;

import com.expedia.bookings.utils.LocaleUtils;

import static org.junit.Assert.assertEquals;

public class LocaleUtilsTests {
	@Test
	public void testLocaleUtilsConstructor() {
		new LocaleUtils();
	}

	@Test
	public void testLocaleUtils() {
		assertEquals(null, LocaleUtils.convertCountryCode(null));
		assertEquals("USA", LocaleUtils.convertCountryCode("USA"));
		assertEquals("USA", LocaleUtils.convertCountryCode("US"));
		assertEquals("JP", LocaleUtils.convertISO3ToISO2CountryCode("JPN"));
		assertEquals("GB", LocaleUtils.convertISO3ToISO2CountryCode("GBR"));
		assertEquals("FR", LocaleUtils.convertISO3ToISO2CountryCode("FRA"));
		assertEquals("AF", LocaleUtils.convertISO3ToISO2CountryCode("AFG"));
		assertEquals("MX", LocaleUtils.convertISO3ToISO2CountryCode("MEX"));
		assertEquals("US", LocaleUtils.convertISO3ToISO2CountryCode("USA"));
	}
}
