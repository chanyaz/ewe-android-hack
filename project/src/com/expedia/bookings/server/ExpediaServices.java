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
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.expedia.bookings.data.AvailabilityResponse;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.BookingResponse;
import com.expedia.bookings.data.CreditCardBrand;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.PropertyInfoResponse;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.ReviewsResponse;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.SearchResponse;
import com.expedia.bookings.data.Session;
import com.expedia.bookings.utils.CurrencyUtils;
import com.mobiata.android.BackgroundDownloader.DownloadListener;
import com.mobiata.android.Log;
import com.mobiata.android.net.AndroidHttpClient;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.NetUtils;

public class ExpediaServices implements DownloadListener {

	public static final int REVIEWS_PER_PAGE = 25;

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
	private static final int F_REVIEWS_URL = 2;

	private Context mContext;
	private Session mSession;

	// For cancelling requests
	private HttpRequestBase mPost;

	// This is just so that the error messages aren't treated severely when a download is canceled - naturally,
	// things kind of crash and burn when you kill the connection midway through.
	private boolean mCancellingDownload;

	public ExpediaServices(Context context) {
		mContext = context;
	}

	public ExpediaServices(Context context, Session session) {
		this(context);
		mSession = session;
	}

	public void setSession(Session session) {
		mSession = session;
	}

	//////////////////////////////////////////////////////////////////////////
	//// Expedia requests

	public SearchResponse search(SearchParams params, int sortType) {
		// Construct the request
		JSONObject request = new JSONObject();
		try {
			addStandardRequestFields(request, "list");

			// Construct the body
			JSONObject body = new JSONObject();
			request.put("body", body);

			JSONObject location = new JSONObject();
			body.put("location", location);

			if (params.hasDestinationId()) {
				location.put("destinationId", params.getDestinationId());
			}
			else if (params.hasSearchLatLon()) {
				JSONObject coords = new JSONObject();
				coords.put("latitude", params.getSearchLatitude());
				coords.put("longitude", params.getSearchLongitude());
				location.put("coords", coords);
			}
			else if (params.hasFreeformLocation()) {
				location.put("destinationString", params.getFreeformLocation());
			}

			addBasicSearchParams(body, params);

			body.put("numberOfAdults", params.getNumAdults());
			body.put("numberOfChildren", params.getNumChildren());

			addCurrencyCode(body);

			body.put("locale", getLocaleString());

			// Display scale sets up proper image thumbnails (better looking than defaults)
			request.put("displayScale", 2);

			// Add some details to the body
			body.put("sort", "OVERALL_VALUE");
			body.put("numberOfResults", -1);

			// Limit to 15 mile search radius
			body.put("searchRadiusUnit", "MI");
			body.put("searchRadius", 15);
		}
		catch (JSONException e) {
			Log.e("Could not construct JSON search object.", e);
			return null;
		}

		return (SearchResponse) doRequest(request, new SearchResponseHandler(mContext), 0);
	}

	public SearchResponse search(SearchResponse lastResponse) {
		// Construct the request
		JSONObject request = new JSONObject();
		try {
			addStandardRequestFields(request, "list");

			// Construct the body
			JSONObject body = new JSONObject();
			request.put("body", body);

			body.put("cacheKey", lastResponse.getCachekey());
			body.put("cacheLocation", lastResponse.getCacheLocation());

			body.put("locale", getLocaleString());
		}
		catch (JSONException e) {
			Log.e("Could not construct JSON search object.", e);
			return null;
		}

		return (SearchResponse) doRequest(request, new SearchResponseHandler(mContext), 0);
	}

	public static boolean hasMoreReviews(Property property, int page) {
		int maxPage = (int) Math.ceil(property.getTotalReviews() / (double) REVIEWS_PER_PAGE);
		return maxPage >= page;
	}

	public ReviewsResponse reviews(Property property, int pageNumber, ReviewSort sort) {
		// Check that there are more reviews to add; if there aren't, just return null
		if (!hasMoreReviews(property, pageNumber)) {
			return null;
		}

		if (sort == null) {
			sort = ReviewSort.NEWEST_REVIEW_FIRST;
		}

		JSONObject request = new JSONObject();
		try {
			request.put("sort", sort.getKey());
			request.put("count", REVIEWS_PER_PAGE);
			request.put("propertyId", property.getExpediaPropertyId() + "");
			request.put("index", pageNumber);
		}
		catch (JSONException e) {
			Log.e("Could not construct JSON reviews object.", e);
			return null;
		}

		return (ReviewsResponse) doRequest(request, new ReviewsResponseHandler(mContext), F_REVIEWS_URL);
	}

	public AvailabilityResponse availability(SearchParams params, Property property) {
		// Construct the request
		JSONObject request = new JSONObject();
		try {
			addStandardRequestFields(request, "availability");

			// Construct the body
			JSONObject body = new JSONObject();
			request.put("body", body);

			addBasicSearchParams(body, params);

			JSONArray rooms = new JSONArray();
			JSONObject room = new JSONObject();
			body.put("rooms", rooms);
			rooms.put(room);

			room.put("numberOfAdults", params.getNumAdults());
			room.put("numberOfChildren", params.getNumChildren());

			body.put("hotelId", property.getPropertyId());

			addCurrencyCode(body);

			body.put("locale", getLocaleString());
		}
		catch (JSONException e) {
			Log.e("Could not construct JSON availability object.", e);
			return null;
		}

		return (AvailabilityResponse) doRequest(request, new AvailabilityResponseHandler(mContext, params, property), 0);
	}

	public PropertyInfoResponse info(Property property) {
		// Construct the request
		JSONObject request = new JSONObject();
		try {
			addStandardRequestFields(request, "info");

			// Construct the body
			JSONObject body = new JSONObject();
			request.put("body", body);

			body.put("hotelId", property.getPropertyId());
		}
		catch (JSONException e) {
			Log.e("Could not construct JSON info object.", e);
			return null;
		}

		return (PropertyInfoResponse) doRequest(request, new PropertyInfoResponseHandler(mContext), 0);
	}

	@SuppressWarnings("unchecked")
	public List<CreditCardBrand> paymentInfo() {
		// Construct the request
		JSONObject request = new JSONObject();
		try {
			addStandardRequestFields(request, "payment_info");

			// Construct the body
			JSONObject body = new JSONObject();
			request.put("body", body);

			body.put("locale", getLocaleString());
			addCurrencyCode(body);
		}
		catch (JSONException e) {
			Log.e("Could not construct JSON payment info object.", e);
			return null;
		}

		return (List<CreditCardBrand>) doRequest(request, new PaymentInfoResponseHandler(), 0);
	}

	public BookingResponse reservation(SearchParams params, Property property, Rate rate, BillingInfo billingInfo) {
		// Construct the request
		JSONObject request = new JSONObject();
		try {
			addStandardRequestFields(request, "reservation");

			// Construct the body
			JSONObject body = new JSONObject();
			request.put("body", body);

			addBasicSearchParams(body, params);

			addCurrencyCode(body);

			// Add hotel information
			body.put("hotelId", property.getPropertyId());
			body.put("rateCode", rate.getRatePlanCode());
			body.put("roomTypeCode", rate.getRoomTypeCode());
			body.put("supplierType", property.getSupplierType());
			if (rate.getRateKey() != null) {
				body.put("rateKey", rate.getRateKey());
			}

			// Figure out what data to put into chargeableRate.  If we can't find one, 
			// throw an error
			Money chargeableRate = rate.getTotalAmountAfterTax();
			if (chargeableRate == null) {
				chargeableRate = rate.getDailyAmountBeforeTax();
				if (chargeableRate == null) {
					Log.e("Could not find a chargeable rate for booking.");
					return null;
				}
			}
			body.put("chargeableRate", chargeableRate.getAmount());

			// TODO: Handle itineraryId

			// Add room information
			JSONArray rooms = new JSONArray();
			body.put("rooms", rooms);
			JSONObject room = new JSONObject();
			rooms.put(room);

			room.put("firstName", billingInfo.getFirstName());
			room.put("lastName", billingInfo.getLastName());
			room.put("numberOfAdults", params.getNumAdults());
			room.put("numberOfChildren", params.getNumChildren());

			// Add billing info
			JSONObject billing = new JSONObject();
			body.put("billingInfo", billing);

			billing.put("firstName", billingInfo.getFirstName());
			billing.put("lastName", billingInfo.getLastName());
			billing.put("email", billingInfo.getEmail());
			billing.put("homePhone", billingInfo.getTelephone());

			JSONObject homeAddress = new JSONObject();
			billing.put("addressInfo", homeAddress);

			Location location = billingInfo.getLocation();
			List<String> streetAddress = location.getStreetAddress();
			for (int a = 0; a < streetAddress.size(); a++) {
				homeAddress.put("address" + (a + 1), streetAddress.get(a));
			}
			homeAddress.put("city", location.getCity());
			homeAddress.putOpt("stateProvinceCode", location.getStateCode());
			homeAddress.putOpt("postalCode", location.getPostalCode());
			homeAddress.put("countryCode", location.getCountryCode());

			JSONObject creditCardInfo = new JSONObject();
			billing.put("creditCardInfo", creditCardInfo);

			creditCardInfo.put("creditCardType", billingInfo.getBrandCode());
			creditCardInfo.put("creditCardNumber", billingInfo.getNumber());
			creditCardInfo.put("creditCardIdentifier", Integer.parseInt(billingInfo.getSecurityCode()));
			creditCardInfo.put("creditCardExpirationMonth", billingInfo.getExpirationDate().get(Calendar.MONTH) + 1);
			creditCardInfo.put("creditCardExpirationYear", billingInfo.getExpirationDate().get(Calendar.YEAR));

			// Email addresses
			JSONArray emails = new JSONArray();
			body.put("EmailItineraryAddresses", emails);
			emails.put(billingInfo.getEmail());

			body.put("locale", getLocaleString());
		}
		catch (JSONException e) {
			Log.e("Could not construct JSON reservation object.", e);
			return null;
		}

		return (BookingResponse) doRequest(request, new BookingResponseHandler(mContext), F_SECURE_REQUEST);
	}

	//////////////////////////////////////////////////////////////////////////
	//// Request building assistance methods

	private void addStandardRequestFields(JSONObject request, String type) throws JSONException {
		request.put("type", type);
		if (!AndroidUtils.isRelease(mContext)) {
			request.put("echoRequest", true);
		}
		request.put("cid", 345106);

		if (mSession != null && !mSession.hasExpired()) {
			Log.i("Applying sessionId: " + mSession.getSessionId());
			request.put("customerSessionId", mSession.getSessionId());
		}
	}

	private void addBasicSearchParams(JSONObject body, SearchParams params) throws JSONException {
		body.put("checkinDate", createDate(params.getCheckInDate()));
		body.put("checkoutDate", createDate(params.getCheckOutDate()));
	}

	private JSONObject createDate(Calendar cal) throws JSONException {
		JSONObject date = new JSONObject();
		date.put("year", cal.get(Calendar.YEAR));
		date.put("month", cal.get(Calendar.MONTH) + 1);
		date.put("dayOfMonth", cal.get(Calendar.DAY_OF_MONTH));
		return date;
	}

	private void addCurrencyCode(JSONObject body) throws JSONException {
		body.put("currencyCode", CurrencyUtils.getCurrencyCode(mContext));
	}

	private String getLocaleString() {
		return Locale.getDefault().toString();
	}

	//////////////////////////////////////////////////////////////////////////
	//// Request methods

	public Object doRequest(JSONObject request, ResponseHandler<?> responseHandler, int flags) {
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
		String userAgent = "ExpediaBookings/" + versionName + " Android";

		// Determine the target URL
		String serverUrl;
		boolean isRelease = AndroidUtils.isRelease(mContext);
		if ((flags & F_SECURE_REQUEST) != 0) {
			serverUrl = (isRelease) ? "https://hotelpal.mobiata.com/appsupport/ean_api/service"
					: "https://70.42.224.108/appsupport/ean_api/service";
		}
		else if ((flags & F_REVIEWS_URL) != 0) {
			serverUrl = (isRelease) ? "http://hotelpal.mobiata.com/appsupport/ExpediaTRS/service/reviews_by_page"
					: "http://70.42.224.108/appsupport/ExpediaTRS/service/reviews_by_page";
		}
		else {
			serverUrl = (isRelease) ? "http://hotelpal.mobiata.com/appsupport/ean_api/service"
					: "http://70.42.224.108/appsupport/ean_api/service";
		}

		// Configure the client
		AndroidHttpClient client = AndroidHttpClient.newInstance(userAgent, mContext);
		mPost = NetUtils.createHttpPost(serverUrl, request.toString());
		AndroidHttpClient.modifyRequestToAcceptGzipResponse(mPost);
		HttpParams httpParameters = client.getParams();
		HttpConnectionParams.setSoTimeout(httpParameters, 100000);

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
		Log.d("Sending request to " + serverUrl);
		Log.d("Request: " + request.toString());
		try {
			return client.execute(mPost, responseHandler);
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

	//////////////////////////////////////////////////////////////////////////
	//// Download listener stuff

	@Override
	public void onCancel() {
		Log.i("Cancelling download!");
		mCancellingDownload = true;
		if (mPost != null) {
			mPost.abort();
		}
	}

	//////////////////////////////////////////////////////////////////////////
	//// Legacy static API

	public static SearchResponse search(Context context, SearchResponse lastResponse) {
		ExpediaServices services = new ExpediaServices(context);
		return services.search(lastResponse);
	}

	public static SearchResponse search(Context context, SearchParams params, int sortType) {
		ExpediaServices services = new ExpediaServices(context);
		return services.search(params, sortType);
	}

	public static AvailabilityResponse availability(Context context, SearchParams params, Property property) {
		ExpediaServices services = new ExpediaServices(context);
		return services.availability(params, property);
	}

	public static List<CreditCardBrand> paymentInfo(Context context) {
		ExpediaServices services = new ExpediaServices(context);
		return services.paymentInfo();
	}

	public static BookingResponse reservation(Context context, SearchParams params, Property property, Rate rate,
			BillingInfo billingInfo) {
		ExpediaServices services = new ExpediaServices(context);
		return services.reservation(params, property, rate, billingInfo);
	}
}
