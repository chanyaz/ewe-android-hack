package com.expedia.bookings.activity;

import android.app.Activity;
import android.os.Bundle;
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
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.TrackingUtils;
import com.expedia.bookings.utils.TuneUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.utils.UserAccountRefresher;
import com.facebook.appevents.AppEventsLogger;
import com.mobiata.android.Log;
import com.mobiata.android.util.SettingUtils;
import java.io.File;
import java.util.concurrent.TimeUnit;
import rx.Observer;

/**
 * This is a routing Activity that points users towards either the phone or
 * tablet version of this app.
 * <p>
 * It is named SearchActivity for historical reasons; this was the original
 * starting Activity for older versions of EH, and we don't want to break any
 * future installs (which may have setup quick links to EH).
 * <p>
 * http://android-developers.blogspot.com/2011/06/things-that-cannot-change.html
 */
public class RouterActivity extends Activity implements UserAccountRefresher.IUserAccountRefreshListener {

	boolean loadSignInViewAbTest = false;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Track the app loading
		OmnitureTracking.trackAppLoading(this);
		TuneUtils.startTune(this);

		// Update data
		ItineraryManager.getInstance().startSync(false, false, true);

		//Hi Facebook!
		facebookInstallTracking();

		cleanupOldCookies();
		cleanupOldSuggestions();

		Ui.getApplication(this).updateFirstLaunchAndUpdateSettings();

		if (User.isLoggedIn(this)) {
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

		AbacusEvaluateQuery query = new AbacusEvaluateQuery(Db.getAbacusGuid(), PointOfSale.getPointOfSale().getTpid(),
			0);

		if (ProductFlavorFeatureConfiguration.getInstance().isAbacusTestEnabled()) {
			query.addExperiment(AbacusUtils.EBAndroidAppFlightTest);

			if (loadSignInViewAbTest) {
				query.addExperiment(AbacusUtils.EBAndroidAppShowSignInOnLaunch);
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
			NavUtils.goToLaunchScreen(RouterActivity.this, false);
			finishActivity();
		}

		@Override
		public void onNext(AbacusResponse abacusResponse) {
			Log.d("Abacus:showSignInOnLaunchTest - onNext");
			AbacusHelperUtils.updateAbacus(abacusResponse, RouterActivity.this);
			if (Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppShowSignInOnLaunch)
				&& loadSignInViewAbTest) {
				NavUtils.goToSignIn(RouterActivity.this);
			}
			else {
				NavUtils.goToLaunchScreen(RouterActivity.this, false);
			}
			finishActivity();
		}
	};

	private void finishActivity() {
		finish();
		overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
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
		if (NavUtils.skipLaunchScreenAndStartEHTablet(this)) {
			// Note: 2.0 will not support launch screen nor Flights on tablet ergo send user to EH tablet
			finishActivity();
		}
		// Show app introduction if available and not already shown.
		else if (ProductFlavorFeatureConfiguration.getInstance().isAppIntroEnabled() && !SettingUtils
			.get(this, R.string.preference_app_intro_shown_once, false)) {
			ProductFlavorFeatureConfiguration.getInstance().launchAppIntroScreen(this);
			finishActivity();
		}
		else {
			launchOpeningView();
		}
	}
}
