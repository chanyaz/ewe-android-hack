package com.expedia.bookings.fragment;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.enums.ResultsFlightLegState;
import com.expedia.bookings.enums.ResultsFlightsListState;
import com.expedia.bookings.interfaces.IBackManageable;
import com.expedia.bookings.interfaces.IResultsFlightLegSelected;
import com.expedia.bookings.interfaces.IResultsFlightSelectedListener;
import com.expedia.bookings.interfaces.IStateListener;
import com.expedia.bookings.interfaces.IStateProvider;
import com.expedia.bookings.interfaces.helpers.BackManager;
import com.expedia.bookings.interfaces.helpers.MeasurementHelper;
import com.expedia.bookings.interfaces.helpers.StateListenerCollection;
import com.expedia.bookings.interfaces.helpers.StateListenerHelper;
import com.expedia.bookings.interfaces.helpers.StateManager;
import com.expedia.bookings.utils.FragmentAvailabilityUtils;
import com.expedia.bookings.utils.GridManager;
import com.expedia.bookings.widget.FrameLayoutTouchController;
import com.expedia.bookings.widget.FruitList;
import com.mobiata.android.Log;
import com.mobiata.android.util.Ui;

/**
 * ResultsRecursiveFlightLegsFragment
 * <p/>
 * This fragment allows us to select N flight legs, by providing us the means to select
 * a single flight leg, and attaching another instance of itself to select a future leg,
 * and so on.
 * <p/>
 * This fragment is architected this way in order to both allow N flight legs, AND to work
 * with our StateProvider/Manager/... system. Where a state represents a definitive ui state.
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class ResultsRecursiveFlightLegsFragment extends Fragment implements IStateProvider<ResultsFlightLegState>,
	FragmentAvailabilityUtils.IFragmentAvailabilityProvider, IBackManageable, IResultsFlightLegSelected,
	IResultsFlightSelectedListener {

	public static ResultsRecursiveFlightLegsFragment newInstance(int legNumber) {
		ResultsRecursiveFlightLegsFragment frag = new ResultsRecursiveFlightLegsFragment(legNumber);
		return frag;
	}

	//Settings
	private static final float DETAILS_MARGIN_PERCENTAGE = 0.1f;

	//Fragment tags
	private static final String FTAG_DETAILS = "FTAG_DETAILS";
	private static final String FTAG_FILTERS = "FTAG_FILTERS";
	private static final String FTAG_LIST = "FTAG_LIST";
	private static final String FTAG_NEXT_LEG = "FTAG_NEXT_LEG";

	//Fragments
	private ResultsFlightListFragment mListFrag;
	private ResultsFlightFiltersFragment mFilterFrag;
	private ResultsFlightDetailsFragment mDetailsFrag;
	private ResultsRecursiveFlightLegsFragment mNextLegFrag;

	//Containers
	private ArrayList<ViewGroup> mContainers = new ArrayList<ViewGroup>();
	private FrameLayoutTouchController mDetailsC;
	private FrameLayoutTouchController mFiltersC;
	private FrameLayoutTouchController mListC;
	private FrameLayoutTouchController mAddToTripC;
	private FrameLayoutTouchController mNextLegC;

	//Other
	private int mLegNumber;
	private GridManager mGrid = new GridManager();
	private StateManager<ResultsFlightLegState> mStateManager;
	private StateListenerCollection<ResultsFlightLegState> mStateListeners;

	private IResultsFlightLegSelected mParentLegSelectedListener;
	private IResultsFlightSelectedListener mParentFlightSelectedListener;


	public ResultsRecursiveFlightLegsFragment() {
		this(0);
	}

	//Constructor
	public ResultsRecursiveFlightLegsFragment(int legNumber) {
		super();
		mLegNumber = legNumber;
		ResultsFlightLegState defaultState = ResultsFlightLegState.LIST_DOWN;
		if (mLegNumber > 0) {
			defaultState = ResultsFlightLegState.FILTERS;
		}
		mStateManager = new StateManager<ResultsFlightLegState>(defaultState, this);
		mStateListeners = new StateListenerCollection<ResultsFlightLegState>(defaultState);
	}

	/**
	 * LIFE CYCLE
	 */

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mParentLegSelectedListener = Ui.findFragmentListener(this, IResultsFlightLegSelected.class, false);
		mParentFlightSelectedListener = Ui.findFragmentListener(this, IResultsFlightSelectedListener.class, false);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_results_recursive_flight_legs, null, false);

		mDetailsC = Ui.findView(view, R.id.details_container);
		mFiltersC = Ui.findView(view, R.id.filters_container);
		mListC = Ui.findView(view, R.id.list_container);
		mAddToTripC = Ui.findView(view, R.id.add_to_trip);
		mNextLegC = Ui.findView(view, R.id.next_leg_container);

		mContainers.add(mDetailsC);
		mContainers.add(mFiltersC);
		mContainers.add(mListC);
		mContainers.add(mAddToTripC);
		mContainers.add(mNextLegC);

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		mMeasurementHelper.registerWithProvider(this);
		registerStateListener(mStateListener, true);
		mBackManager.registerWithParent(this);
		if (mNextLegFrag != null) {
			mNextLegFrag.registerStateListener(mNextLegStateListener, false);
		}
	}

	@Override
	public void onPause() {
		if (mNextLegFrag != null) {
			mNextLegFrag.unRegisterStateListener(mNextLegStateListener);
		}
		mBackManager.unregisterWithParent(this);
		mStateListener.unregisterWithProvider(this);
		mMeasurementHelper.unregisterWithProvider(this);
		super.onPause();
	}

	/**
	 * General  methods
	 */

	public void resetQuery() {
		if (mListFrag != null && mListFrag.isAdded()) {
			mListFrag.resetQuery();
		}
		if (mFilterFrag != null && mFilterFrag.isAdded()) {
			mFilterFrag.onFilterChanged();
		}
		if (mDetailsFrag != null && mDetailsFrag.isAdded()) {
			//TODO: Clear details state
		}
		if (mNextLegFrag != null && mNextLegFrag.isAdded()) {
			mNextLegFrag.resetQuery();
		}
	}

	/*
	 * IResultsFlightSelectedListener
	 */

	@Override
	public void onFlightSelected(int legNumber) {
		Log.d("onFlightSelected mLegNumber:" + mLegNumber + " legNumber:" + legNumber);
		if (legNumber == mLegNumber) {
			if (mStateManager.getState() == ResultsFlightLegState.LIST_DOWN) {
				mStateManager.animateThroughStates(ResultsFlightLegState.FILTERS, ResultsFlightLegState.DETAILS);
			}
			else {
				setState(ResultsFlightLegState.DETAILS, true);
			}
			if (mNextLegFrag != null) {
				mNextLegFrag.resetQuery();
			}

			if (mDetailsFrag != null && mDetailsFrag.isAdded()) {
				mDetailsFrag.bindWithDb();
			}
		}
		if (mParentFlightSelectedListener != null) {
			mParentFlightSelectedListener.onFlightSelected(legNumber);
		}
	}

	/*
	 * IResultsFlightSelectedListener
	 */

	@Override
	public void onTripAdded(int legNumber) {
		Log.d("onTripAdded mLegNumber:" + mLegNumber + " legNumber:" + legNumber);
		if (legNumber == mLegNumber) {
			int totalLegs = Db.getFlightSearch().getSearchParams().getQueryLegCount();
			if (legNumber < (totalLegs - 1)) {
				setState(ResultsFlightLegState.LATER_LEG, true);
			}
			else {
				Db.getTripBucket().clearFlight();
				Db.getTripBucket().add(Db.getFlightSearch().getSearchState());
				Db.saveTripBucket(getActivity());
				setState(ResultsFlightLegState.ADDING_TO_TRIP, true);
			}
		}
		if (mParentLegSelectedListener != null) {
			mParentLegSelectedListener.onTripAdded(legNumber);
		}
	}

	/**
	 * State Listeners
	 */

	private StateListenerHelper<ResultsFlightsListState> mListStateListener = new StateListenerHelper<ResultsFlightsListState>() {
		@Override
		public void onStateTransitionStart(ResultsFlightsListState stateOne, ResultsFlightsListState stateTwo) {
			startStateTransition(translate(stateOne), translate(stateTwo));
		}

		@Override
		public void onStateTransitionUpdate(ResultsFlightsListState stateOne, ResultsFlightsListState stateTwo,
			float percentage) {
			updateStateTransition(translate(stateOne), translate(stateTwo), percentage);
		}

		@Override
		public void onStateTransitionEnd(ResultsFlightsListState stateOne, ResultsFlightsListState stateTwo) {
			endStateTransition(translate(stateOne), translate(stateTwo));
		}

		@Override
		public void onStateFinalized(ResultsFlightsListState state) {
			setState(translate(state), false);
		}

		private ResultsFlightLegState translate(ResultsFlightsListState state) {
			if (state == ResultsFlightsListState.FLIGHTS_LIST_AT_BOTTOM) {
				return ResultsFlightLegState.LIST_DOWN;
			}
			else {
				if (mStateManager.getState() == ResultsFlightLegState.LIST_DOWN) {
					return ResultsFlightLegState.FILTERS;
				}
				else {
					return mStateManager.getState();
				}
			}
		}
	};

	private StateListenerHelper<ResultsFlightLegState> mStateListener = new StateListenerHelper<ResultsFlightLegState>() {
		@Override
		public void onStateTransitionStart(ResultsFlightLegState stateOne, ResultsFlightLegState stateTwo) {

		}

		@Override
		public void onStateTransitionUpdate(ResultsFlightLegState stateOne, ResultsFlightLegState stateTwo,
			float percentage) {

		}

		@Override
		public void onStateTransitionEnd(ResultsFlightLegState stateOne, ResultsFlightLegState stateTwo) {

		}

		@Override
		public void onStateFinalized(ResultsFlightLegState state) {
			updateVisibilitiesForState(state);
			updateFragmentsForState(state);
			updateListForState(state);

			//List and filters state
			if (state == ResultsFlightLegState.LIST_DOWN) {
				mFiltersC.setAlpha(0f);
				if (mListFrag != null && mListFrag.hasList()) {
					mFiltersC.setTranslationY(mListFrag.getMaxDistanceFromTop());
					mListFrag.setPercentage(1f, 0);
				}
			}
			else {
				mFiltersC.setAlpha(1f);
				mFiltersC.setTranslationY(0f);
			}

			//Details state
			if (state == ResultsFlightLegState.DETAILS) {
				setDetailsShowingPercentage(1f);
				mDetailsFrag.bindWithDb();
			}
			else {
				setDetailsShowingPercentage(0f);
			}

			//Next Leg State
			if (state == ResultsFlightLegState.LATER_LEG) {
				setNextLegShowingPercentage(1f);
			}
			else if (mNextLegFrag != null) {
				//If we are showing, the next leg should always be in the filters state.
				mNextLegFrag.setState(ResultsFlightLegState.FILTERS, false);
			}
		}
	};


	private StateListenerHelper<ResultsFlightLegState> mNextLegStateListener = new StateListenerHelper<ResultsFlightLegState>() {
		@Override
		public void onStateTransitionStart(ResultsFlightLegState stateOne, ResultsFlightLegState stateTwo) {

		}

		@Override
		public void onStateTransitionUpdate(ResultsFlightLegState stateOne, ResultsFlightLegState stateTwo,
			float percentage) {

		}

		@Override
		public void onStateTransitionEnd(ResultsFlightLegState stateOne, ResultsFlightLegState stateTwo) {

		}

		@Override
		public void onStateFinalized(ResultsFlightLegState state) {
			if (getState() == ResultsFlightLegState.LATER_LEG && state == ResultsFlightLegState.ADDING_TO_TRIP) {
				setState(ResultsFlightLegState.ADDING_TO_TRIP, false);
			}
		}
	};


	/**
	 * State Helpers
	 */

	public void setState(ResultsFlightLegState state, boolean animate) {
		mStateManager.setState(state, false);
	}

	public ResultsFlightLegState getState() {
		return mStateManager.getState();
	}

	protected void setDetailsShowingPercentage(float percentage) {
		if (percentage == 0) {
			mFiltersC.setTranslationX(0f);
			mListC.setTranslationX(0f);
		}
		else {
			mFiltersC.setTranslationX(percentage * -mGrid.getColRight(0));
			mListC.setTranslationX(percentage * -mGrid.getColLeft(2));
			if (mDetailsFrag != null) {
				int slideInDistance = mGrid.getColSpanWidth(1, 4);
				mDetailsFrag.setDetailsSlideInAnimationState(percentage, slideInDistance, true);
			}
		}
	}

	protected void setNextLegShowingPercentage(float percentage) {
		int transX = mGrid.getColSpanWidth(0, 1);
		mNextLegC.setTranslationX((1f - percentage) * transX);
		mNextLegC.setAlpha(percentage);
	}


	protected void updateListForState(ResultsFlightLegState state) {
		if (mListFrag != null) {
			if (mLegNumber > 0) {
				mListFrag.setListLockedToTop(true);
			}
			else if (state != ResultsFlightLegState.LIST_DOWN && state != ResultsFlightLegState.FILTERS) {
				mListFrag.setListLockedToTop(true);
			}
			else {
				mListFrag.setListLockedToTop(false);
			}

			if (state == ResultsFlightLegState.LIST_DOWN) {
				mListFrag.resetQuery();
			}

			//List scroll position
			mListFrag.unRegisterStateListener(mListStateListener);
			if (state == ResultsFlightLegState.LIST_DOWN) {
				mListFrag.setPercentage(1f, 0);
			}
			else if (mListFrag.hasList() && mListFrag.getPercentage() > 0) {
				mListFrag.setPercentage(0f, 0);
			}
			mListFrag.registerStateListener(mListStateListener, false);
		}
	}

	protected void updateVisibilitiesForState(ResultsFlightLegState state) {
		ArrayList<ViewGroup> visibleViews = new ArrayList<ViewGroup>();

		switch (state) {
		case LIST_DOWN: {
			visibleViews.add(mListC);
			break;
		}
		case FILTERS: {
			visibleViews.add(mListC);
			visibleViews.add(mFiltersC);
			break;
		}
		case DETAILS: {
			visibleViews.add(mListC);
			visibleViews.add(mFiltersC);
			visibleViews.add(mDetailsC);
			break;
		}
		case LATER_LEG: {
			visibleViews.add(mNextLegC);
			break;
		}
		}

		for (ViewGroup vg : mContainers) {
			if (visibleViews.contains(vg)) {
				vg.setVisibility(View.VISIBLE);
			}
			else {
				vg.setVisibility(View.INVISIBLE);
			}
		}
	}

	protected void updateFragmentsForState(ResultsFlightLegState state) {
		FragmentManager manager = getChildFragmentManager();

		//All of the fragment adds/removes come through this method, and we want to make sure our last call
		//is complete before moving forward, so this is important
		manager.executePendingTransactions();

		//We will be adding all of our add/removes to this transaction
		FragmentTransaction transaction = manager.beginTransaction();

		boolean listAvail = false;
		boolean filterAvail = false;
		boolean detailsAvail = false;
		boolean nextLegAvail = false;

		switch (state) {
		case LIST_DOWN: {
			listAvail = true;
			filterAvail = true;
			break;
		}
		case FILTERS: {
			listAvail = true;
			filterAvail = true;
			detailsAvail = true;
			break;
		}
		case DETAILS: {
			listAvail = true;
			filterAvail = true;
			detailsAvail = true;
			break;
		}
		case LATER_LEG: {
			nextLegAvail = true;
			break;
		}
		}

		mListFrag = FragmentAvailabilityUtils
			.setFragmentAvailability(listAvail, FTAG_LIST, manager, transaction, this, R.id.list_container, false);
		mFilterFrag = FragmentAvailabilityUtils
			.setFragmentAvailability(filterAvail, FTAG_FILTERS, manager, transaction, this, R.id.filters_container,
				false);
		mDetailsFrag = FragmentAvailabilityUtils
			.setFragmentAvailability(detailsAvail, FTAG_DETAILS, manager, transaction, this, R.id.details_container,
				true);
		mNextLegFrag = FragmentAvailabilityUtils
			.setFragmentAvailability(nextLegAvail, FTAG_NEXT_LEG, manager, transaction, this, R.id.next_leg_container,
				false);

		transaction.commit();
	}

	/**
	 * IFragmentAvailabilityProvider
	 */

	@Override
	public Fragment getExisitingLocalInstanceFromTag(String tag) {
		if (tag == FTAG_DETAILS) {
			return mDetailsFrag;
		}
		else if (tag == FTAG_FILTERS) {
			return mFilterFrag;
		}
		else if (tag == FTAG_LIST) {
			return mListFrag;
		}
		else if (tag == FTAG_NEXT_LEG) {
			return mNextLegFrag;
		}
		return null;
	}

	@Override
	public Fragment getNewFragmentInstanceFromTag(String tag) {
		if (tag == FTAG_DETAILS) {
			return ResultsFlightDetailsFragment.newInstance(mLegNumber);
		}
		else if (tag == FTAG_FILTERS) {
			return ResultsFlightFiltersFragment.newInstance(mLegNumber);
		}
		else if (tag == FTAG_LIST) {
			return ResultsFlightListFragment.getInstance(mLegNumber);
		}
		else if (tag == FTAG_NEXT_LEG) {
			return ResultsRecursiveFlightLegsFragment.newInstance(mLegNumber + 1);
		}
		return null;
	}

	@Override
	public void doFragmentSetup(String tag, Fragment frag) {
		if (tag == FTAG_DETAILS) {
			updateDetailsFragSizes((ResultsFlightDetailsFragment) frag);
		}
		else if (tag == FTAG_FILTERS) {
			((ResultsFlightFiltersFragment) frag).bindAll();
		}
		else if (tag == FTAG_LIST) {
			ResultsFlightListFragment listFrag = (ResultsFlightListFragment) frag;
			listFrag.registerStateListener(mListStateListener, false);
			if (mLegNumber == 0) {
				listFrag.setTopRightTextButtonText(getString(R.string.Done));
			}
			else {
				listFrag.setTopRightTextButtonEnabled(false);
				listFrag.setPercentage(0f, 0);
				listFrag.setListLockedToTop(true);
			}
		}
		else if (tag == FTAG_NEXT_LEG) {
			((ResultsRecursiveFlightLegsFragment) frag).registerStateListener(mNextLegStateListener, false);
		}
	}

	/**
	 * Fragment Helpers
	 */

	private void updateDetailsFragSizes(ResultsFlightDetailsFragment frag) {
		if (frag != null && mGrid.getTotalWidth() > 0) {
			int actionbarHeight = getActivity().getActionBar().getHeight();
			int leftCol = 2;
			int rightCol = 4;

			Rect position = new Rect();
			position.left = mGrid.getColLeft(leftCol);
			position.right = mGrid.getColRight(rightCol);
			position.top = 0;
			position.bottom = mGrid.getTotalHeight() - actionbarHeight;
			frag.setDefaultDetailsPositionAndDimensions(position, DETAILS_MARGIN_PERCENTAGE);
		}

		if (frag != null && mListFrag != null && mListFrag.hasList()) {
			FruitList list = (FruitList) mListFrag.getListView();
			frag.setDetaultRowDimensions(mGrid.getColWidth(1), list.getRowHeight(false));
		}
	}

	/*
	 * MEASUREMENT LISTENER
	 */

	private MeasurementHelper mMeasurementHelper = new MeasurementHelper() {

		@Override
		public void onContentSizeUpdated(int totalWidth, int totalHeight, boolean isLandscape) {
			mGrid.setDimensions(totalWidth, totalHeight);
			mGrid.setGridSize(3, 5);

			//The top row matches the height of the actionbar
			mGrid.setRowSize(0, getActivity().getActionBar().getHeight());

			//The bottom row
			mGrid.setRowPercentage(2, .50f);

			//These columns are just the spacers between content columns
			int spacerSize = getResources().getDimensionPixelSize(R.dimen.results_column_spacing);
			mGrid.setColumnSize(1, spacerSize);
			mGrid.setColumnSize(3, spacerSize);

			//Horizontal alignment
			mGrid.setContainerToColumn(mFiltersC, 0);
			mGrid.setContainerToColumn(mListC, 2);
			mGrid.setContainerToColumnSpan(mDetailsC, 0, 4);

			//Vertical alignment

			//Most content sits in rows 1 and 2 (below the actionbar)
			mGrid.setContainerToRowSpan(mFiltersC, 1, 2);
			mGrid.setContainerToRowSpan(mListC, 1, 2);
			mGrid.setContainerToRowSpan(mDetailsC, 1, 2);

			//Frag stuff
			updateDetailsFragSizes(mDetailsFrag);
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
			ResultsFlightLegState state = mStateManager.getState();
			if (mStateManager.isAnimating()) {
				setState(state, true);
				return true;
			}
			else {
				if (state == ResultsFlightLegState.LATER_LEG) {
					if (mNextLegFrag != null && mNextLegFrag.getState() == ResultsFlightLegState.FILTERS) {
						setState(ResultsFlightLegState.DETAILS, true);
						return true;
					}
					return false;
				}
				else if (state == ResultsFlightLegState.DETAILS) {
					setState(ResultsFlightLegState.FILTERS, true);
					return true;
				}
				else if (state == ResultsFlightLegState.FILTERS) {
					if (mLegNumber == 0) {
						setState(ResultsFlightLegState.LIST_DOWN, true);
						return true;
					}
				}
			}
			return false;
		}
	};

	/**
	 * ResultsFlightLegState STATE PROVIDER
	 */

	@Override
	public void startStateTransition(ResultsFlightLegState stateOne, ResultsFlightLegState stateTwo) {
		mStateListeners.startStateTransition(stateOne, stateTwo);
	}

	@Override
	public void updateStateTransition(ResultsFlightLegState stateOne, ResultsFlightLegState stateTwo,
		float percentage) {
		mStateListeners.updateStateTransition(stateOne, stateTwo, percentage);
	}

	@Override
	public void endStateTransition(ResultsFlightLegState stateOne, ResultsFlightLegState stateTwo) {
		mStateListeners.endStateTransition(stateOne, stateTwo);
	}

	@Override
	public void finalizeState(ResultsFlightLegState state) {
		mStateListeners.finalizeState(state);
	}

	@Override
	public void registerStateListener(IStateListener<ResultsFlightLegState> listener, boolean fireFinalizeState) {
		mStateListeners.registerStateListener(listener, fireFinalizeState);
	}

	@Override
	public void unRegisterStateListener(IStateListener<ResultsFlightLegState> listener) {
		mStateListeners.unRegisterStateListener(listener);
	}
}
