package com.expedia.bookings.unit;

import org.junit.Assert;
import org.junit.Test;

import com.expedia.bookings.data.cars.LatLong;

public class LatLongTests {
	@Test
	public void testLatLongIsValid() {
		boolean isValid = LatLong.isValid(null, null);
		Assert.assertEquals(false, isValid);

		isValid = LatLong.isValid(10.0d, null);
		Assert.assertEquals(false, isValid);

		isValid = LatLong.isValid(null, 10.0d);
		Assert.assertEquals(false, isValid);

		isValid = LatLong.isValid(100d, 10.0d);
		Assert.assertEquals(false, isValid);

		isValid = LatLong.isValid(45d, 300d);
		Assert.assertEquals(false, isValid);

		isValid = LatLong.isValid(45d, 180d);
		Assert.assertEquals(true, isValid);

		isValid = LatLong.isValid(-45d, -180d);
		Assert.assertEquals(true, isValid);
	}

	@Test
	public void testLatLongFromStrings() {
		LatLong latLong = LatLong.fromLatLngStrings("-45", "-180");
		Assert.assertEquals(latLong.lat, -45d, 1e-10);
		Assert.assertEquals(latLong.lng, -180d, 1e-10);

		latLong = LatLong.fromLatLngStrings("45", "180");
		Assert.assertEquals(latLong.lat, 45d, 1e-10);
		Assert.assertEquals(latLong.lng, 180d, 1e-10);

		latLong = LatLong.fromLatLngStrings("45", "300");
		Assert.assertNull(latLong);

		latLong = LatLong.fromLatLngStrings("garbage1", "garbage2");
		Assert.assertNull(latLong);
	}
}
