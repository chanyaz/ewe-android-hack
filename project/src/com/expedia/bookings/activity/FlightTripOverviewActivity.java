package com.expedia.bookings.activity;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.User;
import com.expedia.bookings.fragment.FlightCheckoutFragment;
import com.expedia.bookings.fragment.FlightCheckoutFragment.CheckoutInformationListener;
import com.expedia.bookings.fragment.FlightSlideToPurchaseFragment;
import com.expedia.bookings.fragment.FlightTripOverviewFragment;
import com.expedia.bookings.fragment.FlightTripOverviewFragment.DisplayMode;
import com.expedia.bookings.fragment.FlightTripPriceFragment;
import com.expedia.bookings.fragment.RetryErrorDialogFragment.RetryErrorDialogFragmentListener;
import com.expedia.bookings.fragment.SignInFragment.SignInFragmentListener;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.ActionBarNavUtils;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.flightlib.utils.DateTimeUtils;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.animation.AnimatorProxy;

public class FlightTripOverviewActivity extends SherlockFragmentActivity implements SignInFragmentListener,
		CheckoutInformationListener, RetryErrorDialogFragmentListener {

	public static final String TAG_OVERVIEW_FRAG = "TAG_OVERVIEW_FRAG";
	public static final String TAG_CHECKOUT_FRAG = "TAG_CHECKOUT_FRAG";
	public static final String TAG_PRICE_BAR_BOTTOM_FRAG = "TAG_PRICE_BAR_BOTTOM_FRAG";
	public static final String TAG_SLIDE_TO_PURCHASE_FRAG = "TAG_SLIDE_TO_PURCHASE_FRAG";

	public static final String STATE_TAG_MODE = "STATE_TAG_MODE";
	public static final String STATE_TAG_STACKED_HEIGHT = "STATE_TAG_STACKED_HEIGHT";
	public static final String STATE_TAG_UNSTACKED_HEIGHT = "STATE_TAG_UNSTACKED_HEIGHT";
	public static final String STATE_TAG_LOADED_DB_INFO = "STATE_TAG_LOADED_DB_INFO";

	public static final int ANIMATION_DURATION = 450;
	public static final int ANIMATION_SNAPBACK_DURATION = 0;

	//We only want to load from disk once: when the activity is first started
	private boolean mLoadedDbInfo = false;
	private Semaphore mLoadCachedDataSem = new Semaphore(1);
	private Semaphore mTransitionSem = new Semaphore(1);

	private boolean mSafeToAttach = true;

	private FlightTripOverviewFragment mOverviewFragment;
	private FlightTripPriceFragment mPriceBottomFragment;
	private FlightCheckoutFragment mCheckoutFragment;
	private FlightSlideToPurchaseFragment mSlideToPurchaseFragment;

	private ViewGroup mOverviewContainer;
	private ViewGroup mCheckoutContainer;
	private ScrollView mContentScrollView;
	private View mBackToOverviewArea;

	private MenuItem mCheckoutMenuItem;

	private DisplayMode mDisplayMode = DisplayMode.OVERVIEW;
	private String mTripKey;

	private int mStackedHeight = 0;
	private int mUnstackedHeight = 0;

	private float mLastCheckoutPercentage = 0f;
	private float mCurrentCardDragDistance = 0f;

	// To make up for a lack of FLAG_ACTIVITY_CLEAR_TASK in older Android versions
	private ActivityKillReceiver mKillReceiver;

	private enum TrackingMode {
		OVERVIEW, CHECKOUT, SLIDE_TO_PURCHASE
	}

	// This variable exists to ensure that the correct tracking event gets called the correct number of times
	private TrackingMode mLastTrackingMode;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_flight_overview_and_checkout);

		mKillReceiver = new ActivityKillReceiver(this);
		mKillReceiver.onCreate();

		// Recover data if it was flushed from memory
		if (Db.getFlightSearch().getSearchResponse() == null) {
			if (!Db.loadCachedFlightData(this)) {
				NavUtils.onDataMissing(this);
			}
		}

		ViewGroup outerContainer = Ui.findView(this, R.id.outer_container);
		BitmapDrawable bg = new BitmapDrawable(getResources(), Db.getBackgroundImage(this, true));
		outerContainer.setBackgroundDrawable(bg);

		mContentScrollView = Ui.findView(this, R.id.content_scroll_view);
		mOverviewContainer = Ui.findView(this, R.id.trip_overview_container);
		mCheckoutContainer = Ui.findView(this, R.id.trip_checkout_container);
		mBackToOverviewArea = Ui.findView(this, R.id.back_to_overview_area);

		if (savedInstanceState != null) {
			mLoadedDbInfo = savedInstanceState.getBoolean(STATE_TAG_LOADED_DB_INFO, false);
		}

		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_TAG_MODE)) {
			mDisplayMode = DisplayMode.valueOf(savedInstanceState.getString(STATE_TAG_MODE));
		}

		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_TAG_STACKED_HEIGHT)) {
			mStackedHeight = savedInstanceState.getInt(STATE_TAG_STACKED_HEIGHT);
			setCheckoutContainerTopPadding(mStackedHeight);
			setBackToOverviewAreaHeight(mStackedHeight);
		}

		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_TAG_UNSTACKED_HEIGHT)) {
			mUnstackedHeight = savedInstanceState.getInt(STATE_TAG_UNSTACKED_HEIGHT);
			mOverviewContainer.setMinimumHeight(mUnstackedHeight);
			LayoutParams params = mOverviewContainer.getLayoutParams();
			if (params != null) {
				params.height = mUnstackedHeight;
				mOverviewContainer.setLayoutParams(params);
			}
		}

		mTripKey = Db.getFlightSearch().getSelectedFlightTrip().getProductKey();

		FragmentTransaction overviewTransaction = getSupportFragmentManager().beginTransaction();
		mOverviewFragment = Ui.findSupportFragment(this, TAG_OVERVIEW_FRAG);
		if (mOverviewFragment == null) {
			mOverviewFragment = FlightTripOverviewFragment.newInstance(mTripKey, mDisplayMode);
		}
		if (!mOverviewFragment.isAdded()) {
			overviewTransaction.add(R.id.trip_overview_container, mOverviewFragment, TAG_OVERVIEW_FRAG);
			overviewTransaction.commit();
		}

		FragmentTransaction bottomBarTrans = getSupportFragmentManager().beginTransaction();
		mPriceBottomFragment = Ui.findSupportFragment(this, TAG_PRICE_BAR_BOTTOM_FRAG);
		if (mPriceBottomFragment == null) {
			mPriceBottomFragment = FlightTripPriceFragment.newInstance();
		}
		if (!mPriceBottomFragment.isAdded()) {
			bottomBarTrans.replace(R.id.trip_price_container_bottom, mPriceBottomFragment, TAG_PRICE_BAR_BOTTOM_FRAG);
			bottomBarTrans.commit();
		}

		//We load things from disk in the background
		startLoadChain();
	}

	@Override
	public void onResume() {
		super.onResume();

		mSafeToAttach = true;

		if (mOverviewFragment != null) {
			mStackedHeight = mOverviewFragment.getStackedHeight();
			mUnstackedHeight = mOverviewFragment.getUnstackedHeight();
			LayoutParams params = mOverviewContainer.getLayoutParams();
			if (params != null) {
				params.height = mUnstackedHeight;
				mOverviewContainer.setLayoutParams(params);
			}
		}

		if (mDisplayMode.compareTo(DisplayMode.CHECKOUT) == 0) {
			gotoCheckoutMode(false);
		}
		else {
			gotoOverviewMode(false);
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
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mKillReceiver != null) {
			mKillReceiver.onDestroy();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle out) {
		super.onSaveInstanceState(out);
		out.putString(STATE_TAG_MODE, mDisplayMode.name());
		out.putInt(STATE_TAG_STACKED_HEIGHT, mStackedHeight);
		out.putInt(STATE_TAG_UNSTACKED_HEIGHT, mUnstackedHeight);
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

	//We do some work on separate threads to keep the UI nice and snappy
	private void startLoadChain() {

		Runnable loadCacheRunnable = new Runnable() {
			@Override
			public void run() {
				loadCachedData();
			}
		};

		Thread startUpThread = new Thread(loadCacheRunnable);
		startUpThread.start();
	}

	private void loadCachedData() {
		boolean semGot = false;
		try {
			if (!mLoadedDbInfo && mLoadCachedDataSem.tryAcquire()) {
				semGot = true;
				Db.loadBillingInfo(this);
				BillingInfo billingInfo = Db.getBillingInfo();

				//Load billing info (only if we don't have a valid card already)
				if (billingInfo == null || TextUtils.isEmpty(billingInfo.getNumber())) {
					billingInfo.load(this);
					StoredCreditCard stored = billingInfo.getStoredCard();
					if (stored != null) {
						if (User.isLoggedIn(this)) {
							if (Db.getUser() == null) {
								Db.loadUser(this);
							}
							List<StoredCreditCard> usrCards = Db.getUser().getStoredCreditCards();
							boolean cardFound = false;
							for (int i = 0; i < usrCards.size(); i++) {
								if (stored.getId().compareTo(usrCards.get(i).getId()) == 0) {
									cardFound = true;
									break;
								}
							}
							//If the storedcard is not part of the user's collection of stored cards, we can't use it
							if (!cardFound) {
								Db.resetBillingInfo();
							}
						}
						else {
							//If we have an expedia account card, but we aren't logged in, we get rid of it
							Db.resetBillingInfo();
						}
					}
				}

				//Load traveler info (only if we don't have traveler info already)
				if (Db.getTravelers() == null || Db.getTravelers().size() == 0 || !Db.getTravelers().get(0).hasName()) {
					Db.loadTravelers(this);
					List<Traveler> travelers = Db.getTravelers();
					if (travelers != null && travelers.size() > 0) {
						if (User.isLoggedIn(this)) {
							//If we are logged in, we need to ensure that any expedia account users are associated with the currently logged in account
							if (Db.getUser() == null) {
								Db.loadUser(this);
							}
							List<Traveler> userTravelers = Db.getUser().getAssociatedTravelers();
							for (int i = 0; i < travelers.size(); i++) {
								Traveler trav = travelers.get(i);
								if (trav.hasTuid()) {
									boolean travFound = false;
									for (int j = 0; j < userTravelers.size(); j++) {
										Traveler usrTrav = userTravelers.get(j);
										if (usrTrav.getTuid().compareTo(trav.getTuid()) == 0) {
											travFound = true;
											break;
										}
									}
									if (!travFound) {
										travelers.set(i, new Traveler());
									}
								}
							}
						}
						else {
							//Remove logged in travelers (because the user is not logged in)
							for (int i = 0; i < travelers.size(); i++) {
								Traveler trav = travelers.get(i);
								if (trav.hasTuid()) {
									travelers.set(i, new Traveler());
								}
							}
						}
					}
				}

				//We only load from disk once
				mLoadedDbInfo = true;
			}
			else {
				//We wait for the semaphore
				mLoadCachedDataSem.acquire();
				semGot = true;
			}
		}
		catch (Exception ex) {
			Log.e("Exception loading data..", ex);
		}
		finally {
			if (semGot) {
				mLoadCachedDataSem.release();
			}
		}
	}

	public void attachCheckout() {
		if (mSafeToAttach) {
			boolean refreshCheckoutData = !mLoadedDbInfo;
			loadCachedData();//because of the sLoaded variable, this will almost always do no work except if we end up in a strange state
			FragmentTransaction checkoutTransaction = getSupportFragmentManager().beginTransaction();
			mCheckoutFragment = Ui.findSupportFragment(this, TAG_CHECKOUT_FRAG);
			if (mCheckoutFragment == null) {
				mCheckoutFragment = FlightCheckoutFragment.newInstance();
			}
			else if (refreshCheckoutData) {
				//Incase we only now finished loading cached data...
				mCheckoutFragment.refreshData();
			}

			if (mCheckoutFragment.isDetached()) {
				checkoutTransaction.attach(mCheckoutFragment);
				checkoutTransaction.commit();
			}
			else if (!mCheckoutFragment.isAdded()) {
				checkoutTransaction.add(R.id.trip_checkout_container, mCheckoutFragment, TAG_CHECKOUT_FRAG);
				checkoutTransaction.commit();
			}
		}
	}

	public void detachCheckout() {
		FragmentTransaction checkoutTransaction = getSupportFragmentManager().beginTransaction();
		mCheckoutFragment = Ui.findSupportFragment(this, TAG_CHECKOUT_FRAG);
		if (mCheckoutFragment != null && mCheckoutFragment.isAdded()) {
			checkoutTransaction.detach(mCheckoutFragment);
			checkoutTransaction.commit();
		}
	}

	public void replacePriceBarWithSlideToCheckout() {
		FragmentTransaction showSlideToCheckoutTransaction = getSupportFragmentManager().beginTransaction();
		mSlideToPurchaseFragment = Ui.findSupportFragment(this, TAG_SLIDE_TO_PURCHASE_FRAG);
		if (mSlideToPurchaseFragment == null) {
			mSlideToPurchaseFragment = FlightSlideToPurchaseFragment.newInstance();
		}
		if (!mSlideToPurchaseFragment.isAdded()) {
			showSlideToCheckoutTransaction.replace(R.id.trip_price_container_bottom, mSlideToPurchaseFragment,
					TAG_SLIDE_TO_PURCHASE_FRAG);
			showSlideToCheckoutTransaction.commit();
		}
	}

	public void replaceSlideToCheckoutWithPriceBar() {
		FragmentTransaction bottomBarTrans = getSupportFragmentManager().beginTransaction();
		mPriceBottomFragment = Ui.findSupportFragment(this, TAG_PRICE_BAR_BOTTOM_FRAG);
		if (mPriceBottomFragment == null) {
			mPriceBottomFragment = FlightTripPriceFragment.newInstance();
		}
		if (!mPriceBottomFragment.isAdded()) {
			bottomBarTrans.replace(R.id.trip_price_container_bottom, mPriceBottomFragment, TAG_PRICE_BAR_BOTTOM_FRAG);
			bottomBarTrans.commit();
		}
	}

	public void setBackToOverviewAreaHeight(int height) {
		RelativeLayout.LayoutParams params = (android.widget.RelativeLayout.LayoutParams) this.mBackToOverviewArea
				.getLayoutParams();
		if (params == null) {
			params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		}
		params.height = height;
		mBackToOverviewArea.setLayoutParams(params);
	}

	protected Animator getAnimator(float startPercentage, final float endPercentage) {
		ValueAnimator animator = ValueAnimator.ofFloat(startPercentage, endPercentage);
		animator.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator anim) {
				Float f = (Float) anim.getAnimatedValue();
				FlightTripOverviewActivity.this.setCheckoutPercent(f.floatValue());
			}
		});
		animator.addListener(new AnimatorListener() {

			@Override
			public void onAnimationCancel(Animator arg0) {
			}

			@Override
			public void onAnimationEnd(Animator arg0) {
				FlightTripOverviewActivity.this.setCheckoutPercent(endPercentage);
			}

			@Override
			public void onAnimationRepeat(Animator arg0) {
			}

			@Override
			public void onAnimationStart(Animator arg0) {
			}
		});
		return animator;
	}

	protected void playSnapBackAnimation(int duration) {
		Animator anim = getAnimator(this.mLastCheckoutPercentage, this.mDisplayMode == DisplayMode.CHECKOUT ? 1f : 0f);
		anim.setDuration(duration);
		anim.start();
	}

	public void gotoOverviewMode(boolean animate) {
		boolean semGot = false;
		try {
			mTransitionSem.acquire();
			semGot = true;
			mDisplayMode = DisplayMode.OVERVIEW;
			setActionBarOverviewMode();

			int duration = animate ? ANIMATION_DURATION : 1;

			replaceSlideToCheckoutWithPriceBar();

			if (mOverviewFragment != null && mOverviewFragment.isAdded()) {
				mBackToOverviewArea.setOnClickListener(null);
				mBackToOverviewArea.setOnTouchListener(null);
				mContentScrollView.setOnTouchListener(mPushToCheckoutOnTouchListener);

				mPriceBottomFragment.showPriceChange();

				if (animate) {
					Animator anim = getAnimator(mLastCheckoutPercentage, 0f);
					anim.setDuration(duration);
					anim.start();
				}
				else {
					this.setCheckoutPercent(0f);
				}
				mOverviewFragment.unStackCards(animate);

			}

			if (mLastTrackingMode != TrackingMode.OVERVIEW) {
				mLastTrackingMode = TrackingMode.OVERVIEW;
				OmnitureTracking.trackPageLoadFlightRateDetailsOverview(this);
			}

		}
		catch (Exception ex) {
			Log.e("Exception in gotoOverviewMode()", ex);
		}
		finally {
			if (semGot) {
				mTransitionSem.release();
			}
		}
	}

	public void gotoCheckoutMode(boolean animate) {
		boolean semGot = false;
		try {

			mTransitionSem.acquire();
			semGot = true;

			mDisplayMode = DisplayMode.CHECKOUT;
			setActionBarCheckoutMode();
			attachCheckout();

			int duration = animate ? ANIMATION_DURATION : 1;

			mContentScrollView.setOnTouchListener(null);
			mBackToOverviewArea.setOnTouchListener(mDragBackToOverviewOnTouchListener);

			if (mOverviewFragment != null && mOverviewFragment.isAdded()) {
				mStackedHeight = mOverviewFragment.getStackedHeight();
				mUnstackedHeight = mOverviewFragment.getUnstackedHeight();
				LayoutParams params = this.mOverviewContainer.getLayoutParams();
				params.height = mUnstackedHeight;
				mOverviewContainer.setLayoutParams(params);

				setCheckoutContainerTopPadding(mStackedHeight);

				if (animate) {
					Animator anim = getAnimator(mLastCheckoutPercentage, 1f);
					anim.setDuration(duration);
					anim.start();
				}
				else {
					setCheckoutPercent(1f);
				}
				mOverviewFragment.stackCards(animate);

				setBackToOverviewAreaHeight(mStackedHeight);
				if (mCheckoutFragment != null) {
					mCheckoutFragment.updateViewVisibilities();
				}
			}
		}
		catch (Exception ex) {
			Log.e("Exception in gotoCheckoutMode()", ex);
		}
		finally {
			if (semGot) {
				mTransitionSem.release();
			}
		}
	}

	OnTouchListener mDragBackToOverviewOnTouchListener = new OnTouchListener() {
		float mStartPosY = 0;
		long mStartTime = 0;
		float mLastPosY = 0;

		@Override
		public boolean onTouch(View arg0, MotionEvent arg1) {
			boolean retVal = false;
			try {
				int action = arg1.getActionMasked();
				switch (action) {
				case MotionEvent.ACTION_DOWN:
					mStartTime = Calendar.getInstance().getTimeInMillis();
					mStartPosY = arg1.getY();
					mBackToOverviewArea.getParent().requestDisallowInterceptTouchEvent(true);
					retVal = true;
					dragCards(0f, true, false);
					mLastPosY = mStartPosY;
					break;
				case MotionEvent.ACTION_MOVE:
					float sinceLastDif = mLastPosY - arg1.getY();
					setCheckoutPercent(dragCards(sinceLastDif, false, false));
					retVal = true;
					mLastPosY = arg1.getY();
					break;
				case MotionEvent.ACTION_UP:
					if (Calendar.getInstance().getTimeInMillis() - mStartTime < 100) {
						FlightTripOverviewActivity.this.gotoOverviewMode(true);
					}
					else {
						if (FlightTripOverviewActivity.this.mOverviewFragment != null
								&& FlightTripOverviewActivity.this.mOverviewFragment.getExpandedPercentage() > 0.95f) {
							FlightTripOverviewActivity.this.gotoOverviewMode(false);
						}
						else {
							playSnapBackAnimation(ANIMATION_SNAPBACK_DURATION);
						}
						mBackToOverviewArea.getParent().requestDisallowInterceptTouchEvent(false);
					}
					retVal = true;

					break;
				default:
					break;
				}
			}
			catch (Exception ex) {
				Log.e("Exception in mDragBackToOverviewOnTouchListener", ex);
			}
			return retVal;
		}

	};

	OnTouchListener mPushToCheckoutOnTouchListener = new OnTouchListener() {
		float mLastPosY = 0;

		@Override
		public boolean onTouch(View arg0, MotionEvent arg1) {

			boolean retVal = false;
			try {
				int action = arg1.getActionMasked();

				int scrollH = FlightTripOverviewActivity.this.mContentScrollView.getHeight();
				int scrollY = FlightTripOverviewActivity.this.mContentScrollView.getScrollY();
				int currentCardHeight = mOverviewFragment.getCurrentHeight();

				boolean bottomCardBelowFold = currentCardHeight - scrollY > scrollH;
				boolean topAboveFold = scrollY > 0;

				switch (action) {
				case MotionEvent.ACTION_DOWN:
					attachCheckout();
					mLastPosY = arg1.getY();
					if (mOverviewContainer != null)
						mOverviewContainer.getParent().requestDisallowInterceptTouchEvent(true);
					retVal = true;
					dragCards(0f, true, true);
					break;
				case MotionEvent.ACTION_MOVE:
					float sinceLastDif = mLastPosY - arg1.getY();

					if (bottomCardBelowFold && sinceLastDif > 0) {
						FlightTripOverviewActivity.this.mContentScrollView.scrollBy(0, Math.round(sinceLastDif));
					}
					else if (topAboveFold && sinceLastDif < 0) {
						FlightTripOverviewActivity.this.mContentScrollView.scrollBy(0, Math.round(sinceLastDif));
					}
					else if (topAboveFold && sinceLastDif > 0) {
						FlightTripOverviewActivity.this.mContentScrollView.scrollBy(0, -Math.round(sinceLastDif));
						setCheckoutPercent(dragCards(sinceLastDif, false, true));
					}
					else {
						setCheckoutPercent(dragCards(sinceLastDif, false, true));
					}

					mLastPosY = arg1.getY();
					retVal = true;
					break;
				case MotionEvent.ACTION_UP:
					if (FlightTripOverviewActivity.this.mOverviewFragment != null
							&& FlightTripOverviewActivity.this.mOverviewFragment.getExpandedPercentage() < 0.1f) {
						FlightTripOverviewActivity.this.gotoCheckoutMode(false);
					}
					else {
						playSnapBackAnimation(ANIMATION_SNAPBACK_DURATION);
					}
					mOverviewContainer.getParent().requestDisallowInterceptTouchEvent(false);
					retVal = true;
					break;
				default:
					break;
				}
			}
			catch (Exception ex) {
				Log.e("Exception in onTouch", ex);
			}
			return retVal;
		}
	};

	/**
	 * 
	 * @param dist - the distance moved defined by lastY - currentY 
	 * @param init - reset the tracked distance
	 * @param north - are we trying to move up (north) or down (south)
	 * @return - what percentage we have made the journey between 0f and 1f 
	 */
	private float dragCards(float dist, boolean init, boolean north) {
		if (init) {
			mCurrentCardDragDistance = 0;
		}

		int range = this.mUnstackedHeight - this.mStackedHeight;
		if (mContentScrollView != null && range > mContentScrollView.getHeight()) {
			range = mContentScrollView.getHeight() / 2;
		}

		mCurrentCardDragDistance += dist;

		if (north && mCurrentCardDragDistance < 0) {
			mCurrentCardDragDistance = 0;
		}
		if (north && mCurrentCardDragDistance > range) {
			mCurrentCardDragDistance = range;
		}
		if (!north && mCurrentCardDragDistance > 0) {
			mCurrentCardDragDistance = 0;
		}
		if (!north && mCurrentCardDragDistance < -range) {
			mCurrentCardDragDistance = -range;
		}

		float percentage = Math.abs(mCurrentCardDragDistance) / range;
		return north ? percentage : 1f - percentage;
	}

	//The percentage we are to checkout mode...
	private void setCheckoutPercent(float percentage) {
		if (percentage < 0f || percentage > 1f) {
			return;
		}
		if (mOverviewFragment != null) {
			this.mOverviewFragment.setExpandedPercentage(1f - percentage);
		}
		setCheckoutVisibility(percentage);
		setCheckoutContainerPaddingFromPercentage(percentage);
		mLastCheckoutPercentage = percentage;

	}

	@SuppressLint("NewApi")
	private void setCheckoutVisibility(float percentage) {
		if (mCheckoutContainer != null && mSafeToAttach) {
			if (percentage == 0) {
				detachCheckout();
				mCheckoutContainer.setVisibility(View.INVISIBLE);
			}
			else {
				mCheckoutContainer.setVisibility(View.VISIBLE);
				attachCheckout();
			}

			if (AndroidUtils.getSdkVersion() >= 11) {
				this.mCheckoutContainer.setAlpha(percentage);
			}
			else {
				AnimatorProxy.wrap(mCheckoutContainer).setAlpha(percentage);
			}
		}
	}

	private void setCheckoutContainerPaddingFromPercentage(float percentage) {
		float topPadding = mStackedHeight + (mUnstackedHeight - mStackedHeight) * (1f - percentage);
		setCheckoutContainerTopPadding(topPadding);
	}

	private void setCheckoutContainerTopPadding(float topPadding) {
		int pos = Math.round(topPadding);

		if (pos < this.mStackedHeight) {
			pos = this.mStackedHeight;
		}
		if (this.mUnstackedHeight > this.mStackedHeight && pos > this.mUnstackedHeight) {
			pos = this.mUnstackedHeight;
		}
		if (mCheckoutContainer != null) {
			mCheckoutContainer.setPadding(0, pos, 0, 0);
		}
	}

	private void setActionBarOverviewMode() {
		ActionBar actionBar = this.getSupportActionBar();
		actionBar.setCustomView(null);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_HOME_AS_UP
				| ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_USE_LOGO);
		actionBar.setTitle(getString(R.string.overview_btn));

		this.supportInvalidateOptionsMenu();

	}

	private void setActionBarCheckoutMode() {
		FlightTrip trip = Db.getFlightSearch().getFlightTrip(mTripKey);
		String cityName = StrUtils.getWaypointCityOrCode(trip.getLeg(0).getLastWaypoint());
		String yourTripToStr = String.format(getString(R.string.your_trip_to_TEMPLATE), cityName);

		Date depDate = DateTimeUtils
				.getTimeInLocalTimeZone(trip.getLeg(0).getFirstWaypoint().getMostRelevantDateTime());
		Date retDate = DateTimeUtils.getTimeInLocalTimeZone(trip.getLeg(trip.getLegCount() - 1).getLastWaypoint()
				.getMostRelevantDateTime());
		String dateRange = DateUtils.formatDateRange(this, depDate.getTime(), retDate.getTime(),
				DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_WEEKDAY
						| DateUtils.FORMAT_ABBREV_MONTH);

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View customView = inflater.inflate(R.layout.action_bar_flight_results, null);
		TextView titleTextView = Ui.findView(customView, R.id.title_text_view);
		TextView subtitleTextView = Ui.findView(customView, R.id.subtitle_text_view);

		titleTextView.setText(yourTripToStr);
		subtitleTextView.setText(dateRange);

		ActionBar actionBar = this.getSupportActionBar();
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
		MenuInflater inflater = this.getSupportMenuInflater();
		inflater.inflate(R.menu.menu_checkout, menu);
		mCheckoutMenuItem = ActionBarNavUtils.setupActionLayoutButton(this, menu, R.id.menu_checkout);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if (mDisplayMode.compareTo(DisplayMode.CHECKOUT) == 0) {
				onBackPressed();
			}
			else {
				clearCCNumber();

				Intent intent = new Intent(this, FlightSearchResultsActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
				FlightTrip trip = Db.getFlightSearch().getSelectedFlightTrip();
				intent.putExtra(FlightSearchResultsActivity.EXTRA_DESELECT_LEG_ID, trip.getLeg(trip.getLegCount() - 1)
						.getLegId());
				startActivity(intent);
			}
			return true;
		case R.id.menu_checkout:
			if (mOverviewFragment != null) {
				gotoCheckoutMode(true);
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
		mCheckoutFragment.onLoginCompleted();
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
			replacePriceBarWithSlideToCheckout();

			//Scroll to bottom to display legal text
			mContentScrollView.scrollTo(0, this.mCheckoutContainer.getBottom());
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
			replaceSlideToCheckoutWithPriceBar();
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// showRetryErrorDialog

	@Override
	public void onRetryError() {
		mPriceBottomFragment.startCreateTripDownload();
	}

	@Override
	public void onCancelError() {
		finish();
	}

}
