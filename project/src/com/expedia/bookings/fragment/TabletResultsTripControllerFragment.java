package com.expedia.bookings.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
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

	private static final String FRAG_TAG_TRIP_OVERVIEW = "FRAG_TAG_TRIP_OVERVIEW";
	private static final String FRAG_BLURRED_BG = "FRAG_BLURRED_BG";

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
		setTripOverviewFragmentAvailability(tripOverviewAvailable, transaction);

		//Blurrred Background (for behind trip overview)
		setBlurredBackgroundFragmentAvailability(blurredBackgroundAvailable, transaction);

		transaction.commit();

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

	private FragmentTransaction setTripOverviewFragmentAvailability(boolean available,
			FragmentTransaction transaction) {
		if (available) {
			if (mTripOverviewFrag == null || !mTripOverviewFrag.isAdded()) {
				if (mTripOverviewFrag == null) {
					mTripOverviewFrag = (ResultsTripOverviewFragment) getChildFragmentManager().findFragmentByTag(
							FRAG_TAG_TRIP_OVERVIEW);//Ui.findSupportFragment(this, FRAG_TAG_TRIP_OVERVIEW);
				}
				if (mTripOverviewFrag == null) {
					mTripOverviewFrag = ResultsTripOverviewFragment.newInstance();
				}
				if (!mTripOverviewFrag.isAdded()) {
					transaction.add(R.id.column_three_trip_pane, mTripOverviewFrag, FRAG_TAG_TRIP_OVERVIEW);
				}
			}
		}
		else {
			if (mTripOverviewFrag == null) {
				mTripOverviewFrag = (ResultsTripOverviewFragment) getChildFragmentManager().findFragmentByTag(
						FRAG_TAG_TRIP_OVERVIEW);
			}
			if (mTripOverviewFrag != null) {
				transaction.remove(mTripOverviewFrag);
			}
		}
		return transaction;
	}

	private FragmentTransaction setBlurredBackgroundFragmentAvailability(boolean available,
			FragmentTransaction transaction) {
		if (available) {
			if (mBlurredBackgroundFrag == null || !mBlurredBackgroundFrag.isAdded()) {
				if (mBlurredBackgroundFrag == null) {
					mBlurredBackgroundFrag = (ResultsBlurBackgroundImageFragment) getChildFragmentManager()
							.findFragmentByTag(FRAG_BLURRED_BG);
				}
				if (mBlurredBackgroundFrag == null) {
					mBlurredBackgroundFrag = ResultsBlurBackgroundImageFragment.newInstance();
				}
				if (!mBlurredBackgroundFrag.isAdded()) {
					transaction.add(R.id.column_three_blurred_bg, mBlurredBackgroundFrag, FRAG_BLURRED_BG);
				}
			}
		}
		else {
			if (mBlurredBackgroundFrag == null) {
				mBlurredBackgroundFrag = (ResultsBlurBackgroundImageFragment) getChildFragmentManager()
						.findFragmentByTag(FRAG_BLURRED_BG);
			}
			if (mBlurredBackgroundFrag != null) {
				transaction.remove(mBlurredBackgroundFrag);
			}
		}
		return transaction;
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
			if(mAddTripEndScaleX > 1f){
				mTripAnimationC.setScaleX(1f + percentage * (mAddTripEndScaleX - 1f));
			}else{
				mTripAnimationC.setScaleX(1f - (1f - mAddTripEndScaleX) * percentage);
			}
			if(mAddTripEndScaleY > 1f){
				mTripAnimationC.setScaleY(1f + percentage * (mAddTripEndScaleY - 1f));
			}else{
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
		//TODO: WE ARE NOT GOING TO ALWAYS BE PASSING ARBITRARY STRINGS AROUND
		boolean isFlights = (data instanceof String && ((String) data).equalsIgnoreCase("FLIGHTS"));

		Rect origRect = ScreenPositionUtils.translateGlobalPositionToLocalPosition(globalCoordinates,mRootC);

		Rect destRect;
		if (isFlights) {
			destRect = ScreenPositionUtils.translateGlobalPositionToLocalPosition(mTripOverviewFrag.getFlightContainerRect(),mRootC);
		}else{
			destRect = ScreenPositionUtils.translateGlobalPositionToLocalPosition(mTripOverviewFrag.getHotelContainerRect(),mRootC);
		}

		//Do calculations
		int originWidth = origRect.right - origRect.left;
		int originHeight = origRect.bottom - origRect.top;
		int destWidth = destRect.right - destRect.left;
		int destHeight = destRect.bottom - destRect.top;

		//Position the view where it will be ending up, but sized as it will be starting...
		LayoutParams params = (LayoutParams) mTripAnimationC.getLayoutParams();
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

		mShadeC.setBackgroundColor(shadeColor);
		if (isFlights) {
			mAddingFlightTrip = true;
		}
		else {
			mAddingHotelTrip = true;
		}
	}

	@Override
	public void performTripHandoff() {
		mShadeC.setVisibility(View.VISIBLE);
		mTripAnimationC.setVisibility(View.VISIBLE);
		mTripAnimationC.setAlpha(1f);
		mShadeC.setAlpha(1f);
	}

}
