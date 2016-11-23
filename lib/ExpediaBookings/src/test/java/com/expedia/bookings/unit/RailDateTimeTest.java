package com.expedia.bookings.unit;

import org.junit.Before;
import org.junit.Test;
import com.expedia.bookings.data.rail.responses.RailDateTime;
import static org.junit.Assert.assertEquals;

public class RailDateTimeTest {

	private RailDateTime railDateTimeOne;
	private RailDateTime railDateTimeTwo;

	@Before
	public void before() {
		railDateTimeOne = new RailDateTime();
		railDateTimeTwo = new RailDateTime();
	}

	@Test
	public void testCompareToTwoNullRailDateTime() {
		assertEquals(0, railDateTimeOne.compareTo(railDateTimeTwo));
	}

	@Test
	public void testCompareToOneNullRailDateTime() {
		railDateTimeOne.epochSeconds = 1L;
		assertEquals(0, railDateTimeOne.compareTo(railDateTimeTwo));
	}

	@Test
	public void testCompareToLessTan() {
		railDateTimeOne.epochSeconds = 1L;
		railDateTimeTwo.epochSeconds = 2L;
		assertEquals(-1, railDateTimeOne.compareTo(railDateTimeTwo));
	}

	@Test
	public void testCompareToGreaterThan() {
		RailDateTime railDateTime1 = new RailDateTime();
		railDateTime1.epochSeconds = 2L;
		RailDateTime railDateTime2 = new RailDateTime();
		railDateTime2.epochSeconds = 1L;
		assertEquals(1, railDateTime1.compareTo(railDateTime2));
	}

	@Test
	public void testCompareToEquals() {
		RailDateTime railDateTime1 = new RailDateTime();
		railDateTime1.epochSeconds = 1L;
		RailDateTime railDateTime2 = new RailDateTime();
		railDateTime2.epochSeconds = 1L;
		assertEquals(0, railDateTime1.compareTo(railDateTime2));
	}
}
