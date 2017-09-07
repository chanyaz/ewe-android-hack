package com.expedia.bookings.server;

import com.crashlytics.android.Crashlytics;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.zip.GZIPInputStream;

import javax.inject.Inject;

import org.apache.http.client.utils.URLEncodedUtils;
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
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.FlightSearchResponse;
import com.expedia.bookings.data.FlightStatsFlightResponse;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Itinerary;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.PushNotificationRegistrationResponse;
import com.expedia.bookings.data.Response;
import com.expedia.bookings.data.ReviewSort;
import com.expedia.bookings.data.ReviewsResponse;
import com.expedia.bookings.data.RoutesResponse;
import com.expedia.bookings.data.SignInResponse;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.data.SuggestResponse;
import com.expedia.bookings.data.SuggestionResultType;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.Traveler.AssistanceType;
import com.expedia.bookings.data.Traveler.Gender;
import com.expedia.bookings.data.TravelerCommitResponse;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.trips.Trip;
import com.expedia.bookings.data.trips.TripBucketItemFlight;
import com.expedia.bookings.data.trips.TripDetailsResponse;
import com.expedia.bookings.data.trips.TripResponse;
import com.expedia.bookings.data.trips.TripShareUrlShortenerResponse;
import com.expedia.bookings.data.user.UserStateManager;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.notification.PushNotificationUtils;
import com.expedia.bookings.services.PersistentCookiesCookieJar;
import com.expedia.bookings.utils.BookingSuppressionUtils;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.OKHttpClientFactory;
import com.expedia.bookings.utils.ServicesUtil;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.Ui;
import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;
import com.mobiata.android.BackgroundDownloader.DownloadListener;
import com.mobiata.android.Log;
import com.mobiata.android.util.AdvertisingIdUtils;
import com.mobiata.android.util.NetUtils;
import com.mobiata.android.util.SettingUtils;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.data.FlightCode;

import okhttp3.Call;
import okhttp3.JavaNetCookieJar;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

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

	@Inject
	public PersistentCookiesCookieJar mCookieManager;

	@Inject
	EndpointProvider mEndpointProvider;

	@Inject
	OkHttpClient mClient;

	@Inject
	OKHttpClientFactory mOkHttpClientFactory;

	@Inject
	UserStateManager userStateManager;

	private Call call;
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

	public static void removeUserLoginCookies(Context context) {
		Log.d("Cookies: Removing user login cookies");
		String[] userCookieNames = {
			"user",
			"minfo",
			"accttype",
		};
		removeUserLoginCookies(context, userCookieNames);
	}

	private static void removeUserLoginCookies(Context context, String[] userCookieNames) {
		ExpediaServices services = new ExpediaServices(context);
		String endpointUrl = Ui.getApplication(context).appComponent().endpointProvider().getE3EndpointUrl();
		services.mCookieManager.removeNamedCookies(endpointUrl, userCookieNames);
	}

	public static void removeUserCookieFromUserLoginCookies(Context context) {
		if (BuildConfig.DEBUG) {
			Log.d("Cookies: Removing user cookie from user login cookies");
			String[] cookieNames = {
				"user"
			};
			removeUserLoginCookies(context, cookieNames);
		}
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
				SuggestionResultType.NEIGHBORHOOD | SuggestionResultType.POINT_OF_INTEREST
				| SuggestionResultType.REGION;
			params.add(new BasicNameValuePair("regiontype", "" + regionType));
			params.add(new BasicNameValuePair("lob", "hotels"));
			responseHandler.setType(SuggestResponseHandler.Type.HOTELS);
			params.add(new BasicNameValuePair("features", "ta_hierarchy"));
		}

		params.add(new BasicNameValuePair("locale", PointOfSale.getSuggestLocaleIdentifier()));
		params.add(new BasicNameValuePair("client", ServicesUtil.generateClient(mContext)));

		Request.Builder get = createHttpGet(url, params);
		get.addHeader("Accept", "application/json");

		// Some logging before passing the request along^M
		Log.d(TAG_REQUEST, "Autosuggest request: " + url + "?" + NetUtils.getParamsForLogging(params));

		return doRequest(get, responseHandler, 0);
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
		}

		return sb.toString();
	}

	private String getGaiaNearbySuggestUrl() {
		StringBuilder sb = new StringBuilder();
		sb.append(mEndpointProvider.getGaiaEndpointUrl());
		sb.append("/v1/features/");
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

	public List<BasicNameValuePair> generateFlightCheckoutParams(TripBucketItemFlight flightItem,
		BillingInfo billingInfo, List<Traveler> travelers) {
		FlightTrip flightTrip = flightItem.getFlightTrip();
		Itinerary itinerary = flightItem.getItinerary();

		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();

		query.add(new BasicNameValuePair("tripId", itinerary.getTripId()));
		query.add(new BasicNameValuePair("expectedTotalFare", flightTrip.getTotalPrice().getAmount().toString() + ""));
		query.add(new BasicNameValuePair("expectedFareCurrencyCode", flightTrip.getTotalPrice().getCurrency()));

		Money cardFee = flightItem.getPaymentFee(billingInfo.getPaymentType(mContext));
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

		if (userStateManager.isUserAuthenticated()) {
			query.add(new BasicNameValuePair("doIThinkImSignedIn", "true"));
			query.add(new BasicNameValuePair("storeCreditCardInUserProfile",
				billingInfo.getSaveCardToExpediaAccount() ? "true" : "false"));
		}

		addCommonParams(query);

		return query;
	}

	// Suppress final bookings if we're not in release mode and the preference is set to suppress
	private static boolean suppressFinalFlightBooking(Context context) {
		return BookingSuppressionUtils
			.shouldSuppressFinalBooking(context, R.string.preference_suppress_flight_bookings);
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
		FlightStatsFlightResponse response;

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

		if (flight.getPrimaryFlightCode() != null) {
			response = doFlightStatsRequest(baseUrl, parameters,
				new FlightStatsFlightStatusResponseHandler(flight.getPrimaryFlightCode().mAirlineCode));
		}
		else {
			Crashlytics.logException(
				new Exception("See Flight segment details:" + flight));
			response = doFlightStatsRequest(baseUrl, parameters,
				new FlightStatsFlightStatusResponseHandler(null));
		}

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

		int flags = F_POST;

		// Always use tripNumber for guests, tripId for logged in
		String tripIdentifier;
		if (trip.isGuest()) {
			// You must always use trip number for guest itineraries
			tripIdentifier = trip.getTripNumber();

			query.add(new BasicNameValuePair("email", trip.getGuestEmailAddress()));
			query.add(new BasicNameValuePair("idtype", "itineraryNumber"));

			// When fetching trips for guest user as a signed in user, the response sends back cookies for both user profile. So let's ignore it.
			flags = flags | F_IGNORE_COOKIES;
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

		addSignInParams(query, flags);
		return doE3Request("api/user/sign-in", query, new SignInResponseHandler());
	}

	public SignInResponse signInWithEmailForAutomationTests(int flags, String email) {
		if (!ExpediaBookingApp.isAutomation()) {
			throw new RuntimeException("signInWithEmailForAutomationTests can be called only from automation builds");
		}
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();

		addSignInParams(query, flags);
		query.add(new BasicNameValuePair("email", email));

		return doE3Request("api/user/sign-in", query, new SignInResponseHandler());
	}

	private void addSignInParams(List<BasicNameValuePair> query, int flags) {
		addCommonParams(query);

		query.add(new BasicNameValuePair("profileOnly", "true"));
		query.add(new BasicNameValuePair("includeFullPaymentProfile", "true"));

		addProfileTypes(query, flags);
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
		if (userStateManager.isUserAuthenticated()) {
			List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();
			addFlightTraveler(query, traveler, "");
			addCommonParams(query);
			Log.i(TAG_REQUEST, "update-traveler body:" + NetUtils.getParamsForLogging(query));
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
		query.add(new BasicNameValuePair(prefix + "passengerCategory",
			traveler.getPassengerCategory().toString()));
		String assistanceOption;
		if (traveler.getAssistance() != null) {
			assistanceOption = traveler.getAssistance().name();
		}
		else {
			assistanceOption = AssistanceType.NONE.name();
		}
		query.add(new BasicNameValuePair(prefix + "specialAssistanceOption", assistanceOption));
		query.add(new BasicNameValuePair(prefix + "seatPreference", traveler.getSafeSeatPreference().name()));

		String travelerPhoneCountryCode = !TextUtils.isEmpty(traveler.getPhoneCountryCode()) ?
			traveler.getPhoneCountryCode() : Db.getUser().getPrimaryTraveler().getPhoneCountryCode();

		query.add(new BasicNameValuePair(prefix + "phoneCountryCode", travelerPhoneCountryCode));

		String travelerPhoneNumber = !TextUtils.isEmpty(traveler.getOrCreatePrimaryPhoneNumber().getNumber()) ?
			traveler.getPrimaryPhoneNumber().getNumber() : Db.getUser().getPrimaryTraveler().getPhoneNumber();

		query.add(new BasicNameValuePair(prefix + "phone", travelerPhoneNumber));

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
		if (!TextUtils.isEmpty(traveler.getKnownTravelerNumber())) {
			query.add(new BasicNameValuePair(prefix + "knownTravelerNumber", traveler.getKnownTravelerNumber()));
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Push Notifications

	public PushNotificationRegistrationResponse registerForPushNotifications(
		String serverUrl, ResponseHandler<PushNotificationRegistrationResponse> responseHandler,
		JSONObject payload, String regId) {

		// Create the request
		Request.Builder post = new Request.Builder().url(serverUrl);
		String data = payload.toString();
		RequestBody body = RequestBody.create(MediaType.parse("application/json"), data);

		// Adding the body sets the Content-type header for us
		post.post(body);

		String appNameForMobiataPushNameHeader = ProductFlavorFeatureConfiguration.getInstance()
			.getAppNameForMobiataPushNameHeader();
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
		List<BasicNameValuePair> additionalParamsForReviewsRequest = ProductFlavorFeatureConfiguration.getInstance()
			.getAdditionalParamsForReviewsRequest();
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

	private <T extends Response> T doRequest(Request.Builder request, ResponseHandler<T> responseHandler, int flags) {
		final String userAgent = ServicesUtil.generateUserAgentString();

		OkHttpClient okHttpClient = mClient;
		request.addHeader("User-Agent", userAgent);
		request.addHeader("Accept-Encoding", "gzip");

		String mobvisid = AdvertisingIdUtils.getIDFA();
		if (Strings.isNotEmpty(mobvisid)) {
			request.addHeader("x-mobvisid", mobvisid);
		}

		String devLocation = ServicesUtil.generateXDevLocationString(mContext);
		if (Strings.isNotEmpty(devLocation)) {
			request.addHeader("x-dev-loc", devLocation);
		}

		final boolean ignoreCookies = (flags & F_IGNORE_COOKIES) != 0;
		if (ignoreCookies) {
			// We don't want cookies so we cannot use the cached client
			JavaNetCookieJar cookieJar = new JavaNetCookieJar(sBlackHoleCookieManager);

			okHttpClient = mOkHttpClientFactory.getOkHttpClient(cookieJar);
		}

		// Make the request
		long start = System.currentTimeMillis();
		mCancellingDownload = false;
		okhttp3.Response response = null;
		try {
			mRequest = request.build();
			call = okHttpClient.newCall(mRequest);
			response = call.execute();
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
				response.body().close();
			}
			Log.d("Total request time: " + (System.currentTimeMillis() - start) + " ms");
			mRequest = null;
		}

		return null;
	}

	private boolean doGet(String url, List<BasicNameValuePair> params) {
		Log.d(TAG_REQUEST, "" + url + "?" + NetUtils.getParamsForLogging(params));

		Request.Builder request = createHttpGet(url, params);
		final String userAgent = ServicesUtil.generateUserAgentString();

		request.addHeader("User-Agent", userAgent);
		request.addHeader("Accept-Encoding", "gzip");

		// Make the request
		long start = System.currentTimeMillis();
		mCancellingDownload = false;
		okhttp3.Response response = null;
		try {
			mRequest = request.build();
			call = mClient.newCall(mRequest);
			response = call.execute();
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
				response.body().close();
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
					call.cancel();
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
