package com.expedia.bookings.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import javax.inject.Inject;
import javax.net.ssl.SSLContext;

import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.text.TextUtils;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.AssociateUserToTripResponse;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.ChildTraveler;
import com.expedia.bookings.data.CreateItineraryResponse;
import com.expedia.bookings.data.CreateTripResponse;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightCheckoutResponse;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.FlightSearchResponse;
import com.expedia.bookings.data.FlightStatsFlightResponse;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.GsonResponse;
import com.expedia.bookings.data.HotelBookingResponse;
import com.expedia.bookings.data.HotelOffersResponse;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.HotelSearchResponse;
import com.expedia.bookings.data.Itinerary;
import com.expedia.bookings.data.LaunchDestinationCollections;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.PushNotificationRegistrationResponse;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.Response;
import com.expedia.bookings.data.ReviewSort;
import com.expedia.bookings.data.ReviewsResponse;
import com.expedia.bookings.data.RoutesResponse;
import com.expedia.bookings.data.SignInResponse;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.data.SuggestResponse;
import com.expedia.bookings.data.SuggestionResponse;
import com.expedia.bookings.data.SuggestionResultType;
import com.expedia.bookings.data.SuggestionSort;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.Traveler.AssistanceType;
import com.expedia.bookings.data.Traveler.Gender;
import com.expedia.bookings.data.TravelerCommitResponse;
import com.expedia.bookings.data.TripBucketItemFlight;
import com.expedia.bookings.data.TripBucketItemHotel;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.trips.Trip;
import com.expedia.bookings.data.trips.TripDetailsResponse;
import com.expedia.bookings.data.trips.TripResponse;
import com.expedia.bookings.data.trips.TripShareUrlShortenerResponse;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.notification.PushNotificationUtils;
import com.expedia.bookings.services.PersistentCookieManager;
import com.expedia.bookings.utils.BookingSuppressionUtils;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.ServicesUtil;
import com.expedia.bookings.utils.StethoShim;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.Ui;
import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;
import com.mobiata.android.BackgroundDownloader.DownloadListener;
import com.mobiata.android.Log;
import com.mobiata.android.util.NetUtils;
import com.mobiata.android.util.SettingUtils;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.data.FlightCode;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

@SuppressLint("SimpleDateFormat")
public class ExpediaServices implements DownloadListener {

	/**
	 * Tag reserved for request URLs (or params).  Often times we're only
	 * interested in the exact request being sent to the server.
	 */
	private static final String TAG_REQUEST = "EBRequest";

	// please note that these keys are specific to EB (for tracking purposes)
	// if you need FLEX API keys for another app, please obtain your own
	private static final String FS_FLEX_APP_ID = "db824f8c";
	private static final String FS_FLEX_APP_KEY = "6cf6ac9c083a45e93c6a290bf0cd442e";
	private static final String FS_FLEX_BASE_URI = "https://api.flightstats.com/flex";

	public static final int REVIEWS_PER_PAGE = 25;

	public static final int HOTEL_MAX_RESULTS = 200;

	public static final int FLIGHT_MAX_TRIPS = 1600;

	// Flags for getE3EndpointUrl()
	public static final int F_HOTELS = 4;
	public static final int F_FLIGHTS = 8;

	// Flags for addBillingInfo()
	public static final int F_HAS_TRAVELER = 16;

	// Flags for GET vs. POST
	private static final int F_GET = 32;
	private static final int F_POST = 64;

	// Skips all cookie sending/receiving
	private static final int F_IGNORE_COOKIES = 128;

	// Allows redirects.  You do not want this by default, as not following
	// redirects has revealed issues in the past.
	private static final int F_ALLOW_REDIRECT = 256;

	// Flag to indicate that we don't need to add the Endpoint while making an E3request
	public static final int F_DONT_ADD_ENDPOINT = 512;

	// Indicator that this request came from the widget, for tracking purposes
	public static final int F_FROM_WIDGET = 1024;

	private Context mContext;

	// We want to use the cached client for all our requests except the ones that ignore cookies
	@Inject
	public OkHttpClient mCachedClient;

	@Inject
	public PersistentCookieManager mCookieManager;

	@Inject
	public EndpointProvider mEndpointProvider;

	@Inject
	public SSLContext mSSLContext;

	private OkHttpClient mClient;
	private Request mRequest;

	// This is just so that the error messages aren't treated severely when a download is canceled - naturally,
	// things kind of crash and burn when you kill the connection midway through.
	private boolean mCancellingDownload;

	public ExpediaServices(Context context) {
		if (context == null) {
			throw new RuntimeException("Context passed to ExpediaServices cannot be null!");
		}
		mContext = context;

		Ui.getApplication(context).appComponent().inject(this);
	}

	private OkHttpClient makeOkHttpClient() {
		OkHttpClient client = new OkHttpClient();

		client.setReadTimeout(100L, TimeUnit.SECONDS);

		// 1902 - Allow redirecting from API calls
		client.setFollowSslRedirects(true);

		// When not a release build, allow SSL from all connections
		// Our test servers use self signed certs
		if (BuildConfig.DEBUG) {
			client.setSslSocketFactory(mSSLContext.getSocketFactory());
			client.setHostnameVerifier(new AllowAllHostnameVerifier());
		}

		// Add Stetho debugging network interceptor
		StethoShim.install(client);

		return client;
	}

	//////////////////////////////////////////////////////////////////////////
	// Cookies

	// Allows one to get the cookie store out of services, in case we need to
	// inject the cookies elsewhere (e.g., a WebView)
	public static List<HttpCookie> getCookies(Context context) {
		ExpediaServices services = new ExpediaServices(context);
		return services.mCookieManager.getCookieStore().getCookies();
	}

	public static void removeUserLoginCookies(Context context) {
		Log.d("Cookies: Removing user login cookies");
		String[] userCookieNames = {
			"user",
			"minfo",
			"accttype",
		};
		ExpediaServices services = new ExpediaServices(context);
		services.mCookieManager.removeNamedCookies(userCookieNames);
	}

	public void clearCookies() {
		Log.d("Cookies: Clearing!");
		mCookieManager.clear();
	}

	//////////////////////////////////////////////////////////////////////////
	// Expedia Suggest API
	//
	// Documentation:
	// https://confluence/display/POS/Expedia+Suggest+API+Family
	//
	// Examples (hotels):
	// http://suggest.expedia.com/hint/es/v1/ac/en_US/bellagio?type=30
	// http://suggest.expedia.com/hint/es/v1/ac/es_MX/seattle?type=30
	//
	// Examples (flights):
	// http://suggest.expedia.com/hint/es/v1/ac/en_US/new%20york?type=95&lob=Flights

	private enum SuggestType {
		AUTOCOMPLETE,
		NEARBY,
		HID,
		RID
	}

	public SuggestResponse suggest(String query, int flags) {
		if (query == null || query.length() < getMinSuggestQueryLength()) {
			return null;
		}

		String url = NetUtils.formatUrl(getSuggestUrl(4, SuggestType.AUTOCOMPLETE) + query);

		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();

		SuggestResponseHandler responseHandler = new SuggestResponseHandler();

		if ((flags & F_FLIGHTS) != 0) {
			// 95 is all regions (AIRPORT, CITY, MULTICITY, NEIGHBORHOOD, POI, METROCODE)
			params.add(new BasicNameValuePair("regiontype", "95"));
			params.add(new BasicNameValuePair("lob", "Flights"));
			params.add(new BasicNameValuePair("features", "nearby_airport"));

			responseHandler.setType(SuggestResponseHandler.Type.FLIGHTS);
		}
		else {
			// 255 is regions(95 Default) + hotels(128) + addresses(32)
			int regionType = SuggestionResultType.HOTEL | SuggestionResultType.AIRPORT | SuggestionResultType.CITY |
			SuggestionResultType.NEIGHBORHOOD | SuggestionResultType.POINT_OF_INTEREST | SuggestionResultType.REGION;
			params.add(new BasicNameValuePair("regiontype", "" + regionType));
			params.add(new BasicNameValuePair("lob", "hotels"));
			responseHandler.setType(SuggestResponseHandler.Type.HOTELS);
			params.add(new BasicNameValuePair("features", "ta_hierarchy"));
		}

		params.add(new BasicNameValuePair("locale", PointOfSale.getSuggestLocaleIdentifier()));
		params.add(new BasicNameValuePair("client", ServicesUtil.generateClientId(mContext)));

		Request.Builder get = createHttpGet(url, params);
		get.addHeader("Accept", "application/json");

		// Some logging before passing the request along^M
		Log.d(TAG_REQUEST, "Autosuggest request: " + url + "?" + NetUtils.getParamsForLogging(params));

		return doRequest(get, responseHandler, 0);
	}

	public SuggestionResponse suggestions(String query) {
		if (query == null || query.length() < getMinSuggestQueryLength()) {
			return null;
		}

		String url = NetUtils.formatUrl(getSuggestUrl(4, SuggestType.AUTOCOMPLETE) + query);

		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		int regionType = SuggestionResultType.HOTEL | SuggestionResultType.AIRPORT | SuggestionResultType.POINT_OF_INTEREST | SuggestionResultType.FLIGHT;
		params.add(new BasicNameValuePair("regiontype", "" + regionType));
		params.add(new BasicNameValuePair("features", "ta_hierarchy"));
		params.add(new BasicNameValuePair("locale", PointOfSale.getSuggestLocaleIdentifier()));
		params.add(new BasicNameValuePair("client", ServicesUtil.generateClientId(mContext)));

		return doSuggestionRequest(url, params);
	}

	public SuggestionResponse suggestionsAirportsNearby(double latitude, double longitude, SuggestionSort sort) {
		// 1 == airports
		return suggestionsNearby(latitude, longitude, sort, SuggestionResultType.AIRPORT);
	}

	public SuggestionResponse suggestionsCityNearby(double latitude, double longitude) {
		// 2 == city
		return suggestionsNearby(latitude, longitude, SuggestionSort.DISTANCE, SuggestionResultType.CITY);
	}

	private SuggestionResponse suggestionsNearby(double latitude, double longitude, SuggestionSort sort, int suggestionResultType) {
		String url = NetUtils.formatUrl(getSuggestUrl(4, SuggestType.NEARBY));

		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		addCommonParams(params);

		String sortCriteria = (sort == SuggestionSort.DISTANCE) ? "distance" : "popularity";
		params.add(new BasicNameValuePair("sortcriteria", sortCriteria));

		params.add(new BasicNameValuePair("regiontype", "" + suggestionResultType));
		params.add(new BasicNameValuePair("features", "ta_hierarchy"));
		params.add(new BasicNameValuePair("locale", PointOfSale.getSuggestLocaleIdentifier()));
		params.add(new BasicNameValuePair("client", ServicesUtil.generateClientId(mContext)));
		params.add(new BasicNameValuePair("maxradius", "150"));
		params.add(new BasicNameValuePair("maxresults", "50"));
		params.add(new BasicNameValuePair("latlong", latitude + "|" + longitude));

		return doSuggestionRequest(url, params);
	}

	public SuggestionResponse suggestionsHotelId(String hotelId) {
		String url = NetUtils.formatUrl(getSuggestUrl(2, SuggestType.HID));

		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();

		addCommonParams(params);

		params.add(new BasicNameValuePair("id", hotelId));

		return doSuggestionRequest(url, params);
	}

	public SuggestionResponse suggestionResolution(String regionId) {
		String urlBase = getSuggestUrl(1, SuggestType.RID);
		urlBase += "/";
		urlBase += regionId;
		String url = NetUtils.formatUrl(urlBase);

		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		addCommonParams(params);

		return doSuggestionRequest(url, params);
	}

	private SuggestionResponse doSuggestionRequest(String url, List<BasicNameValuePair> params) {
		Request.Builder get = createHttpGet(url, params);

		// Make sure the response comes back as JSON
		get.addHeader("Accept", "application/json");

		// Some logging before passing the request along
		Log.d(TAG_REQUEST, "Suggestion request: " + url + "?" + NetUtils.getParamsForLogging(params));

		return doRequest(get, new SuggestionResponseHandler(), 0);
	}

	/**
	 * Get the minimum number of characters required to provide drop down auto fill results.
	 * This is useful for languages like Japanese where Tokyo is spelt with 2 characters.
	 *
	 * @return min number of characters considered to be a valid query
	 */
	private int getMinSuggestQueryLength() {
		if (mContext != null) {
			return mContext.getResources().getInteger(R.integer.suggest_min_query_length);
		}
		else {
			return 3;
		}
	}

	private String getSuggestUrl(int version, SuggestType type) {
		StringBuilder sb = new StringBuilder();
		sb.append(mEndpointProvider.getEssEndpointUrl());
		sb.append("api/");
		// Version #
		sb.append("v" + Integer.toString(version) + "/");

		// Type
		switch (type) {
		case AUTOCOMPLETE:
			sb.append("typeahead/");
			break;
		case NEARBY:
			sb.append("nearby/");
			break;
		case HID:
			sb.append("hid/");
			break;
		case RID:
			sb.append("rid/");
			break;
		}

		return sb.toString();
	}

	//////////////////////////////////////////////////////////////////////////
	// Airport Dropdown Suggest

	public RoutesResponse flightRoutes() {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();

		addCommonParams(query);

		return doFlightsRequest("api/flight/airportDropDown", query, new RoutesResponseHandler(mContext), 0);
	}

	//////////////////////////////////////////////////////////////////////////
	// Expedia Flights API
	//
	// Documentation: http://www.expedia.com/static/mobile/APIConsole/flight.html

	public FlightSearchResponse flightSearch(FlightSearchParams params, int flags) {
		List<BasicNameValuePair> query = generateFlightSearchParams(params);
		return doFlightsRequest("api/flight/search", query, new StreamingFlightSearchResponseHandler(mContext), flags);
	}

	public List<BasicNameValuePair> generateFlightSearchParams(FlightSearchParams params) {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();

		// This code currently assumes that you are either making a one-way or round trip flight,
		// even though FlightSearchParams can be configured to handle multi-leg flights.
		//
		// Once e3 can handle these as well, we will want to update this code.
		query.add(new BasicNameValuePair("departureAirport", params.getDepartureLocation().getDestinationId()));
		query.add(new BasicNameValuePair("arrivalAirport", params.getArrivalLocation().getDestinationId()));

		DateTimeFormatter dtf = ISODateTimeFormat.date();

		query.add(new BasicNameValuePair("departureDate", dtf.print(params.getDepartureDate())));

		if (params.isRoundTrip()) {
			query.add(new BasicNameValuePair("returnDate", dtf.print(params.getReturnDate())));
		}

		query.add(new BasicNameValuePair("numberOfAdultTravelers", Integer.toString(params.getNumAdults())));
		addFlightChildTravelerParameters(query, params);

		addCommonParams(query);

		// Vary the max # of flights based on memory, so we don't run out.  Numbers are semi-educated guesses.
		//
		// TODO: Minimize the memory footprint so we don't have to keep doing this.
		int maxOfferCount;
		final int memClass = ((ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
		if (memClass <= 24) {
			maxOfferCount = 800;
		}
		else if (memClass <= 32) {
			maxOfferCount = 1200;
		}
		else {
			maxOfferCount = FLIGHT_MAX_TRIPS;
		}

		query.add(new BasicNameValuePair("maxOfferCount", Integer.toString(maxOfferCount)));

		query.add(new BasicNameValuePair("lccAndMerchantFareCheckoutAllowed", "true"));

		return query;
	}

	private void addFlightChildTravelerParameters(List<BasicNameValuePair> query, FlightSearchParams params) {
		List<ChildTraveler> children = params.getChildren();
		if (children != null) {
			for (ChildTraveler child : children) {
				query.add(new BasicNameValuePair("childTravelerAge", Integer.toString(child.getAge())));
			}
			query.add(new BasicNameValuePair("infantSeatingInLap", Boolean.toString(params.getInfantSeatingInLap())));
		}
	}

	public CreateItineraryResponse createItinerary(String productKey, int flags) {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();
		query.add(new BasicNameValuePair("productKey", productKey));

		addCommonParams(query);

		return doFlightsRequest("api/flight/trip/create", query, new CreateItineraryResponseHandler(mContext), flags);
	}

	public FlightCheckoutResponse flightCheckout(TripBucketItemFlight flightItem, BillingInfo billingInfo,
		List<Traveler> travelers, int flags) {
		List<BasicNameValuePair> query = generateFlightCheckoutParams(flightItem, billingInfo, travelers);

		Itinerary itinerary = flightItem.getItinerary();
		Log.v("tealeafTransactionId for flight: " + itinerary.getTealeafId());
		addTealeafId(query, itinerary.getTealeafId());

		return doFlightsRequest("api/flight/checkout", query, new FlightCheckoutResponseHandler(mContext), flags);
	}

	public List<BasicNameValuePair> generateFlightCheckoutParams(TripBucketItemFlight flightItem,
		BillingInfo billingInfo, List<Traveler> travelers) {
		FlightTrip flightTrip = flightItem.getFlightTrip();
		Itinerary itinerary = flightItem.getItinerary();

		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();

		query.add(new BasicNameValuePair("tripId", itinerary.getTripId()));
		query.add(new BasicNameValuePair("expectedTotalFare", flightTrip.getTotalFare().getAmount().toString() + ""));
		query.add(new BasicNameValuePair("expectedFareCurrencyCode", flightTrip.getTotalFare().getCurrency()));
		query.add(new BasicNameValuePair("abacusUserGuid", Db.getAbacusGuid()));

		Money cardFee = flightItem.getPaymentFee(billingInfo.getPaymentType());
		if (cardFee != null) {
			query.add(new BasicNameValuePair("expectedCardFee", cardFee.getAmount().toString() + ""));
			query.add(new BasicNameValuePair("expectedCardFeeCurrencyCode", cardFee.getCurrency()));
		}

		addBillingInfo(query, billingInfo, F_HAS_TRAVELER);

		String prefix;
		for (int i = 0; i < travelers.size(); i++) {
			if (i == 0) {
				//NOTE: the values associated with mainFlightPassenger will take precedence over values in BillingInfo
				prefix = "mainFlightPassenger.";
			}
			else {
				prefix = "associatedFlightPassengers[" + Integer.toString(i - 1) + "].";
			}
			addFlightTraveler(query, travelers.get(i), prefix);
		}

		query.add(new BasicNameValuePair("validateWithChildren", "true"));

		String nameOnCard = billingInfo.getNameOnCard();
		if (!TextUtils.isEmpty(nameOnCard)) {
			query.add(new BasicNameValuePair("nameOnCard", nameOnCard));
		}

		// Checkout calls without this flag can make ACTUAL bookings!
		if (suppressFinalFlightBooking(mContext)) {
			query.add(new BasicNameValuePair("suppressFinalBooking", "true"));
		}

		if (User.isLoggedIn(mContext)) {
			query.add(new BasicNameValuePair("doIThinkImSignedIn", "true"));
			query.add(new BasicNameValuePair("storeCreditCardInUserProfile",
				billingInfo.getSaveCardToExpediaAccount() ? "true" : "false"));
		}

		addCommonParams(query);

		return query;
	}

	// Suppress final bookings if we're not in release mode and the preference is set to suppress
	private static boolean suppressFinalFlightBooking(Context context) {
		return BookingSuppressionUtils.shouldSuppressFinalBooking(context, R.string.preference_suppress_flight_bookings);
	}

	private static boolean suppressFinalHotelBooking(Context context) {
		return BookingSuppressionUtils.shouldSuppressFinalBooking(context, R.string.preference_suppress_hotel_bookings);
	}


	////////////////////////////////////////////////////////////////////////////////////
	// Ancillary flight data

	/**
	 * Download an svg from the provided url.
	 * Used for download airport terminal maps
	 * Sometimes this is gzipped and we account for that
	 *
	 * @param url
	 * @return
	 */
	public SVG getSvgFromUrl(String url) {
		try {
			URL dlUrl = new URL(url);
			URLConnection connection = dlUrl.openConnection();
			connection.setRequestProperty("accept-encoding", "gzip");
			InputStream stream = connection.getInputStream();
			if ("gzip".equalsIgnoreCase(connection.getContentEncoding())) {
				stream = new GZIPInputStream(stream);
			}
			return SVGParser.getSVGFromInputStream(stream);
		}
		catch (Exception ex) {
			Log.e("Exception downloading svg", ex);
		}
		return null;
	}

	public Flight getUpdatedFlight(Flight flight) {
		ArrayList<BasicNameValuePair> parameters = new ArrayList<BasicNameValuePair>();

		addCommonFlightStatsParams(parameters);

		String baseUrl;

		if (flight.mFlightHistoryId != -1) {
			// get based on flight history id
			baseUrl = FS_FLEX_BASE_URI + "/flightstatus/rest/v2/json/flight/status/" + flight.mFlightHistoryId + "?";
		}
		else {
			// get based on flight number
			FlightCode flightCode = flight.getPrimaryFlightCode();
			DateTime departure = flight.getOriginWaypoint().getBestSearchDateTime();
			baseUrl = FS_FLEX_BASE_URI + "/flightstatus/rest/v2/json/flight/status/" + flightCode.mAirlineCode + "/"
				+ flightCode.mNumber.trim()
				+ "/dep/" + departure.getYear() + "/" + departure.getMonthOfYear() + "/"
				+ departure.getDayOfMonth() + "?";

			parameters.add(new BasicNameValuePair("utc", "false"));
			parameters.add(new BasicNameValuePair("airport", flight.getOriginWaypoint().mAirportCode));
		}

		FlightStatsFlightResponse response = doFlightStatsRequest(baseUrl, parameters,
			new FlightStatsFlightStatusResponseHandler(flight.getPrimaryFlightCode().mAirlineCode));
		if (response == null) {
			return null;
		}
		else {
			List<Flight> flights = response.getFlights();
			if (flights == null || flights.size() == 0) {
				return null;
			}
			else if (flights.size() == 1) {
				return flights.get(0);
			}
			else {
				String destAirportCode = flight.getDestinationWaypoint().mAirportCode;
				if (destAirportCode != null) {
					for (Flight updatedFlight : flights) {
						// Assumptions:
						//  1) all results have identical airline, flight number, departure airport, departure date
						//  2) results do NOT include two flights on the same exact route
						// Which means, the only piece of information that we need to check is the arrival airport
						if (destAirportCode.equals(updatedFlight.getDestinationWaypoint().mAirportCode)) {
							return updatedFlight;
						}
					}
				}

				// last chance catch-all (somehow we got results that didn't match)
				return null;
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////////////
	// FlightStats Ratings API: https://developer.flightstats.com/api-docs/ratings/v1

	//////////////////////////////////////////////////////////////////////////
	// Expedia hotel API
	//
	// Documentation: http://www.expedia.com/static/mobile/APIConsole/

	public HotelSearchResponse search(HotelSearchParams params, int flags) {
		List<BasicNameValuePair> query = generateHotelSearchParams(params, flags);

		HotelSearchResponseHandler rh = new HotelSearchResponseHandler(mContext);
		if (params.hasSearchLatLon()) {
			rh.setLatLng(params.getSearchLatitude(), params.getSearchLongitude());
		}
		rh.setNumNights(params.getStayDuration());

		return doE3Request("m/api/hotel/search", query, rh, 0);
	}

	public List<BasicNameValuePair> generateHotelSearchParams(HotelSearchParams params, int flags) {
		List<BasicNameValuePair> query = new ArrayList<>();

		query.add(new BasicNameValuePair("sortOrder", "ExpertPicks"));
		addCommonParams(query);

		if (params.hasRegionId()) {
			Log.d("Searching by regionId...");
			query.add(new BasicNameValuePair("regionId", params.getRegionId()));
		}
		else if (params.hasSearchLatLon()) {
			Log.d("Searching by latitude/longitude...");
			query.add(new BasicNameValuePair("latitude", params.getSearchLatitude() + ""));
			query.add(new BasicNameValuePair("longitude", params.getSearchLongitude() + ""));
		}
		else if (params.hasQuery()) {
			Log.d("Searching by city...");
			query.add(new BasicNameValuePair("city", params.getQuery()));
		}

		addHotelSearchParams(query, params);

		if ((flags & F_FROM_WIDGET) != 0) {
			query.add(new BasicNameValuePair("fromWidget", "true"));
		}

		// These values are always the same (for now)
		query.add(new BasicNameValuePair("resultsPerPage", HOTEL_MAX_RESULTS + ""));
		query.add(new BasicNameValuePair("pageIndex", "0"));
		query.add(new BasicNameValuePair("filterUnavailable", "true"));
		query.add(new BasicNameValuePair("enableSponsoredListings", "true"));
		return query;
	}

	public HotelOffersResponse availability(HotelSearchParams params, Property property) {
		List<BasicNameValuePair> query = generateHotelAvailabilityParams(params, property);

		HotelOffersResponseHandler responseHandler = new HotelOffersResponseHandler(mContext, params);

		return doE3Request("m/api/hotel/offers", query, responseHandler, 0);
	}

	public List<BasicNameValuePair> generateHotelAvailabilityParams(HotelSearchParams params, Property property) {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();

		addCommonParams(query);

		//If we have a valid property/propertyId we add it, otherwise we leave it off and let the api respond accordingly
		if (property != null && !TextUtils.isEmpty(property.getPropertyId())) {
			query.add(new BasicNameValuePair("hotelId", property.getPropertyId()));
		}

		// Note: this may stress the mobile API, but the whole point of this flag is so that we can take advantage of
		// discounted room rates for our mobile use-case.
		query.add(new BasicNameValuePair("useCacheForAirAttach", "true"));

		if (params != null) {
			addHotelSearchParams(query, params);
		}

		return query;
	}

	/**
	 * This leverages the same classes as hotel offers, but simply returns less data (since
	 * it won't have any actual rates).
	 */
	public HotelOffersResponse hotelInformation(Property property) {
		List<BasicNameValuePair> query = generateHotelAvailabilityParams(null, property);

		HotelOffersResponseHandler responseHandler = new HotelOffersResponseHandler(mContext, null);

		return doE3Request("m/api/hotel/info", query, responseHandler, 0);
	}

	public CreateTripResponse createTrip(HotelSearchParams params, Property property, Rate rate,
		boolean qualifyAirAttach) {
		List<BasicNameValuePair> query = generateCreateTripParams(rate, params, qualifyAirAttach);
		CreateTripResponseHandler responseHandler = new CreateTripResponseHandler(mContext, params, property);
		return doE3Request("m/api/hotel/trip/create", query, responseHandler);
	}

	private List<BasicNameValuePair> generateCreateTripParams(Rate rate, HotelSearchParams params,
		boolean qualifyAirAttach) {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();
		query.add(new BasicNameValuePair("productKey", rate.getRateKey()));

		String guests = generateHotelGuestString(params);
		query.add(new BasicNameValuePair("roomInfoFields[0].room", guests));

		query.add(new BasicNameValuePair("qualifyAirAttach", Boolean.toString(qualifyAirAttach)));
		addCommonParams(query);

		return query;
	}

	public CreateTripResponse applyCoupon(String couponCode, TripBucketItemHotel hotel) {
		List<BasicNameValuePair> query = generateApplyCouponParams(couponCode, hotel);
		Property property = hotel.getProperty();
		HotelSearchParams params = hotel.getHotelSearchParams();
		CreateTripResponseHandler responseHandler = new CreateTripResponseHandler(mContext, params, property);
		return doE3Request("api/m/trip/coupon", query, responseHandler);
	}

	public List<BasicNameValuePair> generateApplyCouponParams(String couponCode, TripBucketItemHotel hotel) {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();

		addCommonParams(query);

		query.add(new BasicNameValuePair("tripId", hotel.getCreateTripResponse().getTripId()));
		query.add(new BasicNameValuePair("coupon.code", couponCode));

		return query;
	}

	public CreateTripResponse removeCoupon(TripBucketItemHotel hotel) {
		List<BasicNameValuePair> query = generateRemoveCouponParams(hotel);
		Property property = hotel.getProperty();
		HotelSearchParams params = hotel.getHotelSearchParams();
		CreateTripResponseHandler responseHandler = new CreateTripResponseHandler(mContext, params, property);
		return doE3Request("api/m/trip/remove/coupon", query, responseHandler);
	}

	public List<BasicNameValuePair> generateRemoveCouponParams(TripBucketItemHotel hotel) {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();
		addCommonParams(query);
		query.add(new BasicNameValuePair("tripId", hotel.getCreateTripResponse().getTripId()));
		return query;
	}

	public HotelBookingResponse reservation(HotelSearchParams params, Rate rate,
		BillingInfo billingInfo,
		String tripId, String userId, Long tuid, String tealeafId, boolean isMerEmailOptIn) {
		List<BasicNameValuePair> query = generateHotelReservationParams(params, rate, billingInfo, tripId, userId,
			tuid);

		Log.v("tealeafTransactionId for hotel: " + tealeafId);
		addTealeafId(query, tealeafId);

		// #4762. Adding MER email opt in choice
		query.add(new BasicNameValuePair("emailOptIn", String.valueOf(isMerEmailOptIn)));

		return doE3Request("m/api/hotel/trip/checkout", query, new BookingResponseHandler(mContext));
	}

	public List<BasicNameValuePair> generateHotelReservationParams(HotelSearchParams params, Rate rate,
		BillingInfo billingInfo, String tripId, String userId, Long tuid) {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();

		addCommonParams(query);

		query.add(new BasicNameValuePair("expectedTotalFare", "" + rate.getTotalPriceWithMandatoryFees().getAmount()));
		query.add(
			new BasicNameValuePair("expectedFareCurrencyCode", rate.getTotalPriceWithMandatoryFees().getCurrency()));

		addHotelSearchParams(query, params);

		addBillingInfo(query, billingInfo, F_HOTELS);

		query.add(new BasicNameValuePair("sendEmailConfirmation", "true"));

		if (!TextUtils.isEmpty(tripId)) {
			query.add(new BasicNameValuePair("tripId", tripId));
		}

		// Response with user id. Get it from the sign in response first.
		if (tuid != null) {
			query.add(new BasicNameValuePair("userId", String.valueOf(tuid)));
		}
		else if (!TextUtils.isEmpty(userId)) {
			query.add(new BasicNameValuePair("userId", userId));
		}

		if (User.isLoggedIn(mContext)) {
			query.add(new BasicNameValuePair("doIThinkImSignedIn", "true"));
			query.add(new BasicNameValuePair("storeCreditCardInUserProfile",
				billingInfo.getSaveCardToExpediaAccount() ? "true" : "false"));
		}

		// Checkout calls without this flag can make ACTUAL bookings!
		if (suppressFinalHotelBooking(mContext)) {
			query.add(new BasicNameValuePair("suppressFinalBooking", "true"));
		}

		query.add(new BasicNameValuePair("abacusUserGuid", Db.getAbacusGuid()));

		return query;
	}

	private void addHotelSearchParams(List<BasicNameValuePair> query, HotelSearchParams params) {
		DateTimeFormatter dtf = ISODateTimeFormat.date();
		query.add(new BasicNameValuePair("checkInDate", dtf.print(params.getCheckInDate())));
		query.add(new BasicNameValuePair("checkOutDate", dtf.print(params.getCheckOutDate())));

		addHotelGuestParamater(query, params);
	}

	private void addHotelGuestParamater(List<BasicNameValuePair> query, HotelSearchParams params) {
		query.add(new BasicNameValuePair("room1", generateHotelGuestString(params)));
	}

	private String generateHotelGuestString(HotelSearchParams params) {
		StringBuilder guests = new StringBuilder();
		guests.append(params.getNumAdults());
		List<ChildTraveler> children = params.getChildren();
		if (children != null) {
			for (ChildTraveler child : children) {
				guests.append("," + child.getAge());
			}
		}

		return guests.toString();
	}

	private void addTealeafId(List<BasicNameValuePair> query, String tealeafId) {
		if (!TextUtils.isEmpty(tealeafId)) {
			query.add(new BasicNameValuePair("tlPaymentsSubmitEvent", "1"));
			query.add(new BasicNameValuePair("tealeafTransactionId", tealeafId));
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Expedia Itinerary API
	//
	// Documentation: https://www.expedia.com/static/mobile/APIConsole/trip.html

	public TripResponse getTrips(boolean getCachedDetails) {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();
		addCommonParams(query);
		query.add(new BasicNameValuePair("filterBookingStatus", "PENDING"));
		query.add(new BasicNameValuePair("filterBookingStatus", "BOOKED"));
		query.add(new BasicNameValuePair("filterTimePeriod", "UPCOMING"));
		query.add(new BasicNameValuePair("filterTimePeriod", "INPROGRESS"));
		query.add(new BasicNameValuePair("filterTimePeriod", "RECENTLY_COMPLETED"));
		query.add(new BasicNameValuePair("sort", "SORT_STARTDATE_ASCENDING"));

		if (getCachedDetails) {
			query.add(new BasicNameValuePair("getCachedDetails", "10"));
		}

		return doE3Request("api/trips", query, new TripResponseHandler(mContext), F_GET);
	}

	public TripDetailsResponse getTripDetails(Trip trip, boolean useCache) {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();

		addCommonParams(query);

		int flags = F_GET;

		// Always use tripNumber for guests, tripId for logged in
		String tripIdentifier;
		if (trip.isGuest()) {
			// You must always use trip number for guest itineraries
			tripIdentifier = trip.getTripNumber();

			query.add(new BasicNameValuePair("email", trip.getGuestEmailAddress()));

			// This param is deprecated; remove it once it's safely removed from prod
			query.add(new BasicNameValuePair("idtype", "itineraryNumber"));

			flags |= F_IGNORE_COOKIES;
		}
		else {
			tripIdentifier = trip.getTripId();
		}

		query.add(new BasicNameValuePair("useCache", useCache ? "1" : "0"));

		return doE3Request("api/trips/" + tripIdentifier, query, new TripDetailsResponseHandler(mContext), flags);
	}

	//////////////////////////////////////////////////////////////////////////
	// Expedia ItinSharing API
	//

	public TripDetailsResponse getSharedItin(String shareableUrl) {
		int flags = F_GET | F_DONT_ADD_ENDPOINT | F_IGNORE_COOKIES;
		return doE3Request(shareableUrl, null, new TripDetailsResponseHandler(mContext), flags);
	}

	public TripShareUrlShortenerResponse getShortenedShareItinUrl(String longUrl) {
		int flags = F_ALLOW_REDIRECT | F_IGNORE_COOKIES | F_POST;

		//Only one argument!
		JSONObject args = new JSONObject();
		try {
			args.putOpt("long_url", longUrl);
		}
		catch (JSONException e) {
			Log.e("Couldn't add the long_url to the argument json");
		}

		String shortUrl = mEndpointProvider.getShortlyEndpointUrl() + "/v1/shorten";

		Request.Builder post = new Request.Builder().url(shortUrl);
		RequestBody body = RequestBody.create(MediaType.parse("application/json"), args.toString());
		post.post(body);

		// Make sure the response comes back as JSON
		post.addHeader("Accept", "application/json");

		return doRequest(post, new TripShareUrlShortenerHandler(), flags);
	}

	//////////////////////////////////////////////////////////////////////////
	// Expedia user account API
	//
	// Documentation: https://www.expedia.com/static/mobile/APIConsole/flight.html

	// Attempt to sign in again with the stored cookie
	public SignInResponse signIn(int flags) {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();

		addCommonParams(query);

		query.add(new BasicNameValuePair("profileOnly", "true"));
		query.add(new BasicNameValuePair("includeFullPaymentProfile", "true"));

		addProfileTypes(query, flags);

		return doE3Request("api/user/sign-in", query, new SignInResponseHandler());
	}

	public AssociateUserToTripResponse associateUserToTrip(String tripId, int flags) {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();

		addCommonParams(query);

		query.add(new BasicNameValuePair("tripId", tripId));

		addProfileTypes(query, flags);

		return doE3Request("api/user/associateUserToTrip", query, new AssociateUserToTripResponseHandler(mContext));
	}

	/**
	 * Retrieve the full traveler details information, used to fetch full associated traveler data that
	 * does not get returned in the sign-in call.
	 *
	 * @param traveler
	 * @param flags
	 * @return
	 */
	public SignInResponse travelerDetails(Traveler traveler, int flags) {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();

		addCommonParams(query);

		query.add(new BasicNameValuePair("tuid", "" + traveler.getTuid()));

		addProfileTypes(query, flags | F_FLIGHTS | F_HOTELS);

		return doE3Request("api/user/profile", query, new SignInResponseHandler());
	}

	/**
	 * Update (or create) an expedia account traveler
	 *
	 * @param traveler
	 * @return
	 */
	public TravelerCommitResponse commitTraveler(Traveler traveler) {
		if (User.isLoggedIn(mContext)) {
			List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();
			addFlightTraveler(query, traveler, "");
			addCommonParams(query);
			Log.i(TAG_REQUEST, "update-travler body:" + NetUtils.getParamsForLogging(query));
			return doFlightsRequest("api/user/update-traveler", query, new TravelerCommitResponseHandler(mContext,
				traveler));
		}
		else {
			return null;
		}
	}

	private void addProfileTypes(List<BasicNameValuePair> query, int flags) {
		List<String> profileTypes = new ArrayList<String>();

		if ((flags & F_HOTELS) != 0) {
			profileTypes.add("HOTEL");
		}
		if ((flags & F_FLIGHTS) != 0) {
			profileTypes.add("FLIGHT");
		}

		query.add(new BasicNameValuePair("profileTypes", TextUtils.join(",", profileTypes)));
	}

	//////////////////////////////////////////////////////////////////////////
	// Expedia Common

	private void addCommonParams(List<BasicNameValuePair> query) {
		// Source type
		query.add(new BasicNameValuePair("sourceType", ServicesUtil.generateSourceType()));

		String langId = ServicesUtil.generateLangId();
		if (Strings.isNotEmpty(langId)) {
			query.add(new BasicNameValuePair("langid", langId));
		}

		if (mEndpointProvider.requestRequiresSiteId()) {
			query.add(new BasicNameValuePair("siteid", ServicesUtil.generateSiteId()));
		}

		query.add(new BasicNameValuePair("clientid", ServicesUtil.generateClientId(mContext)));
	}

	private void addCommonFlightStatsParams(List<BasicNameValuePair> query) {
		query.add(new BasicNameValuePair("appId", FS_FLEX_APP_ID));
		query.add(new BasicNameValuePair("appKey", FS_FLEX_APP_KEY));
	}

	private void addBillingInfo(List<BasicNameValuePair> query, BillingInfo billingInfo, int flags) {
		if ((flags & F_HAS_TRAVELER) == 0) {
			// Don't add firstname/lastname if we're adding it through the traveler interface later
			query.add(new BasicNameValuePair("firstName", billingInfo.getFirstName()));
			query.add(new BasicNameValuePair("lastName", billingInfo.getLastName()));
			query.add(new BasicNameValuePair("phoneCountryCode", billingInfo.getTelephoneCountryCode()));
			query.add(new BasicNameValuePair("phone", billingInfo.getTelephone()));
			query.add(new BasicNameValuePair("nameOnCard", billingInfo.getNameOnCard()));
		}

		query.add(new BasicNameValuePair("email", billingInfo.getEmail()));

		StoredCreditCard scc = billingInfo.getStoredCard();
		if (scc == null) {
			Location location = billingInfo.getLocation();
			if ((flags & F_HOTELS) != 0) {
				// 130 Hotels reservation requires only postalCode for US POS, no billing info for other POS
				if (location != null && !TextUtils.isEmpty(location.getPostalCode())) {
					query.add(new BasicNameValuePair("postalCode", location.getPostalCode()));
				}
			}
			else {
				// F670: Location can be null if we are using a stored credit card
				if (location != null && location.getStreetAddress() != null) {
					String address = location.getStreetAddress().get(0);
					if (!TextUtils.isEmpty(address)) {
						query.add(new BasicNameValuePair("streetAddress", address));
					}
					if (location.getStreetAddress().size() > 1) {
						String address2 = location.getStreetAddress().get(1);
						if (!TextUtils.isEmpty(address2)) {
							query.add(new BasicNameValuePair("streetAddress2", address2));
						}
					}
					if (!TextUtils.isEmpty(location.getCity())) {
						query.add(new BasicNameValuePair("city", location.getCity()));
					}
					if (!TextUtils.isEmpty(location.getStateCode())) {
						query.add(new BasicNameValuePair("state", location.getStateCode()));
					}
					// #1056. Flights booking postalCode check depends on the billing country chosen during checkout.
					if (!TextUtils.isEmpty(location.getPostalCode())) {
						query.add(new BasicNameValuePair("postalCode", location.getPostalCode()));
					}
					query.add(new BasicNameValuePair("country", location.getCountryCode()));
				}
			}

			query.add(new BasicNameValuePair("creditCardNumber", billingInfo.getNumber()));

			LocalDate expDate = billingInfo.getExpirationDate();

			query.add(new BasicNameValuePair("expirationDate", JodaUtils.format(expDate, "MMyy")));

			// This is an alternative way of representing expiration date, used for Flights.
			// Doesn't hurt to include both methods
			query.add(new BasicNameValuePair("expirationDateYear", JodaUtils.format(expDate, "yyyy")));
			query.add(new BasicNameValuePair("expirationDateMonth", JodaUtils.format(expDate, "MM")));
		}
		else {
			query.add(new BasicNameValuePair("storedCreditCardId", billingInfo.getStoredCard().getId()));
			/*
			 *  The new checkout API requires this field.
			 *  As of this comment, after signIn we only get the storedCreditCardId. The API has to also send us back it's associated nameOnCard.
			 *  We have already filed a defect with the API team, for now let's just send the first and lastName.
			 */
			query.add(
				new BasicNameValuePair("nameOnCard", billingInfo.getFirstName() + " " + billingInfo.getLastName()));
		}
		query.add(new BasicNameValuePair("cvv", billingInfo.getSecurityCode()));
	}

	private void addFlightTraveler(List<BasicNameValuePair> query, Traveler traveler, String prefix) {
		DateTimeFormatter dtf = ISODateTimeFormat.date();
		query.add(new BasicNameValuePair(prefix + "firstName", traveler.getFirstName()));
		if (!TextUtils.isEmpty(traveler.getMiddleName())) {
			query.add(new BasicNameValuePair(prefix + "middleName", traveler.getMiddleName()));
		}
		query.add(new BasicNameValuePair(prefix + "lastName", traveler.getLastName()));
		query.add(new BasicNameValuePair(prefix + "birthDate", dtf.print(traveler.getBirthDate())));
		query.add(new BasicNameValuePair(prefix + "gender", (traveler.getGender() == Gender.MALE) ? "MALE" : "FEMALE"));
		query.add(new BasicNameValuePair(prefix + "passengerCategory", traveler.getPassengerCategory().toString()));
		String assistanceOption;
		if (traveler.getAssistance() != null) {
			assistanceOption = traveler.getAssistance().name();
		}
		else {
			assistanceOption = AssistanceType.NONE.name();
		}
		query.add(new BasicNameValuePair(prefix + "specialAssistanceOption", assistanceOption));
		query.add(new BasicNameValuePair(prefix + "seatPreference", traveler.getSafeSeatPreference().name()));

		if (!TextUtils.isEmpty(traveler.getPhoneCountryCode())) {
			query.add(new BasicNameValuePair(prefix + "phoneCountryCode", traveler.getPhoneCountryCode()));
		}
		if (!TextUtils.isEmpty(traveler.getPhoneNumber())) {
			query.add(new BasicNameValuePair(prefix + "phone", traveler.getPrimaryPhoneNumber().getNumber()));
		}

		//Email is required (but there is no traveler email entry)
		String email = traveler.getEmail();
		if (TextUtils.isEmpty(email)) {
			email = Db.getBillingInfo().getEmail();
		}
		if (TextUtils.isEmpty(email)) {
			email = Db.getUser().getPrimaryTraveler().getEmail();
		}

		query.add(new BasicNameValuePair(prefix + "email", email));

		if (!TextUtils.isEmpty(traveler.getPrimaryPassportCountry())) {
			query.add(new BasicNameValuePair(prefix + "passportCountryCode", traveler.getPrimaryPassportCountry()));
		}
		if (!TextUtils.isEmpty(traveler.getRedressNumber())) {
			query.add(new BasicNameValuePair(prefix + "TSARedressNumber", traveler.getRedressNumber()));
		}
		if (traveler.hasTuid()) {
			query.add(new BasicNameValuePair(prefix + "tuid", traveler.getTuid().toString()));
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Push Notifications

	public PushNotificationRegistrationResponse registerForPushNotifications(
		String serverUrl, ResponseHandler<PushNotificationRegistrationResponse> responseHandler,
		JSONObject payload, String regId) {

		String appNameForMobiataPushNameHeader = ProductFlavorFeatureConfiguration.getInstance().getAppNameForMobiataPushNameHeader();
		if (Strings.isEmpty(appNameForMobiataPushNameHeader)) {
			Log.d("PushNotification registration key is null/blank in feature config!");
			return null;
		}

		// Create the request
		Request.Builder post = new Request.Builder().url(serverUrl);
		String data = payload.toString();
		RequestBody body = RequestBody.create(MediaType.parse("application/json"), data);

		// Adding the body sets the Content-type header for us
		post.post(body);

		if (PushNotificationUtils.REGISTRATION_URL_PRODUCTION.equals(serverUrl)) {
			post.addHeader("MobiataPushName", appNameForMobiataPushNameHeader);
		}
		else {
			post.addHeader("MobiataPushName", appNameForMobiataPushNameHeader + "Alpha");
		}

		if (BuildConfig.RELEASE
			|| !SettingUtils.get(mContext, mContext.getString(R.string.preference_disable_push_registration), false)) {

			synchronized (PushNotificationUtils.getLockObject(regId)) {
				//We first check to see if we have already sent this payload for this regId
				if (PushNotificationUtils.sendPayloadCheck(regId, payload)) {
					//If not we go ahead and do the request
					PushNotificationRegistrationResponse response = doRequest(post, responseHandler, F_POST);
					if (response == null || !response.getSuccess()) {
						//If we failed to register, remove the payload from our map, so we dont prevent ourselves form trying again later.
						PushNotificationUtils.removePayloadFromMap(regId);
					}
					return response;
				}
				else {
					return null;
				}
			}
		}
		else {
			Log.d("PushNotification registration is disabled in settings!");
			return null;
		}
	}

	public PushNotificationRegistrationResponse registerForPushNotifications(
		ResponseHandler<PushNotificationRegistrationResponse> responseHandler, JSONObject payload, String regId) {
		String serverUrl = PushNotificationUtils.getRegistrationUrl(mContext);
		return registerForPushNotifications(serverUrl, responseHandler, payload, regId);
	}

	//////////////////////////////////////////////////////////////////////////
	// User Reviews API
	//
	// API Console: http://test.reviewsvc.expedia.com/APIConsole?segmentedapi=true

	public ReviewsResponse reviews(Property property, ReviewSort sort, int pageNumber) {
		return reviews(property, sort, pageNumber, REVIEWS_PER_PAGE);
	}

	public ReviewsResponse reviews(Property property, ReviewSort sort, int pageNumber, int numReviewsPerPage) {
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();

		params.add(new BasicNameValuePair("_type", "json"));
		params.add(new BasicNameValuePair("sortBy", sort.getSortByApiParam()));
		params.add(new BasicNameValuePair("start", Integer.toString(pageNumber * numReviewsPerPage)));
		params.add(new BasicNameValuePair("items", Integer.toString(numReviewsPerPage)));
		List<BasicNameValuePair> additionalParamsForReviewsRequest = ProductFlavorFeatureConfiguration.getInstance().getAdditionalParamsForReviewsRequest();
		if (additionalParamsForReviewsRequest != null) {
			for (BasicNameValuePair param : additionalParamsForReviewsRequest) {
				params.add(param);
			}
		}
		return doReviewsRequest(getReviewsUrl(property), params, new ReviewsResponseHandler());
	}

	private String getReviewsUrl(Property property) {
		String url = mEndpointProvider.getReviewsEndpointUrl();
		url += "api/hotelreviews/hotel/";
		url += property.getPropertyId();
		return url;
	}

	//////////////////////////////////////////////////////////////////////////
	// Launch data

	private String getLaunchEndpointUrl() {
		return mEndpointProvider.getE3EndpointUrl() + "/static/mobile/LaunchDestinations";
	}

	public LaunchDestinationCollections getLaunchCollections(String localeString) {
		String lowerPos = PointOfSale.getPointOfSale().getTwoLetterCountryCode().toLowerCase(Locale.US);
		String url = getLaunchEndpointUrl() + "/" + lowerPos + "/collections_" + localeString + ".json";
		GsonResponse<LaunchDestinationCollections> result = doLaunchDataRequest(url, null,
			LaunchDestinationCollections.class);
		if (result == null) {
			return null;
		}
		return result.get();
	}

	//////////////////////////////////////////////////////////////////////////
	// Request code

	private <T extends Response> T doFlightsRequest(String targetUrl, List<BasicNameValuePair> params,
		ResponseHandler<T> responseHandler) {
		return doFlightsRequest(targetUrl, params, responseHandler, F_FLIGHTS);
	}

	private <T extends Response> T doFlightsRequest(String targetUrl, List<BasicNameValuePair> params,
		ResponseHandler<T> responseHandler, int flags) {
		return doE3Request(targetUrl, params, responseHandler, flags | F_FLIGHTS);
	}

	private <T extends Response> T doE3Request(String targetUrl, List<BasicNameValuePair> params,
		ResponseHandler<T> responseHandler) {
		return doE3Request(targetUrl, params, responseHandler, 0);
	}

	private <T extends Response> T doE3Request(String targetUrl, List<BasicNameValuePair> params,
		ResponseHandler<T> responseHandler, int flags) {
		String serverUrl;

		/*
		 * If a user shares an itin, then the shareableUrl will be pointing to that particular POS in which the trip was purchased.
		 * When we make a request to download the shared itin data, we won't fetch the EndPointUrl of the device.
		 * This is to make sure that an itin shared from a different POS will still be fetched
		 * and displayed on a device with any POS set.
		 */
		if ((flags & F_DONT_ADD_ENDPOINT) != 0) {
			serverUrl = targetUrl;
		}
		else {
			serverUrl = mEndpointProvider.getE3EndpointUrl() + targetUrl;
		}

		// Create the request
		Request.Builder base;
		if ((flags & F_GET) != 0) {
			base = createHttpGet(serverUrl, params);
		}
		else {
			base = createHttpPost(serverUrl, params);
		}

		if (!ExpediaBookingApp.isAutomation()) {
			base.addHeader("x-eb-client", ServicesUtil.generateXEbClientString(mContext));
		}
		// Some logging before passing the request along
		Log.d(TAG_REQUEST, "Request: " + serverUrl + "?" + NetUtils.getParamsForLogging(params));

		return doRequest(base, responseHandler, flags);
	}

	private <T extends Response> T doBasicGetRequest(String url, List<BasicNameValuePair> params,
		ResponseHandler<T> responseHandler) {

		Request.Builder base = createHttpGet(url, params);

		Log.d(TAG_REQUEST, "" + url + "?" + NetUtils.getParamsForLogging(params));

		return doRequest(base, responseHandler, F_IGNORE_COOKIES);
	}

	private <T extends Response> T doFlightStatsRequest(String baseUrl, List<BasicNameValuePair> params,
		ResponseHandler<T> responseHandler) {
		Request.Builder base = createHttpGet(baseUrl, params);
		return doRequest(base, responseHandler, F_IGNORE_COOKIES);
	}

	private <T extends Response> T doReviewsRequest(String url, List<BasicNameValuePair> params,
		ResponseHandler<T> responseHandler) {
		Request.Builder get = createHttpGet(url, params);

		Log.d(TAG_REQUEST, "User reviews request: " + url + "?" + NetUtils.getParamsForLogging(params));

		return doRequest(get, responseHandler, F_IGNORE_COOKIES);
	}

	private <T> GsonResponse<T> doLaunchDataRequest(String url, List<BasicNameValuePair> params, Class<T> clazz) {
		Request.Builder get = createHttpGet(url, params);

		Log.d(TAG_REQUEST, "Launch destination data request: " + url + "?" + NetUtils.getParamsForLogging(params));

		return (GsonResponse) doRequest(get, new AutoJsonResponseHandler(clazz), F_IGNORE_COOKIES);
	}

	private <T extends Response> T doRequest(Request.Builder request, ResponseHandler<T> responseHandler, int flags) {
		final String userAgent = ServicesUtil.generateUserAgentString(mContext);

		mClient = mCachedClient;
		request.addHeader("User-Agent", userAgent);
		request.addHeader("Accept-Encoding", "gzip");

		final boolean ignoreCookies = (flags & F_IGNORE_COOKIES) != 0;
		if (ignoreCookies) {
			// We don't want cookies so we cannot use the cached client
			mClient = makeOkHttpClient();
			mClient.setCookieHandler(sBlackHoleCookieManager);
		}

		// Make the request
		long start = System.currentTimeMillis();
		mCancellingDownload = false;
		com.squareup.okhttp.Response response = null;
		try {
			mRequest = request.build();
			response = mClient.newCall(mRequest).execute();
			T processedResponse = responseHandler.handleResponse(response);
			return processedResponse;
		}
		catch (IOException e) {
			if (mCancellingDownload) {
				Log.d("Request was canceled.", e);
			}
			else {
				Log.e("Server request failed.", e);
			}
		}
		finally {
			if (response != null) {
				try {
					response.body().close();
				}
				catch (IOException e) {
					Log.e("Response body failed to close:", e);
				}
			}
			Log.d("Total request time: " + (System.currentTimeMillis() - start) + " ms");
			mRequest = null;
		}

		return null;
	}

	private boolean doGet(String url, List<BasicNameValuePair> params) {
		Log.d(TAG_REQUEST, "" + url + "?" + NetUtils.getParamsForLogging(params));

		Request.Builder request = createHttpGet(url, params);
		final String userAgent = ServicesUtil.generateUserAgentString(mContext);

		mClient = mCachedClient;
		request.addHeader("User-Agent", userAgent);
		request.addHeader("Accept-Encoding", "gzip");

		// Make the request
		long start = System.currentTimeMillis();
		mCancellingDownload = false;
		com.squareup.okhttp.Response response = null;
		try {
			mRequest = request.build();
			response = mClient.newCall(mRequest).execute();
			return response.code() == 200;
		}
		catch (IOException e) {
			if (mCancellingDownload) {
				Log.d("Request was canceled.", e);
			}
			else {
				Log.e("Server request failed.", e);
			}
		}
		finally {
			if (response != null) {
				try {
					response.body().close();
				}
				catch (IOException e) {
					Log.e("Response body failed to close:", e);
				}
			}
			Log.d("Total request time: " + (System.currentTimeMillis() - start) + " ms");
			mRequest = null;
		}

		return false;
	}


	//////////////////////////////////////////////////////////////////////////
	// Download listener stuff

	@Override
	public void onCancel() {
		mCancellingDownload = true;

		if (mRequest != null) {
			Log.i("Cancelling download!");
			cancelAndWait();
		}
	}

	synchronized private void cancelAndWait() {
		final CountDownLatch latch = new CountDownLatch(1);
		Thread bgThread = new Thread(new Runnable() {
			@Override
			public void run() {
				if (mRequest != null) {
					mClient.cancel(mRequest);
					mRequest = null;
				}
				latch.countDown();
			}
		});

		bgThread.start();
		try {
			latch.await();
		}
		catch (Throwable t) {
			throw new RuntimeException("Problem cancelling download", t);
		}
	}

	private static final CookieManager sBlackHoleCookieManager = new CookieManager(null, CookiePolicy.ACCEPT_NONE);

	public String getLongUrl(String shortUrl) {
		try {
			URL dlUrl = new URL(shortUrl);
			HttpURLConnection connection = (HttpURLConnection) dlUrl.openConnection();
			connection.setInstanceFollowRedirects(false);
			connection.setRequestMethod("GET");
			String longUrl = connection.getHeaderField("Location");
			return longUrl;
		}
		catch (Exception e) {
			Log.e("Exception getting the long url", e);
		}
		return null;
	}

	private Request.Builder createHttpGet(String url, List<BasicNameValuePair> params) {
		String outUrl = url;
		if (params != null && params.size() > 0) {
			if (!outUrl.endsWith("?")) {
				outUrl += "?";
			}

			String encodedParams = URLEncodedUtils.format(params, "UTF-8");
			if (encodedParams != null) {
				outUrl += encodedParams;
			}
		}
		return new Request.Builder().url(outUrl);

	}

	private Request.Builder createHttpPost(String url, List<BasicNameValuePair> params) {
		String data = "";
		if (params != null && params.size() > 0) {
			data = URLEncodedUtils.format(params, "UTF-8");
		}

		Request.Builder req = new Request.Builder().url(url);
		RequestBody body = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), data);
		req.post(body);
		return req;
	}

	public boolean trackTravelAd(String url, List<BasicNameValuePair> params) {
		if (TextUtils.isEmpty(url)) {
			return false;
		}

		if (mEndpointProvider.getEndPoint() == EndPoint.CUSTOM_SERVER
			|| mEndpointProvider.getEndPoint() == EndPoint.MOCK_MODE) {
			try {
				URL originalURL = new URL(url);
				url = mEndpointProvider.getCustomServerAddress()
					.substring(0, mEndpointProvider.getCustomServerAddress().length() - 1) + originalURL.getFile();
			}
			catch (MalformedURLException ex) {
				Log.e("Exception modifying url", ex);
			}
		}

		return doGet(url, params);
	}

}
