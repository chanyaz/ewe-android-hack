package com.expedia.bookings.utils;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ActivityKillReceiver;
import com.expedia.bookings.activity.CarActivity;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.activity.FlightSearchActivity;
import com.expedia.bookings.activity.FlightSearchResultsActivity;
import com.expedia.bookings.activity.FlightUnsupportedPOSActivity;
import com.expedia.bookings.activity.HotelBookingActivity;
import com.expedia.bookings.activity.HotelSearchActivity;
import com.expedia.bookings.activity.ItineraryActivity;
import com.expedia.bookings.activity.LXBaseActivity;
import com.expedia.bookings.activity.PhoneLaunchActivity;
import com.expedia.bookings.activity.TabletCheckoutActivity;
import com.expedia.bookings.activity.TabletLaunchActivity;
import com.expedia.bookings.activity.TabletResultsActivity;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelFilter;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.Sp;
import com.expedia.bookings.data.cars.CarSearchParams;
import com.expedia.bookings.data.lx.LXSearchParams;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.fragment.HotelBookingFragment;
import com.expedia.bookings.services.CarServices;
import com.google.gson.Gson;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.Log;

/**
 * Utilities for navigating the app (between Activities)
 *
 */
public class NavUtils {

	public static final int FLAG_DEEPLINK = 1;
	public static final int FLAG_OPEN_SEARCH = 2;
	public static final int FLAG_OPEN_RESULTS = 3;

	public static boolean canHandleIntent(Context context, Intent intent) {
		return intent.resolveActivity(context.getPackageManager()) != null;
	}

	public static void startActivity(Context context, Intent intent, Bundle options) {
		context.startActivity(intent, options);
	}

	public static boolean startActivitySafe(Context context, Intent intent) {
		if (canHandleIntent(context, intent)) {
			context.startActivity(intent);
			return true;
		}
		else {
			// Future thought: Should we be showing a toast at all and let app handle it?
			Toast.makeText(context, R.string.app_not_available, Toast.LENGTH_LONG).show();
			return false;
		}
	}

	public static void goToLaunchScreen(Context context) {
		goToLaunchScreen(context, false);
	}

	public static void goToLaunchScreen(Context context, boolean forceShowWaterfall) {
		if (ExpediaBookingApp.useTabletInterface(context)) {
			Intent intent = new Intent(context, TabletLaunchActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		}
		else {
			Intent intent = new Intent(context, PhoneLaunchActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

			if (forceShowWaterfall) {
				intent.putExtra(PhoneLaunchActivity.ARG_FORCE_SHOW_WATERFALL, true);
			}

			sendKillActivityBroadcast(context);
			context.startActivity(intent);
		}
	}

	public static void goToTabletResults(Context context, SearchParams searchParams, LineOfBusiness lob) {
		Sp.setParams(searchParams, false);

		// Reset HotelFilter
		if (Db.getFilter() != null) {
			HotelFilter filter = Db.getFilter();
			filter.reset();
			filter.notifyFilterChanged();
		}

		Intent intent;
		TaskStackBuilder builder = TaskStackBuilder.create(context);
		intent = new Intent(context, TabletLaunchActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		builder.addNextIntent(intent);

		intent = TabletResultsActivity.createIntent(context);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		if (lob == LineOfBusiness.HOTELS) {
			intent.putExtra(TabletResultsActivity.INTENT_EXTRA_DEEP_LINK_HOTEL_STATE, true);
		}
		builder.addNextIntent(intent);
		builder.startActivities();
	}

	public static void goToItin(Context context) {
		Intent intent;
		if (ExpediaBookingApp.useTabletInterface(context)) {
			TaskStackBuilder builder = TaskStackBuilder.create(context);
			intent = new Intent(context, TabletLaunchActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			builder.addNextIntent(intent);

			intent = new Intent(context, ItineraryActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			builder.addNextIntent(intent);
			builder.startActivities();
		}
		else {
			intent = new Intent(context, PhoneLaunchActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			intent.putExtra(PhoneLaunchActivity.ARG_FORCE_SHOW_ITIN, true);
			context.startActivity(intent);
		}
	}

	public static void goToHotels(Context context, HotelSearchParams params) {
		goToHotels(context, params, null, 0);
	}

	public static void goToHotels(Context context, Bundle animOptions) {
		goToHotels(context, null, animOptions, 0);
	}

	public static void goToHotels(Context context, HotelSearchParams params, Bundle animOptions, int flags) {
		sendKillActivityBroadcast(context);

		Intent intent = new Intent();
		Class<? extends Activity> routingTarget;

		// Update the Db object to have our search params (which will be used by hotels search)

		if (params != null) {
			Db.getHotelSearch().setSearchParams(params);

			// Only used by phone search currently, but won't harm to put on tablet as well
			intent.putExtra(Codes.TAG_EXTERNAL_SEARCH_PARAMS, true);
		}

		if ((flags & FLAG_DEEPLINK) != 0) {
			intent.putExtra(Codes.FROM_DEEPLINK, true);
		}

		if ((flags & FLAG_OPEN_SEARCH) != 0) {
			intent.putExtra(Codes.EXTRA_OPEN_SEARCH, true);
		}

		// 13820: Check if a booking is in process at this moment (in case BookingInfoActivity died)
		if (BackgroundDownloader.getInstance().isDownloading(HotelBookingFragment.KEY_DOWNLOAD_BOOKING)) {
			routingTarget = HotelBookingActivity.class;
		}
		else {
			routingTarget = HotelSearchActivity.class;
		}

		// Launch activity based on routing selection
		intent.setClass(context, routingTarget);
		startActivity(context, intent, animOptions);
	}

	public static void goToActivities(Context context, Bundle animOptions) {
		sendKillActivityBroadcast(context);
		Intent intent = new Intent(context, LXBaseActivity.class);
		startActivity(context, intent, animOptions);
	}

	public static void goToFlights(Context context) {
		goToFlights(context, false, null);
	}

	public static void goToFlights(Context context, boolean usePresetSearchParams) {
		goToFlights(context, usePresetSearchParams, null);
	}

	public static void goToFlights(Context context, Bundle animOptions) {
		goToFlights(context, false, animOptions);
	}

	public static void goToFlightsUsingSearchParams(Context context) {
		goToFlights(context, true, null);
	}

	public static void goToFlights(Context context, boolean usePresetSearchParams, Bundle animOptions) {
		goToFlights(context, usePresetSearchParams, animOptions, 0);
	}

	public static void goToFlights(Context context, boolean usePresetSearchParams, Bundle animOptions, int flags) {
		if (!PointOfSale.getPointOfSale().supportsFlights()) {
			// Because the user can't actually navigate forward from here, perhaps it makes sense to preserve the
			// backstack so as not to add insult to injury (can't access Flights, lost activity backstack)
			Intent intent = new Intent(context, FlightUnsupportedPOSActivity.class);
			intent.addFlags(flags);
			startActivity(context, intent, animOptions);
		}
		else {
			sendKillActivityBroadcast(context);
			Intent intent = new Intent(context, FlightSearchActivity.class);
			intent.addFlags(flags);
			if (usePresetSearchParams) {
				intent.putExtra(FlightSearchActivity.ARG_USE_PRESET_SEARCH_PARAMS, true);
			}
			startActivity(context, intent, animOptions);
		}
	}

	public static void goToCars(Context context, Bundle animOptions) {
		sendKillActivityBroadcast(context);
		Intent intent = new Intent(context, CarActivity.class);
		startActivity(context, intent, animOptions);
	}

	public static void goToCars(Context context, Bundle animOptions, CarSearchParams searchParams, String productKey, int flags) {
		sendKillActivityBroadcast(context);
		Intent intent = new Intent(context, CarActivity.class);
		if (searchParams != null) {
			Gson gson = CarServices.generateGson();
			intent.putExtra(Codes.TAG_EXTERNAL_SEARCH_PARAMS, gson.toJson(searchParams));
		}

		if ((flags & FLAG_DEEPLINK) != 0) {
			intent.putExtra(Codes.FROM_DEEPLINK, true);
		}

		intent.putExtra(Codes.CARS_PRODUCT_KEY, productKey);
		startActivity(context, intent, animOptions);
	}

	public static void goToCars(Context context, Bundle animOptions, CarSearchParams searchParams, int flags) {
		sendKillActivityBroadcast(context);
		Intent intent = new Intent(context, CarActivity.class);
		if (searchParams != null) {
			Gson gson = CarServices.generateGson();
			intent.putExtra(Codes.TAG_EXTERNAL_SEARCH_PARAMS, gson.toJson(searchParams));
		}

		if ((flags & FLAG_OPEN_SEARCH) != 0) {
			intent.putExtra(Codes.EXTRA_OPEN_SEARCH, true);
		}

		if ((flags & FLAG_DEEPLINK) != 0) {
			intent.putExtra(Codes.FROM_DEEPLINK, true);
		}

		startActivity(context, intent, animOptions);
	}

	public static void goToActivities(Context context, Bundle animOptions, LXSearchParams searchParams, int flags) {
		sendKillActivityBroadcast(context);
		Intent intent = new Intent(context, LXBaseActivity.class);
		if (searchParams != null) {
			intent.putExtra("startDateStr", DateUtils.localDateToyyyyMMdd(searchParams.startDate));
			intent.putExtra("location", searchParams.location);
		}

		if (flags == FLAG_OPEN_SEARCH) {
			intent.putExtra(Codes.EXTRA_OPEN_SEARCH, true);
		}

		if (flags == FLAG_OPEN_RESULTS) {
			intent.putExtra(Codes.EXTRA_OPEN_RESULTS, true);
		}

		if (flags == FLAG_DEEPLINK) {
			// If we don't have filters, open search box.
			if (searchParams.filters == null) {
				intent.putExtra(Codes.EXTRA_OPEN_SEARCH, true);
			}
			else {
				intent.putExtra("filters", searchParams.filters);
				intent.putExtra(Codes.FROM_DEEPLINK, true);
			}
		}

		startActivity(context, intent, animOptions);
	}

	// Assumes we are already searching in flights, but are not on the flight
	// search screen anymore
	public static void restartFlightSearch(Activity activity) {
		Context context = activity;
		// Clear out old data
		Db.resetBillingInfo();
		Db.getFlightSearch().setSearchResponse(null);

		// If tablet then let's just close the checkout activity to expose the results activity. A new search with old params will kick off.
		if (activity instanceof TabletCheckoutActivity) {
			Db.getTripBucket().clearFlight();
			activity.finish();
		}
		else {
			// Launch search activity (new search should start automatically due to blank data)
			Intent intent = new Intent(context, FlightSearchResultsActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			context.startActivity(intent);
		}
	}

	public static void restartHotelSearch(Activity activity) {
		Context context = activity;
		// Clear out old data
		Db.resetBillingInfo();
		Db.getHotelSearch().setSearchResponse(null);

		// If tablet then let's just close the checkout activity to expose the results activity. A new search with old params will kick off.
		if (activity instanceof TabletCheckoutActivity) {
			Db.getTripBucket().clearHotel();
			activity.finish();
		}
		else {
			// Launch hotel search activity (new search should start automatically due to blank data)
			Intent intent = new Intent(context, HotelSearchActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			context.startActivity(intent);
		}
	}

	public static void goToFlightSearch(Context context) {
		// Clear out old data
		Db.resetBillingInfo();
		Db.getFlightSearch().setSearchResponse(null);

		// Start search activity
		sendKillActivityBroadcast(context);
		Intent intent = new Intent(context, FlightSearchResultsActivity.class);
		context.startActivity(intent);
	}

	/**
	 * Helper method for determining whether or not to skip launch and start EH tablet
	 * @param context
	 * @return true if EHTablet should be (and has been) launched
	 */
	public static boolean skipLaunchScreenAndStartEHTablet(Context context) {
		Intent intent = generateStartEHTabletIntent(context);
		if (intent != null) {
			context.startActivity(intent);
			return true;
		}
		return false;
	}

	/**
	 * Sometimes we want to get out of flights (or anyplace) and want to go back to our start page.
	 * @param context
	 * @return true if we called startActivity on the way to EhTabletStart, false if not
	 */
	public static boolean goBackToEhTabletStart(Context context) {
		Intent intent = generateStartEHTabletIntent(context);
		if (intent != null) {
			sendKillActivityBroadcast(context);
			context.startActivity(intent);
			return true;
		}
		return false;
	}

	/**
	 * Builds the intent for starting EhTablet
	 * @return Intent for going to EHTablet start screen, or null if not valid for this device
	 */
	private static Intent generateStartEHTabletIntent(Context context) {
		if (ExpediaBookingApp.useTabletInterface(context)) {
			return new Intent(context, TabletLaunchActivity.class);
		}
		return null;
	}

	public static void onDataMissing(Activity activity) {
		Log.i("Key data missing - resetting the app!");

		// Reset the db
		Db.clear();

		// Go back to the start
		Intent intent = new Intent(activity, FlightSearchActivity.class);
		intent.putExtra(FlightSearchActivity.EXTRA_DATA_EXPIRED, true);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		activity.startActivity(intent);

		// Finish the current Activity
		activity.finish();
	}

	/**
	 * Send a "kill activity" broadcast to all registered listeners. This will ensure the
	 * activity backstack is erased. This whole framework is to make up for a lack of
	 * Intent.FLAG_ACTIVITY_CLEAR_TASK that we'd otherwise want to use in some cases,
	 * like when we open a hotel details from the widget. Call this method when you want
	 * to clear the task.
	 *
	 * Note: All activities must register a LocalBroadcastReceiver on the KILL_ACTIVITY
	 * intent to guarantee the backstack is actually erased.
	 *
	 * <pre class="prettyprint">
	 * public class MyActivity extends Activity {
	 *     // To make up for a lack of FLAG_ACTIVITY_CLEAR_TASK in older Android versions
	 *     private ActivityKillReceiver mKillReceiver;
	 *
	 *     protected void onCreate(Bundle savedInstanceState) {
	 *         super.onCreate(savedInstanceState);
	 *         mKillReceiver = new ActivityKillReceiver(this);
	 *         mKillReceiver.onCreate();
	 *     }
	 *
	 *     protected void onDestroy();
	 *         if (mKillReceiver != null) {
	 *             mKillReceiver.onDestroy();
	 *         }
	 *     }
	 * }
	 * </pre>
	 *
	 * @param context
	 */
	public static void sendKillActivityBroadcast(Context context) {
		Intent kill = new Intent();
		kill.setAction(ActivityKillReceiver.BROADCAST_KILL_ACTIVITY_INTENT);
		LocalBroadcastManager.getInstance(context).sendBroadcast(kill);
	}

	/**
	 * Inspired by http://android-developers.blogspot.com/2009/01/can-i-use-this-intent.html
	 *
	 * Indicates whether the specified action can be used as an intent. This
	 * method queries the package manager for installed packages that can
	 * respond to an intent with the specified action. If no suitable package is
	 * found, this method returns false.
	 *
	 * @param context The application's environment.
	 * @param intent The Intent action to check for availability.
	 *
	 * @return True if an Intent with the specified action can be sent and
	 *         responded to, false otherwise.
	 */
	public static boolean isIntentAvailable(Context context, Intent intent) {
		final PackageManager packageManager = context.getPackageManager();
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}

	// Takes care of the VSC flow. For now we only support for phone UI.
	// TODO: How do we handle for tablets?
	public static void goToVSC(Context context) {
		sendKillActivityBroadcast(context);

		Class<? extends Activity> routingTarget;

		// Send user to hotelListing by default
		routingTarget = HotelSearchActivity.class;

		Intent intent = new Intent(context, routingTarget);
		context.startActivity(intent);
	}
}
