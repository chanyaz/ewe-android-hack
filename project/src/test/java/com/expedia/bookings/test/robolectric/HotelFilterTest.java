package com.expedia.bookings.test.robolectric;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.expedia.bookings.data.Distance.DistanceUnit;
import com.expedia.bookings.data.HotelFilter;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;

@RunWith(RobolectricRunner.class)
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
		Assert.assertThat(left, not(equalTo(right)));

		right.setHotelName("B");
		Assert.assertThat(left, not(equalTo(right)));

		right.setHotelName("A");
		Assert.assertThat(left, equalTo(right));

		left.setHotelName(null);
		Assert.assertThat(left, not(equalTo(right)));
	}

	@Test
	public void testHotelFilterOtherEquality() {
		HotelFilter left = new HotelFilter();
		HotelFilter right = new HotelFilter();

		// SearchRadius
		left.setSearchRadius(HotelFilter.SearchRadius.SMALL);
		right.setSearchRadius(HotelFilter.SearchRadius.LARGE);
		Assert.assertThat(left, not(equalTo(right)));

		right.setSearchRadius(HotelFilter.SearchRadius.SMALL);
		Assert.assertEquals(left, right);

		// Distance Unit
		left.setDistanceUnit(DistanceUnit.KILOMETERS);
		right.setDistanceUnit(DistanceUnit.MILES);
		Assert.assertThat(left, not(equalTo(right)));

		right.setDistanceUnit(DistanceUnit.KILOMETERS);
		Assert.assertEquals(left, right);

		// Should be good enough due to the symmetry of the remaining equality checks
	}
}
