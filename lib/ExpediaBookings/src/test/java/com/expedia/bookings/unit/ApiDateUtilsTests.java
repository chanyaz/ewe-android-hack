package com.expedia.bookings.unit;


import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Test;

import com.expedia.bookings.utils.ApiDateUtils;

import static com.expedia.bookings.utils.ApiDateUtils.yyyyMMddHHmmToDateTime;
import static org.junit.Assert.assertEquals;

public class ApiDateUtilsTests {
	@Test
	public void testDateUtilsConstructor() {
		new ApiDateUtils();
	}

	@Test
	public void testConvertMilliSecondsintoDateFormat() {
		long data;

		data = 1419504600000L;
		assertEquals("2014/12/25 10:50", ApiDateUtils.convertMilliSecondsForLogging(data));

		data = 1419418200000L;
		assertEquals("2014/12/24 10:50", ApiDateUtils.convertMilliSecondsForLogging(data));

		data = 1420085700000L;
		assertEquals("2015/01/01 04:15", ApiDateUtils.convertMilliSecondsForLogging(data));

		data = 1419999300000L;
		assertEquals("2014/12/31 04:15", ApiDateUtils.convertMilliSecondsForLogging(data));
	}

	@Test
	public void testyyyyMMddToLocalDate() {
		String localDateStringRepresentation = "2015-01-31";
		LocalDate localDateExpected = new LocalDate(2015, 1, 31);

		LocalDate obtained = ApiDateUtils.yyyyMMddToLocalDate(localDateStringRepresentation);

		assertEquals(obtained, localDateExpected);
	}

	@Test
	public void testyyyyMMddToLocalDateSafe() {
		LocalDate localDateExpected = new LocalDate(2015, 1, 31);

		LocalDate obtained = ApiDateUtils.yyyyMMddToLocalDateSafe("not a date", localDateExpected);

		assertEquals(obtained, localDateExpected);
	}

	@Test
	public void testyyyyMMddHHmmssToLocalDate() {
		String localDateStringRepresentation = "2015-01-31 10:00:00";
		LocalDate localDateExpected = new LocalDate(2015, 1, 31);

		LocalDate obtained = ApiDateUtils.yyyyMMddHHmmssToLocalDate(localDateStringRepresentation);

		assertEquals(obtained, localDateExpected);
	}

	@Test
	public void testParseDurationMinutesFromISOFormat() {
		int actualValue = ApiDateUtils.parseDurationMinutesFromISOFormat("PT2H20M");
		assertEquals(140, actualValue);

		actualValue = ApiDateUtils.parseDurationMinutesFromISOFormat("PT7H4M");
		assertEquals(424, actualValue);

		actualValue = ApiDateUtils.parseDurationMinutesFromISOFormat("PT1H16M");
		assertEquals(76, actualValue);
	}

	@Test
	public void testToYYYYMMTddhhmmss() {
		DateTime dateTimeExpected = new DateTime()
			.withDate(new LocalDate(2020, 6, 25))
			.withHourOfDay(13)
			.withMinuteOfHour(40)
			.withSecondOfMinute(1);
		String actual = ApiDateUtils.toYYYYMMTddhhmmss(dateTimeExpected);
		assertEquals(actual, "2020-06-25T13:40:01");
	}

	@Test
	public void testyyyyMMddHHmmssToDateTime() {
		String localDateStringRepresentation = "2015-01-31 13:40:01";
		DateTime dateTimeExpected = new DateTime()
			.withDate(new LocalDate(2015, 1, 31))
			.withHourOfDay(13)
			.withMinuteOfHour(40)
			.withSecondOfMinute(1)
			.withMillisOfSecond(0);

		DateTime obtained = ApiDateUtils.yyyyMMddHHmmssToDateTime(localDateStringRepresentation);

		assertEquals(obtained, dateTimeExpected);
	}

	@Test
	public void testyyyyMMddHHmmToDateTime() {
		DateTime dateTimeExpected = new DateTime()
			.withDate(new LocalDate(2015, 1, 31))
			.withHourOfDay(13)
			.withSecondOfMinute(0)
			.withMillisOfSecond(0)
			.withMinuteOfHour(0);
		DateTime obtained = yyyyMMddHHmmToDateTime("2015-01-31 13:00");
		assertEquals(obtained, dateTimeExpected);
	}

	@Test
	public void testLocalDateAndMillisToDateTime() {
		LocalDate localDate = new LocalDate(2015, 1, 31);
		DateTime dateTimeExpected = new DateTime()
			.withDate(localDate)
			.withHourOfDay(0)
			.withMinuteOfHour(0)
			.withSecondOfMinute(1)
			.withMillisOfSecond(0);
		DateTime obtained = ApiDateUtils.localDateAndMillisToDateTime(localDate, 1000);
		assertEquals(dateTimeExpected, obtained);
	}

	@Test
	public void testEnsureDateIsTodayOrInFuture() {
		LocalDate yesterday = new LocalDate().minusDays(1);
		LocalDate today = new LocalDate();
		LocalDate tomorrow = new LocalDate().plusDays(1);
		assertEquals(today, ApiDateUtils.ensureDateIsTodayOrInFuture(yesterday));
		assertEquals(today, ApiDateUtils.ensureDateIsTodayOrInFuture(today));
		assertEquals(tomorrow, ApiDateUtils.ensureDateIsTodayOrInFuture(tomorrow));
	}

	@Test
	public void testFormatMillisToHHmmss() {
		LocalDate localDate = new LocalDate(2015, 1, 31);
		assertEquals("13:00:00", ApiDateUtils.formatMillisToHHmmss(localDate, (int)TimeUnit.HOURS.toMillis(13L)));
	}

	@Test
	public void testToMMddyyyy() {
		assertEquals("03/10/2018", ApiDateUtils.toMMddyyyy("March 10, 2018 9:41:00 AM"));
	}
}
