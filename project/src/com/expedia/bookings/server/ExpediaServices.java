package com.expedia.bookings.server;

import java.io.IOException;
import java.net.Socket;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.data.AvailabilityResponse;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.BookingResponse;
import com.expedia.bookings.data.CreateItineraryResponse;
import com.expedia.bookings.data.CreateTripResponse;
import com.expedia.bookings.data.FlightCheckoutResponse;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.FlightSearchResponse;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Itinerary;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.Response;
import com.expedia.bookings.data.ReviewsResponse;
import com.expedia.bookings.data.ReviewsStatisticsResponse;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.SearchResponse;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.ServerError.ErrorCode;
import com.expedia.bookings.data.SignInResponse;
import com.expedia.bookings.data.SuggestResponse;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.Traveler.Gender;
import com.expedia.bookings.data.User;
import com.expedia.bookings.utils.CalendarUtils;
import com.expedia.bookings.utils.LocaleUtils;
import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.BackgroundDownloader.DownloadListener;
import com.mobiata.android.Log;
import com.mobiata.android.net.AndroidHttpClient;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.NetUtils;
import com.mobiata.android.util.SettingUtils;

public class ExpediaServices implements DownloadListener {

	private static final String BAZAAR_VOICE_BASE_URL = "http://reviews.expedia.com/data/reviews.json";
	private static final String BAZAAR_VOICE_API_TOKEN = "tq2es494c5r0o2443tc4byu2q";
	private static final String BAZAAR_VOICE_API_VERSION = "5.1";

	private static final String EXPEDIA_SUGGEST_BASE_URL = "http://suggest.expedia.com/hint/es/v1/ac/";

	public static final int REVIEWS_PER_PAGE = 25;

	public static final int HOTEL_MAX_RESULTS = 200;

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

	// Flags for availability()
	public static final int F_EXPENSIVE = 4;

	// Flags for getE3EndpointUrl()
	public static final int F_HOTELS = 8;
	public static final int F_FLIGHTS = 16;

	// Flags for addBillingInfo()
	public static final int F_HAS_TRAVELER = 32;

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
	//// Expedia Suggest API

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
		if (query == null || query.length() < 3) {
			return null;
		}

		// We're displaying data to the user, so use his default locale.
		String localeString = Locale.getDefault().toString();

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
			// 30 is all regions (CITY, MULTICITY, NEIGHBORHOOD, POI)
			params.add(new BasicNameValuePair("type", "30"));

			responseHandler.setType(SuggestResponseHandler.Type.HOTELS);
		}

		HttpGet get = NetUtils.createHttpGet(url, params);
		get.addHeader("Accept", "application/json");

		// Some logging before passing the request along^M
		Log.d("Autosuggest request: " + url + "?" + NetUtils.getParamsForLogging(params));

		return doRequest(get, responseHandler, 0);
	}

	//////////////////////////////////////////////////////////////////////////
	// Expedia Flights API

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

		// TODO: Delete this once no longer valid (all results will eventually be returned as a matrix)
		query.add(new BasicNameValuePair("matrix", "true"));

		return doFlightsRequest("api/flight/search", query, new FlightSearchResponseHandler(mContext), flags);
	}

	public CreateItineraryResponse createItinerary(String productKey, int flags) {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();
		query.add(new BasicNameValuePair("productKey", productKey));

		return doFlightsRequest("api/flight/trip/create", query, new CreateItineraryResponseHandler(mContext), flags
				| F_SECURE_REQUEST);
	}

	public FlightCheckoutResponse flightCheckout(FlightTrip flightTrip, Itinerary itinerary, BillingInfo billingInfo,
			List<Traveler> travelers, int flags) {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();

		query.add(new BasicNameValuePair("tripId", itinerary.getTripId()));
		query.add(new BasicNameValuePair("expectedTotalFare", flightTrip.getTotalFare().getAmount().toString() + ""));
		query.add(new BasicNameValuePair("expectedFareCurrencyCode", flightTrip.getTotalFare().getCurrency()));

		addBillingInfo(query, billingInfo, F_HAS_TRAVELER);

		for (int i = 0; i < travelers.size(); i++) {
			addFlightTraveler(query, travelers.get(i));
		}

		String nameOnCard = billingInfo.getNameOnCard();
		if (!TextUtils.isEmpty(nameOnCard)) {
			query.add(new BasicNameValuePair("nameOnCard", nameOnCard));
		}

		// Checkout calls without this flag can make ACTUAL bookings!
		if (suppressFinalBooking(mContext)) {
			query.add(new BasicNameValuePair("suppressFinalBooking", "true"));
		}

		return doFlightsRequest("api/flight/checkout", query, new FlightCheckoutResponseHandler(mContext), flags
				+ F_SECURE_REQUEST);
	}

	// Suppress final bookings if we're not in release mode and the preference is set to suppress
	public static boolean suppressFinalBooking(Context context) {
		return !AndroidUtils.isRelease(context)
				&& SettingUtils.get(context, context.getString(R.string.preference_suppress_bookings), true);
	}

	//////////////////////////////////////////////////////////////////////////
	//// E3 API

	private static final String ISO_FORMAT = "yyyy-MM-dd";

	public SearchResponse search(SearchParams params, int sortType) {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();

		query.add(new BasicNameValuePair("sourceType", "mobileapp"));
		addPOSParams(query);

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

		addBasicParams(query, params);

		// These values are always the same (for now)
		query.add(new BasicNameValuePair("resultsPerPage", HOTEL_MAX_RESULTS + ""));
		query.add(new BasicNameValuePair("pageIndex", "0"));
		query.add(new BasicNameValuePair("filterUnavailable", "true"));

		SearchResponseHandler rh = new SearchResponseHandler(mContext);
		if (params.hasSearchLatLon()) {
			rh.setLatLng(params.getSearchLatitude(), params.getSearchLongitude());
		}
		rh.setNumNights(params.getStayDuration());
		return doE3Request("MobileHotel/Webapp/SearchResults", query, rh, 0);
	}

	/**
	 * HotelInformation request.
	 * 
	 * Uses AvailabilityResponse as the return, as the "information" request is essentially the
	 * same as a non-expensive AvailabilityResponse request.
	 */
	public AvailabilityResponse information(Property property) {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();

		query.add(new BasicNameValuePair("sourceType", "mobileapp"));
		addPOSParams(query);

		query.add(new BasicNameValuePair("hotelId", property.getPropertyId()));

		AvailabilityResponseHandler responseHandler = new AvailabilityResponseHandler(mContext, null, property);
		return doE3Request("MobileHotel/Webapp/HotelInformation", query, responseHandler, 0);
	}

	public AvailabilityResponse availability(SearchParams params, Property property) {
		return availability(params, property, F_EXPENSIVE);
	}

	public AvailabilityResponse availability(SearchParams params, Property property, int flags) {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();

		query.add(new BasicNameValuePair("sourceType", "mobileapp"));
		addPOSParams(query);

		query.add(new BasicNameValuePair("hotelId", property.getPropertyId()));

		addBasicParams(query, params);

		if ((flags & F_EXPENSIVE) != 0) {
			query.add(new BasicNameValuePair("makeExpensiveRealtimeCall", "true"));
		}

		AvailabilityResponseHandler responseHandler = new AvailabilityResponseHandler(mContext, params, property);
		AvailabilityResponse response = doE3Request("MobileHotel/Webapp/HotelOffers", query, responseHandler, 0);

		// #12701: Often times, Atlantis cache screws up and returns the error "Hotel product's PIID that is 
		// provided by Atlantis has expired."  This error only happens once - the next request is usually fine.
		// As a result, the workaround here is to immediately make a second identical request if the first one
		// fails (for ONLY that reason).
		if (response != null && response.hasErrors()) {
			ServerError error = response.getErrors().get(0);
			if (error.getErrorCode() == ErrorCode.HOTEL_ROOM_UNAVAILABLE
					&& "Hotel product\u0027s PIID that is provided by Atlantis has expired".equals(error.getMessage())) {
				Log.w("Atlantis PIID expired, automatically retrying HotelOffers request once.");
				response = doE3Request("MobileHotel/Webapp/HotelOffers", query, responseHandler, 0);
			}
		}

		return response;
	}

	public BookingResponse reservation(SearchParams params, Property property, Rate rate, BillingInfo billingInfo,
			String tripId, String userId, Long tuid) {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();

		query.add(new BasicNameValuePair("sourceType", "mobileapp"));
		addPOSParams(query);

		query.add(new BasicNameValuePair("hotelId", property.getPropertyId()));
		query.add(new BasicNameValuePair("productKey", rate.getRateKey()));

		addBasicParams(query, params);

		addBillingInfo(query, billingInfo, 0);

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
					Thread.sleep(3000);
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

		return doE3Request("MobileHotel/Webapp/Checkout", query, new BookingResponseHandler(mContext), F_SECURE_REQUEST);
	}

	public CreateTripResponse createTripWithCoupon(String couponCode, SearchParams params, Property property, Rate rate) {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();

		query.add(new BasicNameValuePair("sourceType", "mobileapp"));
		addPOSParams(query);
		addBasicParams(query, params);

		query.add(new BasicNameValuePair("productKey", rate.getRateKey()));
		query.add(new BasicNameValuePair("couponCode", couponCode));

		CreateTripResponseHandler responseHandler = new CreateTripResponseHandler(mContext, params, property);
		return doE3Request("MobileHotel/Webapp/CreateTrip", query, responseHandler, F_SECURE_REQUEST);
	}

	public SignInResponse signIn(String email, String password, int flags) {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();

		query.add(new BasicNameValuePair("sourceType", "mobileapp"));
		addPOSParams(query);

		query.add(new BasicNameValuePair("email", email));
		query.add(new BasicNameValuePair("password", password));
		query.add(new BasicNameValuePair("staySignedIn", "true"));

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

		query.add(new BasicNameValuePair("sourceType", "mobileapp"));
		addPOSParams(query);

		query.add(new BasicNameValuePair("profileOnly", "true"));

		addProfileTypes(query, flags);

		return doE3Request("MobileHotel/Webapp/SignIn", query, new SignInResponseHandler(mContext), F_SECURE_REQUEST);
	}

	public SignInResponse updateTraveler(Traveler traveler, int flags) {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();

		query.add(new BasicNameValuePair("tuid", "" + traveler.getTuid()));

		addProfileTypes(query, flags | F_FLIGHTS | F_HOTELS);

		return doE3Request("api/user/profile", query, new SignInResponseHandler(mContext), F_SECURE_REQUEST);
	}

	public void clearCookies() {
		Log.d("Clearing cookies!");

		PersistantCookieStore cookieStore = new PersistantCookieStore();
		cookieStore.clear();
		cookieStore.save(mContext, COOKIES_FILE);
	}

	private void addBasicParams(List<BasicNameValuePair> query, SearchParams params) {
		DateFormat df = new SimpleDateFormat(ISO_FORMAT);
		df.setTimeZone(CalendarUtils.getFormatTimeZone());

		// #13586: We need a second SimpleDateFormat because on 2.2 and below.  See
		// ticket for more info (bug is complex).
		DateFormat df2 = new SimpleDateFormat(ISO_FORMAT);
		df2.setTimeZone(CalendarUtils.getFormatTimeZone());

		query.add(new BasicNameValuePair("checkInDate", df.format(params.getCheckInDate().getTime())));
		query.add(new BasicNameValuePair("checkOutDate", df2.format(params.getCheckOutDate().getTime())));

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

	private void addPOSParams(List<BasicNameValuePair> query) {
		String langId = LocaleUtils.getDualLanguageId(mContext);
		if (langId != null) {
			query.add(new BasicNameValuePair("langid", langId));
		}

		if (!AndroidUtils.isRelease(mContext) && getEndPoint(mContext) == EndPoint.PUBLIC_INTEGRATION) {
			query.add(new BasicNameValuePair("siteid", LocaleUtils.getSiteId(mContext)));
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

		query.add(new BasicNameValuePair("profileTypes", StrUtils.join(profileTypes, ",")));
	}

	private void addBillingInfo(List<BasicNameValuePair> query, BillingInfo billingInfo, int flags) {
		if ((flags & F_HAS_TRAVELER) == 0) {
			// Don't add firstname/lastname if we're adding it through the traveler interface later
			query.add(new BasicNameValuePair("firstName", billingInfo.getFirstName()));
			query.add(new BasicNameValuePair("lastName", billingInfo.getLastName()));
		}

		query.add(new BasicNameValuePair("phoneCountryCode", billingInfo.getTelephoneCountryCode()));
		query.add(new BasicNameValuePair("phone", billingInfo.getTelephone()));
		query.add(new BasicNameValuePair("email", billingInfo.getEmail()));

		// F670: Location can be null if we are using a stored credit card
		Location location = billingInfo.getLocation();
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
			query.add(new BasicNameValuePair("postalCode", location.getPostalCode()));
			query.add(new BasicNameValuePair("country", location.getCountryCode()));
		}

		if (billingInfo.getStoredCard() == null) {
			query.add(new BasicNameValuePair("creditCardNumber", billingInfo.getNumber()));

			Date expDate = billingInfo.getExpirationDate().getTime();

			DateFormat expFormatter = new SimpleDateFormat("MMyy");
			query.add(new BasicNameValuePair("expirationDate", expFormatter.format(expDate)));

			// This is an alternative way of representing expiration date, used for Flights.
			// Doesn't hurt to include both methods
			query.add(new BasicNameValuePair("expirationDateYear", android.text.format.DateFormat.format("yyyy",
					expDate)
					.toString()));
			query.add(new BasicNameValuePair("expirationDateMonth", android.text.format.DateFormat
					.format("MM", expDate)
					.toString()));
		}
		else {
			query.add(new BasicNameValuePair("storedCreditCardId", billingInfo.getStoredCard().getId()));
		}
		query.add(new BasicNameValuePair("cvv", billingInfo.getSecurityCode()));
	}

	private void addFlightTraveler(List<BasicNameValuePair> query, Traveler traveler) {
		//TODO: This is incomplete. There is a bunch of information not currently supported by the API that needs to go here. 
		// Furthermore, there should be any number of travelers and they shouldn't overwrite one another's birthdays etc. Again, we wait for API updates.
		SimpleDateFormat isoDateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
		query.add(new BasicNameValuePair("firstName", traveler.getFirstName()));
		if (!TextUtils.isEmpty(traveler.getMiddleName())) {
			query.add(new BasicNameValuePair("middleName", traveler.getMiddleName()));
		}
		query.add(new BasicNameValuePair("lastName", traveler.getLastName()));
		query.add(new BasicNameValuePair("birthDate", isoDateFormatter.format(traveler.getBirthDateInMillis())));
		query.add(new BasicNameValuePair("gender", (traveler.getGender() == Gender.MALE) ? "MALE" : "FEMALE"));

		// TODO: We barely have assistance options represented at the moment, update later
		String assistanceOption;
		switch (traveler.getAssistance()) {
		case WHEELCHAIR:
			assistanceOption = "WHEELCHAIRCANNOTCLIMBSTAIRS";
			break;
		default:
			assistanceOption = "NONE";
			break;
		}

		query.add(new BasicNameValuePair("specialAssistanceOption", assistanceOption));

	}

	//////////////////////////////////////////////////////////////////////////
	//// BazaarVoice (Reviews) API

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

		String localesString = LocaleUtils.formatLanguageCodes(languages);

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

		List<String> languages = LocaleUtils.getLanguages(mContext);
		String localesString = LocaleUtils.formatLanguageCodes(languages);

		query.add(new BasicNameValuePair("Filter", "ContentLocale:" + localesString));

		query.add(new BasicNameValuePair("FilteredStats", "Reviews"));

		query.add(new BasicNameValuePair("Include", "Products"));

		return doBazaarRequest(query, new ReviewsStatisticsResponseHandler(mContext));
	}

	//////////////////////////////////////////////////////////////////////////
	//// Request code

	private <T extends Response> T doBazaarRequest(List<BasicNameValuePair> params, ResponseHandler<T> responseHandler) {
		HttpGet get = NetUtils.createHttpGet(BAZAAR_VOICE_BASE_URL, params);

		Log.d("Bazaar reviews request:  " + get.getURI().toString());

		return doRequest(get, responseHandler, 0);
	}

	private <T extends Response> T doE3Request(String targetUrl, List<BasicNameValuePair> params,
			ResponseHandler<T> responseHandler, int flags) {
		String serverUrl = getE3EndpointUrl(flags) + targetUrl;

		// Create the request
		HttpPost post = NetUtils.createHttpPost(serverUrl, params);

		// Some logging before passing the request along
		Log.d("Request: " + serverUrl + "?" + NetUtils.getParamsForLogging(params));

		return doRequest(post, responseHandler, flags);
	}

	private <T extends Response> T doFlightsRequest(String targetUrl, List<BasicNameValuePair> params,
			ResponseHandler<T> responseHandler, int flags) {
		return doE3Request(targetUrl, params, responseHandler, flags | F_FLIGHTS);
	}

	private <T extends Response> T doRequest(HttpRequestBase request, ResponseHandler<T> responseHandler, int flags) {
		// Construct a proper user agent string
		String versionName;
		try {
			PackageManager pm = mContext.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), 0);
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

		mRequest = request;
		AndroidHttpClient client = AndroidHttpClient.newInstance(userAgent, mContext);
		AndroidHttpClient.modifyRequestToAcceptGzipResponse(mRequest);
		HttpParams httpParameters = client.getParams();
		HttpConnectionParams.setSoTimeout(httpParameters, 100000);

		// TODO: Find some way to keep this easily in memory so we're not saving/loading after each request.
		PersistantCookieStore cookieStore = new PersistantCookieStore();
		cookieStore.load(mContext, COOKIES_FILE);
		HttpContext httpContext = new BasicHttpContext();
		httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
		CookieSpecRegistry cookieSpecRegistry = new CookieSpecRegistry();
		cookieSpecRegistry.register("EXPEDIA", new ExpediaCookieSpecFactory(mContext));
		httpContext.setAttribute(ClientContext.COOKIESPEC_REGISTRY, cookieSpecRegistry);

		Log.v("Sending cookies: " + cookieStore.toString());

		HttpClientParams.setCookiePolicy(httpParameters, "EXPEDIA");

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
			return client.execute(mRequest, responseHandler, httpContext);
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

			Log.v("Received cookies: " + cookieStore.toString());

			cookieStore.save(mContext, COOKIES_FILE);

			mRequest = null;
		}

		return null;
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

	public enum EndPoint {
		PRODUCTION,
		DEV,
		INTEGRATION,
		STABLE,
		PROXY,
		PUBLIC_INTEGRATION,
		TRUNK
	}

	/**
	 * Returns the base E3 server url, based on dev settings
	 * @param context
	 * @return
	 */
	public String getE3EndpointUrl(int flags) {
		EndPoint endPoint = getEndPoint(mContext);

		StringBuilder builder = new StringBuilder();

		builder.append(endPoint != EndPoint.PROXY && (flags & F_SECURE_REQUEST) != 0 ? "https://" : "http://");

		switch (endPoint) {
		case PRODUCTION: {
			builder.append("www.");
			builder.append(LocaleUtils.getPointOfSale(mContext));
			builder.append("/");
			break;
		}
		case INTEGRATION: {
			builder.append("www");
			for (String s : LocaleUtils.getPointOfSale(mContext).split("\\.")) {
				builder.append(s);
			}
			builder.append(".integration.sb.karmalab.net/");
			break;
		}
		case STABLE: {
			builder.append("www");
			for (String s : LocaleUtils.getPointOfSale(mContext).split("\\.")) {
				builder.append(s);
			}
			builder.append(".stable.sb.karmalab.net/");
			break;
		}
		case DEV: {
			builder.append("www.");
			builder.append(LocaleUtils.getPointOfSale(mContext));
			builder.append(".chelwebestr37.bgb.karmalab.net/");
			break;
		}
		case TRUNK: {
			builder.append("wwwexpediacom.trunk.sb.karmalab.net/");
			break;
		}
		case PUBLIC_INTEGRATION: {
			builder.append("70.42.224.37/");
			break;
		}
		case PROXY: {
			builder.append(SettingUtils.get(mContext, mContext.getString(R.string.preference_proxy_server_address),
					"localhost:3000"));
			builder.append("/");
			builder.append(LocaleUtils.getPointOfSale(mContext));
			builder.append("/");
			break;
		}
		}

		String e3url = builder.toString();
		Log.d("e3 url: " + e3url);
		return e3url;
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
		else {
			return EndPoint.PRODUCTION;
		}
	}

	//////////////////////////////////////////////////////////////////////////
	//// Download listener stuff

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
						mRequest.abort();
						mRequest = null;
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
	//// Deprecated API calls
	//
	// We need to find replacements for these calls in E3

	@SuppressWarnings("unused")
	@Deprecated
	private void addStandardRequestFields(JSONObject request, String type) throws JSONException {
		request.put("type", type);
		if (!AndroidUtils.isRelease(mContext)) {
			request.put("echoRequest", true);
		}
		request.put("cid", 345106);
	}
}
