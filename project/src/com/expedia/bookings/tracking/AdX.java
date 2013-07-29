package com.expedia.bookings.tracking;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.AdX.tag.AdXConnect;
import com.expedia.bookings.data.pos.PointOfSale;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;

public class AdX {
	private static Context mContext;
	private static boolean mEnabled;
	private static boolean mConnected;
	private static int mLogLevel;

	public static void initialize(Context context, boolean enabled) {
		mConnected = false;
		mContext = context;
		mEnabled = enabled;
		if (AndroidUtils.isRelease(mContext)) {
			mLogLevel = 0;
		}
		else {
			mLogLevel = 5;
		}

		Log.i("AdX tracking initialized (enabled: " + String.valueOf(enabled) + ")");
	}

	private static void connect(String pos, boolean launchedAgain) {
		if (!mConnected) {
			AdXConnect.getAdXConnectInstance(mContext, launchedAgain, mLogLevel, pos);
			mConnected = true;
		}
	}

	public static void trackFirstLaunch() {
		if (mEnabled) {
			String pos = PointOfSale.getPointOfSale(mContext).getTwoLetterCountryCode();
			connect(pos, false);
			AdXConnect.getAdXConnectEventInstance(mContext, "FirstLaunch", "", "");
			Log.i("AdX first launch event PointOfSale=" + pos);

			reportReferralToOmniture();
		}
	}

	public static void trackLaunch() {
		if (mEnabled) {
			String pos = PointOfSale.getPointOfSale(mContext).getTwoLetterCountryCode();
			connect(pos, true);
			AdXConnect.getAdXConnectEventInstance(mContext, "Launch", "", "");
			Log.i("AdX launch event PointOfSale=" + pos);
		}
	}

	public static void trackLogin() {
		if (mEnabled) {
			AdXConnect.getAdXConnectEventInstance(mContext, "Login", "", "");
			Log.i("AdX login event");
		}
	}

	public static void trackViewItinList() {
		if (mEnabled) {
			AdXConnect.getAdXConnectEventInstance(mContext, "Itinerary", "", "");
			Log.i("AdX Itinerary event");
		}
	}

	public static void trackHotelBooked(String currency, double totalPrice) {
		if (mEnabled) {
			AdXConnect.getAdXConnectEventInstance(mContext, "Sale", String.valueOf(totalPrice), currency, "Hotel");
			Log.i("AdX hotel booking event currency=" + currency + " total=" + totalPrice);
		}
	}

	public static void trackFlightBooked(String currency, double totalPrice) {
		if (mEnabled) {
			AdXConnect.getAdXConnectEventInstance(mContext, "Sale", String.valueOf(totalPrice), currency, "Flight");
			Log.i("AdX flight booking event currency=" + currency + " total=" + totalPrice);
		}
	}

	public static void trackHotelCheckoutStarted(String currency, double totalPrice) {
		if (mEnabled) {
			AdXConnect.getAdXConnectEventInstance(mContext, "Checkout", String.valueOf(totalPrice), currency, "Hotel");
			Log.i("AdX hotel checkout started currency=" + currency + " total=" + totalPrice);
		}
	}

	public static void trackFlightCheckoutStarted(String currency, double totalPrice) {
		if (mEnabled) {
			AdXConnect.getAdXConnectEventInstance(mContext, "Checkout", String.valueOf(totalPrice), currency, "Flight");
			Log.i("AdX flight checkout started currency=" + currency + " total=" + totalPrice);
		}
	}

	public static void trackHotelSearch(String regionId) {
		if (mEnabled) {
			AdXConnect.getAdXConnectEventInstance(mContext, "Search", "", regionId, "Hotel");
			Log.i("AdX hotel search regionId=" + regionId);
		}
	}

	public static void trackFlightSearch(String destinationAirport) {
		if (mEnabled) {
			AdXConnect.getAdXConnectEventInstance(mContext, "Search", "", destinationAirport, "Flight");
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

				String referral = AdXConnect.getAdXReferral(mContext, 15);
				if (TextUtils.isEmpty(referral)) {
					Log.w("Unable to retrieve AdX referral string");
				}
				else {
					Log.d("Got AdX referral string: " + referral);
					OmnitureTracking.trackAdXReferralLink(mContext, referral);
				}
			}
		}).start();
	}
}
