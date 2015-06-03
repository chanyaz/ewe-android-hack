package com.expedia.bookings.activity;

import org.joda.time.DateTime;

import android.animation.AnimatorSet;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelOffersResponse;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.HotelSearchParams.SearchType;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.dialog.HotelErrorDialog;
import com.expedia.bookings.fragment.HotelDetailsDescriptionFragment;
import com.expedia.bookings.fragment.HotelDetailsIntroFragment;
import com.expedia.bookings.fragment.HotelDetailsMiniGalleryFragment;
import com.expedia.bookings.fragment.HotelDetailsMiniMapFragment;
import com.expedia.bookings.fragment.HotelDetailsPricePromoFragment;
import com.expedia.bookings.server.CrossContextHelper;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.HotelUtils;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.HotelDetailsScrollView;
import com.expedia.bookings.widget.HotelDetailsScrollView.HotelDetailsMiniMapClickedListener;
import com.expedia.bookings.widget.RecyclerGallery;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.json.JSONUtils;

public class HotelDetailsFragmentActivity extends FragmentActivity implements HotelDetailsMiniMapClickedListener,
	RecyclerGallery.GalleryItemListener {

	private static final long RESUME_TIMEOUT = 20 * DateUtils.MINUTE_IN_MILLIS;
	private static final String INSTANCE_LAST_SEARCH_TIME = "INSTANCE_LAST_SEARCH_TIME";

	// Tags for this activity's fragments
	private static final String FRAGMENT_MINI_GALLERY_TAG = "FRAGMENT_MINI_GALLERY_TAG";
	private static final String FRAGMENT_PRICE_PROMO_TAG = "FRAGMENT_PRICE_PROMO_TAG";
	private static final String FRAGMENT_INTRO_TAG = "FRAGMENT_INTRO_TAG";
	private static final String FRAGMENT_MINI_MAP_TAG = "FRAGMENT_MINI_MAP_TAG";
	private static final String FRAGMENT_DESCRIPTION_TAG = "FRAGMENT_DESCRIPTION_TAG";

	// This is the position in the list that the hotel had when the user clicked on it
	public static final String EXTRA_POSITION = "EXTRA_POSITION";

	// Flag set in the intent if this activity was opened from the widget
	public static final String OPENED_FROM_WIDGET = "OPENED_FROM_WIDGET";

	private Context mContext;
	private ExpediaBookingApp mApp;

	private HotelDetailsMiniGalleryFragment mGalleryFragment;
	private HotelDetailsPricePromoFragment mPricePromoFragment;
	private HotelDetailsIntroFragment mIntroFragment;
	private HotelDetailsMiniMapFragment mMapFragment;
	private HotelDetailsDescriptionFragment mDescriptionFragment;
	private TextView mBookNowButton;
	private TextView mBookByPhoneButton;

	private DateTime mLastSearchTime;

	// In case you try to toggle too quickly
	private AnimatorSet mGalleryToggleAnimator;

	// For tracking - tells you when a user paused the Activity but came back to it
	private boolean mWasStopped;

	// To make up for a lack of FLAG_ACTIVITY_CLEAR_TASK in older Android versions
	private ActivityKillReceiver mKillReceiver;

	//////////////////////////////////////////////////////////////////////////////////////////
	// Static Methods
	//////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Create intent to open this activity in a standard way.
	 *
	 * @param context
	 * @return
	 */
	public static Intent createIntent(Context context) {
		Intent intent = new Intent(context, HotelDetailsFragmentActivity.class);
		return intent;
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// OVERRIDES
	//////////////////////////////////////////////////////////////////////////////////////////

	//----------------------------------
	// LIFECYCLE EVENTS
	//----------------------------------

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = this;
		mApp = (ExpediaBookingApp) getApplicationContext();

		Intent intent = getIntent();

		if (intent.hasExtra(Codes.SEARCH_PARAMS)) {
			HotelSearchParams params = JSONUtils.parseJSONableFromIntent(intent, Codes.SEARCH_PARAMS,
				HotelSearchParams.class);
			Db.getHotelSearch().setSearchParams(params);
		}

		if (checkFinishConditionsAndFinish()) {
			return;
		}

		// #1463: If this is the first time we're launching, clear any background downloads.  This can happen if
		// you run by this screen before the download finishes and complete a booking (so onPause() never runs
		// with a finish parameter).
		if (savedInstanceState == null) {
			BackgroundDownloader bd = BackgroundDownloader.getInstance();
			bd.cancelDownload(CrossContextHelper.KEY_INFO_DOWNLOAD);
		}
		else {
			mLastSearchTime = (DateTime) savedInstanceState.getSerializable(INSTANCE_LAST_SEARCH_TIME);
		}

		if (mLastSearchTime == null) {
			mLastSearchTime = DateTime.now();
		}

		setupHotelActivity(savedInstanceState);

		// Note: the ordering here matters. We want to register the kill receiver after the KILL_ACTIVITY broadcast is
		// sent such that instances of this Activity loaded from the widget do not get finished just as they are loaded
		mKillReceiver = new ActivityKillReceiver(this);
		mKillReceiver.onCreate();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(INSTANCE_LAST_SEARCH_TIME, mLastSearchTime);
	}

	@Override
	protected void onStart() {
		super.onStart();

		if (mWasStopped) {
			doOmnitureTracking();
			mWasStopped = false;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (checkFinishConditionsAndFinish()) {
			return;
		}

		BackgroundDownloader bd = BackgroundDownloader.getInstance();

		String selectedId = Db.getHotelSearch().getSelectedPropertyId();
		HotelOffersResponse infoResponse = Db.getHotelSearch().getHotelOffersResponse(selectedId);
		if (infoResponse != null) {
			// We may have been downloading the data here before getting it elsewhere, so cancel
			// our own download once we have data
			bd.cancelDownload(CrossContextHelper.KEY_INFO_DOWNLOAD);

			// Load the data
			mInfoCallback.onDownload(infoResponse);
		}
		else {
			if (bd.isDownloading(CrossContextHelper.KEY_INFO_DOWNLOAD)) {
				bd.registerDownloadCallback(CrossContextHelper.KEY_INFO_DOWNLOAD, mInfoCallback);
			}
			else {
				bd.startDownload(CrossContextHelper.KEY_INFO_DOWNLOAD,
					CrossContextHelper.getHotelOffersDownload(this, CrossContextHelper.KEY_INFO_DOWNLOAD),
					mInfoCallback);
			}
			mLastSearchTime = DateTime.now();
		}

		OmnitureTracking.onResume(this);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);

		if (checkFinishConditionsAndFinish()) {
			return;
		}

		setupHotelActivity(null);
	}

	@Override
	protected void onPause() {
		super.onPause();

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (isFinishing()) {
			bd.cancelDownload(CrossContextHelper.KEY_INFO_DOWNLOAD);
		}
		else {
			bd.unregisterDownloadCallback(CrossContextHelper.KEY_INFO_DOWNLOAD);
		}

		OmnitureTracking.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mKillReceiver != null) {
			mKillReceiver.onDestroy();
		}
	}

	private boolean checkFinishConditionsAndFinish() {

		if (HotelUtils.checkPhoneFinishConditionsAndFinish(this)) {
			return true;
		}

		if (JodaUtils.isExpired(mLastSearchTime, RESUME_TIMEOUT)) {
			finish();
			return true;
		}

		return false;
	}

	//----------------------------------
	// MENUS
	//----------------------------------

	@TargetApi(11)
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_hotel_details, menu);

		if (checkFinishConditionsAndFinish()) {
			return super.onCreateOptionsMenu(menu);
		}

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayUseLogoEnabled(true);

		Property property = Db.getHotelSearch().getSelectedProperty();
		HotelUtils.setupActionBarHotelNameAndRating(this, property);

		final MenuItem select = menu.findItem(R.id.menu_select_hotel);
		HotelUtils.setupActionBarCheckmark(this, select, property.isAvailable());

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home: {
			onBackPressed();
			return true;
		}
		case R.id.menu_select_hotel: {
			startActivity(HotelRoomsAndRatesActivity.createIntent(this));
			return true;
		}
		default: {
			return super.onOptionsItemSelected(item);
		}
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////////

	//----------------------------------
	// VIEW INITIALIZATION
	//----------------------------------

	private void setupHotelActivity(Bundle savedInstanceState) {
		final Intent intent = getIntent();

		setContentView(R.layout.hotel_details_main);
		getWindow().setBackgroundDrawable(null);

		boolean isUserBucketedForTest = Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHISBookAboveFoldTest);
		int testVariate = Db.getAbacusResponse().variateForTest(AbacusUtils.EBAndroidAppHISBookAboveFoldTest);

		mGalleryFragment = (HotelDetailsMiniGalleryFragment) getSupportFragmentManager().findFragmentByTag(
			FRAGMENT_MINI_GALLERY_TAG);
		if (mGalleryFragment == null) {
			boolean fromLaunch = getIntent().getBooleanExtra(HotelDetailsMiniGalleryFragment.ARG_FROM_LAUNCH, false);
			mGalleryFragment = HotelDetailsMiniGalleryFragment.newInstance(fromLaunch);
		}
		if (!mGalleryFragment.isAdded()) {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.add(R.id.hotel_details_mini_gallery_fragment_container, mGalleryFragment, FRAGMENT_MINI_GALLERY_TAG);
			ft.commit();
		}

		mPricePromoFragment = Ui.findOrAddSupportFragment(this, R.id.hotel_details_price_promo_fragment_container,
			HotelDetailsPricePromoFragment.class, FRAGMENT_PRICE_PROMO_TAG);

		mIntroFragment = Ui.findOrAddSupportFragment(this, R.id.hotel_details_intro_fragment_container,
			HotelDetailsIntroFragment.class, FRAGMENT_INTRO_TAG);

		mMapFragment = Ui.findOrAddSupportFragment(this, R.id.hotel_details_map_fragment_container,
			HotelDetailsMiniMapFragment.class, FRAGMENT_MINI_MAP_TAG);

		mDescriptionFragment = Ui.findOrAddSupportFragment(this, R.id.hotel_details_description_fragment_container,
			HotelDetailsDescriptionFragment.class, FRAGMENT_DESCRIPTION_TAG);

		// 3840. Abacus AB Testing. Let's move hotel Book Now button around.
		int bookNowButtonId = 0;
		if (isUserBucketedForTest) {
			bookNowButtonId = R.id.book_now_button_ABTest;
			Ui.findView(this, R.id.book_now_button).setVisibility(View.GONE);
			if (testVariate == AbacusUtils.HISBookAboveFoldVariate.SELECT_ROOM_ABOVE_FOLD.ordinal()) {
				mBookNowButton = Ui.findView(this, bookNowButtonId);
				mBookNowButton.setText(R.string.book_now_ab_test);
			}
		}
		else {
			bookNowButtonId = R.id.book_now_button;
			Ui.findView(this, R.id.book_now_button_ABTest).setVisibility(View.GONE);
		}
		mBookNowButton = Ui.findView(this, bookNowButtonId);
		if (Db.getHotelSearch().getSelectedProperty().isAvailable()) {
			mBookNowButton.setVisibility(View.VISIBLE);
			mBookNowButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					startActivity(HotelRoomsAndRatesActivity.createIntent(HotelDetailsFragmentActivity.this));
				}
			});
		}
		else {
			mBookNowButton.setVisibility(View.GONE);
		}

		mBookByPhoneButton = Ui.findView(this, R.id.book_by_phone_button);

		// Tracking
		if (savedInstanceState == null) {
			doOmnitureTracking();
		}

		HotelDetailsScrollView scrollView = (HotelDetailsScrollView) findViewById(R.id.hotel_details_portrait);
		if (scrollView != null) {
			scrollView.setHotelDetailsMiniMapClickedListener(this);
		}
	}

	private void doOmnitureTracking() {
		OmnitureTracking.trackPageLoadHotelsInfosite(mContext, getIntent().getIntExtra(EXTRA_POSITION, -1));
		AdTracker.trackHotelInfoSite();
	}

	private void setupBookByPhoneButton(HotelOffersResponse response) {
		if (mBookByPhoneButton == null || response == null) {
			return;
		}

		final Property property = response.getProperty();
		boolean showBookByPhone = property != null
			&& !TextUtils.isEmpty(property.getTelephoneSalesNumber())
			&& !property.isDesktopOverrideNumber()
			&& property.isAvailable();

		if (showBookByPhone) {
			mBookByPhoneButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String number = null;

					switch (User.getLoggedInLoyaltyMembershipTier(mContext)) {
					case SILVER:
						number = PointOfSale.getPointOfSale().getSupportPhoneNumberSilver();
						break;
					case GOLD:
						number = PointOfSale.getPointOfSale().getSupportPhoneNumberGold();
						break;
					}

					if (TextUtils.isEmpty(number)) {
						number = property.getTelephoneSalesNumber();
					}
					SocialUtils.call(HotelDetailsFragmentActivity.this, number);
				}
			});
			mBookByPhoneButton.setVisibility(View.VISIBLE);
		}
		else {
			mBookByPhoneButton.setVisibility(View.GONE);
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// Async loading of ExpediaServices.availability

	private final OnDownloadComplete<HotelOffersResponse> mInfoCallback = new OnDownloadComplete<HotelOffersResponse>() {
		@Override
		public void onDownload(HotelOffersResponse response) {
			// Check if we got a better response elsewhere before loading up this data
			String selectedId = Db.getHotelSearch().getSelectedPropertyId();
			HotelOffersResponse possibleBetterResponse = Db.getHotelSearch().getHotelOffersResponse(selectedId);

			if (possibleBetterResponse != null) {
				response = possibleBetterResponse;
			}
			else {
				Db.getHotelSearch().updateFrom(response);
			}

			if (response == null) {
				showErrorDialog(Ui.obtainThemeResID(mContext, R.attr.skin_errorHotelOffersHotelServiceFailureString));
				return;
			}
			else if (response.hasErrors()) {
				int messageResId;
				if (response.isHotelUnavailable()) {
					messageResId = Ui.obtainThemeResID(mContext, R.attr.skin_sorryRoomsSoldOutErrorMessage);
				}
				else {
					messageResId = Ui.obtainThemeResID(mContext, R.attr.skin_errorHotelOffersHotelServiceFailureString);
				}
				showErrorDialog(messageResId);
			}
			else if ((Db.getHotelSearch().getAvailability(selectedId) == null
				|| Db.getHotelSearch().getAvailability(selectedId).getRateCount() == 0)
				&& Db.getHotelSearch().getSearchParams().getSearchType() != SearchType.HOTEL) {
				showErrorDialog(Ui.obtainThemeResID(mContext, R.attr.skin_sorryRoomsSoldOutErrorMessage));
			}
			else {
				Db.kickOffBackgroundHotelSearchSave(mContext);
			}

			// Notify affected child fragments to refresh.

			if (mIntroFragment != null && mIntroFragment.isAdded()) {
				mIntroFragment.populateViews();
			}

			if (mDescriptionFragment != null && mDescriptionFragment.isAdded()) {
				mDescriptionFragment.populateViews();
			}

			if (mGalleryFragment != null && mGalleryFragment.isAdded()) {
				mGalleryFragment.populateViews(response.getProperty());
			}

			if (mBookByPhoneButton != null) {
				setupBookByPhoneButton(response);
			}
		}
	};

	private void showErrorDialog(int messageResId) {
		HotelErrorDialog dialog = HotelErrorDialog.newInstance();
		dialog.setMessage(messageResId);
		dialog.show(getSupportFragmentManager(), "errorDialog");
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// HotelDetailsMiniMapClickedListener implementation

	@Override
	public void onHotelDetailsMiniMapClicked() {
		Intent intent = HotelMapActivity.createIntent(this);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		startActivity(intent);
		overridePendingTransition(R.anim.fade_in, R.anim.explode);
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// HotelMiniGalleryFragmentListener implementation

	boolean isGalleryFullscreen = false;

	@Override
	public void onGalleryItemClicked(Object item) {
		// Do this if in portrait
		HotelDetailsScrollView scrollView = (HotelDetailsScrollView) findViewById(R.id.hotel_details_portrait);
		if (scrollView != null) {
			scrollView.toggleFullScreenGallery();
			return;
		}
	}

	@Override
	public void onGallerySwiped(int position) {
		mGalleryFragment.toggleSwipeIndicators(position);
	}
}
