package com.expedia.bookings.activity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
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
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.User;
import com.expedia.bookings.dialog.GooglePlayServicesDialog;
import com.expedia.bookings.fragment.ItinItemListFragment;
import com.expedia.bookings.fragment.LaunchFragment;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.DebugMenu;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;
import com.mobiata.android.hockey.HockeyPuck;
import com.mobiata.android.util.AndroidUtils;

public class LaunchActivity extends SherlockFragmentActivity {

	private static final int REQUEST_SETTINGS = 1;

	private static final int PAGER_POS_WATERFALL = 0;
	private static final int PAGER_POS_ITIN = 1;

	private LaunchFragment mLaunchFragment;
	private ItinItemListFragment mItinListFragment;

	private PagerAdapter mPagerAdapter;
	private ViewPager mViewPager;
	private int mPagerPosition = PAGER_POS_WATERFALL;
	private boolean mHasMenu = false;

	private HockeyPuck mHockeyPuck;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().setFormat(android.graphics.PixelFormat.RGBA_8888);

		setContentView(R.layout.activity_launch);
		getWindow().setBackgroundDrawable(null);

		// Get/create fragments
		mLaunchFragment = Ui.findSupportFragment(this, LaunchFragment.TAG);
		if (mLaunchFragment == null) {
			mLaunchFragment = LaunchFragment.newInstance();
		}
		mItinListFragment = Ui.findSupportFragment(this, ItinItemListFragment.TAG);
		if (mItinListFragment == null) {
			mItinListFragment = ItinItemListFragment.newInstance();
		}

		// View Pager
		List<Fragment> frags = new ArrayList<Fragment>();
		frags.add(mLaunchFragment);
		frags.add(mItinListFragment);
		mPagerAdapter = new PagerAdapter(getSupportFragmentManager(), frags);

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
		Tab shopTab = getSupportActionBar().newTab().setText("Shop").setTabListener(mShopTabListener);
		Tab itineraryTab = getSupportActionBar().newTab().setText("Itinerary").setTabListener(mItineraryTabListener);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.addTab(shopTab);
		actionBar.addTab(itineraryTab);

		enableEmbeddedTabs(actionBar);

		// HockeyApp init
		mHockeyPuck = new HockeyPuck(this, Codes.HOCKEY_APP_ID, !AndroidUtils.isRelease(this));
		mHockeyPuck.onCreate(savedInstanceState);
	}

	@Override
	public void onStart() {
		super.onStart();
		OmnitureTracking.trackPageLoadLaunchScreen(this);
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
			if (!mItinListFragment.inListMode()) {
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_SETTINGS && resultCode != RESULT_CANCELED) {
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
				if (mItinListFragment != null) {
					mItinListFragment.startAddGuestItinActivity();
				}
				return true;
			}
			return false;
		}
		case R.id.add_itinerary_login: {
			if (mItinListFragment != null) {
				mItinListFragment.startLoginActivity();
			}
			return true;
		}
		case R.id.add_itinerary_guest: {
			if (mItinListFragment != null) {
				mItinListFragment.startAddGuestItinActivity();
			}
			return true;
		}
		case R.id.ab_log_out: {
			if (mItinListFragment != null) {
				mItinListFragment.accountLogoutClicked();
			}
			return true;
		}
		case R.id.settings: {
			Intent intent = new Intent(this, ExpediaBookingPreferenceActivity.class);
			startActivityForResult(intent, REQUEST_SETTINGS);
			mLaunchFragment.cleanUp();
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

	private void gotoWaterfall() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(false);
		actionBar.setHomeButtonEnabled(false);

		mPagerPosition = PAGER_POS_WATERFALL;
		mViewPager.setCurrentItem(PAGER_POS_WATERFALL);

		Tab tab = actionBar.getTabAt(PAGER_POS_WATERFALL);
		if (tab != null) {
			actionBar.selectTab(tab);
		}

		if (mItinListFragment == null) {
			mItinListFragment.setListMode();
		}

		if (mHasMenu) {
			supportInvalidateOptionsMenu();
		}
	}

	private void gotoItineraries() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);

		if (mItinListFragment != null) {
			mItinListFragment.enableLoadItins();
		}

		mPagerPosition = PAGER_POS_ITIN;
		mViewPager.setCurrentItem(PAGER_POS_ITIN);

		Tab tab = actionBar.getTabAt(PAGER_POS_ITIN);
		if (tab != null) {
			actionBar.selectTab(tab);
		}

		if (mHasMenu) {
			supportInvalidateOptionsMenu();
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
		private List<Fragment> mFragments;

		public PagerAdapter(FragmentManager fm, List<Fragment> fragments) {
			super(fm);
			mFragments = fragments;
		}

		@Override
		public Fragment getItem(int position) {
			return mFragments.get(position);
		}

		@Override
		public int getCount() {
			return mFragments.size();
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
}
