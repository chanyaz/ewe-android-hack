package com.expedia.bookings.fragment;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
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


	/**
	 * LOADING STATE MANAGEMENT
	 */

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
			}
			else if (stateOne == ResultsLoadingState.ALL && stateTwo == ResultsLoadingState.HOTELS) {
				positionTextLabel(percentage, true);
			}
		}

		@Override
		public void onStateTransitionEnd(ResultsLoadingState stateOne, ResultsLoadingState stateTwo) {

		}

		@Override
		public void onStateFinalized(ResultsLoadingState state) {
			setLoadingTextForState(state);
			setBackgroundForState(state);
			positionTextForState(state);
		}
	};

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
