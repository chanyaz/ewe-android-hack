package com.expedia.bookings.fragment;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TabletResultsActivity.GlobalResultsState;
import com.expedia.bookings.interfaces.ITabletResultsController;
import com.expedia.bookings.utils.ColumnManager;
import com.expedia.bookings.widget.BlockEventFrameLayout;
import com.expedia.bookings.widget.FruitScrollUpListView.IFruitScrollUpListViewChangeListener;
import com.expedia.bookings.widget.FruitScrollUpListView.State;
import com.mobiata.android.util.Ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 *  TabletResultsFlightControllerFragment: designed for tablet results 2013
 *  This controls all the fragments relating to FLIGHTS results
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class TabletResultsFlightControllerFragment extends Fragment implements ITabletResultsController {

	public interface IFlightsFruitScrollUpListViewChangeListener {

		public void onFlightsStateChanged(State oldState, State newState, float percentage, View requester);

		public void onFlightsPercentageChanged(State state, float percentage);

	}

	private IFruitScrollUpListViewChangeListener mFruitProxy = new IFruitScrollUpListViewChangeListener() {

		@Override
		public void onStateChanged(State oldState, State newState, float percentage) {
			if (mListener != null) {
				mListener.onFlightsStateChanged(oldState, newState, percentage, mFlightListC);
			}
		}

		@Override
		public void onPercentageChanged(State state, float percentage) {
			if (mListener != null) {
				mListener.onFlightsPercentageChanged(state, percentage);
			}

		}
	};

	private static final String FRAG_TAG_FLIGHT_MAP = "FRAG_TAG_FLIGHT_MAP";
	private static final String FRAG_TAG_FLIGHT_FILTERS = "FRAG_TAG_FLIGHT_FILTERS";
	private static final String FRAG_TAG_FLIGHT_LIST = "FRAG_TAG_FLIGHT_LIST";

	//Containers
	private ViewGroup mRootC;
	private BlockEventFrameLayout mFlightListC;
	private BlockEventFrameLayout mFlightFiltersC;
	private BlockEventFrameLayout mFlightMapC;

	//Fragments
	private ResultsFlightListFragment mFlightListFrag;
	private ResultsFlightMapFragment mFlightMapFrag;
	private ResultsFlightFiltersFragment mFlightFilterFrag;

	//Other
	private GlobalResultsState mGlobalState;
	private IFlightsFruitScrollUpListViewChangeListener mListener;
	private ColumnManager mColumnManager = new ColumnManager(3);

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mListener = Ui.findFragmentListener(this, IFlightsFruitScrollUpListViewChangeListener.class, true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_tablet_results_flights, null, false);

		mRootC = Ui.findView(view, R.id.root_layout);
		mFlightMapC = Ui.findView(view, R.id.column_three_flight_map);
		mFlightFiltersC = Ui.findView(view, R.id.column_one_flight_filters);
		mFlightListC = Ui.findView(view, R.id.column_two_flight_list);

		return view;
	}

	private void setTouchState(GlobalResultsState state) {
		//We never interact with this container
		switch (state) {
		case DEFAULT: {
			mFlightMapC.setBlockNewEventsEnabled(true);
			mFlightFiltersC.setBlockNewEventsEnabled(true);
			mFlightListC.setBlockNewEventsEnabled(false);
			break;
		}
		case FLIGHTS: {
			mFlightMapC.setBlockNewEventsEnabled(false);
			mFlightFiltersC.setBlockNewEventsEnabled(false);
			mFlightListC.setBlockNewEventsEnabled(false);
			break;
		}
		default: {
			mFlightMapC.setBlockNewEventsEnabled(true);
			mFlightFiltersC.setBlockNewEventsEnabled(true);
			mFlightListC.setBlockNewEventsEnabled(true);
			break;
		}
		}
	}

	private void setVisibilityState(GlobalResultsState state) {
		switch (state) {
		case DEFAULT: {
			mFlightMapC.setVisibility(View.INVISIBLE);
			mFlightFiltersC.setVisibility(View.INVISIBLE);
			mFlightListC.setVisibility(View.VISIBLE);
			break;
		}
		case FLIGHTS: {
			mFlightMapC.setVisibility(View.VISIBLE);
			mFlightFiltersC.setVisibility(View.VISIBLE);
			mFlightListC.setVisibility(View.VISIBLE);
			break;
		}
		default: {
			mFlightMapC.setVisibility(View.INVISIBLE);
			mFlightFiltersC.setVisibility(View.INVISIBLE);
			mFlightListC.setVisibility(View.INVISIBLE);
			break;
		}
		}
	}

	private void setFragmentState(GlobalResultsState state) {

		//All of the fragment adds/removes come through this method, and we want to make sure our last call
		//is complete before moving forward, so this is important
		getChildFragmentManager().executePendingTransactions();

		//We will be adding all of our add/removes to this transaction
		FragmentTransaction transaction = this.getChildFragmentManager().beginTransaction();

		boolean flightListAvailable = true;
		boolean flightMapAvailable = true;
		boolean flightFiltersAvailable = true;

		if (state == GlobalResultsState.HOTELS) {
			flightMapAvailable = false;
			flightFiltersAvailable = false;
		}

		//Flight list
		setFlightListFragmentAvailability(flightListAvailable, transaction);

		//Flight map
		setFlightMapFragmentAvailability(flightMapAvailable, transaction);

		//Flight filters
		setFlightFilterFragmentAvailability(flightFiltersAvailable, transaction);

		transaction.commit();

	}

	private FragmentTransaction setFlightListFragmentAvailability(boolean available, FragmentTransaction transaction) {
		if (available) {
			if (mFlightListFrag == null || !mFlightListFrag.isAdded()) {

				if (mFlightListFrag == null) {
					mFlightListFrag = (ResultsFlightListFragment) getChildFragmentManager().findFragmentByTag(
							FRAG_TAG_FLIGHT_LIST);
				}
				if (mFlightListFrag == null) {
					mFlightListFrag = new ResultsFlightListFragment();
				}
				if (!mFlightListFrag.isAdded()) {
					transaction.add(R.id.column_two_flight_list, mFlightListFrag, FRAG_TAG_FLIGHT_LIST);
				}

				mFlightListFrag.setChangeListener(mFruitProxy);
			}
		}
		else {
			if (mFlightListFrag == null) {
				mFlightListFrag = (ResultsFlightListFragment) getChildFragmentManager().findFragmentByTag(
						FRAG_TAG_FLIGHT_LIST);
			}
			if (mFlightListFrag != null) {
				transaction.remove(mFlightListFrag);
			}
		}
		return transaction;
	}

	private FragmentTransaction setFlightMapFragmentAvailability(boolean available,
			FragmentTransaction transaction) {
		if (available) {
			if (mFlightMapFrag == null || !mFlightMapFrag.isAdded()) {
				if (mFlightMapFrag == null) {
					mFlightMapFrag = (ResultsFlightMapFragment) getChildFragmentManager().findFragmentByTag(
							FRAG_TAG_FLIGHT_MAP);
				}
				if (mFlightMapFrag == null) {
					mFlightMapFrag = ResultsFlightMapFragment.newInstance();
				}
				if (!mFlightMapFrag.isAdded()) {
					transaction.add(R.id.column_three_flight_map, mFlightMapFrag, FRAG_TAG_FLIGHT_MAP);
				}
			}
		}
		else {
			if (mFlightMapFrag == null) {
				mFlightMapFrag = (ResultsFlightMapFragment) getChildFragmentManager().findFragmentByTag(
						FRAG_TAG_FLIGHT_MAP);
			}
			if (mFlightMapFrag != null) {
				transaction.remove(mFlightMapFrag);
			}
		}
		return transaction;
	}

	private FragmentTransaction setFlightFilterFragmentAvailability(boolean available,
			FragmentTransaction transaction) {
		if (available) {
			if (mFlightFilterFrag == null || !mFlightFilterFrag.isAdded()) {
				if (mFlightFilterFrag == null) {
					mFlightFilterFrag = (ResultsFlightFiltersFragment) getChildFragmentManager().findFragmentByTag(
							FRAG_TAG_FLIGHT_FILTERS);
				}
				if (mFlightFilterFrag == null) {
					mFlightFilterFrag = ResultsFlightFiltersFragment.newInstance();
				}
				if (!mFlightFilterFrag.isAdded()) {
					transaction.add(R.id.column_one_flight_filters, mFlightFilterFrag, FRAG_TAG_FLIGHT_FILTERS);
				}
			}
		}
		else {
			if (mFlightFilterFrag == null) {
				mFlightFilterFrag = (ResultsFlightFiltersFragment) getChildFragmentManager().findFragmentByTag(
						FRAG_TAG_FLIGHT_FILTERS);
			}
			if (mFlightFilterFrag != null) {
				transaction.remove(mFlightFilterFrag);
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
			mFlightListC.setVisibility(View.VISIBLE);
		}
		else if (state == GlobalResultsState.FLIGHTS) {
			mFlightMapC.setVisibility(View.VISIBLE);
			mFlightFiltersC.setVisibility(View.VISIBLE);
			mFlightListC.setVisibility(View.VISIBLE);
		}

	}

	@Override
	public void setHardwareLayerForTransition(int layerType, GlobalResultsState stateOne, GlobalResultsState stateTwo) {
		if ((stateOne == GlobalResultsState.DEFAULT || stateOne == GlobalResultsState.HOTELS)
				&& (stateTwo == GlobalResultsState.DEFAULT || stateTwo == GlobalResultsState.HOTELS)) {
			//Default -> Hotels or Hotels -> Default transition

			mFlightListC.setLayerType(layerType, null);

		}

		if ((stateOne == GlobalResultsState.DEFAULT || stateOne == GlobalResultsState.FLIGHTS)
				&& (stateTwo == GlobalResultsState.DEFAULT || stateTwo == GlobalResultsState.FLIGHTS)) {
			//Default -> Flights or Flights -> Default transition

			mFlightMapC.setLayerType(layerType, null);
			mFlightFiltersC.setLayerType(layerType, null);
		}
	}

	@Override
	public void blockAllNewTouches(View requester) {
		if (mFlightMapC != requester) {
			mFlightMapC.setBlockNewEventsEnabled(true);
		}
		if (mFlightFiltersC != requester) {
			mFlightFiltersC.setBlockNewEventsEnabled(true);
		}
		if (mFlightListC != requester) {
			mFlightListC.setBlockNewEventsEnabled(true);
		}
	}

	@Override
	public void animateToFlightsPercentage(float percentage) {
		mFlightFiltersC.setAlpha(1f - percentage);
		mFlightMapC.setAlpha(1f - percentage);
		float filterPaneTopTranslation = percentage * mFlightListFrag.getTopSpaceListView().getHeaderSpacerHeight();
		mFlightFiltersC.setTranslationY(filterPaneTopTranslation);
	}

	@Override
	public void animateToHotelsPercentage(float percentage) {
		int colOneDist = mColumnManager.getTotalWidth() - mColumnManager.getColLeft(1);
		int colTwoDist = mColumnManager.getTotalWidth() - mColumnManager.getColLeft(2);

		mFlightMapC.setTranslationX(colTwoDist * (1f - percentage));
		mFlightListC.setTranslationX(colOneDist * (1f - percentage));
	}

	@Override
	public void updateColumnWidths(int totalWidth) {
		mColumnManager.setTotalWidth(totalWidth);

		mColumnManager.setContainerToColumn(mFlightFiltersC, 0);
		mColumnManager.setContainerToColumn(mFlightListC, 1);
		mColumnManager.setContainerToColumn(mFlightMapC, 2);
	}

	@Override
	public boolean handleBackPressed() {
		if (mGlobalState == GlobalResultsState.FLIGHTS) {
			mFlightListFrag.gotoBottomPosition();
			return true;
		}
		return false;
	}
}
