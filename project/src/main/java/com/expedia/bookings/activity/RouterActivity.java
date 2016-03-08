package com.expedia.bookings.activity;

import java.io.File;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.abacus.AbacusEvaluateQuery;
import com.expedia.bookings.data.abacus.AbacusResponse;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AbacusHelperUtils;
import com.expedia.bookings.utils.ClearPrivateDataUtil;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.Ui;
import com.facebook.appevents.AppEventsLogger;
import com.mobiata.android.Log;
import com.mobiata.android.util.SettingUtils;

import rx.Observer;

/**
 * This is a routing Activity that points users towards either the phone or
 * tablet version of this app.
 *
 * It is named SearchActivity for historical reasons; this was the original
 * starting Activity for older versions of EH, and we don't want to break any
 * future installs (which may have setup quick links to EH).
 *
 * http://android-developers.blogspot.com/2011/06/things-that-cannot-change.html
 *
 */
public class RouterActivity extends Activity {

	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;

		// Track the app loading
		OmnitureTracking.trackAppLoading(mContext);
		AdTracker.trackLaunch();

		// Update data
		ItineraryManager.getInstance().startSync(false, false, true);

		//Hi Facebook!
		facebookInstallTracking();

		cleanupOldCookies();
		cleanupOldSuggestions();

		if (NavUtils.skipLaunchScreenAndStartEHTablet(this)) {
			// Note: 2.0 will not support launch screen nor Flights on tablet ergo send user to EH tablet
			finish();
		}
		// Show app introduction if available and not already shown.
		else if (ProductFlavorFeatureConfiguration.getInstance().isAppIntroEnabled() && !SettingUtils
			.get(this, R.string.preference_app_intro_shown_once, false)) {
			ProductFlavorFeatureConfiguration.getInstance().launchAppIntroScreen(this);
			finish();
		}
		else {
			launchOpeningView();
		}
	}

	private void launchOpeningView() {
		boolean isUsersFirstLaunchOfApp = ExpediaBookingApp.isFirstLaunchEver();
		boolean isNewVersionOfApp = ExpediaBookingApp.isFirstLaunchOfAppVersion();
		boolean userNotLoggedIn = !User.isLoggedIn(RouterActivity.this);
		boolean loadSignInViewAbTest = (isUsersFirstLaunchOfApp || isNewVersionOfApp) && userNotLoggedIn;

		if (loadSignInViewAbTest) {
			AbacusEvaluateQuery query = new AbacusEvaluateQuery(Db.getAbacusGuid(), PointOfSale.getPointOfSale().getTpid(), 0);
			query.addExperiment(AbacusUtils.EBAndroidAppShowSignInOnLaunch);
			Ui.getApplication(this).appComponent().abacus().downloadBucket(query, showSignInViewABTestSubscriber, 3, TimeUnit.SECONDS);
		}
		else {
			NavUtils.goToLaunchScreen(RouterActivity.this, false);
			// Finish this Activity after routing
			finish();
		}
	}

	private Observer<AbacusResponse> showSignInViewABTestSubscriber = new Observer<AbacusResponse>() {

		@Override
		public void onCompleted() {
			Log.d("Abacus:showSignInOnLaunchTest - onCompleted");
		}

		@Override
		public void onError(Throwable e) {
			Log.d("Abacus:showSignInOnLaunchTest - onError");
			// Finish this Activity after routing
			NavUtils.goToLaunchScreen(RouterActivity.this, false);
			finish();
		}

		@Override
		public void onNext(AbacusResponse abacusResponse) {
			Log.d("Abacus:showSignInOnLaunchTest - onNext");
			AbacusHelperUtils.updateAbacus(abacusResponse, RouterActivity.this);
			if (Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppShowSignInOnLaunch)) {
				NavUtils.goToSignIn(RouterActivity.this);
			}
			else {
				NavUtils.goToLaunchScreen(RouterActivity.this, false);
			}
			finish();
		}
	};

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
		String[] files = new String[]{
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
		String[] files = new String[]{
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

}
