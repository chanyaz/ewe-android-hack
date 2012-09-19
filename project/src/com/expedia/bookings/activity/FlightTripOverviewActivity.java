package com.expedia.bookings.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.fragment.FlightCheckoutFragment;
import com.expedia.bookings.fragment.FlightTripOverviewFragment;
import com.expedia.bookings.fragment.FlightTripOverviewFragment.DisplayMode;
import com.expedia.bookings.fragment.FlightTripPriceFragment;
import com.expedia.bookings.fragment.SignInFragment.SignInFragmentListener;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;

public class FlightTripOverviewActivity extends SherlockFragmentActivity implements SignInFragmentListener {

	public static final String TAG_OVERVIEW_FRAG = "TAG_OVERVIEW_FRAG";
	public static final String TAG_CHECKOUT_FRAG = "TAG_CHECKOUT_FRAG";
	public static final String TAG_PRICE_BAR_FRAG = "TAG_PRICE_BAR_FRAG";
	public static final String TAG_PRICE_BAR_BOTTOM_FRAG = "TAG_PRICE_BAR_BOTTOM_FRAG";

	public static final String STATE_TAG_MODE = "STATE_TAG_MODE";
	public static final String STATE_TAG_STACKED_HEIGHT = "STATE_TAG_STACKED_HEIGHT";
	public static final String STATE_TAG_UNSTACKED_HEIGHT = "STATE_TAG_UNSTACKED_HEIGHT";
	
	public static final int ANIMATION_DURATION = 1000;

	private FlightTripOverviewFragment mOverviewFragment;
	private FlightTripPriceFragment mPriceFragment;
	private FlightCheckoutFragment mCheckoutFragment;

	private ViewGroup mOverviewContainer;
	private ViewGroup mCheckoutContainer;
	private ViewGroup mPriceContainer;
	private ViewGroup mPriceContainerBottom;
	private ViewGroup mContentScrollView;

	private MenuItem mCheckoutMenuItem;

	private DisplayMode mDisplayMode = DisplayMode.OVERVIEW;

	private int mStackedHeight = 0;
	private int mUnstackedHeight = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_flight_overview_and_checkout);

		// Recover data if it was flushed from memory
		if (Db.getFlightSearch().getSearchResponse() == null) {
			if (!Db.loadCachedFlightData(this)) {
				NavUtils.onDataMissing(this);
				return;
			}
		}

		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_TAG_MODE)) {
			mDisplayMode = DisplayMode.valueOf(savedInstanceState.getString(STATE_TAG_MODE));
		}

		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_TAG_STACKED_HEIGHT)) {
			mStackedHeight = savedInstanceState.getInt(STATE_TAG_STACKED_HEIGHT);
		}

		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_TAG_UNSTACKED_HEIGHT)) {
			mUnstackedHeight = savedInstanceState.getInt(STATE_TAG_UNSTACKED_HEIGHT);
		}

		mContentScrollView = Ui.findView(this, R.id.content_scroll_view);
		mOverviewContainer = Ui.findView(this, R.id.trip_overview_container);
		mCheckoutContainer = Ui.findView(this, R.id.trip_checkout_container);
		mPriceContainer = Ui.findView(this, R.id.trip_price_container);
		mPriceContainerBottom = Ui.findView(this, R.id.trip_price_container_bottom);

		mCheckoutContainer.setPadding(0, mStackedHeight, 0, 0);
		mOverviewContainer.setMinimumHeight(mUnstackedHeight);

		String tripKey = Db.getFlightSearch().getSelectedFlightTrip().getProductKey();

		FragmentTransaction overviewTransaction = getSupportFragmentManager().beginTransaction();
		mOverviewFragment = Ui.findSupportFragment(this, TAG_OVERVIEW_FRAG);
		if (mOverviewFragment == null) {
			mOverviewFragment = FlightTripOverviewFragment.newInstance(tripKey, mDisplayMode);
		}
		if (!mOverviewFragment.isAdded()) {
			overviewTransaction.add(R.id.trip_overview_container, mOverviewFragment, TAG_OVERVIEW_FRAG);
		}
		overviewTransaction.commit();

		FragmentTransaction pricebarTransaction = getSupportFragmentManager().beginTransaction();
		mPriceFragment = Ui.findSupportFragment(this, TAG_PRICE_BAR_FRAG);
		if (mPriceFragment == null) {
			mPriceFragment = FlightTripPriceFragment.newInstance();
		}
		if (!mPriceFragment.isAdded()) {
			pricebarTransaction.add(R.id.trip_price_container, mPriceFragment, TAG_PRICE_BAR_FRAG);
		}
		pricebarTransaction.commit();

		FragmentTransaction bottomBarTrans = getSupportFragmentManager().beginTransaction();
		FlightTripPriceFragment priceBottom = Ui.findSupportFragment(this, TAG_PRICE_BAR_BOTTOM_FRAG);
		if (priceBottom == null) {
			priceBottom = FlightTripPriceFragment.newInstance();
		}
		if (!priceBottom.isAdded()) {
			bottomBarTrans.add(R.id.trip_price_container_bottom, priceBottom, TAG_PRICE_BAR_BOTTOM_FRAG);
		}
		bottomBarTrans.commit();

		//TODO: for now we attach this here, but we should do it after initial create and before any animation
		FragmentTransaction checkoutTransaction = getSupportFragmentManager().beginTransaction();
		mCheckoutFragment = Ui.findSupportFragment(this, TAG_CHECKOUT_FRAG);
		if (mCheckoutFragment == null) {
			mCheckoutFragment = FlightCheckoutFragment.newInstance();
		}
		if (!mCheckoutFragment.isAdded()) {
			checkoutTransaction.add(R.id.trip_checkout_container, mCheckoutFragment, TAG_CHECKOUT_FRAG);
		}
		if (mDisplayMode.compareTo(DisplayMode.OVERVIEW) == 0 ) {
			checkoutTransaction.hide(mCheckoutFragment);
		}else{
			checkoutTransaction.show(mCheckoutFragment);
		}
		checkoutTransaction.commit();

		//Important
		setModeProperties(true);

		FlightTrip trip = Db.getFlightSearch().getFlightTrip(tripKey);
		String cityName = trip.getLeg(0).getLastWaypoint().getAirport().mCity;
		String yourTripToStr = String.format(getString(R.string.your_trip_to_TEMPLATE), cityName);

		//Actionbar
		ActionBar actionBar = this.getSupportActionBar();
		actionBar.setTitle(yourTripToStr);

	}

	@Override
	public void onSaveInstanceState(Bundle out) {
		out.putString(STATE_TAG_MODE, mDisplayMode.name());
		out.putInt(STATE_TAG_STACKED_HEIGHT, mStackedHeight);
		out.putInt(STATE_TAG_UNSTACKED_HEIGHT, mUnstackedHeight);
		super.onSaveInstanceState(out);
	}

	private void computeHeights() {
		if (mOverviewFragment.isAdded()) {
			mStackedHeight = mOverviewFragment.getStackedHeight();
			mUnstackedHeight = mOverviewFragment.getUnstackedHeight();
			mOverviewContainer.setMinimumHeight(mUnstackedHeight);
			mCheckoutContainer.setPadding(0, mStackedHeight, 0, 0);
		}
	}

	private void showCheckoutFragment() {
		FragmentTransaction checkoutTransaction = getSupportFragmentManager().beginTransaction();
		checkoutTransaction.setCustomAnimations(R.anim.fragment_checkout_slide_up, R.anim.fragment_checkout_slide_down,
				R.anim.fragment_checkout_slide_up, R.anim.fragment_checkout_slide_down);
		mCheckoutFragment = Ui.findSupportFragment(this, TAG_CHECKOUT_FRAG);
		if (mCheckoutFragment == null) {
			mCheckoutFragment = FlightCheckoutFragment.newInstance();
		}
		if (!mCheckoutFragment.isVisible()) {
			checkoutTransaction.show(mCheckoutFragment);
		}
		checkoutTransaction.commit();
		computeHeights();
	}

	private void hideCheckoutFragment() {
		FragmentTransaction checkoutTransaction = getSupportFragmentManager().beginTransaction();
		checkoutTransaction.setCustomAnimations(R.anim.fragment_checkout_slide_down,
				R.anim.fragment_checkout_slide_down,
				R.anim.fragment_checkout_slide_down, R.anim.fragment_checkout_slide_down);
		mCheckoutFragment = Ui.findSupportFragment(this, TAG_CHECKOUT_FRAG);
		if (mCheckoutFragment == null) {
			mCheckoutFragment = FlightCheckoutFragment.newInstance();
		}
		if (mCheckoutFragment.isVisible()) {
			checkoutTransaction.hide(mCheckoutFragment);
		}
		checkoutTransaction.commit();
		computeHeights();
	}

	private void setPriceToBelowContent() {
		int futureTop = mPriceContainer.getTop();
		int transitionDistance = futureTop - mPriceContainerBottom.getTop();
		Log.i("futureTop:" + futureTop + " transitionDistance:" + transitionDistance);
		TranslateAnimation priceanimation = new TranslateAnimation(0, 0, 0, transitionDistance);
		priceanimation.setDuration(ANIMATION_DURATION);
		priceanimation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationEnd(Animation animation) {
				mPriceContainer.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationStart(Animation animation) {
				mPriceContainerBottom.setVisibility(View.GONE);
			}

		});
		mPriceContainerBottom.startAnimation(priceanimation);
	}

	private void animatePriceToBottom() {
		int futureTop = mContentScrollView.getHeight() - mPriceContainer.getHeight();
		int transitionDistance = futureTop - mPriceContainer.getTop();
		TranslateAnimation priceanimation = new TranslateAnimation(0, 0, 0, transitionDistance);
		priceanimation.setDuration(ANIMATION_DURATION);
		priceanimation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationEnd(Animation animation) {
				mPriceContainerBottom.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationStart(Animation animation) {
				mPriceContainer.setVisibility(View.GONE);
			}

		});
		mPriceContainer.startAnimation(priceanimation);
	}

	private void setModeProperties(boolean setPriceVisibilities) {
		if (mDisplayMode.compareTo(DisplayMode.OVERVIEW) == 0) {

			if (setPriceVisibilities) {
				mPriceContainer.setVisibility(View.VISIBLE);
				mPriceContainerBottom.setVisibility(View.GONE);
			}
			if (mCheckoutMenuItem != null) {
				mCheckoutMenuItem.setVisible(true);
			}
			if (mOverviewFragment != null) {
				mOverviewFragment.setCardOnClickListeners(null);
			}
		}
		else if (mDisplayMode.compareTo(DisplayMode.CHECKOUT) == 0) {
			if (setPriceVisibilities) {
				mPriceContainer.setVisibility(View.GONE);
				mPriceContainerBottom.setVisibility(View.VISIBLE);
			}
			if (mCheckoutMenuItem != null) {
				mCheckoutMenuItem.setVisible(false);
			}
			if (mOverviewFragment != null) {
				mOverviewFragment.setCardOnClickListeners(new OnClickListener() {
					@Override
					public void onClick(View v) {
						backToOverviewMode();
					}
				});
			}
		}
		
	}

	public void forwardToCheckoutMode() {
		mDisplayMode = DisplayMode.CHECKOUT;

		animatePriceToBottom();
		showCheckoutFragment();
		mOverviewFragment.stackCards(true);

		setModeProperties(false);

	}

	public void backToOverviewMode() {
		mDisplayMode = DisplayMode.OVERVIEW;
		setModeProperties(false);
		
		hideCheckoutFragment();
		mOverviewFragment.unStackCards(true);
		setPriceToBelowContent();
	}

	@Override
	public void onBackPressed() {
		if (mDisplayMode.compareTo(DisplayMode.CHECKOUT) == 0) {
			backToOverviewMode();
		}
		else {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = this.getSupportMenuInflater();
		inflater.inflate(R.menu.menu_checkout, menu);
		mCheckoutMenuItem = menu.findItem(R.id.menu_checkout);
		mCheckoutMenuItem.getActionView().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mOverviewFragment != null) {
					forwardToCheckoutMode();
				}
			}
		});
		if (mDisplayMode.compareTo(DisplayMode.CHECKOUT) == 0) {
			mCheckoutMenuItem.setVisible(false);
		}
		else {
			mCheckoutMenuItem.setVisible(true);
		}
		return true;
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
}