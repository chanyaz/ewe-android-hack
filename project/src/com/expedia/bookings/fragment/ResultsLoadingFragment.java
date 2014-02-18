package com.expedia.bookings.fragment;

import android.annotation.TargetApi;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.enums.ResultsFlightsState;
import com.expedia.bookings.enums.ResultsHotelsState;
import com.expedia.bookings.enums.ResultsLoadingState;
import com.expedia.bookings.interfaces.IStateListener;
import com.expedia.bookings.interfaces.IStateProvider;
import com.expedia.bookings.interfaces.helpers.StateListenerCollection;
import com.expedia.bookings.interfaces.helpers.StateListenerHelper;
import com.expedia.bookings.interfaces.helpers.StateManager;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.FrameLayoutTouchController;

/**
 * Results loading fragment for Tablet
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class ResultsLoadingFragment extends Fragment implements IStateProvider<ResultsLoadingState> {

	private StateManager<ResultsLoadingState> mStateManager = new StateManager<ResultsLoadingState>(ResultsLoadingState.ALL, this);
	private View mRootC;
	private LinearLayout mTextC;

	private TextView mLoadingTv;
	private TextView mHotelsTv;
	private TextView mFlightsTv;
	private TextView mAndTv;
	private TextView mEllipsisTv;

	private FrameLayoutTouchController mBgLeft;
	private FrameLayoutTouchController mBgRight;

	//loading anim vars
	private int mLoadingUpdateInterval = 250;
	private int mLoadingNumber = 0;
	private int mLoadingColorDark = Color.DKGRAY;
	private int mLoadingColorLight = Color.LTGRAY;
	private Runnable mLoadingAnimRunner;
	private ViewGroup mLoadingLeft;
	private ViewGroup mLoadingRight;


	public static ResultsLoadingFragment newInstance() {
		ResultsLoadingFragment frag = new ResultsLoadingFragment();
		return frag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootC = inflater.inflate(R.layout.fragment_results_loading, null);

		mTextC = Ui.findView(mRootC, R.id.loading_text_container);

		mLoadingTv = Ui.findView(mTextC, R.id.loading_tv);
		mHotelsTv = Ui.findView(mTextC, R.id.hotels_tv);
		mFlightsTv = Ui.findView(mTextC, R.id.flights_tv);
		mAndTv = Ui.findView(mTextC, R.id.and_tv);
		mEllipsisTv = Ui.findView(mTextC, R.id.ellipsis_tv);

		mBgLeft = Ui.findView(mRootC, R.id.bg_left);
		mBgRight = Ui.findView(mRootC, R.id.bg_right);

		mLoadingLeft = Ui.findView(mRootC, R.id.loading_left_container);
		mLoadingRight = Ui.findView(mRootC, R.id.loading_right_container);

		registerStateListener(mStateHelper, false);

		return mRootC;
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	private void setLoadingAnimationEnabled(boolean loading) {
		if (!loading) {
			mLoadingAnimRunner = null;
		}
		else if (mRootC != null) {
			mLoadingAnimRunner = new Runnable() {
				@Override
				public void run() {
					if (this == mLoadingAnimRunner && mRootC != null && getActivity() != null) {
						loadingAnimUpdate();
						mRootC.postDelayed(this, mLoadingUpdateInterval);
					}
				}
			};
			mRootC.post(mLoadingAnimRunner);
		}
	}

	private void loadingAnimUpdate() {
		if (mLoadingLeft != null && mLoadingRight != null) {
			mLoadingNumber++;
			int leftDarkInd = -1;
			int rightDarkInd = -1;

			ResultsLoadingState state = mStateManager.getState();
			switch (state) {
			case ALL: {
				mLoadingNumber = mLoadingNumber % (mLoadingLeft.getChildCount() + mLoadingRight.getChildCount());
				if (mLoadingNumber < mLoadingRight.getChildCount()) {
					rightDarkInd = mLoadingNumber;
				}
				else {
					leftDarkInd = mLoadingLeft.getChildCount() - 1 - (mLoadingNumber % mLoadingLeft.getChildCount());
				}
				break;
			}
			case FLIGHTS: {
				mLoadingNumber = mLoadingNumber % (mLoadingRight.getChildCount() * 2 - 2);
				if (mLoadingNumber < mLoadingRight.getChildCount()) {
					rightDarkInd = mLoadingNumber;
				}
				else {
					int maxInd = mLoadingRight.getChildCount() - 1;
					rightDarkInd = maxInd - (mLoadingNumber % maxInd);
				}
				break;
			}
			case HOTELS: {
				mLoadingNumber = mLoadingNumber % (mLoadingLeft.getChildCount() * 2 - 2);
				if (mLoadingNumber < mLoadingLeft.getChildCount()) {
					leftDarkInd = mLoadingNumber;
				}
				else {
					int maxInd = mLoadingLeft.getChildCount() - 1;
					leftDarkInd = maxInd - (mLoadingNumber % maxInd);
				}
				break;
			}
			}

			for (int i = 0; i < mLoadingLeft.getChildCount(); i++) {
				mLoadingLeft.getChildAt(i).setBackgroundColor(i == leftDarkInd ? mLoadingColorDark : mLoadingColorLight);
			}
			for (int i = 0; i < mLoadingRight.getChildCount(); i++) {
				mLoadingRight.getChildAt(i).setBackgroundColor(i == rightDarkInd ? mLoadingColorDark : mLoadingColorLight);
			}
		}
	}


	/**
	 * LISTEN TO HOTELS STATE
	 */

	private ResultsHotelsState mHotelsState = ResultsHotelsState.LOADING;

	public IStateListener<ResultsHotelsState> getHotelsStateListener() {
		return mHotelsStateHelper;
	}

	private StateListenerHelper<ResultsHotelsState> mHotelsStateHelper = new StateListenerHelper<ResultsHotelsState>() {

		@Override
		public void onStateTransitionStart(ResultsHotelsState stateOne, ResultsHotelsState stateTwo) {

		}

		@Override
		public void onStateTransitionUpdate(ResultsHotelsState stateOne, ResultsHotelsState stateTwo, float percentage) {
			if (mStateManager.getState() == ResultsLoadingState.FLIGHTS) {
				if (stateOne == ResultsHotelsState.HOTEL_LIST_DOWN && stateTwo == ResultsHotelsState.HOTEL_LIST_UP) {
					mRootC.setAlpha(1f - percentage);
				}
				else if (stateOne == ResultsHotelsState.HOTEL_LIST_UP && stateTwo == ResultsHotelsState.HOTEL_LIST_DOWN) {
					mRootC.setAlpha(percentage);
				}
			}
		}

		@Override
		public void onStateTransitionEnd(ResultsHotelsState stateOne, ResultsHotelsState stateTwo) {

		}

		@Override
		public void onStateFinalized(ResultsHotelsState state) {
			mHotelsState = state;
			setState(determineState(), true);
		}
	};

	/**
	 * LISTEN TO FLIGHTS STATE
	 */
	private ResultsFlightsState mFlightsState = ResultsFlightsState.LOADING;

	public IStateListener<ResultsFlightsState> getFlightsStateListener() {
		return mFlightsStateHelper;
	}

	private StateListenerHelper<ResultsFlightsState> mFlightsStateHelper = new StateListenerHelper<ResultsFlightsState>() {
		@Override
		public void onStateTransitionStart(ResultsFlightsState stateOne, ResultsFlightsState stateTwo) {

		}

		@Override
		public void onStateTransitionUpdate(ResultsFlightsState stateOne, ResultsFlightsState stateTwo, float percentage) {
			if (mStateManager.getState() == ResultsLoadingState.HOTELS) {
				if (stateOne == ResultsFlightsState.FLIGHT_LIST_DOWN && stateTwo == ResultsFlightsState.FLIGHT_ONE_FILTERS) {
					mRootC.setAlpha(1f - percentage);
				}
				else if (stateOne == ResultsFlightsState.FLIGHT_ONE_FILTERS && stateTwo == ResultsFlightsState.FLIGHT_LIST_DOWN) {
					mRootC.setAlpha(percentage);
				}
			}
		}

		@Override
		public void onStateTransitionEnd(ResultsFlightsState stateOne, ResultsFlightsState stateTwo) {

		}

		@Override
		public void onStateFinalized(ResultsFlightsState state) {
			mFlightsState = state;
			setState(determineState(), true);
		}
	};


	/**
	 * LOADING STATE MANAGEMENT
	 */

	public ResultsLoadingState determineState() {
		return determineState(mFlightsState, mHotelsState);
	}

	public ResultsLoadingState determineState(ResultsFlightsState flightsState, ResultsHotelsState hotelsState) {
		if (flightsState == ResultsFlightsState.LOADING && hotelsState == ResultsHotelsState.LOADING) {
			return ResultsLoadingState.ALL;
		}
		else if (flightsState == ResultsFlightsState.LOADING && hotelsState != ResultsHotelsState.LOADING) {
			return ResultsLoadingState.FLIGHTS;
		}
		else if (flightsState != ResultsFlightsState.LOADING && hotelsState == ResultsHotelsState.LOADING) {
			return ResultsLoadingState.HOTELS;
		}
		return ResultsLoadingState.NONE;
	}

	public void setState(ResultsLoadingState state, boolean animate) {
		mStateManager.setState(state, animate);
	}

	private StateListenerHelper<ResultsLoadingState> mStateHelper = new StateListenerHelper<ResultsLoadingState>() {

		@Override
		public void onStateTransitionStart(ResultsLoadingState stateOne, ResultsLoadingState stateTwo) {

		}

		@Override
		public void onStateTransitionUpdate(ResultsLoadingState stateOne, ResultsLoadingState stateTwo, float percentage) {
			if (stateOne == ResultsLoadingState.ALL && stateTwo == ResultsLoadingState.FLIGHTS) {
				positionTextLabel(percentage, false);
				positionRightLoadingBar(percentage);
			}
			else if (stateOne == ResultsLoadingState.ALL && stateTwo == ResultsLoadingState.HOTELS) {
				positionTextLabel(percentage, true);
				positionLeftLoadingBar(percentage);
			}
		}

		@Override
		public void onStateTransitionEnd(ResultsLoadingState stateOne, ResultsLoadingState stateTwo) {

		}

		@Override
		public void onStateFinalized(ResultsLoadingState state) {
			if (state == ResultsLoadingState.NONE) {
				setLoadingAnimationEnabled(false);
				mRootC.setVisibility(View.GONE);
			}
			else {
				mRootC.setVisibility(View.VISIBLE);
				mRootC.setAlpha(1f);
				setLoadingTextForState(state);
				setBackgroundForState(state);
				positionTextForState(state);
				positionLoadingBarsForState(state);
				setLoadingBarVisibilityForState(state);
				setLoadingAnimationEnabled(true);
			}
		}
	};

	protected void positionLoadingBarsForState(ResultsLoadingState state) {
		if (state == ResultsLoadingState.FLIGHTS) {
			positionRightLoadingBar(1f);
		}
		else if (state == ResultsLoadingState.HOTELS) {
			positionLeftLoadingBar(1f);
		}
		else {
			positionLeftLoadingBar(0f);
			positionRightLoadingBar(0f);
		}
	}

	protected void positionTextForState(ResultsLoadingState state) {
		if (state == ResultsLoadingState.ALL) {
			positionTextLabel(0f, false);
		}
		else if (state == ResultsLoadingState.FLIGHTS) {
			positionTextLabel(1f, false);
		}
		else if (state == ResultsLoadingState.HOTELS) {
			positionTextLabel(1f, true);
		}
	}

	protected void setLoadingTextForState(ResultsLoadingState state) {
		if (state == ResultsLoadingState.ALL) {
			mHotelsTv.setVisibility(View.VISIBLE);
			mFlightsTv.setVisibility(View.VISIBLE);
			mAndTv.setVisibility(View.VISIBLE);
		}
		else {
			mAndTv.setVisibility(View.GONE);
			if (state == ResultsLoadingState.FLIGHTS) {
				mHotelsTv.setVisibility(View.GONE);
				mFlightsTv.setVisibility(View.VISIBLE);
			}
			else if (state == ResultsLoadingState.HOTELS) {
				mHotelsTv.setVisibility(View.VISIBLE);
				mFlightsTv.setVisibility(View.GONE);
			}
		}
	}

	protected void setBackgroundForState(ResultsLoadingState state) {
		if (state == ResultsLoadingState.ALL) {
			setBgShowing(mBgLeft, true);
			setBgShowing(mBgRight, true);
		}
		else if (state == ResultsLoadingState.FLIGHTS) {
			setBgShowing(mBgLeft, false);
			setBgShowing(mBgRight, true);
		}
		else if (state == ResultsLoadingState.HOTELS) {
			setBgShowing(mBgLeft, true);
			setBgShowing(mBgRight, false);
		}
	}

	protected void positionTextLabel(float percentage, boolean toLeft) {
		if (percentage == 0) {
			mTextC.setTranslationX(0f);
		}
		else {
			float rootQuarter = mRootC.getWidth() / 4f;
			mTextC.setTranslationX(percentage * (toLeft ? -rootQuarter : rootQuarter));
		}
	}

	protected void setLoadingBarVisibilityForState(ResultsLoadingState state) {
		if (state == ResultsLoadingState.ALL) {
			mLoadingLeft.setVisibility(View.VISIBLE);
			mLoadingRight.setVisibility(View.VISIBLE);
		}
		else if (state == ResultsLoadingState.FLIGHTS) {
			mLoadingLeft.setVisibility(View.INVISIBLE);
			mLoadingRight.setVisibility(View.VISIBLE);
		}
		else if (state == ResultsLoadingState.HOTELS) {
			mLoadingLeft.setVisibility(View.VISIBLE);
			mLoadingRight.setVisibility(View.INVISIBLE);
		}
		else {
			mLoadingLeft.setVisibility(View.INVISIBLE);
			mLoadingRight.setVisibility(View.INVISIBLE);
		}
	}

	protected void positionLeftLoadingBar(float percentage) {
		if (percentage == 0) {
			mLoadingLeft.setTranslationX(0);
		}
		else {
			float rootQuarter = mRootC.getWidth() / 4f;
			float loadingHalf = mLoadingLeft.getWidth() / 2f;
			float untranslatedCenterX = mLoadingLeft.getLeft() + loadingHalf;
			float destTranslation = rootQuarter - untranslatedCenterX;
			mLoadingLeft.setTranslationX(percentage * destTranslation);
		}
	}

	protected void positionRightLoadingBar(float percentage) {
		if (percentage == 0) {
			mLoadingRight.setTranslationX(0);
		}
		else {
			float rootQuarter = mRootC.getWidth() / 4f;
			float loadingHalf = mLoadingRight.getWidth() / 2f;
			float untranslatedCenterX = mLoadingRight.getLeft() + loadingHalf;
			float destTranslation = (3 * rootQuarter) - untranslatedCenterX;
			mLoadingRight.setTranslationX(percentage * destTranslation);
		}
	}

	private void setBgShowing(FrameLayoutTouchController bg, boolean showing) {
		bg.setVisibility(showing ? View.VISIBLE : View.INVISIBLE);
		bg.setBlockNewEventsEnabled(showing);
	}

	/**
	 * STATE PROVIDER STUFF
	 */

	private StateListenerCollection<ResultsLoadingState> mStateListeners = new StateListenerCollection<ResultsLoadingState>(mStateManager.getState());

	@Override
	public void startStateTransition(ResultsLoadingState stateOne, ResultsLoadingState stateTwo) {
		mStateListeners.startStateTransition(stateOne, stateTwo);
	}

	@Override
	public void updateStateTransition(ResultsLoadingState stateOne, ResultsLoadingState stateTwo, float percentage) {
		mStateListeners.updateStateTransition(stateOne, stateTwo, percentage);
	}

	@Override
	public void endStateTransition(ResultsLoadingState stateOne, ResultsLoadingState stateTwo) {
		mStateListeners.endStateTransition(stateOne, stateTwo);
	}

	@Override
	public void finalizeState(ResultsLoadingState state) {
		mStateListeners.finalizeState(state);
	}

	@Override
	public void registerStateListener(IStateListener<ResultsLoadingState> listener, boolean fireFinalizeState) {
		mStateListeners.registerStateListener(listener, fireFinalizeState);
	}

	@Override
	public void unRegisterStateListener(IStateListener<ResultsLoadingState> listener) {
		mStateListeners.unRegisterStateListener(listener);
	}
}
