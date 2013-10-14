package com.expedia.bookings.fragment;

import java.util.ArrayList;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TabletResultsActivity.GlobalResultsState;
import com.expedia.bookings.animation.CubicBezierAnimation;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.graphics.PercentageFadeColorDrawable;
import com.expedia.bookings.interfaces.IAddToTripListener;
import com.expedia.bookings.interfaces.ITabletResultsController;
import com.expedia.bookings.utils.ColumnManager;
import com.expedia.bookings.utils.FragmentAvailabilityUtils;
import com.expedia.bookings.utils.FragmentAvailabilityUtils.IFragmentAvailabilityProvider;
import com.expedia.bookings.utils.ScreenPositionUtils;
import com.expedia.bookings.widget.BlockEventFrameLayout;
import com.expedia.bookings.widget.FixedTranslationFrameLayout;
import com.expedia.bookings.widget.SwipeOutLayout;
import com.expedia.bookings.widget.SwipeOutLayout.ISwipeOutListener;
import com.mobiata.android.Log;
import com.mobiata.android.util.Ui;

/**
 *  TabletResultsTripControllerFragment: designed for tablet results 2013
 *  This controls all the fragments relating to the Trip Overview
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class TabletResultsTripControllerFragment extends Fragment implements ITabletResultsController,
		IAddToTripListener, IFragmentAvailabilityProvider {

	private static final String FTAG_BLURRED_BG = "FTAG_BLURRED_BG";
	private static final String FTAG_YOUR_TRIP_TO = "FTAG_YOUR_TRIP_TO";
	private static final String FTAG_BUCKET_FLIGHT = "FTAG_BUCKET_FLIGHT";
	private static final String FTAG_BUCKET_HOTEL = "FTAG_BUCKET_HOTEL";

	private static final int ANIM_DUR_BUCKET_ITEM_REMOVE = 300;

	private ResultsBlurBackgroundImageFragment mBlurredBackgroundFrag;
	private ResultsTripBucketYourTripToFragment mTripBucketTripToFrag;
	private ResultsTripBucketFlightFragment mTripBucketFlightFrag;
	private ResultsTripBucketHotelFragment mTripBucketHotelFrag;

	private ViewGroup mRootC;
	private FixedTranslationFrameLayout mBlurredBackgroundC;

	//These are the outer containers of the trip bucket items.
	private BlockEventFrameLayout mTripBucketYourTripToC;
	private BlockEventFrameLayout mTripBucketFlightC;
	private BlockEventFrameLayout mTripBucketHotelC;

	//These are the swipeOutLayouts for the trip bucket items.
	private SwipeOutLayout mFlightSwipeOut;
	private SwipeOutLayout mHotelSwipeOut;

	//These are the containers for the actual trip bucket content
	private FrameLayout mTripFlightC;
	private FrameLayout mTripHotelC;

	//Animation containers
	private BlockEventFrameLayout mTripAnimationC;
	private BlockEventFrameLayout mShadeC;

	private GlobalResultsState mGlobalState;
	private int mTotalHeight = 0;
	private ColumnManager mColumnManager = new ColumnManager(3);
	private ITabletResultsController mParentResultsController;

	private boolean mAddingHotelTrip = false;
	private boolean mAddingFlightTrip = false;

	private Object mAddToTripData;
	private Rect mAddToTripOriginCoordinates;
	private int mAddToTripShadeColor;
	private boolean mHasPreppedAddToTripAnimation = false;

	private CubicBezierAnimation mBezierAnimation;
	private float mAddTripEndScaleX = 1f;
	private float mAddTripEndScaleY = 1f;

	private ArrayList<BlockEventFrameLayout> mBucketContainers = new ArrayList<BlockEventFrameLayout>();

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mParentResultsController = Ui.findFragmentListener(this, ITabletResultsController.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_tablet_results_trip, null, false);

		mRootC = Ui.findView(view, R.id.root_layout);
		mBlurredBackgroundC = Ui.findView(view, R.id.column_three_blurred_bg);
		mTripAnimationC = Ui.findView(view, R.id.trip_add_animation_view);
		mShadeC = Ui.findView(view, R.id.column_one_shade);

		mTripBucketYourTripToC = Ui.findView(view, R.id.trip_bucket_your_trip_to);
		mTripBucketFlightC = Ui.findView(view, R.id.trip_bucket_flight_trip);
		mTripBucketHotelC = Ui.findView(view, R.id.trip_bucket_hotel_trip);

		mBucketContainers.add(mTripBucketYourTripToC);
		mBucketContainers.add(mTripBucketFlightC);
		mBucketContainers.add(mTripBucketHotelC);

		mFlightSwipeOut = Ui.findView(view, R.id.trip_bucket_flight_trip_swipeout);
		mFlightSwipeOut.addListener(mFlightSwipeOutListener);
		mHotelSwipeOut = Ui.findView(view, R.id.trip_bucket_hotel_trip_swipeout);
		mHotelSwipeOut.addListener(mHotelSwipeOutListener);

		mTripFlightC = Ui.findView(view, R.id.flight_trip_content);
		mTripHotelC = Ui.findView(view, R.id.hotel_trip_content);

		return view;
	}

	/*
	 * TRIP BUCKET ITEM POSITIONING AND MANAGEMENT
	 */

	private int getNumberOfBucketContainers() {
		if (hasFlightTrip() && hasHotelTrip()) {
			//We have two trips and the "your trip to"
			return 3;
		}
		else {
			//We have the "your trip to" container, and maybe a trip
			return 2;
		}
	}

	private boolean hasFlightTrip() {
		return Db.getFlightSearch() != null && Db.getFlightSearch().getSelectedFlightTrip() != null;
	}

	private boolean hasHotelTrip() {
		return Db.getHotelSearch() != null && Db.getHotelSearch().getSelectedProperty() != null;
	}

	private Animator perpareTripBucketItemRemovalAnimator(final int removalIndex) {
		if (hasFlightTrip() && hasHotelTrip()) {
			final int[] three = getCenterPositionsForTripBucket(3);
			final int[] two = getCenterPositionsForTripBucket(2);
			ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
			animator.setDuration(ANIM_DUR_BUCKET_ITEM_REMOVE);
			animator.addUpdateListener(new AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator arg0) {
					float val = (Float) arg0.getAnimatedValue();
					float viewHalfHeight = mTripBucketYourTripToC.getHeight() / 2f;
					mTripBucketYourTripToC.setTranslationY(three[0] - viewHalfHeight + (val * (two[0] - three[0])));

					if (removalIndex == 1) {
						//moving hotel upwards
						mTripBucketHotelC.setTranslationY(three[2] - viewHalfHeight + (val * (two[1] - three[2])));
					}
					else if (removalIndex == 2) {
						//moving flight one down
						mTripBucketFlightC.setTranslationY(three[1] - viewHalfHeight + (val * (two[1] - three[1])));

					}
				}
			});
			return animator;
		}
		return null;
	}

	private int[] getCenterPositionsForTripBucket(int itemCount) {
		int viewSpace = (int) ((float) mTotalHeight / itemCount);
		int[] retArr = new int[itemCount];
		int currentCenter = (int) (viewSpace / 2f);
		for (int i = 0; i < itemCount; i++) {
			retArr[i] = currentCenter;
			currentCenter += viewSpace;
		}
		return retArr;
	}

	private void positionTripBucketItems(boolean verticalOnly) {
		//We just split the space evenly between views
		int[] centers = getCenterPositionsForTripBucket(getNumberOfBucketContainers());
		int viewHeight = mTotalHeight / 3;
		float halfViewHeight = viewHeight / 2f;

		setViewHeight(mTripBucketYourTripToC, viewHeight);
		setViewHeight(mTripBucketFlightC, viewHeight);
		setViewHeight(mTripBucketHotelC, viewHeight);

		int index = 0;
		mTripBucketYourTripToC.setTranslationY(centers[index] - halfViewHeight);
		index++;
		if (hasFlightTrip() || hasHotelTrip()) {
			if (hasFlightTrip()) {
				mTripBucketFlightC.setTranslationY(centers[index] - halfViewHeight);
				index++;
			}
			if (hasHotelTrip()) {
				mTripBucketHotelC.setTranslationY(centers[index] - halfViewHeight);
				index++;
			}
		}
		else {
			//TODO: ADD AN EMPTY TRIP BUCKET CONTAINER WITH APPROPRIATE MESSAGING
		}

		if (!verticalOnly) {
			//We set the content containers to be the column width
			setHorizontalPos(mTripFlightC, 0, mColumnManager.getColWidth(2) - mFlightSwipeOut.getPaddingLeft()
					- mFlightSwipeOut.getPaddingRight());
			setHorizontalPos(mTripHotelC, 0, mColumnManager.getColWidth(2) - mHotelSwipeOut.getPaddingLeft()
					- mHotelSwipeOut.getPaddingRight());

			int flightSwipeOutDistance = (int) mFlightSwipeOut.getSwipeOutDistance();
			int hotelSwipeOutDistance = (int) mHotelSwipeOut.getSwipeOutDistance();

			setHorizontalPos(mTripBucketFlightC, mColumnManager.getColLeft(2) - flightSwipeOutDistance,
					mColumnManager.getColWidth(2) + flightSwipeOutDistance);
			setHorizontalPos(mTripBucketHotelC,
					mColumnManager.getColLeft(2) - hotelSwipeOutDistance,
					mColumnManager.getColWidth(2) + hotelSwipeOutDistance);
		}
	}

	private void setViewHeight(View view, int height) {
		((FrameLayout.LayoutParams) view.getLayoutParams()).height = height;
		view.setLayoutParams(view.getLayoutParams());
	}

	private void setHorizontalPos(View view, int leftMargin, int width) {
		((FrameLayout.LayoutParams) view.getLayoutParams()).leftMargin = leftMargin;
		((FrameLayout.LayoutParams) view.getLayoutParams()).width = width;
		view.setLayoutParams(view.getLayoutParams());
	}

	private ISwipeOutListener mFlightSwipeOutListener = new ISwipeOutListener() {

		private PercentageFadeColorDrawable mBgDrawable;

		@Override
		public void onSwipeStateChange(int oldState, int newState) {
			if (mBgDrawable == null) {
				mBgDrawable = new PercentageFadeColorDrawable(Color.TRANSPARENT, Color.RED);
				mFlightSwipeOut.getSwipeOutView().setBackground(mBgDrawable);
			}
		}

		@Override
		public void onSwipeUpdate(float percentage) {
			float outPerc = mHotelSwipeOut.getSwipeOutThresholdPercentage();
			if (percentage < outPerc) {
				mBgDrawable.setPercentage(0);
			}
			else {
				float drawablePercentage = .2f + .8f * (percentage - outPerc) / (1f - outPerc);
				mBgDrawable.setPercentage(drawablePercentage);
			}
		}

		@Override
		public void onSwipeAllTheWay() {
			Animator anim = perpareTripBucketItemRemovalAnimator(1);
			if (anim != null) {
				anim.addListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationStart(Animator animator) {
						mTripBucketFlightC.setVisibility(View.INVISIBLE);
					}

					@Override
					public void onAnimationEnd(Animator animator) {
						if (getActivity() != null) {
							finalizeRemoveFlight();
						}
					}
				});
				anim.start();
			}
			else {
				finalizeRemoveFlight();
			}
		}

		private void finalizeRemoveFlight() {
			for (int i = 0; i < Db.getFlightSearch().getSelectedLegs().length; i++) {
				Db.getFlightSearch().setSelectedLeg(i, null);
			}
			positionTripBucketItems(true);
			setVisibilityState(mGlobalState);
		}

	};

	private ISwipeOutListener mHotelSwipeOutListener = new ISwipeOutListener() {

		private PercentageFadeColorDrawable mBgDrawable;

		@Override
		public void onSwipeStateChange(int oldState, int newState) {
			if (mBgDrawable == null) {
				mBgDrawable = new PercentageFadeColorDrawable(Color.TRANSPARENT, Color.RED);
				mHotelSwipeOut.getSwipeOutView().setBackground(mBgDrawable);
			}
		}

		@Override
		public void onSwipeUpdate(float percentage) {
			float outPerc = mHotelSwipeOut.getSwipeOutThresholdPercentage();
			if (percentage < outPerc) {
				mBgDrawable.setPercentage(0);
			}
			else {
				float drawablePercentage = .2f + .8f * (percentage - outPerc) / (1f - outPerc);
				mBgDrawable.setPercentage(drawablePercentage);
			}
		}

		@Override
		public void onSwipeAllTheWay() {
			Animator anim = perpareTripBucketItemRemovalAnimator(2);
			if (anim != null) {
				anim.addListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationStart(Animator animator) {
						mTripBucketHotelC.setVisibility(View.INVISIBLE);
					}

					@Override
					public void onAnimationEnd(Animator animator) {
						if (getActivity() != null) {
							finalizeRemoveHotel();
						}
					}
				});
				anim.start();
			}
			else {
				finalizeRemoveHotel();
			}
		}

		private void finalizeRemoveHotel() {
			Db.getHotelSearch().clearSelectedProperty();
			setVisibilityState(mGlobalState);
			positionTripBucketItems(true);
		}

	};

	private void setFragmentState(GlobalResultsState state) {
		FragmentManager manager = getChildFragmentManager();

		//All of the fragment adds/removes come through this method, and we want to make sure our last call
		//is complete before moving forward, so this is important
		manager.executePendingTransactions();

		//We will be adding all of our add/removes to this transaction
		FragmentTransaction transaction = manager.beginTransaction();

		boolean blurredBackgroundAvailable = false;
		boolean yourTripToAvailable = true;
		boolean bucketFlightAvailable = true;
		boolean bucketHotelAvailable = true;

		mTripBucketTripToFrag = (ResultsTripBucketYourTripToFragment) FragmentAvailabilityUtils
				.setFragmentAvailability(yourTripToAvailable, FTAG_YOUR_TRIP_TO, manager, transaction, this,
						R.id.trip_bucket_your_trip_to, true);

		mTripBucketFlightFrag = (ResultsTripBucketFlightFragment) FragmentAvailabilityUtils.setFragmentAvailability(
				bucketFlightAvailable, FTAG_BUCKET_FLIGHT, manager, transaction, this, R.id.flight_trip_content, true);

		mTripBucketHotelFrag = (ResultsTripBucketHotelFragment) FragmentAvailabilityUtils.setFragmentAvailability(
				bucketHotelAvailable, FTAG_BUCKET_HOTEL, manager, transaction, this, R.id.hotel_trip_content, true);

		//Blurrred Background (for behind trip overview)
		mBlurredBackgroundFrag = (ResultsBlurBackgroundImageFragment) FragmentAvailabilityUtils
				.setFragmentAvailability(
						blurredBackgroundAvailable, FTAG_BLURRED_BG, manager, transaction, this,
						R.id.column_three_blurred_bg,
						false);

		transaction.commit();
	}

	@Override
	public Fragment getExisitingLocalInstanceFromTag(String tag) {
		Fragment frag = null;
		if (tag == FTAG_BLURRED_BG) {
			frag = mBlurredBackgroundFrag;
		}
		else if (tag == FTAG_YOUR_TRIP_TO) {
			frag = mTripBucketTripToFrag;
		}
		else if (tag == FTAG_BUCKET_FLIGHT) {
			frag = mTripBucketFlightFrag;
		}
		else if (tag == FTAG_BUCKET_HOTEL) {
			frag = mTripBucketHotelFrag;
		}
		return frag;
	}

	@Override
	public Fragment getNewFragmentInstanceFromTag(String tag) {
		Fragment frag = null;
		if (tag == FTAG_BLURRED_BG) {
			frag = ResultsBlurBackgroundImageFragment.newInstance();
		}
		else if (tag == FTAG_YOUR_TRIP_TO) {
			frag = ResultsTripBucketYourTripToFragment.newInstance();
		}
		else if (tag == FTAG_BUCKET_FLIGHT) {
			frag = ResultsTripBucketFlightFragment.newInstance();
		}
		else if (tag == FTAG_BUCKET_HOTEL) {
			frag = ResultsTripBucketHotelFragment.newInstance();
		}
		return frag;
	}

	@Override
	public void doFragmentSetup(String tag, Fragment frag) {
		if (tag == FTAG_YOUR_TRIP_TO) {
			((ResultsTripBucketYourTripToFragment) frag).bindToDb();
		}
		else if (tag == FTAG_BUCKET_FLIGHT) {
			((ResultsTripBucketFlightFragment) frag).bindToDb();
		}
		else if (tag == FTAG_BUCKET_HOTEL) {
			((ResultsTripBucketHotelFragment) frag).bindToDb();
		}
	}

	private void setTouchState(GlobalResultsState state) {
		//We never interact with this container
		mBlurredBackgroundC.setBlockNewEventsEnabled(true);
		mShadeC.setBlockNewEventsEnabled(true);

		switch (state) {
		case DEFAULT: {
			mTripAnimationC.setBlockNewEventsEnabled(false);
			break;
		}
		default: {
			mTripAnimationC.setBlockNewEventsEnabled(true);
			break;
		}
		}
	}

	private void setVisibilityState(GlobalResultsState state) {
		switch (state) {
		case DEFAULT: {
			mTripBucketYourTripToC.setVisibility(View.VISIBLE);
			mTripBucketFlightC.setVisibility(hasFlightTrip() ? View.VISIBLE : View.INVISIBLE);
			mTripBucketHotelC.setVisibility(hasHotelTrip() ? View.VISIBLE : View.INVISIBLE);
			mBlurredBackgroundC.setVisibility(View.VISIBLE);
			mTripAnimationC.setVisibility(View.INVISIBLE);
			mShadeC.setVisibility(View.INVISIBLE);
			break;
		}
		default: {
			mTripBucketYourTripToC.setVisibility(View.INVISIBLE);
			mTripBucketFlightC.setVisibility(View.INVISIBLE);
			mTripBucketHotelC.setVisibility(View.INVISIBLE);
			mBlurredBackgroundC.setVisibility(View.INVISIBLE);
			mTripAnimationC.setVisibility(View.INVISIBLE);
			mShadeC.setVisibility(View.INVISIBLE);
			break;
		}
		}
	}

	@Override
	public void setGlobalResultsState(GlobalResultsState state) {
		mGlobalState = state;
		setTouchState(state);
		setVisibilityState(state);
		setFragmentState(state);

		if (state == GlobalResultsState.DEFAULT) {
			mAddingHotelTrip = false;
			mAddingFlightTrip = false;
			mTripAnimationC.removeAllViews();
		}
	}

	@Override
	public void setAnimatingTowardsVisibility(GlobalResultsState state) {
		if (state == GlobalResultsState.DEFAULT) {
			mBlurredBackgroundC.setVisibility(View.VISIBLE);
			if (mAddingHotelTrip) {
				mTripAnimationC.setVisibility(View.VISIBLE);
			}
		}
	}

	@Override
	public void setHardwareLayerForTransition(int layerType, GlobalResultsState stateOne, GlobalResultsState stateTwo) {
		if ((stateOne == GlobalResultsState.DEFAULT || stateOne == GlobalResultsState.HOTELS)
				&& (stateTwo == GlobalResultsState.DEFAULT || stateTwo == GlobalResultsState.HOTELS)) {
			//Default -> Hotels or Hotels -> Default transition

			mBlurredBackgroundC.setLayerType(layerType, null);
			mTripAnimationC.setLayerType(layerType, null);
			mShadeC.setLayerType(layerType, null);

			for (View container : mBucketContainers) {
				container.setLayerType(layerType, null);
			}

		}

		if ((stateOne == GlobalResultsState.DEFAULT || stateOne == GlobalResultsState.FLIGHTS)
				&& (stateTwo == GlobalResultsState.DEFAULT || stateTwo == GlobalResultsState.FLIGHTS)) {
			//Default -> Flights or Flights -> Default transition

			mBlurredBackgroundC.setLayerType(layerType, null);
			mTripAnimationC.setLayerType(layerType, null);

			for (View container : mBucketContainers) {
				container.setLayerType(layerType, null);
			}
		}
	}

	@Override
	public void blockAllNewTouches(View requester) {
		if (mTripAnimationC != requester) {
			mTripAnimationC.setBlockNewEventsEnabled(true);
		}
	}

	private void animateToPercentage(float percentage, boolean addingTrip) {
		int colTwoDist = mColumnManager.getTotalWidth() - mColumnManager.getColLeft(2);
		float translationX = colTwoDist * (1f - percentage);
		for (View container : mBucketContainers) {
			container.setTranslationX(translationX);
		}
		mBlurredBackgroundC.setTranslationX(translationX);
	}

	private void addTripPercentage(float percentage) {

		if (mAddingHotelTrip || mAddingFlightTrip) {

			//Translate
			mTripAnimationC.setTranslationX(mBezierAnimation.getXInterpolator().interpolate(percentage));
			mTripAnimationC.setTranslationY(mBezierAnimation.getYInterpolator().interpolate(percentage));

			//Scale
			if (mAddTripEndScaleX > 1f) {
				mTripAnimationC.setScaleX(1f + percentage * (mAddTripEndScaleX - 1f));
			}
			else {
				mTripAnimationC.setScaleX(1f - (1f - mAddTripEndScaleX) * percentage);
			}
			if (mAddTripEndScaleY > 1f) {
				mTripAnimationC.setScaleY(1f + percentage * (mAddTripEndScaleY - 1f));
			}
			else {
				mTripAnimationC.setScaleY(1f - (1f - mAddTripEndScaleY) * percentage);
			}

			//Alpha
			mShadeC.setAlpha(1f - percentage);

			if (percentage == 1f && mGlobalState == GlobalResultsState.DEFAULT) {
				mAddingHotelTrip = false;
				mAddingFlightTrip = false;
			}
		}
	}

	@Override
	public void animateToFlightsPercentage(float percentage) {
		animateToPercentage(percentage, mAddingFlightTrip);
		addTripPercentage(percentage);
	}

	@Override
	public void animateToHotelsPercentage(float percentage) {
		animateToPercentage(percentage, mAddingHotelTrip);
		addTripPercentage(percentage);
	}

	@Override
	public void updateContentSize(int totalWidth, int totalHeight) {
		mTotalHeight = totalHeight;
		mColumnManager.setTotalWidth(totalWidth);

		mColumnManager.setContainerToColumn(mBlurredBackgroundC, 2);
		mColumnManager.setContainerToColumn(mTripAnimationC, 2);

		mColumnManager.setContainerToColumn(mTripBucketYourTripToC, 2);

		positionTripBucketItems(false);
	}

	@Override
	public boolean handleBackPressed() {
		return false;
	}

	/**
	 * IAddToTripListener
	 */

	@Override
	public void beginAddToTrip(Object data, Rect globalCoordinates, int shadeColor) {

		mAddToTripData = data;
		mAddToTripOriginCoordinates = globalCoordinates;
		mAddToTripShadeColor = shadeColor;

		//We position things
		positionTripBucketItems(true);

		//Ideally we want to put things in place and have them ready when the trip handoff happens.
		prepareAddToTripAnimation();
	}

	@Override
	public void performTripHandoff() {
		//Do another prepare pass, so if layout has changed, our animation isnt wonky
		prepareAddToTripAnimation();

		//clean up
		mHasPreppedAddToTripAnimation = false;
		mAddToTripData = null;
		mAddToTripOriginCoordinates = null;
		mAddToTripShadeColor = Color.TRANSPARENT;

		//Set animation starting state
		mShadeC.setVisibility(View.VISIBLE);
		mTripAnimationC.setVisibility(View.VISIBLE);
		mTripAnimationC.setAlpha(1f);
		mShadeC.setAlpha(1f);
	}

	private void prepareAddToTripAnimation() {
		//TODO: WE ARE NOT GOING TO ALWAYS BE PASSING ARBITRARY STRINGS AROUND
		boolean isFlights = (mAddToTripData instanceof String && ((String) mAddToTripData)
				.equalsIgnoreCase("FLIGHTS"));

		Rect globalDestRect = ScreenPositionUtils.getGlobalScreenPosition(isFlights ? mTripFlightC : mTripHotelC, true,
				false);
		Rect localDestRect = ScreenPositionUtils.translateGlobalPositionToLocalPosition(globalDestRect, mRootC);

		LayoutParams params = (LayoutParams) mTripAnimationC.getLayoutParams();
		if (!mHasPreppedAddToTripAnimation || params.leftMargin != localDestRect.left
				|| params.topMargin != localDestRect.top) {

			Rect origRect = ScreenPositionUtils.translateGlobalPositionToLocalPosition(mAddToTripOriginCoordinates,
					mRootC);

			//Do calculations
			int originWidth = origRect.right - origRect.left;
			int originHeight = origRect.bottom - origRect.top;
			int destWidth = localDestRect.right - localDestRect.left;
			int destHeight = localDestRect.bottom - localDestRect.top;

			//Position the view where it will be ending up, but sized as it will be starting...
			params.leftMargin = localDestRect.left;
			params.topMargin = localDestRect.top;
			params.width = originWidth;
			params.height = originHeight;
			mTripAnimationC.setLayoutParams(params);

			//TODO: We should be flying around something without fake in the def name
			mTripAnimationC.addView(getFakeAnimationView(), LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

			//Translation. We setup the translation animation to start at deltaX, deltaY and animate
			// to it's final resting place of (0,0), i.e. no translation.
			int deltaX = origRect.left - localDestRect.left;
			int deltaY = origRect.top - localDestRect.top;
			mBezierAnimation = CubicBezierAnimation.newOutsideInAnimation(deltaX, deltaY, 0, 0);

			mAddTripEndScaleX = (float) destWidth / originWidth;
			mAddTripEndScaleY = (float) destHeight / originHeight;

			//Set initial values
			mTripAnimationC.setAlpha(0f);
			mTripAnimationC.setVisibility(View.VISIBLE);
			mTripAnimationC.setPivotX(0);
			mTripAnimationC.setPivotY(0);
			mTripAnimationC.setScaleX(1f);
			mTripAnimationC.setScaleY(1f);
			mTripAnimationC.setTranslationX(deltaX);
			mTripAnimationC.setTranslationY(deltaY);

			mShadeC.setBackgroundColor(mAddToTripShadeColor);
			if (isFlights) {
				mAddingFlightTrip = true;
			}
			else {
				mAddingHotelTrip = true;
			}

			mHasPreppedAddToTripAnimation = true;
		}
	}

	private View getFakeAnimationView() {
		TextView tv = new TextView(getActivity());
		tv.setText("Animation View");
		tv.setBackgroundColor(Color.YELLOW);
		return tv;
	}

}
