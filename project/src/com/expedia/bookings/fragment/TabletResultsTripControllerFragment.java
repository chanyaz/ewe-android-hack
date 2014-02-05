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

import com.expedia.bookings.R;
import com.expedia.bookings.animation.CubicBezierAnimation;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.enums.ResultsState;
import com.expedia.bookings.enums.TripBucketItemState;
import com.expedia.bookings.graphics.PercentageFadeColorDrawable;
import com.expedia.bookings.interfaces.IAddToTripListener;
import com.expedia.bookings.interfaces.IBackManageable;
import com.expedia.bookings.interfaces.helpers.BackManager;
import com.expedia.bookings.interfaces.helpers.MeasurementHelper;
import com.expedia.bookings.interfaces.helpers.StateListenerHelper;
import com.expedia.bookings.section.FlightLegSummarySectionTablet;
import com.expedia.bookings.utils.FragmentAvailabilityUtils;
import com.expedia.bookings.utils.FragmentAvailabilityUtils.IFragmentAvailabilityProvider;
import com.expedia.bookings.utils.GridManager;
import com.expedia.bookings.utils.ScreenPositionUtils;
import com.expedia.bookings.widget.FixedTranslationFrameLayout;
import com.expedia.bookings.widget.FrameLayoutTouchController;
import com.expedia.bookings.widget.SwipeOutLayout;
import com.expedia.bookings.widget.SwipeOutLayout.ISwipeOutListener;
import com.expedia.bookings.widget.TextView;
import com.mobiata.android.util.Ui;

/**
 * TabletResultsTripControllerFragment: designed for tablet results 2013
 * This controls all the fragments relating to the Trip Overview
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class TabletResultsTripControllerFragment extends Fragment implements
	IAddToTripListener, IFragmentAvailabilityProvider, IBackManageable {

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
	private FrameLayoutTouchController mTripBucketYourTripToC;
	private FrameLayoutTouchController mTripBucketFlightC;
	private FrameLayoutTouchController mTripBucketHotelC;

	//These are the swipeOutLayouts for the trip bucket items.
	private SwipeOutLayout mFlightSwipeOut;
	private SwipeOutLayout mHotelSwipeOut;

	//These are the containers for the actual trip bucket content
	private FrameLayout mTripFlightC;
	private FrameLayout mTripHotelC;

	//Animation containers
	private FrameLayoutTouchController mTripAnimationC;
	private FrameLayoutTouchController mShadeC;

	private ResultsState mGlobalState;
	private GridManager mGrid = new GridManager();

	private boolean mAddingHotelTrip = false;
	private boolean mAddingFlightTrip = false;

	private Object mAddToTripData;
	private Rect mAddToTripOriginCoordinates;
	private int mAddToTripShadeColor;
	private boolean mHasPreppedAddToTripAnimation = false;

	private CubicBezierAnimation mBezierAnimation;
	private float mAddTripEndScaleX = 1f;
	private float mAddTripEndScaleY = 1f;

	private ArrayList<FrameLayoutTouchController> mBucketContainers = new ArrayList<FrameLayoutTouchController>();

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
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

	@Override
	public void onResume() {
		super.onResume();
		mStateHelper.registerWithProvider(this);
		mMeasurementHelper.registerWithProvider(this);
		mBackManager.registerWithParent(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		mStateHelper.unregisterWithProvider(this);
		mMeasurementHelper.unregisterWithProvider(this);
		mBackManager.unregisterWithParent(this);
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
		return Db.getTripBucket().getFlight() != null;
	}

	private boolean hasHotelTrip() {
		return Db.getTripBucket().getHotel() != null;
	}

	private Animator perpareTripBucketItemRemovalAnimator(final int removalIndex) {
		if (hasFlightTrip() && hasHotelTrip()) {
			int viewHeight = (mGrid.isLandscape() ? mGrid.getTotalHeight() : mGrid.getRowHeight(0)) / 3;
			final int[] bucketItemYPositions = mTripBucketTripToFrag.getBucketItemsPosition(1, (int) viewHeight);
			ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
			animator.setDuration(ANIM_DUR_BUCKET_ITEM_REMOVE);
			animator.addUpdateListener(new AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator arg0) {
					if (removalIndex == 1) {
						//moving hotel upwards
						mTripBucketHotelC.setTranslationY(bucketItemYPositions[0]);
					}
					else if (removalIndex == 2) {
						//moving flight one down
						mTripBucketFlightC.setTranslationY(bucketItemYPositions[0]);

					}
				}
			});
			return animator;
		}
		return null;
	}

	private int[] getCenterPositionsForTripBucket(int itemCount) {
		int viewSpace = (int) ((float) (mGrid.isLandscape() ? mGrid.getTotalHeight() : mGrid.getRowHeight(0)) / itemCount);
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
		int viewHeight = (mGrid.isLandscape() ? mGrid.getTotalHeight() : mGrid.getRowHeight(0)) / 3;
		int[] bucketItemYPositions = mTripBucketTripToFrag.getBucketItemsPosition(getNumberOfBucketContainers() - 1,
			(int) viewHeight);

		if (mGrid.isLandscape()) {
			if (!verticalOnly) {

				int flightSwipeOutDistance = (int) mFlightSwipeOut.getSwipeOutDistance();
				int hotelSwipeOutDistance = (int) mHotelSwipeOut.getSwipeOutDistance();
				int margin = getResources().getDimensionPixelSize(R.dimen.trip_bucket_item_horizontal_margin);
				int usableSpace = mGrid.getColWidth(2) - (2 * margin);

				setHorizontalPos(mTripBucketFlightC,
					mGrid.getColLeft(2) + margin - flightSwipeOutDistance,
					usableSpace + flightSwipeOutDistance);

				setHorizontalPos(mTripBucketHotelC,
					mGrid.getColLeft(2) + margin - hotelSwipeOutDistance,
					usableSpace + hotelSwipeOutDistance);
			}
		}
		else {
			if (!verticalOnly) {
				//We set the content containers to be the column width
				int flightSwipeOutDistance = (int) mFlightSwipeOut.getSwipeOutDistance();
				int hotelSwipeOutDistance = (int) mHotelSwipeOut.getSwipeOutDistance();

				setHorizontalPos(mTripFlightC, 0, mGrid.getTotalWidth() - mFlightSwipeOut.getPaddingLeft()
					- mFlightSwipeOut.getPaddingRight() - flightSwipeOutDistance);
				setHorizontalPos(mTripHotelC, 0, mGrid.getTotalWidth() - mHotelSwipeOut.getPaddingLeft()
					- mHotelSwipeOut.getPaddingRight() - hotelSwipeOutDistance);

				setHorizontalPos(mTripBucketFlightC, 0, mGrid.getTotalWidth());
				setHorizontalPos(mTripBucketHotelC, 0, mGrid.getTotalWidth());
			}
		}

		int index = 0;
		if (hasFlightTrip() || hasHotelTrip()) {
			if (hasFlightTrip()) {
				mTripBucketFlightC.setTranslationY(bucketItemYPositions[index]);
				index++;
			}
			if (hasHotelTrip()) {
				mTripBucketHotelC.setTranslationY(bucketItemYPositions[index]);
				index++;
			}
			mTripBucketTripToFrag.hideEmptyTripContainer(true);
		}
		else {
			mTripBucketTripToFrag.hideEmptyTripContainer(false);
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
			Db.getFlightSearch().clearSelectedLegs();
			Db.getTripBucket().clearFlight();
			positionTripBucketItems(true);
			setVisibilityState(mGlobalState);

			Db.kickOffBackgroundFlightSearchSave(getActivity());
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
			Db.getTripBucket().clearHotel();
			setVisibilityState(mGlobalState);
			positionTripBucketItems(true);

			Db.kickOffBackgroundHotelSearchSave(getActivity());
		}

	};

	private void setFragmentState(ResultsState state) {
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
			((ResultsTripBucketFlightFragment) frag).setState(TripBucketItemState.SHOWING_CHECKOUT_BUTTON);
			((ResultsTripBucketFlightFragment) frag).bind();
		}
		else if (tag == FTAG_BUCKET_HOTEL) {
			((ResultsTripBucketHotelFragment) frag).setState(TripBucketItemState.SHOWING_CHECKOUT_BUTTON);
			((ResultsTripBucketHotelFragment) frag).bind();
		}
	}

	private void setTouchState(ResultsState state) {
		//We never interact with this container
		mBlurredBackgroundC.setBlockNewEventsEnabled(true);
		mShadeC.setBlockNewEventsEnabled(true);

		switch (state) {
		case OVERVIEW: {
			mTripAnimationC.setBlockNewEventsEnabled(false);
			break;
		}
		default: {
			mTripAnimationC.setBlockNewEventsEnabled(true);
			break;
		}
		}
	}

	private void setVisibilityState(ResultsState state) {
		switch (state) {
		case OVERVIEW: {
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

	private void animateToPercentage(float percentage, boolean addingTrip) {
		if (mGrid.isLandscape()) {
			int colTwoDist = mGrid.getTotalWidth() - mGrid.getColLeft(2);
			float translationX = colTwoDist * (1f - percentage);
			for (View container : mBucketContainers) {
				container.setTranslationX(translationX);
			}
			mBlurredBackgroundC.setTranslationX(translationX);
		}
		else {
			//Background is easy
			int topDist = mGrid.getRowBottom(0) - getActivity().getActionBar().getHeight();
			float translationY = Math.max(-topDist * (1f - percentage), -topDist);
			mBlurredBackgroundC.setTranslationY(translationY);
			mBlurredBackgroundC.setAlpha(percentage);
			mTripBucketYourTripToC.setAlpha(percentage);

			//Trip bucket items are a bit harder
			int[] centers = getCenterPositionsForTripBucket(getNumberOfBucketContainers());
			int viewHeight = (mGrid.isLandscape() ? mGrid.getTotalHeight() : mGrid.getRowHeight(0)) / 3;
			float halfViewHeight = viewHeight / 2f;

			int index = 0;
			mTripBucketYourTripToC.setTranslationY(centers[index] - halfViewHeight
				- ((viewHeight + centers[index] - halfViewHeight) * (1f - percentage)));
			index++;
			if (hasFlightTrip() || hasHotelTrip()) {
				if (hasFlightTrip()) {
					mTripBucketFlightC.setTranslationY(centers[index] - halfViewHeight
						- ((viewHeight + centers[index] - halfViewHeight) * (1f - percentage)));
					index++;
				}
				if (hasHotelTrip()) {
					mTripBucketHotelC.setTranslationY(centers[index] - halfViewHeight
						- ((viewHeight + centers[index] - halfViewHeight) * (1f - percentage)));
					index++;
				}
			}
		}
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

			if (percentage == 1f && mGlobalState == ResultsState.OVERVIEW) {
				mAddingHotelTrip = false;
				mAddingFlightTrip = false;
			}
		}
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

		if (isFlights && !mGrid.isLandscape()) {
			//In this case our flights thing is in a wierd spot, so we need to figure out its final destination
			localDestRect = getAddedFlightPosFromLocalRect(localDestRect);
		}

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

			mTripAnimationC.addView(getAnimationView(isFlights), LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

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

	private View getAnimationView(boolean isFlights) {
		if (isFlights) {
			FlightLegSummarySectionTablet view = Ui.inflate(getActivity(),
				R.layout.flight_card_tablet_add_tripbucket, null);
			view.bindForTripBucket(Db.getFlightSearch());
			return view;
		}
		else {
			TextView tv = new TextView(getActivity());
			tv.setText("Hotel Animation View");
			tv.setBackgroundColor(Color.YELLOW);
			return tv;
		}
	}

	private Rect getAddedFlightPosFromLocalRect(Rect rect) {
		int[] centers = getCenterPositionsForTripBucket(hasHotelTrip() ? 3 : 2);
		int halfViewHeight = (rect.bottom - rect.top) / 2;

		Rect retRect = new Rect(rect);
		retRect.top = centers[1] - halfViewHeight;
		retRect.bottom = centers[1] + halfViewHeight;

		return retRect;
	}

	/*
	 * RESULTS STATE LISTENER
	 */

	private StateListenerHelper<ResultsState> mStateHelper = new StateListenerHelper<ResultsState>() {

		@Override
		public void onStateTransitionStart(ResultsState stateOne, ResultsState stateTwo) {
			//Touch
			mTripAnimationC.setBlockNewEventsEnabled(true);

			//Visibility
			if (stateOne == ResultsState.OVERVIEW || stateTwo == ResultsState.OVERVIEW) {
				mBlurredBackgroundC.setVisibility(View.VISIBLE);
				if (mAddingHotelTrip) {
					mTripAnimationC.setVisibility(View.VISIBLE);
				}
			}

			//layer type
			int layerType = View.LAYER_TYPE_HARDWARE;
			if ((stateOne == ResultsState.OVERVIEW || stateOne == ResultsState.HOTELS)
				&& (stateTwo == ResultsState.OVERVIEW || stateTwo == ResultsState.HOTELS)) {
				//Default -> Hotels or Hotels -> Default transition

				mBlurredBackgroundC.setLayerType(layerType, null);
				mTripAnimationC.setLayerType(layerType, null);
				mShadeC.setLayerType(layerType, null);

				for (View container : mBucketContainers) {
					container.setLayerType(layerType, null);
				}

			}

			if ((stateOne == ResultsState.OVERVIEW || stateOne == ResultsState.FLIGHTS)
				&& (stateTwo == ResultsState.OVERVIEW || stateTwo == ResultsState.FLIGHTS)) {
				//Default -> Flights or Flights -> Default transition

				mBlurredBackgroundC.setLayerType(layerType, null);
				mTripAnimationC.setLayerType(layerType, null);

				for (View container : mBucketContainers) {
					container.setLayerType(layerType, null);
				}
			}
		}

		@Override
		public void onStateTransitionUpdate(ResultsState stateOne, ResultsState stateTwo, float percentage) {
			if (stateOne == ResultsState.OVERVIEW && stateTwo == ResultsState.FLIGHTS) {
				animateToPercentage(1f - percentage, mAddingFlightTrip);
				addTripPercentage(1f - percentage);
			}
			else if (stateOne == ResultsState.FLIGHTS && stateTwo == ResultsState.OVERVIEW) {
				animateToPercentage(percentage, mAddingFlightTrip);
				addTripPercentage(percentage);
			}

			if (stateOne == ResultsState.OVERVIEW && stateTwo == ResultsState.HOTELS) {
				animateToPercentage(1f - percentage, mAddingHotelTrip);
				addTripPercentage(1f - percentage);
			}
			else if (stateOne == ResultsState.HOTELS && stateTwo == ResultsState.OVERVIEW) {
				animateToPercentage(percentage, mAddingHotelTrip);
				addTripPercentage(percentage);
			}
		}

		@Override
		public void onStateTransitionEnd(ResultsState stateOne, ResultsState stateTwo) {
			//Touch
			mTripAnimationC.setBlockNewEventsEnabled(true);

			//layer type
			int layerType = View.LAYER_TYPE_NONE;
			if ((stateOne == ResultsState.OVERVIEW || stateOne == ResultsState.HOTELS)
				&& (stateTwo == ResultsState.OVERVIEW || stateTwo == ResultsState.HOTELS)) {
				//Default -> Hotels or Hotels -> Default transition

				mBlurredBackgroundC.setLayerType(layerType, null);
				mTripAnimationC.setLayerType(layerType, null);
				mShadeC.setLayerType(layerType, null);

				for (View container : mBucketContainers) {
					container.setLayerType(layerType, null);
				}

			}

			if ((stateOne == ResultsState.OVERVIEW || stateOne == ResultsState.FLIGHTS)
				&& (stateTwo == ResultsState.OVERVIEW || stateTwo == ResultsState.FLIGHTS)) {
				//Default -> Flights or Flights -> Default transition

				mBlurredBackgroundC.setLayerType(layerType, null);
				mTripAnimationC.setLayerType(layerType, null);

				for (View container : mBucketContainers) {
					container.setLayerType(layerType, null);
				}
			}

		}

		@Override
		public void onStateFinalized(ResultsState state) {
			mGlobalState = state;
			setTouchState(state);
			setVisibilityState(state);
			setFragmentState(state);

			if (state == ResultsState.OVERVIEW) {
				mAddingHotelTrip = false;
				mAddingFlightTrip = false;
				mTripAnimationC.removeAllViews();

				animateToPercentage(1, false);
				addTripPercentage(1);
			}
		}
	};

	/*
	 * MEASUREMENT LISTENER
	 */

	private MeasurementHelper mMeasurementHelper = new MeasurementHelper() {

		@Override
		public void onContentSizeUpdated(int totalWidth, int totalHeight, boolean isLandscape) {
			mGrid.setDimensions(totalWidth, totalHeight);

			if (isLandscape) {
				mGrid.setGridSize(1, 3);
				mGrid.setContainerToColumn(mBlurredBackgroundC, 2);
				mGrid.setContainerToColumn(mTripAnimationC, 2);
				mGrid.setContainerToColumn(mTripBucketYourTripToC, 2);
			}
			else {
				mGrid.setGridSize(2, 2);
				mGrid.setContainerToRow(mBlurredBackgroundC, 0);
				mGrid.setContainerToRow(mTripAnimationC, 0);
				mGrid.setContainerToRow(mTripBucketYourTripToC, 0);
				mGrid.setContainerToColumnSpan(mTripBucketYourTripToC, 0, 1);
			}

			positionTripBucketItems(false);
		}

	};

	/*
	 * BACK STACK MANAGEMENT
	 */

	@Override
	public BackManager getBackManager() {
		return mBackManager;
	}

	private BackManager mBackManager = new BackManager(this) {

		@Override
		public boolean handleBackPressed() {
			return false;
		}

	};

}
