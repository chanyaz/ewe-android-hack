package com.expedia.bookings.fragment;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TabletResultsActivity.GlobalResultsState;
import com.expedia.bookings.interfaces.ITabletResultsController;
import com.expedia.bookings.utils.ColumnManager;
import com.expedia.bookings.widget.BlockEventFrameLayout;
import com.expedia.bookings.widget.FixedTranslationFrameLayout;
import com.mobiata.android.util.Ui;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 *  TabletResultsTripControllerFragment: designed for tablet results 2013
 *  This controls all the fragments relating to the Trip Overview
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class TabletResultsTripControllerFragment extends Fragment implements ITabletResultsController {

	private static final String FRAG_TAG_TRIP_OVERVIEW = "FRAG_TAG_TRIP_OVERVIEW";
	private static final String FRAG_BLURRED_BG = "FRAG_BLURRED_BG";

	private ResultsTripOverviewFragment mTripOverviewFrag;
	private ResultsBlurBackgroundImageFragment mBlurredBackgroundFrag;

	private ViewGroup mRootC;
	private BlockEventFrameLayout mTripOverviewC;
	private FixedTranslationFrameLayout mBlurredBackgroundC;

	private GlobalResultsState mGlobalState;
	private ColumnManager mColumnManager = new ColumnManager(3);

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_tablet_results_trip, null, false);

		mRootC = Ui.findView(view, R.id.root_layout);
		mTripOverviewC = Ui.findView(view, R.id.column_three_trip_pane);
		mBlurredBackgroundC = Ui.findView(view, R.id.column_three_blurred_bg);

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

		switch (state) {
		case DEFAULT: {
			mTripOverviewC.setBlockNewEventsEnabled(false);
			break;
		}
		default: {
			mTripOverviewC.setBlockNewEventsEnabled(true);
			break;
		}
		}
	}

	private void setVisibilityState(GlobalResultsState state) {
		switch (state) {
		case DEFAULT: {
			mTripOverviewC.setVisibility(View.VISIBLE);
			mBlurredBackgroundC.setVisibility(View.VISIBLE);
			break;
		}
		default: {
			mTripOverviewC.setVisibility(View.GONE);
			mBlurredBackgroundC.setVisibility(View.GONE);
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
			mBlurredBackgroundC.setVisibility(View.VISIBLE);
		}
	}

	private void setHardwareLayers(boolean useHardwareLayer) {
		int layerValue = useHardwareLayer ? View.LAYER_TYPE_HARDWARE : View.LAYER_TYPE_NONE;
		mTripOverviewC.setLayerType(layerValue, null);
		mBlurredBackgroundC.setLayerType(layerValue, null);
	}

	@Override
	public void setHardwareLayerFlightsTransition(boolean useHardwareLayer) {
		setHardwareLayers(useHardwareLayer);
	}

	@Override
	public void setHardwareLayerHotelsTransition(boolean useHardwareLayer) {
		setHardwareLayers(useHardwareLayer);

	}

	@Override
	public void blockAllNewTouches(View requester) {
		if (mTripOverviewC != requester) {
			mTripOverviewC.setBlockNewEventsEnabled(true);
		}
	}

	private void animateToPercentage(float percentage) {
		int colTwoDist = mColumnManager.getTotalWidth() - mColumnManager.getColLeft(2);

		mTripOverviewC.setTranslationX(colTwoDist * (1f - percentage));
		mBlurredBackgroundC.setTranslationX(colTwoDist * (1f - percentage));
	}

	@Override
	public void animateToFlightsPercentage(float percentage) {
		animateToPercentage(percentage);
	}

	@Override
	public void animateToHotelsPercentage(float percentage) {
		animateToPercentage(percentage);

	}

	@Override
	public void updateColumnWidths(int totalWidth) {
		mColumnManager.setTotalWidth(totalWidth);

		mColumnManager.setContainerToColumn(mTripOverviewC, 2);
		mColumnManager.setContainerToColumn(mBlurredBackgroundC, 2);
	}

	@Override
	public boolean handleBackPressed() {
		return false;
	}

}
