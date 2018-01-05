package com.mobiata.android;

import java.util.Calendar;
import java.util.List;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.content.ContextCompat;

import com.mobiata.android.services.GoogleServices;

/**
 * Location-based utilities.
 * 
 * In order to use this, you will need the following permissions in the manifest:
 * <pre>
 * &lt;uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
 * &lt;uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
 * </pre>
 */
// permissions are only needed if you're actually using this class, so not included in the lib manifest
@SuppressWarnings("MissingPermission")
public class LocationServices {

	public static List<Address> geocodeGoogle(Context context, String locationName) {
		Log.d(Params.LOGGING_TAG, "Geocoding location (google) \"" + locationName + "\".");

		GoogleServices services = new GoogleServices(context);
		return services.geocode(locationName);
	}

	public static String formatAddress(Address address) {
		int len = address.getMaxAddressLineIndex() + 1;
		StringBuilder sb = new StringBuilder();
		for (int a = 0; a < len; a++) {
			sb.append(address.getAddressLine(a));
			if (a + 1 != len) {
				sb.append(", ");
			}
		}
		return sb.toString();
	}

	public static Location getLastBestLocation(Context context, long minTime) {
		return getLastBestLocation(context, minTime, Integer.MAX_VALUE);
	}

	public static Location getLastBestLocation(Context context, long minTime, int minDistance) {
		int permissionCheck = ContextCompat.checkSelfPermission(context,
			Manifest.permission.ACCESS_FINE_LOCATION);

		if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
			return null;
		}

		Location bestResult = null;
		float bestAccuracy = Float.MAX_VALUE;
		long bestTime = Long.MIN_VALUE;

		LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		for (String provider : lm.getAllProviders()) {
			Location location = lm.getLastKnownLocation(provider);

			if (location != null) {
				float accuracy = (location.hasAccuracy()) ? location.getAccuracy() : Float.MAX_VALUE;
				long time = location.getTime();

				if (time > bestTime && accuracy < bestAccuracy) {
					bestResult = location;
					bestAccuracy = accuracy;
					bestTime = time;
				}
				else if (time < minTime && bestAccuracy == Float.MAX_VALUE && time > bestTime) {
					bestResult = location;
					bestTime = time;
				}
			}
		}

		if (bestTime < minTime || bestAccuracy > minDistance) {
			Log.w("Could not find a best last location.");
			return null;
		}

		Log.d("Found best last result: " + bestResult);
		Log.d("It was from " + ((Calendar.getInstance().getTimeInMillis() - bestResult.getTime()) / 60000)
				+ " minutes ago, " + bestResult.getAccuracy() + " meters accuracy");
		return bestResult;
	}
}
