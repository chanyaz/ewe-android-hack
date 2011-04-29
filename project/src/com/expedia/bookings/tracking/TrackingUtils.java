package com.expedia.bookings.tracking;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.telephony.TelephonyManager;

import com.mobiata.android.util.AndroidUtils;
import com.mobiata.hotellib.data.BillingInfo;
import com.mobiata.hotellib.data.Property;
import com.omniture.AppMeasurement;

public class TrackingUtils {

	// Most tracking events are pretty simple and can be captured by these few fields.  Just enter them
	// and we'll handle the rest
	public static void trackSimpleEvent(Context context, String pageName, String events, String shopperConfirmer,
			String referrerId) {
		AppMeasurement s = new AppMeasurement((Application) context.getApplicationContext());

		addStandardFields(context, s);

		if (events != null) {
			s.events = events;
		}

		if (shopperConfirmer != null) {
			s.eVar25 = s.prop25 = shopperConfirmer;
		}

		if (referrerId != null) {
			s.eVar28 = s.prop16 = referrerId;
		}

		// Handle the tracking different for pageLoads and onClicks.
		// If there is no pageName, it is an onClick (by default)
		if (pageName != null) {
			s.pageName = pageName;
			s.track();
		}
		else {
			trackOnClick(s);
		}
	}

	public static void trackOnClick(AppMeasurement s) {
		s.trackLink(null, "o", s.eVar28);
	}

	public static void addStandardFields(Context context, AppMeasurement s) {
		// Information gathering (before we run in and start setting variables)
		Calendar now = Calendar.getInstance();
		Date gmt = new Date(now.getTimeInMillis() - now.getTimeZone().getOffset(now.getTimeInMillis()));

		// Add debugging flag if not release
		if (!AndroidUtils.isRelease(context)) {
			s.debugTracking = true;
		}

		// account
		s.account = (AndroidUtils.isRelease(context)) ? "expedia1androidcom" : "expedia1androidcomdev";

		// Server
		// TODO: Not used currently, but might be if we have multiple servers someday

		// Time parting
		// Format is: YY:DayOfYear:Interval Size:Interval Num
		// Interval size == 60 minutes
		DateFormat df = new SimpleDateFormat("yy:DDD:60:HH");
		s.eVar49 = df.format(gmt);

		// Experience segmentation
		s.eVar50 = "app.android";

		// hashed email
		BillingInfo billingInfo = new BillingInfo();
		if (billingInfo.load(context)) {
			s.prop11 = md5(billingInfo.getEmail());
		}

		// Unique device id
		// TODO: Add this once we've figured out what we want the "unique identifier" to be.

		// GMT timestamp
		s.prop32 = gmt.getTime() + "";

		// Device carrier network
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		s.prop33 = tm.getNetworkOperatorName();

		// App version
		s.prop35 = AndroidUtils.getAppVersion(context);

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
	}

	// The "products" field uses this format:
	// Hotel;<supplier> Hotel:<hotel id>
	public static void addProducts(AppMeasurement s, Property property) {
		// Determine supplier type
		String supplierType = property.getSupplierType();
		String supplier = null;
		if (supplierType.equals("E")) {
			supplier = "Merchant";
		}
		else if (supplierType.equals("S")) {
			supplier = "Sabre";
		}
		else if (supplierType.equals("W")) {
			supplier = "Worldspan";
		}
		else {
			supplier = "Unknown";
		}

		s.products = "Hotel; " + supplier + " Hotel:" + property.getPropertyId();
	}

	public static void addHotelRating(AppMeasurement s, Property property) {
		s.prop38 = property.getTripAdvisorRating() + "";
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
}
