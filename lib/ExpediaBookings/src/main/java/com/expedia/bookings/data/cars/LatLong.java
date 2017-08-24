package com.expedia.bookings.data.cars;

import com.expedia.bookings.utils.NumberUtils;

public class LatLong {
	public final double lat;
	public final double lng;

	public LatLong(double lat, double lng) {
		this.lat = lat;
		this.lng = lng;
	}

	public static boolean isValid(Double locationLat, Double locationLng) {
		return (locationLat != null && locationLat >= -90d && locationLat <= 90d) &&
			(locationLng != null && locationLng >= -180d && locationLng <= 180d);
	}

	public static LatLong fromLatLngStrings(String locationLatStr, String locationLngStr) {
		LatLong latLong = null;

		Double locationLat = NumberUtils.parseDoubleSafe(locationLatStr);
		Double locationLng = NumberUtils.parseDoubleSafe(locationLngStr);
		if (LatLong.isValid(locationLat, locationLng)) {
			latLong = new LatLong(locationLat, locationLng);
		}

		return latLong;
	}

	public double getLatitude() {
		return lat;
	}

	public double getLongitude() {
		return lng;
	}
}
