package com.expedia.bookings.activity;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Pair;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.internal.ResourcesCompat;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.data.BackgroundImageCache;
import com.expedia.bookings.data.BackgroundImageResponse;
import com.expedia.bookings.data.Date;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightFilter;
import com.expedia.bookings.data.FlightFilter.Sort;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightSearch;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.FlightSearchResponse;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.FlightTripLeg;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.ServerError.ApiMethod;
import com.expedia.bookings.data.ServerError.ErrorCode;
import com.expedia.bookings.fragment.BlurredBackgroundFragment;
import com.expedia.bookings.fragment.FlightDetailsFragment;
import com.expedia.bookings.fragment.FlightDetailsFragment.FlightDetailsFragmentListener;
import com.expedia.bookings.fragment.FlightFilterDialogFragment;
import com.expedia.bookings.fragment.FlightListFragment;
import com.expedia.bookings.fragment.FlightListFragment.FlightListFragmentListener;
import com.expedia.bookings.fragment.NoFlightsFragment;
import com.expedia.bookings.fragment.NoFlightsFragment.NoFlightsFragmentListener;
import com.expedia.bookings.fragment.RetryErrorDialogFragment;
import com.expedia.bookings.fragment.RetryErrorDialogFragment.RetryErrorDialogFragmentListener;
import com.expedia.bookings.fragment.StatusFragment;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.ViewUtils;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ValueAnimator;

public class FlightSearchResultsActivity extends SherlockFragmentActivity implements FlightListFragmentListener,
		OnBackStackChangedListener, RetryErrorDialogFragmentListener, NoFlightsFragmentListener,
		FlightDetailsFragmentListener {

	public static final String EXTRA_DESELECT_LEG_ID = "EXTRA_DESELECT_LEG_ID";

	private static final String DOWNLOAD_KEY = "com.expedia.bookings.flights";
	private static final String BACKGROUND_IMAGE_INFO_DOWNLOAD_KEY = "BACKGROUND_IMAGE_INFO_DOWNLOAD_KEY";
	private static final String BACKGROUND_IMAGE_FILE_DOWNLOAD_KEY = "BACKGROUND_IMAGE_FILE_DOWNLOAD_KEY";

	private static final String INSTANCE_LEG_POSITION = "INSTANCE_LEG_POSITION";

	private static final int REQUEST_CODE_SEARCH_PARAMS = 1;

	private static final String BACKSTACK_LOADING = "BACKSTACK_LOADING";
	private static final String BACKSTACK_NO_FLIGHTS = "BACKSTACK_NO_FLIGHTS";
	private static final String BACKSTACK_FLIGHT_DETAILS_PREFIX = "BACKSTACK_FLIGHT_DETAILS";
	private static final String BACKSTACK_FLIGHT_LIST_PREFIX = "BACKSTACK_FLIGHT_LIST";
	private static final String BACKSTACK_ANIM_PREFIX = "BACKSTACK_ANIM";
	private static final String BACKSTACK_ANIM_FLIGHT_DETAILS_PREFIX = BACKSTACK_ANIM_PREFIX + "_FLIGHT_DETAILS_PREFIX";
	private static final String BACKSTACK_ANIM_FLIGHT_LIST_PREFIX = BACKSTACK_ANIM_PREFIX + "_FLIGHT_LIST_PREFIX";

	private Context mContext;

	private BlurredBackgroundFragment mBgFragment;
	private StatusFragment mStatusFragment;
	private FlightListFragment mListFragment;
	private FlightDetailsFragment mFlightDetailsFragment;
	private NoFlightsFragment mNoFlightsFragment;

	// Current leg being displayed
	private int mLegPosition = 0;

	// This is needed in order to avoid timing issues with fragments.
	// If you want to indicate to the app to start a new search, but
	// you may not have resumed yet, use this variable.
	private boolean mStartSearchOnPostResume;

	// Sets up a leg to be deselected in post resume (for fragment state
	// reasons, this must be done later).
	private int mDeselectLegPos = -1;

	// Action bar views
	private ViewGroup mFlightSummaryContainer;
	private TextView mTitleTextView;
	private TextView mSubtitleTextView;
	private ViewGroup mFlightDetailsActionContainer;
	private View mCancelButton;
	private View mSelectFlightButton;

	private ActivityKillReceiver mKillReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = this;

		mKillReceiver = new ActivityKillReceiver(this);
		mKillReceiver.onCreate();

		// Recover data if it was flushed from memory
		if (savedInstanceState != null && !BackgroundDownloader.getInstance().isDownloading(DOWNLOAD_KEY)
				&& Db.getFlightSearch().getSearchResponse() == null) {
			if (!Db.loadCachedFlightData(this)) {
				NavUtils.onDataMissing(this);
				return;
			}
		}

		if (savedInstanceState != null) {
			mLegPosition = savedInstanceState.getInt(INSTANCE_LEG_POSITION);
		}

		setContentView(R.layout.activity_flight_results);

		// Try to recover any Fragments
		mBgFragment = Ui.findSupportFragment(this, R.id.background_fragment);
		mStatusFragment = Ui.findSupportFragment(this, StatusFragment.TAG);
		mNoFlightsFragment = Ui.findSupportFragment(this, NoFlightsFragment.TAG);
		mListFragment = Ui.findSupportFragment(this, FlightListFragment.TAG);
		mFlightDetailsFragment = Ui.findSupportFragment(this, FlightDetailsFragment.TAG);

		// Configure the custom action bar view
		ActionBar actionBar = this.getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setCustomView(R.layout.action_bar_flight_results);
		actionBar.setTitle(R.string.searching);

		View customView = actionBar.getCustomView();
		mFlightSummaryContainer = Ui.findView(customView, R.id.flight_summary_container);
		mTitleTextView = Ui.findView(customView, R.id.title_text_view);
		mSubtitleTextView = Ui.findView(customView, R.id.subtitle_text_view);
		mFlightDetailsActionContainer = Ui.findView(customView, R.id.flight_details_action_container);
		mCancelButton = Ui.findView(customView, R.id.cancel_button);
		mSelectFlightButton = Ui.findView(customView, R.id.select_button);

		mCancelButton.setOnClickListener(mOnCancelClick);
		mSelectFlightButton.setOnClickListener(mSelectFlightClick);

		ViewUtils.setAllCaps((TextView) Ui.findView(mCancelButton, R.id.cancel_text_view));
		ViewUtils.setAllCaps((TextView) Ui.findView(mSelectFlightButton, R.id.select_text_view));

		// Need to do this, or else the custom view won't take up the entire space available
		customView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		if (savedInstanceState == null) {
			// On first launch, start a search
			mStartSearchOnPostResume = true;

			// #664: We delay the starting of the first search
			// until *after* we've created the options menu.  That way
			// the PlaneWindowView will know if it needs to account
			// for a split action bar taking up space or not.
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		if (intent.hasExtra(EXTRA_DESELECT_LEG_ID)) {
			String legId = intent.getStringExtra(EXTRA_DESELECT_LEG_ID);

			Log.i("Got new intent, deselecting leg id=" + legId);

			FlightTripLeg selectedLegs[] = Db.getFlightSearch().getSelectedLegs();
			for (mDeselectLegPos = 0; mDeselectLegPos < selectedLegs.length; mDeselectLegPos++) {
				if (selectedLegs[mDeselectLegPos].getFlightLeg().getLegId().equals(legId)) {
					break;
				}
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		getSupportFragmentManager().addOnBackStackChangedListener(this);
	}

	@Override
	protected void onPostResume() {
		super.onPostResume();

		if (mStartSearchOnPostResume) {
			mStartSearchOnPostResume = false;
			startSearch();
		}
		else if (mDeselectLegPos != -1) {
			deselectBackToLegPosition(mDeselectLegPos);
			mDeselectLegPos = -1;
		}
		else {
			BackgroundDownloader.getInstance().registerDownloadCallback(DOWNLOAD_KEY, mDownloadCallback);
			BackgroundDownloader.getInstance().registerDownloadCallback(BACKGROUND_IMAGE_INFO_DOWNLOAD_KEY,
					mBackgroundImageInfoDownloadCallback);
			BackgroundDownloader.getInstance().registerDownloadCallback(BACKGROUND_IMAGE_FILE_DOWNLOAD_KEY,
					mBackgroundImageFileDownloadCallback);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(INSTANCE_LEG_POSITION, mLegPosition);
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (!isFinishing()) {
			BackgroundDownloader.getInstance().unregisterDownloadCallback(DOWNLOAD_KEY);
			BackgroundDownloader.getInstance().unregisterDownloadCallback(BACKGROUND_IMAGE_INFO_DOWNLOAD_KEY);
			BackgroundDownloader.getInstance().unregisterDownloadCallback(BACKGROUND_IMAGE_FILE_DOWNLOAD_KEY);
		}
		else {
			BackgroundDownloader.getInstance().cancelDownload(DOWNLOAD_KEY);
			BackgroundDownloader.getInstance().cancelDownload(BACKGROUND_IMAGE_INFO_DOWNLOAD_KEY);
			BackgroundDownloader.getInstance().cancelDownload(BACKGROUND_IMAGE_FILE_DOWNLOAD_KEY);
		}

		getSupportFragmentManager().removeOnBackStackChangedListener(this);

		// End any animations now
		if (mCurrentAnimator != null && mCurrentAnimator.isRunning()) {
			mCurrentAnimator.end();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_CODE_SEARCH_PARAMS && resultCode == RESULT_OK) {
			Log.i("Got new search params from FlightSearchOverlayActivity");

			FlightSearchParams params = JSONUtils.getJSONable(data, FlightSearchOverlayActivity.EXTRA_SEARCH_PARAMS,
					FlightSearchParams.class);
			Db.getFlightSearch().setSearchParams(params);
			mStartSearchOnPostResume = true;
		}
	}

	@Override
	public void onBackPressed() {
		String name = getTopBackStackName();

		if (name == null || name.equals(BACKSTACK_LOADING) || name.equals(getFlightListBackStackName(0))
				|| name.equals(BACKSTACK_NO_FLIGHTS)) {
			finish();
		}
		else if (mCurrentAnimator != null && mCurrentAnimator.isRunning()) {
			// Allow reversal of the current animation.  If the user is already going backwards, skip
			if (mAnimForward) {
				reverseCurrentAnimation();
				mSetNewLegPosition = -1;
			}

			// If there is a backwards animation in progress, don't go back until it's done.
			// This just makes everything a lot easier at the moment.
			//
			// TODO: Improve this behavior at some point.
		}
		else {
			mAnimForward = false;
			super.onBackPressed();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		mKillReceiver.onDestroy();
	}

	//////////////////////////////////////////////////////////////////////////
	// Stack management

	private void popBackStack() {
		getSupportFragmentManager().popBackStack();
		mAnimForward = false;
	}

	private void popBackStack(String name) {
		getSupportFragmentManager().popBackStack(name, 0);
		mSkipAnimation = true;
	}

	private void showLoadingFragment() {
		if (mStatusFragment == null) {
			mStatusFragment = new StatusFragment();
		}

		FragmentManager fm = getSupportFragmentManager();
		if (fm.getBackStackEntryCount() == 0) {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.replace(R.id.content_container, mStatusFragment, StatusFragment.TAG);
			ft.addToBackStack(BACKSTACK_LOADING);
			ft.commit();
		}
		else {
			fm.popBackStack(BACKSTACK_LOADING, 0);
		}

		mStatusFragment.showLoading(getString(R.string.loading_flights));
	}

	private void showNoFlights(CharSequence errMsg) {
		mNoFlightsFragment = NoFlightsFragment.newInstance(errMsg);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.content_container, mNoFlightsFragment, NoFlightsFragment.TAG);
		ft.addToBackStack(BACKSTACK_NO_FLIGHTS);
		ft.commit();
	}

	private void showResultsListFragment(int position) {
		mListFragment = FlightListFragment.newInstance(position);

		// Make sure to cover up while showing the list fragment
		if (position == 0) {
			mStatusFragment.setCoverEnabled(true);
		}

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.content_container, mListFragment, FlightListFragment.TAG);
		ft.addToBackStack(getFlightListBackStackName(position));
		ft.commit();

		mAnimForward = true;
	}

	private void showFlightDetails(FlightTrip trip, FlightLeg leg) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		mFlightDetailsFragment = FlightDetailsFragment.newInstance(trip, leg, mLegPosition);
		ft.add(R.id.details_container, mFlightDetailsFragment, FlightDetailsFragment.TAG);
		ft.addToBackStack(getFlightDetailsBackStackName(mLegPosition));
		ft.commit();

		mAnimForward = true;
	}

	private void deselectBackToLegPosition(int newLegPosition) {
		// Clear the selected flight legs
		setNewLegPosition(newLegPosition);

		// Pop back stack to proper location
		popBackStack(getFlightListBackStackName(newLegPosition));
	}

	//////////////////////////////////////////////////////////////////////////
	// Animation

	private Animator mCurrentAnimator;

	private Fragment mAnimRemoveFragment;

	// Determines whether we're moving *forwards* or *backwards
	private boolean mAnimForward;

	// Whether or not to skip an animation.  Is defaul to true, so
	// that we intentionally always skip the first animation (since
	// it is either the showing of the flight list, which has no animation,
	// or it is on config change, which also should not animate).
	private boolean mSkipAnimation = true;

	private int mSetNewLegPosition;

	private void onFragmentLoaded(Fragment fragment) {
		Log.d("onFragmentLoaded(" + fragment.getTag() + "): skipAnim=" + mSkipAnimation + " animForward="
				+ mAnimForward);

		if (mSkipAnimation) {
			mSkipAnimation = false;
			return;
		}

		// Find out where we're headed
		String backStackTopName = getTopBackStackName();
		FlightLeg flightLeg = (mFlightDetailsFragment != null) ? mFlightDetailsFragment.getFlightLeg() : null;
		mSetNewLegPosition = -1;
		mAnimRemoveFragment = null;
		Animator forwardAnim = null;
		Animator backwardAnim = null;
		if (mAnimForward) {
			if (backStackTopName.startsWith(BACKSTACK_FLIGHT_LIST_PREFIX) && mFlightDetailsFragment != null) {
				Pair<Integer, Integer> topAndBottom = mListFragment.getSelectedFlightCardTopAndBottom();

				// Details --> List
				mAnimRemoveFragment = mFlightDetailsFragment;
				forwardAnim = mListFragment.createLegSelectAnimator(true);
				backwardAnim = mFlightDetailsFragment.createAnimator(topAndBottom.first, topAndBottom.second, false);

				// Save new leg position here, before animation (in case it gets canceled due to config change)
				mSetNewLegPosition = 1;
			}
			else if (backStackTopName.startsWith(BACKSTACK_FLIGHT_DETAILS_PREFIX)) {
				Pair<Integer, Integer> topAndBottom = mListFragment.getFlightCardTopAndBottom(flightLeg);

				// List --> Details
				mAnimRemoveFragment = mListFragment;
				forwardAnim = mFlightDetailsFragment.createAnimator(topAndBottom.first, topAndBottom.second, true);
				backwardAnim = mListFragment.createLegClickAnimator(false, flightLeg);
			}
		}
		else {
			if (backStackTopName.startsWith(BACKSTACK_FLIGHT_DETAILS_PREFIX)) {
				Pair<Integer, Integer> topAndBottom = mListFragment.getFlightCardTopAndBottom(flightLeg);

				// Details --> List (back)
				mAnimRemoveFragment = mFlightDetailsFragment;
				forwardAnim = mListFragment.createLegClickAnimator(true, flightLeg);
				backwardAnim = mFlightDetailsFragment.createAnimator(topAndBottom.first, topAndBottom.second, false);
			}
			else if (backStackTopName.startsWith(BACKSTACK_FLIGHT_LIST_PREFIX) && mFlightDetailsFragment != null) {
				Pair<Integer, Integer> topAndBottom = mListFragment.getSelectedFlightCardTopAndBottom();

				// List --> Details (back)
				mAnimRemoveFragment = mFlightDetailsFragment;
				forwardAnim = mFlightDetailsFragment.createAnimator(topAndBottom.first, topAndBottom.second, true);
				backwardAnim = mListFragment.createLegSelectAnimator(false);

				// Save new leg position here, before animation (in case it gets canceled due to config change)
				mSetNewLegPosition = 0;
			}
		}

		if (mAnimRemoveFragment == null || forwardAnim == null || backwardAnim == null) {
			return;
		}

		// Begin an animation to unload things
		AnimatorSet set = new AnimatorSet();
		set.playTogether(forwardAnim, backwardAnim);
		set.setDuration(450);
		set.addListener(mAnimatorListener);
		set.start();

		mCurrentAnimator = set;
	}

	private AnimatorListenerAdapter mAnimatorListener = new AnimatorListenerAdapter() {
		@Override
		public void onAnimationStart(Animator animation) {
			setMenusEnabled(false);
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			if (mAnimForward) {
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				ft.remove(mAnimRemoveFragment);
				if (mAnimRemoveFragment == mListFragment) {
					ft.addToBackStack(BACKSTACK_ANIM_FLIGHT_LIST_PREFIX + mLegPosition);
				}
				else if (mAnimRemoveFragment == mFlightDetailsFragment) {
					ft.addToBackStack(BACKSTACK_ANIM_FLIGHT_DETAILS_PREFIX + mLegPosition);
				}
				ft.commit();
			}
			else {
				getSupportFragmentManager().popBackStack();
			}

			setMenusEnabled(true);

			if (mSetNewLegPosition != -1) {
				setNewLegPosition(mSetNewLegPosition);
			}
		}
	};

	private void setMenusEnabled(boolean enabled) {
		for (int a = 0; a < mMenu.size(); a++) {
			mMenu.getItem(a).setEnabled(enabled);
		}

		mSelectFlightButton.setEnabled(enabled);
		mCancelButton.setEnabled(enabled);
		mSearchMenuItem.getActionView().setEnabled(enabled);
	}

	private void reverseCurrentAnimation() {
		mAnimRemoveFragment = (mAnimRemoveFragment == mListFragment) ? mFlightDetailsFragment : mListFragment;
		mAnimForward = !mAnimForward;
		reverseAnimator(mCurrentAnimator);
	}

	private void reverseAnimator(Animator animator) {
		Stack<Animator> stack = new Stack<Animator>();
		stack.add(animator);

		while (!stack.isEmpty()) {
			Animator anim = stack.pop();

			if (anim instanceof ValueAnimator) {
				((ValueAnimator) anim).reverse();
			}
			else if (anim instanceof AnimatorSet) {
				stack.addAll(((AnimatorSet) anim).getChildAnimations());
			}
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Action bar

	private Menu mMenu;

	private MenuItem mSearchMenuItem;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		mMenu = menu;

		getSupportMenuInflater().inflate(R.menu.menu_flight_results, menu);

		mSearchMenuItem = menu.findItem(R.id.menu_search);
		mSearchMenuItem.getActionView().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onOptionsItemSelected(mSearchMenuItem);
			}
		});

		return super.onCreateOptionsMenu(menu);
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// Either show standard action bar options, or just show the custom
		// flight details action view, depending on whether flight details
		// are currently visible
		boolean isSearching = BackgroundDownloader.getInstance().isDownloading(DOWNLOAD_KEY);
		boolean areFlightDetailsShowing = areFlightDetailsShowing();
		boolean gotNoResults = getTopBackStackName().equals(BACKSTACK_NO_FLIGHTS);
		mFlightSummaryContainer.setVisibility(areFlightDetailsShowing ? View.GONE : View.VISIBLE);
		mFlightDetailsActionContainer.setVisibility(areFlightDetailsShowing ? View.VISIBLE : View.GONE);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowHomeEnabled(!areFlightDetailsShowing);
		actionBar.setDisplayShowCustomEnabled(!isSearching && !gotNoResults);
		actionBar.setDisplayShowTitleEnabled(isSearching || gotNoResults);
		for (int a = 0; a < menu.size(); a++) {
			menu.getItem(a).setVisible(!areFlightDetailsShowing && !gotNoResults);
		}

		if (isSearching) {
			actionBar.setTitle(R.string.searching);
		}
		else {
			actionBar.setTitle(R.string.search_flights);
		}

		// Search item has slightly different rules for visibility
		mSearchMenuItem.setVisible(!isSearching && !gotNoResults && !areFlightDetailsShowing);

		if (!areFlightDetailsShowing && !gotNoResults) {
			updateTitleBar();

			// Show sort/filter only if we have results and are not showing the search params fragment/flight details
			FlightSearchResponse response = Db.getFlightSearch().getSearchResponse();
			boolean resultsVisible = response != null && !response.hasErrors();
			menu.setGroupVisible(R.id.group_results, resultsVisible);

			// Crazy hack to get the item view to take up space.
			//
			// ASSUMPTIONS:
			// 1. You can detect split action bar status using ABS
			// 2. There are a detectable # of menu items
			// 3. The action bar is the full window width
			if (ResourcesCompat.getResources_getBoolean(this, R.bool.abs__split_action_bar_is_narrow)
					&& mSearchMenuItem.isVisible()) {
				int numVisible = 0;
				for (int a = 0; a < menu.size(); a++) {
					if (menu.getItem(a).isVisible()) {
						numVisible++;
					}
				}
				mSearchMenuItem.getActionView().setMinimumWidth(
						getWindowManager().getDefaultDisplay().getWidth() / numVisible);
			}

			if (resultsVisible) {
				// Configure the checked sort button
				FlightFilter filter = Db.getFlightSearch().getFilter(mLegPosition);
				int selectedId;
				switch (filter.getSort()) {
				default:
				case PRICE:
					selectedId = R.id.menu_select_sort_price;
					break;
				case DEPARTURE:
					selectedId = R.id.menu_select_sort_departs;
					break;
				case ARRIVAL:
					selectedId = R.id.menu_select_sort_arrives;
					break;
				case DURATION:
					selectedId = R.id.menu_select_sort_duration;
					break;
				}
				menu.findItem(selectedId).setChecked(true);
			}
		}

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home: {
			// Either go back one leg, or go back to the search page
			if (mLegPosition != 0) {
				deselectBackToLegPosition(mLegPosition - 1);
			}
			else {
				NavUtils.goToFlights(this);
			}
			return true;
		}
		case R.id.menu_select_sort_price:
		case R.id.menu_select_sort_departs:
		case R.id.menu_select_sort_arrives:
		case R.id.menu_select_sort_duration:
			FlightFilter filter = Db.getFlightSearch().getFilter(mLegPosition);
			switch (item.getItemId()) {
			case R.id.menu_select_sort_price:
				filter.setSort(Sort.PRICE);
				break;
			case R.id.menu_select_sort_departs:
				filter.setSort(Sort.DEPARTURE);
				break;
			case R.id.menu_select_sort_arrives:
				filter.setSort(Sort.ARRIVAL);
				break;
			case R.id.menu_select_sort_duration:
				filter.setSort(Sort.DURATION);
				break;
			}
			filter.notifyFilterChanged();
			item.setChecked(true);

			OmnitureTracking.trackLinkFlightSort(mContext, filter.getSort().name(), mLegPosition);

			return true;
		case R.id.menu_search: {
			openEditSearchOverlay();
			return true;
		}
		}

		return super.onOptionsItemSelected(item);
	}

	public void updateTitleBar() {
		// Configure the title based on which leg the user is selecting
		FlightSearchParams params = Db.getFlightSearch().getSearchParams();
		int titleStrId = (mLegPosition == 0) ? R.string.outbound_TEMPLATE : R.string.inbound_TEMPLATE;
		Location location = (mLegPosition == 0) ? params.getArrivalLocation() : params.getDepartureLocation();
		mTitleTextView.setText(getString(titleStrId, StrUtils.getLocationCityOrCode(location)));

		// Configure subtitle based on which user the leg is selecting
		Date date = (mLegPosition == 0) ? params.getDepartureDate() : params.getReturnDate();
		mSubtitleTextView.setText(DateUtils.formatDateTime(mContext, date.getCalendar().getTimeInMillis(),
				DateUtils.FORMAT_SHOW_DATE + DateUtils.FORMAT_SHOW_YEAR + DateUtils.FORMAT_SHOW_WEEKDAY));
	}

	//////////////////////////////////////////////////////////////////////////
	// Search download

	private void openEditSearchOverlay() {
		Intent intent = new Intent(this, FlightSearchOverlayActivity.class);
		startActivityForResult(intent, REQUEST_CODE_SEARCH_PARAMS);

		OmnitureTracking.trackLinkFlightRefine(mContext, mLegPosition);
	}

	private void startSearch() {
		// #445: Need to reset the search results before starting a new one
		Db.getFlightSearch().setSearchResponse(null);
		mLegPosition = 0;
		Db.kickOffBackgroundSave(this);

		showLoadingFragment();

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		bd.cancelDownload(DOWNLOAD_KEY);
		bd.startDownload(DOWNLOAD_KEY, mDownload, mDownloadCallback);

		bd.cancelDownload(BACKGROUND_IMAGE_INFO_DOWNLOAD_KEY);
		bd.startDownload(BACKGROUND_IMAGE_INFO_DOWNLOAD_KEY, mBackgroundImageInfoDownload,
				mBackgroundImageInfoDownloadCallback);

	}

	private Download<FlightSearchResponse> mDownload = new Download<FlightSearchResponse>() {
		@Override
		public FlightSearchResponse doDownload() {
			ExpediaServices services = new ExpediaServices(mContext);
			BackgroundDownloader.getInstance().addDownloadListener(DOWNLOAD_KEY, services);
			return services.flightSearch(Db.getFlightSearch().getSearchParams(), 0);
		}
	};

	private OnDownloadComplete<FlightSearchResponse> mDownloadCallback = new OnDownloadComplete<FlightSearchResponse>() {
		@Override
		public void onDownload(FlightSearchResponse response) {
			Log.i("Finished flights download!");

			// If the response is null, fake an error response (for the sake of cleaner code)
			if (response == null) {
				response = new FlightSearchResponse();
				ServerError error = new ServerError(ApiMethod.FLIGHT_SEARCH);
				error.setPresentationMessage(getString(R.string.error_server));
				error.setCode("SIMULATED");
				response.addError(error);
			}

			FlightSearch search = Db.getFlightSearch();
			search.setSearchResponse(response);
			Db.addAirlineNames(response.getAirlineNames());

			Db.kickOffBackgroundSave(mContext);

			if (response.hasErrors()) {
				handleErrors(response);
			}
			else if (response.getTripCount() == 0) {
				showNoFlights(null);
			}
			else {

				//We set the bgimage for getter or worse
				BackgroundDownloader.getInstance().unregisterDownloadCallback(BACKGROUND_IMAGE_INFO_DOWNLOAD_KEY);
				BackgroundDownloader.getInstance().unregisterDownloadCallback(BACKGROUND_IMAGE_FILE_DOWNLOAD_KEY);
				BackgroundDownloader.getInstance().cancelDownload(BACKGROUND_IMAGE_INFO_DOWNLOAD_KEY);
				BackgroundDownloader.getInstance().cancelDownload(BACKGROUND_IMAGE_FILE_DOWNLOAD_KEY);
				if (FlightSearchResultsActivity.this.mBgFragment != null) {
					BackgroundImageCache cache = Db.getBackgroundImageCache(FlightSearchResultsActivity.this);
					if (cache.isAddingBitmap()) {
						//Didn't finish in time. We continue to download, but we get rid of our reference to the bg key, and thus revert to defaults
						Db.setBackgroundImageInfo(null);
					}
					FlightSearchResultsActivity.this.mBgFragment.setBitmap(
							Db.getBackgroundImage(FlightSearchResultsActivity.this, false),
							Db.getBackgroundImage(FlightSearchResultsActivity.this, true));

				}

				showResultsListFragment(0);
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// Background Image Download

	private Download<BackgroundImageResponse> mBackgroundImageInfoDownload = new Download<BackgroundImageResponse>() {
		@SuppressWarnings("deprecation")
		@SuppressLint("NewApi")
		@Override
		public BackgroundImageResponse doDownload() {
			ExpediaServices services = new ExpediaServices(mContext);
			BackgroundDownloader.getInstance().addDownloadListener(BACKGROUND_IMAGE_INFO_DOWNLOAD_KEY, services);
			String code = Db.getFlightSearch().getSearchParams().getArrivalLocation().getDestinationId();

			int width, height;
			if (AndroidUtils.getSdkVersion() >= 13) {
				Display display = getWindowManager().getDefaultDisplay();
				Point size = new Point();
				display.getSize(size);
				width = size.x;
				height = size.y;
			}
			else {
				Display display = getWindowManager().getDefaultDisplay();
				width = display.getWidth();
				height = display.getHeight();
			}

			return services.getFlightsBackgroundImage(code, width, height);
		}
	};

	private OnDownloadComplete<BackgroundImageResponse> mBackgroundImageInfoDownloadCallback = new OnDownloadComplete<BackgroundImageResponse>() {
		@Override
		public void onDownload(BackgroundImageResponse response) {
			Log.i("Finished background image info download!");

			// If the response is null, fake an error response (for the sake of cleaner code)
			if (response == null) {
				response = new BackgroundImageResponse();
				ServerError error = new ServerError(ApiMethod.BACKGROUND_IMAGE);
				error.setPresentationMessage(getString(R.string.error_server));
				error.setCode("SIMULATED");
				response.addError(error);
			}

			Db.setBackgroundImageInfo(response);

			if (response.hasErrors()) {
				Log.e("Errors downloading background image info");
			}
			else {
				if (!TextUtils.isEmpty(response.getmCacheKey())) {
					BackgroundImageCache cache = Db.getBackgroundImageCache(FlightSearchResultsActivity.this);
					if (!cache.hasKeyAndBlurredKey(response.getmCacheKey())) {
						BackgroundDownloader bd = BackgroundDownloader.getInstance();
						bd.cancelDownload(BACKGROUND_IMAGE_FILE_DOWNLOAD_KEY);
						bd.startDownload(BACKGROUND_IMAGE_FILE_DOWNLOAD_KEY, mBackgroundImageFileDownload,
								mBackgroundImageFileDownloadCallback);
					}
				}
			}
		}
	};

	private Download<Bitmap> mBackgroundImageFileDownload = new Download<Bitmap>() {
		@Override
		public Bitmap doDownload() {
			ExpediaServices services = new ExpediaServices(mContext);
			BackgroundDownloader.getInstance().addDownloadListener(BACKGROUND_IMAGE_FILE_DOWNLOAD_KEY, services);
			return services.getFlightsBackgroundBitmap(Db.getBackgroundImageInfo().getmImageUrl());
		}
	};

	private OnDownloadComplete<Bitmap> mBackgroundImageFileDownloadCallback = new OnDownloadComplete<Bitmap>() {
		@Override
		public void onDownload(Bitmap response) {
			Log.i("Finished background image file download!");

			// If the response is null, fake an error response (for the sake of cleaner code)
			if (response != null) {
				BackgroundImageCache cache = Db.getBackgroundImageCache(FlightSearchResultsActivity.this);
				cache.putBitmap(Db.getBackgroundImageKey(), response, true);
			}
			else {
				Log.e("Image download returned null.");
			}
		}
	};

	private void handleErrors(FlightSearchResponse response) {
		// Collect all the error fields
		Set<String> invalidFields = new HashSet<String>();
		for (ServerError error : response.getErrors()) {
			if (error.getErrorCode() == ErrorCode.INVALID_INPUT) {
				invalidFields.add(error.getExtra("field"));
			}
		}

		if (invalidFields.size() > 0) {
			boolean invalidDeparture = invalidFields.contains("departureAirport");
			boolean invalidArrival = invalidFields.contains("arrivalAirport");

			int resId = 0;
			if (invalidDeparture && invalidArrival) {
				resId = R.string.error_invalid_departure_arrival_airports;
			}
			else if (invalidDeparture) {
				resId = R.string.error_invalid_departure_airport;
			}
			else if (invalidArrival) {
				resId = R.string.error_invalid_arrival_airport;
			}

			if (resId != 0) {
				showNoFlights(getString(resId));
				return;
			}
		}

		// If we haven't handled the error by now, throw generic message
		RetryErrorDialogFragment df = new RetryErrorDialogFragment();
		df.show(getSupportFragmentManager(), "retryErrorDialog");
		mStatusFragment.showError(null);
	}

	//////////////////////////////////////////////////////////////////////////
	// Flight list

	private String getFlightListBackStackName(int legPosition) {
		return BACKSTACK_FLIGHT_LIST_PREFIX + "#" + legPosition;
	}

	//////////////////////////////////////////////////////////////////////////
	// Flight details

	private boolean areFlightDetailsShowing() {
		return getTopBackStackName().startsWith(BACKSTACK_FLIGHT_DETAILS_PREFIX);
	}

	private String getFlightDetailsBackStackName(int legPosition) {
		return BACKSTACK_FLIGHT_DETAILS_PREFIX + "#" + legPosition;
	}

	private OnClickListener mOnCancelClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			popBackStack();
		}
	};

	private OnClickListener mSelectFlightClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// Set the selected leg
			FlightTripLeg ftl = mFlightDetailsFragment.getFlightTripLeg();
			FlightSearch flightSearch = Db.getFlightSearch();
			flightSearch.setSelectedLeg(mLegPosition, new FlightTripLeg(ftl.getFlightTrip(), ftl.getFlightLeg()));
			Db.kickOffBackgroundSave(mContext);

			if (flightSearch.getSelectedFlightTrip() == null) {
				OmnitureTracking.trackPageLoadFlightSearchResultsInboundList(mContext);

				// Remove the flight details fragment, show new list results
				showResultsListFragment(1);
			}
			else {
				Intent intent = new Intent(mContext, FlightTripOverviewActivity.class);
				startActivity(intent);
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// Filter dialog

	public void showFilterDialog() {
		FlightFilterDialogFragment fragment = FlightFilterDialogFragment.newInstance(mLegPosition);
		fragment.show(getSupportFragmentManager(), FlightFilterDialogFragment.TAG);
	}

	//////////////////////////////////////////////////////////////////////////
	// FlightListFragmentListener

	@Override
	public void onFlightListLayout(FlightListFragment fragment) {
		mListFragment = fragment;
		onFragmentLoaded(mListFragment);
	}

	@Override
	public void onFlightLegClick(FlightTrip trip, FlightLeg leg, int legPosition) {
		showFlightDetails(trip, leg);
	}

	@Override
	public void onDeselectFlightLeg() {
		popBackStack(getFlightListBackStackName(0));

		// Note: For now, deselecting a flight here always means it is the outbound flight that is removed
		OmnitureTracking.trackLinkFlightRemoveOutboundSelection(mContext);
	}

	@Override
	public void onDisableFade() {
		mBgFragment.setFadeEnabled(false);
	}

	@Override
	public void onFadeRangeChange(int startY, int endY) {
		mBgFragment.setFadeRange(startY, endY);
	}

	//////////////////////////////////////////////////////////////////////////
	// OnBackStackChangedListener

	private void setNewLegPosition(int legPosition) {
		Log.d("Setting new leg position=" + legPosition + ", last=" + mLegPosition);

		if (mLegPosition > legPosition) {
			Db.getFlightSearch().setSelectedLeg(mLegPosition, null);
			Db.getFlightSearch().clearQuery(mLegPosition); // #443: Clear cached query
		}

		Db.kickOffBackgroundSave(mContext);

		mLegPosition = legPosition;
	}

	@Override
	public void onBackStackChanged() {
		String name = getTopBackStackName();

		if (name.startsWith(BACKSTACK_FLIGHT_DETAILS_PREFIX)) {
			onDisableFade();
		}

		supportInvalidateOptionsMenu();

		// Leave debug message
		Log.d("onBackStackChanged(): legPos=" + mLegPosition + ", stack=" + getBackStackDebugString());
	}

	//////////////////////////////////////////////////////////////////////////
	// Back stack utils

	public String getTopBackStackName() {
		FragmentManager fm = getSupportFragmentManager();
		int backStackEntryCount = fm.getBackStackEntryCount();
		if (backStackEntryCount > 0) {
			// Skip anim from backstack, as it's just an interstitial state
			String name = fm.getBackStackEntryAt(backStackEntryCount - 1).getName();
			if (name.startsWith(BACKSTACK_ANIM_PREFIX)) {
				name = fm.getBackStackEntryAt(backStackEntryCount - 2).getName();
			}
			return name;
		}
		return "";
	}

	public String getBackStackDebugString() {
		FragmentManager fm = getSupportFragmentManager();
		StringBuilder sb = new StringBuilder();
		for (int a = 0; a < fm.getBackStackEntryCount(); a++) {
			if (a != 0) {
				sb.append(" --> ");
			}

			sb.append(fm.getBackStackEntryAt(a).getName());
		}
		return sb.toString();
	}

	//////////////////////////////////////////////////////////////////////////
	// RetryErrorDialogFragmentListener

	@Override
	public void onRetryError() {
		startSearch();
	}

	@Override
	public void onCancelErorr() {
		finish();
	}

	//////////////////////////////////////////////////////////////////////////
	// NoFlightsFragmentListener

	@Override
	public void onClickEditSearch() {
		openEditSearchOverlay();
	}

	//////////////////////////////////////////////////////////////////////////
	// FlightDetailsFragmentListener

	@Override
	public void onFlightDetailsLayout(FlightDetailsFragment fragment) {
		mFlightDetailsFragment = fragment;
		onFragmentLoaded(mFlightDetailsFragment);
	}
}
