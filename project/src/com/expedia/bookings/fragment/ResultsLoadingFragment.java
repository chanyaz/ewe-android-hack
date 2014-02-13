package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.enums.ResultsLoadingState;
import com.expedia.bookings.interfaces.IStateListener;
import com.expedia.bookings.interfaces.IStateProvider;
import com.expedia.bookings.interfaces.helpers.StateListenerCollection;
import com.expedia.bookings.interfaces.helpers.StateListenerHelper;
import com.expedia.bookings.interfaces.helpers.StateManager;
import com.expedia.bookings.utils.Ui;

/**
 * Results loading fragment for Tablet
 */
public class ResultsLoadingFragment extends Fragment implements IStateProvider<ResultsLoadingState> {

	private StateManager<ResultsLoadingState> mStateManager = new StateManager<ResultsLoadingState>(ResultsLoadingState.ALL, this);
	private TextView mLoadingTv;

	public static ResultsLoadingFragment newInstance() {
		ResultsLoadingFragment frag = new ResultsLoadingFragment();
		return frag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_results_loading, null);
		mLoadingTv = Ui.findView(view,R.id.loading_tv);


		registerStateListener(mStateHelper, false);

		return view;
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

	public void setState(ResultsLoadingState state, boolean animate){
		mStateManager.setState(state, animate);
	}

	private StateListenerHelper<ResultsLoadingState> mStateHelper = new StateListenerHelper<ResultsLoadingState>() {

		@Override
		public void onStateTransitionStart(ResultsLoadingState stateOne, ResultsLoadingState stateTwo) {

		}

		@Override
		public void onStateTransitionUpdate(ResultsLoadingState stateOne, ResultsLoadingState stateTwo, float percentage) {

		}

		@Override
		public void onStateTransitionEnd(ResultsLoadingState stateOne, ResultsLoadingState stateTwo) {

		}

		@Override
		public void onStateFinalized(ResultsLoadingState state) {
			setLoadingTextForState(state);
		}
	};

	protected void setLoadingTextForState(ResultsLoadingState state){
		if(state == ResultsLoadingState.ALL){
			mLoadingTv.setText(R.string.loading);
		}else if(state == ResultsLoadingState.FLIGHTS){
			mLoadingTv.setText(R.string.loading_flights);
		}else if(state == ResultsLoadingState.HOTELS){
			mLoadingTv.setText(R.string.loading_hotels);
		}
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
