package com.expedia.bookings.fragment;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TabletResultsActivity.GlobalResultsState;
import com.expedia.bookings.interfaces.IAddToTripListener;
import com.expedia.bookings.interfaces.ITabletResultsController;
import com.expedia.bookings.utils.ColumnManager;
import com.expedia.bookings.widget.BlockEventFrameLayout;
import com.expedia.bookings.widget.FixedTranslationFrameLayout;
import com.mobiata.android.util.Ui;

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
import android.widget.FrameLayout;

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
	private BlockEventFrameLayout mTripHotelC;
	private FixedTranslationFrameLayout mBlurredBackgroundC;
	private BlockEventFrameLayout mShadeC;

	private GlobalResultsState mGlobalState;
	private ColumnManager mColumnManager = new ColumnManager(3);
	private ITabletResultsController mParentResultsController;

	private boolean mAddingHotelTrip = false;
	private boolean mAddingFlightTrip = false;

	private int mAddHotelsStartingTranslationX = 0;
	private int mAddHotelStartingTranslationY = 0;
	private float mAddHotelStartingScaleX = 1f;
	private float mAddHotelStartingScaleY = 1f;

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
		mTripHotelC = Ui.findView(view, R.id.column_three_trip_hotel);
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
			mTripHotelC.setBlockNewEventsEnabled(false);
			break;
		}
		default: {
			mTripOverviewC.setBlockNewEventsEnabled(true);
			mTripHotelC.setBlockNewEventsEnabled(true);
			break;
		}
		}
	}

	private void setVisibilityState(GlobalResultsState state) {
		switch (state) {
		case DEFAULT: {
			mTripOverviewC.setVisibility(View.VISIBLE);
			mBlurredBackgroundC.setVisibility(View.VISIBLE);
			mTripHotelC.setVisibility(View.VISIBLE);
			if (!mAddingHotelTrip) {
				mShadeC.setVisibility(View.INVISIBLE);
			}
			break;
		}
		default: {
			mTripOverviewC.setVisibility(View.INVISIBLE);
			mBlurredBackgroundC.setVisibility(View.INVISIBLE);
			mTripHotelC.setVisibility(View.INVISIBLE);
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
	}

	@Override
	public void setAnimatingTowardsVisibility(GlobalResultsState state) {
		if (state == GlobalResultsState.DEFAULT) {
			mTripOverviewC.setVisibility(View.VISIBLE);
			mTripHotelC.setVisibility(View.VISIBLE);
			mBlurredBackgroundC.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void setHardwareLayerForTransition(int layerType, GlobalResultsState stateOne, GlobalResultsState stateTwo) {
		if ((stateOne == GlobalResultsState.DEFAULT || stateOne == GlobalResultsState.HOTELS)
				&& (stateTwo == GlobalResultsState.DEFAULT || stateTwo == GlobalResultsState.HOTELS)) {
			//Default -> Hotels or Hotels -> Default transition

			mTripOverviewC.setLayerType(layerType, null);
			mBlurredBackgroundC.setLayerType(layerType, null);
			mTripHotelC.setLayerType(layerType, null);
			mShadeC.setLayerType(layerType, null);

		}

		if ((stateOne == GlobalResultsState.DEFAULT || stateOne == GlobalResultsState.FLIGHTS)
				&& (stateTwo == GlobalResultsState.DEFAULT || stateTwo == GlobalResultsState.FLIGHTS)) {
			//Default -> Flights or Flights -> Default transition

			mTripOverviewC.setLayerType(layerType, null);
			mBlurredBackgroundC.setLayerType(layerType, null);
			mTripHotelC.setLayerType(layerType, null);
		}
	}

	@Override
	public void blockAllNewTouches(View requester) {
		if (mTripOverviewC != requester) {
			mTripOverviewC.setBlockNewEventsEnabled(true);
		}
		if (mTripHotelC != requester) {
			mTripHotelC.setBlockNewEventsEnabled(true);
		}
	}

	private void animateToPercentage(float percentage, boolean addingTrip) {
		int colTwoDist = mColumnManager.getTotalWidth() - mColumnManager.getColLeft(2);

		mTripOverviewC.setTranslationX(colTwoDist * (1f - percentage));
		mBlurredBackgroundC.setTranslationX(colTwoDist * (1f - percentage));

		if (!addingTrip) {
			mTripHotelC.setTranslationX(colTwoDist * (1f - percentage));
		}
	}

	private void addHotelsPercentage(float percentage) {

		if (mAddingHotelTrip) {

			mTripHotelC.setTranslationX((1f - percentage) * mAddHotelsStartingTranslationX);
			mTripHotelC.setTranslationY((1f - percentage) * mAddHotelStartingTranslationY);
			mTripHotelC.setScaleX(1f + ((mAddHotelStartingScaleX - 1f) * (1f - percentage)));
			mTripHotelC.setScaleY(1f + ((mAddHotelStartingScaleY - 1f) * (1f - percentage)));
			mShadeC.setAlpha(1f - percentage);

			if (percentage == 1f && mGlobalState == GlobalResultsState.DEFAULT) {
				mAddingHotelTrip = false;
			}
		}
	}

	@Override
	public void animateToFlightsPercentage(float percentage) {
		animateToPercentage(percentage, mAddingFlightTrip);
	}

	@Override
	public void animateToHotelsPercentage(float percentage) {
		animateToPercentage(percentage, mAddingHotelTrip);
		addHotelsPercentage(percentage);
	}

	@Override
	public void updateColumnWidths(int totalWidth) {
		mColumnManager.setTotalWidth(totalWidth);

		mColumnManager.setContainerToColumn(mTripOverviewC, 2);
		mColumnManager.setContainerToColumn(mBlurredBackgroundC, 2);
		mColumnManager.setContainerToColumn(mTripHotelC, 2);

		//Update where the hotel trip goes. This should probably be controlled by the ResultsTripOverview fragment...
		if (mTripHotelC != null) {
			((FrameLayout.LayoutParams) mTripHotelC.getLayoutParams()).topMargin = mRootC.getHeight()
					- mTripHotelC.getHeight();
			mTripHotelC.setLayoutParams(mTripHotelC.getLayoutParams());
		}
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
		int[] rootGlobalPos = new int[2];
		mRootC.getLocationOnScreen(rootGlobalPos);

		int globalLeft = rootGlobalPos[0] + mTripHotelC.getLeft();
		int globalTop = rootGlobalPos[1] + mTripHotelC.getTop();
		mAddHotelsStartingTranslationX = globalCoordinates.left - globalLeft;
		mAddHotelStartingTranslationY = globalCoordinates.top - globalTop;
		int destWidth = globalCoordinates.right - globalCoordinates.left;
		int destHeight = globalCoordinates.bottom - globalCoordinates.top;
		float scaleMultX = (float) destWidth / mTripHotelC.getWidth();
		float scaleMultY = (float) destHeight / mTripHotelC.getHeight();

		mTripHotelC.setAlpha(0f);
		mTripHotelC.setVisibility(View.VISIBLE);
		mTripHotelC.setPivotX(0);
		mTripHotelC.setPivotY(0);
		mTripHotelC.setTranslationX(mAddHotelsStartingTranslationX);
		mTripHotelC.setTranslationY(mAddHotelStartingTranslationY);
		mTripHotelC.setScaleX(scaleMultX);
		mTripHotelC.setScaleY(scaleMultY);

		mAddHotelStartingScaleX = mTripHotelC.getScaleX();
		mAddHotelStartingScaleY = mTripHotelC.getScaleY();

		mShadeC.setBackgroundColor(shadeColor);
		mAddingHotelTrip = true;
	}

	@Override
	public void guiElementInPosition() {
		mShadeC.setVisibility(View.VISIBLE);
		mTripHotelC.setVisibility(View.VISIBLE);
		mTripHotelC.setAlpha(1f);
		mShadeC.setAlpha(1f);
	}

}
