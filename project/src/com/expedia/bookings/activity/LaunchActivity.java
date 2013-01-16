package com.expedia.bookings.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.animation.CollapseAnimation;
import com.expedia.bookings.animation.ExpandAnimation;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.fragment.ItinItemListFragment;
import com.expedia.bookings.fragment.ItineraryGuestAddDialogFragment;
import com.expedia.bookings.fragment.LaunchFragment;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.ActionBarNavUtils;
import com.expedia.bookings.utils.DebugMenu;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.hockey.HockeyPuck;
import com.mobiata.android.util.AndroidUtils;

public class LaunchActivity extends SherlockFragmentActivity {

	private static final int REQUEST_SETTINGS = 1;

	private ViewGroup mHeader;

	private LaunchFragment mLaunchFragment;
	private ItinItemListFragment mItinListFragment;

	private PagerAdapter mPagerAdapter;
	private ViewPager mViewPager;

	private HockeyPuck mHockeyPuck;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().setFormat(android.graphics.PixelFormat.RGBA_8888);

		setContentView(R.layout.activity_launch);
		getWindow().setBackgroundDrawable(null);

		mHeader = Ui.findView(this, R.id.header);

		mLaunchFragment = Ui.findSupportFragment(this, LaunchFragment.TAG);
		if (mLaunchFragment == null) {
			mLaunchFragment = LaunchFragment.newInstance();
		}
		mItinListFragment = Ui.findSupportFragment(this, ItinItemListFragment.TAG);
		if (mItinListFragment == null) {
			mItinListFragment = ItinItemListFragment.newInstance();
		}

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
				if (position == 0) {
					gotoWaterfall();
				}
				else if (position == 1) {
					gotoItineraries();
				}
			}
		});

		// HockeyApp init
		mHockeyPuck = new HockeyPuck(this, Codes.HOCKEY_APP_ID, !AndroidUtils.isRelease(this));
		mHockeyPuck.onCreate(savedInstanceState);

		Ui.findView(this, R.id.hotels_button).setOnClickListener(mHeaderItemOnClickListener);
		Ui.findView(this, R.id.flights_button).setOnClickListener(mHeaderItemOnClickListener);

		FontCache.setTypeface((TextView) Ui.findView(this, R.id.hotels_label_text_view), FontCache.Font.ROBOTO_LIGHT);
		FontCache.setTypeface((TextView) Ui.findView(this, R.id.hotels_prompt_text_view), FontCache.Font.ROBOTO_LIGHT);
		FontCache.setTypeface((TextView) Ui.findView(this, R.id.flights_label_text_view), FontCache.Font.ROBOTO_LIGHT);
		FontCache.setTypeface((TextView) Ui.findView(this, R.id.flights_prompt_text_view), FontCache.Font.ROBOTO_LIGHT);

		// H833 If the prompt is too wide on this POS/in this language, then hide it
		// (and also hide its sibling to maintain a consistent look)
		// Wrap this in a Runnable so as to happen after the TextViews have been measured
		Ui.findView(this, R.id.hotels_prompt_text_view).post(new Runnable() {
			@Override
			public void run() {
				View hotelPrompt = Ui.findView(LaunchActivity.this, R.id.hotels_prompt_text_view);
				View hotelIcon = Ui.findView(LaunchActivity.this, R.id.big_hotel_icon);
				View flightsPrompt = Ui.findView(LaunchActivity.this, R.id.flights_prompt_text_view);
				View flightsIcon = Ui.findView(LaunchActivity.this, R.id.big_flights_icon);
				if (hotelPrompt.getLeft() < hotelIcon.getRight() || flightsPrompt.getLeft() < flightsIcon.getRight()) {
					hotelPrompt.setVisibility(View.INVISIBLE);
					flightsPrompt.setVisibility(View.INVISIBLE);
				}
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();

		//HockeyApp crash
		mHockeyPuck.onResume();
	}

	@Override
	public void onStart() {
		super.onStart();
		OmnitureTracking.trackPageLoadLaunchScreen(this);
	}

	@Override
	public void onBackPressed() {
		if (mViewPager.getCurrentItem() == 1) {
			if (!mItinListFragment.inListMode()) {
				mItinListFragment.showList();
				return;
			}

			mViewPager.setCurrentItem(0);
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

		ActionBarNavUtils.setupActionLayoutButton(this, menu, R.id.itineraries);

		DebugMenu.onCreateOptionsMenu(this, menu);
		mHockeyPuck.onCreateOptionsMenu(menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (menu != null) {
			boolean itinButtonEnabled = true;
			boolean addNewItinButtonEnabled;
			if (mViewPager != null && mViewPager.getCurrentItem() == 1) {
				itinButtonEnabled = false;
			}
			else {
				itinButtonEnabled = true;
			}
			addNewItinButtonEnabled = !itinButtonEnabled;
			MenuItem itinBtn = menu.findItem(R.id.itineraries);
			if (itinBtn != null) {
				itinBtn.setVisible(itinButtonEnabled);
				itinBtn.setEnabled(itinButtonEnabled);
			}
			MenuItem addNewItinBtn = menu.findItem(R.id.add_itinerary);
			if (addNewItinBtn != null) {
				addNewItinBtn.setVisible(addNewItinButtonEnabled);
				addNewItinBtn.setEnabled(addNewItinButtonEnabled);
			}

		}

		DebugMenu.onPrepareOptionsMenu(this, menu);

		mHockeyPuck.onPrepareOptionsMenu(menu);

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			gotoWaterfall();
			return true;
		case R.id.itineraries:
			gotoItineraries();
			return true;
		case R.id.add_itinerary:
			showAddItinDialog();
			return true;
		case R.id.settings:
			Intent intent = new Intent(this, ExpediaBookingPreferenceActivity.class);
			startActivityForResult(intent, REQUEST_SETTINGS);
			mLaunchFragment.cleanUp();
			return true;
		case R.id.about:
			Intent aboutIntent = new Intent(this, AboutActivity.class);
			startActivity(aboutIntent);
			return true;
		}

		if (DebugMenu.onOptionsItemSelected(this, item) || mHockeyPuck.onOptionsItemSelected(item)) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private final View.OnClickListener mHeaderItemOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.hotels_button:
				NavUtils.goToHotels(LaunchActivity.this);

				OmnitureTracking.trackLinkLaunchScreenToHotels(LaunchActivity.this);
				break;
			case R.id.flights_button:
				NavUtils.goToFlights(LaunchActivity.this);

				OmnitureTracking.trackLinkLaunchScreenToFlights(LaunchActivity.this);
				break;
			}

			if (mLaunchFragment != null) {
				mLaunchFragment.cleanUp();
			}
		}
	};

	private void gotoWaterfall() {
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		getSupportActionBar().setHomeButtonEnabled(false);

		mViewPager.setCurrentItem(0);
		mItinListFragment.showList();

		supportInvalidateOptionsMenu();
	}

	private void gotoItineraries() {
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		mItinListFragment.enableLoadItins();

		mViewPager.setCurrentItem(1);

		supportInvalidateOptionsMenu();
	}

	private void showAddItinDialog() {
		ItineraryGuestAddDialogFragment addNewItinFrag = (ItineraryGuestAddDialogFragment) getSupportFragmentManager()
				.findFragmentByTag(ItineraryGuestAddDialogFragment.TAG);
		if (addNewItinFrag == null) {
			addNewItinFrag = ItineraryGuestAddDialogFragment.newInstance();
		}
		if (!addNewItinFrag.isVisible()) {
			addNewItinFrag.show(getSupportFragmentManager(), ItineraryGuestAddDialogFragment.TAG);
		}
	}

	public void hideHeader() {
		if (mHeader.getVisibility() == View.GONE) {
			return;
		}

		if (mHeader.getAnimation() != null) {
			mHeader.getAnimation().cancel();
		}

		mHeader.startAnimation(new CollapseAnimation(mHeader));
	}

	public void showHeader() {
		if (mHeader.getVisibility() == View.VISIBLE) {
			return;
		}

		if (mHeader.getAnimation() != null) {
			mHeader.getAnimation().cancel();
		}

		mHeader.startAnimation(new ExpandAnimation(mHeader));
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
}
