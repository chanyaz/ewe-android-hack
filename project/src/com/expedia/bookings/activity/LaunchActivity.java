package com.expedia.bookings.activity;

import java.lang.reflect.Method;
import java.util.List;

import org.joda.time.DateTime;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.L2ImageCache;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.dialog.GooglePlayServicesDialog;
import com.expedia.bookings.fragment.ConfirmLogoutDialogFragment.DoLogoutListener;
import com.expedia.bookings.fragment.ItinItemListFragment;
import com.expedia.bookings.fragment.ItinItemListFragment.ItinItemListFragmentListener;
import com.expedia.bookings.fragment.LaunchFragment;
import com.expedia.bookings.fragment.LaunchFragment.LaunchFragmentListener;
import com.expedia.bookings.fragment.TravelocityLauncherFragment;
import com.expedia.bookings.notification.Notification;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.DebugMenu;
import com.expedia.bookings.utils.ExpediaDebugUtil;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.DisableableViewPager;
import com.expedia.bookings.widget.ItinListView.OnListModeChangedListener;
import com.mobiata.android.Log;
import com.mobiata.android.hockey.HockeyPuck;
import com.mobiata.android.util.AndroidUtils;

public class LaunchActivity extends FragmentActivity implements OnListModeChangedListener,
		ItinItemListFragmentListener, LaunchFragmentListener, DoLogoutListener {

	public static final String ARG_FORCE_SHOW_WATERFALL = "ARG_FORCE_SHOW_WATERFALL";
	public static final String ARG_FORCE_SHOW_ITIN = "ARG_FORCE_SHOW_ITIN";
	public static final String ARG_JUMP_TO_NOTIFICATION = "ARG_JUMP_TO_NOTIFICATION";

	private static final int REQUEST_SETTINGS = 1;
	private static final int PAGER_POS_WATERFALL = 0;

	private static final int PAGER_POS_ITIN = 1;

	private LaunchFragment mLaunchFragment;
	private ItinItemListFragment mItinListFragment;

	private PagerAdapter mPagerAdapter;
	private DisableableViewPager mViewPager;
	private int mPagerPosition = PAGER_POS_WATERFALL;
	private boolean mHasMenu = false;

	private HockeyPuck mHockeyPuck;

	private String mJumpToItinId = null;

	//////////////////////////////////////////////////////////////////////////////////////////
	// Static Methods
	//////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Create intent to open this activity and jump straight to a particular itin item.
	 * @param context
	 * @return
	 */
	public static Intent createIntent(Context context, Notification notification) {
		Intent intent = new Intent(context, LaunchActivity.class);
		intent.putExtra(ARG_JUMP_TO_NOTIFICATION, notification.toJson().toString());
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

		// Even though we don't use the url directly anywhere, Android OS needs a way
		// to differentiate multiple intents to this same activity.
		// http://developer.android.com/reference/android/content/Intent.html#filterEquals(android.content.Intent)
		intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

		return intent;
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// Lifecycle Methods
	//////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().setFormat(android.graphics.PixelFormat.RGBA_8888);
		requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

		setContentView(R.layout.activity_launch);
		getWindow().setBackgroundDrawable(null);

		// View Pager
		mPagerAdapter = new PagerAdapter(getSupportFragmentManager());
		mViewPager = Ui.findView(this, R.id.viewpager);
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setOnPageChangeListener(new SimpleOnPageChangeListener() {
			@Override
			public void onPageScrollStateChanged(int state) {
				super.onPageScrollStateChanged(state);
				if (mLaunchFragment != null
						&& (state == ViewPager.SCROLL_STATE_IDLE || state == ViewPager.SCROLL_STATE_SETTLING)) {
					mLaunchFragment.startMarquee();
				}
			}

			@Override
			public void onPageSelected(int position) {
				if (mViewPager != null && position != mPagerPosition) {
					if (position == PAGER_POS_WATERFALL) {
						gotoWaterfall();
					}
					else if (position == PAGER_POS_ITIN) {
						gotoItineraries();
					}
				}
			}
		});

		// Tabs
		Tab shopTab = getActionBar().newTab().setText(R.string.shop).setTabListener(mShopTabListener);
		Tab itineraryTab = getActionBar().newTab().setText(R.string.trips).setTabListener(mItineraryTabListener);

		ActionBar actionBar = getActionBar();
		enableEmbeddedTabs(actionBar);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.addTab(shopTab, PAGER_POS_WATERFALL);
		actionBar.addTab(itineraryTab, PAGER_POS_ITIN);

		Intent intent = getIntent();
		if (intent.getBooleanExtra(ARG_FORCE_SHOW_WATERFALL, false)) {
			// No need to do anything special, waterfall is the default behavior anyway
		}
		else if (intent.hasExtra(ARG_JUMP_TO_NOTIFICATION)) {
			handleArgJumpToNotification(intent);
			gotoItineraries();
		}
		else if (intent.getBooleanExtra(ARG_FORCE_SHOW_ITIN, false)) {
			gotoItineraries();
		}
		else if (haveTimelyItinItem()) {
			gotoItineraries();
		}

		// Debug code to notify QA to open ExpediaDebug app
		ExpediaDebugUtil.showExpediaDebugToastIfNeeded(this);

		// HockeyApp init
		mHockeyPuck = new HockeyPuck(this, getString(R.string.hockey_app_id), !AndroidUtils.isRelease(this));
		mHockeyPuck.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Clear mem caches
		L2ImageCache.sGeneralPurpose.clearMemoryCache();
		L2ImageCache.sDestination.clearMemoryCache();

		//HockeyApp crash
		mHockeyPuck.onResume();

		GooglePlayServicesDialog gpsd = new GooglePlayServicesDialog(this);
		gpsd.startChecking();

		supportInvalidateOptionsMenu();

		OmnitureTracking.onResume(this);
		AdTracker.trackViewHomepage();
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (isFinishing() && mLaunchFragment != null) {
			mLaunchFragment.cleanUp();
		}

		OmnitureTracking.onPause();
	}

	@Override
	public void onBackPressed() {
		if (mViewPager.getCurrentItem() == PAGER_POS_ITIN) {
			if (mItinListFragment != null && mItinListFragment.isInDetailMode()) {
				mItinListFragment.hideDetails();
				return;
			}

			mViewPager.setCurrentItem(PAGER_POS_WATERFALL);
			return;
		}

		super.onBackPressed();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		mHockeyPuck.onSaveInstanceState(outState);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		if (intent.getBooleanExtra(ARG_FORCE_SHOW_WATERFALL, false)) {
			gotoWaterfall();
		}
		else if (intent.hasExtra(ARG_JUMP_TO_NOTIFICATION)) {
			handleArgJumpToNotification(intent);
			gotoItineraries();
		}
		else if (intent.getBooleanExtra(ARG_FORCE_SHOW_ITIN, false)) {
			gotoItineraries();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (mLaunchFragment != null && requestCode == REQUEST_SETTINGS
				&& resultCode == ExpediaBookingPreferenceActivity.RESULT_CHANGED_PREFS) {
			mLaunchFragment.reset();
			Db.getHotelSearch().resetSearchData();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_launch, menu);

		DebugMenu.onCreateOptionsMenu(this, menu);
		if (!AndroidUtils.isRelease(this)) {
			mHockeyPuck.onCreateOptionsMenu(menu);
		}
		mHasMenu = super.onCreateOptionsMenu(menu);
		return mHasMenu;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean retVal = super.onPrepareOptionsMenu(menu);
		if (menu != null) {
			boolean isInItinMode = false;
			boolean addNewItinButtonEnabled = false;
			boolean loginBtnEnabled = false;
			boolean logoutBtnEnabled = false;

			isInItinMode = mViewPager != null && mViewPager.getCurrentItem() == PAGER_POS_ITIN;
			if (isInItinMode && mItinListFragment != null && mItinListFragment.getItinCardCount() > 0) {
				addNewItinButtonEnabled = true;
			}
			loginBtnEnabled = isInItinMode && !User.isLoggedIn(this);
			logoutBtnEnabled = isInItinMode && User.isLoggedIn(this);

			MenuItem addNewItinBtn = menu.findItem(R.id.add_itinerary);
			if (addNewItinBtn != null) {
				addNewItinBtn.setVisible(addNewItinButtonEnabled);
				addNewItinBtn.setEnabled(addNewItinButtonEnabled);
			}
			//We have a submenu here, but if we arent showing login we may as well just go to the guest add screen and not show the submenu
			Menu addNewItinMenu = addNewItinBtn.getSubMenu();
			if (addNewItinMenu != null) {
				MenuItem loginBtn = addNewItinMenu.findItem(R.id.add_itinerary_login);
				if (loginBtn != null) {
					loginBtn.setVisible(loginBtnEnabled);
					loginBtn.setEnabled(loginBtnEnabled);
				}
				MenuItem addGuestBtn = addNewItinMenu.findItem(R.id.add_itinerary_guest);
				if (addGuestBtn != null) {
					addGuestBtn.setVisible(loginBtnEnabled);
					addGuestBtn.setEnabled(loginBtnEnabled);
				}
			}
			MenuItem logOutBtn = menu.findItem(R.id.ab_log_out);
			if (logOutBtn != null) {
				logOutBtn.setVisible(logoutBtnEnabled);
				logOutBtn.setEnabled(logoutBtnEnabled);
			}
		}

		DebugMenu.onPrepareOptionsMenu(this, menu);

		if (!AndroidUtils.isRelease(this)) {
			mHockeyPuck.onPrepareOptionsMenu(menu);
		}

		return retVal;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home: {
			gotoWaterfall();
			return true;
		}
		case R.id.add_itinerary: {
			if (User.isLoggedIn(this)) {
				if (Ui.isAdded(mItinListFragment)) {
					mItinListFragment.startAddGuestItinActivity();
				}
				return true;
			}
			return false;
		}
		case R.id.add_itinerary_login: {
			if (Ui.isAdded(mItinListFragment)) {
				mItinListFragment.startLoginActivity();
			}
			return true;
		}
		case R.id.add_itinerary_guest: {
			if (Ui.isAdded(mItinListFragment)) {
				mItinListFragment.startAddGuestItinActivity();
			}
			return true;
		}
		case R.id.ab_log_out: {
			if (Ui.isAdded(mItinListFragment)) {
				mItinListFragment.accountLogoutClicked();
			}
			return true;
		}
		case R.id.settings: {
			Intent intent = new Intent(this, ExpediaBookingPreferenceActivity.class);
			startActivityForResult(intent, REQUEST_SETTINGS);
			if (Ui.isAdded(mLaunchFragment)) {
				mLaunchFragment.cleanUp();
			}
			return true;
		}
		case R.id.about: {
			Intent aboutIntent = new Intent(this, AboutActivity.class);
			startActivity(aboutIntent);
			return true;
		}
		}

		if (DebugMenu.onOptionsItemSelected(this, item)) {
			return true;
		}

		if (!AndroidUtils.isRelease(this) && mHockeyPuck.onOptionsItemSelected(item)) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * Returns true if the user has an in progress or upcoming trip, as of the current time.
	 * @return
	 */
	private boolean haveTimelyItinItem() {
		ItineraryManager manager = ItineraryManager.getInstance();
		List<DateTime> startTimes = manager.getStartTimes();
		List<DateTime> endTimes = manager.getEndTimes();
		if (startTimes != null && endTimes != null && startTimes.size() == endTimes.size()) {
			DateTime now = DateTime.now();
			DateTime oneWeekFromNow = now.plusWeeks(1);
			for (int i = 0; i < startTimes.size(); i++) {
				DateTime start = startTimes.get(i);
				DateTime end = endTimes.get(i);
				if (now.isBefore(end) && oneWeekFromNow.isAfter(start)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Parses ARG_JUMP_TO_NOTIFICATION out of the intent into a Notification object,
	 * sets mJumpToItinId.
	 * This function expects to be called only when this activity is started via
	 * the given intent (onCreate or onNewIntent) and has side effects that
	 * rely on that assumption:
	 * 1. Tracks this incoming intent in Omniture.
	 * 2. Updates the Notifications table that this notification is dismissed.
	 *
	 * *** This is duplicated in ItineraryActivity ***
	 *
	 * @param intent
	 */
	private void handleArgJumpToNotification(Intent intent) {
		String jsonNotification = intent.getStringExtra(ARG_JUMP_TO_NOTIFICATION);
		Notification notification = Notification.getInstanceFromJsonString(jsonNotification);

		if (!Notification.hasExisting(notification)) {
			return;
		}

		mJumpToItinId = notification.getItinId();
		OmnitureTracking.trackNotificationClick(this, notification);

		// There's no need to dismiss with the notification manager, since it was set to
		// auto dismiss when clicked.
		Notification.dismissExisting(notification);
	}

	private synchronized void gotoWaterfall() {
		if (mPagerPosition != PAGER_POS_WATERFALL) {
			ActionBar actionBar = getActionBar();

			mPagerPosition = PAGER_POS_WATERFALL;
			mViewPager.setCurrentItem(PAGER_POS_WATERFALL);
			actionBar.setSelectedNavigationItem(mPagerPosition);

			if (mItinListFragment != null && mItinListFragment.isInDetailMode()) {
				mItinListFragment.hideDetails();
			}

			if (mLaunchFragment != null) {
				mLaunchFragment.startMarquee();
			}

			if (mHasMenu) {
				supportInvalidateOptionsMenu();
			}
		}
		OmnitureTracking.trackPageLoadLaunchScreen(this);
	}

	private synchronized void gotoItineraries() {
		if (mPagerPosition != PAGER_POS_ITIN) {
			ActionBar actionBar = getActionBar();

			if (mItinListFragment != null) {
				mItinListFragment.resetTrackingState();
				mItinListFragment.enableLoadItins();
			}

			mPagerPosition = PAGER_POS_ITIN;
			mViewPager.setCurrentItem(PAGER_POS_ITIN);
			actionBar.setSelectedNavigationItem(mPagerPosition);

			if (mHasMenu) {
				supportInvalidateOptionsMenu();
			}
		}

		if (mJumpToItinId != null && mItinListFragment != null) {
			mItinListFragment.showItinCard(mJumpToItinId, false);
			mJumpToItinId = null;
		}
	}

	private void enableEmbeddedTabs(Object actionBar) {
		try {
			Method setHasEmbeddedTabsMethod = actionBar.getClass().getDeclaredMethod("setHasEmbeddedTabs",
					boolean.class);
			setHasEmbeddedTabsMethod.setAccessible(true);
			setHasEmbeddedTabsMethod.invoke(actionBar, true);
		}
		catch (Exception e) {
			Log.e("Error embedding ActionBar tabs.", e);
		}
	}

	public class PagerAdapter extends FragmentPagerAdapter {

		public PagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			Fragment frag;
			switch (position) {
			case PAGER_POS_ITIN:
				frag = ItinItemListFragment.newInstance(mJumpToItinId);
				break;
			case PAGER_POS_WATERFALL:
				if (ExpediaBookingApp.IS_TRAVELOCITY) {
					frag = TravelocityLauncherFragment.newInstance();
				}
				else {
					frag = LaunchFragment.newInstance();
				}
				break;
			default:
				throw new RuntimeException("Position out of bounds position=" + position);
			}

			return frag;
		}

		@Override
		public int getCount() {
			return 2;
		}
	}

	private TabListener mShopTabListener = new TabListener() {
		@Override
		public void onTabSelected(Tab tab, android.app.FragmentTransaction ft) {
			gotoWaterfall();
		}

		@Override
		public void onTabUnselected(Tab tab, android.app.FragmentTransaction ft) {
			// ignore
		}

		@Override
		public void onTabReselected(Tab tab, android.app.FragmentTransaction ft) {
			// ignore
		}
	};

	private TabListener mItineraryTabListener = new TabListener() {
		@Override
		public void onTabSelected(Tab tab, android.app.FragmentTransaction ft) {
			gotoItineraries();
		}

		@Override
		public void onTabUnselected(Tab tab, android.app.FragmentTransaction ft) {
			// ignore
		}

		@Override
		public void onTabReselected(Tab tab, android.app.FragmentTransaction ft) {
			// ignore
		}
	};

	@Override
	public void onListModeChanged(boolean isInDetailMode, boolean animate) {
		mViewPager.setPageSwipingEnabled(!isInDetailMode);
		if (isInDetailMode) {
			if (getActionBar().isShowing()) {
				getActionBar().hide();
			}
		}
		else {
			// The collapse animation takes 400ms, and the actionbar.show
			// animation happens in 200ms, so make it use the last 200ms
			// of the animation (and check to make sure there wasn't another
			// mode change in between)
			mViewPager.postDelayed(new Runnable() {
				public void run() {
					if (!mItinListFragment.isInDetailMode() && !getActionBar().isShowing()) {
						getActionBar().show();
					}
				}
			}, 200); // 400ms - 200ms
		}
	}

	@Override
	public void onLaunchFragmentAttached(LaunchFragment frag) {
		mLaunchFragment = frag;
	}

	@Override
	public void onItinItemListFragmentAttached(ItinItemListFragment frag) {
		mItinListFragment = frag;
		if (mPagerPosition == PAGER_POS_ITIN) {
			mItinListFragment.enableLoadItins();
		}

		if (mJumpToItinId != null) {
			mItinListFragment.showItinCard(mJumpToItinId, false);
			mJumpToItinId = null;
		}
	}

	@Override
	public void onItinCardClicked(ItinCardData data) {
		// Do nothing (let fragment handle it)
	}

	//////////////////////////////////////////////////////////////////////////
	// DoLogoutListener

	@Override
	public void doLogout() {
		if (Ui.isAdded(mItinListFragment)) {
			mItinListFragment.doLogout();
		}
	}
}
