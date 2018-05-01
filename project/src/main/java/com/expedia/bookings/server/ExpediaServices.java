package com.expedia.bookings.server;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;

import com.crashlytics.android.Crashlytics;
import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.AssociateUserToTripResponse;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightStatsFlightResponse;
import com.expedia.bookings.data.PushNotificationRegistrationResponse;
import com.expedia.bookings.data.Response;
import com.expedia.bookings.data.RoutesResponse;
import com.expedia.bookings.data.SignInResponse;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.Traveler.AssistanceType;
import com.expedia.bookings.data.Traveler.Gender;
import com.expedia.bookings.data.TravelerCommitResponse;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.trips.TripResponse;
import com.expedia.bookings.data.user.UserStateManager;
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.notification.PushNotificationUtils;
import com.expedia.bookings.services.PersistentCookiesCookieJar;
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

import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONObject;

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

import okhttp3.Call;
import okhttp3.Interceptor;
import okhttp3.JavaNetCookieJar;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

@SuppressLint("SimpleDateFormat")
public class ExpediaServices implements DownloadListener, ExpediaServicesPushInterface {

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

	// Flags for getE3EndpointUrl()
	public static final int F_HOTELS = 4;
	public static final int F_FLIGHTS = 8;

	// Flags for GET vs. POST
	private static final int F_GET = 32;
	private static final int F_POST = 64;

	// Skips all cookie sending/receiving
	private static final int F_IGNORE_COOKIES = 128;

	// Flag to indicate that we don't need to add the Endpoint while making an E3request
	private static final int F_DONT_ADD_ENDPOINT = 512;

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
	// Airport Dropdown Suggest

	public RoutesResponse flightRoutes() {
		List<BasicNameValuePair> query = new ArrayList<>();

		addCommonParams(query);

		return doFlightsRequest("api/flight/airportDropDown", query, new RoutesResponseHandler(mContext), 0);
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
		ArrayList<BasicNameValuePair> parameters = new ArrayList<>();

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

	//////////////////////////////////////////////////////////////////////////
	// Expedia Itinerary API
	//
	// Documentation: https://www.expedia.com/static/mobile/APIConsole/trip.html

	public TripResponse getTrips() {
		List<BasicNameValuePair> query = new ArrayList<>();
		addCommonParams(query);
		query.add(new BasicNameValuePair("filterBookingStatus", "PENDING"));
		query.add(new BasicNameValuePair("filterBookingStatus", "BOOKED"));
		query.add(new BasicNameValuePair("filterTimePeriod", "UPCOMING"));
		query.add(new BasicNameValuePair("filterTimePeriod", "INPROGRESS"));
		query.add(new BasicNameValuePair("filterTimePeriod", "RECENTLY_COMPLETED"));
		query.add(new BasicNameValuePair("sort", "SORT_STARTDATE_ASCENDING"));
		return doE3Request("api/trips", query, new TripResponseHandler(mContext), F_GET);
	}

	//////////////////////////////////////////////////////////////////////////
	// Expedia user account API
	//
	// Documentation: https://www.expedia.com/static/mobile/APIConsole/flight.html

	// Attempt to sign in again with the stored cookie
	public SignInResponse signIn(int flags) {
		List<BasicNameValuePair> query = new ArrayList<>();

		addSignInParams(query, flags);
		if (AbacusFeatureConfigManager.isBucketedForTest(mContext, AbacusUtils.EBAndroidAppAccountsAPIKongEndPoint)) {
			return doApimE3Request("api/user/sign-in", query, new SignInResponseHandler());
		}
		else {
			return doE3Request("api/user/sign-in", query, new SignInResponseHandler());
		}
	}

	public SignInResponse signInWithEmailForAutomationTests(int flags, String email) {
		if (!ExpediaBookingApp.isAutomation()) {
			throw new RuntimeException("signInWithEmailForAutomationTests can be called only from automation builds");
		}
		List<BasicNameValuePair> query = new ArrayList<>();

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
		List<BasicNameValuePair> query = new ArrayList<>();

		addCommonParams(query);

		query.add(new BasicNameValuePair("tripId", tripId));

		addProfileTypes(query, flags);
		if (AbacusFeatureConfigManager.isBucketedForTest(mContext, AbacusUtils.EBAndroidAppAccountsAPIKongEndPoint)) {
			return doApimE3Request("api/user/associateUserToTrip", query, new AssociateUserToTripResponseHandler(mContext));
		}
		else {
			return doE3Request("api/user/associateUserToTrip", query, new AssociateUserToTripResponseHandler(mContext));
		}
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
		List<BasicNameValuePair> query = new ArrayList<>();

		addCommonParams(query);

		query.add(new BasicNameValuePair("tuid", "" + traveler.getTuid()));

		addProfileTypes(query, flags | F_FLIGHTS | F_HOTELS);

		if (AbacusFeatureConfigManager.isBucketedForTest(mContext, AbacusUtils.EBAndroidAppAccountsAPIKongEndPoint)) {
			return doApimE3Request("api/user/profile", query, new SignInResponseHandler());
		}
		else {
			return doE3Request("api/user/profile", query, new SignInResponseHandler());
		}
	}

	/**
	 * Update (or create) an expedia account traveler
	 *
	 * @param traveler
	 * @return
	 */
	public TravelerCommitResponse commitTraveler(Traveler traveler) {
		if (userStateManager.isUserAuthenticated()) {
			List<BasicNameValuePair> query = new ArrayList<>();
			addFlightTraveler(query, traveler, "");
			addCommonParams(query);
			Log.i(TAG_REQUEST, "update-traveler body:" + NetUtils.getParamsForLogging(query));
			if (AbacusFeatureConfigManager.isBucketedForTest(mContext, AbacusUtils.EBAndroidAppAccountsAPIKongEndPoint)) {
				return doFlightsAPIMRequest("api/user/update", query, new TravelerCommitResponseHandler(mContext, traveler));
			}
			else {
				return doFlightsRequest("api/user/update-traveler", query, new TravelerCommitResponseHandler(mContext,
					traveler));
			}
		}
		else {
			return null;
		}
	}

	private void addProfileTypes(List<BasicNameValuePair> query, int flags) {
		List<String> profileTypes = new ArrayList<>();

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

		String phoneCountryCode = null;
		String phoneNumberString = null;
		String travelerEmail = null;

		if (userStateManager.getUserSource().getUser() != null) {
			Traveler primaryTraveler = userStateManager.getUserSource().getUser().getPrimaryTraveler();

			phoneCountryCode = primaryTraveler.getPhoneCountryCode();
			phoneNumberString = primaryTraveler.getPhoneNumber();
			travelerEmail = primaryTraveler.getEmail();
		}

		String travelerPhoneCountryCode = !TextUtils.isEmpty(traveler.getPhoneCountryCode()) ?
			traveler.getPhoneCountryCode() : phoneCountryCode;

		query.add(new BasicNameValuePair(prefix + "phoneCountryCode", travelerPhoneCountryCode));

		String travelerPhoneNumber = !TextUtils.isEmpty(traveler.getOrCreatePrimaryPhoneNumber().getNumber()) ?
			traveler.getPrimaryPhoneNumber().getNumber() : phoneNumberString;

		query.add(new BasicNameValuePair(prefix + "phone", travelerPhoneNumber));

		//Email is required (but there is no traveler email entry)
		String email = traveler.getEmail();
		if (TextUtils.isEmpty(email)) {
			email = Db.getBillingInfo().getEmail();
		}
		if (TextUtils.isEmpty(email)) {
			email = travelerEmail;
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
					PushNotificationRegistrationResponse response = doRequest(post, responseHandler, F_POST, new ArrayList<Interceptor>());
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

	@Override
	public PushNotificationRegistrationResponse registerForPushNotifications(
		ResponseHandler<PushNotificationRegistrationResponse> responseHandler, JSONObject payload, String regId) {
		String serverUrl = PushNotificationUtils.getRegistrationUrl();
		return registerForPushNotifications(serverUrl, responseHandler, payload, regId);
	}

	//////////////////////////////////////////////////////////////////////////
	// Request code

	private <T extends Response> T doFlightsRequest(String targetUrl, List<BasicNameValuePair> params,
		ResponseHandler<T> responseHandler) {
		return doFlightsRequest(targetUrl, params, responseHandler, F_FLIGHTS);
	}

	private <T extends Response> T doFlightsAPIMRequest(String targetUrl, List<BasicNameValuePair> params,
		ResponseHandler<T> responseHandler) {
		return doApimE3Request(targetUrl, params, responseHandler, F_FLIGHTS);
	}

	private <T extends Response> T doFlightsRequest(String targetUrl, List<BasicNameValuePair> params,
		ResponseHandler<T> responseHandler, int flags) {
		return doE3Request(targetUrl, params, responseHandler, flags | F_FLIGHTS, new ArrayList<Interceptor>());
	}

	private <T extends Response> T doE3Request(String targetUrl, List<BasicNameValuePair> params,
		ResponseHandler<T> responseHandler) {
		return doE3Request(targetUrl, params, responseHandler, 0);
	}

	private <T extends Response> T doE3Request(String targetUrl, List<BasicNameValuePair> params,
		ResponseHandler<T> responseHandler, int flags) {
		return doE3Request(targetUrl, params, responseHandler, flags, new ArrayList<Interceptor>());
	}

	private <T extends Response> T doApimE3Request(String targetUrl, List<BasicNameValuePair> params,
		ResponseHandler<T> responseHandler, int flags) {

		EndpointProvider endpointProvider = Ui.getApplication(mContext).appComponent().endpointProvider();
		List<Interceptor> interceptorList = new ArrayList<>();

		return doE3Request(endpointProvider.getKongEndpointUrl() + targetUrl, params, responseHandler,
			flags | F_DONT_ADD_ENDPOINT, interceptorList);
	}

	private <T extends Response> T doApimE3Request(String targetUrl, List<BasicNameValuePair> params,
		ResponseHandler<T> responseHandler) {
		return doApimE3Request(targetUrl, params, responseHandler, 0);
	}

	private <T extends Response> T doE3Request(String targetUrl, List<BasicNameValuePair> params,
		ResponseHandler<T> responseHandler, int flags, List<Interceptor> interceptorList) {
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

		return doRequest(base, responseHandler, flags, interceptorList);
	}

	private <T extends Response> T doFlightStatsRequest(String baseUrl, List<BasicNameValuePair> params,
		ResponseHandler<T> responseHandler) {
		Request.Builder base = createHttpGet(baseUrl, params);
		return doRequest(base, responseHandler, F_IGNORE_COOKIES, new ArrayList<Interceptor>());
	}

	private <T extends Response> T doRequest(Request.Builder request, ResponseHandler<T> responseHandler, int flags, List<Interceptor> interceptorList) {
		final String userAgent = ServicesUtil.generateUserAgentString();

		OkHttpClient.Builder okHttpClientBuilder = mClient.newBuilder();
		for (Interceptor interceptor : interceptorList) {
			okHttpClientBuilder.addInterceptor(interceptor);
		}
		OkHttpClient okHttpClient = okHttpClientBuilder.build();
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
			return responseHandler.handleResponse(response);
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
