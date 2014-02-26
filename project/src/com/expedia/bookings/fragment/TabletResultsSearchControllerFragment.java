package com.expedia.bookings.fragment;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.enums.ResultsSearchState;
import com.expedia.bookings.enums.ResultsState;
import com.expedia.bookings.interfaces.IBackManageable;
import com.expedia.bookings.interfaces.IStateListener;
import com.expedia.bookings.interfaces.IStateProvider;
import com.expedia.bookings.interfaces.helpers.BackManager;
import com.expedia.bookings.interfaces.helpers.MeasurementHelper;
import com.expedia.bookings.interfaces.helpers.StateListenerCollection;
import com.expedia.bookings.interfaces.helpers.StateListenerHelper;
import com.expedia.bookings.interfaces.helpers.StateManager;
import com.expedia.bookings.utils.GridManager;
import com.mobiata.android.util.Ui;

/**
 * TabletResultsSearchControllerFragment: designed for tablet results 2014
 * This controls all the fragments relating to searching on the results screen
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class TabletResultsSearchControllerFragment extends Fragment implements IBackManageable, IStateProvider<ResultsSearchState> {

	private GridManager mGrid = new GridManager();
	private StateManager<ResultsSearchState> mSearchStateManager = new StateManager<ResultsSearchState>(ResultsSearchState.DEFAULT, this);

	//Containers
	private ViewGroup mRootC;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_tablet_results_search, null, false);
		mRootC = Ui.findView(view, R.id.root_layout);
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		mResultsStateHelper.registerWithProvider(this);
		mMeasurementHelper.registerWithProvider(this);
		mBackManager.registerWithParent(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		mResultsStateHelper.unregisterWithProvider(this);
		mMeasurementHelper.unregisterWithProvider(this);
		mBackManager.unregisterWithParent(this);
	}


		/*
	 * RESULTS STATE LISTENER
	 */

	public StateListenerHelper<ResultsState> getResultsListener() {
		return mResultsStateHelper;
	}

	private StateListenerHelper<ResultsState> mResultsStateHelper = new StateListenerHelper<ResultsState>() {

		@Override
		public void onStateTransitionStart(ResultsState stateOne, ResultsState stateTwo) {
			if (stateOne == ResultsState.OVERVIEW && stateTwo == ResultsState.HOTELS) {

			}
			else if (stateOne == ResultsState.HOTELS && stateTwo == ResultsState.OVERVIEW) {

			}
			else if (stateOne == ResultsState.FLIGHTS && stateTwo == ResultsState.OVERVIEW) {

			}
			else if (stateOne == ResultsState.OVERVIEW && stateTwo == ResultsState.FLIGHTS) {

			}
		}

		@Override
		public void onStateTransitionUpdate(ResultsState stateOne, ResultsState stateTwo, float percentage) {
			if (stateOne == ResultsState.OVERVIEW && stateTwo == ResultsState.HOTELS) {

			}
			else if (stateOne == ResultsState.HOTELS && stateTwo == ResultsState.OVERVIEW) {

			}
			else if (stateOne == ResultsState.OVERVIEW && stateTwo == ResultsState.FLIGHTS) {

			}
			else if (stateOne == ResultsState.FLIGHTS && stateTwo == ResultsState.OVERVIEW) {

			}
		}

		@Override
		public void onStateTransitionEnd(ResultsState stateOne, ResultsState stateTwo) {
			if (stateOne == ResultsState.OVERVIEW && stateTwo == ResultsState.HOTELS) {

			}
			else if (stateOne == ResultsState.HOTELS && stateTwo == ResultsState.OVERVIEW) {

			}
			else if (stateOne == ResultsState.OVERVIEW && stateTwo == ResultsState.FLIGHTS) {

			}
			else if (stateOne == ResultsState.FLIGHTS && stateTwo == ResultsState.OVERVIEW) {

			}
		}

		@Override
		public void onStateFinalized(ResultsState state) {

		}

	};


	/*
	 * MEASUREMENT LISTENER
	 */

	private MeasurementHelper mMeasurementHelper = new MeasurementHelper() {
		@Override
		public void onContentSizeUpdated(int totalWidth, int totalHeight, boolean isLandscape) {
			mGrid.setDimensions(totalWidth, totalHeight);
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


	/*
	 * RESULTS SEARCH STATE PROVIDER
	 */

	private StateListenerCollection<ResultsSearchState> mLis = new StateListenerCollection<ResultsSearchState>(mSearchStateManager.getState());

	@Override
	public void startStateTransition(ResultsSearchState stateOne, ResultsSearchState stateTwo) {
		mLis.startStateTransition(stateOne, stateTwo);
	}

	@Override
	public void updateStateTransition(ResultsSearchState stateOne, ResultsSearchState stateTwo, float percentage) {
		mLis.updateStateTransition(stateOne, stateTwo, percentage);
	}

	@Override
	public void endStateTransition(ResultsSearchState stateOne, ResultsSearchState stateTwo) {
		mLis.endStateTransition(stateOne, stateTwo);
	}

	@Override
	public void finalizeState(ResultsSearchState state) {
		mLis.finalizeState(state);
	}

	@Override
	public void registerStateListener(IStateListener<ResultsSearchState> listener, boolean fireFinalizeState) {
		mLis.registerStateListener(listener, fireFinalizeState);
	}

	@Override
	public void unRegisterStateListener(IStateListener<ResultsSearchState> listener) {
		mLis.unRegisterStateListener(listener);
	}
}