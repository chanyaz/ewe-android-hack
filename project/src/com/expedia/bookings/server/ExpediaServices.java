package com.expedia.bookings.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.AssociateUserToTripResponse;
import com.expedia.bookings.data.BackgroundImageResponse;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.BookingResponse;
import com.expedia.bookings.data.CreateItineraryResponse;
import com.expedia.bookings.data.CreateTripResponse;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.ExpediaImageManager.ImageType;
import com.expedia.bookings.data.FacebookLinkResponse;
import com.expedia.bookings.data.FlightCheckoutResponse;
import com.expedia.bookings.data.FlightSearchHistogramResponse;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.FlightSearchResponse;
import com.expedia.bookings.data.FlightStatsFlightResponse;
import com.expedia.bookings.data.FlightStatsRatingResponse;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.HotelAffinitySearchResponse;
import com.expedia.bookings.data.HotelOffersResponse;
import com.expedia.bookings.data.HotelProductResponse;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.HotelSearchResponse;
import com.expedia.bookings.data.Itinerary;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.PushNotificationRegistrationResponse;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.Response;
import com.expedia.bookings.data.ReviewSort;
import com.expedia.bookings.data.ReviewsResponse;
import com.expedia.bookings.data.RoutesResponse;
import com.expedia.bookings.data.SamsungWalletResponse;
import com.expedia.bookings.data.Scenario;
import com.expedia.bookings.data.ScenarioResponse;
import com.expedia.bookings.data.ScenarioSetResponse;
import com.expedia.bookings.data.SignInResponse;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.data.SuggestResponse;
import com.expedia.bookings.data.SuggestionResponse;
import com.expedia.bookings.data.SuggestionSort;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.Traveler.AssistanceType;
import com.expedia.bookings.data.Traveler.Gender;
import com.expedia.bookings.data.TravelerCommitResponse;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.WalletPromoResponse;
import com.expedia.bookings.data.WalletPromoResponseHandler;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.trips.Trip;
import com.expedia.bookings.data.trips.TripDetailsResponse;
import com.expedia.bookings.data.trips.TripResponse;
import com.expedia.bookings.data.trips.TripShareUrlShortenerResponse;
import com.expedia.bookings.notification.PushNotificationUtils;
import com.expedia.bookings.utils.JodaUtils;
import com.facebook.Session;
import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;
import com.mobiata.android.BackgroundDownloader.DownloadListener;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.IoUtils;
import com.mobiata.android.util.NetUtils;
import com.mobiata.android.util.SettingUtils;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.data.FlightCode;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

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

	private static final String EXPEDIA_SUGGEST_BASE_URL = "http://suggest.expedia.com/hint/es/";

	public static final int REVIEWS_PER_PAGE = 25;

	public static final int HOTEL_MAX_RESULTS = 200;

	public static final int FLIGHT_MAX_TRIPS = 1600;

	private static final String COOKIES_FILE = "cookies.dat";

	// Flags for doRequest()
	private static final int F_SECURE_REQUEST = 1;

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
	private static OkHttpClient sCachedClient;
	private static HttpCookieStore sCookieStore;
	private OkHttpClient mClient;
	private Request mRequest;

	// This is just so that the error messages aren't treated severely when a download is canceled - naturally,
	// things kind of crash and burn when you kill the connection midway through.
	private boolean mCancellingDownload;

	public ExpediaServices(Context context) {
		mContext = context;
	}

	public static void init(Context context) {
		sCachedClient = makeOkHttpClient(context);

		sCookieStore = new HttpCookieStore();
		sCookieStore.init(context);

		ExpediaCookiePolicy policy = new ExpediaCookiePolicy();
		policy.updateSettings(context);

		sCachedClient.setCookieHandler(new CookieManager(sCookieStore, policy));
	}

	private static OkHttpClient makeOkHttpClient(Context context) {
		OkHttpClient client = new OkHttpClient();

		client.setReadTimeout(100L, TimeUnit.SECONDS);

		// 1902 - Allow redirecting from API calls
		client.setFollowProtocolRedirects(true);

		// When not a release build, allow SSL from all connections
		// Our test servers use self signed certs
		if (!AndroidUtils.isRelease(context)) {
			try {
				SSLContext socketContext = SSLContext.getInstance("TLS");
				socketContext.init(null, sEasyTrustManager, new java.security.SecureRandom());
				client.setSslSocketFactory(socketContext.getSocketFactory());
			}
			catch (Exception e) {
				Log.w("Something sad happened during manipulation of SSL", e);
			}
		}

		return client;
	}

	//////////////////////////////////////////////////////////////////////////
	// Cookies

	// Allows one to get the cookie store out of services, in case we need to
	// inject the cookies elsewhere (e.g., a WebView)
	public static List<HttpCookie> getCookies(Context context) {
		HttpCookieStore cs = new HttpCookieStore();
		// Load what we have on disk
		cs.init(context);
		return cs.getCookies();
	}

	public static void removeUserLoginCookies(Context context) {
		Log.d("Cookies: Removing user login cookies");
		String[] userCookieNames = {
			"user",
			"minfo",
			"accttype",
		};
		sCookieStore.removeAllCookiesByName(userCookieNames);
	}

	public void clearCookies() {
		Log.d("Cookies: Clearing!");
		sCookieStore.removeAll();
	}

	//////////////////////////////////////////////////////////////////////////
	// User-Agent

	/**
	 * Constructs a user agent string to be used against Expedia requests. It is important to exclude the word "Android"
	 * otherwise mobile redirects occur when we don't want them. This is useful for all API requests contained here
	 * in ExpediaServices as well as certain requests through WebViewActivity in order to prevent the redirects.
	 *
	 * @param context
	 * @return
	 */
	public static String getUserAgentString(Context context) {
		// Construct a proper user agent string
		String versionName;
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
			versionName = pi.versionName;
		}
		catch (Exception e) {
			// PackageManager is traditionally wonky, need to accept all exceptions here.
			Log.w("Couldn't get package info in order to submit proper version #!", e);
			versionName = "1.0";
		}
		// Be careful not to use the word "Android" here
		// https://mingle/projects/e3_mobile_web/cards/676
		String userAgent = "ExpediaBookings/" + versionName + " (EHad; Mobiata)";
		return userAgent;
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
		NEARBY
	}

	public SuggestResponse suggest(String query, int flags) {
		if (query == null || query.length() < getMinSuggestQueryLength()) {
			return null;
		}

		String url = NetUtils.formatUrl(getSuggestUrl(2, SuggestType.AUTOCOMPLETE) + "/" + query);

		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();

		SuggestResponseHandler responseHandler = new SuggestResponseHandler();

		if ((flags & F_FLIGHTS) != 0) {
			// 95 is all regions (AIRPORT, CITY, MULTICITY, NEIGHBORHOOD, POI, METROCODE)
			params.add(new BasicNameValuePair("type", "95"));
			params.add(new BasicNameValuePair("lob", "Flights"));

			responseHandler.setType(SuggestResponseHandler.Type.FLIGHTS);
		}
		else {
			// 255 is regions(95 Default) + hotels(128) + addresses(32)
			params.add(new BasicNameValuePair("type", "255"));

			responseHandler.setType(SuggestResponseHandler.Type.HOTELS);
		}

		Request.Builder get = createHttpGet(url, params);
		get.addHeader("Accept", "application/json");

		// Some logging before passing the request along^M
		Log.d(TAG_REQUEST, "Autosuggest request: " + url + "?" + NetUtils.getParamsForLogging(params));

		return doRequest(get, responseHandler, 0);
	}

	public SuggestionResponse suggestions(String query, int flags) {
		if (query == null || query.length() < getMinSuggestQueryLength()) {
			return null;
		}

		String url = NetUtils.formatUrl(getSuggestUrl(3, SuggestType.AUTOCOMPLETE) + "/" + query);

		return doSuggestionRequest(url, null);
	}

	public SuggestionResponse suggestionsNearby(double latitude, double longitude, SuggestionSort sort, int flags) {
		String url = NetUtils.formatUrl(getSuggestUrl(1, SuggestType.NEARBY));

		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();

		addCommonParams(params);

		params.add(new BasicNameValuePair("latlong", latitude + "|" + longitude));

		// 1 == airports, which is all that's supported for now
		params.add(new BasicNameValuePair("type", "1"));

		if (sort == SuggestionSort.DISTANCE) {
			params.add(new BasicNameValuePair("sort", "d"));
		}
		else {
			// Default to popularity sort
			params.add(new BasicNameValuePair("sort", "p"));
		}

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
		sb.append(EXPEDIA_SUGGEST_BASE_URL);

		// Version #
		sb.append("v" + Integer.toString(version) + "/");

		// Type
		switch (type) {
		case AUTOCOMPLETE:
			sb.append("ac/");
			break;
		case NEARBY:
			sb.append("nearby/");
			break;
		}

		// Locale identifier
		sb.append(PointOfSale.getSuggestLocaleIdentifier());

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
		List<BasicNameValuePair> query = generateFlightSearchParams(params, flags);
		return doFlightsRequest("api/flight/search", query, new StreamingFlightSearchResponseHandler(mContext), flags);
	}

	public List<BasicNameValuePair> generateFlightSearchParams(FlightSearchParams params, int flags) {
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

	public CreateItineraryResponse createItinerary(String productKey, int flags) {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();
		query.add(new BasicNameValuePair("productKey", productKey));

		addCommonParams(query);

		return doFlightsRequest("api/flight/trip/create", query, new CreateItineraryResponseHandler(mContext), flags
			| F_SECURE_REQUEST);
	}

	public FlightCheckoutResponse flightCheckout(FlightTrip flightTrip, Itinerary itinerary, BillingInfo billingInfo,
		List<Traveler> travelers, int flags) {
		List<BasicNameValuePair> query = generateFlightCheckoutParams(flightTrip, itinerary, billingInfo, travelers,
			flags);
		return doFlightsRequest("api/flight/checkout", query, new FlightCheckoutResponseHandler(mContext), flags
			+ F_SECURE_REQUEST);
	}

	public List<BasicNameValuePair> generateFlightCheckoutParams(FlightTrip flightTrip, Itinerary itinerary,
		BillingInfo billingInfo, List<Traveler> travelers, int flags) {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();

		query.add(new BasicNameValuePair("tripId", itinerary.getTripId()));
		query.add(new BasicNameValuePair("expectedTotalFare", flightTrip.getTotalFare().getAmount().toString() + ""));
		query.add(new BasicNameValuePair("expectedFareCurrencyCode", flightTrip.getTotalFare().getCurrency()));

		Money cardFee = flightTrip.getCardFee(billingInfo.getCardType());
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
		return !AndroidUtils.isRelease(context)
			&& SettingUtils.get(context, context.getString(R.string.preference_suppress_flight_bookings), true);
	}

	private static boolean suppressFinalHotelBooking(Context context) {
		return !AndroidUtils.isRelease(context)
			&& SettingUtils.get(context, context.getString(R.string.preference_suppress_hotel_bookings), true);
	}

	//////////////////////////////////////////////////////////////////////////
	// Images API

	public BackgroundImageResponse getExpediaImage(ImageType imageType, String imageCode, int width, int height) {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();

		addCommonParams(query);

		query.add(new BasicNameValuePair("imageType", imageType.getIdentifier()));
		query.add(new BasicNameValuePair("imageCode", imageCode));
		query.add(new BasicNameValuePair("imageWidth", Integer.toString(width)));
		query.add(new BasicNameValuePair("imageHeight", Integer.toString(height)));

		return doFlightsRequest("api/mobile/image", query, new BackgroundImageResponseHandler(mContext), 0);
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
			Calendar departure = flight.mOrigin.getBestSearchDateTime();
			baseUrl = FS_FLEX_BASE_URI + "/flightstatus/rest/v2/json/flight/status/" + flightCode.mAirlineCode + "/"
				+ flightCode.mNumber.trim()
				+ "/dep/" + departure.get(Calendar.YEAR) + "/" + (departure.get(Calendar.MONTH) + 1) + "/"
				+ departure.get(Calendar.DAY_OF_MONTH) + "?";

			parameters.add(new BasicNameValuePair("utc", "false"));
			parameters.add(new BasicNameValuePair("airport", flight.mOrigin.mAirportCode));
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
				String destAirportCode = flight.mDestination.mAirportCode;
				if (destAirportCode != null) {
					for (Flight updatedFlight : flights) {
						// Assumptions:
						//  1) all results have identical airline, flight number, departure airport, departure date
						//  2) results do NOT include two flights on the same exact route
						// Which means, the only piece of information that we need to check is the arrival airport
						if (destAirportCode.equals(updatedFlight.mDestination.mAirportCode)) {
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

	public FlightStatsRatingResponse getFlightStatsRating(Flight flight) {
		ArrayList<BasicNameValuePair> parameters = new ArrayList<BasicNameValuePair>();

		addCommonFlightStatsParams(parameters);

		String airlineCode = flight.getPrimaryFlightCode().mAirlineCode;
		String airlineNum = flight.getPrimaryFlightCode().mNumber;
		String baseUrl = FS_FLEX_BASE_URI + "/ratings/rest/v1/json/flight/" + airlineCode + "/" + airlineNum;

		return doFlightStatsRequest(baseUrl, parameters, new FlightStatsRatingResponseHandler());
	}

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

		if (!AndroidUtils.isRelease(mContext)) {
			boolean disabled = SettingUtils
				.get(mContext, mContext.getString(R.string.preference_disable_domain_v2_hotel_search), false);

			if (!disabled) {
				query.add(new BasicNameValuePair("forceV2Search", "true"));
			}
		}

		return doE3Request("MobileHotel/Webapp/SearchResults", query, rh, 0);
	}

	public List<BasicNameValuePair> generateHotelSearchParams(HotelSearchParams params, int flags) {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();

		if (getEndPoint(mContext) == EndPoint.MOCK_SERVER) {
			query.add(new BasicNameValuePair("city", "saved_product"));
			return query;
		}

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

		return query;
	}

	public HotelOffersResponse availability(HotelSearchParams params, Property property) {
		List<BasicNameValuePair> query = generateHotelAvailabilityParams(params, property);

		HotelOffersResponseHandler responseHandler = new HotelOffersResponseHandler(mContext, params, property);

		return doE3Request("MobileHotel/Webapp/HotelOffers", query, responseHandler, 0);
	}

	public List<BasicNameValuePair> generateHotelAvailabilityParams(HotelSearchParams params, Property property) {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();

		addCommonParams(query);

		//If we have a valid property/propertyId we add it, otherwise we leave it off and let the api respond accordingly
		if (property != null && !TextUtils.isEmpty(property.getPropertyId())) {
			query.add(new BasicNameValuePair("hotelId", property.getPropertyId()));
		}

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

		HotelOffersResponseHandler responseHandler = new HotelOffersResponseHandler(mContext, null, property);

		return doE3Request("MobileHotel/Webapp/HotelInformation", query, responseHandler, 0);
	}

	public HotelProductResponse hotelProduct(HotelSearchParams params, Property property, Rate rate) {
		List<BasicNameValuePair> query = generateHotelProductParmas(params, property, rate);
		HotelProductResponseHandler responseHandler = new HotelProductResponseHandler(mContext, params, property, rate);
		return doE3Request("MobileHotel/Webapp/HotelProduct", query, responseHandler, 0);
	}

	public List<BasicNameValuePair> generateHotelProductParmas(HotelSearchParams params, Property property, Rate rate) {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();

		// Adds sourceType
		addCommonParams(query);

		query.add(new BasicNameValuePair("productKey", rate.getRateKey()));

		// For room1 parameter
		addHotelGuestParamater(query, params);

		return query;
	}

	public CreateTripResponse createTrip(HotelSearchParams params, Property property) {
		List<BasicNameValuePair> query = generateCreateTripParams(property, params);
		CreateTripResponseHandler responseHandler = new CreateTripResponseHandler(mContext, params, property);
		return doE3Request("api/m/trip/create", query, responseHandler, F_SECURE_REQUEST);
	}

	public List<BasicNameValuePair> generateCreateTripParams(Property property, HotelSearchParams params) {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();

		query.add(new BasicNameValuePair("productKey", Db.getHotelSearch().getSelectedRate().getRateKey()));
		query.add(
			new BasicNameValuePair("roomInfoFields[0].room", "" + (params.getNumAdults() + params.getNumChildren())));

		return query;
	}

	public CreateTripResponse applyCoupon(String couponCode, HotelSearchParams params, Property property) {
		List<BasicNameValuePair> query = generateApplyCouponParams(couponCode);
		CreateTripResponseHandler responseHandler = new CreateTripResponseHandler(mContext, params, property);
		return doE3Request("api/m/trip/coupon", query, responseHandler, F_SECURE_REQUEST);
	}

	public List<BasicNameValuePair> generateApplyCouponParams(String couponCode) {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();

		addCommonParams(query);

		query.add(new BasicNameValuePair("tripId", Db.getHotelSearch().getCreateTripResponse().getTripId()));
		query.add(new BasicNameValuePair("coupon.code", couponCode));

		return query;
	}

	public BookingResponse reservation(HotelSearchParams params, Property property, Rate rate, BillingInfo billingInfo,
		String tripId, String userId, Long tuid) {
		List<BasicNameValuePair> query = generateHotelReservationParams(params, rate, billingInfo, tripId,
			userId, tuid);

		return doE3Request("api/hotel/checkout", query, new BookingResponseHandler(mContext), F_SECURE_REQUEST);
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

		return query;
	}

	private void addHotelSearchParams(List<BasicNameValuePair> query, HotelSearchParams params) {
		DateTimeFormatter dtf = ISODateTimeFormat.date();
		query.add(new BasicNameValuePair("checkInDate", dtf.print(params.getCheckInDate())));
		query.add(new BasicNameValuePair("checkOutDate", dtf.print(params.getCheckOutDate())));

		addHotelGuestParamater(query, params);
	}

	private void addHotelGuestParamater(List<BasicNameValuePair> query, HotelSearchParams params) {
		StringBuilder guests = new StringBuilder();
		guests.append(params.getNumAdults());
		List<Integer> children = params.getChildren();
		if (children != null) {
			for (int child : children) {
				guests.append("," + child);
			}
		}

		query.add(new BasicNameValuePair("room1", guests.toString()));
	}

	//////////////////////////////////////////////////////////////////////////
	// Hotels Affinity Search
	//
	// Documentation: https://confluence/display/POS/Affinity+Search+API
	//


	// TODO determine the correct endpoint to hit
	private static final String HOTELS_AFFINITY_SEARCH_BASE_URL = "http://afs.integration.bgb.karmalab.net:52418/affinity/api/v1/get/hotels";

	public HotelAffinitySearchResponse hotelAffinitySearch(HotelSearchParams params) {
		List<BasicNameValuePair> query = generateHotelAffinitySearchParams(params);
		return doBasicGetRequest(HOTELS_AFFINITY_SEARCH_BASE_URL, query, new HotelAffinitySearchResponseHandler());
	}

	public List<BasicNameValuePair> generateHotelAffinitySearchParams(HotelSearchParams params) {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();

		query.add(new BasicNameValuePair("format", "json"));

		query.add(new BasicNameValuePair("userId", "ama")); // TODO get real client ID

		if (params.hasRegionId()) {
			Log.d("Searching by regionId...");
			query.add(new BasicNameValuePair("regionId", params.getRegionId()));
		}
		else {
			// TODO support current loc, etc..
			throw new RuntimeException("Attempting an affinity search that is not yet supported");
		}

		return query;
	}

	//////////////////////////////////////////////////////////////////////////
	// Global Deals Engine API (GDE) - Flights
	//
	// Documentation: https://confluence/display/MTTFG/Global+Deals+Engine+-+GDE
	//
	// Flights: https://confluence/display/MTTFG/GDE+Flights+API+Documentation

	public FlightSearchHistogramResponse flightSearchHistogram(FlightSearchParams params) {
		List<BasicNameValuePair> query = generateFlightSearchHistogramParams(params);
		return doBasicGetRequest(getGdeEndpointUrl(), query, new FlightSearchHistogramResponseHandler());
	}

	public List<BasicNameValuePair> generateFlightSearchHistogramParams(FlightSearchParams params) {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();

		Location destination = params.getArrivalLocation();
		String destKey = destination.isMetroCode() ? "tripToMetroAirportCode" : "tripTo";
		query.add(new BasicNameValuePair(destKey, destination.getDestinationId()));

		Location origin = params.getDepartureLocation();
		if (origin != null) {
			String origKey = origin.isMetroCode() ? "tripFromMetroAirportCode" : "tripFrom";
			query.add(new BasicNameValuePair(origKey, origin.getDestinationId()));
		}

		// TODO the API might update and no longer require this field
		query.add(new BasicNameValuePair("pos", PointOfSale.getPointOfSale().getTwoLetterCountryCode()));

		return query;
	}

	//////////////////////////////////////////////////////////////////////////
	// Expedia Itinerary API
	//
	// Documentation: https://www.expedia.com/static/mobile/APIConsole/trip.html

	public TripResponse getTrips(boolean getCachedDetails, int flags) {
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

		return doE3Request("api/trips", query, new TripResponseHandler(mContext), F_SECURE_REQUEST | F_GET);
	}

	public TripDetailsResponse getTripDetails(Trip trip, boolean useCache) {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();

		addCommonParams(query);

		int flags = F_SECURE_REQUEST | F_GET;

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
		int flags = F_SECURE_REQUEST | F_GET | F_DONT_ADD_ENDPOINT;
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

		Request.Builder post = new Request.Builder().url("http://e.xpda.co/v1/shorten");
		Request.Body body = Request.Body.create(MediaType.parse("application/json"), args.toString());
		post.post(body);

		// Make sure the response comes back as JSON
		post.addHeader("Accept", "application/json");

		return doRequest(post, new TripShareUrlShortenerHandler(), flags);
	}

	//////////////////////////////////////////////////////////////////////////
	// Expedia user account API
	//
	// Documentation: https://www.expedia.com/static/mobile/APIConsole/flight.html

	public SignInResponse signIn(String email, String password, int flags) {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();

		addCommonParams(query);

		query.add(new BasicNameValuePair("email", email));
		query.add(new BasicNameValuePair("password", password));
		query.add(new BasicNameValuePair("staySignedIn", "true"));
		query.add(new BasicNameValuePair("includeFullPaymentProfile", "true"));

		addProfileTypes(query, flags);

		// Make sure we're signed out before we try to sign in again
		if (User.isLoggedIn(mContext)) {
			User.signOut(mContext);
		}

		return doE3Request("MobileHotel/Webapp/SignIn", query, new SignInResponseHandler(mContext), F_SECURE_REQUEST);
	}

	// Attempt to sign in again with the stored cookie
	public SignInResponse signIn(int flags) {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();

		addCommonParams(query);

		query.add(new BasicNameValuePair("profileOnly", "true"));
		query.add(new BasicNameValuePair("includeFullPaymentProfile", "true"));

		addProfileTypes(query, flags);

		return doE3Request("MobileHotel/Webapp/SignIn", query, new SignInResponseHandler(mContext), F_SECURE_REQUEST);
	}

	public AssociateUserToTripResponse associateUserToTrip(String tripId, int flags) {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();

		addCommonParams(query);

		query.add(new BasicNameValuePair("tripId", tripId));

		addProfileTypes(query, flags);

		return doE3Request("api/user/associateUserToTrip", query, new AssociateUserToTripResponseHandler(mContext),
			F_SECURE_REQUEST);
	}

	public SignInResponse updateTraveler(Traveler traveler, int flags) {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();

		addCommonParams(query);

		query.add(new BasicNameValuePair("tuid", "" + traveler.getTuid()));

		addProfileTypes(query, flags | F_FLIGHTS | F_HOTELS);

		return doE3Request("api/user/profile", query, new SignInResponseHandler(mContext), F_SECURE_REQUEST);
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
				traveler), F_SECURE_REQUEST);
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
	// Samsung Wallet ticket creation

	public SamsungWalletResponse getSamsungWalletTicketId(String itineraryId) {
		return doE3Request("api/common/samsungwallet/" + itineraryId, null, new SamsungWalletResponseHandler(
			mContext), 0);
	}

	//////////////////////////////////////////////////////////////////////////
	// Google Wallet coupon promotion

	public WalletPromoResponse googleWalletPromotionEnabled() {
		return doE3Request("static/mobile/walletcheck", null, new WalletPromoResponseHandler(), 0);
	}

	//////////////////////////////////////////////////////////////////////////
	// Expedia Common

	private void addCommonParams(List<BasicNameValuePair> query) {
		// Source type
		query.add(new BasicNameValuePair("sourceType", "mobileapp"));

		// Point of sale information
		int langId = PointOfSale.getPointOfSale().getDualLanguageId();
		if (langId != 0) {
			query.add(new BasicNameValuePair("langid", Integer.toString(langId)));
		}

		if (!AndroidUtils.isRelease(mContext) && getEndPoint(mContext) == EndPoint.PUBLIC_INTEGRATION) {
			query.add(new BasicNameValuePair("siteid", Integer.toString(PointOfSale.getPointOfSale()
				.getSiteId())));
		}

		// Client id (see https://confluence/display/POS/ewe+trips+api#ewetripsapi-apiclients)
		query.add(new BasicNameValuePair("clientid", "expedia.phone.android:" + AndroidUtils.getAppVersion(mContext)));
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
		if (scc == null || scc.isGoogleWallet()) {
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
					query.add(new BasicNameValuePair("streetAddress", location.getStreetAddress().get(0)));
					if (location.getStreetAddress().size() > 1) {
						String address2 = location.getStreetAddress().get(1);
						if (!TextUtils.isEmpty(address2)) {
							query.add(new BasicNameValuePair("streetAddress2", address2));
						}
					}
					query.add(new BasicNameValuePair("city", location.getCity()));
					query.add(new BasicNameValuePair("state", location.getStateCode()));
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

			query.add(new BasicNameValuePair(prefix + "phone", traveler.getPrimaryPhoneNumber().getAreaCode()
				+ traveler.getPrimaryPhoneNumber().getNumber()));
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
	// Facebook Login API
	//
	// Note: This is the api for working with Expedia in regard to a facebook login.
	// The calls to facebook itself are handled by the FB sdk, and currently happen in LoginFragment.java

	/**
	 * We want to user our production facebbok app id for hitting prod, otherwise use our dev one.
	 *
	 * @param context
	 * @return
	 */
	public static String getFacebookAppId(Context context) {
		EndPoint endPoint = getEndPoint(context);
		String appId = null;
		switch (endPoint) {
		case INTEGRATION:
		case STABLE:
		case DEV:
		case TRUNK:
		case PUBLIC_INTEGRATION:
		case PROXY:
		case CUSTOM_SERVER:
			appId = context.getString(R.string.facebook_dev_app_id);
			break;
		case PRODUCTION:
		default:
			appId = context.getString(R.string.facebook_app_id);
			break;
		}
		return appId;

	}

	/**
	 * Login to expedia using facebook credentials
	 *
	 * @param facebookUserId
	 * @param facebookAccessToken
	 * @return
	 */
	public FacebookLinkResponse facebookAutoLogin(String facebookUserId, String facebookAccessToken) {
		Session fbSession = Session.getActiveSession();
		if (fbSession == null || fbSession.isClosed()) {
			throw new RuntimeException("We must be logged into facebook inorder to call facebookAutoLogin");
		}

		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();
		query.add(new BasicNameValuePair("provider", "Facebook"));
		query.add(new BasicNameValuePair("userId", facebookUserId));
		query.add(new BasicNameValuePair("accessToken", facebookAccessToken));

		return doE3Request("api/auth/autologin", query, new FacebookLinkResponseHandler(mContext), F_SECURE_REQUEST);
	}

	/**
	 * Create a new expedia user, and associate that user with the provided facebook account
	 *
	 * @param facebookUserId
	 * @param facebookAccessToken
	 * @param facebookEmailAddress
	 * @return
	 */
	public FacebookLinkResponse facebookLinkNewUser(String facebookUserId, String facebookAccessToken,
		String facebookEmailAddress) {

		Session fbSession = Session.getActiveSession();
		if (fbSession == null || fbSession.isClosed()) {
			throw new RuntimeException("We must be logged into facebook inorder to call facebookLinkNewUser");
		}

		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();
		query.add(new BasicNameValuePair("provider", "Facebook"));
		query.add(new BasicNameValuePair("userId", facebookUserId));
		query.add(new BasicNameValuePair("accessToken", facebookAccessToken));
		query.add(new BasicNameValuePair("email", facebookEmailAddress));

		return doE3Request("api/auth/linkNewAccount", query, new FacebookLinkResponseHandler(mContext),
			F_SECURE_REQUEST);
	}

	/**
	 * Link an existing expedia user with a facebook account
	 *
	 * @param facebookUserId
	 * @param facebookAccessToken
	 * @param facebookEmailAddress
	 * @param expediaPassword
	 * @return
	 */
	public FacebookLinkResponse facebookLinkExistingUser(String facebookUserId, String facebookAccessToken,
		String facebookEmailAddress, String expediaPassword) {
		Session fbSession = Session.getActiveSession();
		if (fbSession == null || fbSession.isClosed()) {
			throw new RuntimeException("We must be logged into facebook inorder to call facebookLinkNewUser");
		}

		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();
		query.add(new BasicNameValuePair("provider", "Facebook"));
		query.add(new BasicNameValuePair("userId", facebookUserId));
		query.add(new BasicNameValuePair("accessToken", facebookAccessToken));
		query.add(new BasicNameValuePair("email", facebookEmailAddress));
		query.add(new BasicNameValuePair("password", expediaPassword));

		return doE3Request("api/auth/linkExistingAccount", query, new FacebookLinkResponseHandler(mContext),
			F_SECURE_REQUEST);
	}

	//////////////////////////////////////////////////////////////////////////
	// Push Notifications

	public PushNotificationRegistrationResponse registerForPushNotifications(
		String serverUrl, ResponseHandler<PushNotificationRegistrationResponse> responseHandler,
		JSONObject payload, String regId) {

		// Create the request
		Request.Builder post = new Request.Builder().url(serverUrl);
		String data = payload.toString();
		Request.Body body = Request.Body.create(MediaType.parse("application/json"), data);

		// Adding the body sets the Content-type header for us
		post.post(body);

		if (PushNotificationUtils.REGISTRATION_URL_PRODUCTION.equals(serverUrl)) {
			post.addHeader("MobiataPushName", "ExpediaBookings");
		}
		else {
			post.addHeader("MobiataPushName", "ExpediaBookingsAlpha");
		}

		if (!ExpediaBookingApp.IS_VSC && (AndroidUtils.isRelease(mContext)
			|| !SettingUtils
			.get(mContext, mContext.getString(R.string.preference_disable_push_registration), false))) {

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
	// API Console: http://reviews-web-eweprod-a-lb-109857973.us-east-1.elb.amazonaws.com/static/index.html

	private static final String REVIEWS_BASE_URL = "http://reviews-web-eweprod-a-lb-109857973.us-east-1.elb.amazonaws.com/reviews/v1/";

	public ReviewsResponse reviews(Property property, ReviewSort sort, int pageNumber) {
		return reviews(property, sort, pageNumber, REVIEWS_PER_PAGE);
	}

	public ReviewsResponse reviews(Property property, ReviewSort sort, int pageNumber, int numReviewsPerPage) {
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();

		params.add(new BasicNameValuePair("_type", "json"));
		params.add(new BasicNameValuePair("sortBy", sort.getSortByApiParam()));
		params.add(new BasicNameValuePair("start", Integer.toString(pageNumber * numReviewsPerPage)));
		params.add(new BasicNameValuePair("items", Integer.toString(numReviewsPerPage)));
		if (ExpediaBookingApp.IS_TRAVELOCITY) {
			params.add(new BasicNameValuePair("origin", "TRAVELOCITY"));
		}
		return doReviewsRequest(getReviewsUrl(property), params, new ReviewsResponseHandler());
	}

	private static String getReviewsUrl(Property property) {
		String locale = PointOfSale.getPointOfSale().getLocaleIdentifier();

		String url = REVIEWS_BASE_URL;
		url += "retrieve/getReviewsForHotelId/";
		url += property.getPropertyId();
		url += "/";
		url += locale;

		return url;
	}

	//////////////////////////////////////////////////////////////////////////
	// Request code

	private <T extends Response> T doFlightsRequest(String targetUrl, List<BasicNameValuePair> params,
		ResponseHandler<T> responseHandler, int flags) {
		return doE3Request(targetUrl, params, responseHandler, flags | F_FLIGHTS);
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
			serverUrl = getE3EndpointUrl(flags) + targetUrl;
		}

		// Create the request
		Request.Builder base;
		if ((flags & F_GET) != 0) {
			base = createHttpGet(serverUrl, params);
		}
		else {
			base = createHttpPost(serverUrl, params);
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

		return doRequest(get, responseHandler, 0);
	}

	private <T extends Response> T doRequest(Request.Builder request, ResponseHandler<T> responseHandler, int flags) {
		final String userAgent = getUserAgentString(mContext);

		mClient = sCachedClient;
		request.setUserAgent(userAgent);
		request.addHeader("Accept-Encoding", "gzip");

		final boolean ignoreCookies = (flags & F_IGNORE_COOKIES) != 0;
		if (ignoreCookies) {
			// We don't want cookies so we cannot use the cached client
			mClient = makeOkHttpClient(mContext);
			mClient.setCookieHandler(sBlackHoleCookieManager);
		}

		final boolean cookiesAreLoggedIn = User.isLoggedIn(mContext);

		// Make the request
		long start = System.currentTimeMillis();
		mCancellingDownload = false;
		com.squareup.okhttp.Response response = null;
		try {
			mRequest = request.build();
			response = mClient.execute(mRequest);
			Response processedResponse = responseHandler.handleResponse(response);
			if (!ignoreCookies && !mCancellingDownload) {
				if (cookiesAreLoggedIn != User.isLoggedIn(mContext)) {
					//The login state has changed, so we should redo the network request with the appropriate new cookies
					//this prevents us from overwritting our cookies with bunk loggedin/loggedout cookie states.
					Log.d(
						"Login state has changed since the request began - we are going to resend the request using appropriate cookies. The request began in the logged "
							+ (cookiesAreLoggedIn ? "IN" : "OUT") + " state.");
					return doRequest(request, responseHandler, flags);
				}

				// FIXME : cookieStore.save(mContext, COOKIES_FILE);
			}
			return (T) processedResponse;
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

	//////////////////////////////////////////////////////////////////////////
	// Endpoints

	public enum EndPoint {
		PRODUCTION,
		DEV,
		INTEGRATION,
		STABLE,
		PROXY,
		MOCK_SERVER,
		PUBLIC_INTEGRATION,
		TRUNK,
		TRUNK_STUBBED,
		CUSTOM_SERVER,
	}

	private static Map<EndPoint, String> sServerUrls = new HashMap<EndPoint, String>();

	public static void initEndPoints(Context context, String assetPath) {
		try {
			InputStream is = context.getAssets().open(assetPath);
			JSONObject data = new JSONObject(IoUtils.convertStreamToString(is));

			sServerUrls.put(EndPoint.PRODUCTION, data.optString("production").replace('@', 's'));
			sServerUrls.put(EndPoint.DEV, data.optString("development").replace('@', 's'));
			sServerUrls.put(EndPoint.INTEGRATION, data.optString("integration").replace('@', 's'));
			sServerUrls.put(EndPoint.STABLE, data.optString("stable").replace('@', 's'));
			sServerUrls.put(EndPoint.PUBLIC_INTEGRATION, data.optString("publicIntegration").replace('@', 's'));
			sServerUrls.put(EndPoint.TRUNK, data.optString("trunk").replace('@', 's'));
			sServerUrls.put(EndPoint.TRUNK_STUBBED, data.optString("stubbed").replace('@', 's'));
		}
		catch (Exception e) {
			// If the endpoints fail to load, then we should fail horribly
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns the base E3 server url, based on dev settings
	 */
	public String getE3EndpointUrl(int flags) {
		EndPoint endPoint = getEndPoint(mContext);
		String domain = PointOfSale.getPointOfSale().getUrl();

		String urlTemplate = sServerUrls.get(endPoint);
		if (!TextUtils.isEmpty(urlTemplate)) {
			String protocol = (flags & F_SECURE_REQUEST) != 0 ? "https" : "http";

			// Use dot-less domain names for everything besides production
			if (endPoint != EndPoint.PRODUCTION) {
				domain = TextUtils.join("", domain.split("\\."));
			}

			return String.format(urlTemplate, protocol, domain);
		}
		else if (endPoint == EndPoint.PROXY || endPoint == EndPoint.MOCK_SERVER) {
			return "http://" + SettingUtils.get(mContext, mContext.getString(R.string.preference_proxy_server_address),
				"localhost:3000") + "/" + domain + "/";
		}
		else if (endPoint == EndPoint.CUSTOM_SERVER) {
			String protocol = (flags & F_SECURE_REQUEST) != 0 ? "https" : "http";
			String server = SettingUtils
				.get(mContext, mContext.getString(R.string.preference_proxy_server_address), "localhost:3000");
			return protocol + "://" + server + "/";
		}
		else {
			throw new RuntimeException("Didn't know how to handle EndPoint: " + endPoint);
		}
	}

	// TODO move to ESD, make more like E3 URL construction
	private static Map<String, String> sGdePosUrlMap = new HashMap<String, String>() {
		{
			put("AU", "http://deals.expedia.com/beta/deals/flights.json");
			put("CA", "http://deals.expedia.com/beta/stats/flights.json");
			put("NZ", "http://deals.expedia.com/beta/stats/flights.json");
			put("US", "http://deals.expedia.com/beta/stats/flights.json");
			put("AT", "http://deals.expedia.at/beta/stats/flights.json");
			put("BE", "http://deals.expedia.be/beta/stats/flights.json");
			put("DE", "http://deals.expedia.de/beta/stats/flights.json");
			put("DK", "http://deals.expedia.dk/beta/stats/flights.json");
			put("ES", "http://deals.expedia.es/beta/stats/flights.json");
			put("FR", "http://deals.expedia.fr/beta/stats/flights.json");
			put("IE", "http://deals.expedia.ie/beta/stats/flights.json");
			put("IT", "http://deals.expedia.it/beta/stats/flights.json");
			put("NL", "http://deals.expedia.nl/beta/stats/flights.json");
			put("NO", "http://deals.expedia.no/beta/stats/flights.json");
			put("SE", "http://deals.expedia.se/beta/stats/flights.json");
			put("UK", "http://deals.expedia.co.uk/beta/stats/flights.json");
			put("IN", "http://deals.expedia.co.in/beta/stats/flights.json");
			put("JP", "http://deals.expedia.co.jp/beta/stats/flights.json");
			put("MY", "http://deals.expedia.com.my/beta/stats/flights.json");
			put("SG", "http://deals.expedia.com.sg/beta/stats/flights.json");
			put("TH", "http://deals.expedia.co.th/beta/stats/flights.json");
		}
	};

	public String getGdeEndpointUrl() {
		String key = PointOfSale.getPointOfSale().getTwoLetterCountryCode().toUpperCase();
		return sGdePosUrlMap.get(key);
	}

	public static EndPoint getEndPoint(Context context) {
		boolean isRelease = AndroidUtils.isRelease(context);
		if (isRelease) {
			// Fastpath
			return EndPoint.PRODUCTION;
		}

		String which = SettingUtils.get(context, context.getString(R.string.preference_which_api_to_use_key), "");

		if (which.equals("Dev")) {
			return EndPoint.DEV;
		}
		else if (which.equals("Proxy")) {
			return EndPoint.PROXY;
		}
		else if (which.equals("Mock Server")) {
			return EndPoint.MOCK_SERVER;
		}
		else if (which.equals("Public Integration")) {
			return EndPoint.PUBLIC_INTEGRATION;
		}
		else if (which.equals("Integration")) {
			return EndPoint.INTEGRATION;
		}
		else if (which.equals("Stable")) {
			return EndPoint.STABLE;
		}
		else if (which.equals("Trunk")) {
			return EndPoint.TRUNK;
		}
		else if (which.equals("Trunk (Stubbed)")) {
			return EndPoint.TRUNK_STUBBED;
		}
		else if (which.equals("Custom Server")) {
			return EndPoint.CUSTOM_SERVER;
		}
		else {
			return EndPoint.PRODUCTION;
		}
	}

	public static String getStubConfigUrl(Context context) {
		ExpediaServices services = new ExpediaServices(context);
		return services.getE3EndpointUrl(0) + "stubConfiguration/list";
	}

	//////////////////////////////////////////////////////////////////////////
	// Download listener stuff

	@Override
	public void onCancel() {
		if (mRequest != null) {
			Log.i("Cancelling download!");
			mCancellingDownload = true;

			// If we're on the main thread, then run the abort
			// in its own thread (to avoid network calls in
			// main thread).  If we're not, feel free to just
			// abort.
			if ("main".equals(Thread.currentThread().getName())) {
				(new Thread(new Runnable() {
					@Override
					public void run() {
						// Due to timing issues, we could end up such that the
						// request has finished by the time we get here.  In
						// that case, let's not NPE.
						if (mRequest != null) {
							mClient.cancel(mRequest);
							mRequest = null;
						}
					}
				})).start();
			}
			else {
				mClient.cancel(mRequest);
				mRequest = null;
			}
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Debug/utility (not for release)

	public ScenarioResponse getScenarios() {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();
		query.add(new BasicNameValuePair("json", "true"));
		return doE3Request("stubConfiguration/list", query, new ScenarioResponseHandler(), 0);
	}

	public ScenarioSetResponse setScenario(Scenario config) {
		String serverUrl = getE3EndpointUrl(0) + config.getUrl();
		Log.d(TAG_REQUEST, "Hitting scenario: " + serverUrl);
		Request.Builder get = new Request.Builder().url(serverUrl);
		return doRequest(get, new ScenarioSetResponseHandler(), F_ALLOW_REDIRECT);
	}

	// Automatically trusts all SSL certificates.  ONLY USE IN TESTING!
	private static final TrustManager[] sEasyTrustManager = new TrustManager[] {
		new X509TrustManager() {
			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				// So easy
			}

			public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				// So easy
			}

			public X509Certificate[] getAcceptedIssuers() {
				// So easy
				return null;
			}
		}
	};

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
		if (params != null) {
			String outUrl = url;
			if (!outUrl.endsWith("?")) {
				outUrl += "?";
			}

			String encodedParams = URLEncodedUtils.format(params, "UTF-8");
			if (encodedParams != null) {
				outUrl += encodedParams;
			}

			Request.Builder req = new Request.Builder().url(outUrl);
			return req;
		}
		return new Request.Builder().url(url);

	}


	private Request.Builder createHttpPost(String url, List<BasicNameValuePair> params) {
		Request.Builder req = new Request.Builder().url(url);
		String data = NetUtils.getParamsForLogging(params);
		Request.Body body = Request.Body.create(MediaType.parse("application/x-www-form-urlencoded"), data);
		req.post(body);
		return req;
	}
}
