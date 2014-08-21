package com.expedia.bookings.activity;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.Locale;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.text.format.DateUtils;

import com.activeandroid.ActiveAndroid;
import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.L2ImageCache;
import com.expedia.bookings.data.Db;
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
import com.expedia.bookings.utils.AdvertisingIdUtils;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.SocketActivityHierarchyServer;
import com.expedia.bookings.utils.WalletUtils;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.DebugUtils;
import com.mobiata.android.Log;
import com.mobiata.android.debug.MemoryUtils;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.SettingUtils;
import com.mobiata.android.util.TimingLogger;
import com.mobiata.flightlib.data.sources.FlightStatsDbUtils;
import net.danlew.android.joda.ResourceZoneInfoProvider;

public class ExpediaBookingApp extends Application implements UncaughtExceptionHandler {
	// Don't change the actual string, updated identifier for clarity
	private static final String PREF_FIRST_LAUNCH_OCCURED = "PREF_FIRST_LAUNCH";

	// For logged in backward compatibility with AccountManager
	private static final String PREF_UPGRADED_TO_ACCOUNT_MANAGER = "PREF_UPGRADED_TO_ACCOUNT_MANAGER";

	// For bug #2249 where we did not point at the production push server
	private static final String PREF_UPGRADED_TO_PRODUCTION_PUSH = "PREF_UPGRADED_TO_PRODUCTION_PUSH";

	private static final int MIN_IMAGE_CACHE_SIZE = (1024 * 1024 * 6); // 6 MB
	public static final boolean IS_EXPEDIA = BuildConfig.IS_EXPEDIA;
	public static final boolean IS_VSC = BuildConfig.IS_VSC;
	public static final boolean IS_TRAVELOCITY = BuildConfig.IS_TRAVELOCITY;
	public static final boolean IS_AAG = BuildConfig.IS_AAG;

	public static final boolean IS_AUTOMATION = BuildConfig.IS_AUTOMATION;

	public static final String MEDIA_URL = BuildConfig.MEDIA_URL;

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

		if (SettingUtils.get(this, getString(R.string.preference_should_start_hierarchy_server), false)) {
			SocketActivityHierarchyServer activityHierarchyServer = new SocketActivityHierarchyServer();
			try {
				activityHierarchyServer.start();
				registerActivityLifecycleCallbacks(activityHierarchyServer);
			}
			catch (Exception e) {
				Log.e("Failed to start HierarchyServer", e);
			}
			startupTimer.addSplit("SocketActivityHierarchyServer Init");
		}

		ActiveAndroid.initialize(this);

		startupTimer.addSplit("ActiveAndroid Init");

		boolean isRelease = AndroidUtils.isRelease(this);
		boolean isLogEnablerInstalled = DebugUtils.isLogEnablerInstalled(this);
		Log.configureLogging("ExpediaBookings", !isRelease || isLogEnablerInstalled);

		startupTimer.addSplit("Logger Init");

		// We want this fairly high up there so that we set this as
		// the Provider before anything tries to use Joda time
		ResourceZoneInfoProvider.init(this);
		startupTimer.addSplit("Joda TZ Provider Init");

		ExpediaServices.init(this);
		startupTimer.addSplit("ExpediaServices init");

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
		// Pull down advertising ID
		AdvertisingIdUtils.loadIDFA(this);
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

		L2ImageCache.initAllCacheInstances(this);
		startupTimer.addSplit("L2ImageCache Init");

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
				L2ImageCache.sGeneralPurpose.debugInfo();
				L2ImageCache.sDestination.debugInfo();

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

	// Due to a low number of users and a desire to use latest APIs,
	// we only use tablet UI on ICS+
	public static boolean useTabletInterface(Context context) {
		return AndroidUtils.isTablet(context);
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
