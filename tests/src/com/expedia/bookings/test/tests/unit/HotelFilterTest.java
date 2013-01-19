package com.expedia.bookings.test.tests.unit;

import junit.framework.TestCase;

import com.expedia.bookings.data.Distance.DistanceUnit;
import com.expedia.bookings.data.Filter;

public class HotelFilterTest extends TestCase {

	public void assertNotEquals(Object left, Object right) {
		assertTrue(!(left.equals(right)));
	}

	public void testHotelFilterBasicEquality() {
		Filter left = new Filter();
		Filter right = new Filter();

		assertEquals(left, left);
		assertEquals(left, right);
	}

	public void testHotelFilterNameEquality() {
		Filter left = new Filter();
		Filter right = new Filter();

		left.setHotelName("A");
		assertNull(right.getHotelName());
		assertNotEquals(left, right);

		right.setHotelName("B");
		assertNotEquals(left, right);

		right.setHotelName("A");
		assertEquals(left, right);

		left.setHotelName(null);
		assertNotEquals(left, right);
	}

	public void testHotelFilterOtherEquality() {
		Filter left = new Filter();
		Filter right = new Filter();

		// SearchRadius
		left.setSearchRadius(Filter.SearchRadius.SMALL);
		right.setSearchRadius(Filter.SearchRadius.LARGE);
		assertNotEquals(left, right);

		right.setSearchRadius(Filter.SearchRadius.SMALL);
		assertEquals(left, right);

		// Distance Unit
		left.setDistanceUnit(DistanceUnit.KILOMETERS);
		right.setDistanceUnit(DistanceUnit.MILES);
		assertNotEquals(left, right);

		right.setDistanceUnit(DistanceUnit.KILOMETERS);
		assertEquals(left, right);

		// Should be good enough due to the symmetry of the remaining equality checks
	}
}
