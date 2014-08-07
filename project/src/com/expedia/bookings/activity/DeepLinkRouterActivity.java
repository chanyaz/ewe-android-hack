package com.expedia.bookings.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.TimeFormatException;

import com.expedia.bookings.R;
import com.expedia.bookings.data.ChildTraveler;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.HotelSearchParams.SearchType;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.AdX;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.GuestsPickerUtils;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.StrUtils;
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
		Log.d(TAG, "Got deeplink: " + host + "/" + data.toString());
		Set<String> queryData = StrUtils.getQueryParameterNames(data);

		AdX.trackDeepLinkLaunch(data);
		OmnitureTracking.parseAndTrackDeepLink(data, queryData);

		if (ExpediaBookingApp.useTabletInterface(this)) {
			Intent tabletLaunch = new Intent(this, TabletLaunchActivity.class);
			startActivity(tabletLaunch);
			return;
		}

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
			if (queryData.contains("hotelId")) {
				params.setSearchType(SearchType.HOTEL);
				String hotelId = data.getQueryParameter("hotelId");
				params.setQuery(getString(R.string.search_hotel_id_TEMPLATE, hotelId));
				params.setRegionId(hotelId);
				Log.d(TAG, "Setting hotel search id: " + params.getRegionId());
			}
			else if (queryData.contains("latitude") && queryData.contains("longitude")) {
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

			// Add dates (if supplied)
			if (queryData.contains("checkInDate")) {
				String checkInDateStr = data.getQueryParameter("checkInDate");
				try {
					LocalDate date = LocalDate.parse(checkInDateStr);
					params.setCheckInDate(date);
					Log.d(TAG, "Set hotel check in date: " + date);
				}
				catch (TimeFormatException e) {
					Log.w(TAG, "Could not parse check in date: " + checkInDateStr, e);
				}
			}

			if (queryData.contains("checkOutDate")) {
				String checkOutDateStr = data.getQueryParameter("checkOutDate");
				try {
					LocalDate date = LocalDate.parse(checkOutDateStr);
					params.setCheckOutDate(date);
					Log.d(TAG, "Set hotel check out date: " + date);
				}
				catch (TimeFormatException e) {
					Log.w(TAG, "Could not parse check out date: " + checkOutDateStr, e);
				}
			}

			// Add adults (if supplied)
			if (queryData.contains("numAdults")) {
				params.setNumAdults(parseNumAdults(data.getQueryParameter("numAdults")));
			}

			// Add children (if supplied)
			if (queryData.contains("childAges")) {
				params.setChildren(parseChildAges(data.getQueryParameter("childAges"), params.getNumAdults()));
			}

			// Launch hotel search
			Log.i(TAG, "Launching hotel search from deep link!");
			NavUtils.goToHotels(this, params, null, NavUtils.FLAG_DEEPLINK);
		}
		else if (host.equals("flightSearch")) {
			// Fill FlightSearchParams with query data
			FlightSearchParams params = new FlightSearchParams();

			if (queryData.contains("origin")) {
				Location departureLocation = new Location();
				departureLocation.setDestinationId(data.getQueryParameter("origin"));
				params.setDepartureLocation(departureLocation);
				Log.d(TAG, "Set flight origin: " + departureLocation.getDestinationId());
			}

			if (queryData.contains("destination")) {
				Location arrivalLocation = new Location();
				arrivalLocation.setDestinationId(data.getQueryParameter("destination"));
				params.setArrivalLocation(arrivalLocation);
				Log.d(TAG, "Set flight destination: " + arrivalLocation.getDestinationId());
			}

			if (queryData.contains("departureDate")) {
				String departureDateStr = data.getQueryParameter("departureDate");
				try {
					LocalDate date = LocalDate.parse(departureDateStr);
					params.setDepartureDate(date);
					Log.d(TAG, "Set flight departure date: " + date);
				}
				catch (TimeFormatException e) {
					Log.w(TAG, "Could not parse flight departure date: " + departureDateStr, e);
				}
			}

			if (queryData.contains("returnDate")) {
				String returnDateStr = data.getQueryParameter("returnDate");
				try {
					LocalDate date = LocalDate.parse(returnDateStr);
					params.setReturnDate(date);
					Log.d(TAG, "Set flight return date: " + date);
				}
				catch (TimeFormatException e) {
					Log.w(TAG, "Could not parse flight return date: " + returnDateStr, e);
				}
			}

			params.ensureValidDates();

			// Add adults (if supplied)
			if (queryData.contains("numAdults")) {
				params.setNumAdults(parseNumAdults(data.getQueryParameter("numAdults")));
			}

			// Launch flight search
			Db.getFlightSearch().setSearchParams(params);
			if (params.isFilled()) {
				Log.i(TAG, "Launching flight search results activity from deep link!");
				NavUtils.goToFlightSearch(this);
			}
			else {
				Log.i(TAG, "Launching flight search params activity from deep link!");
				NavUtils.goToFlights(this, true);
			}
		}
		/*
		 * Let's handle iOS implementation of sharing/importing itins, cause we can - Yeah, Android ROCKS !!!
		 * iOS prepends the sharableLink this way "expda://addSharedItinerary?url=<actual_sharable_link_here>"
		 * We intercept this uri too, extract the link and then send to fetch the itin.
		 */
		else if (host.equals("addSharedItinerary") && data.toString().contains("m/trips/shared")) {
			goFetchSharedItin(data.getQueryParameter("url"));
		}
		else if (data.toString().contains("m/trips/shared")) {
			goFetchSharedItin(data.toString());
		}
		else if ("e.xpda.co".equalsIgnoreCase(host)) {
			final String shortUrl = data.toString();
			final ExpediaServices services = new ExpediaServices(this);
			new Thread(new Runnable() {
				@Override
				public void run() {
					String longUrl = services.getLongUrl(shortUrl);
					if (null != longUrl) {
						goFetchSharedItin(longUrl);
					}
				}
			}).start();
		}
		else {
			Ui.showToast(this, "Cannot yet handle data: " + data);
		}

		// This Activity should never fully launch
		finish();
	}

	private void goFetchSharedItin(String sharableUrl) {
		ItineraryManager.getInstance().fetchSharedItin(sharableUrl);
		NavUtils.goToItin(this);
	}

	private int parseNumAdults(String numAdultsStr) {
		try {
			int numAdults = Integer.parseInt(numAdultsStr);
			int maxAdults = GuestsPickerUtils.getMaxAdults(0);
			if (numAdults > maxAdults) {
				Log.w(TAG, "Number of adults (" + numAdults + ") exceeds maximum, lowering to " + maxAdults);
				numAdults = maxAdults;
			}
			else if (numAdults < GuestsPickerUtils.MIN_ADULTS) {
				Log.w(TAG, "Number of adults (" + numAdults + ") below minimum, raising to "
						+ GuestsPickerUtils.MIN_ADULTS);
				numAdults = GuestsPickerUtils.MIN_ADULTS;
			}
			Log.d(TAG, "Setting number of adults: " + numAdults);

			return numAdults;
		}
		catch (NumberFormatException e) {
			Log.w(TAG, "Could not parse numAdults: " + numAdultsStr, e);
		}

		return GuestsPickerUtils.MIN_ADULTS;
	}

	// Note that we still abide by the max guests - we bias towards # adults first
	private List<ChildTraveler> parseChildAges(String childAgesStr, int numAdults) {
		String[] childAgesArr = childAgesStr.split(",");
		int maxChildren = GuestsPickerUtils.getMaxChildren(numAdults);
		List<ChildTraveler> children = new ArrayList<ChildTraveler>();
		try {
			for (int a = 0; a < childAgesArr.length && children.size() < maxChildren; a++) {
				int childAge = Integer.parseInt(childAgesArr[a]);

				if (childAge <= GuestsPickerUtils.MIN_CHILD_AGE) {
					Log.w(TAG, "Child age (" + childAge + ") less than that of a child, not adding: "
							+ childAge);
				}
				else if (childAge > GuestsPickerUtils.MAX_CHILD_AGE) {
					Log.w(TAG, "Child age (" + childAge + ") not an actual child, ignoring: " + childAge);
				}
				else {
					children.add(new ChildTraveler(childAge, false));
				}
			}

			if (children.size() > 0) {
				Log.d(TAG, "Setting children ages: " + Arrays.toString(children.toArray(new Integer[0])));
				return children;
			}
		}
		catch (NumberFormatException e) {
			Log.w(TAG, "Could not parse childAges: " + childAgesStr, e);
		}

		return null;
	}
}
