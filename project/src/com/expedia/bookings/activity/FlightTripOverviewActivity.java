package com.expedia.bookings.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
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
import com.expedia.bookings.fragment.SignInFragment.SignInFragmentListener;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.StrUtils;
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

	//We only want to load from disk once: when the activity is first started
	private static boolean sLoaded = false;

	private boolean mTransitionHappening = false;

	private FlightTripOverviewFragment mOverviewFragment;
	private FlightTripPriceFragment mPriceBottomFragment;
	private FlightCheckoutFragment mCheckoutFragment;
	private FlightSlideToPurchaseFragment mSlideToPurchaseFragment;

	private ViewGroup mOverviewContainer;
	private ViewGroup mCheckoutContainer;
	private ViewGroup mPriceContainerBottom;
	private ViewGroup mContentScrollView;
	private View mBackToOverviewArea;

	private MenuItem mCheckoutMenuItem;

	private DisplayMode mDisplayMode = DisplayMode.OVERVIEW;
	private String mTripKey;

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
			}
		}

		//This activity is the first time we see any billingInfo/traveler stuff.
		loadCachedData();

		mContentScrollView = Ui.findView(this, R.id.content_scroll_view);
		mOverviewContainer = Ui.findView(this, R.id.trip_overview_container);
		mCheckoutContainer = Ui.findView(this, R.id.trip_checkout_container);
		mPriceContainerBottom = Ui.findView(this, R.id.trip_price_container_bottom);
		mBackToOverviewArea = Ui.findView(this, R.id.back_to_overview_area);

		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_TAG_MODE)) {
			mDisplayMode = DisplayMode.valueOf(savedInstanceState.getString(STATE_TAG_MODE));
		}

		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_TAG_STACKED_HEIGHT)) {
			mStackedHeight = savedInstanceState.getInt(STATE_TAG_STACKED_HEIGHT);
			mCheckoutContainer.setPadding(0, mStackedHeight, 0, 0);
			setBackToOverviewAreaHeight(mStackedHeight);
		}

		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_TAG_UNSTACKED_HEIGHT)) {
			mUnstackedHeight = savedInstanceState.getInt(STATE_TAG_UNSTACKED_HEIGHT);
			mOverviewContainer.setMinimumHeight(mUnstackedHeight);

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

		//TODO: for now we attach this here, but we should do it after initial create and before any animation
		attachCheckout();
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
		super.onSaveInstanceState(out);
		out.putString(STATE_TAG_MODE, mDisplayMode.name());
		out.putInt(STATE_TAG_STACKED_HEIGHT, mStackedHeight);
		out.putInt(STATE_TAG_UNSTACKED_HEIGHT, mUnstackedHeight);
	}

	private void loadCachedData() {
		if (!sLoaded) {

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
			sLoaded = true;
		}
	}

	public void attachCheckout() {
		FragmentTransaction checkoutTransaction = getSupportFragmentManager().beginTransaction();
		mCheckoutFragment = Ui.findSupportFragment(this, TAG_CHECKOUT_FRAG);
		if (mCheckoutFragment == null) {
			mCheckoutFragment = FlightCheckoutFragment.newInstance();
		}
		if (!mCheckoutFragment.isAdded()) {
			checkoutTransaction.add(R.id.trip_checkout_container, mCheckoutFragment, TAG_CHECKOUT_FRAG);
			checkoutTransaction.commit();
		}
	}

	public void detachCheckout() {
		FragmentTransaction checkoutTransaction = getSupportFragmentManager().beginTransaction();
		mCheckoutFragment = Ui.findSupportFragment(this, TAG_CHECKOUT_FRAG);
		if (mCheckoutFragment != null && mCheckoutFragment.isAdded()) {
			checkoutTransaction.remove(mCheckoutFragment);
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

	private AnimatorListener mDetachCheckoutAnimatorListener = new AnimatorListener() {

		@Override
		public void onAnimationCancel(Animator arg0) {
		}

		@Override
		public void onAnimationEnd(Animator arg0) {
			detachCheckout();
		}

		@Override
		public void onAnimationRepeat(Animator arg0) {
		}

		@Override
		public void onAnimationStart(Animator arg0) {
		}

	};

	public void gotoOverviewMode(boolean animate) {
		if (!mTransitionHappening) {

			mDisplayMode = DisplayMode.OVERVIEW;
			setActionBarOverviewMode();

			int duration = animate ? ANIMATION_DURATION : 1;

			replaceSlideToCheckoutWithPriceBar();

			if (mOverviewFragment != null && mOverviewFragment.isAdded()) {
				mBackToOverviewArea.setOnClickListener(null);

				Animator hideCheckout = getCheckoutHideAnimator(true, false);
				mPriceBottomFragment.showPriceChange();
				AnimatorSet animSet = new AnimatorSet();
				animSet.playTogether(hideCheckout);
				animSet.setDuration(duration);
				animSet.addListener(mTransitionInProgressAnimatorListener);
				animSet.addListener(mDetachCheckoutAnimatorListener);

				mOverviewFragment.unStackCards(animate);
				animSet.start();

			}

			OmnitureTracking.trackPageLoadFlightOverview(this);
		}
	}

	public void gotoCheckoutMode(boolean animate) {
		if (!mTransitionHappening) {

			mDisplayMode = DisplayMode.CHECKOUT;
			setActionBarCheckoutMode();
			attachCheckout();

			int duration = animate ? ANIMATION_DURATION : 1;

			if (mOverviewFragment != null && mOverviewFragment.isAdded()) {
				mStackedHeight = mOverviewFragment.getStackedHeight();
				mCheckoutContainer.setPadding(0, mStackedHeight, 0, 0);
				Animator slideIn = getCheckoutShowAnimator();
				mPriceBottomFragment.hidePriceChange();
				AnimatorSet animSet = new AnimatorSet();
				animSet.playTogether(slideIn);
				animSet.setDuration(duration);
				animSet.addListener(mTransitionInProgressAnimatorListener);

				mOverviewFragment.stackCards(animate);
				animSet.start();

				setBackToOverviewAreaHeight(mStackedHeight);//(mStackedHeight);
				mBackToOverviewArea.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						gotoOverviewMode(true);
					}
				});
			}

			OmnitureTracking.trackPageLoadFlightCheckout(this);
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

		Calendar depDate = trip.getLeg(0).getFirstWaypoint().getMostRelevantDateTime();
		Calendar retDate = trip.getLeg(trip.getLegCount() - 1).getLastWaypoint().getMostRelevantDateTime();
		String dateRange = DateUtils.formatDateRange(this, depDate.getTimeInMillis(),
				retDate.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY
						| DateUtils.FORMAT_ABBREV_WEEKDAY | DateUtils.FORMAT_ABBREV_MONTH);

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

	public Animator getCheckoutShowAnimator() {
		ObjectAnimator mover = ObjectAnimator.ofFloat(mCheckoutContainer, "y", this.mContentScrollView.getBottom(), 0f);
		mover.addListener(new AnimatorListener() {

			@Override
			public void onAnimationCancel(Animator arg0) {
			}

			@Override
			public void onAnimationEnd(Animator arg0) {
				//This will force checkout to validate call the listener methods if need be
				mCheckoutFragment.updateViewVisibilities();
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
			gotoOverviewMode(true);
		}
		else {
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
			mCheckoutMenuItem = menu.findItem(R.id.menu_checkout);

			if (mDisplayMode.compareTo(DisplayMode.CHECKOUT) == 0) {
				displayCheckoutButton(false);
			}
			else {
				displayCheckoutButton(true);
			}

			mCheckoutMenuItem.getActionView().setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mOverviewFragment != null) {
						gotoCheckoutMode(true);
					}
				}
			});
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = this.getSupportMenuInflater();
		inflater.inflate(R.menu.menu_checkout, menu);
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
		if (mDisplayMode.compareTo(DisplayMode.CHECKOUT) == 0) {

			//Bring in the slide to checkout view
			replacePriceBarWithSlideToCheckout();

			//Scroll to bottom to display legal text
			mContentScrollView.scrollTo(0, this.mCheckoutContainer.getBottom());
		}
	}

	@Override
	public void checkoutInformationIsNotValid() {
		if (mDisplayMode.compareTo(DisplayMode.CHECKOUT) == 0) {
			//Bring in the price bar
			replaceSlideToCheckoutWithPriceBar();
		}
	}

}