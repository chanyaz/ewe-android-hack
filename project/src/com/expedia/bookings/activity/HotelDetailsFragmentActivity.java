package com.expedia.bookings.activity;

import java.util.Calendar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import com.expedia.bookings.R;
import com.expedia.bookings.data.AvailabilityResponse;
import com.expedia.bookings.data.Codes;
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
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.TrackingUtils;
import com.expedia.bookings.utils.ConfirmationUtils;
import com.expedia.bookings.utils.LayoutUtils;
import com.expedia.bookings.widget.HotelDetailsScrollView;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.util.AndroidUtils;
import com.omniture.AppMeasurement;

public class HotelDetailsFragmentActivity extends FragmentActivity implements HotelMiniMapFragmentListener,
		HotelMiniGalleryFragmentListener {

	// Tags for this activity's fragments
	private static final String FRAGMENT_MINI_GALLERY_TAG = "FRAGMENT_MINI_GALLERY_TAG";
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
	private HotelDetailsIntroFragment mIntroFragment;
	private HotelDetailsMiniMapFragment mMapFragment;
	private HotelDetailsDescriptionFragment mDescriptionFragment;
	private HotelDetailsScrollView mScrollView;

	// For tracking - tells you when a user paused the Activity but came back to it
	private boolean mWasStopped;
	private boolean mIsStartingReviewsActivity = false;

	private long mLastResumeTime = -1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		mApp = (ExpediaBookingApp) getApplicationContext();
		setupHotelActivity(savedInstanceState);
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

		// Haxxy fix for #13798, only required on pre-Honeycomb
		if (AndroidUtils.getSdkVersion() <= 10 && ConfirmationUtils.hasSavedConfirmationData(this)) {
			finish();
			return;
		}

		// #14135, set a 1 hour timeout on this screen
		if (mLastResumeTime != -1 && mLastResumeTime + RESUME_TIMEOUT < Calendar.getInstance().getTimeInMillis()) {
			finish();
			return;
		}
		mLastResumeTime = Calendar.getInstance().getTimeInMillis();

		mIsStartingReviewsActivity = false;

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
		setupHotelActivity(null);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// app icon in action bar clicked; go back
			finish();
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

	private void setupHotelActivity(Bundle savedInstanceState) {
		final Intent intent = getIntent();

		setContentView(R.layout.hotel_details_main);

		// Fill in header views
		OnClickListener onBookNowClick = new OnClickListener() {
			public void onClick(View v) {
				startRoomRatesActivity();
			}
		};

		Property property = Db.getSelectedProperty();
		OnClickListener onReviewsClick = (!property.hasExpediaReviews()) ? null : new OnClickListener() {
			public synchronized void onClick(final View v) {
				if (!mIsStartingReviewsActivity) {
					mIsStartingReviewsActivity = true;
					Intent newIntent = new Intent(mContext, UserReviewsListActivity.class);
					newIntent.fillIn(intent, 0);
					startActivity(newIntent);
				}
			}
		};
		LayoutUtils.configureHeader(this, property, onBookNowClick, onReviewsClick);

		mScrollView = (HotelDetailsScrollView) findViewById(R.id.hotel_details_main);

		FragmentManager manager = getSupportFragmentManager();
		FragmentTransaction ft = manager.beginTransaction();

		mGalleryFragment = (HotelDetailsMiniGalleryFragment) manager.findFragmentByTag(FRAGMENT_MINI_GALLERY_TAG);
		if (mGalleryFragment == null) {
			mGalleryFragment = HotelDetailsMiniGalleryFragment.newInstance();
		}
		ft.add(R.id.hotel_details_mini_gallery_fragment_container, mGalleryFragment, FRAGMENT_MINI_GALLERY_TAG);

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
	}

	public void startRoomRatesActivity() {
		Intent roomsRatesIntent = new Intent(this, RoomsAndRatesListActivity.class);
		startActivity(roomsRatesIntent);
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
		startActivity(intent);
		overridePendingTransition(R.anim.fade_in, R.anim.explode);
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// HotelMiniGalleryFragmentListener implementation

	@Override
	public void onMiniGalleryItemClicked(Property property, Object item) {
		mScrollView.toggleFullScreenGallery();
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
