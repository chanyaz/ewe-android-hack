package com.expedia.bookings.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.CookieSpecRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.expedia.bookings.R;
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
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.FlightSearchResponse;
import com.expedia.bookings.data.FlightStatsFlightResponse;
import com.expedia.bookings.data.FlightTrip;
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
import com.expedia.bookings.data.ReviewsResponse;
import com.expedia.bookings.data.ReviewsStatisticsResponse;
import com.expedia.bookings.data.SamsungWalletResponse;
import com.expedia.bookings.data.Scenario;
import com.expedia.bookings.data.ScenarioResponse;
import com.expedia.bookings.data.ScenarioSetResponse;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.ServerError.ErrorCode;
import com.expedia.bookings.data.SignInResponse;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.data.SuggestResponse;
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
import com.expedia.bookings.notification.PushNotificationUtils;
import com.facebook.Session;
import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;
import com.mobiata.android.BackgroundDownloader.DownloadListener;
import com.mobiata.android.Log;
import com.mobiata.android.net.AndroidHttpClient;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.IoUtils;
import com.mobiata.android.util.NetUtils;
import com.mobiata.android.util.SettingUtils;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.data.FlightCode;

@SuppressLint("SimpleDateFormat")
public class ExpediaServices implements DownloadListener {

	private static final String ISO_FORMAT = "yyyy-MM-dd";

	// please note that these keys are specific to EB (for tracking purposes)
	// if you need FLEX API keys for another app, please obtain your own
	private static final String FS_FLEX_APP_ID = "db824f8c";
	private static final String FS_FLEX_APP_KEY = "6cf6ac9c083a45e93c6a290bf0cd442e";
	private static final String FS_FLEX_BASE_URI = "https://api.flightstats.com/flex";

	private static final String BAZAAR_VOICE_BASE_URL = "http://reviews.expedia.com/data/reviews.json";
	private static final String BAZAAR_VOICE_API_TOKEN = "tq2es494c5r0o2443tc4byu2q";
	private static final String BAZAAR_VOICE_API_VERSION = "5.1";

	private static final String EXPEDIA_SUGGEST_BASE_URL = "http://suggest.expedia.com/hint/es/v2/ac/";

	public static final int REVIEWS_PER_PAGE = 25;

	public static final int HOTEL_MAX_RESULTS = 200;

	public static final int FLIGHT_MAX_TRIPS = 1600;

	// It is appalling how often the HotelOffers call fails; these
	// variables below control how many times we retry on failure
	private static final int MAX_ATLANTIS_ERROR_RETRIES = 10;
	private static final int MAX_AVAILABILITY_ERROR_RETRIES = 3;

	private static final String COOKIES_FILE = "cookies.dat";

	public enum ReviewSort {
		NEWEST_REVIEW_FIRST("NewestReviewFirst"),
		HIGHEST_RATING_FIRST("HighestRatingFirst"),
		LOWEST_RATING_FIRST("LowestRatingFirst");

		private String mKey;

		private ReviewSort(String key) {
			mKey = key;
		}

		public String getKey() {
			return mKey;
		}
	}

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

	private Context mContext;

	// For cancelling requests
	private HttpRequestBase mRequest;

	// This is just so that the error messages aren't treated severely when a download is canceled - naturally,
	// things kind of crash and burn when you kill the connection midway through.
	private boolean mCancellingDownload;

	public ExpediaServices(Context context) {
		mContext = context;
	}

	//////////////////////////////////////////////////////////////////////////
	// Cookies

	// Allows one to get the cookie store out of services, in case we need to
	// inject the cookies elsewhere (e.g., a WebView)
	public static PersistantCookieStore getCookieStore(Context context) {
		PersistantCookieStore cookieStore = new PersistantCookieStore();
		cookieStore.load(context, COOKIES_FILE);
		return cookieStore;
	}

	public static void removeUserLoginCookies(Context context) {
		Log.d("Cookies: Removing user login cookies");
		PersistantCookieStore cookieStore = new PersistantCookieStore();
		cookieStore.load(context, COOKIES_FILE);
		String[] userCookieNames = { "user", "minfo", "accttype" };
		cookieStore.removeAllCookiesByName(userCookieNames);
		if (cookieStore.isDirty()) {
			cookieStore.save(context, COOKIES_FILE);
		}
	}

	public void clearCookies() {
		Log.d("Cookies: Clearing!");

		PersistantCookieStore cookieStore = new PersistantCookieStore();
		cookieStore.clear();
		cookieStore.save(mContext, COOKIES_FILE);
	}

	//////////////////////////////////////////////////////////////////////////
	// User-Agent

	/**
	 * Constructs a user agent string to be used against Expedia requests. It is important to exclude the word "Android"
	 * otherwise mobile redirects occur when we don't want them. This is useful for all API requests contained here
	 * in ExpediaServices as well as certain requests through WebViewActivity in order to prevent the redirects.
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
	// http://confluence/display/POS/Expedia+Suggest+%28Type+Ahead%29+API+Family
	//
	// Examples (hotels):
	// http://suggest.expedia.com/hint/es/v1/ac/en_US/bellagio?type=30
	// http://suggest.expedia.com/hint/es/v1/ac/es_MX/seattle?type=30
	//
	// Examples (flights):
	// http://suggest.expedia.com/hint/es/v1/ac/en_US/new%20york?type=95&lob=Flights

	public SuggestResponse suggest(String query, int flags) {
		if (query == null || query.length() < getMinSuggestQueryLength()) {
			return null;
		}

		String localeString = PointOfSale.getSuggestLocaleIdentifier();

		String url = NetUtils.formatUrl(EXPEDIA_SUGGEST_BASE_URL + localeString + "/" + query);

		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();

		SuggestResponseHandler responseHandler = new SuggestResponseHandler();

		if ((flags & F_FLIGHTS) != 0) {
			// 95 is all regions (AIRPORT, CITY, MULTICITY, NEIGHBORHOOD, POI, METROCODE)
			params.add(new BasicNameValuePair("type", "95"));
			params.add(new BasicNameValuePair("lob", "Flights"));

			responseHandler.setType(SuggestResponseHandler.Type.FLIGHTS);
		}
		else {
			// 223 is regions(95 Default) + hotels(128)
			params.add(new BasicNameValuePair("type", "223"));

			responseHandler.setType(SuggestResponseHandler.Type.HOTELS);
		}

		HttpGet get = NetUtils.createHttpGet(url, params);
		get.addHeader("Accept", "application/json");

		// Some logging before passing the request along^M
		Log.d("Autosuggest request: " + url + "?" + NetUtils.getParamsForLogging(params));

		return doRequest(get, responseHandler, 0);
	}

	/**
	 * Get the minimum number of characters required to provide drop down auto fill results.
	 * This is useful for languages like Japanese where Tokyo is spelt with 2 characters.
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

	//////////////////////////////////////////////////////////////////////////
	// Expedia Flights API
	//
	// Documentation: http://www.expedia.com/static/mobile/APIConsole/flight.html

	public FlightSearchResponse flightSearch(FlightSearchParams params, int flags) {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();

		// This code currently assumes that you are either making a one-way or round trip flight,
		// even though FlightSearchParams can be configured to handle multi-leg flights.
		//
		// Once e3 can handle these as well, we will want to update this code.
		query.add(new BasicNameValuePair("departureAirport", params.getDepartureLocation().getDestinationId()));
		query.add(new BasicNameValuePair("arrivalAirport", params.getArrivalLocation().getDestinationId()));

		DateFormat df = new SimpleDateFormat(ISO_FORMAT);

		com.expedia.bookings.data.Date depDate = params.getDepartureDate();
		query.add(new BasicNameValuePair("departureDate", df.format(depDate.getCalendar().getTime())));

		if (params.isRoundTrip()) {
			Date retDate = params.getReturnDate().getCalendar().getTime();
			query.add(new BasicNameValuePair("returnDate", df.format(retDate)));
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

		return doFlightsRequest("api/flight/search", query, new StreamingFlightSearchResponseHandler(), flags);
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
		if (suppressFinalBooking(mContext)) {
			query.add(new BasicNameValuePair("suppressFinalBooking", "true"));
		}

		if (User.isLoggedIn(mContext)) {
			query.add(new BasicNameValuePair("doIThinkImSignedIn", "true"));
			query.add(new BasicNameValuePair("storeCreditCardInUserProfile",
					billingInfo.getSaveCardToExpediaAccount() ? "true" : "false"));
		}

		addCommonParams(query);

		return doFlightsRequest("api/flight/checkout", query, new FlightCheckoutResponseHandler(mContext), flags
				+ F_SECURE_REQUEST);
	}

	// Suppress final bookings if we're not in release mode and the preference is set to suppress
	public static boolean suppressFinalBooking(Context context) {
		return !AndroidUtils.isRelease(context)
				&& SettingUtils.get(context, context.getString(R.string.preference_suppress_bookings), true);
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
		parameters.add(new BasicNameValuePair("appId", FS_FLEX_APP_ID));
		parameters.add(new BasicNameValuePair("appKey", FS_FLEX_APP_KEY));

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

	//////////////////////////////////////////////////////////////////////////
	// Expedia hotel API
	//
	// Documentation: http://www.expedia.com/static/mobile/APIConsole/

	public HotelSearchResponse search(HotelSearchParams params, int sortType) {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();

		if (getEndPoint(mContext) == EndPoint.MOCK_SERVER) {
			query.add(new BasicNameValuePair("city", "saved_product"));
			HotelSearchResponseHandler rh = new HotelSearchResponseHandler(mContext);
			return doE3Request("MobileHotel/Webapp/SearchResults", query, rh, 0);
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

		// These values are always the same (for now)
		query.add(new BasicNameValuePair("resultsPerPage", HOTEL_MAX_RESULTS + ""));
		query.add(new BasicNameValuePair("pageIndex", "0"));
		query.add(new BasicNameValuePair("filterUnavailable", "true"));

		HotelSearchResponseHandler rh = new HotelSearchResponseHandler(mContext);
		if (params.hasSearchLatLon()) {
			rh.setLatLng(params.getSearchLatitude(), params.getSearchLongitude());
		}
		rh.setNumNights(params.getStayDuration());
		return doE3Request("MobileHotel/Webapp/SearchResults", query, rh, 0);
	}

	public HotelOffersResponse availability(HotelSearchParams params, Property property) {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();

		addCommonParams(query);

		query.add(new BasicNameValuePair("hotelId", property.getPropertyId()));

		if (params != null) {
			addHotelSearchParams(query, params);
		}

		HotelOffersResponseHandler responseHandler = new HotelOffersResponseHandler(mContext, params, property);
		HotelOffersResponse response = doE3Request("MobileHotel/Webapp/HotelOffers", query, responseHandler, 0);

		int numTries = 1;
		while (response != null && response.hasErrors()) {
			numTries++;

			// #12701: Often times, Atlantis cache screws up and returns the error "Hotel product's PIID that is
			// provided by Atlantis has expired."  This error can happen multiple times. As a result, the workaround
			// here is to immediately make multiple identical request if the previous ones fail.
			//
			// We also retry for any other generic error (just in case).  This call kind of has reliability issues.
			ServerError error = response.getErrors().get(0);
			if (error.getErrorCode() == ErrorCode.HOTEL_ROOM_UNAVAILABLE
					&& "Hotel product\u0027s PIID that is provided by Atlantis has expired".equals(error
							.getMessage())) {
				if (numTries > MAX_ATLANTIS_ERROR_RETRIES) {
					return response;
				}

				Log.w("Atlantis PIID expired, automatically retrying HotelOffers request.  Attempt #" + numTries);
				response = doE3Request("MobileHotel/Webapp/HotelOffers", query, responseHandler, 0);
			}
			else {
				if (numTries > MAX_AVAILABILITY_ERROR_RETRIES) {
					return response;
				}

				Log.w("Random availability error, automatically retrying HotelOffers request.  Attempt #" + numTries);
				response = doE3Request("MobileHotel/Webapp/HotelOffers", query, responseHandler, 0);
			}
		}

		return response;
	}

	public HotelProductResponse hotelProduct(HotelSearchParams params, Property property, Rate rate) {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();

		// Adds sourceType
		addCommonParams(query);

		query.add(new BasicNameValuePair("productKey", rate.getRateKey()));

		// For room1 parameter
		addHotelGuestParamater(query, params);

		HotelProductResponseHandler responseHandler = new HotelProductResponseHandler(mContext, params, property, rate);
		return doE3Request("MobileHotel/Webapp/HotelProduct", query, responseHandler, 0);
	}

	public CreateTripResponse createTripWithCoupon(String couponCode, HotelSearchParams params, Property property,
			Rate rate) {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();

		addCommonParams(query);
		addHotelSearchParams(query, params);

		query.add(new BasicNameValuePair("productKey", rate.getRateKey()));
		query.add(new BasicNameValuePair("couponCode", couponCode));

		CreateTripResponseHandler responseHandler = new CreateTripResponseHandler(mContext, params, property);
		return doE3Request("MobileHotel/Webapp/CreateTrip", query, responseHandler, F_SECURE_REQUEST);
	}

	public BookingResponse reservation(HotelSearchParams params, Property property, Rate rate, BillingInfo billingInfo,
			String tripId, String userId, Long tuid) {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();

		addCommonParams(query);

		query.add(new BasicNameValuePair("hotelId", property.getPropertyId()));
		query.add(new BasicNameValuePair("productKey", rate.getRateKey()));

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

		// Simulate a valid checkout, to bypass the actual checkout process
		if (!AndroidUtils.isRelease(mContext)) {
			boolean spoofBookings = SettingUtils.get(mContext, mContext.getString(R.string.preference_spoof_bookings),
					false);

			if (spoofBookings) {
				// Show a log of what URL would have been called had this not been spoofed
				String serverUrl = getE3EndpointUrl(F_SECURE_REQUEST) + "Checkout";
				Log.d("Request (spoofed): " + serverUrl + "?" + NetUtils.getParamsForLogging(query));

				String simulatedResponse = "{\"warnings\":[],\"cancellationPolicy\":\" \",\"nonLocalizedhotelName\":\"Hotel Deadbeef\",\"hotelName\":\"Hotel Deadbeef\",\"localizedHotelName\":\"Hotel Deadbeef\",\"hotelAddress\":\"250 W 43rd St\",\"hotelPostalCode\":\"10036\",\"hotelStateProvinceCode\":\"NY\",\"hotelCountryCode\":\"USA\",\"hotelCity\":\"New York\",\"hotelPhone\":\"1-212-944-6000\",\"hotelLongitude\":\"-73.98791\",\"hotelLatitude\":\"40.75731\",\"nightCount\":\"1\",\"maxGuestCount\":\"2\",\"checkInInstructions\":\"\",\"roomDescription\":\" Single/double\",\"checkInDate\":\"2013-06-05\",\"checkInDateForTracking\":\"6/5/2013\",\"checkOutDate\":\"2013-06-06\",\"pricePerDayBreakdown\":\"true\",\"averageDailyHotelPrice\":\"132.93\",\"taxes\":\"20.14\",\"fees\":\"13.85\",\"averageBaseRate\":\"98.94\",\"totalPrice\":\"132.93\",\"currencyCode\":\"USD\",\"nightlyRates\":[{\"promo\":\"false\",\"baseRate\":\"98.94\",\"rate\":\"98.94\"}],\"supplierType\":\"MERCHANT\",\"confirmationPending\":\"false\",\"itineraryNumber\":\"12345678901\",\"travelRecordLocator\":\"11890585\",\"numberOfRoomsBooked\":\"1\",\"nonRefundable\":\"false\",\"email\":\"qa-ehcc@mobiata.com\",\"guestFullName\":\"JexperCC MobiataTestaverde\",\"guestPhone\":{\"number\":\"9992222\",\"areaCode\":\"919\",\"category\":\"PRIMARY\",\"countryCode\":\"1\"},\"tripId\":\"deadbeef-feed-cede-bead-f00f00f00f00\",\"isMerchant\":true,\"isGDS\":false,\"isOpaque\":false,\"hotelInventoryTypeName\":\"MERCHANT\"}";
				JSONObject json = null;
				try {
					json = new JSONObject(simulatedResponse);
					Thread.sleep(20000);
				}
				catch (JSONException e) {
					e.printStackTrace();
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
				return new BookingResponseHandler(mContext).handleJson(json);
			}
		}

		if (User.isLoggedIn(mContext)) {
			query.add(new BasicNameValuePair("doIThinkImSignedIn", "true"));
			query.add(new BasicNameValuePair("storeCreditCardInUserProfile",
					billingInfo.getSaveCardToExpediaAccount() ? "true" : "false"));
		}

		return doE3Request("MobileHotel/Webapp/Checkout", query, new BookingResponseHandler(mContext), F_SECURE_REQUEST);
	}

	private void addHotelSearchParams(List<BasicNameValuePair> query, HotelSearchParams params) {
		DateFormat df = new SimpleDateFormat(ISO_FORMAT);

		// #13586: We need a second SimpleDateFormat because on 2.2 and below.  See
		// ticket for more info (bug is complex).
		//
		// Update: having removed the timezone portion, no longer sure if this is necessary;
		// but I sure don't want to break anything.
		DateFormat df2 = new SimpleDateFormat(ISO_FORMAT);

		query.add(new BasicNameValuePair("checkInDate", df.format(params.getCheckInDate().getTime())));
		query.add(new BasicNameValuePair("checkOutDate", df2.format(params.getCheckOutDate().getTime())));

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
	 * @param traveler
	 * @return
	 */
	public TravelerCommitResponse commitTraveler(Traveler traveler) {
		if (User.isLoggedIn(mContext)) {
			List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();
			addFlightTraveler(query, traveler, "");
			addCommonParams(query);
			Log.i("update-travler body:" + NetUtils.getParamsForLogging(query));
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

	private void addBillingInfo(List<BasicNameValuePair> query, BillingInfo billingInfo, int flags) {
		if ((flags & F_HAS_TRAVELER) == 0) {
			// Don't add firstname/lastname if we're adding it through the traveler interface later
			query.add(new BasicNameValuePair("firstName", billingInfo.getFirstName()));
			query.add(new BasicNameValuePair("lastName", billingInfo.getLastName()));
			query.add(new BasicNameValuePair("phoneCountryCode", billingInfo.getTelephoneCountryCode()));
			query.add(new BasicNameValuePair("phone", billingInfo.getTelephone()));
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

			Date expDate = billingInfo.getExpirationDate().getTime();

			DateFormat expFormatter = new SimpleDateFormat("MMyy");
			query.add(new BasicNameValuePair("expirationDate", expFormatter.format(expDate)));

			// This is an alternative way of representing expiration date, used for Flights.
			// Doesn't hurt to include both methods
			query.add(new BasicNameValuePair("expirationDateYear", android.text.format.DateFormat.format("yyyy",
					expDate).toString()));
			query.add(new BasicNameValuePair("expirationDateMonth", android.text.format.DateFormat
					.format("MM", expDate).toString()));
		}
		else {
			query.add(new BasicNameValuePair("storedCreditCardId", billingInfo.getStoredCard().getId()));
		}
		query.add(new BasicNameValuePair("cvv", billingInfo.getSecurityCode()));
	}

	private void addFlightTraveler(List<BasicNameValuePair> query, Traveler traveler, String prefix) {
		SimpleDateFormat isoDateFormatter = new SimpleDateFormat(ISO_FORMAT);
		query.add(new BasicNameValuePair(prefix + "firstName", traveler.getFirstName()));
		if (!TextUtils.isEmpty(traveler.getMiddleName())) {
			query.add(new BasicNameValuePair(prefix + "middleName", traveler.getMiddleName()));
		}
		query.add(new BasicNameValuePair(prefix + "lastName", traveler.getLastName()));
		query.add(new BasicNameValuePair(prefix + "birthDate", isoDateFormatter.format(traveler.getBirthDateInMillis())));
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
			ResponseHandler<PushNotificationRegistrationResponse> responseHandler, JSONObject payload, String regId) {
		String serverUrl = PushNotificationUtils.REGISTRATION_URL;

		// Create the request
		HttpPost post = NetUtils.createHttpPost(serverUrl, (List<BasicNameValuePair>) null);
		post.setHeader("Content-type", "application/json");
		post.addHeader("MobiataPushName", "ExpediaBookingsAlpha");

		try {
			StringEntity strEntity = new StringEntity(payload.toString());
			post.setEntity(strEntity);
		}
		catch (UnsupportedEncodingException e) {
			Log.e("Failure to create StringEntity", e);
		}

		if (AndroidUtils.isRelease(mContext)
				|| !SettingUtils
						.get(mContext, mContext.getString(R.string.preference_disable_push_registration), false)) {

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

	//////////////////////////////////////////////////////////////////////////
	// BazaarVoice (Reviews) API

	public ReviewsResponse reviews(Property property, ReviewSort sort, int pageNumber, List<String> languages) {
		return reviews(property, sort, pageNumber, languages, REVIEWS_PER_PAGE);
	}

	public ReviewsResponse reviews(Property property, ReviewSort sort, int pageNumber, List<String> languages,
			int number) {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();

		query.add(new BasicNameValuePair("apiversion", BAZAAR_VOICE_API_VERSION));
		query.add(new BasicNameValuePair("passkey", BAZAAR_VOICE_API_TOKEN));
		query.add(new BasicNameValuePair("limit", Integer.toString(number)));
		query.add(new BasicNameValuePair("offset", Integer.toString(pageNumber * number)));

		query.add(new BasicNameValuePair("Filter", "ProductId:" + property.getPropertyId()));

		String localesString = PointOfSale.getFormattedLanguageCodes(languages);

		query.add(new BasicNameValuePair("Filter", "ContentLocale:" + localesString));

		// emulate the expedia.com esktop website way of displaying reviews
		switch (sort) {
		case NEWEST_REVIEW_FIRST:
			query.add(new BasicNameValuePair("Sort", "SubmissionTime:desc"));
			break;
		case HIGHEST_RATING_FIRST:
			query.add(new BasicNameValuePair("Filter", "Rating:gte:3"));
			query.add(new BasicNameValuePair("Sort", "Rating:desc,SubmissionTime:desc"));
			break;
		case LOWEST_RATING_FIRST:
			query.add(new BasicNameValuePair("Filter", "Rating:lte:2"));
			query.add(new BasicNameValuePair("Sort", "Rating:asc,SubmissionTime:desc"));
			break;
		}

		query.add(new BasicNameValuePair("include", "products"));

		return doBazaarRequest(query, new ReviewsResponseHandler(mContext));
	}

	/*
	 * Method for retrieving reviews statistics. Historically, the number of reviews AND number of recommended
	 * reviews returned by Expedia on the property is outdated and malformed. Correct for this by grabbing these
	 * numbers from the BV aPI using the "Stats" param
	 */
	public ReviewsStatisticsResponse reviewsStatistics(Property property) {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();

		query.add(new BasicNameValuePair("apiversion", BAZAAR_VOICE_API_VERSION));
		query.add(new BasicNameValuePair("passkey", BAZAAR_VOICE_API_TOKEN));
		query.add(new BasicNameValuePair("limit", "1"));

		query.add(new BasicNameValuePair("Filter", "ProductId:" + property.getPropertyId()));

		List<String> languages = Arrays.asList(PointOfSale.getPointOfSale().getReviewLanguages());
		String localesString = PointOfSale.getFormattedLanguageCodes(languages);

		query.add(new BasicNameValuePair("Filter", "ContentLocale:" + localesString));

		query.add(new BasicNameValuePair("FilteredStats", "Reviews"));

		query.add(new BasicNameValuePair("Include", "Products"));

		return doBazaarRequest(query, new ReviewsStatisticsResponseHandler(mContext));
	}

	//////////////////////////////////////////////////////////////////////////
	// Request code

	private <T extends Response> T doFlightsRequest(String targetUrl, List<BasicNameValuePair> params,
			ResponseHandler<T> responseHandler, int flags) {
		return doE3Request(targetUrl, params, responseHandler, flags | F_FLIGHTS);
	}

	private <T extends Response> T doE3Request(String targetUrl, List<BasicNameValuePair> params,
			ResponseHandler<T> responseHandler, int flags) {
		String serverUrl = getE3EndpointUrl(flags) + targetUrl;

		// Create the request
		HttpRequestBase base;
		if ((flags & F_GET) != 0) {
			base = NetUtils.createHttpGet(serverUrl, params);
		}
		else {
			base = NetUtils.createHttpPost(serverUrl, params);
		}

		// Some logging before passing the request along
		Log.d("Request: " + serverUrl + "?" + NetUtils.getParamsForLogging(params));

		return doRequest(base, responseHandler, flags);
	}

	private <T extends Response> T doFlightStatsRequest(String baseUrl, List<BasicNameValuePair> params,
			ResponseHandler<T> responseHandler) {
		HttpRequestBase base = NetUtils.createHttpGet(baseUrl, params);

		return doRequest(base, responseHandler, F_IGNORE_COOKIES);
	}

	private <T extends Response> T doBazaarRequest(List<BasicNameValuePair> params, ResponseHandler<T> responseHandler) {
		HttpGet get = NetUtils.createHttpGet(BAZAAR_VOICE_BASE_URL, params);

		Log.d("Bazaar reviews request:  " + get.getURI().toString());

		return doRequest(get, responseHandler, 0);
	}

	private <T extends Response> T doRequest(HttpRequestBase request, ResponseHandler<T> responseHandler, int flags) {
		String userAgent = getUserAgentString(mContext);

		mRequest = request;
		AndroidHttpClient client = AndroidHttpClient.newInstance(userAgent, mContext);
		AndroidHttpClient.modifyRequestToAcceptGzipResponse(mRequest);
		HttpParams httpParameters = client.getParams();
		HttpConnectionParams.setSoTimeout(httpParameters, 100000);

		boolean ignoreCookies = (flags & F_IGNORE_COOKIES) != 0;
		boolean logCookies = !AndroidUtils.isRelease(mContext)
				&& SettingUtils.get(mContext, mContext.getString(R.string.preference_cookie_logging), false);

		HttpContext httpContext = new BasicHttpContext();
		PersistantCookieStore cookieStore = null;
		if (!ignoreCookies) {
			// TODO: Find some way to keep this easily in memory so we're not saving/loading after each request.
			cookieStore = getCookieStore(mContext);
			httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
			CookieSpecRegistry cookieSpecRegistry = new CookieSpecRegistry();
			cookieSpecRegistry.register("EXPEDIA", new ExpediaCookieSpecFactory(mContext));
			httpContext.setAttribute(ClientContext.COOKIESPEC_REGISTRY, cookieSpecRegistry);

			if (logCookies) {
				Log.v("Sending cookies:");
				cookieStore.log();
			}

			HttpClientParams.setCookiePolicy(httpParameters, "EXPEDIA");
		}

		// When not a release build, allow SSL from all connections
		if ((flags & F_SECURE_REQUEST) != 0 && !AndroidUtils.isRelease(mContext)) {
			try {
				KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
				trustStore.load(null, null);

				SSLSocketFactory sf = new TrustingSSLSocketFactory(trustStore);
				sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

				ClientConnectionManager ccm = client.getConnectionManager();
				SchemeRegistry registry = ccm.getSchemeRegistry();
				registry.unregister("https");
				registry.register(new Scheme("https", sf, 443));
			}
			catch (Exception e) {
				Log.w("Something sad happened during manipulation of SSL", e);
			}
		}

		// Make the request
		long start = System.currentTimeMillis();
		mCancellingDownload = false;
		try {
			T response = client.execute(mRequest, responseHandler, httpContext);
			if (!ignoreCookies && !mCancellingDownload) {
				if (logCookies) {
					Log.v("Received cookies: ");
					cookieStore.log();
				}

				cookieStore.save(mContext, COOKIES_FILE);
			}
			return response;
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
			client.close();
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
	 * @param context
	 * @return
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
			return "http://" + SettingUtils.get(mContext, mContext.getString(R.string.preference_proxy_server_address),
					"localhost:3000") + "/";
		}
		else {
			throw new RuntimeException("Didn't know how to handle EndPoint: " + endPoint);
		}
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
							mRequest.abort();
							mRequest = null;
						}
					}
				})).start();
			}
			else {
				mRequest.abort();
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
		Log.d("Hitting scenario: " + serverUrl);
		HttpGet get = new HttpGet(serverUrl);
		return doRequest(get, new ScenarioSetResponseHandler(), 0);
	}

	// Automatically trusts all SSL certificates.  ONLY USE IN TESTING!
	private class TrustingSSLSocketFactory extends SSLSocketFactory {
		SSLContext sslContext = SSLContext.getInstance("TLS");

		public TrustingSSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException,
				KeyStoreException, UnrecoverableKeyException {
			super(truststore);

			TrustManager tm = new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				}

				public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				}

				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};

			sslContext.init(null, new TrustManager[] { tm }, null);
		}

		@Override
		public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException,
				UnknownHostException {
			return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
		}

		@Override
		public Socket createSocket() throws IOException {
			return sslContext.getSocketFactory().createSocket();
		}
	}
}
