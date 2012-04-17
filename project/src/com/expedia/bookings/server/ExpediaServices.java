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
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.cookie.NetscapeDraftSpec;
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

import com.expedia.bookings.R;
import com.expedia.bookings.data.AvailabilityResponse;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.BookingResponse;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.ReviewsResponse;
import com.expedia.bookings.data.ReviewsStatisticsResponse;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.SearchResponse;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.ServerError.ErrorCode;
import com.expedia.bookings.data.SignInResponse;
import com.expedia.bookings.data.SuggestResponse;
import com.expedia.bookings.utils.CalendarUtils;
import com.expedia.bookings.utils.LocaleUtils;
import com.mobiata.android.BackgroundDownloader.DownloadListener;
import com.mobiata.android.Log;
import com.mobiata.android.net.AndroidHttpClient;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.NetUtils;
import com.mobiata.android.util.SettingUtils;

public class ExpediaServices implements DownloadListener {

	private static final String COOKIES_FILE = "cookies.dat";

	private static final String BAZAAR_VOICE_BASE_URL = "http://reviews.expedia.com/data/reviews.json";
	private static final String BAZAAR_VOICE_API_TOKEN = "tq2es494c5r0o2443tc4byu2q";
	private static final String BAZAAR_VOICE_API_VERSION = "5.1";

	private static final String EXPEDIA_SUGGEST_BASE_URL = "http://suggest.expedia.com/hint/es/v1/ac/";

	public static final int REVIEWS_PER_PAGE = 25;

	public static final int HOTEL_MAX_RESULTS = 200;

	public enum ReviewSort {
		NEWEST_REVIEW_FIRST("NewestReviewFirst"), HIGHEST_RATING_FIRST("HighestRatingFirst"), LOWEST_RATING_FIRST(
				"LowestRatingFirst");

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
	// Examples:
	// http://suggest.expedia.com/hint/es/v1/ac/en_US/bellagio?type=31
	// http://suggest.expedia.com/hint/es/v1/ac/es_MX/seattle?type=31

	public SuggestResponse suggest(String query) {
		if (query == null || query.length() < 3) {
			return null;
		}

		// We're displaying data to the user, so use his default locale.
		String localeString = Locale.getDefault().toString();

		String url = NetUtils.formatUrl(EXPEDIA_SUGGEST_BASE_URL + localeString + "/" + query);

		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("type", "30")); // city & multi-city & neighborhood & POI

		HttpGet get = NetUtils.createHttpGet(url, params);
		get.addHeader("Accept", "application/json");
		get.addHeader("Accept-Encoding", "gzip");

		SuggestResponseHandler responseHandler = new SuggestResponseHandler(mContext);

		return (SuggestResponse) doRequest(get, responseHandler, 0);
	}

	//////////////////////////////////////////////////////////////////////////
	//// E3 API

	private static final String ISO_FORMAT = "yyyy-MM-dd";

	public SearchResponse search(SearchParams params, int sortType) {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();

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
		else if (params.hasFreeformLocation()) {
			Log.d("Searching by city...");
			query.add(new BasicNameValuePair("city", params.getFreeformLocation()));
		}

		addBasicParams(query, params);

		// These values are always the same (for now)
		query.add(new BasicNameValuePair("resultsPerPage", HOTEL_MAX_RESULTS + ""));
		query.add(new BasicNameValuePair("pageIndex", "0"));
		query.add(new BasicNameValuePair("filterUnavailable", "true"));
		query.add(new BasicNameValuePair("filterInventoryType", "MERCHANT"));
		query.add(new BasicNameValuePair("wantTotalRecommendations", "true"));

		SearchResponseHandler rh = new SearchResponseHandler(mContext);
		if (params.hasSearchLatLon()) {
			rh.setLatLng(params.getSearchLatitude(), params.getSearchLongitude());
		}
		rh.setNumNights(params.getStayDuration());
		return (SearchResponse) doE3Request("SearchResults", query, rh, 0);
	}

	public AvailabilityResponse availability(SearchParams params, Property property) {
		return availability(params, property, F_EXPENSIVE);
	}

	public AvailabilityResponse availability(SearchParams params, Property property, int flags) {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();

		addPOSParams(query);

		query.add(new BasicNameValuePair("hotelId", property.getPropertyId()));

		addBasicParams(query, params);

		if ((flags & F_EXPENSIVE) != 0) {
			query.add(new BasicNameValuePair("makeExpensiveRealtimeCall", "true"));
		}

		AvailabilityResponseHandler responseHandler = new AvailabilityResponseHandler(mContext, params, property);
		AvailabilityResponse response = (AvailabilityResponse) doE3Request("HotelOffers", query, responseHandler, 0);

		// #12701: Often times, Atlantis cache screws up and returns the error "Hotel product's PIID that is 
		// provided by Atlantis has expired."  This error only happens once - the next request is usually fine.
		// As a result, the workaround here is to immediately make a second identical request if the first one
		// fails (for ONLY that reason).
		if (response != null && response.hasErrors()) {
			ServerError error = response.getErrors().get(0);
			if (error.getErrorCode() == ErrorCode.HOTEL_ROOM_UNAVAILABLE
					&& "Hotel product\u0027s PIID that is provided by Atlantis has expired".equals(error.getMessage())) {
				Log.w("Atlantis PIID expired, automatically retrying HotelOffers request once.");
				response = (AvailabilityResponse) doE3Request("HotelOffers", query, responseHandler, 0);
			}
		}

		return response;
	}

	public BookingResponse reservation(SearchParams params, Property property, Rate rate, BillingInfo billingInfo) {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();

		addPOSParams(query);

		query.add(new BasicNameValuePair("hotelId", property.getPropertyId()));
		query.add(new BasicNameValuePair("productKey", rate.getRateKey()));

		addBasicParams(query, params);

		query.add(new BasicNameValuePair("firstName", billingInfo.getFirstName()));
		query.add(new BasicNameValuePair("lastName", billingInfo.getLastName()));
		query.add(new BasicNameValuePair("phoneCountryCode", billingInfo.getTelephoneCountryCode()));
		query.add(new BasicNameValuePair("phone", billingInfo.getTelephone()));
		query.add(new BasicNameValuePair("email", billingInfo.getEmail()));

		Location location = billingInfo.getLocation();
		query.add(new BasicNameValuePair("streetAddress", location.getStreetAddressString()));
		query.add(new BasicNameValuePair("city", location.getCity()));
		query.add(new BasicNameValuePair("state", location.getStateCode()));
		query.add(new BasicNameValuePair("postalCode", location.getPostalCode()));
		query.add(new BasicNameValuePair("country", location.getCountryCode()));

		query.add(new BasicNameValuePair("creditCardNumber", billingInfo.getNumber()));
		query.add(new BasicNameValuePair("cvv", billingInfo.getSecurityCode()));
		query.add(new BasicNameValuePair("sendEmailConfirmation", "true"));

		DateFormat expFormatter = new SimpleDateFormat("MMyy");
		query.add(new BasicNameValuePair("expirationDate", expFormatter.format(billingInfo.getExpirationDate()
				.getTime())));

		return (BookingResponse) doE3Request("Checkout", query, new BookingResponseHandler(mContext), F_SECURE_REQUEST);
	}

	public SignInResponse signIn(String email, String password) {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();

		addPOSParams(query);

		query.add(new BasicNameValuePair("email", email));
		query.add(new BasicNameValuePair("password", password));
		query.add(new BasicNameValuePair("staySignedIn", "true"));

		return (SignInResponse) doE3Request("SignIn", query, new SignInResponseHandler(mContext), F_SECURE_REQUEST);
	}

	// Attempt to sign in again with the stored cookie
	public SignInResponse signIn() {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();

		addPOSParams(query);

		query.add(new BasicNameValuePair("profileOnly", "true"));

		return (SignInResponse) doE3Request("SignIn", query, new SignInResponseHandler(mContext), F_SECURE_REQUEST);
	}

	public void signOut() {
		PersistantCookieStore cookieStore = new PersistantCookieStore();
		cookieStore.load(mContext, COOKIES_FILE);
		cookieStore.clear();
		cookieStore.save(mContext, COOKIES_FILE);
		return;
	}

	private void addBasicParams(List<BasicNameValuePair> query, SearchParams params) {
		DateFormat df = new SimpleDateFormat(ISO_FORMAT);
		df.setTimeZone(CalendarUtils.getFormatTimeZone());
		query.add(new BasicNameValuePair("checkInDate", df.format(params.getCheckInDate().getTime())));
		query.add(new BasicNameValuePair("checkOutDate", df.format(params.getCheckOutDate().getTime())));

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

		return (ReviewsResponse) doBazaarRequest(query, new ReviewsResponseHandler(mContext));
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

		return (ReviewsStatisticsResponse) doBazaarRequest(query, new ReviewsStatisticsResponseHandler(mContext));

	}

	//////////////////////////////////////////////////////////////////////////
	//// Request code

	private Object doBazaarRequest(List<BasicNameValuePair> params, ResponseHandler<?> responseHandler) {
		HttpGet get = NetUtils.createHttpGet(BAZAAR_VOICE_BASE_URL, params);

		Log.d("Bazaar reviews request:  " + get.getURI().toString());

		return doRequest(get, responseHandler, 0);
	}

	private Object doE3Request(String targetUrl, List<BasicNameValuePair> params, ResponseHandler<?> responseHandler,
			int flags) {
		String serverUrl = getE3EndpointUrl(flags) + targetUrl;

		// Create the request
		HttpPost post = NetUtils.createHttpPost(serverUrl, params);

		// Some logging before passing the request along
		Log.d("Request: " + serverUrl + "?" + NetUtils.getParamsForLogging(params));

		return doRequest(post, responseHandler, flags);
	}

	private Object doRequest(HttpRequestBase request, ResponseHandler<?> responseHandler, int flags) {
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
		httpContext.setAttribute(ClientContext.COOKIE_SPEC, new NetscapeDraftSpec());

		HttpClientParams.setCookiePolicy(httpParameters, CookiePolicy.NETSCAPE);

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

			cookieStore.save(mContext, COOKIES_FILE);
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
		PRODUCTION, DEV, INTEGRATION, STABLE, PROXY
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
			builder.append("/MobileHotel/Webapp/");
			break;
		}
		case INTEGRATION: {
			builder.append("www");
			for (String s : LocaleUtils.getPointOfSale(mContext).split("\\.")) {
				builder.append(s);
			}
			builder.append(".integration.sb.karmalab.net/MobileHotel/Webapp/");
			break;
		}
		case STABLE: {
			builder.append("www");
			for (String s : LocaleUtils.getPointOfSale(mContext).split("\\.")) {
				builder.append(s);
			}
			builder.append(".stable.sb.karmalab.net/MobileHotel/Webapp/");
			break;
		}
		case DEV: {
			builder.append("www.");
			builder.append(LocaleUtils.getPointOfSale(mContext));
			builder.append(".chelwebestr37.bgb.karmalab.net");
			builder.append("/MobileHotel/Webapp/");
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
		else if (which.equals("Integration")) {
			return EndPoint.INTEGRATION;
		}
		else if (which.equals("Stable")) {
			return EndPoint.STABLE;
		}
		else {
			return EndPoint.PRODUCTION;
		}
	}

	//////////////////////////////////////////////////////////////////////////
	//// Download listener stuff

	@Override
	public void onCancel() {
		Log.i("Cancelling download!");
		mCancellingDownload = true;
		if (mRequest != null) {
			mRequest.abort();
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
