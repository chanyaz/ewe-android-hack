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

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.Location;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.SparseArray;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Filter;
import com.expedia.bookings.data.Filter.Sort;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.SearchParams.SearchType;
import com.expedia.bookings.data.pos.PointOfSaleInfo;
import com.expedia.bookings.utils.LocaleUtils;
import com.mobiata.android.DebugUtils;
import com.mobiata.android.LocationServices;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.SettingUtils;
import com.omniture.AppMeasurement;

/**
 * Utilities for omniture tracking.  Should rarely (if ever) be called directly; instead, most calls
 * should be routed through Tracker.java (since most things we have to track for both the phone and
 * tablet versions of the UI).
 * 
 */
public class TrackingUtils {

	private static final String EMAIL_HASH_KEY = "email_hash";
	private static final String NO_EMAIL = "NO EMAIL PROVIDED";

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
		AppMeasurement s = createSimpleEvent(context, pageName, events, shopperConfirmer, referrerId);

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

	public static void trackOnClick(AppMeasurement s) {
		s.trackLink(null, "o", s.eVar28);
	}

	public static AppMeasurement createSimpleEvent(Context context, String pageName, String events,
			String shopperConfirmer, String referrerId) {
		AppMeasurement s = new AppMeasurement((Application) context.getApplicationContext());

		addStandardFields(context, s);

		s.pageName = pageName;

		if (events != null) {
			s.events = events;
		}

		if (shopperConfirmer != null) {
			s.eVar25 = s.prop25 = shopperConfirmer;
		}

		if (referrerId != null) {
			s.eVar28 = s.prop16 = referrerId;
		}

		return s;
	}

	public static void addStandardFields(Context context, AppMeasurement s) {
		// Information gathering (before we run in and start setting variables)
		Calendar now = Calendar.getInstance();
		Date gmt = new Date(now.getTimeInMillis() - now.getTimeZone().getOffset(now.getTimeInMillis()));

		// Add debugging flag if not release
		if (!AndroidUtils.isRelease(context) || DebugUtils.isLogEnablerInstalled(context)) {
			s.debugTracking = true;
		}

		// Add offline tracking, so user doesn't have to be online to be tracked
		s.trackOffline = true;

		// account
		boolean usingTabletInterface = (ExpediaBookingApp.useTabletInterface(context));
		if (AndroidUtils.isRelease(context)) {
			s.account = (usingTabletInterface) ? "expedia1tabletandroid" : "expedia1androidcom";
			s.account += ",expediaglobalapp";
		}
		else {
			s.account = (usingTabletInterface) ? "expedia1tabletandroiddev" : "expedia1androidcomdev";
			s.account += ",expediaglobalappdev";
		}

		// Amobee tracking

		s.visitorID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
		s.eVar7 = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
		s.eVar10 = SettingUtils.get(context, context.getString(R.string.preference_amobee_marketing_date), "");

		// Server
		s.trackingServer = "om.expedia.com";
		s.trackingServerSecure = "oms.expedia.com";

		// Add the country locale
		s.eVar31 = Locale.getDefault().getCountry();

		// Experience segmentation
		s.eVar50 = (usingTabletInterface) ? "app.tablet.android" : "app.phone.android";

		// TPID
		s.prop7 = Integer.toString(PointOfSaleInfo.getPointOfSaleInfo().getTpid());

		// hashed email
		// Normally we store this in a setting; in 1.0 we stored this in BillingInfo, but
		// that's really slow to retrieve so we no longer do that.
		String emailHashed = null;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		if (!prefs.contains(EMAIL_HASH_KEY)) {
			BillingInfo billingInfo = new BillingInfo();
			if (billingInfo.load(context)) {
				saveEmailForTracking(context, billingInfo.getEmail());
			}
		}
		else {
			emailHashed = prefs.getString(EMAIL_HASH_KEY, null);
		}
		if (emailHashed != null && !emailHashed.equals(NO_EMAIL)) {
			s.prop11 = emailHashed;
		}

		// Unique device id
		s.prop12 = Installation.id(context);

		// GMT timestamp
		s.prop32 = gmt.getTime() + "";

		// Device carrier network info - format is "android|<carrier>|<network>"
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		s.prop33 = "android|" + tm.getNetworkOperatorName() + "|" + getNetworkType(tm);

		// App version
		s.prop35 = AndroidUtils.getAppVersion(context);

		// Language/locale
		s.prop37 = Locale.getDefault().getLanguage();

		// Screen orientation
		Configuration config = context.getResources().getConfiguration();
		switch (config.orientation) {
		case Configuration.ORIENTATION_LANDSCAPE:
			s.prop39 = "landscape";
			break;
		case Configuration.ORIENTATION_PORTRAIT:
			s.prop39 = "portrait";
			break;
		case Configuration.ORIENTATION_SQUARE:
			s.prop39 = "square";
			break;
		case Configuration.ORIENTATION_UNDEFINED:
			s.prop39 = "undefined";
			break;
		}

		// User location
		Location bestLastLocation = LocationServices.getLastBestLocation(context, 0);
		if (bestLastLocation != null) {
			s.prop40 = bestLastLocation.getLatitude() + "," + bestLastLocation.getLongitude() + "|"
					+ bestLastLocation.getAccuracy();
		}
	}

	public static void saveEmailForTracking(Context context, String email) {
		if (email != null && email.length() > 0) {
			SettingUtils.save(context, EMAIL_HASH_KEY, md5(email));
		}
		else {
			SettingUtils.save(context, EMAIL_HASH_KEY, NO_EMAIL);
		}
	}

	// The "products" field uses this format:
	// Hotel;<supplier> Hotel:<hotel id>
	public static void addProducts(AppMeasurement s, Property property) {
		// Determine supplier type
		String supplierType = property.getSupplierType();
		s.products = "Hotel;" + supplierType + " Hotel:" + property.getPropertyId();
	}

	public static void addProducts(AppMeasurement s, Property property, int numNights, double totalCost) {
		addProducts(s, property);

		DecimalFormat df = new DecimalFormat("#.##");
		s.products += ";" + numNights + ";" + df.format(totalCost);
	}

	public static void addHotelRating(AppMeasurement s, Property property) {
		s.prop38 = property.getAverageExpediaRating() + "";
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
			for (int i = 0; i < messageDigest.length; i++)
				hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
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
