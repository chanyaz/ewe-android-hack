package com.expedia.bookings.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TabletResultsActivity.GlobalResultsState;
import com.expedia.bookings.animation.CubicBezierAnimation;
import com.expedia.bookings.interfaces.IAddToTripListener;
import com.expedia.bookings.interfaces.ITabletResultsController;
import com.expedia.bookings.utils.ColumnManager;
import com.expedia.bookings.utils.ScreenPositionUtils;
import com.expedia.bookings.widget.BlockEventFrameLayout;
import com.expedia.bookings.widget.FixedTranslationFrameLayout;
import com.mobiata.android.util.Ui;

/**
 *  TabletResultsTripControllerFragment: designed for tablet results 2013
 *  This controls all the fragments relating to the Trip Overview
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class TabletResultsTripControllerFragment extends Fragment implements ITabletResultsController,
		IAddToTripListener {

	private enum FragTag {
		TRIP_OVERVIEW,
		BLURRED_BG
	}

	private ResultsTripOverviewFragment mTripOverviewFrag;
	private ResultsBlurBackgroundImageFragment mBlurredBackgroundFrag;

	private ViewGroup mRootC;
	private BlockEventFrameLayout mTripOverviewC;
	private BlockEventFrameLayout mTripAnimationC;
	private FixedTranslationFrameLayout mBlurredBackgroundC;
	private BlockEventFrameLayout mShadeC;

	private GlobalResultsState mGlobalState;
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

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mParentResultsController = Ui.findFragmentListener(this, ITabletResultsController.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_tablet_results_trip, null, false);

		mRootC = Ui.findView(view, R.id.root_layout);
		mTripOverviewC = Ui.findView(view, R.id.column_three_trip_pane);
		mBlurredBackgroundC = Ui.findView(view, R.id.column_three_blurred_bg);
		mTripAnimationC = Ui.findView(view, R.id.trip_add_animation_view);
		mShadeC = Ui.findView(view, R.id.column_one_shade);

		return view;
	}

	private void setFragmentState(GlobalResultsState state) {

		//All of the fragment adds/removes come through this method, and we want to make sure our last call
		//is complete before moving forward, so this is important
		getChildFragmentManager().executePendingTransactions();

		//We will be adding all of our add/removes to this transaction
		FragmentTransaction transaction = this.getChildFragmentManager().beginTransaction();

		boolean tripOverviewAvailable = true;
		boolean blurredBackgroundAvailable = true;

		//Trip Overview
		mTripOverviewFrag = (ResultsTripOverviewFragment) setFragmentAvailability(tripOverviewAvailable,
				FragTag.TRIP_OVERVIEW, transaction, R.id.column_three_trip_pane, false);

		//Blurrred Background (for behind trip overview)
		mBlurredBackgroundFrag = (ResultsBlurBackgroundImageFragment) setFragmentAvailability(
				blurredBackgroundAvailable, FragTag.BLURRED_BG, transaction, R.id.column_three_blurred_bg, false);

		transaction.commit();

	}

	private Fragment setFragmentAvailability(boolean available, FragTag tag, FragmentTransaction transaction,
			int container, boolean alwaysRunSetup) {
		Fragment frag = fragmentGetLocalInstance(tag);
		if (available) {
			if (frag == null || !frag.isAdded()) {
				if (frag == null) {
					frag = getChildFragmentManager().findFragmentByTag(tag.name());
				}
				if (frag == null) {
					frag = fragmentNewInstance(tag);
				}
				if (!frag.isAdded()) {
					transaction.add(container, frag, tag.name());
				}
				fragmentSetup(tag, frag);
			}
			else if (alwaysRunSetup) {
				fragmentSetup(tag, frag);
			}
		}
		else {
			if (frag != null) {
				transaction.remove(frag);
			}
			frag = null;
		}
		return frag;
	}

	public Fragment fragmentGetLocalInstance(FragTag tag) {
		Fragment frag = null;
		switch (tag) {
		case TRIP_OVERVIEW: {
			frag = mTripOverviewFrag;
			break;
		}
		case BLURRED_BG: {
			frag = mBlurredBackgroundFrag;
			break;
		}
		}
		return frag;
	}

	public Fragment fragmentNewInstance(FragTag tag) {
		Fragment frag = null;
		switch (tag) {
		case TRIP_OVERVIEW: {
			frag = ResultsTripOverviewFragment.newInstance();
			break;
		}
		case BLURRED_BG: {
			frag = ResultsBlurBackgroundImageFragment.newInstance();
			break;
		}
		}
		return frag;
	}

	public void fragmentSetup(FragTag tag, Fragment frag) {
		//Currently the fragments require no setup
	}

	private void setTouchState(GlobalResultsState state) {
		//We never interact with this container
		mBlurredBackgroundC.setBlockNewEventsEnabled(true);
		mShadeC.setBlockNewEventsEnabled(true);

		switch (state) {
		case DEFAULT: {
			mTripOverviewC.setBlockNewEventsEnabled(false);
			mTripAnimationC.setBlockNewEventsEnabled(false);
			break;
		}
		default: {
			mTripOverviewC.setBlockNewEventsEnabled(true);
			mTripAnimationC.setBlockNewEventsEnabled(true);
			break;
		}
		}
	}

	private void setVisibilityState(GlobalResultsState state) {
		switch (state) {
		case DEFAULT: {
			mTripOverviewC.setVisibility(View.VISIBLE);
			mBlurredBackgroundC.setVisibility(View.VISIBLE);
			mTripAnimationC.setVisibility(View.INVISIBLE);
			mShadeC.setVisibility(View.INVISIBLE);
			break;
		}
		default: {
			mTripOverviewC.setVisibility(View.INVISIBLE);
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
			mTripOverviewC.setVisibility(View.VISIBLE);
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

			mTripOverviewC.setLayerType(layerType, null);
			mBlurredBackgroundC.setLayerType(layerType, null);
			mTripAnimationC.setLayerType(layerType, null);
			mShadeC.setLayerType(layerType, null);

		}

		if ((stateOne == GlobalResultsState.DEFAULT || stateOne == GlobalResultsState.FLIGHTS)
				&& (stateTwo == GlobalResultsState.DEFAULT || stateTwo == GlobalResultsState.FLIGHTS)) {
			//Default -> Flights or Flights -> Default transition

			mTripOverviewC.setLayerType(layerType, null);
			mBlurredBackgroundC.setLayerType(layerType, null);
			mTripAnimationC.setLayerType(layerType, null);
		}
	}

	@Override
	public void blockAllNewTouches(View requester) {
		if (mTripOverviewC != requester) {
			mTripOverviewC.setBlockNewEventsEnabled(true);
		}
		if (mTripAnimationC != requester) {
			mTripAnimationC.setBlockNewEventsEnabled(true);
		}
	}

	private void animateToPercentage(float percentage, boolean addingTrip) {
		int colTwoDist = mColumnManager.getTotalWidth() - mColumnManager.getColLeft(2);

		mTripOverviewC.setTranslationX(colTwoDist * (1f - percentage));
		mBlurredBackgroundC.setTranslationX(colTwoDist * (1f - percentage));
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
		mColumnManager.setTotalWidth(totalWidth);

		mColumnManager.setContainerToColumn(mTripOverviewC, 2);
		mColumnManager.setContainerToColumn(mBlurredBackgroundC, 2);
		mColumnManager.setContainerToColumn(mTripAnimationC, 2);
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

		Rect destRect;
		if (isFlights) {
			destRect = ScreenPositionUtils.translateGlobalPositionToLocalPosition(
					mTripOverviewFrag.getFlightContainerRect(), mRootC);
		}
		else {
			destRect = ScreenPositionUtils.translateGlobalPositionToLocalPosition(
					mTripOverviewFrag.getHotelContainerRect(), mRootC);
		}

		LayoutParams params = (LayoutParams) mTripAnimationC.getLayoutParams();
		if (!mHasPreppedAddToTripAnimation || params.leftMargin != destRect.left
				|| params.topMargin != destRect.top) {

			Rect origRect = ScreenPositionUtils.translateGlobalPositionToLocalPosition(mAddToTripOriginCoordinates,
					mRootC);

			//Do calculations
			int originWidth = origRect.right - origRect.left;
			int originHeight = origRect.bottom - origRect.top;
			int destWidth = destRect.right - destRect.left;
			int destHeight = destRect.bottom - destRect.top;

			//Position the view where it will be ending up, but sized as it will be starting...

			params.leftMargin = destRect.left;
			params.topMargin = destRect.top;
			params.width = originWidth;
			params.height = originHeight;
			mTripAnimationC.setLayoutParams(params);

			if (isFlights) {
				mTripAnimationC.addView(mTripOverviewFrag.getFlightViewForAddTrip(), LayoutParams.MATCH_PARENT,
						LayoutParams.MATCH_PARENT);
			}
			else {
				mTripAnimationC.addView(mTripOverviewFrag.getHotelViewForAddTrip(), LayoutParams.MATCH_PARENT,
						LayoutParams.MATCH_PARENT);
			}

			//Translation. We setup the translation animation to start at deltaX, deltaY and animate
			// to it's final resting place of (0,0), i.e. no translation.
			int deltaX = origRect.left - destRect.left;
			int deltaY = origRect.top - destRect.top;
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
}
