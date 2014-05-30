package com.expedia.bookings.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelOffersResponse;
import com.expedia.bookings.data.HotelSearch;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.HotelSearchParams.SearchType;
import com.expedia.bookings.data.HotelSearchResponse;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.dialog.HotelErrorDialog;
import com.expedia.bookings.fragment.HotelDetailsDescriptionFragment;
import com.expedia.bookings.fragment.HotelDetailsIntroFragment;
import com.expedia.bookings.fragment.HotelDetailsMiniGalleryFragment;
import com.expedia.bookings.fragment.HotelDetailsMiniGalleryFragment.HotelMiniGalleryFragmentListener;
import com.expedia.bookings.fragment.HotelDetailsMiniMapFragment;
import com.expedia.bookings.fragment.HotelDetailsMiniMapFragment.HotelMiniMapFragmentListener;
import com.expedia.bookings.fragment.HotelDetailsPricePromoFragment;
import com.expedia.bookings.server.CrossContextHelper;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.HotelUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.AlphaImageView;
import com.expedia.bookings.widget.HotelDetailsScrollView;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.util.AndroidUtils;

public class HotelDetailsFragmentActivity extends FragmentActivity implements HotelMiniMapFragmentListener,
		HotelMiniGalleryFragmentListener {

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
	 * @param context
	 * @return
	 */
	public static Intent createIntent(Context context) {
		Intent intent = new Intent(context, HotelDetailsFragmentActivity.class);
		return intent;
	}

	/**
	 * Create intent to open this Activity from a widget.
	 * @param context
	 * @param appWidgetId
	 * @param params
	 * @param property
	 * @return
	 */
	public static Intent createIntent(Context context, int appWidgetId, HotelSearchParams params, Property property) {
		Intent intent = new Intent(context, HotelDetailsFragmentActivity.class);

		intent.putExtra(OPENED_FROM_WIDGET, true);

		intent.putExtra(Codes.APP_WIDGET_ID, appWidgetId);
		intent.putExtra(Codes.SEARCH_PARAMS, params.toJson().toString());
		if (property != null) {
			intent.putExtra(Codes.PROPERTY, property.toJson().toString());
		}

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

		if (intent.getBooleanExtra(OPENED_FROM_WIDGET, false)) {
			com.expedia.bookings.utils.NavUtils.sendKillActivityBroadcast(mContext);

			Property property = JSONUtils.getJSONable(getIntent(), Codes.PROPERTY, Property.class);
			if (property != null) {
				// #1496: We need to create a fake SearchResponse and stuff this Property into it, based on
				// how things are architected now
				HotelSearch search = Db.getHotelSearch();
				search.resetSearchData();
				HotelSearchResponse searchResponse = new HotelSearchResponse();
				searchResponse.addProperty(property);
				search.setSearchResponse(searchResponse);
				search.setSelectedProperty(property);
			}
			else {
				// It means we came back from the reviews activity and Db.getSelectedProperty is already valid
			}
		}

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
			OmnitureTracking.trackPageLoadHotelsInfosite(mContext, getIntent().getIntExtra(EXTRA_POSITION, -1));
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

			// Don't keep widget HotelSearch in Db after leaving, otherwise it will stick around. 1811
			if (getIntent().getBooleanExtra(OPENED_FROM_WIDGET, false)) {
				Db.getHotelSearch().resetSearchData();
			}
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

		ViewGroup titleView = Ui.inflate(this, R.layout.actionbar_hotel_name_with_stars, null);

		Property property = Db.getHotelSearch().getSelectedProperty();
		String title = property.getName();
		((TextView) titleView.findViewById(R.id.title)).setText(title);

		float rating = (float) property.getHotelRating();
		((RatingBar) titleView.findViewById(R.id.rating)).setRating(rating);

		actionBar.setCustomView(titleView);

		final MenuItem select = menu.findItem(R.id.menu_select_hotel);
		HotelUtils.setupActionBarCheckmark(this, select, property.isAvailable());

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home: {
			// TODO: this doesn't seem to be working, even on JB. But really, we know the one case
			// when the task stack should be recreated: when we've found this hotel via widget.
			//boolean shouldUpRecreateTask = NavUtils.shouldUpRecreateTask(this, intent);
			boolean shouldUpRecreateTask = Db.getHotelSearch().getSearchParams().isFromWidget();

			if (shouldUpRecreateTask) {
				Intent intent = PhoneSearchActivity.createIntent(this, true);
				// This activity is not part of the application's task, so create a new task
				// with a synthesized back stack.
				TaskStackBuilder.create(this)
						.addNextIntent(new Intent(this, LaunchActivity.class))
						.addNextIntent(intent)
						.startActivities();
				finish();
			}
			else {
				onBackPressed();
			}

			return true;
		}
		case R.id.menu_select_hotel: {
			startActivity(RoomsAndRatesListActivity.createIntent(this));
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

		mBookNowButton = Ui.findView(this, R.id.book_now_button);
		if (Db.getHotelSearch().getSelectedProperty().isAvailable()) {
			mBookNowButton.setVisibility(View.VISIBLE);
			mBookNowButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					startActivity(RoomsAndRatesListActivity.createIntent(HotelDetailsFragmentActivity.this));
				}
			});
		}
		else {
			mBookNowButton.setVisibility(View.GONE);
		}

		mBookByPhoneButton = Ui.findView(this, R.id.book_by_phone_button);

		// Tracking
		if (savedInstanceState == null) {
			OmnitureTracking.trackPageLoadHotelsInfosite(mContext, getIntent().getIntExtra(EXTRA_POSITION, -1));

			// Track here if user opened app from widget.  Currently assumes that all widget searches
			// are "nearby" - if this ever changes, this needs to be updated.
			if (intent.getBooleanExtra(OPENED_FROM_WIDGET, false)) {
				OmnitureTracking.trackSimpleEvent(this, null, null, "App.Widget.Deal.Nearby");
				mApp.broadcastSearchParamsChangedInWidget(JSONUtils.getJSONable(intent, Codes.SEARCH_PARAMS,
						HotelSearchParams.class));
			}
		}

		initLandscapeGalleryLayout();
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
					if (User.isElitePlus(mContext)) {
						SocialUtils.call(HotelDetailsFragmentActivity.this,
								PointOfSale.getPointOfSale().getSupportPhoneNumberElitePlus());
					}
					else {
						SocialUtils.call(HotelDetailsFragmentActivity.this, property.getTelephoneSalesNumber());
					}
				}
			});
			mBookByPhoneButton.setVisibility(View.VISIBLE);
		}
		else {
			mBookByPhoneButton.setVisibility(View.GONE);
		}
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
					gallery.setTranslationX(-windowWidth * 0.275f);
					ViewGroup.LayoutParams lp = pricePromo.getLayoutParams();
					lp.width = (int) (windowWidth * .45f) + 1;
					pricePromo.setLayoutParams(lp);
				}
			});
		}
	}

	private boolean checkFinishConditionsAndFinish() {
		// Attempt to load hotel data from disk
		if (Db.getHotelSearch().getSelectedProperty() == null) {
			Db.loadHotelSearchFromDisk(this);
		}

		if (Db.getHotelSearch().getSelectedProperty() == null) {
			Log.i("Detected expired DB, finishing activity.");
			finish();
			return true;
		}

		return false;
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
				showErrorDialog(R.string.e3_error_hotel_offers_hotel_service_failure);
				return;
			}
			else if (response.hasErrors()) {
				int messageResId;
				if (response.isHotelUnavailable()) {
					messageResId = R.string.error_room_is_now_sold_out;
				}
				else {
					messageResId = R.string.e3_error_hotel_offers_hotel_service_failure;
				}
				showErrorDialog(messageResId);
			}
			else if (Db.getHotelSearch().getAvailability(selectedId).getRateCount() == 0
					&& Db.getHotelSearch().getSearchParams().getSearchType() != SearchType.HOTEL) {
				showErrorDialog(R.string.error_hotel_is_now_sold_out);
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
				mGalleryFragment.populateViews();
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
	// HotelMiniMapFragmentListener implementation

	@Override
	public void onMiniMapClicked() {
		Intent intent = HotelMapActivity.createIntent(this);
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
		HotelDetailsScrollView scrollView = (HotelDetailsScrollView) findViewById(R.id.hotel_details_portrait);
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
		final View detailsFragment = findViewById(R.id.hotel_details_landscape);
		final View galleryFragment = findViewById(R.id.hotel_details_mini_gallery_fragment_container);
		final View pricePromoFragment = findViewById(R.id.hotel_details_price_promo_fragment_container);
		final View pricePromoLayout = findViewById(R.id.price_and_promo_layout);
		final AlphaImageView vipAccessIcon = (AlphaImageView) findViewById(R.id.vip_badge);
		final int windowWidth = getWindow().getDecorView().getWidth();
		final float rightSideWidth = windowWidth * .55f;

		if (mGalleryToggleAnimator != null && mGalleryToggleAnimator.isRunning()) {
			mGalleryToggleAnimator.cancel();
		}
		if (!isGalleryFullscreen) {
			mGalleryToggleAnimator = new AnimatorSet();
			mGalleryToggleAnimator.playTogether(
				ObjectAnimator.ofFloat(detailsFragment, "translationX", windowWidth),
				ObjectAnimator.ofFloat(galleryFragment, "translationX", 0.0f),
				ObjectAnimator.ofFloat(pricePromoLayout, "translationX", -rightSideWidth, 0.0f),
				ObjectAnimator.ofFloat(vipAccessIcon, "translationX", -rightSideWidth, 0.0f),
				ObjectAnimator.ofInt(vipAccessIcon, "drawAlpha", 255, 0)
			);
			mGalleryToggleAnimator.addListener(new AnimatorListenerAdapter() {
				@TargetApi(11)
				@Override
				public void onAnimationStart(Animator arg0) {
					ViewGroup.LayoutParams lp = pricePromoFragment.getLayoutParams();
					lp.width = windowWidth;
					pricePromoFragment.setLayoutParams(lp);

					mPricePromoFragment.setVipIconEnabled(false);
				}
			});
			mGalleryToggleAnimator.start();
		}
		else {
			mGalleryToggleAnimator = new AnimatorSet();
			mGalleryToggleAnimator.playTogether(
				ObjectAnimator.ofFloat(detailsFragment, "translationX", 0.0f),
				ObjectAnimator.ofFloat(galleryFragment, "translationX", -rightSideWidth / 2.0f),
				ObjectAnimator.ofFloat(pricePromoLayout, "translationX", -rightSideWidth),
				ObjectAnimator.ofFloat(vipAccessIcon, "translationX", -rightSideWidth),
				ObjectAnimator.ofInt(vipAccessIcon, "drawAlpha", 0, 255)
			);
			mGalleryToggleAnimator.addListener(new AnimatorListenerAdapter() {
				@TargetApi(11)
				@Override
				public void onAnimationEnd(Animator arg0) {
					ViewGroup.LayoutParams lp = pricePromoFragment.getLayoutParams();
					lp.width = (int) (windowWidth * .45f) + 1;
					pricePromoFragment.setLayoutParams(lp);

					pricePromoLayout.setTranslationX(0f);
					vipAccessIcon.setTranslationX(0f);

					mPricePromoFragment.setVipIconEnabled(true);
				}
			});
			mGalleryToggleAnimator.start();
		}

		isGalleryFullscreen = !isGalleryFullscreen;
	}
}
