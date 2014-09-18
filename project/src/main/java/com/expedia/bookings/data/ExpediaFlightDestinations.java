package com.expedia.bookings.data;

import java.io.InputStream;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.expedia.bookings.data.pos.PointOfSale;
import com.mobiata.android.Log;
import com.mobiata.android.util.IoUtils;

/**
 * This is a class for reading/parsing/and returning lists of flight destinations (for the launch screen) based on PointOfSale.
 * 
 * The data for this comes from our shared data
 */
public class ExpediaFlightDestinations {

	private static final String DEFAULT_KEY = "*";
	private static final String FILE_LOCATION = "ExpediaSharedData/ExpediaFlightDestinations.json";

	private JSONObject mFlightDestinationJSON;

	public ExpediaFlightDestinations(Context context) {
		loadData(context);
	}

	/**
	 * Get flight destinations for the supplied POS
	 * @param pos
	 * @return
	 */
	public String[] getDestinations(PointOfSale pos) {
		return getDestinations(pos, Integer.MAX_VALUE);
	}

	/**
	 * Get flight destinations for the supplied POS
	 * 
	 * @param pos
	 * @param maxDestinationsReturned - only return the first maxDestinationsReturned destinations
	 * @return
	 */
	public String[] getDestinations(PointOfSale pos, int maxDestinationsReturned) {
		String posKey = getPosSpecificKey(pos);
		try {
			JSONArray destinations = mFlightDestinationJSON.getJSONArray(posKey);
			int limit = Math.min(maxDestinationsReturned, destinations.length());
			String[] retArr = new String[limit];
			for (int i = 0; i < limit && i < maxDestinationsReturned; i++) {
				JSONObject dest = destinations.getJSONObject(i);
				retArr[i] = dest.getString("code");
			}
			return retArr;
		}
		catch (JSONException ex) {
			Log.d("Failure to getDestinations for posKey:" + posKey, ex);
		}
		return null;
	}

	/**
	 * Some POSs have their own set of destinations. Does the supplied POS use the default set?
	 * Or does it have its own set?
	 *
	 * @param pos
	 * @return true if the supplied pos uses the default list.
	 */
	public boolean usesDefaultDestinationList(PointOfSale pos) {
		return getPosSpecificKey(pos).equals(DEFAULT_KEY);
	}

	/**
	 * Load  flight destination data from disk.
	 * 
	 * @param context
	 */
	private void loadData(Context context) {
		try {
			InputStream flightDestinationInputStream = context.getAssets().open(FILE_LOCATION);
			String flightDestinationString = IoUtils.convertStreamToString(flightDestinationInputStream);
			mFlightDestinationJSON = new JSONObject(flightDestinationString);
		}
		catch (Exception ex) {
			Log.e("Failure to loadData", ex);
		}
	}

	/**
	 * Get the key that we can use to look up the appropriate list of flight destination images.
	 * 
	 * @param pos
	 * @return
	 */
	private String getPosSpecificKey(PointOfSale pos) {
		String countryCode = pos.getTwoLetterCountryCode().toUpperCase(Locale.ENGLISH);
		String posKey = mFlightDestinationJSON.has(countryCode) ? countryCode : DEFAULT_KEY;
		return posKey;
	}

}
