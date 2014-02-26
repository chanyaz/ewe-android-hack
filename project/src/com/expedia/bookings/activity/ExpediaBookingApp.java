package com.expedia.bookings.activity;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.Locale;

import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.text.format.DateUtils;

import com.activeandroid.ActiveAndroid;
import com.expedia.bookings.R;
import com.expedia.bookings.appwidget.ExpediaBookingsWidgetProvider;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.ExpediaImageManager;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.LocalExpertSite;
import com.expedia.bookings.data.PushNotificationRegistrationResponse;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.WalletPromoResponse;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.notification.GCMRegistrationKeeper;
import com.expedia.bookings.notification.PushNotificationUtils;
import com.expedia.bookings.server.CrossContextHelper;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AssetZoneInfoProvider;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.WalletUtils;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.DebugUtils;
import com.mobiata.android.Log;
import com.mobiata.android.bitmaps.TwoLevelImageCache;
import com.mobiata.android.debug.MemoryUtils;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.BuildConfigUtils;
import com.mobiata.android.util.SettingUtils;
import com.mobiata.android.util.TimingLogger;
import com.mobiata.flightlib.data.sources.FlightStatsDbUtils;

public class ExpediaBookingApp extends Application implements UncaughtExceptionHandler {
	// Don't change the actual string, updated identifier for clarity
	private static final String PREF_FIRST_LAUNCH_OCCURED = "PREF_FIRST_LAUNCH";

	// For logged in backward compatibility with AccountManager
	private static final String PREF_UPGRADED_TO_ACCOUNT_MANAGER = "PREF_UPGRADED_TO_ACCOUNT_MANAGER";

	// For bug #2249 where we did not point at the production push server
	private static final String PREF_UPGRADED_TO_PRODUCTION_PUSH = "PREF_UPGRADED_TO_PRODUCTION_PUSH";

	private static final int MIN_IMAGE_CACHE_SIZE = (1024 * 1024 * 6); // 6 MB
	public static final boolean IS_EXPEDIA = AndroidUtils.getBuildConfigValue("IS_EXPEDIA"); // Check to see if this is an Expedia app build
	public static final boolean IS_VSC = AndroidUtils.getBuildConfigValue("IS_VSC"); // Check to see if this is a VSC app build
	public static final boolean IS_TRAVELOCITY = AndroidUtils.getBuildConfigValue("IS_TRAVELOCITY"); // Check to see if this is a Travelocity app build

	public static final String MEDIA_URL = BuildConfigUtils.get("com.expedia.bookings", "MEDIA_URL");

	private UncaughtExceptionHandler mOriginalUncaughtExceptionHandler;

	// This is used only for testing; normally you can assume that onCreate()
	// has been called before any other code, but that's not always the case
	// with unit tests.  This allows a unit test to wait until it knows that
	// we've initialized key parts of the app.
	private boolean mInitialized = false;

	@Override
	public void onCreate() {
		super.onCreate();

		TimingLogger startupTimer = new TimingLogger("ExpediaBookings", "startUp");

		ActiveAndroid.initialize(this);

		startupTimer.addSplit("ActiveAndroid Init");

		boolean isRelease = AndroidUtils.isRelease(this);
		boolean isLogEnablerInstalled = DebugUtils.isLogEnablerInstalled(this);
		Log.configureLogging("ExpediaBookings", !isRelease || isLogEnablerInstalled);

		startupTimer.addSplit("Logger Init");

		// We want this fairly high up there so that we set this as
		// the Provider before anything tries to use Joda time
		AssetZoneInfoProvider.init(this, "joda/data/");
		startupTimer.addSplit("Joda TZ Provider Init");

		try {
			if (!isRelease) {
				FlightStatsDbUtils.setUpgradeCutoff(DateUtils.DAY_IN_MILLIS); // 1 day cutoff for upgrading FS.db
			}

			FlightStatsDbUtils.createDatabaseIfNotExists(this);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}

		startupTimer.addSplit("FS.db Init");

		if (IS_VSC) {
			Locale locale = new Locale("fr", "FR");
			Configuration myConfig = new Configuration(getResources().getConfiguration());
			Locale.setDefault(locale);

			myConfig.locale = locale;
			getBaseContext().getResources().updateConfiguration(myConfig, getResources().getDisplayMetrics());
			startupTimer.addSplit("VSC force fr locale");
		}

		// Init required for Omniture tracking
		OmnitureTracking.init(this);
		// Setup Omniture for tracking crashes
		mOriginalUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);

		startupTimer.addSplit("Omniture Init");

		// Initialize some parts of the code that require a Context
		PointOfSale.init(this);
		startupTimer.addSplit("PointOfSale Init");

		FontCache.initialize(this);
		startupTimer.addSplit("FontCache Init");

		AdTracker.initialize(this);
		startupTimer.addSplit("AdTracker Init");

		ItineraryManager.getInstance().init(this);
		startupTimer.addSplit("ItineraryManager Init");

		ExpediaImageManager.init(this);
		startupTimer.addSplit("ExpediaImageManager Init");

		LocalExpertSite.init(this);
		startupTimer.addSplit("LocalExpertSite Init");

		String serverUrlPath = IS_VSC ? "ExpediaSharedData/VSCServerURLs.json"
				: "ExpediaSharedData/ExpediaServerURLs.json";
		ExpediaServices.initEndPoints(this, serverUrlPath);
		startupTimer.addSplit("ExpediaServices endpoints init");

		// If we are upgrading from a pre-AccountManager version, update account manager to include our logged in user.
		if (!SettingUtils.get(this, PREF_UPGRADED_TO_ACCOUNT_MANAGER, false)) {
			if (User.isLoggedInOnDisk(this) && !User.isLoggedInToAccountManager(this)) {
				if (Db.getUser() == null) {
					Db.loadUser(this);
				}
				if (Db.getUser() != null) {
					User.addUserToAccountManager(this, Db.getUser());
				}
			}
			SettingUtils.save(this, PREF_UPGRADED_TO_ACCOUNT_MANAGER, true);
		}
		startupTimer.addSplit("User upgraded to use AccountManager (if needed)");

		// 2249: We were not sending push registrations to the prod push server
		// If we are upgrading from a previous version we will send an unregister to the test push server
		// We also don't want to bother if the user has never launched the app before
		if (isRelease
			&& !SettingUtils.get(this, PREF_UPGRADED_TO_PRODUCTION_PUSH, false)
			&& SettingUtils.get(this, PREF_FIRST_LAUNCH_OCCURED, false)) {

			final String testPushServer = PushNotificationUtils.REGISTRATION_URL_TEST;
			final String regId = GCMRegistrationKeeper.getInstance(this).getRegistrationId(this);
			OnDownloadComplete<PushNotificationRegistrationResponse> callback = new OnDownloadComplete<PushNotificationRegistrationResponse>() {
				@Override
				public void onDownload(PushNotificationRegistrationResponse result) {
					Log.d("Unregistered from test server");
					SettingUtils.save(ExpediaBookingApp.this, PREF_UPGRADED_TO_PRODUCTION_PUSH, true);
				}
			};
			PushNotificationUtils.unRegister(this, testPushServer, regId, callback);
		}
		startupTimer.addSplit("Push server unregistered (if needed)");

		// We want to try to start loading data (but it may not be finished syncing before someone tries to use it).
		ItineraryManager.getInstance().startSync(false);

		if (!SettingUtils.get(this, PREF_FIRST_LAUNCH_OCCURED, false)) {
			SettingUtils.save(this, PREF_FIRST_LAUNCH_OCCURED, true);
			AdTracker.trackFirstLaunch();
			startupTimer.addSplit("AdTracker first launch tracking");
		}

		// 2249: We don't need to unregister if this is the user's first launch
		if (!SettingUtils.get(this, PREF_FIRST_LAUNCH_OCCURED, false)) {
			SettingUtils.save(ExpediaBookingApp.this, PREF_UPGRADED_TO_PRODUCTION_PUSH, true);
		}

		// #13097: We need a way to disable the widget on ICS tablets.  This is a hacky way of doing so,
		// in that it requires the app to be launched at least once before it can be disabled, but it's
		// the best we can do for the time being.
		// #1807. VSC Disable widgets
		if (AndroidUtils.isHoneycombTablet(this) || ExpediaBookingApp.IS_VSC) {
			try {
				PackageManager pm = getPackageManager();
				ComponentName cn = new ComponentName(this, ExpediaBookingsWidgetProvider.class);
				pm.setComponentEnabledSetting(cn, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
						PackageManager.DONT_KILL_APP);
			}
			catch (Exception e) {
				// Just in case, PM can be touchy
				Log.w("PackageManager blew up.", e);
			}
		}

		startupTimer.addSplit("Disable ICS Tablet & VSC Widgets");

		// Some useful info to have on hand in case of memory crashes
		long maxMemory = Runtime.getRuntime().maxMemory();
		int memoryClass = ((ActivityManager) getSystemService(ACTIVITY_SERVICE)).getMemoryClass();
		Log.i("MaxMemory=" + maxMemory + " bytes (" + (maxMemory / 1048576) + "MB) MemoryClass=" + memoryClass + "MB");

		// Here's what we're aiming for, in terms of memory cache size:
		// 1. At least MIN_IMAGE_CACHE_SIZE
		// 2. No greater than 1/5th the memory available
		int maxCacheSize = (1024 * 1024 * memoryClass) / 5;
		if (maxCacheSize < MIN_IMAGE_CACHE_SIZE) {
			maxCacheSize = MIN_IMAGE_CACHE_SIZE;
		}

		// Init TwoLevelImageCache
		TwoLevelImageCache.init(this, maxCacheSize);

		startupTimer.addSplit("TwoLevelImageCache init");

		// Kick off thread to determine if the Google Wallet promo is still available
		(new Thread(new Runnable() {
			@Override
			public void run() {
				boolean walletPromoEnabled = SettingUtils.get(getApplicationContext(),
						WalletUtils.SETTING_SHOW_WALLET_COUPON, false);

				ExpediaServices services = new ExpediaServices(getApplicationContext());
				WalletPromoResponse response = services.googleWalletPromotionEnabled();
				boolean isNowEnabled = response != null && response.isEnabled();

				if (walletPromoEnabled != isNowEnabled) {
					Log.i("Google Wallet promo went from \"" + walletPromoEnabled + "\" to \"" + isNowEnabled + "\"");
					SettingUtils.save(getApplicationContext(), WalletUtils.SETTING_SHOW_WALLET_COUPON,
							isNowEnabled);
				}
				else {
					Log.d("Google Wallet promo enabled: " + walletPromoEnabled);
				}
			}
		})).start();

		startupTimer.addSplit("Google Wallet promo thread creation");

		// If the current POS needs flight routes, update our data
		if (PointOfSale.getPointOfSale().displayFlightDropDownRoutes()) {
			CrossContextHelper.updateFlightRoutesData(getApplicationContext(), false);
			startupTimer.addSplit("Flight routes download started");
		}

		startupTimer.dumpToLog();

		mInitialized = true;
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		OmnitureTracking.trackCrash(this, ex);

		// Perform a heap dump on OOME for easy reference.
		if (ex != null) {
			Throwable rootCause = ex;
			while (rootCause.getCause() != null) {
				rootCause = rootCause.getCause();
			}

			Log.d("ExpediaBookingApp exception handler w/ class:" + ex.getClass() + "; root cause="
					+ rootCause.getClass());
			if (OutOfMemoryError.class.equals(rootCause.getClass())) {
				TwoLevelImageCache.debugInfo();

				if (MemoryUtils.dumpMemoryStateToDisk(getApplicationContext())) {
					SettingUtils.save(this, getString(R.string.preference_debug_notify_oom_crash), true);
				}
			}
		}

		// Call the original exception handler
		mOriginalUncaughtExceptionHandler.uncaughtException(thread, ex);
	}

	/**
	 * Tells testers if the app has been initialized.  I would warn against
	 * using it outside of a testing environment, as its use would indicate
	 * you are doing something wrong.
	 */
	public boolean isInitialized() {
		return mInitialized;
	}

	//////////////////////////////////////////////////////////////////////////
	// All-app utilities

	// #11076 - for Android 3.0, we still use the phone version of the app due to crippling bugs.
	public static boolean useTabletInterface(Context context) {
		return AndroidUtils.getSdkVersion() >= 12
				&& (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// LISTENERS FOR WHEN SEARCH PARAMS CHANGE IN THE WIDGET
	//////////////////////////////////////////////////////////////////////////////////////////
	/*
	 *  The app maintains a list of listeners to notify when search params
	 *  change in the widget. This is so that we can easily propogate through the
	 *  the app the need to use searchParams from the widget instead of the ones
	 *  being driven by user parameters set within the app
	 */
	public interface OnSearchParamsChangedInWidgetListener {
		public void onSearchParamsChanged(HotelSearchParams searchParams);
	};

	private ArrayList<OnSearchParamsChangedInWidgetListener> mListeners;

	public void registerSearchParamsChangedInWidgetListener(OnSearchParamsChangedInWidgetListener listener) {
		if (mListeners == null) {
			mListeners = new ArrayList<OnSearchParamsChangedInWidgetListener>();
		}
		mListeners.add(listener);
	}

	public void unregisterSearchParamsChangedInWidgetListener(OnSearchParamsChangedInWidgetListener listener) {
		if (mListeners == null) {
			return;
		}
		mListeners.remove(listener);
	}

	public void broadcastSearchParamsChangedInWidget(HotelSearchParams searchParams) {
		if (mListeners != null) {
			for (OnSearchParamsChangedInWidgetListener listener : mListeners) {
				listener.onSearchParamsChanged(searchParams);
			}
		}
	}

	private Locale mOldLocale;

	@Override
	public void onConfigurationChanged(final Configuration newConfig) {
		if (IS_VSC) {
			handleVscConfigurationChanged(newConfig);
		}
		else {
			// Default behaviour, we want to ignore this completely
			super.onConfigurationChanged(newConfig);
		}
	}

	private void handleVscConfigurationChanged(final Configuration newConfig) {
		Locale locale = new Locale("fr", "FR");
		if (!newConfig.locale.equals(locale)) {
			Log.d("VSC: Forcing fr locale");
			Configuration myConfig = new Configuration(newConfig);
			Locale.setDefault(locale);

			myConfig.locale = locale;
			getBaseContext().getResources().updateConfiguration(myConfig, getResources().getDisplayMetrics());

			// Send broadcast so that we can re-create activities
			Intent intent = new Intent(VSCLocaleChangeReceiver.ACTION_LOCALE_CHANGED);
			sendBroadcast(intent);
		}

		super.onConfigurationChanged(newConfig);
	}
}
