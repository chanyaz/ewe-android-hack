package com.expedia.bookings.unit;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;
import com.expedia.bookings.utils.DateUtils;
import static org.junit.Assert.assertEquals;

public class DateUtilsTests {
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
}
