package com.expedia.bookings.test.unit.tests;

import junit.framework.TestCase;

import com.expedia.bookings.data.Distance.DistanceUnit;
import com.expedia.bookings.data.HotelFilter;

public class HotelFilterTest extends TestCase {

	public void assertNotEquals(Object left, Object right) {
		assertTrue(!(left.equals(right)));
	}

	public void testHotelHotelFilterBasicEquality() {
		HotelFilter left = new HotelFilter();
		HotelFilter right = new HotelFilter();

		assertEquals(left, left);
		assertEquals(left, right);
	}

	public void testHotelHotelFilterNameEquality() {
		HotelFilter left = new HotelFilter();
		HotelFilter right = new HotelFilter();

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
		HotelFilter left = new HotelFilter();
		HotelFilter right = new HotelFilter();

		// SearchRadius
		left.setSearchRadius(HotelFilter.SearchRadius.SMALL);
		right.setSearchRadius(HotelFilter.SearchRadius.LARGE);
		assertNotEquals(left, right);

		right.setSearchRadius(HotelFilter.SearchRadius.SMALL);
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
