package com.expedia.bookings.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.Time;
import android.util.TimeFormatException;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Date;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.HotelSearchParams.SearchType;
import com.expedia.bookings.utils.GuestsPickerUtils;
import com.expedia.bookings.utils.NavUtils;
import com.mobiata.android.Log;
import com.mobiata.android.util.Ui;

/**
 * This class acts as a router for incoming deep links.  It seems a lot
 * easier to just route through one Activity rather than try to handle it
 * all in the manifest (where you may need to handle the same scheme in multiple
 * possible activities).
 */
public class DeepLinkRouterActivity extends Activity {

	private static final String TAG = "ExpediaDeepLink";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Handle incoming intents
		Intent intent = getIntent();
		Uri data = intent.getData();
		String host = data.getHost();
		Set<String> queryData = data.getQueryParameterNames();

		if (host.equals("home")) {
			Log.i(TAG, "Launching home screen from deep link!");
			NavUtils.goToLaunchScreen(this, true);
		}
		else if (host.equals("showTrips") || host.equals("trips")) {
			Log.i(TAG, "Launching itineraries from deep link!");
			NavUtils.goToItin(this);
		}
		else if (host.equals("hotelSearch")) {
			// Fill HotelSearchParams with query data
			HotelSearchParams params = new HotelSearchParams();

			// Determine the search location.  Defaults to "current location" if none supplied
			// or the supplied variables could not be parsed.
			if (queryData.contains("latitude") && queryData.contains("longitude")) {
				String latStr = data.getQueryParameter("latitude");
				String lngStr = data.getQueryParameter("longitude");

				try {
					double lat = Double.parseDouble(latStr);
					double lng = Double.parseDouble(lngStr);

					// Check that lat/lng are valid
					if (lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180) {
						params.setSearchType(SearchType.ADDRESS);
						params.setQuery("(" + lat + ", " + lng + ")");
						params.setSearchLatLon(lat, lng);
						Log.d(TAG, "Setting hotel search lat/lng: (" + lat + ", " + lng + ")");
					}
					else {
						Log.w(TAG, "Lat/lng out of valid range: (" + latStr + ", " + lngStr + ")");
					}
				}
				catch (NumberFormatException e) {
					Log.w(TAG, "Could not parse latitude/longitude (" + latStr + ", " + lngStr + ")", e);
				}
			}
			else if (queryData.contains("location")) {
				params.setSearchType(SearchType.CITY);
				params.setQuery(data.getQueryParameter("location"));
				Log.d(TAG, "Setting hotel search location: " + params.getQuery());
			}
			else if (queryData.contains("hotelId")) {
				params.setSearchType(SearchType.HOTEL);
				String hotelId = data.getQueryParameter("hotelId");
				params.setQuery(getString(R.string.search_hotel_id_TEMPLATE, hotelId));
				params.setRegionId(hotelId);
				Log.d(TAG, "Setting hotel search id: " + params.getRegionId());
			}

			// Add dates (if supplied)
			if (queryData.contains("checkInDate")) {
				String checkInDateStr = data.getQueryParameter("checkInDate");
				try {
					Time time = new Time();
					time.parse3339(checkInDateStr);
					Date date = new Date(time);
					params.setCheckInDate(new Date(time));
					Log.d(TAG, "Set hotel check in date: " + date);
				}
				catch (TimeFormatException e) {
					Log.w(TAG, "Could not parse check in date: " + checkInDateStr, e);
				}
			}

			if (queryData.contains("checkOutDate")) {
				String checkOutDateStr = data.getQueryParameter("checkOutDate");
				try {
					Time time = new Time();
					time.parse3339(checkOutDateStr);
					Date date = new Date(time);
					params.setCheckOutDate(date);
					Log.d(TAG, "Set hotel check out date: " + date);
				}
				catch (TimeFormatException e) {
					Log.w(TAG, "Could not parse check out date: " + checkOutDateStr, e);
				}
			}

			// Add adults (if supplied)
			//
			// Note that we still abide by the max guests - we bias towards # adults first
			if (queryData.contains("numAdults")) {
				String numAdultsStr = data.getQueryParameter("numAdults");
				try {
					int numAdults = Integer.parseInt(numAdultsStr);
					int maxAdults = GuestsPickerUtils.getMaxAdults(0);
					if (numAdults > maxAdults) {
						Log.w(TAG, "Number of adults exceeds maximum, lowering to " + maxAdults);
						numAdults = maxAdults;
					}
					params.setNumAdults(numAdults);
					Log.d(TAG, "Setting number of adults: " + numAdults);
				}
				catch (NumberFormatException e) {
					Log.w(TAG, "Could not parse numAdults: " + numAdultsStr, e);
				}
			}

			// Add children (if supplied)
			if (queryData.contains("childAges")) {
				String childAgesStr = data.getQueryParameter("childAges");
				String[] childAgesArr = childAgesStr.split(",");
				int maxChildren = GuestsPickerUtils.getMaxChildren(params.getNumAdults());
				List<Integer> childAges = new ArrayList<Integer>();
				try {
					for (int a = 0; a < childAgesArr.length && childAges.size() < maxChildren; a++) {
						int childAge = Integer.parseInt(childAgesArr[a]);

						if (childAge <= GuestsPickerUtils.MIN_CHILD_AGE) {
							Log.w(TAG, "Child age less than that of a child, not adding: " + childAge);
						}
						else if (childAge > GuestsPickerUtils.MAX_CHILD_AGE) {
							Log.w(TAG, "Child age not an actual child, adding as adult: " + childAge);
							params.setNumAdults(params.getNumAdults() + 1);
							maxChildren = GuestsPickerUtils.getMaxChildren(params.getNumAdults());
						}
						else {
							childAges.add(childAge);
						}
					}

					if (childAges.size() > 0) {
						params.setChildren(childAges);
						Log.d(TAG, "Setting children ages: " + Arrays.toString(childAges.toArray(new Integer[0])));
					}
				}
				catch (NumberFormatException e) {
					Log.w(TAG, "Could not parse childAges: " + childAgesStr, e);
				}
			}

			// Launch hotel search
			Log.i(TAG, "Launching hotel search from deep link!");
			NavUtils.goToHotels(this, params);
		}
		else {
			Ui.showToast(this, "Cannot yet handle data: " + data);
		}

		// This Activity should never fully launch
		finish();
	}
}
