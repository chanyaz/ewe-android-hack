package com.expedia.bookings.test.robolectric;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.expedia.bookings.data.Distance.DistanceUnit;
import com.expedia.bookings.data.HotelFilter;

@RunWith(RobolectricSubmoduleTestRunner.class)
public class HotelFilterTest {

	@Test
	public void testHotelHotelFilterBasicEquality() {
		HotelFilter left = new HotelFilter();
		HotelFilter right = new HotelFilter();

		Assert.assertEquals(left, left);
		Assert.assertEquals(left, right);
	}

	@Test
	public void testHotelHotelFilterNameEquality() {
		HotelFilter left = new HotelFilter();
		HotelFilter right = new HotelFilter();

		left.setHotelName("A");
		Assert.assertNull(right.getHotelName());
		Assert.assertNotEquals(left, right);

		right.setHotelName("B");
		Assert.assertNotEquals(left, right);

		right.setHotelName("A");
		Assert.assertEquals(left, right);

		left.setHotelName(null);
		Assert.assertNotEquals(left, right);
	}

	@Test
	public void testHotelFilterOtherEquality() {
		HotelFilter left = new HotelFilter();
		HotelFilter right = new HotelFilter();

		// SearchRadius
		left.setSearchRadius(HotelFilter.SearchRadius.SMALL);
		right.setSearchRadius(HotelFilter.SearchRadius.LARGE);
		Assert.assertNotEquals(left, right);

		right.setSearchRadius(HotelFilter.SearchRadius.SMALL);
		Assert.assertEquals(left, right);

		// Distance Unit
		left.setDistanceUnit(DistanceUnit.KILOMETERS);
		right.setDistanceUnit(DistanceUnit.MILES);
		Assert.assertNotEquals(left, right);

		right.setDistanceUnit(DistanceUnit.KILOMETERS);
		Assert.assertEquals(left, right);

		// Should be good enough due to the symmetry of the remaining equality checks
	}
}
