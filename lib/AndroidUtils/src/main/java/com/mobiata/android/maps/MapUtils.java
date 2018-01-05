package com.mobiata.android.maps;

import com.google.android.gms.maps.model.LatLng;

/**
 * A collection of utilities that applies to the Google Maps API.
 */
public class MapUtils {
	public static final float RADIUS_EARTH_MI = 3963.1676f;

	public static double milesToKilometers(double miles) {
		return miles * 1.609344;
	}

	public static double kilometersToMiles(double kilometers) {
		return kilometers * .621371192;
	}

	/**
	 * Returns the distance between two points in miles
	 *
	 * @see #getDistance(double, double, double, double)
	 */
	public static double getDistance(LatLng point1, LatLng point2) {
		return getDistance(point1.latitude, point1.longitude, point2.latitude, point2.longitude);
	}

	/**
	 * Returns the distance between two points in miles.
	 *
	 * @param lat1 the latitude of the first point in degrees
	 * @param lon1 the longitude of the first point in degrees
	 * @param lat2 the latitude of the second point in degrees
	 * @param lon2 the longitude of the second point in degrees
	 * @return the distance between the two points in miles
	 */
	public static double getDistance(double lat1, double lon1, double lat2, double lon2) {
		// latitude and longitude in radians
		lat1 = Math.toRadians(lat1);
		lon1 = Math.toRadians(lon1);
		lat2 = Math.toRadians(lat2);
		lon2 = Math.toRadians(lon2);

		// Distance in radians between two points
		final double dLat = lat2 - lat1;
		final double dLon = lon2 - lon1;

		// Square of half the chord length between the two points
		final double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(lat2) * Math.cos(lat1) * Math.sin(dLon / 2)
				* Math.sin(dLon / 2);

		// Angular distance in radians
		final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

		// Distance in miles
		return RADIUS_EARTH_MI * c;
	}

}
