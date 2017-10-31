package com.expedia.bookings.activity;

import java.io.File;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.abacus.AbacusEvaluateQuery;
import com.expedia.bookings.data.abacus.AbacusResponse;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.data.user.UserStateManager;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AbacusHelperUtils;
import com.expedia.bookings.utils.ClearPrivateDataUtil;
import com.expedia.bookings.utils.ProWizardBucketCache;
import com.expedia.bookings.utils.TrackingUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.utils.UserAccountRefresher;
import com.expedia.bookings.utils.navigation.NavUtils;
import com.facebook.appevents.AppEventsLogger;
import com.mobiata.android.util.SettingUtils;

import rx.Observer;

public class RouterActivity extends Activity implements UserAccountRefresher.IUserAccountRefreshListener {
	boolean loadSignInView = false;
	private UserStateManager userStateManager;

	private enum LaunchDestination {
		SIGN_IN,
		LAUNCH_SCREEN
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		userStateManager = Ui.getApplication(this).appComponent().userStateManager();

		// Track the app loading
		OmnitureTracking.trackAppLoading(this);

		// Update data
		ItineraryManager.getInstance().startSync(false, false, true);

		//Hi Facebook!
		facebookInstallTracking();

		cleanupOldCookies();
		cleanupOldSuggestions();

		Ui.getApplication(this).updateFirstLaunchAndUpdateSettings();

		userStateManager.ensureUserStateSanity(this);
	}

	private void launchOpeningView() {
		TrackingUtils.initializeTracking(this.getApplication());

		boolean isUsersFirstLaunchOfApp = ExpediaBookingApp.isFirstLaunchEver();
		boolean isNewVersionOfApp = ExpediaBookingApp.isFirstLaunchOfAppVersion();
		boolean userNotLoggedIn = !userStateManager.isUserAuthenticated();
		loadSignInView = (isUsersFirstLaunchOfApp || isNewVersionOfApp) && userNotLoggedIn;
		PointOfSale pos = PointOfSale.getPointOfSale();

		AbacusEvaluateQuery query = new AbacusEvaluateQuery(Db.getAbacusGuid(), pos.getTpid(), 0);
		if (ProductFlavorFeatureConfiguration.getInstance().isAbacusTestEnabled()) {
			query.addExperiment(AbacusUtils.EBAndroidAppShowMemberPricingCardOnLaunchScreen.getKey());
			query.addExperiment(AbacusUtils.EBAndroidAppShowAirAttachMessageOnLaunchScreen.getKey());
			query.addExperiment(AbacusUtils.EBAndroidAppFlightAdvanceSearch.getKey());
			query.addExperiment(AbacusUtils.EBAndroidAppFlightAATest.getKey());
			query.addExperiment(AbacusUtils.PackagesTitleChange.getKey());
			query.addExperiment(AbacusUtils.EBAndroidAppFlightSearchSuggestionLabel.getKey());
			query.addExperiment(AbacusUtils.EBAndroidAppFlightSuggestionOnOneCharacter.getKey());
			query.addExperiment(AbacusUtils.EBAndroidAppAPIMAuth.getKey());
			query.addExperiment(AbacusUtils.EBAndroidAppFlightSubpubChange.getKey());
			query.addExperiment(AbacusUtils.EBAndroidAppFlightsEvolable.getKey());
			query.addExperiment(AbacusUtils.EBAndroidAppFlightTravelerFormRevamp.getKey());
			query.addExperiment(AbacusUtils.EBAndroidAppCarsAATest.getKey());
			query.addExperiment(AbacusUtils.EBAndroidAppLocaleBasedDateFormatting.getKey());
			query.addExperiment(AbacusUtils.ProWizardTest.getKey());
			query.addExperiment(AbacusUtils.EBAndroidAppSoftPromptLocation.getKey());
			query.addExperiment(PointOfSale.getPointOfSale().getRailsWebViewABTestID().getKey());
			query.addExperiment(AbacusUtils.EBAndroidAppFlightsSearchResultCaching.getKey());
			query.addExperiment(AbacusUtils.EBAndroidAppAccountSinglePageSignUp.getKey());
		}

		Ui.getApplication(this).appComponent().abacus()
				.downloadBucket(query, evaluatePreLaunchABTestsSubscriber, 3, TimeUnit.SECONDS);
	}

	private Observer<AbacusResponse> evaluatePreLaunchABTestsSubscriber = new Observer<AbacusResponse>() {

		@Override
		public void onCompleted() {
		}

		@Override
		public void onError(Throwable e) {
			if (BuildConfig.DEBUG) {
				AbacusHelperUtils.updateAbacus(new AbacusResponse(), RouterActivity.this);
				cacheProWizardBucket(0);
			}
			launchScreenSelection();
		}

		@Override
		public void onNext(AbacusResponse abacusResponse) {
			cacheProWizardBucket(abacusResponse.variateForTest(AbacusUtils.ProWizardTest));
			AbacusHelperUtils.updateAbacus(abacusResponse, RouterActivity.this);
			launchScreenSelection();
		}
	};

	private void launchScreenSelection() {
		if (loadSignInView && !ExpediaBookingApp.isInstrumentation()) {
			showSplashThenLaunchOpeningView(LaunchDestination.SIGN_IN);
		}
		else {
			showSplashThenLaunchOpeningView(LaunchDestination.LAUNCH_SCREEN);
		}
	}

	private void finishActivity() {
		finish();
		overridePendingTransition(R.anim.hold, R.anim.slide_down_splash);
	}

	/**
	 * Tell facebook we installed the app every time we launch!
	 * This is asynchronous, and after we get a success message back from FB this call no longer does anything at all.
	 */
	private void facebookInstallTracking() {
		AppEventsLogger.activateApp(this);
	}

	private static final String COOKIE_FILE_V2 = "cookies-2.dat";
	private static final String COOKIE_FILE_V3 = "cookies-3.dat";

	private void cleanupOldCookies() {
		String[] files = new String[] {
			COOKIE_FILE_V2,
			COOKIE_FILE_V3,
		};
		// Nuke app data if old files exist
		// Delete old cookie files
		boolean cleanedSomething = false;
		for (String file : files) {
			File old = getFileStreamPath(file);
			if (old.exists()) {
				cleanedSomething = true;
				old.delete();
			}
		}

		if (cleanedSomething) {
			ClearPrivateDataUtil.clear(this);
		}
	}

	public static final String RECENT_ROUTES_LX_LOCATION_FILE_BEFORE_V4 = "recent-lx-city-list.dat";
	public static final String RECENT_ROUTES_CARS_LOCATION_FILE_BEFORE_V4 = "recent-cars-airport-routes-list.dat";

	private void cleanupOldSuggestions() {
		String[] files = new String[] {
			RECENT_ROUTES_LX_LOCATION_FILE_BEFORE_V4,
			RECENT_ROUTES_CARS_LOCATION_FILE_BEFORE_V4,
		};
		// Nuke old suggestions if they exist. Yay Happy new year, er I mean new SuggestionsV4 !!!
		for (String file : files) {
			File old = getFileStreamPath(file);
			if (old.exists()) {
				old.delete();
			}
		}
	}

	@Override
	public void onUserAccountRefreshed() {
		launchOpeningView();
	}

	private void showSplashThenLaunchOpeningView(final LaunchDestination destination) {
		Handler handler = new Handler(Looper.getMainLooper());
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (showNewUserOnboarding()) {
					ProductFlavorFeatureConfiguration.getInstance().launchAppIntroScreen(RouterActivity.this);
				}
				else {
					if (destination == LaunchDestination.LAUNCH_SCREEN) {
						NavUtils.goToLaunchScreen(RouterActivity.this);
					}
					else {
						NavUtils.goToSignIn(RouterActivity.this);
					}
				}
				finishActivity();
			}
		}, getResources().getInteger(android.R.integer.config_longAnimTime));
	}

	private boolean showNewUserOnboarding() {
		return ExpediaBookingApp.isFirstLaunchEver() && ProductFlavorFeatureConfiguration.getInstance().isAppIntroEnabled();
	}

	private void cacheProWizardBucket(int testValue) {
		if (BuildConfig.DEBUG) {
			int debugValue = SettingUtils.get(getApplicationContext(),
					String.valueOf(AbacusUtils.ProWizardTest.getKey()), AbacusUtils.ABTEST_IGNORE_DEBUG);
			ProWizardBucketCache.cacheBucket(RouterActivity.this, debugValue);
		}
		else {
			ProWizardBucketCache.cacheBucket(RouterActivity.this, testValue);
		}
	}
}
