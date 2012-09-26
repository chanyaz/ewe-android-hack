package com.expedia.bookings.activity;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.fragment.FlightCheckoutFragment;
import com.expedia.bookings.fragment.FlightCheckoutFragment.CheckoutInformationListener;
import com.expedia.bookings.fragment.FlightSlideToPurchaseFragment;
import com.expedia.bookings.fragment.FlightTripOverviewFragment;
import com.expedia.bookings.fragment.FlightTripOverviewFragment.DisplayMode;
import com.expedia.bookings.fragment.FlightTripPriceFragment;
import com.expedia.bookings.fragment.SignInFragment.SignInFragmentListener;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.Ui;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

public class FlightTripOverviewActivity extends SherlockFragmentActivity implements SignInFragmentListener,
		CheckoutInformationListener {

	public static final String TAG_OVERVIEW_FRAG = "TAG_OVERVIEW_FRAG";
	public static final String TAG_CHECKOUT_FRAG = "TAG_CHECKOUT_FRAG";
	public static final String TAG_PRICE_BAR_BOTTOM_FRAG = "TAG_PRICE_BAR_BOTTOM_FRAG";
	public static final String TAG_SLIDE_TO_PURCHASE_FRAG = "TAG_SLIDE_TO_PURCHASE_FRAG";

	public static final String STATE_TAG_MODE = "STATE_TAG_MODE";
	public static final String STATE_TAG_STACKED_HEIGHT = "STATE_TAG_STACKED_HEIGHT";
	public static final String STATE_TAG_UNSTACKED_HEIGHT = "STATE_TAG_UNSTACKED_HEIGHT";

	public static final int ANIMATION_DURATION = 1000;

	private boolean mTransitionHappening = false;

	private FlightTripOverviewFragment mOverviewFragment;
	private FlightTripPriceFragment mPriceBottomFragment;
	private FlightCheckoutFragment mCheckoutFragment;
	private FlightSlideToPurchaseFragment mSlideToPurchaseFragment;

	private ViewGroup mOverviewContainer;
	private ViewGroup mCheckoutContainer;
	private ViewGroup mPriceContainerBottom;
	private ViewGroup mContentScrollView;

	private MenuItem mCheckoutMenuItem;

	private DisplayMode mDisplayMode = DisplayMode.OVERVIEW;
	private boolean mShowingSlideToPurchase = false; //Slide to purchase is showing
	private boolean mOverviewIsHidden = false; //No flight cards are visible

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

		mContentScrollView = Ui.findView(this, R.id.content_scroll_view);
		mOverviewContainer = Ui.findView(this, R.id.trip_overview_container);
		mCheckoutContainer = Ui.findView(this, R.id.trip_checkout_container);
		mPriceContainerBottom = Ui.findView(this, R.id.trip_price_container_bottom);

		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_TAG_MODE)) {
			mDisplayMode = DisplayMode.valueOf(savedInstanceState.getString(STATE_TAG_MODE));
		}

		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_TAG_STACKED_HEIGHT)) {
			mStackedHeight = savedInstanceState.getInt(STATE_TAG_STACKED_HEIGHT);
			mCheckoutContainer.setPadding(0, mStackedHeight, 0, 0);
		}

		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_TAG_UNSTACKED_HEIGHT)) {
			mUnstackedHeight = savedInstanceState.getInt(STATE_TAG_UNSTACKED_HEIGHT);
			mOverviewContainer.setMinimumHeight(mUnstackedHeight);
		}

		String tripKey = Db.getFlightSearch().getSelectedFlightTrip().getProductKey();

		FragmentTransaction overviewTransaction = getSupportFragmentManager().beginTransaction();
		mOverviewFragment = Ui.findSupportFragment(this, TAG_OVERVIEW_FRAG);
		if (mOverviewFragment == null) {
			mOverviewFragment = FlightTripOverviewFragment.newInstance(tripKey, mDisplayMode);
		}
		overviewTransaction.add(R.id.trip_overview_container, mOverviewFragment, TAG_OVERVIEW_FRAG);
		overviewTransaction.commit();

		FragmentTransaction bottomBarTrans = getSupportFragmentManager().beginTransaction();
		mPriceBottomFragment = Ui.findSupportFragment(this, TAG_PRICE_BAR_BOTTOM_FRAG);
		if (mPriceBottomFragment == null) {
			mPriceBottomFragment = FlightTripPriceFragment.newInstance();
		}
		bottomBarTrans.add(R.id.trip_price_container_bottom, mPriceBottomFragment, TAG_PRICE_BAR_BOTTOM_FRAG);
		bottomBarTrans.commit();

		//TODO: for now we attach this here, but we should do it after initial create and before any animation
		FragmentTransaction checkoutTransaction = getSupportFragmentManager().beginTransaction();
		mCheckoutFragment = Ui.findSupportFragment(this, TAG_CHECKOUT_FRAG);
		if (mCheckoutFragment == null) {
			mCheckoutFragment = FlightCheckoutFragment.newInstance();
		}
		checkoutTransaction.add(R.id.trip_checkout_container, mCheckoutFragment, TAG_CHECKOUT_FRAG);
		checkoutTransaction.commit();

		FlightTrip trip = Db.getFlightSearch().getFlightTrip(tripKey);
		String cityName = trip.getLeg(0).getLastWaypoint().getAirport().mCity;
		String yourTripToStr = String.format(getString(R.string.your_trip_to_TEMPLATE), cityName);

		//Actionbar
		ActionBar actionBar = this.getSupportActionBar();
		actionBar.setTitle(yourTripToStr);
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mDisplayMode.compareTo(DisplayMode.CHECKOUT) == 0) {
			gotoCheckoutMode(false);
		}
		else {
			gotoOverviewMode(false);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle out) {
		out.putString(STATE_TAG_MODE, mDisplayMode.name());
		out.putInt(STATE_TAG_STACKED_HEIGHT, mStackedHeight);
		out.putInt(STATE_TAG_UNSTACKED_HEIGHT, mUnstackedHeight);
		super.onSaveInstanceState(out);
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

	private AnimatorListener mTransitionInProgressAnimatorListener = new AnimatorListener() {

		@Override
		public void onAnimationCancel(Animator arg0) {
			mTransitionHappening = false;
		}

		@Override
		public void onAnimationEnd(Animator arg0) {
			mTransitionHappening = false;
		}

		@Override
		public void onAnimationRepeat(Animator arg0) {
		}

		@Override
		public void onAnimationStart(Animator arg0) {
			mTransitionHappening = true;

		}

	};

	public void gotoOverviewMode(boolean animate) {
		if (!mTransitionHappening) {
			int duration = animate ? ANIMATION_DURATION : 1;

			if (mOverviewFragment != null && mOverviewFragment.isAdded()) {
				Animator hideCheckout = getCheckoutHideAnimator(true, false);
				mPriceBottomFragment.showPriceChange();
				AnimatorSet animSet = new AnimatorSet();
				animSet.playTogether(hideCheckout);
				animSet.setDuration(duration);
				animSet.addListener(mTransitionInProgressAnimatorListener);

				mOverviewFragment.unStackCards(animate);
				animSet.start();

				mOverviewFragment.setCardOnClickListeners(null);
			}
			if (mCheckoutMenuItem != null) {
				mCheckoutMenuItem.setVisible(true);
			}
			mDisplayMode = DisplayMode.OVERVIEW;
		}
	}

	public void gotoCheckoutMode(boolean animate) {
		if (!mTransitionHappening) {
			int duration = animate ? ANIMATION_DURATION : 1;

			if (mOverviewFragment != null && mOverviewFragment.isAdded()) {
				mCheckoutContainer.setPadding(0, mOverviewFragment.getStackedHeight(), 0, 0);
				Animator slideIn = getCheckoutShowAnimator();
				mPriceBottomFragment.hidePriceChange();
				AnimatorSet animSet = new AnimatorSet();
				animSet.playTogether(slideIn);
				animSet.setDuration(duration);
				animSet.addListener(mTransitionInProgressAnimatorListener);

				mOverviewFragment.stackCards(animate);
				animSet.start();

				mOverviewFragment.setCardOnClickListeners(new OnClickListener() {
					@Override
					public void onClick(View v) {
						gotoOverviewMode(true);
					}
				});
			}

			if (mCheckoutMenuItem != null) {
				mCheckoutMenuItem.setVisible(false);
			}
			mDisplayMode = DisplayMode.CHECKOUT;
		}
	}

	public Animator getCheckoutShowAnimator() {
		ObjectAnimator mover = ObjectAnimator.ofFloat(mCheckoutContainer, "y", this.mContentScrollView.getBottom(), 0f);
		mover.addListener(new AnimatorListener() {

			@Override
			public void onAnimationCancel(Animator arg0) {
			}

			@Override
			public void onAnimationEnd(Animator arg0) {
			}

			@Override
			public void onAnimationRepeat(Animator arg0) {
			}

			@Override
			public void onAnimationStart(Animator arg0) {
				mCheckoutContainer.setVisibility(View.VISIBLE);
				ObjectAnimator restoreAlpha = ObjectAnimator.ofFloat(mCheckoutContainer, "alpha", 0f, 1f);
				restoreAlpha.setDuration(1);
				restoreAlpha.start();
			}
		});
		return mover;
	}

	public Animator getCheckoutHideAnimator(boolean slide, boolean fade) {
		ArrayList<Animator> animators = new ArrayList<Animator>();

		if (slide) {
			ObjectAnimator mover = ObjectAnimator.ofFloat(mCheckoutContainer, "y", 0f,
					this.mContentScrollView.getBottom());
			mover.addListener(new AnimatorListener() {

				@Override
				public void onAnimationCancel(Animator arg0) {
				}

				@Override
				public void onAnimationEnd(Animator arg0) {
					mCheckoutContainer.setVisibility(View.GONE);
				}

				@Override
				public void onAnimationRepeat(Animator arg0) {
				}

				@Override
				public void onAnimationStart(Animator arg0) {
				}
			});
			animators.add(mover);
		}

		if (fade) {
			ObjectAnimator fadeOut = ObjectAnimator.ofFloat(mCheckoutContainer, "alpha", 1f, 0f);
			fadeOut.addListener(new AnimatorListener() {
				@Override
				public void onAnimationCancel(Animator arg0) {
				}

				@Override
				public void onAnimationEnd(Animator arg0) {
					mCheckoutContainer.setVisibility(View.GONE);
				}

				@Override
				public void onAnimationRepeat(Animator arg0) {
				}

				@Override
				public void onAnimationStart(Animator arg0) {
				}
			});
			animators.add(fadeOut);
		}

		AnimatorSet set = new AnimatorSet();
		set.playTogether(animators);
		return set;
	}

	
	
	@Override
	public void onBackPressed() {
		if (mDisplayMode.compareTo(DisplayMode.CHECKOUT) == 0) {
			if(mOverviewIsHidden){
				bringBackOverview();
			}else{
				gotoOverviewMode(true);
			}
		}
		else {
			super.onBackPressed();
		}
	}
	
	public void hideOverviewEntirely(){
		mOverviewIsHidden = true;
		this.mOverviewContainer.setVisibility(View.GONE);
		mCheckoutContainer.setPadding(0, 0, 0, 0);
	}
	
	public void bringBackOverview(){
		mOverviewIsHidden = false;
		mCheckoutContainer.setPadding(0, mOverviewFragment.getStackedHeight(), 0, 0);
		this.mOverviewContainer.setVisibility(View.VISIBLE);
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
					gotoCheckoutMode(true);
				}
			}
		});
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

	//Checkout listener	
	@Override
	public void checkoutInformationIsValid() {
		mShowingSlideToPurchase = true;
		hideOverviewEntirely();
		replacePriceBarWithSlideToCheckout();
	}

	@Override
	public void checkoutInformationIsNotValid() {
		
		replaceSlideToCheckoutWithPriceBar();
	}
}