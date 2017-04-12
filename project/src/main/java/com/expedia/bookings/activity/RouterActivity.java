package com.expedia.bookings.activity;

import java.io.File;
import java.util.concurrent.TimeUnit;

import android.animation.Animator;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.abacus.AbacusEvaluateQuery;
import com.expedia.bookings.data.abacus.AbacusResponse;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AbacusHelperUtils;
import com.expedia.bookings.utils.ClearPrivateDataUtil;
import com.expedia.bookings.utils.FeatureToggleUtil;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.TrackingUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.utils.UserAccountRefresher;
import com.facebook.appevents.AppEventsLogger;
import com.mobiata.android.Log;
import com.mobiata.android.util.SettingUtils;

import rx.Observer;

public class RouterActivity extends Activity implements UserAccountRefresher.IUserAccountRefreshListener {

	boolean loadSignInViewAbTest = false;

	ImageView logoView;
	View content;

	private enum LaunchDestination {
		SIGN_IN,
		LAUNCH_SCREEN
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash_screen);
		// Track the app loading
		OmnitureTracking.trackAppLoading(this);

		// Update data
		ItineraryManager.getInstance().startSync(false, false, true);

		//Hi Facebook!
		facebookInstallTracking();

		cleanupOldCookies();
		cleanupOldSuggestions();

		Ui.getApplication(this).updateFirstLaunchAndUpdateSettings();

		content = findViewById(android.R.id.content);
		logoView = (ImageView) findViewById(R.id.splash_logo_id);

		if (User.isLoggedInToAccountManager(this) && !User.isLoggedInOnDisk(this)) {
			User.loadUser(this, this);
		}
		else {
			handleAppLaunch();
		}
	}

	private void launchOpeningView() {
		boolean isUsersFirstLaunchOfApp = ExpediaBookingApp.isFirstLaunchEver();
		boolean isNewVersionOfApp = ExpediaBookingApp.isFirstLaunchOfAppVersion();
		boolean userNotLoggedIn = !User.isLoggedIn(RouterActivity.this);
		loadSignInViewAbTest = (isUsersFirstLaunchOfApp || isNewVersionOfApp) && userNotLoggedIn;

		AbacusEvaluateQuery query = new AbacusEvaluateQuery(Db.getAbacusGuid(), PointOfSale.getPointOfSale().getTpid(), 0);
		if (ProductFlavorFeatureConfiguration.getInstance().isAbacusTestEnabled()) {
			if (loadSignInViewAbTest) {
				query.addExperiment(AbacusUtils.EBAndroidAppShowSignInFormOnLaunch);
			}
			query.addExperiment(AbacusUtils.EBAndroidAppShowMemberPricingCardOnLaunchScreen);
			query.addExperiment(AbacusUtils.EBAndroidAppLOBAccentuating);
			query.addExperiment(AbacusUtils.EBAndroidAppShowPopularHotelsCardOnLaunchScreen);
			query.addExperiment(AbacusUtils.EBAndroidAppShowSignInCardOnLaunchScreen);
			query.addExperiment(AbacusUtils.EBAndroidAppFlightPremiumClass);
			query.addExperiment(AbacusUtils.EBAndroidAppTripsNewSignInPage);
			query.addExperiment(AbacusUtils.EBAndroidAppShowAirAttachMessageOnLaunchScreen);
			query.addExperiment(AbacusUtils.EBAndroidAppShowCarWebView);
			query.addExperiment(AbacusUtils.EBAndroidAppLaunchShowGuestItinCard);
			query.addExperiment(AbacusUtils.EBAndroidAppLaunchShowActiveItinCard);
			if (FeatureToggleUtil.isFeatureEnabled(this, R.string.preference_itin_crystal_theme)) {
				query.addExperiment(AbacusUtils.EBAndroidAppItinCrystalSkin);
			}
		}

		Ui.getApplication(this).appComponent().abacus()
			.downloadBucket(query, evaluatePreLaunchABTestsSubscriber, 3, TimeUnit.SECONDS);
	}

	private Observer<AbacusResponse> evaluatePreLaunchABTestsSubscriber = new Observer<AbacusResponse>() {

		@Override
		public void onCompleted() {
			Log.d("Abacus:showSignInOnLaunchTest - onCompleted");
		}

		@Override
		public void onError(Throwable e) {
			Log.d("Abacus:showSignInOnLaunchTest - onError");
			if (BuildConfig.DEBUG) {
				AbacusHelperUtils.updateAbacus(new AbacusResponse(), RouterActivity.this);
			}
			doAnimationThenLaunchOpeningView(LaunchDestination.LAUNCH_SCREEN);
		}

		@Override
		public void onNext(AbacusResponse abacusResponse) {
			Log.d("Abacus:showSignInOnLaunchTest - onNext");
			AbacusHelperUtils.updateAbacus(abacusResponse, RouterActivity.this);

			if (Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppShowSignInFormOnLaunch)
				&& loadSignInViewAbTest) {
				doAnimationThenLaunchOpeningView(LaunchDestination.SIGN_IN);
			}
			else {
				doAnimationThenLaunchOpeningView(LaunchDestination.LAUNCH_SCREEN);
			}
		}
	};

	private void finishActivity() {
		finish();
		overridePendingTransition(R.anim.fade_in, R.anim.slide_down);
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
		handleAppLaunch();
	}

	private void handleAppLaunch() {
		TrackingUtils.initializeTracking(this.getApplication());
		// Show app introduction if available and not already shown.
		if (ProductFlavorFeatureConfiguration.getInstance().isAppIntroEnabled() && !SettingUtils
			.get(this, R.string.preference_app_intro_shown_once, false)) {
			ProductFlavorFeatureConfiguration.getInstance().launchAppIntroScreen(this);
			finishActivity();
		}
		else {
			launchOpeningView();
		}
	}

	private void doAnimationThenLaunchOpeningView(final LaunchDestination destination) {
		if (logoView.getHeight() != 0) {
			logoView.animate().translationY(content.getHeight() / 2 + logoView.getHeight() / 2 + 2)
				.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime)).setInterpolator(new LinearInterpolator()).setListener(
				new Animator.AnimatorListener() {
					@Override
					public void onAnimationStart(Animator animator) {
					}
					@Override
					public void onAnimationEnd(Animator animator) {
						if (destination == LaunchDestination.LAUNCH_SCREEN) {
							NavUtils.goToLaunchScreen(RouterActivity.this);
						}
						else {
							NavUtils.goToSignIn(RouterActivity.this);
						}
						finishActivity();
					}
					@Override
					public void onAnimationCancel(Animator animator) {
					}

					@Override
					public void onAnimationRepeat(Animator animator) {
					}
				}).start();
		}
		else {
			logoView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					logoView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
					doAnimationThenLaunchOpeningView(destination);
				}
			});
		}
	}
}
