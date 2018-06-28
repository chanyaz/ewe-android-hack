package com.expedia.bookings.activity;

import java.io.File;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.arch.lifecycle.Lifecycle;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RawRes;
import android.support.annotation.VisibleForTesting;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.clarisite.mobile.ClarisiteAgent;
import com.clarisite.mobile.exceptions.EyeViewException;
import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.animation.ActivityTransitionCircularRevealHelper;
import com.expedia.bookings.appstartup.persistence.SharedPreferencesSplashScreenAnimationProvider;
import com.expedia.bookings.appstartup.persistence.SplashScreenAnimationProvider;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.abacus.AbacusEvaluateQuery;
import com.expedia.bookings.data.abacus.AbacusResponse;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.abacus.AbacusVariant;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.data.user.UserStateManager;
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.features.Features;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.tracking.RouterToLaunchTimeLogger;
import com.expedia.bookings.tracking.RouterToOnboardingTimeLogger;
import com.expedia.bookings.tracking.RouterToSignInTimeLogger;
import com.expedia.bookings.utils.AbacusHelperUtils;
import com.expedia.bookings.utils.ClearPrivateDataUtil;
import com.expedia.bookings.utils.LaunchNavBucketCache;
import com.expedia.bookings.utils.TrackingUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.utils.UserAccountRefresher;
import com.expedia.bookings.utils.navigation.NavUtils;
import com.facebook.appevents.AppEventsLogger;
import com.mobiata.android.util.SettingUtils;
import com.mobiata.android.util.TimingLogger;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.reactivex.Observer;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableObserver;

import static com.expedia.bookings.utils.FeatureUtilKt.isGlassboxEnabled;

public class RouterActivity extends AppCompatActivity implements UserAccountRefresher.IUserAccountRefreshListener {

	@Inject
	UserStateManager userStateManager;

	@Inject
	RouterToOnboardingTimeLogger routerToOnboardingTimeLogger;

	@Inject
	RouterToLaunchTimeLogger routerToLaunchTimeLogger;

	@Inject
	RouterToSignInTimeLogger routerToSignInTimeLogger;

	@InjectView(R.id.root_layout)
	protected ConstraintLayout rootLayout;

	@InjectView(R.id.start_animation_view)
	LottieAnimationView startAnimationView;

	@InjectView(R.id.loop_animation_view)
	LottieAnimationView loopAnimationView;

	@InjectView(R.id.end_animation_view)
	LottieAnimationView endAnimationView;

	protected SplashScreenAnimationProvider splashScreenAnimationProvider;

	boolean loadSignInView = false;
	boolean userLoadIsComplete;
	protected boolean splashLoadingAnimationShouldRun;
	protected boolean splashLoadingWasInterrupted;

	private enum LaunchDestination {
		SIGN_IN,
		LAUNCH_SCREEN
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		TimingLogger startupTimer = new TimingLogger("Router Activity", " Router on create startUp");
		setIfSplashLoadingAnimationShouldRun();

		if (splashLoadingAnimationShouldRun) {
			setTheme(R.style.SplashThemeForLoadingAnimation);
			super.onCreate(savedInstanceState);
			setupActivityForSplashLoadingAnimationAndPlayAnimation();
		}
		else {
			super.onCreate(savedInstanceState);
		}
		startupTimer.addSplit("Time taken to decide which intent to trigger");

		Ui.getApplication(this).appComponent().inject(this);

		startupTimer.addSplit("Injecting router activity to app component");

		setTimeLogging();

		// Track the app loading
		OmnitureTracking.trackAppLoading(this);

		startupTimer.addSplit("Track omniture app loading");

		// Update data
		ItineraryManager.getInstance().startSync(false, false, true);

		startupTimer.addSplit("ItineraryManager sync");

		//Hi Facebook!
		facebookInstallTracking();
		startupTimer.addSplit("Facebook install tracking");

		cleanupOldCookies();
		cleanupOldSuggestions();

		startupTimer.addSplit("Cleanup cookies and suggestions");

		Ui.getApplication(this).updateFirstLaunchAndUpdateSettings();

		startupTimer.addSplit("Updating Launch settings");

		userStateManager.ensureUserStateSanity(this);

		startupTimer.addSplit("Ensuring sanity of users");
		startupTimer.dumpToLog();

		if (isGlassboxEnabled()) {
			glassBoxStart();
		}
	}

	protected void setIfSplashLoadingAnimationShouldRun() {
		splashScreenAnimationProvider =
			new SharedPreferencesSplashScreenAnimationProvider(this);

		splashLoadingAnimationShouldRun =
			ProductFlavorFeatureConfiguration.getInstance().isSplashLoadingAnimationEnabled() &&
				Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP &&
				splashScreenAnimationProvider.shouldSplashAnimationRun();

		if (splashLoadingAnimationShouldRun) {
			disableSplashScreenAnimationFromRunningOnNextLaunchToPreventPossibleCrashes();
		}
	}

	private void disableSplashScreenAnimationFromRunningOnNextLaunchToPreventPossibleCrashes() {
		splashScreenAnimationProvider.put(false);
	}

	private void enableSplashScreenAnimationForNextLaunchBecauseAnimationWasSuccessfullyCompleted() {
		splashScreenAnimationProvider.put(Features.Companion.getAll().getShowSplashLoadingAnimationScreen().enabled());
	}

	public void setupActivityForSplashLoadingAnimationAndPlayAnimation() {
		setContentView(R.layout.activity_router_launch_animation);
		ButterKnife.inject(this);

		setupLottieSplashLoadingAnimations();
		startAnimationView.playAnimation();
	}

	private void glassBoxStart() {
		try {
			ClarisiteAgent.start();
		}
		catch (EyeViewException e) {
			e.printStackTrace();
		}
	}

	private void setupLottieSplashLoadingAnimations() {
		initStartAnimation(R.raw.splash_intro_60);
		initLoopAnimation(R.raw.splash_loop_60);
		initEndAnimation(R.raw.splash_exit_60);
	}

	private void initStartAnimation(@RawRes int animationRes) {
		startAnimationView.setAnimation(animationRes);
		startAnimationView.addAnimatorListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				super.onAnimationEnd(animation);
				startAnimationView.setVisibility(View.GONE);

				if (userLoadIsComplete) {
					endAnimationView.setVisibility(View.VISIBLE);
					endAnimationView.playAnimation();
				}
				else {
					loopAnimationView.setVisibility(View.VISIBLE);
					loopAnimationView.playAnimation();
				}
			}
		});
	}

	private void initLoopAnimation(@RawRes int animationRes) {
		loopAnimationView.setAnimation(animationRes);
		loopAnimationView.setRepeatCount(LottieDrawable.INFINITE);
		loopAnimationView.addAnimatorListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationRepeat(Animator animation) {
				super.onAnimationRepeat(animation);
				if (userLoadIsComplete) {
					loopAnimationView.cancelAnimation();
					loopAnimationView.setVisibility(View.GONE);
					endAnimationView.setVisibility(View.VISIBLE);
					endAnimationView.playAnimation();
				}
			}
		});
	}

	private void initEndAnimation(@RawRes int animationRes) {
		endAnimationView.setAnimation(animationRes);
		endAnimationView.addAnimatorListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				super.onAnimationEnd(animation);
				launchNextActivityWithSplashLoadingAnimation(endAnimationView);
			}
		});
	}

	@VisibleForTesting
	protected void notifySplashLoadingAnimationsThatDataHasLoaded() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (loopAnimationView.isAnimating()) {
					loopAnimationView.removeAllAnimatorListeners();
					AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
					fadeOut.setDuration(600);
					loopAnimationView.startAnimation(fadeOut);

					AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
					fadeIn.setDuration(400);
					endAnimationView.setVisibility(View.VISIBLE);
					endAnimationView.startAnimation(fadeIn);
					endAnimationView.playAnimation();
				}
				else {
					userLoadIsComplete = true;
				}
			}
		});
	}

	private void setTimeLogging() {
		routerToOnboardingTimeLogger.setStartTime();
		routerToLaunchTimeLogger.setStartTime();
		routerToSignInTimeLogger.setStartTime();
	}

	@Override
	public void onUserAccountRefreshed() {
		initializeAndDownloadAbacusTests();
	}

	private void initializeAndDownloadAbacusTests() {
		TrackingUtils.initializeTracking(this.getApplication());

		boolean isUsersFirstLaunchOfApp = ExpediaBookingApp.isFirstLaunchEver();
		boolean isNewVersionOfApp = ExpediaBookingApp.isFirstLaunchOfAppVersion();
		boolean userNotLoggedIn = !userStateManager.isUserAuthenticated();
		loadSignInView = (isUsersFirstLaunchOfApp || isNewVersionOfApp) && userNotLoggedIn;
		PointOfSale pos = PointOfSale.getPointOfSale();

		AbacusEvaluateQuery query = new AbacusEvaluateQuery(Db.sharedInstance.getAbacusGuid(), pos.getTpid(), 0);
		if (ProductFlavorFeatureConfiguration.getInstance().isAbacusTestEnabled()) {
			query.addExperiment(AbacusUtils.EBAndroidAppFlightAdvanceSearch.getKey());
			query.addExperiment(AbacusUtils.EBAndroidAppFlightAATest.getKey());
			query.addExperiment(AbacusUtils.PackagesTitleChange.getKey());
			query.addExperiment(AbacusUtils.EBAndroidAppFlightSearchSuggestionLabel.getKey());
			query.addExperiment(AbacusUtils.EBAndroidAppAPIMAuth.getKey());
			query.addExperiment(AbacusUtils.EBAndroidAppFlightSubpubChange.getKey());
			query.addExperiment(AbacusUtils.EBAndroidAppFlightsEvolable.getKey());
			query.addExperiment(AbacusUtils.EBAndroidAppCarsAATest.getKey());
			query.addExperiment(AbacusUtils.EBAndroidAppSoftPromptLocation.getKey());
			query.addExperiment(AbacusUtils.EBAndroidAppFlightsGreedySearchCall.getKey());
			query.addExperiment(PointOfSale.getPointOfSale().getRailsWebViewABTestID().getKey());
			query.addExperiment(AbacusUtils.EBAndroidAppFlightsAPIKongEndPoint.getKey());
			query.addExperiment(AbacusUtils.EBAndroidAppFlightsRecentSearch.getKey());
			query.addExperiment(AbacusUtils.EBAndroidAppBrandColors.getKey());
			query.addExperiment(AbacusUtils.EBAndroidAppBottomNavTabs.getKey());
			query.addExperiment(AbacusUtils.HotelEarn2xMessaging.getKey());
			query.addExperiment(AbacusUtils.EBAndroidAppAccountsAPIKongEndPoint.getKey());
			query.addExperiment(AbacusUtils.DisableSignInPageAsFirstScreen.getKey());
			query.addExperiment(AbacusUtils.MesoAd.getKey());
			query.addExperiment(AbacusUtils.HomeScreenDisplayLogic.getKey());
			query.addExperiment(AbacusUtils.JoinRewardsLaunchCard.getKey());
			query.addExperiment(AbacusUtils.EBAndroidAppPackagesFFPremiumClass.getKey());
			query.addExperiment(AbacusUtils.EBAndroidAppPackagesWebviewFHC.getKey());
			query.addExperiment(AbacusUtils.TripFoldersFragment.getKey());
			query.addExperiment(AbacusUtils.EBAndroidAppFlightsContentHighlightInTypeahead.getKey());
			query.addExperiment(AbacusUtils.EBAndroidAppPackagesAATest.getKey());
			query.addExperiment(AbacusUtils.EBAndroidAppFlightByotSearch.getKey());
			query.addExperiment(AbacusUtils.EBAndroidAppFlightsHolidayCalendar.getKey());
			query.addExperiment(AbacusUtils.EBAndroidAppFLightLoadingStateV1.getKey());
			query.addExperiment(AbacusUtils.EBAndroidAppAccountNewSignIn.getKey());
			query.addExperiment(AbacusUtils.EBAndroidAppPackagesSearchFormRenameToFrom.getKey());
		}

		Ui.getApplication(this).appComponent().abacus()
			.downloadBucket(query, evaluatePreLaunchABTestsSubscriber, 3, TimeUnit.SECONDS);

		if (BuildConfig.DEBUG && Features.Companion.getAll().getProductionAbacus().enabled()) {
			Handler handler = new Handler(Looper.getMainLooper());
			handler.post(new Runnable() {
				public void run() {
					Toast.makeText(RouterActivity.this.getApplication(),
						"Production Abacus is enabled! Go to Settings to change it", Toast.LENGTH_LONG).show();
				}
			});
		}
	}

	private Observer<AbacusResponse> evaluatePreLaunchABTestsSubscriber = new DisposableObserver<AbacusResponse>() {

		@Override
		public void onComplete() {
		}

		@Override
		public void onError(Throwable e) {
			if (BuildConfig.DEBUG) {
				AbacusHelperUtils.updateAbacus(new AbacusResponse(), RouterActivity.this);
				cacheLaunchNavBucket(0);
			}
			routeToNextScreen();
		}

		@Override
		public void onNext(AbacusResponse abacusResponse) {
			cacheLaunchNavBucket(abacusResponse.variateForTest(AbacusUtils.EBAndroidAppBottomNavTabs));
			AbacusHelperUtils.updateAbacus(abacusResponse, RouterActivity.this);
			routeToNextScreen();
		}
	};

	private void cacheLaunchNavBucket(int testValue) {
		if (BuildConfig.DEBUG) {
			int debugValue = SettingUtils
				.get(getApplicationContext(), String.valueOf(AbacusUtils.EBAndroidAppBottomNavTabs.getKey()),
					AbacusVariant.NO_BUCKET.getValue());
			LaunchNavBucketCache.cacheBucket(RouterActivity.this, debugValue);
		}
		else {
			LaunchNavBucketCache.cacheBucket(RouterActivity.this, testValue);
		}
	}

	private void routeToNextScreen() {
		if (splashLoadingAnimationShouldRun) {
			notifySplashLoadingAnimationsThatDataHasLoaded();
		}
		else {
			launchNextActivityWithStaticScreen();
		}
	}

	/**
	 * Tell facebook we installed the app every time we launch!
	 * This is asynchronous, and after we get a success message back from FB this call no longer does anything at all.
	 */
	@VisibleForTesting
	protected void facebookInstallTracking() {
		AppEventsLogger.activateApp(this.getApplication());
	}

	private boolean isFacebookTrackingEnabled() {
		return Features.Companion.getAll().getFacebookAdTracking().enabled();
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
	protected void onResume() {
		super.onResume();
		if (splashLoadingWasInterrupted) {
			if (splashLoadingAnimationShouldRun) {
				endAnimationView.playAnimation();
			}
			else {
				launchNextActivityWithStaticScreen();
			}
		}
	}

	public void launchNextActivityWithSplashLoadingAnimation(View sharedView) {
		if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
			LaunchDestination destination = getLaunchDestination();
			int revealX = (int) (sharedView.getX() + sharedView.getWidth() / 2);
			int revealY = (int) (sharedView.getY() + sharedView.getHeight() / 2);
			int backgroundColor = ActivityTransitionCircularRevealHelper.Companion.getViewBackgroundColor(rootLayout);

			ActivityOptionsCompat options = ActivityTransitionCircularRevealHelper.Companion
				.getSceneTransitionAnimation(this, sharedView, "transition");

			ActivityTransitionCircularRevealHelper.Companion.subscribeToAnimationEnd(
				new DisposableCompletableObserver() {
					@Override
					public void onComplete() {
						enableSplashScreenAnimationForNextLaunchBecauseAnimationWasSuccessfullyCompleted();
						finish();
					}

					@Override
					public void onError(Throwable e) {
						finish();
					}
				});

			if (showNewUserOnboarding()) {
				NavUtils.goToOnboardingScreen(this, options.toBundle(), revealX, revealY, backgroundColor);
			}
			else if (destination == LaunchDestination.SIGN_IN) {
				NavUtils.goToSignIn(RouterActivity.this);
			}
			else {
				NavUtils.goToLaunchScreen(RouterActivity.this, options.toBundle(), revealX, revealY, backgroundColor);
			}

			overridePendingTransition(R.anim.hold, R.anim.hold);
		}
		else {
			splashLoadingWasInterrupted = true;
		}
	}

	public void launchNextActivityWithStaticScreen() {
		if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
			final LaunchDestination destination = getLaunchDestination();
			Handler handler = new Handler(Looper.getMainLooper());
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					if (showNewUserOnboarding()) {
						NavUtils.goToOnboardingScreen(RouterActivity.this);
					}
					else if (destination == LaunchDestination.LAUNCH_SCREEN) {
						NavUtils.goToLaunchScreen(RouterActivity.this);
					}
					else {
						NavUtils.goToSignIn(RouterActivity.this);
					}

					finish();
					overridePendingTransition(R.anim.hold, R.anim.slide_down_splash);
				}
			}, getResources().getInteger(android.R.integer.config_longAnimTime));
		}
		else {
			splashLoadingWasInterrupted = true;
		}
	}

	private LaunchDestination getLaunchDestination() {
		return (loadSignInView &&
			!ExpediaBookingApp.isInstrumentation() &&
			!AbacusFeatureConfigManager.isBucketedForTest(this, AbacusUtils.DisableSignInPageAsFirstScreen))
			? LaunchDestination.SIGN_IN
			: LaunchDestination.LAUNCH_SCREEN;
	}

	private boolean showNewUserOnboarding() {
		return ExpediaBookingApp.isFirstLaunchEver() && ProductFlavorFeatureConfiguration.getInstance()
			.isAppIntroEnabled();
	}
}
