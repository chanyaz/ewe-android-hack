package com.expedia.bookings.activity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.internal.app.ActionBarImpl;
import com.actionbarsherlock.internal.app.ActionBarWrapper;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.DateTime;
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
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.DebugMenu;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.DisableableViewPager;
import com.expedia.bookings.widget.ItinListView;
import com.expedia.bookings.widget.ItinListView.OnListModeChangedListener;
import com.mobiata.android.Log;
import com.mobiata.android.hockey.HockeyPuck;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.flightlib.utils.DateTimeUtils;

public class LaunchActivity extends SherlockFragmentActivity implements OnListModeChangedListener,
		ItinItemListFragmentListener, LaunchFragmentListener, DoLogoutListener {

	public static final String ARG_FORCE_SHOW_WATERFALL = "ARG_FORCE_SHOW_WATERFALL";

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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().setFormat(android.graphics.PixelFormat.RGBA_8888);

		setContentView(R.layout.activity_launch);
		getWindow().setBackgroundDrawable(null);

		// View Pager
		mPagerAdapter = new PagerAdapter(getSupportFragmentManager());
		mViewPager = Ui.findView(this, R.id.viewpager);
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageScrollStateChanged(int state) {
			}

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
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
		Tab shopTab = getSupportActionBar().newTab().setText(R.string.shop).setTabListener(mShopTabListener);
		Tab itineraryTab = getSupportActionBar().newTab().setText(R.string.trips).setTabListener(mItineraryTabListener);

		ActionBar actionBar = getSupportActionBar();
		enableEmbeddedTabs(actionBar);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.addTab(shopTab, PAGER_POS_WATERFALL);
		actionBar.addTab(itineraryTab, PAGER_POS_ITIN);

		// Switch to itin mode if we have an inprogress or upcoming trip (and we aren't forcing reverse waterfall)
		boolean allowSkipToItin = !getIntent().getBooleanExtra(ARG_FORCE_SHOW_WATERFALL, false);
		if (allowSkipToItin) {
			List<DateTime> startTimes = ItineraryManager.getInstance().getStartTimes();
			List<DateTime> endTimes = ItineraryManager.getInstance().getEndTimes();
			if (startTimes != null && endTimes != null && startTimes.size() == endTimes.size()) {
				boolean startInItin = false;
				Calendar now = Calendar.getInstance();
				Calendar oneWeek = Calendar.getInstance();
				oneWeek.add(Calendar.DATE, 7);
				for (int i = 0; i < startTimes.size(); i++) {
					DateTime start = startTimes.get(i);
					DateTime end = endTimes.get(i);
					if (DateTimeUtils.getTimeInCurrentTimeZone(now).getTime() < end.getMillisFromEpoch()
							&& DateTimeUtils.getTimeInCurrentTimeZone(oneWeek).getTime() > start.getMillisFromEpoch()) {
						startInItin = true;
						break;
					}
				}
				if (startInItin) {
					gotoItineraries();
				}
			}
		}

		// HockeyApp init
		mHockeyPuck = new HockeyPuck(this, Codes.HOCKEY_APP_ID, !AndroidUtils.isRelease(this));
		mHockeyPuck.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();

		//HockeyApp crash
		mHockeyPuck.onResume();

		GooglePlayServicesDialog gpsd = new GooglePlayServicesDialog(this);
		gpsd.startChecking();

		supportInvalidateOptionsMenu();

		OmnitureTracking.onResume(this);
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
			if (mItinListFragment != null && !mItinListFragment.inListMode()) {
				mItinListFragment.setListMode();
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
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (mLaunchFragment != null && requestCode == REQUEST_SETTINGS
				&& resultCode == ExpediaBookingPreferenceActivity.RESULT_CHANGED_PREFS) {
			mLaunchFragment.reset();

			Db.clearHotelSearch();
			Db.resetSearchParams();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_launch, menu);

		DebugMenu.onCreateOptionsMenu(this, menu);
		mHockeyPuck.onCreateOptionsMenu(menu);
		mHasMenu = super.onCreateOptionsMenu(menu);
		return mHasMenu;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean retVal = super.onPrepareOptionsMenu(menu);
		if (menu != null) {
			boolean itinButtonEnabled = true;
			boolean addNewItinButtonEnabled = false;
			boolean loginBtnEnabled = false;
			boolean logoutBtnEnabled = false;

			if (mViewPager != null && mViewPager.getCurrentItem() == PAGER_POS_ITIN) {
				itinButtonEnabled = false;
			}
			else {
				itinButtonEnabled = true;
			}
			addNewItinButtonEnabled = !itinButtonEnabled;
			loginBtnEnabled = !itinButtonEnabled && !User.isLoggedIn(this);
			logoutBtnEnabled = !itinButtonEnabled && User.isLoggedIn(this);

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

		mHockeyPuck.onPrepareOptionsMenu(menu);

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

		if (DebugMenu.onOptionsItemSelected(this, item) || mHockeyPuck.onOptionsItemSelected(item)) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private synchronized void gotoWaterfall() {
		if (mPagerPosition != PAGER_POS_WATERFALL) {
			ActionBar actionBar = getSupportActionBar();
			actionBar.setDisplayHomeAsUpEnabled(false);
			actionBar.setHomeButtonEnabled(false);

			mPagerPosition = PAGER_POS_WATERFALL;
			mViewPager.setCurrentItem(PAGER_POS_WATERFALL);
			actionBar.setSelectedNavigationItem(mPagerPosition);

			if (mItinListFragment != null) {
				mItinListFragment.setListMode();
			}

			if (mLaunchFragment != null) {
				mLaunchFragment.startMarquee();
			}

			if (mHasMenu) {
				supportInvalidateOptionsMenu();
			}

			OmnitureTracking.trackPageLoadLaunchScreen(this);
		}
	}

	private synchronized void gotoItineraries() {
		if (mPagerPosition != PAGER_POS_ITIN) {
			ActionBar actionBar = getSupportActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setHomeButtonEnabled(true);

			if (mItinListFragment != null) {
				mItinListFragment.enableLoadItins();
			}

			mPagerPosition = PAGER_POS_ITIN;
			mViewPager.setCurrentItem(PAGER_POS_ITIN);
			actionBar.setSelectedNavigationItem(mPagerPosition);

			if (mHasMenu) {
				supportInvalidateOptionsMenu();
			}
		}
	}

	private void enableEmbeddedTabs(Object actionBar) {
		try {
			if (!(actionBar instanceof ActionBarImpl) && actionBar instanceof ActionBarWrapper) {
				Field actionBarField = actionBar.getClass().getDeclaredField("mActionBar");
				actionBarField.setAccessible(true);
				actionBar = actionBarField.get(actionBar);
			}

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
				frag = ItinItemListFragment.newInstance();
				break;
			case PAGER_POS_WATERFALL:
				frag = LaunchFragment.newInstance();
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
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			gotoWaterfall();
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
		}
	};

	private TabListener mItineraryTabListener = new TabListener() {
		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			gotoItineraries();
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
		}
	};

	@Override
	public void onListModeChanged(int mode) {
		if (mode == ItinListView.MODE_LIST) {
			mViewPager.setPageSwipingEnabled(true);
		}
		else if (mode == ItinListView.MODE_DETAIL) {
			mViewPager.setPageSwipingEnabled(false);
		}
		else {
			mViewPager.setPageSwipingEnabled(true);
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
