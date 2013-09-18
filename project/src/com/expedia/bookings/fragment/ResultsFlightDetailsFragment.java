package com.expedia.bookings.fragment;

import com.expedia.bookings.R;
import com.expedia.bookings.fragment.base.MeasurableFragment;
import com.expedia.bookings.fragment.base.MeasurableFragmentListener;
import com.expedia.bookings.utils.ColumnManager;
import com.mobiata.android.Log;
import com.mobiata.android.util.Ui;

import android.annotation.TargetApi;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;

/**
 * ResultsFlightFiltersFragment: The filters fragment designed for tablet results 2013
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ResultsFlightDetailsFragment extends MeasurableFragment implements MeasurableFragmentListener {

	public static ResultsFlightDetailsFragment newInstance() {
		ResultsFlightDetailsFragment frag = new ResultsFlightDetailsFragment();
		return frag;
	}

	//Views
	private ViewGroup mRootC;
	private ViewGroup mAnimationFlightRowC;
	private ViewGroup mDetailsC;

	//Animation and position
	private float mFlightRowOrigAlpha;
	private float mFlightRowDestAlpha;
	private float mFlightDetailsOrigAlpha;
	private float mFlightDetailsDestAlpha;
	private Pair<Float, Float> mFlightRowOrigScale;
	private Pair<Float, Float> mFlightRowDestScale;
	private Pair<Float, Float> mFlightDetailsOrigScale;
	private Pair<Float, Float> mFlightDetailsDestScale;
	private Pair<Integer, Integer> mFlightRowOrigPos;
	private Pair<Integer, Integer> mFlightRowDestPos;
	private Pair<Integer, Integer> mFlightDetailsOrigPos;
	private Pair<Integer, Integer> mFlightDetailsDestPos;
	private Rect mFlightRowLocation;
	private boolean mAnimationPrepared = false;

	//misc
	private ColumnManager mColumnManager;
	private FlightDetailsState mFDetailsState = FlightDetailsState.DETAILS;

	public enum FlightDetailsState {
		DETAILS, TOP_LEFT, ADD_TRIP
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		mRootC = (ViewGroup) inflater.inflate(R.layout.fragment_tablet_flight_details, null);
		mAnimationFlightRowC = Ui.findView(mRootC, R.id.flight_details_shrunk_container);
		mAnimationFlightRowC.setPivotX(0);
		mAnimationFlightRowC.setPivotY(0);
		mDetailsC = Ui.findView(mRootC, R.id.flight_details_expanded_container);
		mDetailsC.setPivotX(0);
		mDetailsC.setPivotY(0);

		if (mColumnManager != null) {
			setState(mFDetailsState);

		}

		return mRootC;
	}

	public void setColumnManager(ColumnManager manager) {
		mColumnManager = manager;
		if (mRootC != null) {
			setState(mFDetailsState);
		}
	}

	private Pair<Integer, Integer> getFlightDetailsPositionForState(FlightDetailsState state) {
		int left = 0;
		int top = 0;

		switch (state) {
		case DETAILS: {
			//Center in column 1 - 2
			left = (int) (mColumnManager.getColWidth(0) + ((mColumnManager.getColWidth(1)
					+ mColumnManager.getColWidth(2) - mDetailsC.getWidth()) / 2f));
			top = (int) ((mRootC.getHeight() - mDetailsC.getHeight()) / 2f);
			break;
		}
		case TOP_LEFT: {
			return getFlightRowTranslationForState(state);
		}
		case ADD_TRIP: {
			return getFlightRowTranslationForState(state);
		}
		}
		return new Pair<Integer, Integer>(left, top);
	}

	private Pair<Integer, Integer> getFlightRowTranslationForState(FlightDetailsState state) {
		int left = mColumnManager.getColLeft(1);
		int top = 0;

		switch (state) {
		case DETAILS: {
			return getFlightDetailsPositionForState(state);
		}
		case TOP_LEFT: {
			left = mFlightRowLocation.left;
			top = mFlightRowLocation.top;
			break;
		}
		case ADD_TRIP: {
			left = mFlightRowLocation.left;
			top = mFlightRowLocation.top;
			break;
		}
		}
		return new Pair<Integer, Integer>(left, top);
	}

	private Pair<Float, Float> getFlightDetailsScaleFromState(FlightDetailsState state) {
		float scaleX = 1f;
		float scaleY = 1f;

		switch (state) {
		case DETAILS: {
			//FlightDetails stays at 1 in details mode
			break;
		}
		case TOP_LEFT:
		case ADD_TRIP: {
			scaleX = (1f * mFlightRowLocation.right - mFlightRowLocation.left) / mDetailsC.getWidth();
			scaleY = (1f * mFlightRowLocation.bottom - mFlightRowLocation.top) / mDetailsC.getHeight();
			break;
		}
		}
		return new Pair<Float, Float>(scaleX, scaleY);
	}

	private Pair<Float, Float> getFlightRowScaleFromState(FlightDetailsState state) {
		float scaleX = 1f;
		float scaleY = 1f;

		switch (state) {
		case DETAILS: {
			if (mFlightRowLocation != null) {
				scaleX = (1f * mDetailsC.getWidth()) / (mFlightRowLocation.right - mFlightRowLocation.left);
			}
			//			else {
			//				scaleX = (1f * mDetailsC.getWidth()) / mAnimationFlightRowC.getWidth();
			//			}
			break;
		}
		case TOP_LEFT: {
			break;
		}
		case ADD_TRIP: {
			break;
		}
		}
		return new Pair<Float, Float>(scaleX, scaleY);
	}

	private float getFlightDetailsAlphaFromState(FlightDetailsState state) {
		if (state == FlightDetailsState.DETAILS) {
			return 1f;
		}
		else {
			return 0f;
		}
	}

	private float getFlightRowAlphaFromState(FlightDetailsState state) {
		if (state != FlightDetailsState.DETAILS) {
			return 1f;
		}
		else {
			return 0f;
		}
	}

	private void setPositionsFromState(FlightDetailsState state) {
		Pair<Integer, Integer> shrunkPos = getFlightRowTranslationForState(state);
		Pair<Integer, Integer> expandedPos = getFlightDetailsPositionForState(state);

		mAnimationFlightRowC.setTranslationX(shrunkPos.first);
		mAnimationFlightRowC.setTranslationY(shrunkPos.second);

		mDetailsC.setTranslationX(expandedPos.first);
		mDetailsC.setTranslationY(expandedPos.second);
	}

	private void setScaleFromState(FlightDetailsState state) {
		Pair<Float, Float> shrunkScale = getFlightRowScaleFromState(state);
		Pair<Float, Float> expandedScale = getFlightDetailsScaleFromState(state);

		mAnimationFlightRowC.setScaleX(shrunkScale.first);
		mAnimationFlightRowC.setScaleY(shrunkScale.second);

		mDetailsC.setScaleX(expandedScale.first);
		mDetailsC.setScaleY(expandedScale.second);
	}

	private void setFlightRowSizeAndPosition(Rect rect) {
		LayoutParams params = (LayoutParams) mAnimationFlightRowC.getLayoutParams();
		params.width = rect.right - rect.left;
		params.height = rect.bottom - rect.top;
		mAnimationFlightRowC.setLayoutParams(params);
		mAnimationFlightRowC.setTranslationX(rect.left);
		mAnimationFlightRowC.setTranslationY(rect.top);
		Log.d("JOE: setFlightRowSizeAndPosition width:" + params.width + " height:" + params.height + " left:"
				+ rect.left + " top:" + rect.top);

	}

	private void setAlphaFromState(FlightDetailsState state) {
		mAnimationFlightRowC.setAlpha(getFlightRowAlphaFromState(state));
		mDetailsC.setAlpha(getFlightDetailsAlphaFromState(state));
	}

	public void setState(FlightDetailsState state) {
		mFDetailsState = state;
		setPositionsFromState(state);
		setScaleFromState(state);
		setAlphaFromState(state);
	}

	public void perpareTransition(FlightDetailsState startState, FlightDetailsState endState,
			Rect globalFlightRowLocation) {
		mFlightRowLocation = globalFlightRowLocation;

		setFlightRowSizeAndPosition(globalFlightRowLocation);

		mFlightRowOrigAlpha = getFlightRowAlphaFromState(startState);
		mFlightRowDestAlpha = getFlightRowAlphaFromState(endState);
		mFlightRowOrigScale = getFlightRowScaleFromState(startState);
		mFlightRowDestScale = getFlightRowScaleFromState(endState);
		mFlightRowOrigPos = getFlightRowTranslationForState(startState);
		mFlightRowDestPos = getFlightRowTranslationForState(endState);

		mFlightDetailsOrigAlpha = getFlightDetailsAlphaFromState(startState);
		mFlightDetailsDestAlpha = getFlightDetailsAlphaFromState(endState);
		mFlightDetailsOrigScale = getFlightDetailsScaleFromState(startState);
		mFlightDetailsDestScale = getFlightDetailsScaleFromState(endState);
		mFlightDetailsOrigPos = getFlightDetailsPositionForState(startState);
		mFlightDetailsDestPos = getFlightDetailsPositionForState(endState);

		mAnimationPrepared = true;
	}

	public void finalizeTransition(FlightDetailsState startState, FlightDetailsState endState) {
		mFlightRowOrigScale = null;
		mFlightRowDestScale = null;
		mFlightDetailsOrigScale = null;
		mFlightDetailsDestScale = null;
		mFlightRowOrigPos = null;
		mFlightRowDestPos = null;
		mFlightDetailsOrigPos = null;
		mFlightDetailsDestPos = null;

		mAnimationPrepared = false;
	}

	public void setTransitionPercentage(FlightDetailsState startState, FlightDetailsState endState, float percentage) {
		if (mAnimationPrepared) {
			mAnimationFlightRowC.setAlpha(mFlightRowOrigAlpha + (mFlightRowDestAlpha - mFlightRowOrigAlpha)
					* percentage);
			mDetailsC.setAlpha(mFlightDetailsOrigAlpha + (mFlightDetailsDestAlpha - mFlightDetailsOrigAlpha)
					* percentage);

			mAnimationFlightRowC.setTranslationX(mFlightRowOrigPos.first
					+ (mFlightRowDestPos.first - mFlightRowOrigPos.first) * percentage);
			mAnimationFlightRowC.setTranslationY(mFlightRowOrigPos.second
					+ (mFlightRowDestPos.second - mFlightRowOrigPos.second)
					* percentage);

			mDetailsC.setTranslationX(mFlightDetailsOrigPos.first
					+ (mFlightDetailsDestPos.first - mFlightDetailsOrigPos.first)
					* percentage);
			mDetailsC.setTranslationY(mFlightDetailsOrigPos.second
					+ (mFlightDetailsDestPos.second - mFlightDetailsOrigPos.second)
					* percentage);

			mAnimationFlightRowC.setScaleX(mFlightRowOrigScale.first
					+ (mFlightRowDestScale.first - mFlightRowOrigScale.first) * percentage);
			mAnimationFlightRowC.setScaleY(mFlightRowOrigScale.second
					+ (mFlightRowDestScale.second - mFlightRowOrigScale.second)
					* percentage);

			mDetailsC.setScaleX(mFlightDetailsOrigScale.first
					+ (mFlightDetailsDestScale.first - mFlightDetailsOrigScale.first)
					* percentage);
			mDetailsC.setScaleY(mFlightDetailsOrigScale.second
					+ (mFlightDetailsDestScale.second - mFlightDetailsOrigScale.second)
					* percentage);
		}
	}

	@Override
	public void canMeasure(Fragment fragment) {
		setState(mFDetailsState);
	}
}
