package com.expedia.bookings.activity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.joda.time.DateTime;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.FragmentTransaction;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.fragment.UserReviewsFragment;
import com.expedia.bookings.fragment.UserReviewsFragment.UserReviewsFragmentListener;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.HotelUtils;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.utils.UserReviewsUtils;
import com.expedia.bookings.widget.UserReviewsFragmentPagerAdapter;
import com.mobiata.android.Log;

public class UserReviewsListActivity extends FragmentActivity
	implements UserReviewsFragmentListener, TabListener, OnPageChangeListener {

	private static final long RESUME_TIMEOUT = 20 * DateUtils.MINUTE_IN_MILLIS;
	private DateTime mLastResumeTime;

	// Instance variable names
	private static final String INSTANCE_VIEWED_REVIEWS = "INSTANCE_VIEWED_REVIEWS";

	// Fragments and Views
	private ViewPager mViewPager;
	private UserReviewsFragmentPagerAdapter mPagerAdapter;

	private Set<String> mViewedReviews;

	// To make up for a lack of FLAG_ACTIVITY_CLEAR_TASK in older Android versions
	private ActivityKillReceiver mKillReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mKillReceiver = new ActivityKillReceiver(this);
		mKillReceiver.onCreate();

		if (checkFinishConditionsAndFinish()) {
			return;
		}

		if (savedInstanceState != null) {
			mViewedReviews = new HashSet<>(savedInstanceState.getStringArrayList(INSTANCE_VIEWED_REVIEWS));
		}
		else {
			mViewedReviews = new HashSet<>();
		}

		setContentView(R.layout.activity_user_reviews);
		getWindow().setBackgroundDrawable(null);

		initializePager(savedInstanceState);
		initializeActionBar();
		populateReviewsStats();
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (checkFinishConditionsAndFinish()) {
			return;
		}
	}

	private boolean checkFinishConditionsAndFinish() {
		// #13365: If the Db expired, finish out of this activity
		if (Db.getHotelSearch().getSelectedProperty() == null) {
			Log.i("Detected expired DB, finishing activity.");
			finish();
			return true;
		}

		// #14135, set a 1 hour timeout on this screen
		if (JodaUtils.isExpired(mLastResumeTime, RESUME_TIMEOUT)) {
			finish();
			return true;
		}
		mLastResumeTime = DateTime.now();

		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_hotel_details, menu);

		final MenuItem select = menu.findItem(R.id.menu_select_hotel);
		HotelUtils.setupActionBarCheckmark(this, select, Db.getHotelSearch().getSelectedProperty().isAvailable());

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home: {
			// app icon in action bar clicked; go back
			onBackPressed();
			return true;
		}
		case R.id.menu_select_hotel: {
			startActivity(HotelRoomsAndRatesActivity.createIntent(this));
		}
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mKillReceiver != null) {
			mKillReceiver.onDestroy();
		}

		if (isFinishing()) {
			UserReviewsUtils.getInstance().clearCache();

			// Track # of reviews seen
			if (mViewedReviews != null) {
				int numReviewsSeen = mViewedReviews.size();
				Log.d("Tracking # of reviews seen: " + numReviewsSeen);
				String referrerId = "App.Hotels.Reviews." + numReviewsSeen + "ReviewsViewed";
				OmnitureTracking.trackSimpleEvent(null, null, referrerId);
			}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		ArrayList<String> viewedReviews = new ArrayList<String>(mViewedReviews);
		outState.putStringArrayList(INSTANCE_VIEWED_REVIEWS, viewedReviews);

		mPagerAdapter.onSaveInstanceState(getSupportFragmentManager(), outState);
	}

	private void populateReviewsStats() {
		View titleView = getActionBar().getCustomView();
		if (titleView == null) {
			return;
		}

		TextView titleTextView = (TextView) titleView.findViewById(R.id.title);
		RatingBar ratingBar = (RatingBar) titleView.findViewById(R.id.user_rating);

		Property property = Db.getHotelSearch().getSelectedProperty();

		if (titleTextView != null) {
			Resources res = getResources();

			int count = property.getTotalReviews();
			String title = res.getQuantityString(R.plurals.number_of_reviews, count, count);
			titleTextView.setText(title);
		}

		if (ratingBar != null) {
			ratingBar.setRating((float) property.getAverageExpediaRating());
			ratingBar.setVisibility(View.VISIBLE);
		}
	}

	private void initializeActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setCustomView(Ui.inflate(this, R.layout.actionbar_reviews, null));

		Tab recentTab = actionBar.newTab().setText(R.string.user_review_sort_button_recent);
		recentTab.setTabListener(this);
		recentTab.setTag(0);
		actionBar.addTab(recentTab);

		Tab favorableTab = actionBar.newTab().setText(R.string.user_review_sort_button_favorable);
		favorableTab.setTabListener(this);
		favorableTab.setTag(1);
		actionBar.addTab(favorableTab);

		Tab criticalTab = actionBar.newTab().setText(R.string.user_review_sort_button_critical);
		criticalTab.setTabListener(this);
		criticalTab.setTag(2);
		actionBar.addTab(criticalTab);
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// UserReviewsFragmentListener

	@Override
	public void onUserReviewsFragmentReady(UserReviewsFragment frag) {
		frag.bind();
	}

	@Override
	public void addMoreReviewsSeen(Set<String> reviews) {
		mViewedReviews.addAll(reviews);
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// FragmentViewPager

	private void initializePager(Bundle savedInstanceState) {
		mPagerAdapter = new UserReviewsFragmentPagerAdapter(getSupportFragmentManager(), savedInstanceState);

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setOffscreenPageLimit(2);
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setOnPageChangeListener(this);
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ActionBar.TabListener

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		int index = (Integer) tab.getTag();

		if (mViewPager.getCurrentItem() != index) {
			mViewPager.setCurrentItem(index);
		}

		String referrerId = null;
		if (index == 0) {
			referrerId = "App.Hotels.Reviews.Sort.Recent";
		}
		else if (index == 1) {
			referrerId = "App.Hotels.Reviews.Sort.Favorable";
		}
		else if (index == 2) {
			referrerId = "App.Hotels.Reviews.Sort.Critical";
		}
		Log.d("Tracking \"App.Hotels.Reviews\" pageLoad");
		OmnitureTracking.trackSimpleEvent("App.Hotels.Reviews", null, referrerId);
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// nothing to do here
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// nothing to do here
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// OnPageChangeListener

	@Override
	public void onPageScrollStateChanged(int state) {
		// nothing to do here
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		// nothing to do here
	}

	@Override
	public void onPageSelected(int position) {
		ActionBar actionBar = getActionBar();
		Tab tab = actionBar.getTabAt(position);
		if (tab != actionBar.getSelectedTab()) {
			tab.select();
		}
	}
}
