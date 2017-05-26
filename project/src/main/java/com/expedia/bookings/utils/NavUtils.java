package com.expedia.bookings.utils;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.expedia.account.Config;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.AccountLibActivity;
import com.expedia.bookings.activity.ActivityKillReceiver;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.cars.CarSearchParam;
import com.expedia.bookings.data.lx.LxSearchParams;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.user.User;
import com.expedia.bookings.launch.activity.NewPhoneLaunchActivity;
import com.expedia.bookings.lob.lx.ui.activity.LXBaseActivity;
import com.expedia.bookings.mia.activity.MemberDealActivity;
import com.expedia.bookings.rail.activity.RailActivity;
import com.expedia.bookings.services.CarServices;
import com.expedia.bookings.tracking.CarWebViewTracking;
import com.expedia.bookings.widget.ItinerarySyncLoginExtender;
import com.expedia.ui.CarActivity;
import com.expedia.ui.CarWebViewActivity;
import com.expedia.ui.FlightActivity;
import com.expedia.ui.HotelActivity;
import com.expedia.ui.PackageActivity;
import com.google.gson.Gson;

/**
 * Utilities for navigating the app (between Activities)
 */
public class NavUtils {

	public static final int FLAG_DEEPLINK = 1;
	public static final int FLAG_OPEN_SEARCH = 2;
	public static final int FLAG_OPEN_RESULTS = 3;
	public static final int MEMBER_ONLY_DEAL_SEARCH = 4;


	public static boolean canHandleIntent(Context context, Intent intent) {
		return intent.resolveActivity(context.getPackageManager()) != null;
	}

	public static void startActivityForResult(Context context, Intent intent, Bundle options, int requestCode) {
		((AppCompatActivity) context).startActivityForResult(intent, requestCode, options);
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
		Intent intent = getLaunchIntent(context);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		if (forceShowWaterfall) {
			intent.putExtra(NewPhoneLaunchActivity.ARG_FORCE_SHOW_WATERFALL, true);
		}
		sendKillActivityBroadcast(context);
		context.startActivity(intent);
	}

	public static void goToLaunchScreen(Context context, boolean forceShowWaterfall, LineOfBusiness lobNotSupported) {
		Intent intent = getLaunchIntent(context);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		if (forceShowWaterfall) {
			intent.putExtra(NewPhoneLaunchActivity.ARG_FORCE_SHOW_WATERFALL, true);
		}
		sendKillActivityBroadcast(context);
		intent.putExtra(Codes.LOB_NOT_SUPPORTED, lobNotSupported);
		context.startActivity(intent);
	}

	public static void goToItin(Context context) {
		goToItin(context, null);
	}

	public static void goToItin(Context context, String itinNum) {
		Intent intent = getLaunchIntent(context);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra(NewPhoneLaunchActivity.ARG_FORCE_SHOW_ITIN, true);
		if (itinNum != null) {
			intent.putExtra(NewPhoneLaunchActivity.ARG_ITIN_NUM, itinNum);
		}
		context.startActivity(intent);
	}

	public static void goToAccount(Activity activity) {
		Bundle args = AccountLibActivity
			.createArgumentsBundle(LineOfBusiness.PROFILE, Config.InitialState.CreateAccount,
				new ItinerarySyncLoginExtender());
		User.signIn(activity, args);
	}

	public static void goToMemberPricing(Context context) {
		sendKillActivityBroadcast(context);
		Intent intent = new Intent(context, MemberDealActivity.class);
		context.startActivity(intent);
	}

	public static void goToSignIn(Context context) {
		goToSignIn(context, true, false, 0);
	}

	public static void goToSignIn(Context context, int flags) {
		goToSignIn(context, true, false, flags);
	}

	public static void goToSignIn(Context context, boolean showAccount, boolean useItinSyncExtender, int flags) {
		Intent intent = getLaunchIntent(context);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra(NewPhoneLaunchActivity.ARG_FORCE_SHOW_ACCOUNT, showAccount);
		context.startActivity(intent);
		Bundle bundle = new Bundle();
		if (useItinSyncExtender) {
			bundle = AccountLibActivity.createArgumentsBundle(LineOfBusiness.ITIN, new ItinerarySyncLoginExtender());
		}

		if ((flags & MEMBER_ONLY_DEAL_SEARCH) != 0) {
			bundle.putBoolean(Codes.MEMBER_ONLY_DEALS, true);
		}

		User.signIn((Activity) context, bundle);
	}

	public static void goToHotels(Context context, int flags) {
		goToHotels(context, null, null, flags);
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

		if ((flags & FLAG_DEEPLINK) != 0) {
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra(Codes.FROM_DEEPLINK, true);
		}

		if ((flags & FLAG_OPEN_SEARCH) != 0) {
			intent.putExtra(Codes.EXTRA_OPEN_SEARCH, true);
		}

		if ((flags & MEMBER_ONLY_DEAL_SEARCH) != 0) {
			intent.putExtra(Codes.MEMBER_ONLY_DEALS, true);
		}

		Class<HotelActivity> routingTarget = HotelActivity.class;
		if (params != null) {
			com.expedia.bookings.data.hotels.HotelSearchParams v2params = HotelsV2DataUtil.Companion
				.getHotelV2SearchParams(context, params);
			Gson gson = HotelsV2DataUtil.Companion.generateGson();
			intent.putExtra(HotelActivity.EXTRA_HOTEL_SEARCH_PARAMS, gson.toJson(v2params));
			intent.putExtra(Codes.TAG_EXTERNAL_SEARCH_PARAMS, true);
		}

		// Launch activity based on routing selection
		intent.setClass(context, routingTarget);
		startActivity(context, intent, animOptions);
	}

	public static void goToHotelsV2(Context context, com.expedia.bookings.data.hotels.HotelSearchParams params,
		Bundle animOptions, int flags) {
		sendKillActivityBroadcast(context);

		Intent intent = new Intent();

		if ((flags & FLAG_DEEPLINK) != 0) {
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra(Codes.FROM_DEEPLINK, true);
		}

		if ((flags & FLAG_OPEN_SEARCH) != 0) {
			intent.putExtra(Codes.EXTRA_OPEN_SEARCH, true);
		}

		Class<HotelActivity> routingTarget = HotelActivity.class;
		if (params != null) {
			Gson gson = HotelsV2DataUtil.Companion.generateGson();
			intent.putExtra(HotelActivity.EXTRA_HOTEL_SEARCH_PARAMS, gson.toJson(params));
			intent.putExtra(Codes.TAG_EXTERNAL_SEARCH_PARAMS, true);
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

	public static void goToTransport(Context context, Bundle animOptions) {
		sendKillActivityBroadcast(context);
		Intent intent = new Intent(context, LXBaseActivity.class);
		intent.putExtra(LXBaseActivity.EXTRA_IS_GROUND_TRANSPORT, true);
		startActivity(context, intent, animOptions);
	}

	public static void goToPackages(Context context, Bundle data, Bundle animOptions) {
		sendKillActivityBroadcast(context);
		Intent intent = new Intent(context, PackageActivity.class);
		if (data != null) {
			intent.putExtras(data);
		}
		startActivity(context, intent, animOptions);
	}

	public static void goToPackagesForResult(Context context, Bundle data, Bundle animOptions, int requestCode) {
		sendKillActivityBroadcast(context);
		Intent intent = new Intent(context, PackageActivity.class);
		if (data != null) {
			intent.putExtras(data);
		}
		startActivityForResult(context, intent, animOptions, requestCode);
	}

	public static void goToRail(Context context, Bundle animOptions) {
		sendKillActivityBroadcast(context);
		Intent intent = new Intent(context, RailActivity.class);
		startActivity(context, intent, animOptions);
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
		goToFlights(context, usePresetSearchParams, animOptions, 0, null);
	}

	public static void goToFlights(Context context, FlightSearchParams params) {
		goToFlights(context, true, null, 0, params);
	}

	private static void goToFlights(Context context, boolean usePresetSearchParams, Bundle animOptions, int flags,
		FlightSearchParams flightSearchParams) {
		if (!PointOfSale.getPointOfSale().supports(LineOfBusiness.FLIGHTS)) {
			NavUtils.goToLaunchScreen(context, false, LineOfBusiness.FLIGHTS);
		}
		else {
			sendKillActivityBroadcast(context);
			Intent intent;
			intent = new Intent(context, FlightActivity.class);
			if (flightSearchParams != null) {
				Gson gson = FlightsV2DataUtil.generateGson();
				intent.putExtra(Codes.SEARCH_PARAMS, gson.toJson(flightSearchParams));
			}
			intent.addFlags(flags);
			startActivity(context, intent, animOptions);
		}
	}

	public static void goToCars(Context context, Bundle animOptions) {
		sendKillActivityBroadcast(context);
		new CarWebViewTracking().trackAppCarWebViewABTest();
		if (PointOfSale.getPointOfSale().supportsCarsWebView()
			&& Db.getAbacusResponse().isUserBucketedForTest(PointOfSale.getPointOfSale().getCarsWebViewABTestID())) {
			CarWebViewActivity.IntentBuilder builder = new CarWebViewActivity.IntentBuilder(context);
			builder.setUrl(PointOfSale.getPointOfSale().getCarsTabWebViewURL());
			builder.setInjectExpediaCookies(true);
			builder.setAllowMobileRedirects(true);
			builder.setAttemptForceMobileSite(true);
			builder.setLoginEnabled(true);
			builder.setHandleBack(true);
			builder.setRetryOnFailure(true);
			builder.setTitle(context.getString(R.string.nav_car_rentals));
			builder.setTrackingName("CarWebView");
			startActivity(context, builder.getIntent(), null);
		}
		else {
			Intent intent = new Intent(context, CarActivity.class);
			startActivity(context, intent, animOptions);
		}
	}

	public static void goToCars(Context context, Bundle animOptions, CarSearchParam searchParams, String productKey,
		int flags) {
		sendKillActivityBroadcast(context);
		Intent intent = new Intent(context, CarActivity.class);
		if (searchParams != null) {
			Gson gson = CarServices.generateGson();
			intent.putExtra("carSearchParams", gson.toJson(searchParams));
		}

		if ((flags & FLAG_DEEPLINK) != 0) {
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra(Codes.FROM_DEEPLINK, true);
		}

		intent.putExtra(Codes.CARS_PRODUCT_KEY, productKey);
		startActivity(context, intent, animOptions);
	}

	public static void goToCars(Context context, Bundle animOptions, CarSearchParam searchParams, int flags) {
		sendKillActivityBroadcast(context);
		Intent intent = new Intent(context, CarActivity.class);
		if (searchParams != null) {
			Gson gson = CarServices.generateGson();
			intent.putExtra("carSearchParams", gson.toJson(searchParams));
			intent.putExtra(Codes.TAG_EXTERNAL_SEARCH_PARAMS, true);
		}

		if ((flags & FLAG_OPEN_SEARCH) != 0) {
			intent.putExtra(Codes.EXTRA_OPEN_SEARCH, true);
		}

		if ((flags & FLAG_DEEPLINK) != 0) {
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra(Codes.FROM_DEEPLINK, true);
		}

		startActivity(context, intent, animOptions);
	}

	public static void goToActivities(Context context, Bundle animOptions, LxSearchParams searchParams, int flags) {
		sendKillActivityBroadcast(context);
		Intent intent = new Intent(context, LXBaseActivity.class);
		if (searchParams != null) {
			intent.putExtra("startDateStr", DateUtils.localDateToyyyyMMdd(searchParams.getActivityStartDate()));
			intent.putExtra("endDateStr", DateUtils.localDateToyyyyMMdd(searchParams.getActivityEndDate()));
			intent.putExtra("location", searchParams.getLocation());
		}

		if (flags == FLAG_OPEN_SEARCH) {
			intent.putExtra(Codes.EXTRA_OPEN_SEARCH, true);
		}

		if (flags == FLAG_OPEN_RESULTS) {
			intent.putExtra(Codes.EXTRA_OPEN_RESULTS, true);
		}

		if (flags == FLAG_DEEPLINK) {
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			// If we don't have filters, open search box.
			if (Strings.isNotEmpty(searchParams.getActivityId())) {
				intent.putExtra("activityId", searchParams.getActivityId());
				intent.putExtra(Codes.FROM_DEEPLINK_TO_DETAILS, true);
			}
			else {
				if (Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppLXNavigateToSRP)
					|| !searchParams.getFilters().isEmpty()) {
					intent.putExtra("filters", searchParams.getFilters());
					intent.putExtra(Codes.FROM_DEEPLINK, true);
				}
				else {
					intent.putExtra(Codes.EXTRA_OPEN_SEARCH, true);
				}
			}
		}

		startActivity(context, intent, animOptions);
	}

	// Assumes we are already searching in flights, but are not on the flight
	// search screen anymore
	public static void restartFlightSearch(Activity activity) {
		// Clear out old data
		Db.resetBillingInfo();
		Db.getFlightSearch().setSearchResponse(null);

		// If tablet then let's just close the checkout activity to expose the results activity. A new search with old params will kick off.
		Db.getTripBucket().clearFlight();
		activity.finish();
	}

	public static void restartHotelSearch(Activity activity) {
		// Clear out old data
		Db.resetBillingInfo();
		Db.getHotelSearch().setSearchResponse(null);
	}

	/**
	 * Send a "kill activity" broadcast to all registered listeners. This will ensure the
	 * activity backstack is erased. This whole framework is to make up for a lack of
	 * Intent.FLAG_ACTIVITY_CLEAR_TASK that we'd otherwise want to use in some cases,
	 * like when we open a hotel details from the widget. Call this method when you want
	 * to clear the task.
	 * <p/>
	 * Note: All activities must register a LocalBroadcastReceiver on the KILL_ACTIVITY
	 * intent to guarantee the backstack is actually erased.
	 * <p/>
	 * <pre class="prettyprint">
	 * public class MyActivity extends Activity {
	 * // To make up for a lack of FLAG_ACTIVITY_CLEAR_TASK in older Android versions
	 * private ActivityKillReceiver mKillReceiver;
	 * <p/>
	 * protected void onCreate(Bundle savedInstanceState) {
	 * super.onCreate(savedInstanceState);
	 * mKillReceiver = new ActivityKillReceiver(this);
	 * mKillReceiver.onCreate();
	 * }
	 * <p/>
	 * protected void onDestroy();
	 * if (mKillReceiver != null) {
	 * mKillReceiver.onDestroy();
	 * }
	 * }
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
	 * <p/>
	 * Indicates whether the specified action can be used as an intent. This
	 * method queries the package manager for installed packages that can
	 * respond to an intent with the specified action. If no suitable package is
	 * found, this method returns false.
	 *
	 * @param context The application's environment.
	 * @param intent  The Intent action to check for availability.
	 * @return True if an Intent with the specified action can be sent and
	 * responded to, false otherwise.
	 */
	public static boolean isIntentAvailable(Context context, Intent intent) {
		final PackageManager packageManager = context.getPackageManager();
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}

	public static Intent getLaunchIntent(Context context) {
		return new Intent(context, NewPhoneLaunchActivity.class);
	}
}
