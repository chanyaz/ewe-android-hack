package com.expedia.bookings.test.unit.tests;

import java.util.GregorianCalendar;

import android.test.AndroidTestCase;

import com.expedia.bookings.data.Date;

public class DateTestCase extends AndroidTestCase {

	//////////////////////////////////////////////////////////////////////////
	// Tests

	public void testDateComparison() {
		int year = 2012;
		int month = 5;
		int dayOfMonth = 5;

		// Baseline date
		Date date = new Date(year, month, dayOfMonth);

		// Equals
		assertEquals(date, new Date(year, month, dayOfMonth));

		// Test dates before
		for (int a = -1; a < 2; a++) {
			for (int b = -1; b < 2; b++) {
				assertTrue(date.after(new Date(year - 1, month + a, dayOfMonth + b)));
			}
		}
		for (int a = -1; a < 2; a++) {
			assertTrue(date.after(new Date(year, month - 1, dayOfMonth + a)));
		}
		assertTrue(date.after(new Date(year, month, dayOfMonth - 1)));

		// Test dates after
		for (int a = -1; a < 2; a++) {
			for (int b = -1; b < 2; b++) {
				assertTrue(date.before(new Date(year + 1, month + a, dayOfMonth + b)));
			}
		}
		for (int a = -1; a < 2; a++) {
			assertTrue(date.before(new Date(year, month + 1, dayOfMonth + a)));
		}
		assertTrue(date.before(new Date(year, month, dayOfMonth + 1)));
	}

	public void testDateNullComparison() {
		Date date = new Date(2012, 5, 5);

		assertFalse(date.equals(null));

		try {
			date.after(null);
			fail("Should have thrown exception");
		}
		catch (IllegalArgumentException e) {
			//ignore
		}

		try {
			date.before(null);
			fail("Should have thrown exception");
		}
		catch (IllegalArgumentException e) {
			//ignore
		}
	}

	public void testDateCalendarComparison() {
		int year = 2012;
		int month = 5;
		int dayOfMonth = 5;

		// Baseline date
		Date date = new Date(year, month, dayOfMonth);

		// Equals
		assertEquals(date, new GregorianCalendar(year, month - 1, dayOfMonth));

		// Test dates before
		for (int a = -1; a < 2; a++) {
			for (int b = -1; b < 2; b++) {
				assertTrue(date.after(new GregorianCalendar(year - 1, month + a - 1, dayOfMonth + b)));
			}
		}
		for (int a = -1; a < 2; a++) {
			assertTrue(date.after(new GregorianCalendar(year, month - 2, dayOfMonth + a)));
		}
		assertTrue(date.after(new GregorianCalendar(year, month - 1, dayOfMonth - 1)));

		// Test dates after
		for (int a = -1; a < 2; a++) {
			for (int b = -1; b < 2; b++) {
				assertTrue(date.before(new GregorianCalendar(year + 1, month + a - 1, dayOfMonth + b)));
			}
		}
		for (int a = -1; a < 2; a++) {
			assertTrue(date.before(new GregorianCalendar(year, month, dayOfMonth + a)));
		}
		assertTrue(date.before(new GregorianCalendar(year, month - 1, dayOfMonth + 1)));
	}
}
