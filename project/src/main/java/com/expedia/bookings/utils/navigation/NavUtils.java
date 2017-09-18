package com.expedia.bookings.utils.navigation;

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
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.user.UserStateManager;
import com.expedia.bookings.launch.activity.NewPhoneLaunchActivity;
import com.expedia.bookings.lob.lx.ui.activity.LXBaseActivity;
import com.expedia.bookings.mia.activity.MemberDealActivity;
import com.expedia.bookings.rail.activity.RailActivity;
import com.expedia.bookings.server.EndpointProvider;
import com.expedia.bookings.tracking.RailWebViewTracking;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.ItinerarySyncLoginExtender;
import com.expedia.ui.LOBWebViewActivity;

/**
 * Utilities for navigating the app (between Activities)
 */
public class NavUtils {
	public static final int FLAG_DEEPLINK = 0x00000001;
	public static final int FLAG_OPEN_SEARCH = 0x00000002;
	public static final int FLAG_OPEN_RESULTS = 0x00000004;
	public static final int MEMBER_ONLY_DEAL_SEARCH = 0x00000008;
	public static final int FLAG_REMOVE_CALL_ACTIVITY_FROM_STACK = 0x00000010;

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

	public static void goToAccount(Activity activity, Config.InitialState initialState) {
		Bundle args = AccountLibActivity
			.createArgumentsBundle(LineOfBusiness.PROFILE, initialState,
				new ItinerarySyncLoginExtender());

		getUserStateManager(activity).signIn(activity, args);
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
			bundle = AccountLibActivity.createArgumentsBundle(LineOfBusiness.LAUNCH, new ItinerarySyncLoginExtender());
		}

		if ((flags & MEMBER_ONLY_DEAL_SEARCH) != 0) {
			bundle.putBoolean(Codes.MEMBER_ONLY_DEALS, true);
		}

		getUserStateManager(context).signIn((Activity) context, bundle);
	}

	public static void goToTransport(Context context, Bundle animOptions, int expediaFlags) {
		sendKillActivityBroadcast(context);
		Intent intent = new Intent(context, LXBaseActivity.class);
		intent.putExtra(LXBaseActivity.EXTRA_IS_GROUND_TRANSPORT, true);
		startActivity(context, intent, animOptions);
		finishIfFlagged(context, expediaFlags);
	}

 	public static void goToRail(Context context, Bundle animOptions, int expediaFlags) {
		sendKillActivityBroadcast(context);
		RailWebViewTracking.trackAppRailWebViewABTest();
		boolean isAbTestEnabled = Db.getAbacusResponse().isUserBucketedForTest(PointOfSale.getPointOfSale().getRailsWebViewABTestID());
		if (PointOfSale.getPointOfSale().supportsRailsWebView() && isAbTestEnabled) {
			LOBWebViewActivity.IntentBuilder builder = new LOBWebViewActivity.IntentBuilder(context);
			EndpointProvider endpointProvider = Ui.getApplication(context).appComponent().endpointProvider();
			builder.setUrl(endpointProvider.getRailWebViewEndpointUrl());
			builder.setInjectExpediaCookies(true);
			builder.setAllowMobileRedirects(true);
			builder.setLoginEnabled(true);
			builder.setHandleBack(true);
			builder.setRetryOnFailure(true);
			builder.setTitle(context.getString(R.string.nav_rail));
			builder.setTrackingName("RailWebView");
			startActivity(context, builder.getIntent(), null);
			finishIfFlagged(context, expediaFlags);
		}
		else {
			Intent intent = new Intent(context, RailActivity.class);
			startActivity(context, intent, animOptions);
			finishIfFlagged(context, expediaFlags);
		}
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

	protected static void finishIfFlagged(Context context, int flags) {
		if ((flags & FLAG_REMOVE_CALL_ACTIVITY_FROM_STACK) != 0) {
			if (context instanceof AppCompatActivity) {
				((AppCompatActivity) context).finish();
			}
			else {
				throw new IllegalArgumentException("Error: Expected an AppCompatActivity context. " +
						"Can't finish() a non AppCompatActivity context");
			}
		}
	}

	private static UserStateManager getUserStateManager(Context context) {
		return Ui.getApplication(context).appComponent().userStateManager();
	}
}
