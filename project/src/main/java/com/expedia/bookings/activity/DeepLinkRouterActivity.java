package com.expedia.bookings.activity;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.TimeFormatException;

import com.expedia.bookings.R;
import com.expedia.bookings.data.ChildTraveler;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearch;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.HotelFilter;
import com.expedia.bookings.data.HotelSearch;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.HotelSearchParams.SearchType;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.Sp;
import com.expedia.bookings.data.SuggestionResponse;
import com.expedia.bookings.data.SuggestionV2;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.AdX;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.GuestsPickerUtils;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.BackgroundDownloader;
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

	private static final String DL_KEY_HOTEL_ID = "DeepLink.HotelId";
	private static final String DL_KEY_LAT_LNG = "DeepLink.LatLng";
	private static final String DL_KEY_LOCATION_SUGGEST = "DeepLink.LocationSuggest";
	private static final String DL_KEY_FLIGHT_SUGGEST = "DeepLink.FlightSuggest";


	private SearchParams mSearchParams;
	private LineOfBusiness mLobToLaunch = null;

	private boolean mIsCurrentLocationSearch;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Handle incoming intents
		Intent intent = getIntent();
		Uri data = intent.getData();
		String host = data.getHost();
		String dataString = data.toString();

		// Decoding the URL, as it is not being captured because of the encoding of the url on the test server's.
		try {
			dataString = URLDecoder.decode(data.toString(), "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			Log.w(TAG, "Could not decode deep link data" + data.toString(), e);
		}

		Log.d(TAG, "Got deeplink: " + host + "/" + dataString);
		Set<String> queryData = StrUtils.getQueryParameterNames(data);

		AdX.trackDeepLinkLaunch(data);
		OmnitureTracking.parseAndTrackDeepLink(data, queryData);

		/*
		 * Let's handle iOS implementation of sharing/importing itins, cause we can - Yeah, Android ROCKS !!!
		 * iOS prepends the sharableLink this way "expda://addSharedItinerary?url=<actual_sharable_link_here>"
		 * We intercept this uri too, extract the link and then send to fetch the itin.
		 */
		if (host.equals("addSharedItinerary") && dataString.contains("m/trips/shared")) {
			goFetchSharedItin(data.getQueryParameter("url"));
			finish();
			return;
		}
		else if (dataString.contains("m/trips/shared")) {
			goFetchSharedItin(dataString);
			finish();
			return;
		}
		else if (ProductFlavorFeatureConfiguration.getInstance().getHostnameForShortUrl().equalsIgnoreCase(host)) {
			final String shortUrl = dataString;
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
			finish();
			return;
		}

		boolean finish;
		switch (host) {
		case "home":
			Log.i(TAG, "Launching home screen from deep link!");
			NavUtils.goToLaunchScreen(this, true);
			finish = true;
			break;
		case "showTrips":
		case "trips":
			Log.i(TAG, "Launching itineraries from deep link!");
			NavUtils.goToItin(this);
			finish = true;
			break;
		case "hotelSearch":
			finish = handleHotelSearch(data, queryData);
			break;
		case "flightSearch":
			handleFlightSearch(data, queryData);
			finish = true;
			break;
		case "destination":
			Log.i(TAG, "Launching destination search from deep link!");
			handleDestination(data, queryData);
			finish = true;
			break;
		default:
			Ui.showToast(this, "Cannot yet handle data: " + data);
			finish = true;
		}

		// This Activity should never fully launch
		if (finish) {
			finish();
		}
	}

	/**
	 * We'll parse any deep link whose url matches: expda://hotelSearch/*
	 * <p/>
	 * Example: search by hotelId:
	 * This will search for hotel suggestions by hotelId and launch the first suggestion
	 * expda://hotelSearch/?checkInDate=2015-01-01&checkOutDate=2015-01-02&numAdults=2&childAges=5,6,7&hotelId=12345
	 * <p/>
	 * Example: search by latitude, longitude:
	 * This will search for hotel suggestions and return the nearest city to the passed latitude, longitude
	 * expda://hotelSearch/?checkInDate=2015-01-01&checkOutDate=2015-01-02&numAdults=2&childAges=5,6,7&latitude=-17.027&longitude=177.1435
	 * <p/>
	 * Example: search by location:
	 * This will search for hotel suggestions based on the location text, and launch the first suggestion
	 * expda://hotelSearch/?checkInDate=2015-01-01&checkOutDate=2015-01-02&numAdults=2&childAges=5,6,7&location=San+Diego
	 *
	 * @param data
	 * @param queryData
	 * @return
	 */
	private boolean handleHotelSearch(Uri data, Set<String> queryData) {
		LocalDate startDate = null;
		LocalDate endDate = null;
		int numAdults = 0;
		List<ChildTraveler> children = null;

		// Add dates (if supplied)
		if (queryData.contains("checkInDate")) {
			String checkInDateStr = data.getQueryParameter("checkInDate");
			try {
				startDate = LocalDate.parse(checkInDateStr);
				Log.d(TAG, "Set hotel check in date: " + startDate);
			}
			catch (TimeFormatException | IllegalArgumentException e) {
				Log.w(TAG, "Could not parse check in date: " + checkInDateStr, e);
			}
		}

		if (queryData.contains("checkOutDate")) {
			String checkOutDateStr = data.getQueryParameter("checkOutDate");
			try {
				endDate = LocalDate.parse(checkOutDateStr);
				Log.d(TAG, "Set hotel check out date: " + endDate);
			}
			catch (TimeFormatException | IllegalArgumentException e) {
				Log.w(TAG, "Could not parse check out date: " + checkOutDateStr, e);
			}
		}

		// Add adults (if supplied)
		if (queryData.contains("numAdults")) {
			numAdults = parseNumAdults(data.getQueryParameter("numAdults"));
		}

		// Add children (if supplied)
		if (queryData.contains("childAges")) {
			children = parseChildAges(data.getQueryParameter("childAges"), numAdults);
		}

		if (ExpediaBookingApp.useTabletInterface(this)) {
			mSearchParams = new SearchParams();
			mLobToLaunch = LineOfBusiness.HOTELS;

			if (startDate != null) {
				mSearchParams.setStartDate(startDate);
			}
			if (endDate != null) {
				mSearchParams.setEndDate(endDate);
			}
			if (numAdults != 0) {
				mSearchParams.setNumAdults(numAdults);
			}
			if (children != null) {
				mSearchParams.setChildTravelers(children);
			}

			// Validation
			if (!mSearchParams.isDurationValid()) {
				mSearchParams.setDefaultDuration();
			}
			if (!mSearchParams.areGuestsValid()) {
				mSearchParams.setDefaultGuests();
			}

			BackgroundDownloader bgd = BackgroundDownloader.getInstance();

			// Determine the search location.  Defaults to "current location" if none supplied
			// or the supplied variables could not be parsed.
			if (queryData.contains("hotelId")) {
				final String hotelId = data.getQueryParameter("hotelId");
				bgd.startDownload(DL_KEY_HOTEL_ID, new BackgroundDownloader.Download<SuggestionResponse>() {
					@Override
					public SuggestionResponse doDownload() {
						ExpediaServices services = new ExpediaServices(DeepLinkRouterActivity.this);
						return services.suggestionsHotelId(hotelId);
					}
				}, mSuggestCallback);
				return false;
			}
			else if (queryData.contains("latitude") && queryData.contains("longitude")) {
				String latStr = data.getQueryParameter("latitude");
				String lngStr = data.getQueryParameter("longitude");

				try {
					final double lat = Double.parseDouble(latStr);
					final double lng = Double.parseDouble(lngStr);

					// Check that lat/lng are valid
					if (lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180) {
						Log.d(TAG, "Setting hotel search lat/lng: (" + lat + ", " + lng + ")");
						bgd.startDownload(DL_KEY_LAT_LNG, new BackgroundDownloader.Download<SuggestionResponse>() {
							@Override
							public SuggestionResponse doDownload() {
								ExpediaServices services = new ExpediaServices(DeepLinkRouterActivity.this);
								return services.suggestionsCityNearby(lat, lng);
							}
						}, mSuggestCallback);
						return false;
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
				final String query = data.getQueryParameter("location");
				Log.d(TAG, "Setting hotel search location: " + query);
				bgd.startDownload(DL_KEY_LOCATION_SUGGEST, new BackgroundDownloader.Download<SuggestionResponse>() {
					@Override
					public SuggestionResponse doDownload() {
						ExpediaServices services = new ExpediaServices(DeepLinkRouterActivity.this);
						return services.suggestions(query, 0);
					}
				}, mSuggestCallback);
				return false;
			}
			else {
				SuggestionV2 destination = new SuggestionV2();
				destination.setResultType(SuggestionV2.ResultType.CURRENT_LOCATION);
				mSearchParams.setDestination(destination);
				NavUtils.goToTabletResults(this, mSearchParams, LineOfBusiness.HOTELS);
			}
		}
		else {
			// Fill HotelSearchParams with query data
			HotelSearchParams params = new HotelSearchParams();

			if (startDate != null) {
				params.setCheckInDate(startDate);
			}
			if (endDate != null) {
				params.setCheckOutDate(endDate);
			}
			if (numAdults != 0) {
				params.setNumAdults(numAdults);
			}
			if (children != null) {
				params.setChildren(children);
			}

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

			// Launch hotel search
			Log.i(TAG, "Launching hotel search from deep link!");
			NavUtils.goToHotels(this, params, null, NavUtils.FLAG_DEEPLINK);
		}
		return true;
	}

	/**
	 * We'll parse any deep link whose url matches: expda://flightSearch/*
	 * <p/>
	 * Example:
	 * expda://flightSearch/?origin=LAX&destination=SFO&departureDate=2015-01-01&returnDate=2015-01-02&numAdults=2
	 *
	 * @param data
	 * @param queryData
	 * @return
	 */
	private void handleFlightSearch(Uri data, Set<String> queryData) {
		String originAirportCode = null;
		String destinationAirportCode = null;
		LocalDate startDate = null;
		LocalDate endDate = null;
		int numAdults = 0;
		List<ChildTraveler> children = null;

		if (queryData.contains("origin")) {
			originAirportCode = data.getQueryParameter("origin");
		}
		if (queryData.contains("destination")) {
			destinationAirportCode = data.getQueryParameter("destination");
		}
		if (queryData.contains("departureDate")) {
			String departureDateStr = data.getQueryParameter("departureDate");
			try {
				startDate = LocalDate.parse(departureDateStr);
			}
			catch (TimeFormatException | IllegalArgumentException e) {
				Log.w(TAG, "Could not parse flight departure date: " + departureDateStr, e);
			}
		}
		if (queryData.contains("returnDate")) {
			String returnDateStr = data.getQueryParameter("returnDate");
			try {
				endDate = LocalDate.parse(returnDateStr);
			}
			catch (TimeFormatException | IllegalArgumentException e) {
				Log.w(TAG, "Could not parse flight return date: " + returnDateStr, e);
			}
		}
		if (queryData.contains("numAdults")) {
			numAdults = parseNumAdults(data.getQueryParameter("numAdults"));
		}


		if (ExpediaBookingApp.useTabletInterface(this)) {
			mSearchParams = new SearchParams();

			if (startDate != null) {
				mSearchParams.setStartDate(startDate);
			}
			if (endDate != null) {
				mSearchParams.setEndDate(endDate);
			}
			if (numAdults != 0) {
				mSearchParams.setNumAdults(numAdults);
			}

			// Validation
			if (!mSearchParams.isDurationValid()) {
				mSearchParams.setDefaultDuration();
			}
			if (!mSearchParams.areGuestsValid()) {
				mSearchParams.setDefaultGuests();
			}

			if (originAirportCode != null) {
				SuggestionV2 origin = new SuggestionV2();
				origin.setAirportCode(originAirportCode);
				origin.setDisplayName(originAirportCode);
				origin.setLocation(new Location());
				mSearchParams.setOrigin(origin);
			}

			final String destAirportCode = destinationAirportCode;
			BackgroundDownloader.getInstance().startDownload(DL_KEY_FLIGHT_SUGGEST,
				new BackgroundDownloader.Download<SuggestionResponse>() {
					@Override
					public SuggestionResponse doDownload() {
						ExpediaServices services = new ExpediaServices(DeepLinkRouterActivity.this);
						return services.suggestions(destAirportCode, 0);
					}
				}, mSuggestCallback);
		}
		else {
			// Fill FlightSearchParams with query data
			FlightSearchParams params = new FlightSearchParams();

			if (originAirportCode != null) {
				Location departureLocation = new Location();
				departureLocation.setDestinationId(originAirportCode);
				params.setDepartureLocation(departureLocation);
				Log.d(TAG, "Set flight origin: " + departureLocation.getDestinationId());
			}

			if (destinationAirportCode != null) {
				Location arrivalLocation = new Location();
				arrivalLocation.setDestinationId(destinationAirportCode);
				params.setArrivalLocation(arrivalLocation);
				Log.d(TAG, "Set flight destination: " + arrivalLocation.getDestinationId());
			}

			if (startDate != null) {
				Log.d(TAG, "Set flight departure date: " + startDate);
				params.setDepartureDate(startDate);
			}

			if (endDate != null) {
				params.setReturnDate(endDate);
				Log.d(TAG, "Set flight return date: " + endDate);
			}

			params.ensureValidDates();

			// Add adults (if supplied)
			if (numAdults != 0) {
				params.setNumAdults(numAdults);
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
	}

	private void handleDestination(Uri data, Set<String> queryData) {
		try {
			if (ExpediaBookingApp.useTabletInterface(this)) {
				SuggestionV2 destination = new SuggestionV2();
				destination.setDisplayName(data.getQueryParameter("displayName"));
				destination.setSearchType(SuggestionV2.SearchType.valueOf(data.getQueryParameter("searchType")));
				destination.setRegionId(Integer.parseInt(data.getQueryParameter("hotelId")));
				destination.setAirportCode(data.getQueryParameter("airportCode"));
				destination.setMultiCityRegionId(Integer.parseInt(data.getQueryParameter("regionId")));

				Location location = new Location();
				double lat = Double.parseDouble(data.getQueryParameter("latitude"));
				double lng = Double.parseDouble(data.getQueryParameter("longitude"));
				if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
					throw new RuntimeException("Lat/Lng out of valid range (" + lat + ", " + lng + ")");
				}
				location.setLatitude(lat);
				location.setLongitude(lng);
				destination.setLocation(location);

				destination.setImageCode(data.getQueryParameter("imageCode"));

				mSearchParams = Sp.getParams();
				mSearchParams.restoreToDefaults();
				mSearchParams.setDestination(destination);

				HotelSearch hotelSearch = Db.getHotelSearch();
				FlightSearch flightSearch = Db.getFlightSearch();

				// Search results filters
				HotelFilter filter = Db.getFilter();
				filter.reset();
				filter.notifyFilterChanged();

				// Start the search
				Log.i("Starting search with params: " + Sp.getParams());
				hotelSearch.setSearchResponse(null);
				flightSearch.setSearchResponse(null);

				Db.deleteCachedFlightData(this);
				Db.deleteHotelSearchData(this);

				NavUtils.goToTabletResults(this, Sp.getParams(), null);
			}

			// Phones don't have a "destination" results concept, so we'll use hotels instead
			else {
				// Fill HotelSearchParams with query data
				HotelSearchParams params = new HotelSearchParams();

				double lat = Double.parseDouble(data.getQueryParameter("latitude"));
				double lng = Double.parseDouble(data.getQueryParameter("longitude"));
				if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
					throw new RuntimeException("Lat/Lng out of valid range (" + lat + ", " + lng + ")");
				}

				params.setSearchType(SearchType.ADDRESS);
				params.setQuery(Html.fromHtml(data.getQueryParameter("displayName")).toString());
				params.setSearchLatLon(lat, lng);
				Log.d(TAG, "Setting hotel search lat/lng: (" + lat + ", " + lng + ")");

				// Launch hotel search
				Log.i(TAG, "Launching hotel search from deep link!");
				NavUtils.goToHotels(this, params, null, NavUtils.FLAG_DEEPLINK);
			}
		}
		catch (Exception e) {
			Log.w(TAG, "Could not decode destination", e);
			NavUtils.goToLaunchScreen(this);
		}
	}

	private boolean kickoffLatLngSearch(BackgroundDownloader bgd, final Double lat, final Double lng) {
		boolean finish = true;
		try {
			// Check that lat/lng are valid
			if (lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180) {
				Log.d(TAG, "Setting hotel search lat/lng: (" + lat + ", " + lng + ")");
				finish = false;
				bgd.startDownload(DL_KEY_LAT_LNG, new BackgroundDownloader.Download<SuggestionResponse>() {
					@Override
					public SuggestionResponse doDownload() {
						ExpediaServices services = new ExpediaServices(DeepLinkRouterActivity.this);
						return services.suggestionsCityNearby(lat, lng);
					}
				}, mSuggestCallback);
			}
			else {
				Log.w(TAG, "Lat/lng out of valid range: (" + lat + ", " + lng + ")");
			}
		}
		catch (NumberFormatException e) {
			Log.w(TAG, "Could not parse latitude/longitude (" + lat + ", " + lng + ")", e);
		}
		return finish;
	}

	@Override
	protected void onResume() {
		super.onResume();
		BackgroundDownloader bgd = BackgroundDownloader.getInstance();
		if (bgd.isDownloading(DL_KEY_LAT_LNG)) {
			bgd.registerDownloadCallback(DL_KEY_LAT_LNG, mSuggestCallback);
		}
		if (bgd.isDownloading(DL_KEY_LOCATION_SUGGEST)) {
			bgd.registerDownloadCallback(DL_KEY_LOCATION_SUGGEST, mSuggestCallback);
		}
		if (bgd.isDownloading(DL_KEY_HOTEL_ID)) {
			bgd.registerDownloadCallback(DL_KEY_HOTEL_ID, mSuggestCallback);
		}
		if (bgd.isDownloading(DL_KEY_FLIGHT_SUGGEST)) {
			bgd.registerDownloadCallback(DL_KEY_FLIGHT_SUGGEST, mSuggestCallback);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		BackgroundDownloader bgd = BackgroundDownloader.getInstance();
		if (isFinishing()) {
			bgd.cancelDownload(DL_KEY_LAT_LNG);
			bgd.cancelDownload(DL_KEY_LOCATION_SUGGEST);
			bgd.cancelDownload(DL_KEY_HOTEL_ID);
			bgd.cancelDownload(DL_KEY_FLIGHT_SUGGEST);
		}
		else {
			bgd.unregisterDownloadCallback(DL_KEY_HOTEL_ID);
			bgd.unregisterDownloadCallback(DL_KEY_LOCATION_SUGGEST);
			bgd.unregisterDownloadCallback(DL_KEY_LAT_LNG);
			bgd.unregisterDownloadCallback(DL_KEY_FLIGHT_SUGGEST);
		}
	}

	private BackgroundDownloader.OnDownloadComplete<SuggestionResponse> mSuggestCallback = new BackgroundDownloader.OnDownloadComplete<SuggestionResponse>() {
		@Override
		public void onDownload(SuggestionResponse results) {
			if (results != null && results.getSuggestions().size() > 0) {
				SuggestionV2 destination = results.getSuggestions().get(0);
				if (mIsCurrentLocationSearch) {
					destination.setResultType(SuggestionV2.ResultType.CURRENT_LOCATION);
				}
				mSearchParams.setDestination(destination);
				NavUtils.goToTabletResults(DeepLinkRouterActivity.this, mSearchParams, mLobToLaunch);
			}
			else {
				Intent launchIntent = new Intent(DeepLinkRouterActivity.this, TabletLaunchActivity.class);
				startActivity(launchIntent);
			}
			finish();
		}
	};

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
		List<ChildTraveler> children = new ArrayList<>();
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
				Log.d(TAG, "Setting children ages: " + Arrays.toString(children.toArray(new ChildTraveler[0])));
				return children;
			}
		}
		catch (NumberFormatException e) {
			Log.w(TAG, "Could not parse childAges: " + childAgesStr, e);
		}

		return null;
	}
}
