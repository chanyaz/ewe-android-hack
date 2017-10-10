package com.mobiata.android.net;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Address;

import com.mobiata.android.Log;
import com.mobiata.android.Params;

public class GeocoderResponseHandler extends JsonResponseHandler<List<Address>> {

	@Override
	public List<Address> handleJson(JSONObject response) {
		List<Address> addresses = new ArrayList<>();

		// Parse the status
		String status = response.optString("status");
		if (status.equals("OK")) {
			try {
				JSONArray results = response.getJSONArray("results");
				int len = results.length();
				for (int a = 0; a < len; a++) {
					JSONObject addressJson = results.getJSONObject(a);

					// Parse the address
					Address address = new Address(Locale.getDefault());
					address.setAddressLine(0, addressJson.getString("formatted_address"));

					// Parse the address components
					JSONArray components = addressJson.getJSONArray("address_components");
					int len2 = components.length();
					for (int b = 0; b < len2; b++) {
						parseComponent(address, components.getJSONObject(b));
					}

					// Parse the latitude/longitude
					JSONObject geometry = addressJson.getJSONObject("geometry");
					JSONObject location = geometry.getJSONObject("location");
					address.setLatitude(location.getDouble("lat"));
					address.setLongitude(location.getDouble("lng"));

					addresses.add(address);
				}

				return addresses;
			}
			catch (JSONException e) {
				Log.e(Params.LOGGING_TAG, "Could not parse Google Geocoder response.", e);
				return null;
			}
		}
		else if (status.equals("ZERO_RESULTS")) {
			return addresses;
		}
		else {
			Log.e(Params.LOGGING_TAG, "Got a bad status code from Google Geocoder: " + status);
			return null;
		}
	}

	// Parses an address component.  Does not handle all fields. 
	private void parseComponent(Address address, JSONObject component) throws JSONException {
		String shortName = component.getString("short_name");
		String longName = component.getString("long_name");
		JSONArray types = component.getJSONArray("types");
		int len = types.length();
		for (int a = 0; a < len; a++) {
			String type = types.getString(a);
			if (type.equals("country")) {
				address.setCountryCode(shortName);
				address.setCountryName(longName);
			}
			else if (type.equals("administrative_area_level_1")) {
				address.setAdminArea(longName);
			}
			else if (type.equals("administrative_area_level_2")) {
				address.setSubAdminArea(longName);
			}
			else if (type.equals("locality")) {
				address.setLocality(longName);
			}
			else if (type.equals("postal_code")) {
				address.setPostalCode(longName);
			}
			else if (type.equals("route")) {
				address.setThoroughfare(longName);
			}
			else if (type.equals("street_number")) {
				address.setSubThoroughfare(longName);
			}
		}
	}
}
