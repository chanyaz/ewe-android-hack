package com.expedia.bookings.activity;

import java.util.Calendar;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.data.AvailabilityResponse;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.ConfirmationState;
import com.expedia.bookings.data.ConfirmationState.Type;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.ReviewsStatisticsResponse;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.fragment.HotelDetailsDescriptionFragment;
import com.expedia.bookings.fragment.HotelDetailsIntroFragment;
import com.expedia.bookings.fragment.HotelDetailsMiniGalleryFragment;
import com.expedia.bookings.fragment.HotelDetailsMiniGalleryFragment.HotelMiniGalleryFragmentListener;
import com.expedia.bookings.fragment.HotelDetailsMiniMapFragment;
import com.expedia.bookings.fragment.HotelDetailsMiniMapFragment.HotelMiniMapFragmentListener;
import com.expedia.bookings.fragment.HotelDetailsPricePromoFragment;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.TrackingUtils;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.widget.HotelDetailsScrollView;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.Ui;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.animation.AnimatorProxy;
import com.omniture.AppMeasurement;

public class HotelDetailsFragmentActivity extends SherlockFragmentActivity implements HotelMiniMapFragmentListener,
		HotelMiniGalleryFragmentListener {

	// Tags for this activity's fragments
	private static final String FRAGMENT_MINI_GALLERY_TAG = "FRAGMENT_MINI_GALLERY_TAG";
	private static final String FRAGMENT_PRICE_PROMO_TAG = "FRAGMENT_PRICE_PROMO_TAG";
	private static final String FRAGMENT_INTRO_TAG = "FRAGMENT_INTRO_TAG";
	private static final String FRAGMENT_MINI_MAP_TAG = "FRAGMENT_MINI_MAP_TAG";
	private static final String FRAGMENT_DESCRIPTION_TAG = "FRAGMENT_DESCRIPTION_TAG";

	// This is the position in the list that the hotel had when the user clicked on it 
	public static final String EXTRA_POSITION = "EXTRA_POSITION";

	private static final String INFO_DOWNLOAD_KEY = HotelDetailsFragmentActivity.class.getName() + ".info";
	private static final String REVIEWS_DOWNLOAD_KEY = HotelDetailsFragmentActivity.class.getName() + ".reviews";

	private static final long RESUME_TIMEOUT = 1000 * 60 * 20; // 20 minutes

	private Context mContext;
	private ExpediaBookingApp mApp;

	private HotelDetailsMiniGalleryFragment mGalleryFragment;
	private HotelDetailsPricePromoFragment mPricePromoFragment;
	private HotelDetailsIntroFragment mIntroFragment;
	private HotelDetailsMiniMapFragment mMapFragment;
	private HotelDetailsDescriptionFragment mDescriptionFragment;

	// For tracking - tells you when a user paused the Activity but came back to it
	private boolean mWasStopped;

	private long mLastResumeTime = -1;

	private ActivityKillReceiver mKillReceiver;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = this;
		mApp = (ExpediaBookingApp) getApplicationContext();

		if (getIntent().getBooleanExtra(Codes.OPENED_FROM_WIDGET, false)) {
			NavUtils.sendKillActivityBroadcast(mContext);

			Property property = (Property) JSONUtils.parseJSONableFromIntent(getIntent(), Codes.PROPERTY,
					Property.class);
			if (property != null) {
				Db.setSelectedProperty(property);
			}
			else {
				// It means we came back from the reviews activity and Db.getSelectedProperty is already valid
			}
		}

		if (checkFinishConditionsAndFinish())
			return;

		setupHotelActivity(savedInstanceState);

		// Note: the ordering here matters. We want to register the kill receiver after the KILL_ACTIVITY broadcast is
		// sent such that instances of this Activity loaded from the widget do not get finished just as they are loaded
		mKillReceiver = new ActivityKillReceiver(this);
		mKillReceiver.onCreate();
	}

	@Override
	protected void onStart() {
		super.onStart();

		if (mWasStopped) {
			onPageLoad();
			mWasStopped = false;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (checkFinishConditionsAndFinish())
			return;

		BackgroundDownloader bd = BackgroundDownloader.getInstance();

		AvailabilityResponse infoResponse = Db.getSelectedInfoResponse();
		if (infoResponse != null) {
			// We may have been downloading the data here before getting it elsewhere, so cancel
			// our own download once we have data
			bd.cancelDownload(INFO_DOWNLOAD_KEY);

			// Load the data
			mInfoCallback.onDownload(infoResponse);
		}
		else {
			if (bd.isDownloading(INFO_DOWNLOAD_KEY)) {
				bd.registerDownloadCallback(INFO_DOWNLOAD_KEY, mInfoCallback);
			}
			else {
				bd.startDownload(INFO_DOWNLOAD_KEY, mInfoDownload, mInfoCallback);
			}
		}

		ReviewsStatisticsResponse reviewsResponse = Db.getSelectedReviewsStatisticsResponse();
		if (reviewsResponse != null) {
			// We may have been downloading the data here before getting it elsewhere, so cancel
			// our own download once we have data
			bd.cancelDownload(REVIEWS_DOWNLOAD_KEY);

			// Load the data
			mReviewsCallback.onDownload(reviewsResponse);
		}
		else {
			if (bd.isDownloading(REVIEWS_DOWNLOAD_KEY)) {
				bd.registerDownloadCallback(REVIEWS_DOWNLOAD_KEY, mReviewsCallback);
			}
			else {
				bd.startDownload(REVIEWS_DOWNLOAD_KEY, mReviewsDownload, mReviewsCallback);
			}
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);

		if (checkFinishConditionsAndFinish())
			return;

		setupHotelActivity(null);
	}

	@TargetApi(11)
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_hotel_details, menu);

		if (checkFinishConditionsAndFinish())
			return super.onCreateOptionsMenu(menu);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayUseLogoEnabled(true);

		ViewGroup titleView = (ViewGroup) getLayoutInflater().inflate(R.layout.actionbar_hotel_name_with_stars, null);

		Property property = Db.getSelectedProperty();
		String title = property.getName();
		((TextView) titleView.findViewById(R.id.title)).setText(title);

		float rating = (float) property.getHotelRating();
		((RatingBar) titleView.findViewById(R.id.rating)).setRating(rating);

		actionBar.setCustomView(titleView);

		final MenuItem select = menu.findItem(R.id.menu_select_hotel);
		Button tv = (Button) getLayoutInflater().inflate(R.layout.actionbar_select_hotel, null);
		tv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onOptionsItemSelected(select);
			}
		});
		select.setActionView(tv);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// If opened from the widget, then "up" means to start a current location search
			if (getIntent().getBooleanExtra(Codes.OPENED_FROM_WIDGET, false)) {
				// Launch the PhoneSearchActivity action, with EXTRA_NEW_SEARCH flag
				Intent intent = new Intent(this, PhoneSearchActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtra(Codes.EXTRA_NEW_SEARCH, true);
				startActivity(intent);
			}
			finish();
			return true;
		case R.id.menu_select_hotel:
			startRoomRatesActivity();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (!isFinishing()) {
			BackgroundDownloader bd = BackgroundDownloader.getInstance();
			bd.unregisterDownloadCallback(INFO_DOWNLOAD_KEY);
			bd.unregisterDownloadCallback(REVIEWS_DOWNLOAD_KEY);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mKillReceiver != null) {
			mKillReceiver.onDestroy();
		}

		if (isFinishing()) {
			BackgroundDownloader bd = BackgroundDownloader.getInstance();
			bd.cancelDownload(INFO_DOWNLOAD_KEY);
			bd.cancelDownload(REVIEWS_DOWNLOAD_KEY);
		}
	}

	private void setupHotelActivity(Bundle savedInstanceState) {
		final Intent intent = getIntent();

		setContentView(R.layout.hotel_details_main);

		FragmentManager manager = getSupportFragmentManager();
		FragmentTransaction ft = manager.beginTransaction();

		mGalleryFragment = (HotelDetailsMiniGalleryFragment) manager.findFragmentByTag(FRAGMENT_MINI_GALLERY_TAG);
		if (mGalleryFragment == null) {
			mGalleryFragment = HotelDetailsMiniGalleryFragment.newInstance();
		}
		ft.add(R.id.hotel_details_mini_gallery_fragment_container, mGalleryFragment, FRAGMENT_MINI_GALLERY_TAG);

		mPricePromoFragment = (HotelDetailsPricePromoFragment) manager.findFragmentByTag(FRAGMENT_PRICE_PROMO_TAG);
		if (mPricePromoFragment == null) {
			mPricePromoFragment = HotelDetailsPricePromoFragment.newInstance();
		}
		ft.add(R.id.hotel_details_price_promo_fragment_container, mPricePromoFragment, FRAGMENT_PRICE_PROMO_TAG);

		mIntroFragment = (HotelDetailsIntroFragment) manager.findFragmentByTag(FRAGMENT_INTRO_TAG);
		if (mIntroFragment == null) {
			mIntroFragment = HotelDetailsIntroFragment.newInstance();
		}
		ft.add(R.id.hotel_details_intro_fragment_container, mIntroFragment, FRAGMENT_INTRO_TAG);

		mMapFragment = (HotelDetailsMiniMapFragment) manager.findFragmentByTag(FRAGMENT_MINI_MAP_TAG);
		if (mMapFragment == null) {
			mMapFragment = HotelDetailsMiniMapFragment.newInstance();
		}
		ft.add(R.id.hotel_details_map_fragment_container, mMapFragment, FRAGMENT_MINI_MAP_TAG);

		mDescriptionFragment = (HotelDetailsDescriptionFragment) manager.findFragmentByTag(FRAGMENT_DESCRIPTION_TAG);
		if (mDescriptionFragment == null) {
			mDescriptionFragment = HotelDetailsDescriptionFragment.newInstance();
		}
		ft.add(R.id.hotel_details_description_fragment_container, mDescriptionFragment, FRAGMENT_DESCRIPTION_TAG);

		// Start the animated transition.
		ft.commit();

		// Tracking
		if (savedInstanceState == null) {
			onPageLoad();

			// Track here if user opened app from widget.  Currently assumes that all widget searches
			// are "nearby" - if this ever changes, this needs to be updated.
			if (intent.getBooleanExtra(Codes.OPENED_FROM_WIDGET, false)) {
				TrackingUtils.trackSimpleEvent(this, null, null, null, "App.Widget.Deal.Nearby");
				mApp.broadcastSearchParamsChangedInWidget((SearchParams) JSONUtils.parseJSONableFromIntent(intent,
						Codes.SEARCH_PARAMS, SearchParams.class));
			}
		}

		initLandscapeGalleryLayout();
	}

	// Initialize the gallery if we're in landscape mode. The gallery should be scooched
	// a little to the left to center it within the left 45% of the screen, and the 
	// price promo banner should take up the left 45% of the screen. "post" it to make
	// sure that windowWidth is populated when this runs.
	@TargetApi(11)
	private void initLandscapeGalleryLayout() {
		final View details = findViewById(R.id.hotel_details_landscape);
		if (details != null) {
			details.post(new Runnable() {
				@Override
				public void run() {
					View gallery = findViewById(R.id.hotel_details_mini_gallery_fragment_container);
					View pricePromo = findViewById(R.id.hotel_details_price_promo_fragment_container);
					int windowWidth = getWindow().getDecorView().getWidth();
					if (AndroidUtils.getSdkVersion() >= 11) {
						gallery.setTranslationX(-windowWidth * 0.275f);
					}
					else {
						AnimatorProxy.wrap(gallery).setTranslationX(-windowWidth * 0.275f);
					}
					ViewGroup.LayoutParams lp = pricePromo.getLayoutParams();
					lp.width = (int) (windowWidth * .45f) + 1;
					pricePromo.setLayoutParams(lp);
				}
			});
		}
	}

	public void startRoomRatesActivity() {
		Intent roomsRatesIntent = new Intent(this, RoomsAndRatesListActivity.class);
		startActivity(roomsRatesIntent);
	}

	public boolean checkFinishConditionsAndFinish() {
		Property property = Db.getSelectedProperty();
		if (property == null) {
			Log.i("Detected expired DB, finishing activity.");
			finish();
			return true;
		}

		// Haxxy fix for #13798, only required on pre-Honeycomb
		if (ConfirmationState.hasSavedData(this, Type.HOTEL)) {
			finish();
			return true;
		}

		// #14135, set a 1 hour timeout on this screen
		if (mLastResumeTime != -1 && mLastResumeTime + RESUME_TIMEOUT < Calendar.getInstance().getTimeInMillis()) {
			finish();
			return true;
		}
		mLastResumeTime = Calendar.getInstance().getTimeInMillis();

		return false;
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// Async loading of ExpediaServices.information

	private final Download<AvailabilityResponse> mInfoDownload = new Download<AvailabilityResponse>() {
		@Override
		public AvailabilityResponse doDownload() {
			ExpediaServices services = new ExpediaServices(mContext);
			return services.information(Db.getSelectedProperty());
		}
	};

	private final OnDownloadComplete<AvailabilityResponse> mInfoCallback = new OnDownloadComplete<AvailabilityResponse>() {
		@Override
		public void onDownload(AvailabilityResponse response) {
			// Check if we got a better response elsewhere before loading up this data
			AvailabilityResponse possibleBetterResponse = Db.getSelectedInfoResponse();
			if (possibleBetterResponse != null && !possibleBetterResponse.canRequestMoreData()) {
				response = possibleBetterResponse;
			}
			else {
				Db.addAvailabilityResponse(response);
			}

			// Notify affected child fragments to refresh.

			if (mIntroFragment != null) {
				mIntroFragment.populateViews();
			}

			if (mDescriptionFragment != null) {
				mDescriptionFragment.populateViews();
			}

			if (mGalleryFragment != null) {
				mGalleryFragment.populateViews();
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////////////////////
	// Async loading of ExpediaServices.reviewsStatistics

	private final Download<ReviewsStatisticsResponse> mReviewsDownload = new Download<ReviewsStatisticsResponse>() {
		@Override
		public ReviewsStatisticsResponse doDownload() {
			ExpediaServices services = new ExpediaServices(mContext);
			return services.reviewsStatistics(Db.getSelectedProperty());
		}
	};

	private final OnDownloadComplete<ReviewsStatisticsResponse> mReviewsCallback = new OnDownloadComplete<ReviewsStatisticsResponse>() {
		@Override
		public void onDownload(ReviewsStatisticsResponse response) {

			Db.addReviewsStatisticsResponse(response);

			// Notify affected child fragments to refresh.

			if (mIntroFragment != null) {
				mIntroFragment.populateViews();
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////////////////////
	// HotelMiniMapFragmentListener implementation

	@Override
	public void onMiniMapClicked() {
		Intent intent = new Intent(this, HotelMapActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		startActivity(intent);
		overridePendingTransition(R.anim.fade_in, R.anim.explode);
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// HotelMiniGalleryFragmentListener implementation

	boolean isGalleryFullscreen = false;

	@Override
	public void onMiniGalleryItemClicked(Property property, Object item) {
		// Do this if in portrait
		HotelDetailsScrollView scrollView = Ui.findView(this, R.id.hotel_details_portrait);
		if (scrollView != null) {
			scrollView.toggleFullScreenGallery();
			return;
		}

		View details = findViewById(R.id.hotel_details_landscape);
		if (details != null) {
			toggleFullScreenGalleryLandscape();
		}
	}

	private void toggleFullScreenGalleryLandscape() {
		// Do this if in landscape
		final View details = findViewById(R.id.hotel_details_landscape);
		final View gallery = findViewById(R.id.hotel_details_mini_gallery_fragment_container);
		final View pricePromo = findViewById(R.id.hotel_details_price_promo_fragment_container);
		final int windowWidth = getWindow().getDecorView().getWidth();

		if (!isGalleryFullscreen) {
			ObjectAnimator anim1 = ObjectAnimator.ofFloat(details, "translationX", windowWidth);
			ObjectAnimator anim2 = ObjectAnimator.ofFloat(gallery, "translationX", 0f);
			AnimatorSet animatorSet = new AnimatorSet();
			animatorSet.play(anim1).with(anim2);
			animatorSet.start();
			ViewGroup.LayoutParams lp = pricePromo.getLayoutParams();
			lp.width = windowWidth;
			pricePromo.setLayoutParams(lp);
		}
		else {
			ObjectAnimator anim1 = ObjectAnimator.ofFloat(details, "translationX", 0f);
			ObjectAnimator anim2 = ObjectAnimator.ofFloat(gallery, "translationX", -windowWidth * 0.275f);
			AnimatorSet animatorSet = new AnimatorSet();
			animatorSet.play(anim1).with(anim2);
			animatorSet.addListener(new AnimatorListener() {

				@Override
				public void onAnimationCancel(Animator arg0) {
					// Intentionally left blank
				}

				@Override
				public void onAnimationEnd(Animator arg0) {
					ViewGroup.LayoutParams lp = pricePromo.getLayoutParams();
					lp.width = (int) (windowWidth * .45f) + 1;
					pricePromo.setLayoutParams(lp);
				}

				@Override
				public void onAnimationRepeat(Animator arg0) {
					// Intentionally left blank
				}

				@Override
				public void onAnimationStart(Animator arg0) {
					// Intentionally left blank
				}
			});
			animatorSet.start();
		}

		isGalleryFullscreen = !isGalleryFullscreen;
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// Omniture tracking

	public void onPageLoad() {
		Log.d("Tracking \"App.Hotels.Infosite\" pageLoad");

		AppMeasurement s = new AppMeasurement(getApplication());

		TrackingUtils.addStandardFields(this, s);

		s.pageName = "App.Hotels.Infosite";

		s.events = "event32";

		// Shopper/Confirmer
		s.eVar25 = s.prop25 = "Shopper";

		// Rating or highly rated
		Property property = Db.getSelectedProperty();
		TrackingUtils.addHotelRating(s, property);

		// Products
		TrackingUtils.addProducts(s, property);

		// Position, if opened from list
		int position = getIntent().getIntExtra(EXTRA_POSITION, -1);
		if (position != -1) {
			s.eVar39 = position + "";
		}

		// Send the tracking data
		s.track();
	}

}
