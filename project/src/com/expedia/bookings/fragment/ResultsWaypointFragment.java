package com.expedia.bookings.fragment;

import android.annotation.TargetApi;

import com.expedia.bookings.enums.ResultsSearchState;
import com.expedia.bookings.enums.WaypointChooserState;
import com.expedia.bookings.interfaces.helpers.StateListenerHelper;

/**
 * A large search fragment only suitable for tablet sizes.
 */
@TargetApi(14)
public class ResultsWaypointFragment extends TabletWaypointFragment {

	@Override
	public void onResume() {
		super.onResume();
		mSearchStateListener.registerWithProvider(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		mSearchStateListener.unregisterWithProvider(this);
	}

	///////////////////////////////////////////////////////////////////////////
	///// StateListenerHelper<ResultsSearchState>

	private StateListenerHelper<ResultsSearchState> mSearchStateListener = new StateListenerHelper<ResultsSearchState>() {

		@Override
		public void onStateTransitionStart(ResultsSearchState stateOne, ResultsSearchState stateTwo) {
			startStateTransition(translateState(stateOne), translateState(stateTwo));
		}

		@Override
		public void onStateTransitionUpdate(ResultsSearchState stateOne, ResultsSearchState stateTwo,
			float percentage) {
			updateStateTransition(translateState(stateOne), translateState(stateTwo), percentage);
		}

		@Override
		public void onStateTransitionEnd(ResultsSearchState stateOne, ResultsSearchState stateTwo) {
			endStateTransition(translateState(stateOne), translateState(stateTwo));
		}

		@Override
		public void onStateFinalized(ResultsSearchState state) {
			if (state == ResultsSearchState.FLIGHT_ORIGIN) {
				updateViewsForOrigin();
			}
			else if (state == ResultsSearchState.DESTINATION) {
				updateViewsForDestination();
			}
			setState(translateState(state), false);
		}

		private WaypointChooserState translateState(ResultsSearchState state) {
			if (state == ResultsSearchState.FLIGHT_ORIGIN || state == ResultsSearchState.DESTINATION) {
				return WaypointChooserState.VISIBLE;
			}
			return WaypointChooserState.HIDDEN;
		}
	};
}
