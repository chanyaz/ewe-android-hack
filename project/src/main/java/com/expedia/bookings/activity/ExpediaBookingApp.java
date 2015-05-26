package com.expedia.bookings.activity;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Locale;
import java.util.UUID;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.support.multidex.MultiDexApplication;
import android.text.format.DateUtils;

import com.activeandroid.ActiveAndroid;
import com.crashlytics.android.Crashlytics;
import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.dagger.AppComponent;
import com.expedia.bookings.dagger.AppModule;
import com.expedia.bookings.dagger.CarComponent;
import com.expedia.bookings.dagger.DaggerAppComponent;
import com.expedia.bookings.dagger.DaggerCarComponent;
import com.expedia.bookings.dagger.DaggerHotelComponent;
import com.expedia.bookings.dagger.DaggerLXComponent;
import com.expedia.bookings.dagger.DaggerLaunchComponent;
import com.expedia.bookings.dagger.HotelComponent;
import com.expedia.bookings.dagger.LXComponent;
import com.expedia.bookings.dagger.LaunchComponent;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.PushNotificationRegistrationResponse;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.WalletPromoResponse;
import com.expedia.bookings.data.abacus.AbacusEvaluateQuery;
import com.expedia.bookings.data.abacus.AbacusResponse;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.notification.GCMRegistrationKeeper;
import com.expedia.bookings.notification.PushNotificationUtils;
import com.expedia.bookings.server.CrossContextHelper;
import com.expedia.bookings.server.EndPoint;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.CurrencyUtils;
import com.expedia.bookings.utils.DebugInfoUtils;
import com.expedia.bookings.utils.ExpediaDebugUtil;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.KahunaUtils;
import com.expedia.bookings.utils.LeanPlumUtils;
import com.expedia.bookings.utils.MockModeShim;
import com.expedia.bookings.utils.StethoShim;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.WalletUtils;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.DebugUtils;
import com.mobiata.android.Log;
import com.mobiata.android.util.AdvertisingIdUtils;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.SettingUtils;
import com.mobiata.android.util.TimingLogger;
import com.mobiata.flightlib.data.sources.FlightStatsDbUtils;
import com.squareup.leakcanary.LeakCanary;

import net.danlew.android.joda.JodaTimeAndroid;

import io.fabric.sdk.android.Fabric;
import rx.Observer;

public class ExpediaBookingApp extends MultiDexApplication implements UncaughtExceptionHandler,
	AdvertisingIdUtils.OnIDFALoaded {
	// Don't change the actual string, updated identifier for clarity
	private static final String PREF_FIRST_LAUNCH_OCCURED = "PREF_FIRST_LAUNCH";

	// For logged in backward compatibility with AccountManager
	private static final String PREF_UPGRADED_TO_ACCOUNT_MANAGER = "PREF_UPGRADED_TO_ACCOUNT_MANAGER";

	// For bug #2249 where we did not point at the production push server
	private static final String PREF_UPGRADED_TO_PRODUCTION_PUSH = "PREF_UPGRADED_TO_PRODUCTION_PUSH";

	// For Abacus bucketing GUID
	private static final String PREF_ABACUS_GUID = "PREF_ABACUS_GUID";

	public static final String MEDIA_URL = BuildConfig.MEDIA_URL;

	private UncaughtExceptionHandler mOriginalUncaughtExceptionHandler;

	// Debug / test settings

	public static boolean sIsAutomation = false;

	public static boolean isAutomation() {
		return sIsAutomation;
	}

	public static void setAutomation(boolean isAutomation) {
		sIsAutomation = isAutomation;
	}

	@Override
	public void onCreate() {
		TimingLogger startupTimer = new TimingLogger("ExpediaBookings", "startUp");
		super.onCreate();
		startupTimer.addSplit("super.onCreate()");

		if (!isRobolectric() && !isAutomation()) {
			Fabric.with(this, new Crashlytics());
			startupTimer.addSplit("Crashlytics started.");

			StethoShim.install(this);
			startupTimer.addSplit("Stetho init");

			LeakCanary.install(this);
			startupTimer.addSplit("LeakCanary init");
		}

		mAppComponent = DaggerAppComponent.builder()
			.appModule(new AppModule(this))
			.build();
		startupTimer.addSplit("Dagger AppModule created");

		if (mAppComponent.endpointProvider().getEndPoint() == EndPoint.MOCK_MODE) {
			MockModeShim.initMockWebServer(this);
			startupTimer.addSplit("Mock mode init");
		}

		PicassoHelper.init(this, mAppComponent.okHttpClient());
		startupTimer.addSplit("Picasso started.");

		ActiveAndroid.initialize(this);

		startupTimer.addSplit("ActiveAndroid Init");

		boolean isLogEnablerInstalled = BuildConfig.DEBUG ||
			DebugUtils.isLogEnablerInstalled(this) ||
			ExpediaDebugUtil.isEBToolApkInstalled(this);
		Log.configureLogging("ExpediaBookings", isLogEnablerInstalled);

		startupTimer.addSplit("Logger Init");

		// We want this fairly high up there so that we set this as
		// the Provider before anything tries to use Joda time
		JodaTimeAndroid.init(this);
		startupTimer.addSplit("Joda TZ Provider Init");

		try {
			if (BuildConfig.DEBUG) {
				FlightStatsDbUtils.setUpgradeCutoff(DateUtils.DAY_IN_MILLIS); // 1 day cutoff for upgrading FS.db
			}

			FlightStatsDbUtils.createDatabaseIfNotExists(this);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}

		startupTimer.addSplit("FS.db Init");

		// Pull down advertising ID
		if (!isAutomation()) {
			AdvertisingIdUtils.loadIDFA(this, this);
			startupTimer.addSplit("IDFA wireup");
		}

		// Init required for Omniture tracking
		OmnitureTracking.init(this);
		if (!isRobolectric()) {
			// Setup Omniture for tracking crashes
			mOriginalUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
			Thread.setDefaultUncaughtExceptionHandler(this);
		}

		startupTimer.addSplit("Omniture Init");

		// Initialize some parts of the code that require a Context
		PointOfSale.init(this);
		startupTimer.addSplit("PointOfSale Init");

		if (ProductFlavorFeatureConfiguration.getInstance().wantsCustomHandlingForLocaleConfiguration()) {

			Locale locale = getLocaleForWhiteLabels();

			Configuration myConfig = new Configuration(getResources().getConfiguration());
			Locale.setDefault(locale);

			myConfig.locale = locale;
			getBaseContext().getResources().updateConfiguration(myConfig, getResources().getDisplayMetrics());
			startupTimer.addSplit("Force locale to " + locale.getLanguage());
		}

		FontCache.initialize(this);
		startupTimer.addSplit("FontCache Init");

		AdTracker.initialize(this);
		startupTimer.addSplit("AdTracker Init");

		ItineraryManager.getInstance().init(this);
		// Load data from Disk
		ItineraryManager.getInstance().startSync(false, true, false);
		startupTimer.addSplit("ItineraryManager Init/Load");

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

		if (ProductFlavorFeatureConfiguration.getInstance().isLeanPlumEnabled()) {
			LeanPlumUtils.init(this);
			startupTimer.addSplit("LeanPlum started.");
		}

		if (ProductFlavorFeatureConfiguration.getInstance().isKahunaEnabled()) {
			KahunaUtils.init(this);
			registerActivityLifecycleCallbacks(new ExpediaActivityLifeCycleCallBack());
			startupTimer.addSplit("Kahuna started.");
		}

		// 2249: We were not sending push registrations to the prod push server
		// If we are upgrading from a previous version we will send an unregister to the test push server
		// We also don't want to bother if the user has never launched the app before
		if (BuildConfig.RELEASE
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
		}, "WalletCheck")).start();

		startupTimer.addSplit("Google Wallet promo thread creation");

		// If the current POS needs flight routes, update our data
		if (PointOfSale.getPointOfSale().displayFlightDropDownRoutes()) {
			CrossContextHelper.updateFlightRoutesData(getApplicationContext(), false);
			startupTimer.addSplit("Flight routes download started");
		}

		CurrencyUtils.initMap(this);
		startupTimer.addSplit("Currency Utils init");

		AbacusEvaluateQuery query = new AbacusEvaluateQuery(generateAbacusGuid(), PointOfSale.getPointOfSale().getTpid(), 0);
		query.addExperiments(AbacusUtils.getActiveTests());
		mAppComponent.abacus().downloadBucket(query, abacusSubscriber);
		startupTimer.addSplit("Abacus Guid init");

		startupTimer.dumpToLog();
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		setCrashlyticsMetadata();

		// Call the original exception handler - probably crashlytics
		mOriginalUncaughtExceptionHandler.uncaughtException(thread, ex);

		OmnitureTracking.trackCrash(this, ex);
	}

	//////////////////////////////////////////////////////////////////////////
	// All-app utilities

	// Due to a low number of users and a desire to use latest APIs,
	// we only use tablet UI on ICS+
	public static boolean useTabletInterface(Context context) {
		return AndroidUtils.isTablet(context);
	}

	//////////////////////////////////////////////////////////////////////////
	// Dagger instances

	private AppComponent mAppComponent;
	private CarComponent mCarComponent;
	private HotelComponent mHotelComponent;
	private LaunchComponent mLaunchComponent;

	private LXComponent mLXComponent;
	private LXComponent mLXTestComponent;

	public AppComponent appComponent() {
		return mAppComponent;
	}

	public void defaultCarComponents() {
		setCarComponent(DaggerCarComponent.builder()
			.appComponent(mAppComponent)
			.build());
	}

	public void setCarComponent(CarComponent carComponent) {
		mCarComponent = carComponent;
	}

	public CarComponent carComponent() {
		return mCarComponent;
	}

	public void defaultHotelComponents() {
		setHotelComponent(DaggerHotelComponent.builder()
			.appComponent(mAppComponent)
			.build());
	}

	public void setHotelComponent(HotelComponent hotelComponent) {
		mHotelComponent = hotelComponent;
	}

	public HotelComponent hotelComponent() {
		return mHotelComponent;
	}

	public void defaultLXComponents() {
		if (mLXTestComponent == null) {
			mLXComponent = DaggerLXComponent.builder()
				.appComponent(mAppComponent)
				.build();
		}
		else {
			mLXComponent = mLXTestComponent;
		}
	}

	public void setLXTestComponent(LXComponent lxTestComponent) {
		mLXTestComponent = lxTestComponent;
	}

	public LXComponent lxComponent() {
		return mLXComponent;
	}

	public void defaultLaunchComponents() {
		setLaunchComponent(DaggerLaunchComponent.builder()
			.appComponent(mAppComponent)
			.build());
	}

	public void setLaunchComponent(LaunchComponent launchComponent) {
		mLaunchComponent = launchComponent;
	}

	public LaunchComponent launchComponent() {
		return mLaunchComponent;
	}

	// Configuration changes

	@Override
	public void onConfigurationChanged(final Configuration newConfig) {
		if (!ProductFlavorFeatureConfiguration.getInstance().wantsCustomHandlingForLocaleConfiguration()) {
			// Default behaviour, we want to ignore this completely
			super.onConfigurationChanged(newConfig);
		}
		else {
			handleConfigurationChanged(newConfig, getLocaleForWhiteLabels());
		}
	}

	public void handleConfigurationChanged(final Configuration newConfig, Locale locale) {

		if (locale.equals(getResources().getConfiguration().locale)) {
			Log.d("No Locale change required, locale=" + locale.toString());
			return;
		}

		Log.d("Forcing locale to " + locale.getLanguage());
		Configuration myConfig = new Configuration(newConfig);
		Locale.setDefault(locale);

		myConfig.locale = locale;
		getBaseContext().getResources().updateConfiguration(myConfig, getResources().getDisplayMetrics());

		// Send broadcast so that we can re-create activities
		String localeChangeAction = ProductFlavorFeatureConfiguration.getInstance().getActionForLocaleChangeEvent();
		if (localeChangeAction != null) {
			Intent intent = new Intent(localeChangeAction);
			sendBroadcast(intent);
		}
		super.onConfigurationChanged(newConfig);
	}


	private Locale getLocaleForWhiteLabels() {
		String localeIdentifier = PointOfSale.getPointOfSale().getLocaleIdentifier();
		String[] langCountryArray = localeIdentifier.split("_");
		return new Locale(langCountryArray[0], langCountryArray[1]);
	}

	public void setCrashlyticsMetadata() {

		Context context = getApplicationContext();

		Point displayPoint = AndroidUtils.getDisplaySize(context);
		Point screenPoint = AndroidUtils.getScreenSize(context);
		int screenDpi = AndroidUtils.getScreenDpi(context);
		String screenDpiClass = AndroidUtils.getScreenDensityClass(context);

		String screenSize = Integer.toString(screenPoint.x) + " x " + Integer.toString(screenPoint.y);
		String displaySize = Integer.toString(displayPoint.x) + " x " + Integer.toString(displayPoint.y);

		String localeId = PointOfSale.getPointOfSale().getLocaleIdentifier();
		String posId = PointOfSale.getPointOfSale().getPointOfSaleId().name();
		String api = appComponent().endpointProvider().getEndPoint().name();
		String gcmId = GCMRegistrationKeeper.getInstance(context).getRegistrationId(context);
		String mc1Cookie = DebugInfoUtils.getMC1CookieStr(context);

		ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		int memClass = am.getMemoryClass();

		Crashlytics.setString("screen size", screenSize);
		Crashlytics.setString("display size", displaySize);
		Crashlytics.setInt("screen dpi", screenDpi);
		Crashlytics.setString("screen dpi class", screenDpiClass);
		Crashlytics.setString("pos id", posId);
		Crashlytics.setString("locale id", localeId);
		Crashlytics.setString("mc1 cookie", mc1Cookie);
		Crashlytics.setString("api", api);
		Crashlytics.setInt("memory class", memClass);

		if (!gcmId.isEmpty()) {
			Crashlytics.setString("gcm token", gcmId);
		}
	}

	@Override
	public void onIDFALoaded(String idfa) {

	}

	@Override
	public void onIDFAFailed() {
		// ignore
	}

	public boolean isRobolectric() {
		return false;
	}

	private Observer<AbacusResponse> abacusSubscriber = new Observer<AbacusResponse>() {
		@Override
		public void onCompleted() {
			Log.d("AbacusReponse - onCompleted");
		}

		@Override
		public void onError(Throwable e) {
			// onError is called during debuging & cannot connect to dev endpoint
			// but we still want to modify the tests for debugging and QA purposes
			updateAbacus(new AbacusResponse());
			Log.d("AbacusReponse - onError", e);
		}

		@Override
		public void onNext(AbacusResponse abacusResponse) {
			updateAbacus(abacusResponse);
			Log.d("AbacusReponse - onNext");
		}
	};

	private void updateAbacus(AbacusResponse abacusResponse) {
		if (ExpediaBookingApp.isAutomation()) {
			return;
		}

		Db.setAbacusResponse(abacusResponse);
		// Modify the bucket values based on dev settings;
		if (BuildConfig.DEBUG) {
			for (int key : AbacusUtils.getActiveTests()) {
				Db.getAbacusResponse().updateABTestForDebug(key, SettingUtils.get(ExpediaBookingApp.this, String.valueOf(key), AbacusUtils.ABTEST_IGNORE_DEBUG));
			}
		}
	}

	public String generateAbacusGuid() {
		String guid = SettingUtils.get(ExpediaBookingApp.this, PREF_ABACUS_GUID, "");
		if (Strings.isEmpty(guid)) {
			guid = UUID.randomUUID().toString();
			SettingUtils.save(ExpediaBookingApp.this, PREF_ABACUS_GUID, guid);
		}
		Db.setAbacusGuid(guid);
		return guid;
	}
}
