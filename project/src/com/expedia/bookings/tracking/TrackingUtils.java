package com.expedia.bookings.tracking;

import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.res.Configuration;
import android.location.Location;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.SparseArray;

import com.adobe.adms.measurement.ADMS_Measurement;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Filter;
import com.expedia.bookings.data.Filter.Sort;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.SearchParams.SearchType;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.pos.PointOfSale;
import com.mobiata.android.DebugUtils;
import com.mobiata.android.LocationServices;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.SettingUtils;

/**
 * Utilities for omniture tracking.  Should rarely (if ever) be called directly; instead, most calls
 * should be routed through Tracker.java (since most things we have to track for both the phone and
 * tablet versions of the UI).
 *
 */
public class TrackingUtils {

	/**
	 * Most tracking events are pretty simple and can be captured by these few fields.  This method handles
	 * both onClick and pageLoad events (depending on whether pageName is supplied).
	 *
	 * @param context the context
	 * @param pageName the page name if this is a pageLoad event; for onClick, this should be null
	 * @param events The "events" variable, if one needs to be set.  Can be null.
	 * @param shopperConfirmer Either "Shopper" or "Confirmer".  Typically should be "Shopper" (to indicate someone
	 *                         is shopping currently for hotels), can also be null.
	 * @param referrerId The "referrer" for an event.  Typically this is the name of the onClick event.
	 */
	public static void trackSimpleEvent(Context context, String pageName, String events, String shopperConfirmer,
			String referrerId) {
		ADMS_Measurement s = createSimpleEvent(context, pageName, events, shopperConfirmer, referrerId);

		// Handle the tracking different for pageLoads and onClicks.
		// If there is no pageName, it is an onClick (by default)
		if (pageName != null) {
			s.track();
		}
		else {
			trackOnClick(s);
		}
	}

	// Simplified method for tracking error pages
	public static void trackErrorPage(Context context, String errorName) {
		Log.d("Tracking \"App.Error." + errorName + "\" pageLoad.");
		trackSimpleEvent(context, "App.Error." + errorName, "event38", null, null);
	}

	public static void trackOnClick(ADMS_Measurement s) {
		s.trackLink(null, "o", s.getEvar(28), null, null);
	}

	public static ADMS_Measurement createSimpleEvent(Context context, String pageName, String events,
			String shopperConfirmer, String referrerId) {
		ADMS_Measurement s = OmnitureTracking.getFreshTrackingObject(context);

		addStandardFields(context, s);

		s.setAppState(pageName);

		if (events != null) {
			s.setEvents(events);
		}

		if (shopperConfirmer != null) {
			s.setEvar(25, shopperConfirmer);
			s.setProp(25, shopperConfirmer);
		}

		if (referrerId != null) {
			s.setEvar(25, referrerId);
			s.setProp(16, referrerId);
		}

		return s;
	}

	public static void addStandardFields(Context context, ADMS_Measurement s) {
		// Information gathering (before we run in and start setting variables)
		Calendar now = Calendar.getInstance();
		Date gmt = new Date(now.getTimeInMillis() - now.getTimeZone().getOffset(now.getTimeInMillis()));

		// Add debugging flag if not release
		if (!AndroidUtils.isRelease(context) || DebugUtils.isLogEnablerInstalled(context)) {
			s.setDebugLogging(true);
		}

		// Add offline tracking, so user doesn't have to be online to be tracked
		s.setOfflineTrackingEnabled(true);

		// account
		s.setReportSuiteIDs(getReportSuiteIds(context));

		// Amobee tracking

		s.setVisitorID(Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
		s.setEvar(7, Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
		s.setEvar(10, SettingUtils.get(context, context.getString(R.string.preference_amobee_marketing_date), ""));

		// Server
		s.setTrackingServer(getTrackingServer());
		s.setSSL(false);

		// Add the country locale
		s.setEvar(31, Locale.getDefault().getCountry());

		// Experience segmentation
		boolean usingTabletInterface = (ExpediaBookingApp.useTabletInterface(context));
		s.setEvar(50, (usingTabletInterface) ? "app.tablet.android" : "app.phone.android");

		// TPID
		s.setProp(7, Integer.toString(PointOfSale.getPointOfSale().getTpid()));

		// Unique device id
		s.setProp(12, Installation.id(context));

		// GMT timestamp
		s.setProp(32, gmt.getTime() + "");

		// Device carrier network info - format is "android|<carrier>|<network>"
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		s.setProp(33, "android|" + tm.getNetworkOperatorName() + "|" + getNetworkType(tm));

		// App version
		s.setProp(35, AndroidUtils.getAppVersion(context));

		// Language/locale
		s.setProp(37, Locale.getDefault().getLanguage());

		String email = null;

		// If the user is logged in, we want to send their email address along with request
		if (User.isLoggedIn(context)) {
			// Load the user into the Db if it has not been done (which will most likely be the case on app launch)
			if (Db.getUser() == null) {
				Db.loadUser(context);
			}
			if (Db.getUser() != null && Db.getUser().getPrimaryTraveler() != null) {
				email = Db.getUser().getPrimaryTraveler().getEmail();
			}
		}

		// If the email is still null, check against the BillingInfo in Db which is populated from manual forms
		if (TextUtils.isEmpty(email)) {
			if (Db.loadBillingInfo(context)) {
				if (Db.getBillingInfo() != null) {
					email = Db.getBillingInfo().getEmail();
				}
			}
		}

		if (!TextUtils.isEmpty(email)) {
			s.setProp(11, md5(email));
		}

		// Screen orientation
		Configuration config = context.getResources().getConfiguration();
		switch (config.orientation) {
		case Configuration.ORIENTATION_LANDSCAPE:
			s.setProp(39, "landscape");
			break;
		case Configuration.ORIENTATION_PORTRAIT:
			s.setProp(39, "portrait");
			break;
		case Configuration.ORIENTATION_SQUARE:
			s.setProp(39, "square");
			break;
		case Configuration.ORIENTATION_UNDEFINED:
			s.setProp(39, "undefined");
			break;
		}

		// User location
		Location bestLastLocation = LocationServices.getLastBestLocation(context, 0);
		if (bestLastLocation != null) {
			s.setProp(40, bestLastLocation.getLatitude() + "," + bestLastLocation.getLongitude() + "|"
					+ bestLastLocation.getAccuracy());
		}
	}

	// The "products" field uses this format:
	// Hotel;<supplier> Hotel:<hotel id>
	public static void addProducts(ADMS_Measurement s, Property property) {
		// Determine supplier type
		String supplierType = property.getSupplierType();
		s.setProducts("Hotel;" + supplierType + " Hotel:" + property.getPropertyId());
	}

	public static void addProducts(ADMS_Measurement s, Property property, int numNights, double totalCost) {
		addProducts(s, property);

		DecimalFormat df = new DecimalFormat("#.##");
		String products = s.getProducts();
		products += ";" + numNights + ";" + df.format(totalCost);
		s.setProducts(products);
	}

	public static void addHotelRating(ADMS_Measurement s, Property property) {
		s.setProp(38, property.getAverageExpediaRating() + "");
	}

	public static String getReportSuiteIds(Context context) {
		String id;
		boolean usingTabletInterface = (ExpediaBookingApp.useTabletInterface(context));
		if (AndroidUtils.isRelease(context)) {
			id = (usingTabletInterface) ? "expedia1tabletandroid" : "expedia1androidcom";
			id += ",expediaglobalapp";
		}
		else {
			id = (usingTabletInterface) ? "expedia1tabletandroiddev" : "expedia1androidcomdev";
			id += ",expediaglobalappdev";
		}
		return id;
	}

	public static String getTrackingServer() {
		return "om.expedia.com";
	}

	private static SparseArray<String> mNetworkTypes = null;

	// This method is complicated by the fact that new network types were added to later versions
	// of the Android OS.  As a result, we just check the hardcoded values in some cases.
	private static String getNetworkType(TelephonyManager tm) {
		if (mNetworkTypes == null) {
			SparseArray<String> types = new SparseArray<String>();

			try {
				Field[] fields = tm.getClass().getFields();
				for (Field field : fields) {
					String fieldName = field.getName();
					if (fieldName.equals("NETWORK_TYPE_1xRTT")) {
						types.put(field.getInt(null), "1xrtt");
					}
					else if (fieldName.equals("NETWORK_TYPE_CDMA")) {
						types.put(field.getInt(null), "cdma");
					}
					else if (fieldName.equals("NETWORK_TYPE_EDGE")) {
						types.put(field.getInt(null), "edge");
					}
					else if (fieldName.equals("NETWORK_TYPE_EHRPD")) {
						types.put(field.getInt(null), "ehrpd");
					}
					else if (fieldName.equals("NETWORK_TYPE_EVDO_0")) {
						types.put(field.getInt(null), "evdo_0");
					}
					else if (fieldName.equals("NETWORK_TYPE_EVDO_A")) {
						types.put(field.getInt(null), "evdo_a");
					}
					else if (fieldName.equals("NETWORK_TYPE_EVDO_B")) {
						types.put(field.getInt(null), "evdo_b");
					}
					else if (fieldName.equals("NETWORK_TYPE_GPRS")) {
						types.put(field.getInt(null), "gprs");
					}
					else if (fieldName.equals("NETWORK_TYPE_HSDPA")) {
						types.put(field.getInt(null), "hsdpa");
					}
					else if (fieldName.equals("NETWORK_TYPE_HSPA")) {
						types.put(field.getInt(null), "hspa");
					}
					else if (fieldName.equals("NETWORK_TYPE_HSPAP")) {
						types.put(field.getInt(null), "hspap");
					}
					else if (fieldName.equals("NETWORK_TYPE_HSUPA")) {
						types.put(field.getInt(null), "hsupa");
					}
					else if (fieldName.equals("NETWORK_TYPE_IDEN")) {
						types.put(field.getInt(null), "iden");
					}
					else if (fieldName.equals("NETWORK_TYPE_LTE")) {
						types.put(field.getInt(null), "lte");
					}
					else if (fieldName.equals("NETWORK_TYPE_UMTS")) {
						types.put(field.getInt(null), "umts");
					}
					else if (fieldName.equals("NETWORK_TYPE_UNKNOWN")) {
						types.put(field.getInt(null), "NA");
					}
				}

				mNetworkTypes = types;
			}
			catch (IllegalAccessException e) {
				Log.w("Could not load network types.", e);
			}
		}

		int networkType = tm.getNetworkType();
		if (mNetworkTypes != null && mNetworkTypes.get(networkType) != null) {
			return mNetworkTypes.get(networkType);
		}

		return "NA";
	}

	private static String md5(String s) {
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();

			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++) {
				hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
			}
			return hexString.toString();
		}
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static String getRefinements(SearchParams searchParams, SearchParams oldSearchParams, Filter filter,
			Filter oldFilter) {
		if (oldFilter != null && oldSearchParams != null) {
			List<String> refinements = new ArrayList<String>();

			// Sort change
			if (oldFilter.getSort() != filter.getSort()) {
				Sort sort = filter.getSort();
				if (sort == Sort.POPULAR) {
					refinements.add("App.Hotels.Search.Sort.Popular");
				}
				else if (sort == Sort.PRICE) {
					refinements.add("App.Hotels.Search.Sort.Price");
				}
				else if (sort == Sort.DISTANCE) {
					refinements.add("App.Hotels.Search.Sort.Distance");
				}
				else if (sort == Sort.RATING) {
					refinements.add("App.Hotels.Search.Sort.Rating");
				}
			}

			// Number of travelers change
			if (searchParams.getNumAdults() != oldSearchParams.getNumAdults()
					|| searchParams.getNumChildren() != oldSearchParams.getNumChildren()) {
				refinements.add("App.Hotels.Search.Refine.NumberTravelers");
			}

			// Location change
			// Checks that the search type is the same, or else that a search of a particular type hasn't
			// been modified (e.g., freeform text changing on a freeform search)
			if (!searchParams.equals(oldSearchParams.getSearchType())) {
				refinements.add("App.Hotels.Search.Refine.Location");
			}
			else if (searchParams.getSearchType() == SearchType.MY_LOCATION
					|| searchParams.getSearchType() == SearchType.VISIBLE_MAP_AREA) {
				if (searchParams.getSearchLatitude() != oldSearchParams.getSearchLatitude()
						|| searchParams.getSearchLongitude() != oldSearchParams.getSearchLongitude()) {
					refinements.add("App.Hotels.Search.Refine.Location");
				}
			}
			else {
				if (!searchParams.getQuery().equals(oldSearchParams.getQuery())) {
					refinements.add("App.Hotels.Search.Refine.Location");
				}
			}

			// Checkin date change
			if (!searchParams.getCheckInDate().equals(oldSearchParams.getCheckInDate())) {
				refinements.add("App.Hotels.Search.Refine.CheckinDate");
			}

			// Checkout date change
			if (!searchParams.getCheckOutDate().equals(oldSearchParams.getCheckOutDate())) {
				refinements.add("App.Hotels.Search.Refine.CheckoutDate");
			}

			// Search radius change
			if (filter.getSearchRadius() != oldFilter.getSearchRadius()) {
				refinements.add("App.Hotels.Search.Refine.SearchRadius");
			}

			// Price range change
			if (filter.getPriceRange() != oldFilter.getPriceRange()) {
				refinements.add("App.Hotels.Search.Refine.PriceRange");
			}

			// Star rating change
			double minStarRating = filter.getMinimumStarRating();
			if (minStarRating != oldFilter.getMinimumStarRating()) {
				if (minStarRating == 5) {
					refinements.add("App.Hotels.Search.Refine.AllStars");
				}
				else {
					refinements.add("App.Hotels.Search.Refine." + minStarRating + "Stars");
				}
			}

			boolean hasHotelFilter = filter.getHotelName() != null;
			boolean oldHasHotelFilter = oldFilter.getHotelName() != null;
			if (hasHotelFilter != oldHasHotelFilter
					|| (hasHotelFilter && !filter.getHotelName().equals(oldFilter.getHotelName()))) {
				refinements.add("App.Hotels.Search.Refine.Name");
			}

			int numRefinements = refinements.size();
			if (numRefinements == 0) {
				return null;
			}

			StringBuilder sb = new StringBuilder();
			for (int a = 0; a < numRefinements; a++) {
				if (a != 0) {
					sb.append("|");
				}
				sb.append(refinements.get(a));
			}

			return sb.toString();
		}

		return null;
	}
}
