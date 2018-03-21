package com.expedia.bookings.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.abacus.AbacusResponse;
import com.expedia.bookings.data.lx.LxSearchParams;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.data.user.UserStateManager;
import com.expedia.bookings.deeplink.ActivityDeepLink;
import com.expedia.bookings.deeplink.CarDeepLink;
import com.expedia.bookings.deeplink.DeepLink;
import com.expedia.bookings.deeplink.DeepLinkParser;
import com.expedia.bookings.deeplink.FlightDeepLink;
import com.expedia.bookings.deeplink.FlightShareDeepLink;
import com.expedia.bookings.deeplink.ForceBucketDeepLink;
import com.expedia.bookings.deeplink.HomeDeepLink;
import com.expedia.bookings.deeplink.HotelDeepLink;
import com.expedia.bookings.deeplink.MemberPricingDeepLink;
import com.expedia.bookings.deeplink.PackageDeepLink;
import com.expedia.bookings.deeplink.RailDeepLink;
import com.expedia.bookings.deeplink.ReviewFeedbackEmailDeeplink;
import com.expedia.bookings.deeplink.ReviewSubmissionDeepLink;
import com.expedia.bookings.deeplink.SharedItineraryDeepLink;
import com.expedia.bookings.deeplink.ShortUrlDeepLink;
import com.expedia.bookings.deeplink.SignInDeepLink;
import com.expedia.bookings.deeplink.SupportEmailDeepLink;
import com.expedia.bookings.deeplink.TripDeepLink;
import com.expedia.bookings.deeplink.WebDeepLink;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.features.Feature;
import com.expedia.bookings.features.Features;
import com.expedia.bookings.hotel.deeplink.HotelIntentBuilder;
import com.expedia.bookings.marketing.carnival.CarnivalUtils;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.services.IClientLogServices;
import com.expedia.bookings.utils.AbacusHelperUtils;
import com.expedia.bookings.utils.DebugInfoUtils;
import com.expedia.bookings.utils.DeepLinkUtils;
import com.expedia.bookings.utils.LXDataUtils;
import com.expedia.bookings.utils.LXNavUtils;
import com.expedia.bookings.utils.OmnitureDeepLinkAnalytics;
import com.expedia.bookings.utils.ShortcutUtils;
import com.expedia.bookings.utils.TrackingUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.utils.UserAccountRefresher;
import com.expedia.bookings.utils.navigation.CarNavUtils;
import com.expedia.bookings.utils.navigation.FlightNavUtils;
import com.expedia.bookings.utils.navigation.HotelNavUtils;
import com.expedia.bookings.utils.navigation.NavUtils;
import com.expedia.bookings.utils.navigation.PackageNavUtils;
import com.expedia.util.ForceBucketPref;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.mobiata.android.Log;
import com.mobiata.android.SocialUtils;

import io.reactivex.Observer;
import io.reactivex.observers.DisposableObserver;
import okhttp3.HttpUrl;

/**
 * This class acts as a router for incoming deep links.  It seems a lot
 * easier to just route through one Activity rather than try to handle it
 * all in the manifest (where you may need to handle the same scheme in multiple
 * possible activities).
 */
public class DeepLinkRouterActivity extends Activity implements UserAccountRefresher.IUserAccountRefreshListener {

	private static final String TAG = "ExpediaDeepLink";

	@VisibleForTesting
	void setUniversalWebviewDeepLinkFeature(Feature universalWebviewDeepLinkFeature) {
		this.universalWebviewDeepLinkFeature = universalWebviewDeepLinkFeature;
	}

	private Feature universalWebviewDeepLinkFeature = Features.Companion.getAll().getUniversalWebviewDeepLink();

	private UserStateManager userStateManager;
	IClientLogServices clientLogServices;
	private DeepLinkParser deepLinkParser = null;
	private boolean supportsRails = PointOfSale.getPointOfSale().supports(LineOfBusiness.RAILS);

	Observer<AbacusResponse> evaluateAbTests = new DisposableObserver<AbacusResponse>() {

		@Override
		public void onComplete() {
			handleDeeplink();
		}

		@Override
		public void onError(Throwable e) {
			AbacusHelperUtils.updateAbacus(new AbacusResponse(), DeepLinkRouterActivity.this);
			handleDeeplink();
		}

		@Override
		public void onNext(AbacusResponse abacusResponse) {
			AbacusHelperUtils.updateAbacus(abacusResponse, DeepLinkRouterActivity.this);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		deepLinkParser = new DeepLinkParser(this.getAssets());
		userStateManager = Ui.getApplication(this).appComponent().userStateManager();

		startProcessing();
	}

	@VisibleForTesting
	protected void startProcessing() {
		userStateManager.ensureUserStateSanity(this);
	}

	@VisibleForTesting
	protected void handleDeeplink() {
		TrackingUtils.initializeTracking(this.getApplication());

		if (ProductFlavorFeatureConfiguration.getInstance().isFirebaseEnabled()) {
			handleDeeplinkFromFirebase();
		}
		else {
			handleDeeplinkFromIntent();
		}
	}

	private void handleDeeplinkFromFirebase() {
		// First we try to load *every* deeplink as a Firebase dynamic link.
		// If that fails (because it's not a firebase link) we fallback to regular intent deeplink

		FirebaseDynamicLinks firebase = getFirebaseDynamicLinksInstance();
		if (firebase != null) {
			firebase.getDynamicLink(getIntent())
				.addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
					@Override
					public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
						// Get deep link from result (may be null if no link is found)
						Uri deepLink;
						if (pendingDynamicLinkData != null) {
							deepLink = pendingDynamicLinkData.getLink();
							Log.d(TAG, "getDynamicLink:onSuccess:foundDynamicLink");
							handleDeepLinkUri(deepLink);
						}
						else {
							Log.d(TAG, "getDynamicLink:onSuccess:noDynamicLink");
							handleDeeplinkFromIntent();
						}
					}
				})
				.addOnFailureListener(this, new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {
						Log.d(TAG, "getDynamicLink:onFailure", e);
						handleDeeplinkFromIntent();
					}
				});
		}
		else {
			// This should never occur in practice; mostly useful for testing
			Log.d(TAG, "getDynamicLink:nullFirebase");
			handleDeeplinkFromIntent();
		}
	}

	private void handleDeeplinkFromIntent() {
		Intent intent = getIntent();
		Uri data = intent.getData();
		if (data == null || data.getHost() == null) {
			// bad data
			finish();
		}
		else {
			handleDeepLinkUri(data);
		}
	}

	private void handleDeepLinkUri(Uri data) {
		clientLogServices = Ui.getApplication(this).appComponent().clientLog();
		DeepLinkUtils.parseAndTrackDeepLink(clientLogServices, HttpUrl.parse(data.toString()), new OmnitureDeepLinkAnalytics());

		if (this.getIntent().getExtras() != null && this.getIntent().getExtras().getBoolean(CarnivalUtils.CustomCarnivalListener.Companion.getKEY_NOTIFICATION_PROVIDER_VALUE(), false)) {
			CarnivalUtils.getInstance().trackCarnivalPush(this, data, this.getIntent().getExtras());
			data = CarnivalUtils.getInstance().createParameterizedDeeplinkWithStoredValues(data);
		}

		DeepLink deepLink = deepLinkParser.parseDeepLink(data);

		boolean finish;

		if (deepLink instanceof HotelDeepLink) {
			finish = handleHotelSearch((HotelDeepLink) deepLink);
		}
		else if (deepLink instanceof FlightDeepLink) {
			handleFlightSearch((FlightDeepLink) deepLink);
			finish = true;
		}
		else if (deepLink instanceof CarDeepLink) {
			handleCarsSearch((CarDeepLink) deepLink);
			finish = true;
		}
		else if (deepLink instanceof ActivityDeepLink) {
			handleActivitySearch((ActivityDeepLink) deepLink);
			finish = true;
		}
		else if (deepLink instanceof SignInDeepLink) {
			handleSignIn();
			finish = true;
		}
		else if (deepLink instanceof MemberPricingDeepLink) {
			handleMemberPricing();
			finish = true;
		}
		else if (deepLink instanceof TripDeepLink) {
			handleTrip((TripDeepLink) deepLink);
			finish = true;
		}
		else if (deepLink instanceof SharedItineraryDeepLink) {
			goFetchSharedItin(((SharedItineraryDeepLink) deepLink).getUrl());
			finish = true;
		}
		else if (deepLink instanceof ShortUrlDeepLink) {
			handleShortUrl((ShortUrlDeepLink) deepLink);
			finish = true;
		}
		else if (deepLink instanceof SupportEmailDeepLink) {
			handleSupportEmail();
			finish = true;
		}
		else if (deepLink instanceof ReviewFeedbackEmailDeeplink) {
			handleReviewFeedbackEmail();
			finish = true;
		}
		else if (deepLink instanceof ForceBucketDeepLink) {
			handleForceBucketing((ForceBucketDeepLink) deepLink);
			finish = true;
		}
		else if (deepLink instanceof HomeDeepLink) {
			NavUtils.goToLaunchScreen(this, true);
			finish = true;
		}
		else if (deepLink instanceof PackageDeepLink) {
			handlePackageSearch((PackageDeepLink) deepLink);
			finish = true;
		}
		else if (deepLink instanceof RailDeepLink) {
			if (supportsRails) {
				handleRailSearch((RailDeepLink) deepLink);
			}
			else {
				NavUtils.goToLaunchScreen(this, true);
			}
			finish = true;
		}
		else if (deepLink instanceof FlightShareDeepLink) {
			handleFlightShareDeepLink((FlightShareDeepLink)deepLink);
			finish = true;
		}
		else if (deepLink instanceof ReviewSubmissionDeepLink) {
			NavUtils.goToWebView(this, ((ReviewSubmissionDeepLink) deepLink).getUrl());
			finish = true;
		}
		else if (deepLink instanceof WebDeepLink) {
			if (isWebViewFeatureEnabled()) {
				handleWebDeepLink((WebDeepLink) deepLink);
			}
			else {
				NavUtils.goToLaunchScreen(this, true);
			}
			finish = true;
		}
		else {
			com.mobiata.android.util.Ui.showToast(this, "Cannot yet handle data: " + data);
			finish = true;
		}

		if (finish) {
			finish();
		}
	}

	private boolean isWebViewFeatureEnabled() {
		return universalWebviewDeepLinkFeature.enabled();
	}

	private void handleWebDeepLink(WebDeepLink deepLink) {
		NavUtils.goToWebView(this, deepLink.getUrl());
	}

	private void handleForceBucketing(ForceBucketDeepLink forceBucketDeepLink) {
		//reset and revert back to default abacus test map
		if (isInteger(forceBucketDeepLink.getValue()) && isInteger(forceBucketDeepLink.getKey())) {
			int key = Integer.valueOf(forceBucketDeepLink.getKey());
			int value = Integer.valueOf(forceBucketDeepLink.getValue());

			if (key == 0) {
				ForceBucketPref.setUserForceBucketed(this, false);
				AbacusHelperUtils.downloadBucket(this);
			}
			else {
				ForceBucketPref.setUserForceBucketed(this, true);
				ForceBucketPref.saveForceBucketedTestKeyValue(this, key, value);
				Db.sharedInstance.getAbacusResponse().forceUpdateABTest(key, value);
			}
		}
	}

	private boolean isInteger(String value) {
		if (value != null && !value.isEmpty()) {
			try {
				Integer.parseInt(value);
			}
			catch (NumberFormatException ex) {
				return false;
			}
			return true;
		}
		return false;
	}

	private boolean handleCarsSearch(CarDeepLink carDeepLink) {

		PointOfSale pos = PointOfSale.getPointOfSale();
		if (pos.supports(LineOfBusiness.CARS)) {
			CarNavUtils.goToCars(this, 0);
		}
		else {
			NavUtils.goToLaunchScreen(this, false, LineOfBusiness.CARS);
		}
		return true;
	}

	private boolean handleActivitySearch(ActivityDeepLink activityDeepLink) {

		if (PointOfSale.getPointOfSale().supports(LineOfBusiness.LX)) {
			LxSearchParams searchParams = LXDataUtils.buildLXSearchParamsFromDeeplink(activityDeepLink, this);
			LXNavUtils.goToActivities(this, null, searchParams, NavUtils.FLAG_DEEPLINK);
		}
		else {
			NavUtils.goToLaunchScreen(this, false, LineOfBusiness.LX);
		}
		return true;
	}

	private boolean handleHotelSearch(HotelDeepLink deepLink) {
		Intent hotelIntent = new HotelIntentBuilder().from(this, deepLink).build(this);

		if (deepLink.isBaseURL()) {
			HotelNavUtils.goToHotels(this, NavUtils.FLAG_DEEPLINK);
		}
		else {
			HotelNavUtils.goToHotels(this, hotelIntent);
		}

		finish();
		return false;
	}

	private void handleFlightSearch(FlightDeepLink flightDeepLink) {

		// Fill FlightSearchParams with query data
		FlightSearchParams params = new FlightSearchParams();

		if (flightDeepLink.getOrigin() != null) {
			Location departureLocation = new Location();
			departureLocation.setDestinationId(flightDeepLink.getOrigin());
			params.setDepartureLocation(departureLocation);
			Log.d(TAG, "Set flight origin: " + departureLocation.getDestinationId());
		}

		if (flightDeepLink.getDestination() != null) {
			Location arrivalLocation = new Location();
			arrivalLocation.setDestinationId(flightDeepLink.getDestination());
			params.setArrivalLocation(arrivalLocation);
			Log.d(TAG, "Set flight destination: " + arrivalLocation.getDestinationId());
		}

		if (flightDeepLink.getDepartureDate() != null) {
			Log.d(TAG, "Set flight departure date: " + flightDeepLink.getDepartureDate());
			params.setDepartureDate(flightDeepLink.getDepartureDate());
		}

		if (flightDeepLink.getReturnDate() != null) {
			params.setReturnDate(flightDeepLink.getReturnDate());
			Log.d(TAG, "Set flight return date: " + flightDeepLink.getReturnDate());
		}

		params.ensureValidDates();

		// Add adults (if supplied)
		if (flightDeepLink.getNumAdults() != 0) {
			params.setNumAdults(flightDeepLink.getNumAdults());
		}

		if (flightDeepLink.isBaseURL()) {
			FlightNavUtils.goToFlights(this);
		}
		else {
			FlightNavUtils.goToFlights(this, params);
		}
	}

	private void handleShortUrl(ShortUrlDeepLink shortUrlDeepLink) {
		final String shortUrl = shortUrlDeepLink.getShortUrl();
		goFetchSharedItinWithShortUrl(shortUrl, new OnSharedItinUrlReceiveListener() {
			@Override
			public void onSharedItinUrlReceiveListener(String longUrl) {
				if (longUrl != null) {
					goFetchSharedItin(longUrl);
				}
			}
		});
	}

	protected interface OnSharedItinUrlReceiveListener {
		void onSharedItinUrlReceiveListener(String longUrl);
	}

	protected void goFetchSharedItinWithShortUrl(final String shortUrl, final OnSharedItinUrlReceiveListener runnable) {
		final ExpediaServices services = new ExpediaServices(this);
		new Thread(new Runnable() {
			@Override
			public void run() {
				String longUrl = services.getLongUrl(shortUrl);
				runnable.onSharedItinUrlReceiveListener(longUrl);
			}
		}).start();
	}

	private void goFetchSharedItin(String sharableUrl) {
		getItineraryManagerInstance().fetchSharedItin(sharableUrl);
		NavUtils.goToItin(this);
	}

	private void handleTrip(TripDeepLink tripDeepLink) {
		NavUtils.goToItin(this, tripDeepLink.getItinNum());
	}

	private void handlePackageSearch(PackageDeepLink packageDeepLink) {
		PackageNavUtils.goToPackages(this, null, null, 0);
	}

	private void handleRailSearch(RailDeepLink deepLink) {
		NavUtils.goToRail(this, null, 0);
	}

	private void handleFlightShareDeepLink(FlightShareDeepLink flightShareDeepLink) {
		ShortcutUtils.INSTANCE.shareFlightStatus(this.getBaseContext());
	}

	@VisibleForTesting
	protected void handleSupportEmail() {
		Intent intent = SocialUtils
			.getEmailIntent(this, getString(R.string.email_app_support), getString(R.string.email_app_support_headline),
				DebugInfoUtils.generateEmailBody(this));
		startActivity(intent);
	}

	@VisibleForTesting
	protected void handleReviewFeedbackEmail() {
		Intent intent = SocialUtils
			.getEmailIntent(this, getString(R.string.email_app_review_feedback),
				getString(R.string.email_app_support_headline),
				DebugInfoUtils.generateEmailBody(this));
		startActivity(intent);
	}

	@VisibleForTesting
	protected void handleMemberPricing() {
		if (userStateManager.isUserAuthenticated()) {
			NavUtils.goToMemberPricing(this);
		}
		else {
			NavUtils.goToSignIn(this, NavUtils.DEAL_SEARCH);
		}
	}

	protected ItineraryManager getItineraryManagerInstance() {
		return ItineraryManager.getInstance();
	}

	protected FirebaseDynamicLinks getFirebaseDynamicLinksInstance() {
		return FirebaseDynamicLinks.getInstance();
	}

	@VisibleForTesting
	protected void handleSignIn() {
		NavUtils.goToSignIn(this);
	}

	@Override
	public void onUserAccountRefreshed() {
		AbacusHelperUtils.downloadBucketWithWait(this, evaluateAbTests);
	}
}
