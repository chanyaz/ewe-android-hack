package com.expedia.bookings.activity;

import java.util.Date;

import android.app.ActionBar;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.data.CheckoutDataLoader;
import com.expedia.bookings.data.CreditCardType;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.fragment.FlightCheckoutFragment;
import com.expedia.bookings.fragment.FlightCheckoutFragment.CheckoutInformationListener;
import com.expedia.bookings.fragment.FlightTripOverviewFragment;
import com.expedia.bookings.fragment.FlightTripOverviewFragment.DisplayMode;
import com.expedia.bookings.fragment.FlightTripPriceFragment;
import com.expedia.bookings.fragment.FlightTripPriceFragment.FlightTripPriceFragmentListener;
import com.expedia.bookings.fragment.LoginConfirmLogoutDialogFragment.DoLogoutListener;
import com.expedia.bookings.fragment.LoginFragment.LogInListener;
import com.expedia.bookings.fragment.SlideToPurchaseFragment;
import com.expedia.bookings.fragment.WalletFragment;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.ActionBarNavUtils;
import com.expedia.bookings.utils.Akeakamai;
import com.expedia.bookings.utils.BookingInfoUtils;
import com.expedia.bookings.utils.FlightUtils;
import com.expedia.bookings.utils.Images;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.ScrollView;
import com.expedia.bookings.widget.ScrollView.OnScrollListener;
import com.expedia.bookings.widget.SlideToWidget.ISlideToListener;
import com.expedia.bookings.widget.TouchableFrameLayout;
import com.mobiata.android.Log;
import com.mobiata.flightlib.utils.DateTimeUtils;

public class FlightTripOverviewActivity extends FragmentActivity implements LogInListener,
	CheckoutInformationListener, ISlideToListener, DoLogoutListener,
	FlightTripPriceFragmentListener {

	public static final String TAG_OVERVIEW_FRAG = "TAG_OVERVIEW_FRAG";
	public static final String TAG_CHECKOUT_FRAG = "TAG_CHECKOUT_FRAG";
	public static final String TAG_PRICE_BAR_BOTTOM_FRAG = "TAG_PRICE_BAR_BOTTOM_FRAG";
	public static final String TAG_SLIDE_TO_PURCHASE_FRAG = "TAG_SLIDE_TO_PURCHASE_FRAG";

	public static final String STATE_TAG_MODE = "STATE_TAG_MODE";
	public static final String STATE_TAG_LOADED_DB_INFO = "STATE_TAG_LOADED_DB_INFO";

	//We only want to load from disk once: when the activity is first started
	private boolean mLoadedDbInfo = false;
	private boolean mSafeToAttach = true;

	private FlightTripOverviewFragment mOverviewFragment;
	private FlightTripPriceFragment mPriceBottomFragment;
	private FlightCheckoutFragment mCheckoutFragment;
	private SlideToPurchaseFragment mSlideToPurchaseFragment;

	private ViewGroup mContentRoot;
	private ViewGroup mOverviewContainer;
	private ViewGroup mCheckoutContainer;
	private TouchableFrameLayout mCheckoutBlocker;
	private View mBelowOverviewSpacer;
	private ImageView mBgImageView;

	private ScrollViewListener mScrollViewListener;
	private ScrollView mContentScrollView;

	private MenuItem mCheckoutMenuItem;

	private DisplayMode mDisplayMode = DisplayMode.OVERVIEW;

	private int mStackedHeight = 0;
	private int mUnstackedHeight = 0;
	private float mLastCheckoutPercentage = 0f;

	// To make up for a lack of FLAG_ACTIVITY_CLEAR_TASK in older Android versions
	private ActivityKillReceiver mKillReceiver;

	private enum TrackingMode {
		OVERVIEW,
		CHECKOUT,
		SLIDE_TO_PURCHASE
	}

	// This variable exists to ensure that the correct tracking event gets called the correct number of times
	private TrackingMode mLastTrackingMode;

	private enum BottomBarMode {
		PRICE_BAR,
		SLIDE_TO_PURCHASE
	}

	// Go to a mode on resume, if we need to change state during an unsafe time
	private BottomBarMode mBottomBarMode;

	private boolean mIsBailing = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (!ExpediaBookingApp.useTabletInterface(this)) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		// Recover data if it was flushed from memory
		if (Db.getTripBucket().isEmpty()) {
			boolean wasSuccess = Db.loadTripBucket(this);
			if (!wasSuccess || Db.getTripBucket().getFlight() == null) {
				finish();
				mIsBailing = true;
			}
		}

		super.onCreate(savedInstanceState);

		if (mIsBailing) {
			return;
		}

		setContentView(R.layout.activity_flight_overview_and_checkout);

		mKillReceiver = new ActivityKillReceiver(this);
		mKillReceiver.onCreate();

		mBgImageView = Ui.findView(this, R.id.background_bg_view);
		Point portrait = Ui.getPortraitScreenSize(this);
		final String code = Db.getTripBucket().getFlight().getFlightSearchParams().getArrivalLocation().getDestinationId();
		final String url = new Akeakamai(Images.getFlightDestination(code)) //
			.resizeExactly(portrait.x, portrait.y) //
			.build();
		new PicassoHelper.Builder(mBgImageView).applyBlurTransformation(true).setPlaceholder(R.drawable.default_flights_background_blurred).build().load(url);
		mContentRoot = Ui.findView(this, R.id.content_root);
		mContentScrollView = Ui.findView(this, R.id.content_scroll_view);
		mOverviewContainer = Ui.findView(this, R.id.trip_overview_container);
		mCheckoutContainer = Ui.findView(this, R.id.trip_checkout_container);
		mBelowOverviewSpacer = Ui.findView(this, R.id.below_overview_spacer);
		mCheckoutBlocker = Ui.findView(this, R.id.checkout_event_blocker);

		if (savedInstanceState != null) {
			mLoadedDbInfo = savedInstanceState.getBoolean(STATE_TAG_LOADED_DB_INFO, false) && Db.hasBillingInfo();
		}
		//We load things from disk in the background
		loadCachedData(false);

		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_TAG_MODE)) {
			mDisplayMode = DisplayMode.valueOf(savedInstanceState.getString(STATE_TAG_MODE));
		}

		addOverviewFragment();

		mScrollViewListener = new ScrollViewListener();
		mContentScrollView.addOnScrollListener(mScrollViewListener);
		mContentScrollView.setOnTouchListener(mScrollViewListener);

		AdTracker.trackFlightCheckoutStarted();
	}

	@Override
	public void onResume() {
		super.onResume();

		OmnitureTracking.onResume(this);
	}

	@Override
	protected void onPostResume() {
		super.onPostResume();

		mSafeToAttach = true;

		mOverviewContainer.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				if (setContainerHeights() && mOverviewContainer.getHeight() >= 0) {
					//We want this to be attached regardless of mode
					attachCheckoutFragment();

					if (mDisplayMode.compareTo(DisplayMode.CHECKOUT) == 0) {
						gotoCheckoutMode(true, true);
						if (mScrollViewListener.getScrollY() >= mScrollViewListener.getCheckoutScrollY()) {
							mOverviewContainer.getViewTreeObserver().removeOnPreDrawListener(this);
						}
					}
					else {
						mOverviewContainer.getViewTreeObserver().removeOnPreDrawListener(this);
						gotoOverviewMode(false);
					}
				}
				return true;
			}

		});

		// Normally we won't need this; but if we try to attach when it's not safe
		// we will want to make the changes later.
		if (mBottomBarMode == BottomBarMode.PRICE_BAR) {
			addPriceBarFragment(false);
		}
		else if (mBottomBarMode == BottomBarMode.SLIDE_TO_PURCHASE) {
			addSlideToCheckoutFragment();
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		mSafeToAttach = false;

		mLastTrackingMode = null;

		//In the case that we go back to the start of the app, we want the CC number to be cleared when we return
		if (this.isFinishing()) {
			clearCCNumber();
		}

		OmnitureTracking.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// When leaving the activity, we want to ensure that if the user returns, they do not see the card fee on the
		// overview, even if we have a card selected for them in the background.
		if (isFinishing() && Db.getTripBucket().getFlight() != null) {
			FlightTrip flightTrip = Db.getTripBucket().getFlight().getFlightTrip();
			if (flightTrip != null) {
				flightTrip.setShowFareWithCardFee(false);
			}
		}

		if (mKillReceiver != null) {
			mKillReceiver.onDestroy();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle out) {
		super.onSaveInstanceState(out);
		out.putString(STATE_TAG_MODE, mDisplayMode.name());
		out.putBoolean(STATE_TAG_LOADED_DB_INFO, mLoadedDbInfo);
	}

	private void clearCCNumber() {
		try {
			Db.getBillingInfo().setNumber(null);
		}
		catch (Exception ex) {
			Log.e("Error clearing billingInfo card number", ex);
		}
	}

	private void loadCachedData(boolean wait) {
		if (!mLoadedDbInfo) {
			CheckoutDataLoader.CheckoutDataLoadedListener listener = new CheckoutDataLoader.CheckoutDataLoadedListener() {
				@Override
				public void onCheckoutDataLoaded(boolean wasSuccessful) {
					mLoadedDbInfo = wasSuccessful;
				}
			};
			CheckoutDataLoader.getInstance().loadCheckoutData(this, true, true, listener, wait);
		}
	}

	private void googleWalletTripPaymentTypeCheck() {
		//If we have a valid trip, we check if google wallet is a supported payment type and tell the checkout fragment
		FlightCheckoutFragment checkoutFrag = mCheckoutFragment;
		if (checkoutFrag == null) {
			//Sometimes this will be called on rotation, so our instance may exist, but be otherwise un-initialized for this orientation.
			checkoutFrag = Ui.findSupportFragment(this, TAG_CHECKOUT_FRAG);
		}
		if (checkoutFrag != null && Db.getTripBucket() != null
			&& Db.getTripBucket().getFlight() != null
			&& !TextUtils.isEmpty(Db.getTripBucket().getFlight().getFlightTrip().getItineraryNumber())) {
			// Disable Google Wallet if not a valid payment type
			if (!Db.getTripBucket().getFlight().isCardTypeSupported(CreditCardType.GOOGLE_WALLET)) {
				Log.d("disableGoogleWallet: safeGoogleWalletTripPaymentTypeCheck");
				checkoutFrag.disableGoogleWallet();
			}
		}
	}

	public void attachCheckoutFragment() {
		if (mSafeToAttach) {
			boolean refreshCheckoutData = !mLoadedDbInfo;
			loadCachedData(
				true);//because of the sLoaded variable, this will almost always do no work except if we end up in a strange state
			mCheckoutFragment = Ui.findSupportFragment(this, TAG_CHECKOUT_FRAG);
			if (mCheckoutFragment == null) {
				mCheckoutFragment = FlightCheckoutFragment.newInstance();
			}
			else if (refreshCheckoutData) {
				//Incase we only now finished loading cached data...
				mCheckoutFragment.refreshData();
			}

			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			if (mCheckoutFragment.isDetached()) {
				transaction.attach(mCheckoutFragment);
				transaction.commit();
			}
			else if (!mCheckoutFragment.isAdded()) {
				transaction.add(R.id.trip_checkout_container, mCheckoutFragment, TAG_CHECKOUT_FRAG);
				transaction.commit();
			}

			//If the trip call is already complete, lets ensure that the google wallet state is ok
			googleWalletTripPaymentTypeCheck();
		}
	}

	private void addOverviewFragment() {
		mOverviewFragment = Ui.findSupportFragment(this, TAG_OVERVIEW_FRAG);
		if (mOverviewFragment == null) {
			mOverviewFragment = FlightTripOverviewFragment.newInstance(mDisplayMode);
		}
		if (!mOverviewFragment.isAdded()) {
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			transaction.add(R.id.trip_overview_container, mOverviewFragment, TAG_OVERVIEW_FRAG);
			transaction.commit();
		}
	}

	private void addSlideToCheckoutFragment() {
		if (mSafeToAttach) {
			mBottomBarMode = null;

			mSlideToPurchaseFragment = Ui.findSupportFragment(this, TAG_SLIDE_TO_PURCHASE_FRAG);
			if (mSlideToPurchaseFragment == null) {
				String text = FlightUtils.getSlideToPurchaseString(this, Db.getTripBucket().getFlight());
				mSlideToPurchaseFragment = SlideToPurchaseFragment.newInstance(text);
			}
			if (!mSlideToPurchaseFragment.isAdded()) {
				FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
				transaction.replace(R.id.trip_price_container_bottom, mSlideToPurchaseFragment,
					TAG_SLIDE_TO_PURCHASE_FRAG);
				transaction.commit();
			}
		}
		else {
			mBottomBarMode = BottomBarMode.SLIDE_TO_PURCHASE;
		}
	}

	private void addPriceBarFragment(boolean waitForTransactionCompletion) {
		if (mSafeToAttach) {
			mBottomBarMode = null;

			mPriceBottomFragment = Ui.findSupportFragment(this, TAG_PRICE_BAR_BOTTOM_FRAG);
			if (mPriceBottomFragment == null) {
				mPriceBottomFragment = FlightTripPriceFragment.newInstance();
			}
			if (!mPriceBottomFragment.isAdded()) {
				FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
				transaction.replace(R.id.trip_price_container_bottom, mPriceBottomFragment, TAG_PRICE_BAR_BOTTOM_FRAG);
				transaction.commit();

				if (waitForTransactionCompletion) {
					getSupportFragmentManager().executePendingTransactions();
				}
			}
		}
		else {
			mBottomBarMode = BottomBarMode.PRICE_BAR;
		}
	}

	public void gotoOverviewMode(boolean animate) {
		mDisplayMode = DisplayMode.OVERVIEW;
		mCheckoutBlocker.setBlockNewEventsEnabled(true);

		//Make sure sizes are as they should be
		setContainerHeights();

		addPriceBarFragment(true);
		if (mPriceBottomFragment != null) {
			mPriceBottomFragment.showPriceChange();
		}

		if (mOverviewFragment != null && mOverviewFragment.isAdded()) {
			if (animate) {
				doScroll(true, 0);
			}
			else {
				setCheckoutPercent(0f, true);
			}
		}

		setActionBarOverviewMode();
		modeChangeComplete();

		if (mLastTrackingMode != TrackingMode.OVERVIEW) {
			mLastTrackingMode = TrackingMode.OVERVIEW;
			OmnitureTracking.trackPageLoadFlightRateDetailsOverview(this);
		}
	}

	public void gotoCheckoutMode(boolean animate, boolean updateScrollPosition) {
		mDisplayMode = DisplayMode.CHECKOUT;

		//Make sure sizes are as they should be
		setContainerHeights();

		if (mOverviewFragment != null && mOverviewFragment.isAdded()) {

			if (animate) {
				doScroll(true, (int) mOverviewFragment.getScrollOffsetForPercentage(1f));
			}
			else {
				setCheckoutPercent(1f, updateScrollPosition);
			}

			if (mCheckoutFragment != null) {
				mCheckoutFragment.updateViewVisibilities();
			}
		}

		setActionBarCheckoutMode();
		mCheckoutBlocker.setBlockNewEventsEnabled(false);
		modeChangeComplete();
	}

	private void doScroll(final boolean animate, final int y) {
		mContentScrollView.post(new Runnable() {
			@Override
			public void run() {
				mContentScrollView.scrollTo(0, mScrollViewListener.getScrollY());
				if (animate) {
					mContentScrollView.smoothScrollTo(0, y);
				}
				else {
					mContentScrollView.scrollTo(0, y);
				}
			}
		});
	}

	private void modeChangeComplete() {
		// In checkout mode, we always want to show the price at bottom with the card fee
		Db.getTripBucket().getFlight().getFlightTrip().setShowFareWithCardFee(true);

		// We only want to update the price bottom fragment if it is added to the content right now, otherwise
		// it will attempt to find a context (that doesn't exist) and blow up, despite having a reference to it.
		if (Ui.isAdded(mPriceBottomFragment)) {
			mPriceBottomFragment.bind();
		}

		if (mOverviewFragment != null) {
			mOverviewFragment.updateCardInfoText();
		}
	}

	private boolean setContainerHeights() {
		boolean retVal = true;
		if (mOverviewFragment != null && mOverviewFragment.isAdded()) {
			mStackedHeight = mOverviewFragment.getStackedHeight();
			mUnstackedHeight = mOverviewFragment.getUnstackedHeight();
			LayoutParams overviewContainerParams = mOverviewContainer.getLayoutParams();
			if (overviewContainerParams != null && mUnstackedHeight > 0) {
				overviewContainerParams.height = mUnstackedHeight;
				mOverviewContainer.setLayoutParams(overviewContainerParams);

			}
			else {
				retVal = false;
			}

			LayoutParams spacerParams = mBelowOverviewSpacer.getLayoutParams();
			if (spacerParams != null) {
				spacerParams.height = mContentScrollView.getHeight() - mStackedHeight;
				mBelowOverviewSpacer.setLayoutParams(spacerParams);
			}
			else {
				retVal = false;
			}

			mScrollViewListener.updateThresh(mUnstackedHeight - mStackedHeight, (int) (mUnstackedHeight / 2f));
		}
		return retVal;
	}

	private class ScrollViewListener implements OnScrollListener,
		OnTouchListener {

		private boolean mTouchDown = false;
		private int mScrollY;
		private int mMidY;
		private int mCheckoutY;

		public void updateThresh(int checkoutY, int midY) {
			mCheckoutY = checkoutY;
			mMidY = midY;
		}

		public boolean getIsCurrentlyTouching() {
			return mTouchDown;
		}

		public int getScrollY() {
			return mScrollY;
		}

		public int getCheckoutScrollY() {
			return mCheckoutY;
		}

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				mTouchDown = true;
			}
			else if (event.getAction() == MotionEvent.ACTION_UP) {
				mTouchDown = false;
			}
			if (event.getAction() == MotionEvent.ACTION_UP) {
				if (mScrollY < mMidY) {
					gotoOverviewMode(true);
				}
				else if (mScrollY >= mMidY && (mScrollY <= mCheckoutY || mDisplayMode != DisplayMode.CHECKOUT)) {
					gotoCheckoutMode(true, true);
				}
				else {
					doScroll(true, mScrollY);
				}
			}

			return false;
		}

		@Override
		public void onScrollChanged(ScrollView scrollView, int x, int y, int oldx, int oldy) {
			mScrollY = y;
			if (mOverviewFragment != null) {
				float percentage = 1f - ((float) mScrollY) / mCheckoutY;
				setCheckoutPercent(1f - percentage, false);
			}
		}
	}

	//The percentage we are to checkout mode...
	private void setCheckoutPercent(float percentage, boolean doScroll) {
		if (percentage < 0) {
			percentage = 0;
		}
		else if (percentage > 1) {
			percentage = 1;
		}

		if (doScroll && mOverviewFragment != null) {
			int scrollY = (int) mOverviewFragment.getScrollOffsetForPercentage(percentage);
			mContentScrollView.scrollTo(0, scrollY);
		}
		else {
			if (mOverviewFragment != null) {
				mOverviewFragment.setExpandedPercentage(1f - percentage);
			}

			mCheckoutContainer
				.setTranslationY((Math.max(mContentRoot.getHeight(), mUnstackedHeight) - mOverviewContainer
					.getHeight()) * (1f - percentage));

			if (percentage > 0 && mCheckoutContainer.getVisibility() != View.VISIBLE) {
				mCheckoutContainer.setVisibility(View.VISIBLE);
			}
			else if (percentage == 0 && mCheckoutContainer.getVisibility() == View.VISIBLE) {
				mCheckoutContainer.setVisibility(View.INVISIBLE);
			}

			mLastCheckoutPercentage = percentage;
		}
	}

	private void setActionBarOverviewMode() {
		ActionBar actionBar = getActionBar();
		actionBar.setCustomView(null);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_HOME_AS_UP
			| ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_USE_LOGO);
		actionBar.setTitle(getString(R.string.overview_btn));

		this.supportInvalidateOptionsMenu();

	}

	private void setActionBarCheckoutMode() {
		FlightSearchParams params = Db.getTripBucket().getFlight().getFlightSearchParams();
		int numTravelers = params.getNumAdults() + params.getNumChildren();
		String travelers = getResources().getQuantityString(R.plurals.number_of_travelers_TEMPLATE, numTravelers,
			numTravelers);

		FlightTrip trip = Db.getTripBucket().getFlight().getFlightTrip();
		String cityName = StrUtils.getWaypointCityOrCode(trip.getLeg(0).getLastWaypoint());
		String yourTripToStr = String.format(getString(R.string.your_trip_to_TEMPLATE), cityName);

		Date depDate = DateTimeUtils
			.getTimeInLocalTimeZone(trip.getLeg(0).getFirstWaypoint().getMostRelevantDateTime());
		Date retDate = DateTimeUtils.getTimeInLocalTimeZone(trip.getLeg(trip.getLegCount() - 1).getLastWaypoint()
			.getMostRelevantDateTime());
		String dateRange = DateUtils.formatDateRange(this, depDate.getTime(), retDate.getTime(),
			DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_WEEKDAY
				| DateUtils.FORMAT_ABBREV_MONTH
		);

		View customView = Ui.inflate(this, R.layout.action_bar_flight_results, null);
		TextView titleTextView = Ui.findView(customView, R.id.title_text_view);
		TextView subtitleTextView = Ui.findView(customView, R.id.subtitle_text_view);

		titleTextView.setText(yourTripToStr);
		subtitleTextView.setText(travelers + ", " + dateRange);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_HOME_AS_UP
			| ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_USE_LOGO);
		actionBar.setCustomView(customView);

		this.supportInvalidateOptionsMenu();

	}

	@Override
	public void onBackPressed() {
		if (mDisplayMode.compareTo(DisplayMode.CHECKOUT) == 0) {
			gotoOverviewMode(true);
		}
		else {
			clearCCNumber();
			super.onBackPressed();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (mCheckoutFragment == null) {
			mSafeToAttach = true;
			attachCheckoutFragment();
		}
		if (WalletFragment.isRequestCodeFromWalletFragment(requestCode)) {
			mCheckoutFragment.onActivityResult(requestCode, resultCode, data);
		}
		else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	public void displayCheckoutButton(boolean visible) {
		if (mCheckoutMenuItem != null) {
			mCheckoutMenuItem.setVisible(visible);
			mCheckoutMenuItem.setEnabled(visible);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (menu != null) {
			if (mDisplayMode.compareTo(DisplayMode.CHECKOUT) == 0) {
				displayCheckoutButton(false);
			}
			else {
				displayCheckoutButton(true);
			}
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_checkout, menu);
		mCheckoutMenuItem = ActionBarNavUtils.setupActionLayoutButton(this, menu, R.id.menu_checkout);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if (mDisplayMode == DisplayMode.CHECKOUT) {
				onBackPressed();
			}
			else {
				clearCCNumber();

				Intent intent = new Intent(this, FlightSearchResultsActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
				FlightTrip trip = Db.getTripBucket().getFlight().getFlightTrip();
				intent.putExtra(FlightSearchResultsActivity.EXTRA_DESELECT_LEG_ID, trip.getLeg(trip.getLegCount() - 1)
					.getLegId());
				startActivity(intent);
			}
			return true;
		case R.id.menu_checkout:
			if (mOverviewFragment != null && mDisplayMode == DisplayMode.OVERVIEW) {
				gotoCheckoutMode(true, true);
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onLoginStarted() {
		// Do nothing?
	}

	@Override
	public void onLoginCompleted() {
		mPriceBottomFragment.startCreateTripDownload();
	}

	@Override
	public void onLoginFailed() {
		// TODO: Update UI to show that we're no longer logged in
	}

	//Checkout listener
	@Override
	public void checkoutInformationIsValid() {
		if (mDisplayMode.compareTo(DisplayMode.CHECKOUT) == 0) {

			if (mLastTrackingMode != TrackingMode.SLIDE_TO_PURCHASE) {
				mLastTrackingMode = TrackingMode.SLIDE_TO_PURCHASE;
				OmnitureTracking.trackPageLoadFlightCheckoutSlideToPurchase(this);
			}

			//Bring in the slide to checkout view
			addSlideToCheckoutFragment();

			//Scroll to bottom to display legal text
			doScroll(true, mCheckoutContainer.getBottom());
		}
	}

	@Override
	public void checkoutInformationIsNotValid() {
		if (mDisplayMode.compareTo(DisplayMode.CHECKOUT) == 0) {

			if (mLastTrackingMode != TrackingMode.CHECKOUT) {
				mLastTrackingMode = TrackingMode.CHECKOUT;
				OmnitureTracking.trackPageLoadFlightCheckoutInfo(this);
			}

			//Bring in the price bar
			addPriceBarFragment(false);
		}
	}

	@Override
	public void onBillingInfoChange() {
		if (Ui.isAdded(mPriceBottomFragment)) {
			mPriceBottomFragment.bind();
		}
	}

	@Override
	public void onLogout() {
		// Don't care, already handled elsewhere
	}

	//////////////////////////////////////////////////////////////////////////
	// FlightTripPriceFragmentListener

	@Override
	public void onCreateTripFinished() {
		googleWalletTripPaymentTypeCheck();

		if (mCheckoutFragment != null) {
			mCheckoutFragment.refreshData();
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// ISlideToListener
	// (for FlightSlideToPurchaseFragment)

	@Override
	public void onSlideStart() {
	}

	@Override
	public void onSlideAllTheWay() {
		if (!BookingInfoUtils
			.migrateRequiredCheckoutDataToDbBillingInfo(this, LineOfBusiness.FLIGHTS, Db.getTravelers().get(0), true)) {
			if (mSlideToPurchaseFragment != null) {
				mSlideToPurchaseFragment.resetSlider();
			}
			Ui.showToast(this, R.string.please_enter_a_valid_email_address);
			gotoCheckoutMode(false, true);
		}

		//IMPORTANT: Above we ensure we have the correct email address for booking, and we put it in billingInfo.
		//However, on the api the information associated with the primary traveler takes precedence. So in
		//FlightBookingActivity we make sure that the first traveler in Db.getTravelers() gets copied and set up to use
		//the email address in BillingInfo.
		//THIS STEP MUST HAPPEN, so if this code is getting copied or refactored or whatever, you must ensure that the
		//the "mainFlightPassenger" of the booking request has the correct email address, without altering our stored traveler info.

		//Seal the deal
		Intent intent = new Intent(this, FlightBookingActivity.class);
		startActivity(intent);
	}

	@Override
	public void onSlideAbort() {
	}

	//////////////////////////////////////////////////////////////////////////
	// DoLogoutListener

	@Override
	public void doLogout() {
		mCheckoutFragment.doLogout();
		Events.post(new Events.CreateTripDownloadRetry());
	}
}
