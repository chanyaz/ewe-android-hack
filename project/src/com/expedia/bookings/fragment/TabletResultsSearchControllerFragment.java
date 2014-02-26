package com.expedia.bookings.fragment;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
import com.expedia.bookings.interfaces.helpers.StateListenerLogger;
import com.expedia.bookings.interfaces.helpers.StateManager;
import com.expedia.bookings.utils.GridManager;
import com.expedia.bookings.widget.FrameLayoutTouchController;
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
	private FrameLayoutTouchController mTopHalfC;
	private FrameLayoutTouchController mSearchBarC;
	private ViewGroup mRightButtonsC;
	private FrameLayoutTouchController mWidgetC;

	//Search action buttons
	private TextView mDestBtn;
	private TextView mOrigBtn;
	private TextView mCalBtn;
	private TextView mTravBtn;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_tablet_results_search, null, false);

		mRootC = Ui.findView(view, R.id.root_layout);
		mTopHalfC = Ui.findView(view, R.id.top_half_container);
		mSearchBarC = Ui.findView(view, R.id.search_bar_conatiner);
		mRightButtonsC = Ui.findView(view, R.id.right_buttons_container);
		mWidgetC = Ui.findView(view, R.id.widget_container);

		mDestBtn = Ui.findView(view, R.id.dest_btn);
		mOrigBtn = Ui.findView(view, R.id.origin_btn);
		mCalBtn = Ui.findView(view, R.id.calendar_btn);
		mTravBtn = Ui.findView(view, R.id.traveler_btn);

		registerStateListener(mSearchStateHelper, false);
		registerStateListener(new StateListenerLogger<ResultsSearchState>(), false);

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
	 * SEARCH STATE LISTENER
	 */

	private StateListenerHelper<ResultsSearchState> mSearchStateHelper = new StateListenerHelper<ResultsSearchState>() {
		@Override
		public void onStateTransitionStart(ResultsSearchState stateOne, ResultsSearchState stateTwo) {

		}

		@Override
		public void onStateTransitionUpdate(ResultsSearchState stateOne, ResultsSearchState stateTwo, float percentage) {
			boolean goingUp = (stateOne == ResultsSearchState.DEFAULT && (stateTwo == ResultsSearchState.FLIGHTS_UP || stateTwo == ResultsSearchState.HOTELS_UP));
			boolean goingDown = ((stateOne == ResultsSearchState.FLIGHTS_UP || stateOne == ResultsSearchState.HOTELS_UP) && stateTwo == ResultsSearchState.DEFAULT);

			if (goingUp || goingDown) {
				float perc = goingUp ? percentage : (1f - percentage);
				setSlideUpAnimationPercentage(perc);
				if (stateOne == ResultsSearchState.HOTELS_UP || stateTwo == ResultsSearchState.HOTELS_UP) {
					//For hotels we also fade
					setSlideUpHotelsOnlyAnimationPercentage(perc);
				}
			}
		}

		@Override
		public void onStateTransitionEnd(ResultsSearchState stateOne, ResultsSearchState stateTwo) {

		}

		@Override
		public void onStateFinalized(ResultsSearchState state) {
			switch (state) {
			case HOTELS_UP: {
				setSlideUpAnimationPercentage(1f);
				setSlideUpHotelsOnlyAnimationPercentage(1f);
				break;
			}
			case FLIGHTS_UP: {
				setSlideUpAnimationPercentage(1f);
				setSlideUpHotelsOnlyAnimationPercentage(0f);
				break;
			}
			default: {
				setSlideUpAnimationPercentage(0f);
				setSlideUpHotelsOnlyAnimationPercentage(0f);
			}
			}
		}

		private void setSlideUpAnimationPercentage(float percentage) {
			int barTransDistance = mTopHalfC.getHeight() - mSearchBarC.getHeight();
			mSearchBarC.setTranslationY(percentage * -barTransDistance);
			mWidgetC.setTranslationY(percentage * mWidgetC.getHeight());
			mDestBtn.setTranslationY(percentage * mSearchBarC.getHeight());
			mDestBtn.setAlpha(1f - percentage);
			//TODO: Use better number (this is to move to the left of the action bar buttons)
			mRightButtonsC.setTranslationX(percentage * -(getActivity().getActionBar().getHeight()));
		}

		private void setSlideUpHotelsOnlyAnimationPercentage(float percentage) {
			mOrigBtn.setAlpha(1f - percentage);
		}
	};


	/*
	 * RESULTS STATE LISTENER
	 */

	public StateListenerHelper<ResultsState> getResultsListener() {
		return mResultsStateHelper;
	}

	private StateListenerHelper<ResultsState> mResultsStateHelper = new StateListenerHelper<ResultsState>() {

		@Override
		public void onStateTransitionStart(ResultsState stateOne, ResultsState stateTwo) {
			startStateTransition(translateState(stateOne), translateState(stateTwo));
		}

		@Override
		public void onStateTransitionUpdate(ResultsState stateOne, ResultsState stateTwo, float percentage) {
			updateStateTransition(translateState(stateOne), translateState(stateTwo), percentage);
		}

		@Override
		public void onStateTransitionEnd(ResultsState stateOne, ResultsState stateTwo) {
			endStateTransition(translateState(stateOne), translateState(stateTwo));
		}

		@Override
		public void onStateFinalized(ResultsState state) {
			finalizeState(translateState(state));
		}

		public ResultsSearchState translateState(ResultsState state) {
			switch (state) {
			case FLIGHTS: {
				return ResultsSearchState.FLIGHTS_UP;
			}
			case HOTELS: {
				return ResultsSearchState.HOTELS_UP;
			}
			default: {
				return ResultsSearchState.DEFAULT;
			}
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
			mGrid.setNumRows(2);
			mGrid.setNumCols(3);

			mGrid.setContainerToRow(mTopHalfC, 0);
			mGrid.setContainerToRow(mWidgetC, 1);
			mGrid.setContainerToColumn(mWidgetC, 2);
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