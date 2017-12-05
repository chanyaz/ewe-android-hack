package com.expedia.bookings.activity;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Locale;

import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.text.format.DateUtils;

import com.activeandroid.ActiveAndroid;
import com.crashlytics.android.Crashlytics;
import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.dagger.AppComponent;
import com.expedia.bookings.dagger.AppModule;
import com.expedia.bookings.dagger.DaggerAppComponent;
import com.expedia.bookings.dagger.DaggerFlightComponent;
import com.expedia.bookings.dagger.DaggerHotelComponent;
import com.expedia.bookings.dagger.DaggerLXComponent;
import com.expedia.bookings.dagger.DaggerLaunchComponent;
import com.expedia.bookings.dagger.DaggerPackageComponent;
import com.expedia.bookings.dagger.DaggerRailComponent;
import com.expedia.bookings.dagger.DaggerTravelerComponent;
import com.expedia.bookings.dagger.DaggerTripComponent;
import com.expedia.bookings.dagger.FlightComponent;
import com.expedia.bookings.dagger.HotelComponent;
import com.expedia.bookings.dagger.LXComponent;
import com.expedia.bookings.dagger.LaunchComponent;
import com.expedia.bookings.dagger.PackageComponent;
import com.expedia.bookings.dagger.RailComponent;
import com.expedia.bookings.dagger.TravelerComponent;
import com.expedia.bookings.dagger.TripComponent;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.PushNotificationRegistrationResponse;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.country.CountryConfig;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.pos.PointOfSaleConfigHelper;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.featureconfig.SatelliteFeatureConfigManager;
import com.expedia.bookings.itin.services.FlightRegistrationHandler;
import com.expedia.bookings.notification.GCMRegistrationKeeper;
import com.expedia.bookings.notification.PushNotificationUtils;
import com.expedia.bookings.server.CrossContextHelper;
import com.expedia.bookings.server.EndPoint;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.AppStartupTimeLogger;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AbacusHelperUtils;
import com.expedia.bookings.utils.AccessibilityUtil;
import com.expedia.bookings.utils.CarnivalUtils;
import com.expedia.bookings.utils.CurrencyUtils;
import com.expedia.bookings.utils.DebugInfoUtils;
import com.expedia.bookings.utils.ExpediaDebugUtil;
import com.expedia.bookings.utils.FeatureToggleUtil;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.MockModeShim;
import com.expedia.bookings.utils.ShortcutUtils;
import com.expedia.bookings.utils.TuneUtils;
import com.facebook.FacebookSdk;
import com.facebook.applinks.AppLinkData;
import com.google.android.gms.common.GoogleApiAvailability;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.DebugUtils;
import com.mobiata.android.Log;
import com.mobiata.android.util.AdvertisingIdUtils;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.SettingUtils;
import com.mobiata.android.util.TimingLogger;
import com.mobiata.flightlib.data.sources.FlightStatsDbUtils;

import net.danlew.android.joda.JodaTimeAndroid;

import io.fabric.sdk.android.Fabric;

public class ExpediaBookingApp extends Application implements UncaughtExceptionHandler {
	// Don't change the actual string, updated identifier for clarity
	private static final String PREF_FIRST_LAUNCH = "PREF_FIRST_LAUNCH";

	public static final String PREF_LAST_VERSION_OF_APP_LAUNCHED = "PREF_LAST_VERSION_OF_APP_LAUNCHED";

	// For bug #2249 where we did not point at the production push server
	private static final String PREF_UPGRADED_TO_PRODUCTION_PUSH = "PREF_UPGRADED_TO_PRODUCTION_PUSH";

	private UncaughtExceptionHandler mOriginalUncaughtExceptionHandler;

	// Debug / test settings

	private static boolean sIsRobolectric = false;
	//64 memory class or lower is a shitty device
	private static boolean sIsDeviceShitty = false;
	private static boolean sIsInstrumentation = false;
	private static boolean sIsFirstLaunchEver = true;
	private static boolean sIsFirstLaunchOfAppVersion = true;

	public static boolean isFirstLaunchOfAppVersion() {
		return sIsFirstLaunchOfAppVersion;
	}

	public static boolean isFirstLaunchEver() {
		return sIsFirstLaunchEver;
	}

	public static boolean isAutomation() {
		return sIsRobolectric || sIsInstrumentation;
	}

	public static boolean isInstrumentation() {
		return sIsInstrumentation;
	}

	public static boolean isRobolectric() {
		return sIsRobolectric;
	}

	public static boolean isDeviceShitty() {
		return sIsDeviceShitty && !isAutomation();
	}

	public static void setIsInstrumentation(boolean isInstrumentation) {
		sIsInstrumentation = isInstrumentation;
	}

	public static void setIsRobolectric(boolean isRobolectric) {
		sIsRobolectric = isRobolectric;
	}

	private AppStartupTimeLogger appStartupTimeLogger;

	@Override
	public void onCreate() {
		TimingLogger startupTimer = new TimingLogger("ExpediaBookings", "startUp");

		// Initialize some parts of the code that require a Context
		initializePointOfSale();
		startupTimer.addSplit("PointOfSale Init");

		CountryConfig.loadCountryConfigs(getAssets());

		mAppComponent = DaggerAppComponent.builder()
			.appModule(new AppModule(this))
			.build();
		startupTimer.addSplit("Dagger AppModule created");

		appStartupTimeLogger = mAppComponent.appStartupTimeLogger();
		appStartupTimeLogger.setAppLaunchedTime(System.currentTimeMillis());

		// We want this first so that we set this as the Provider before anything tries to use Joda time
		JodaTimeAndroid.init(this);
		startupTimer.addSplit("Joda TZ Provider Init");

		super.onCreate();
		startupTimer.addSplit("super.onCreate()");

		ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		int memClass = am.getMemoryClass();
		sIsDeviceShitty = memClass <= 64;

		if (!isAutomation()) {
			Fabric.with(this, new Crashlytics());
			startupTimer.addSplit("Crashlytics started.");
		}

		if (mAppComponent.endpointProvider().getEndPoint() == EndPoint.MOCK_MODE) {
			MockModeShim.initMockWebServer(this);
			startupTimer.addSplit("Mock mode init");
		}

		FacebookSdk.sdkInitialize(this);
		startupTimer.addSplit("FacebookSdk started.");

		PicassoHelper.init(this, appComponent().okHttpClient());
		startupTimer.addSplit("Picasso started.");

		ActiveAndroid.initialize(this);

		startupTimer.addSplit("ActiveAndroid Init");

		boolean isLogEnablerInstalled = BuildConfig.DEBUG ||
			DebugUtils.isLogEnablerInstalled(this) ||
			ExpediaDebugUtil.isEBToolApkInstalled(this);
		Log.configureLogging("ExpediaBookings", isLogEnablerInstalled);

		startupTimer.addSplit("Logger Init");

		try {
			if (BuildConfig.DEBUG) {
				FlightStatsDbUtils.setUpgradeCutoff(DateUtils.DAY_IN_MILLIS); // 1 day cutoff for upgrading FS.db
			}

			FlightStatsDbUtils.createDatabaseIfNotExists(this, BuildConfig.RELEASE);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}

		startupTimer.addSplit("FS.db Init");

		// Pull down advertising ID
		if (!isAutomation()) {
			AdvertisingIdUtils.loadIDFA(this, null);
			startupTimer.addSplit("Load Advertising Id");
		}

		// Init required for Omniture tracking
		OmnitureTracking.init(this);
		if (!isAutomation()) {
			// Setup Omniture for tracking crashes
			mOriginalUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
			Thread.setDefaultUncaughtExceptionHandler(this);
		}

		startupTimer.addSplit("Omniture Init");

		AbacusHelperUtils.generateAbacusGuid(this);
		startupTimer.addSplit("Generate Abacus GUID");

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

		ItineraryManager.getInstance().init(this);
		// Load data from Disk
		ItineraryManager.getInstance().startSync(false, true, false);
		startupTimer.addSplit("ItineraryManager Init/Load");

		startupTimer.addSplit("User upgraded to use AccountManager (if needed)");

		if (!isAutomation()) {
			AppLinkData.fetchDeferredAppLinkData(this,
				new AppLinkData.CompletionHandler() {
					@Override
					public void onDeferredAppLinkDataFetched(AppLinkData appLinkData) {
						// applinkData is null in case it is not a deferred deeplink.
						if (appLinkData != null && appLinkData.getTargetUri() != null) {
							Log.v("Facebook Deferred Deeplink: ", appLinkData.getTargetUri().toString());
							Intent intent = new Intent();
							intent.setData(appLinkData.getTargetUri());
							intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							intent.setComponent(new ComponentName(BuildConfig.APPLICATION_ID,
								"com.expedia.bookings.activity.DeepLinkRouterActivity"));
							startActivity(intent);

							TuneUtils.setFacebookReferralUrl(String.valueOf(appLinkData.getTargetUri()));
						}
					}
				}
			);
		}

		// 2249: We were not sending push registrations to the prod push server
		// If we are upgrading from a previous version we will send an unregister to the test push server
		// We also don't want to bother if the user has never launched the app before
		if (BuildConfig.RELEASE
			&& !SettingUtils.get(this, PREF_UPGRADED_TO_PRODUCTION_PUSH, false)
			&& SettingUtils.get(this, PREF_FIRST_LAUNCH, false)) {

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

		if (SettingUtils.get(ExpediaBookingApp.this, PREF_FIRST_LAUNCH, true)) {
			startupTimer.addSplit("AdTracker first launch tracking");
		}

		// 2249: We don't need to unregister if this is the user's first launch
		if (!SettingUtils.get(this, PREF_FIRST_LAUNCH, false)) {
			SettingUtils.save(ExpediaBookingApp.this, PREF_UPGRADED_TO_PRODUCTION_PUSH, true);
		}

		// If the current POS needs flight routes, update our data
		if (PointOfSale.getPointOfSale().displayFlightDropDownRoutes()) {
			CrossContextHelper.updateFlightRoutesData(getApplicationContext(), false);
			startupTimer.addSplit("Flight routes download started");
		}

		CurrencyUtils.initMap(this);
		startupTimer.addSplit("Currency Utils init");
		startupTimer.dumpToLog();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
			ShortcutUtils.INSTANCE.initialize(getBaseContext());
		}

		initializeFeatureConfig();
		CarnivalUtils.getInstance().initialize(this);

		if (FeatureToggleUtil.isUserBucketedAndFeatureEnabled(this, AbacusUtils.TripsNewFlightAlerts,
			R.string.preference_enable_trips_flight_alerts)) {
			FlightRegistrationHandler flightRegistrationHandler = appComponent().flightRegistrationService();
			flightRegistrationHandler.setup();
			appComponent().userLoginStateChangedModel().getUserLoginStateChanged()
				.subscribe(flightRegistrationHandler.getUserLoginStateChanged());
		}
	}

	private void initializeFeatureConfig() {
		SatelliteFeatureConfigManager.refreshFeatureConfigIfStale(this);
	}

	private void initializePointOfSale() {
		PointOfSaleConfigHelper configHelper = new PointOfSaleConfigHelper(getAssets(),
			ProductFlavorFeatureConfiguration.getInstance().getPOSConfigurationPath());

		String pointOfSaleKey = SettingUtils.get(this, getString(R.string.PointOfSaleKey), null);

		pointOfSaleKey = PointOfSale.init(configHelper, pointOfSaleKey, false);

		SettingUtils.save(this, getString(R.string.PointOfSaleKey), pointOfSaleKey);

	}

	public void updateFirstLaunchAndUpdateSettings() {
		if (SettingUtils.get(ExpediaBookingApp.this, PREF_FIRST_LAUNCH, true)) {
			sIsFirstLaunchEver = true;
			SettingUtils.save(ExpediaBookingApp.this, PREF_FIRST_LAUNCH, false);
		}
		else {
			sIsFirstLaunchEver = false;
		}

		sIsFirstLaunchOfAppVersion = isFirstLaunchOfNewAppVersion();
		SettingUtils.save(ExpediaBookingApp.this, PREF_LAST_VERSION_OF_APP_LAUNCHED, BuildConfig.VERSION_NAME);
	}

	private boolean isFirstLaunchOfNewAppVersion() {
		String lastVersionOfAppLaunched = SettingUtils
			.get(ExpediaBookingApp.this, PREF_LAST_VERSION_OF_APP_LAUNCHED, "");
		return !BuildConfig.VERSION_NAME.equals(lastVersionOfAppLaunched);
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		setCrashlyticsMetadata();

		// Call the original exception handler - probably crashlytics
		mOriginalUncaughtExceptionHandler.uncaughtException(thread, ex);

		OmnitureTracking.trackCrash(ex);
	}

	//////////////////////////////////////////////////////////////////////////
	// Dagger instances

	private AppComponent mAppComponent;
	private HotelComponent mHotelComponent;
	private RailComponent mRailComponent;
	private PackageComponent mPackageComponent;
	private TripComponent mTripComponent;
	private FlightComponent mFlightComponent;
	private TravelerComponent mTravelerComponent;
	private LaunchComponent mLaunchComponent;

	private LXComponent mLXComponent;
	private LXComponent mLXTestComponent;

	public AppComponent appComponent() {
		return mAppComponent;
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

	public void defaultPackageComponents() {
		setPackageComponent(DaggerPackageComponent.builder()
			.appComponent(mAppComponent)
			.build());
	}

	public void defaultTripComponents() {
		setTripComponent(DaggerTripComponent.builder()
			.appComponent(mAppComponent)
			.build());
	}

	public void setRailComponent(RailComponent railComponent) {
		mRailComponent = railComponent;
	}

	public RailComponent railComponent() {
		return mRailComponent;
	}

	public void defaultRailComponents() {
		setRailComponent(DaggerRailComponent.builder()
			.appComponent(mAppComponent)
			.build());
	}

	public TravelerComponent travelerComponent() {
		return mTravelerComponent;
	}

	public void setTravelerComponent(TravelerComponent travelerComponent) {
		mTravelerComponent = travelerComponent;
	}

	public void setPackageComponent(PackageComponent packageComponent) {
		mPackageComponent = packageComponent;
	}

	public void setTripComponent(TripComponent tripComponent) {
		mTripComponent = tripComponent;
	}

	public PackageComponent packageComponent() {
		return mPackageComponent;
	}

	public TripComponent tripComponent() {
		return mTripComponent;
	}

	public void setFlightComponent(FlightComponent flightComponent) {
		mFlightComponent = flightComponent;
	}

	public FlightComponent flightComponent() {
		return mFlightComponent;
	}

	public void defaultTravelerComponent() {
		setTravelerComponent(DaggerTravelerComponent.builder().appComponent(appComponent()).build());
	}

	public void defaultFlightComponents() {
		setFlightComponent(DaggerFlightComponent.builder()
			.appComponent(mAppComponent)
			.build());
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

	public LXComponent lxTestComponent() {
		return mLXTestComponent;
	}

	public LXComponent lxComponent() {
		return mLXComponent;
	}

	public void defaultLaunchComponents() {
		setLaunchComponent(DaggerLaunchComponent.builder().appComponent(appComponent()).build());
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
		//Update Locale
		AdTracker.updatePOS();
		// Clear out saved flight route data
		Db.deleteCachedFlightRoutes(getBaseContext());
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

		String screenSize = Integer.toString(screenPoint.x) + "x" + Integer.toString(screenPoint.y);
		String displaySize = Integer.toString(displayPoint.x) + "x" + Integer.toString(displayPoint.y);

		String localeId = PointOfSale.getPointOfSale().getLocaleIdentifier();
		String posId = PointOfSale.getPointOfSale().getPointOfSaleId().name();
		String api = appComponent().endpointProvider().getEndPoint().name();
		String gcmId = GCMRegistrationKeeper.getInstance(context).getRegistrationId(context);
		String mc1Cookie = DebugInfoUtils.getMC1CookieStr(context);
		String abacusGuid = Db.getAbacusGuid();
		boolean isAccessibilityOn = AccessibilityUtil.isTalkBackEnabled(this);
		int gpsVersion;
		try {
			gpsVersion = getPackageManager()
				.getPackageInfo(GoogleApiAvailability.GOOGLE_PLAY_SERVICES_PACKAGE, 0).versionCode;
		}
		catch (PackageManager.NameNotFoundException e) {
			gpsVersion = 0;
		}

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
		Crashlytics.setString("abacus guid", abacusGuid);
		Crashlytics.setBool("a11y active", isAccessibilityOn);
		Crashlytics.setInt("google play services version", gpsVersion);

		if (!gcmId.isEmpty()) {
			Crashlytics.setString("gcm token", gcmId);
		}
	}
}
