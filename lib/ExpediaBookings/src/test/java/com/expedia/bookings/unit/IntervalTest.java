package com.expedia.bookings.unit;

import org.junit.Test;

import com.expedia.bookings.utils.Interval;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class IntervalTest {

	@Test
	public void testAdd() {
		Interval interval = new Interval();
		assertFalse(interval.bounded());

		interval.add(1);
		assertEquals(1, interval.getMin());
		assertEquals(1, interval.getMax());
		assertFalse(interval.different());
		assertTrue(interval.bounded());

		interval.add(2);
		assertEquals(1, interval.getMin());
		assertEquals(2, interval.getMax());
		assertTrue(interval.different());
		assertTrue(interval.bounded());

		interval.add(0);
		assertEquals(0, interval.getMin());
		assertEquals(2, interval.getMax());
		assertTrue(interval.bounded());
	}

	@Test
	public void testAddIgnoreZero() {
		Interval interval = new Interval();
		assertFalse(interval.bounded());

		interval.addIgnoreZero(0);
		assertFalse(interval.bounded());
		assertTrue(interval.different());

		interval.add(1);
		interval.add(2);
		assertEquals(1, interval.getMin());
		assertEquals(2, interval.getMax());
		assertTrue(interval.different());
		assertTrue(interval.bounded());

		interval.addIgnoreZero(0);
		assertEquals(1, interval.getMin());
		assertEquals(2, interval.getMax());

		interval.addIgnoreZero(3);
		assertEquals(1, interval.getMin());
		assertEquals(3, interval.getMax());
	}

}
