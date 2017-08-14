package com.expedia.bookings.activity;

import java.util.List;

import org.joda.time.DateTime;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.dialog.GooglePlayServicesDialog;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.fragment.ItinItemListFragment;
import com.expedia.bookings.fragment.ItinItemListFragment.ItinItemListFragmentListener;
import com.expedia.bookings.fragment.LoginConfirmLogoutDialogFragment.DoLogoutListener;
import com.expedia.bookings.fragment.PhoneLaunchFragment;
import com.expedia.bookings.interfaces.IPhoneLaunchActivityLaunchFragment;
import com.expedia.bookings.interfaces.IPhoneLaunchFragmentListener;
import com.expedia.bookings.notification.Notification;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.DebugMenu;
import com.expedia.bookings.utils.ExpediaDebugUtil;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.DisableableViewPager;
import com.expedia.bookings.widget.ItinListView;
import com.expedia.bookings.widget.PhoneLaunchToolbar;
import com.squareup.phrase.Phrase;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class PhoneLaunchActivity extends ActionBarActivity implements ItinListView.OnListModeChangedListener,
	ItinItemListFragmentListener, IPhoneLaunchFragmentListener, DoLogoutListener {

	public static final String ARG_FORCE_SHOW_WATERFALL = "ARG_FORCE_SHOW_WATERFALL";
	public static final String ARG_FORCE_SHOW_ITIN = "ARG_FORCE_SHOW_ITIN";
	public static final String ARG_JUMP_TO_NOTIFICATION = "ARG_JUMP_TO_NOTIFICATION";

	private static final int REQUEST_SETTINGS = 1;
	private static final int PAGER_POS_WATERFALL = 0;

	private static final int PAGER_POS_ITIN = 1;

	private static final int TOOLBAR_ANIM_DURATION = 200;

	private IPhoneLaunchActivityLaunchFragment mLaunchFragment;
	private ItinItemListFragment mItinListFragment;

	private PagerAdapter mPagerAdapter;

	@InjectView(R.id.viewpager)
	DisableableViewPager mViewPager;

	@InjectView(R.id.launch_toolbar)
	PhoneLaunchToolbar mToolbar;

	private int mPagerPosition = PAGER_POS_WATERFALL;
	private boolean mHasMenu = false;

	private String mJumpToItinId = null;

	public PhoneLaunchToolbar getToolbar() {
		return mToolbar;
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// Static Methods
	//////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Create intent to open this activity and jump straight to a particular itin item.
	 *
	 * @param context
	 * @return
	 */
	public static Intent createIntent(Context context, Notification notification) {
		Intent intent = new Intent(context, PhoneLaunchActivity.class);
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
		Ui.getApplication(this).defaultLaunchComponents();

		if (Db.getTripBucket().isEmpty()) {
			Db.loadTripBucket(this);
		}

		super.onCreate(savedInstanceState);

		getWindow().setFormat(android.graphics.PixelFormat.RGBA_8888);

		setContentView(R.layout.activity_phone_launch);
		ButterKnife.inject(this);
		getWindow().setBackgroundDrawable(null);

		// View Pager
		mPagerAdapter = new PagerAdapter(getSupportFragmentManager());
		mViewPager.setAdapter(mPagerAdapter);

		// Toolbar/support Actionbar
		mToolbar.slidingTabLayout.setViewPager(mViewPager);
		mToolbar.slidingTabLayout.setOnPageChangeListener(mPageChangeListener);
		setSupportActionBar(mToolbar);
		getSupportActionBar().getThemedContext();

		Intent intent = getIntent();
		LineOfBusiness lineOfBusiness = (LineOfBusiness) intent.getSerializableExtra(Codes.LOB_NOT_SUPPORTED);
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
		else if (lineOfBusiness != null) {
			CharSequence errorMessage = null;
			if (lineOfBusiness == LineOfBusiness.CARS) {
				errorMessage = Phrase.from(this, R.string.lob_not_supported_error_message)
					.put("lob", getString(R.string.Car))
					.format();
			}
			else if (lineOfBusiness == LineOfBusiness.LX) {
				errorMessage = Phrase.from(this, R.string.lob_not_supported_error_message)
					.put("lob", getString(R.string.Activity))
					.format();
			}
			showLOBNotSupportedAlertMessage(this, errorMessage, R.string.ok);
		}

		AlertDialog unsupportedVersionDialog = ProductFlavorFeatureConfiguration.getInstance().getUnsupportedVersionDialog(this);
		if(unsupportedVersionDialog != null) {
			unsupportedVersionDialog.show();
		}

		// Debug code to notify QA to open ExpediaDebug app
		ExpediaDebugUtil.showExpediaDebugToastIfNeeded(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		GooglePlayServicesDialog gpsd = new GooglePlayServicesDialog(this);
		gpsd.startChecking();

		supportInvalidateOptionsMenu();

		OmnitureTracking.onResume(this);

		AdTracker.trackViewHomepage();
	}

	@Override
	protected void onPause() {
		super.onPause();
		OmnitureTracking.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Db.setLaunchListHotelData(null);
	}

	@Override
	public void onBackPressed() {
		if (mLaunchFragment != null && mLaunchFragment.onBackPressed()) {
			return;
		}

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
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_launch, menu);

		DebugMenu.onCreateOptionsMenu(this, menu);
		mHasMenu = super.onCreateOptionsMenu(menu);
		return mHasMenu;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean retVal = super.onPrepareOptionsMenu(menu);
		if (menu != null) {
			boolean loginBtnEnabled = !User.isLoggedIn(this);
			boolean logoutBtnEnabled = User.isLoggedIn(this);

			MenuItem loginBtn = menu.findItem(R.id.ab_log_in);
			if (loginBtn != null) {
				loginBtn.setVisible(loginBtnEnabled);
				loginBtn.setEnabled(loginBtnEnabled);
			}
			MenuItem logOutBtn = menu.findItem(R.id.ab_log_out);
			if (logOutBtn != null) {
				logOutBtn.setVisible(logoutBtnEnabled);
				logOutBtn.setEnabled(logoutBtnEnabled);
			}
		}
		if (BuildConfig.RELEASE) {
			MenuItem settingsBtn = menu.findItem(R.id.settings);
			if (settingsBtn != null) {
				settingsBtn.setVisible(ProductFlavorFeatureConfiguration.getInstance().isSettingsInMenuVisible());
			}
		}
		DebugMenu.onPrepareOptionsMenu(this, menu);

		return retVal;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home: {
			gotoWaterfall();
			return true;
		}
		case R.id.ab_log_in: {
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

		return super.onOptionsItemSelected(item);
	}

	/**
	 * Returns true if the user has an in progress or upcoming trip, as of the current time.
	 *
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
	 * <p/>
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

	private SimpleOnPageChangeListener mPageChangeListener = new SimpleOnPageChangeListener() {
		@Override
		public void onPageScrollStateChanged(int state) {
			super.onPageScrollStateChanged(state);
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
	};

	private synchronized void gotoWaterfall() {
		if (mLaunchFragment != null && mLaunchFragment.onBackPressed()) {
			return;
		}
		if (mPagerPosition != PAGER_POS_WATERFALL) {
			mPagerPosition = PAGER_POS_WATERFALL;
			mViewPager.setCurrentItem(PAGER_POS_WATERFALL);

			if (mItinListFragment != null && mItinListFragment.isInDetailMode()) {
				mItinListFragment.hideDetails();
			}

			if (mHasMenu) {
				supportInvalidateOptionsMenu();
			}
		}
	}

	private synchronized void gotoItineraries() {
		if (mPagerPosition != PAGER_POS_ITIN) {
			if (mItinListFragment != null) {
				mItinListFragment.resetTrackingState();
				mItinListFragment.enableLoadItins();
			}

			mPagerPosition = PAGER_POS_ITIN;
			mViewPager.setCurrentItem(PAGER_POS_ITIN);

			if (mHasMenu) {
				supportInvalidateOptionsMenu();
			}
		}

		if (mJumpToItinId != null && mItinListFragment != null) {
			mItinListFragment.showItinCard(mJumpToItinId, false);
			mJumpToItinId = null;
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
				frag = new PhoneLaunchFragment();
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

		@Override
		public String getPageTitle(int i) {
			String title;
			switch (i) {
			case PAGER_POS_ITIN:
				title = getResources()
					.getString(Ui.obtainThemeResID(PhoneLaunchActivity.this, R.attr.skin_tripsTabText));
				break;
			case PAGER_POS_WATERFALL:
				title = getResources().getString(R.string.shop);
				break;
			default:
				throw new RuntimeException("Position out of bounds position = " + i);
			}
			return title;
		}
	}

	private ValueAnimator.AnimatorUpdateListener hideToolbarAnimator = new ValueAnimator.AnimatorUpdateListener() {
		@Override
		public void onAnimationUpdate(ValueAnimator arg0) {
			float val = (float) arg0.getAnimatedValue();
			mToolbar.setTranslationY(-val * mToolbar.getHeight());
		}
	};

	private Animator.AnimatorListener hideToolbarListener = new Animator.AnimatorListener() {

		@Override
		public void onAnimationStart(Animator animation) {
			mToolbar.setTranslationY(0);
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			mToolbar.setVisibility(View.GONE);
		}

		@Override
		public void onAnimationCancel(Animator animation) {
			// ignore
		}

		@Override
		public void onAnimationRepeat(Animator animation) {
			// ignore
		}
	};

	private ValueAnimator.AnimatorUpdateListener showToolbarAnimator = new ValueAnimator.AnimatorUpdateListener() {

		@Override
		public void onAnimationUpdate(ValueAnimator arg0) {
			float val = (float) arg0.getAnimatedValue();
			mToolbar.setTranslationY((1 - val) * -mToolbar.getHeight());
		}
	};

	private Animator.AnimatorListener showToolbarListener = new Animator.AnimatorListener() {
		@Override
		public void onAnimationStart(Animator animation) {
			mToolbar.setTranslationY(-getSupportActionBar().getHeight());
			mToolbar.setVisibility(View.VISIBLE);
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			// ignore
		}

		@Override
		public void onAnimationCancel(Animator animation) {
			// ignore
		}

		@Override
		public void onAnimationRepeat(Animator animation) {
			// ignore
		}
	};


	@Override
	public void onListModeChanged(boolean isInDetailMode, boolean animate) {
		mViewPager.setPageSwipingEnabled(!isInDetailMode);
		if (isInDetailMode) {
			if (getSupportActionBar().isShowing()) {
				ValueAnimator anim = ValueAnimator.ofFloat(0, 1);
				anim.setDuration(TOOLBAR_ANIM_DURATION);
				anim.addUpdateListener(hideToolbarAnimator);
				anim.addListener(hideToolbarListener);
				anim.start();
			}
		}
		else {
			// The collapse animation takes 400ms, and the actionbar.show
			// animation happens in 200ms, so make it use the last 200ms
			// of the animation (and check to make sure there wasn't another
			// mode change in between)
			ValueAnimator anim = ValueAnimator.ofFloat(0, 1);
			anim.setDuration(TOOLBAR_ANIM_DURATION);
			anim.addUpdateListener(showToolbarAnimator);
			anim.addListener(showToolbarListener);
			mToolbar.setVisibility(View.VISIBLE);
			anim.start();
		}
	}

	@Override
	public void onLaunchFragmentAttached(IPhoneLaunchActivityLaunchFragment frag) {
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

	public static void showLOBNotSupportedAlertMessage(Context context, CharSequence errorMessage,
		int confirmButtonResourceId) {
		AlertDialog.Builder b = new AlertDialog.Builder(context);
		b.setCancelable(false)
			.setMessage(errorMessage)
			.setPositiveButton(confirmButtonResourceId, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			})
			.show();
	}
}
