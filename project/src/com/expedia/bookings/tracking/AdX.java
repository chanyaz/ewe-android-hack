package com.expedia.bookings.tracking;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.AdX.tag.AdXConnect;
import com.expedia.bookings.data.pos.PointOfSale;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;

public class AdX {
	private static Context sAppContext;
	private static boolean sEnabled;
	private static boolean sConnected;
	private static int sLogLevel;

	public static void initialize(Context context, boolean enabled) {
		sConnected = false;
		sAppContext = context.getApplicationContext();
		sEnabled = enabled;
		if (AndroidUtils.isRelease(sAppContext)) {
			sLogLevel = 0;
		}
		else {
			sLogLevel = 5;
		}

		Log.i("AdX tracking initialized (enabled: " + String.valueOf(enabled) + ")");
	}

	private static void connect(String pos, boolean launchedAgain) {
		if (!sConnected) {
			AdXConnect.getAdXConnectInstance(sAppContext, launchedAgain, sLogLevel);
			sConnected = true;
		}
	}

	public static void trackFirstLaunch() {
		if (sEnabled) {
			String pos = PointOfSale.getPointOfSale().getTwoLetterCountryCode();
			connect(pos, false);
			AdXConnect.getAdXConnectEventInstance(sAppContext, "FirstLaunch", "", "");
			Log.i("AdX first launch event PointOfSale=" + pos);

			reportReferralToOmniture();
		}
	}

	public static void trackLaunch() {
		if (sEnabled) {
			String pos = PointOfSale.getPointOfSale().getTwoLetterCountryCode();
			connect(pos, true);
			AdXConnect.getAdXConnectEventInstance(sAppContext, "Launch", "", "");
			Log.i("AdX launch event PointOfSale=" + pos);
		}
	}

	public static void trackDeepLinkLaunch(Uri data) {
		if (sEnabled) {
			String adxid = data.getQueryParameter("ADXID");
			if (adxid != null && adxid.length() > 0) {
				AdXConnect.getAdXConnectEventInstance(sAppContext, "DeepLinkLaunch", adxid, "");
				Log.i("AdX deep link launch, Ad-X ID=" + adxid);
			}
		}
	}

	public static void trackLogin() {
		if (sEnabled) {
			AdXConnect.getAdXConnectEventInstance(sAppContext, "Login", "", "");
			Log.i("AdX login event");
		}
	}

	public static void trackViewItinList() {
		if (sEnabled) {
			AdXConnect.getAdXConnectEventInstance(sAppContext, "Itinerary", "", "");
			Log.i("AdX Itinerary event");
		}
	}

	public static void trackHotelBooked(String currency, double totalPrice) {
		if (sEnabled) {
			AdXConnect.getAdXConnectEventInstance(sAppContext, "Sale", String.valueOf(totalPrice), currency, "Hotel");
			Log.i("AdX hotel booking event currency=" + currency + " total=" + totalPrice);
		}
	}

	public static void trackFlightBooked(String currency, double totalPrice) {
		if (sEnabled) {
			AdXConnect.getAdXConnectEventInstance(sAppContext, "Sale", String.valueOf(totalPrice), currency, "Flight");
			Log.i("AdX flight booking event currency=" + currency + " total=" + totalPrice);
		}
	}

	public static void trackHotelCheckoutStarted(String currency, double totalPrice) {
		if (sEnabled) {
			AdXConnect.getAdXConnectEventInstance(sAppContext, "Checkout", String.valueOf(totalPrice), currency, "Hotel");
			Log.i("AdX hotel checkout started currency=" + currency + " total=" + totalPrice);
		}
	}

	public static void trackFlightCheckoutStarted(String currency, double totalPrice) {
		if (sEnabled) {
			AdXConnect.getAdXConnectEventInstance(sAppContext, "Checkout", String.valueOf(totalPrice), currency, "Flight");
			Log.i("AdX flight checkout started currency=" + currency + " total=" + totalPrice);
		}
	}

	public static void trackHotelSearch(String regionId) {
		if (sEnabled) {
			AdXConnect.getAdXConnectEventInstance(sAppContext, "Search", "", regionId, "Hotel");
			Log.i("AdX hotel search regionId=" + regionId);
		}
	}

	public static void trackFlightSearch(String destinationAirport) {
		if (sEnabled) {
			AdXConnect.getAdXConnectEventInstance(sAppContext, "Search", "", destinationAirport, "Flight");
			Log.i("AdX flight search destination=" + destinationAirport);
		}
	}

	private static void reportReferralToOmniture() {
		// getAdXReferral is blocking, run off the UI thread
		new Thread(new Runnable() {
			@Override
			public void run() {
				// Was told by the AdX guys to just hold off for a bit before
				// calling getAdXReferral()
				try {
					Thread.sleep(15 * DateUtils.SECOND_IN_MILLIS);
				}
				catch (Exception e) {
					// Should not ever happen
				}

				String referral = AdXConnect.getAdXReferral(sAppContext, 15);
				if (TextUtils.isEmpty(referral)) {
					Log.w("Unable to retrieve AdX referral string");
				}
				else {
					Log.d("Got AdX referral string: " + referral);
					OmnitureTracking.trackAdXReferralLink(sAppContext, referral);
				}
			}
		}).start();
	}
}
