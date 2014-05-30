package com.expedia.bookings.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.TripBucketItemFlight;
import com.expedia.bookings.enums.ResultsFlightsState;
import com.expedia.bookings.interfaces.helpers.StateListenerHelper;
import com.expedia.bookings.section.FlightLegSummarySectionTablet;
import com.expedia.bookings.utils.ScreenPositionUtils;
import com.mobiata.android.util.Ui;

/**
 * ResultsFlightAddToTrip: The add to trip fragment designed for tablet results 2013
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ResultsFlightAddToTrip extends Fragment {

	public static ResultsFlightAddToTrip newInstance() {
		ResultsFlightAddToTrip frag = new ResultsFlightAddToTrip();
		return frag;
	}

	// Views
	private ViewGroup mRootC;

	private FlightLegSummarySectionTablet mFlightCard;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootC = Ui.inflate(inflater, R.layout.fragment_tablet_flight_add_to_trip, null);
		mFlightCard = Ui.findView(mRootC, R.id.flight_row);
		return mRootC;
	}

	@Override
	public void onResume() {
		super.onResume();
		mFlightsStateHelper.registerWithProvider(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		mFlightsStateHelper.unregisterWithProvider(this);
	}

	public Rect getRowRect() {
		if (mFlightCard != null) {
			return ScreenPositionUtils.getGlobalScreenPosition(mFlightCard);
		}
		return new Rect();
	}

	private StateListenerHelper<ResultsFlightsState> mFlightsStateHelper = new StateListenerHelper<ResultsFlightsState>() {
		@Override
		public void onStateTransitionStart(ResultsFlightsState stateOne, ResultsFlightsState stateTwo) {
			if (stateOne == ResultsFlightsState.ADDING_FLIGHT_TO_TRIP
				&& stateTwo == ResultsFlightsState.FLIGHT_LIST_DOWN) {
				mFlightCard.setTranslationY(0f);
				mFlightCard.setLayerType(View.LAYER_TYPE_HARDWARE, null);
			}
		}

		@Override
		public void onStateTransitionUpdate(ResultsFlightsState stateOne, ResultsFlightsState stateTwo,
			float percentage) {
			if (stateOne == ResultsFlightsState.ADDING_FLIGHT_TO_TRIP
				&& stateTwo == ResultsFlightsState.FLIGHT_LIST_DOWN) {
				mFlightCard.setTranslationY(mFlightCard.getBottom() * -percentage);
			}
		}

		@Override
		public void onStateTransitionEnd(ResultsFlightsState stateOne, ResultsFlightsState stateTwo) {
			if (stateOne == ResultsFlightsState.ADDING_FLIGHT_TO_TRIP
				&& stateTwo == ResultsFlightsState.FLIGHT_LIST_DOWN) {
				mFlightCard.setLayerType(View.LAYER_TYPE_NONE, null);
			}
		}

		@Override
		public void onStateFinalized(ResultsFlightsState state) {
			mFlightCard.setTranslationY(0f);
			if (state == ResultsFlightsState.ADDING_FLIGHT_TO_TRIP) {
				TripBucketItemFlight flight = Db.getTripBucket().getFlight();
				boolean isRoundTrip = Db.getFlightSearch().getSearchParams().isRoundTrip();
				int legs = isRoundTrip ? 2 : 1;

				mFlightCard.bindForTripBucket(flight.getFlightTrip(), flight.getFlightSearchState().getSelectedLegs(legs),
					isRoundTrip);
				mFlightCard.setVisibility(View.VISIBLE);
			}
			else {
				mFlightCard.setVisibility(View.INVISIBLE);
			}
		}
	};
}
