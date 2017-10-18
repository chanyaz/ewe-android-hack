package com.expedia.bookings.unit;


import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;

import com.expedia.bookings.utils.DateUtils;

import static org.junit.Assert.assertEquals;

public class DateUtilsTests {
	@Test
	public void testDateUtilsConstructor() {
		new DateUtils();
	}

	@Test
	public void testStringsConvertDatetoInt() {
		DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy/MM/dd");

		LocalDate date1 = dtf.parseLocalDate("2014/01/12");
		assertEquals(20140112, DateUtils.convertDatetoInt(date1));

		LocalDate date2 = dtf.parseLocalDate("2014/11/09");
		assertEquals(20141109, DateUtils.convertDatetoInt(date2));

		LocalDate date3 = dtf.parseLocalDate("2015/01/01");
		assertEquals(20150101, DateUtils.convertDatetoInt(date3));

		assertEquals(0, DateUtils.convertDatetoInt(null));
	}

	@Test
	public void testCarSearchFormatFromDateTime() {
		DateTime dt = DateTime.now()
			.withYear(2012)
			.withMonthOfYear(5)
			.withDayOfMonth(13)
			.withHourOfDay(4)
			.withMinuteOfHour(0)
			.withSecondOfMinute(0);
		String result = DateUtils.carSearchFormatFromDateTime(dt);
		String expected = "2012-05-13T04:00:00";

		assertEquals(expected, result);
	}

	@Test
	public void testConvertMilliSecondsintoDateFormat() {
		long data;

		data = 1419504600000L;
		assertEquals("2014/12/25 10:50", DateUtils.convertMilliSecondsForLogging(data));

		data = 1419418200000L;
		assertEquals("2014/12/24 10:50", DateUtils.convertMilliSecondsForLogging(data));

		data = 1420085700000L;
		assertEquals("2015/01/01 04:15", DateUtils.convertMilliSecondsForLogging(data));

		data = 1419999300000L;
		assertEquals("2014/12/31 04:15", DateUtils.convertMilliSecondsForLogging(data));
	}

	@Test
	public void testyyyyMMddToLocalDate() {
		String localDateStringRepresentation = "2015-01-31";
		LocalDate localDateExpected = new LocalDate(2015, 1, 31);

		LocalDate obtained = DateUtils.yyyyMMddToLocalDate(localDateStringRepresentation);

		assertEquals(obtained, localDateExpected);
	}

	@Test
	public void testyyyyMMddHHmmssToLocalDate() {
		String localDateStringRepresentation = "2015-01-31 10:00:00";
		LocalDate localDateExpected = new LocalDate(2015, 1, 31);

		LocalDate obtained = DateUtils.yyyyMMddHHmmssToLocalDate(localDateStringRepresentation);

		assertEquals(obtained, localDateExpected);
	}

	@Test
	public void testyyyyMMddTHHmmssToLocalDate() {
		DateTime dateTimeExpected = DateTime.now()
			.withYear(2015)
			.withMonthOfYear(1)
			.withDayOfMonth(31)
			.withHourOfDay(10)
			.withMinuteOfHour(0)
			.withSecondOfMinute(0)
			.withMillisOfSecond(0);

		String localDateStringRepresentation = "2015-01-31T10:00:00";

		DateTime dateTimeObtained = DateUtils.yyyyMMddTHHmmssToDateTimeSafe(localDateStringRepresentation,
			DateTime.now());

		assertEquals(dateTimeObtained, dateTimeExpected);
	}

	@Test
	public void testParseDurationFromISOFormat() {
		String date = "PT2H20M";
		Period period = DateUtils.parseDurationFromISOFormat(date);
		assertEquals(2, period.getHours());
		assertEquals(20, period.getMinutes());
	}

	@Test
	public void testParseDurationMinutesFromISOFormat() {
		int actualValue = DateUtils.parseDurationMinutesFromISOFormat("PT2H20M");
		assertEquals(140, actualValue);

		actualValue = DateUtils.parseDurationMinutesFromISOFormat("PT7H4M");
		assertEquals(424, actualValue);

		actualValue = DateUtils.parseDurationMinutesFromISOFormat("PT1H16M");
		assertEquals(76, actualValue);
	}
}
