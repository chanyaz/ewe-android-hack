package com.expedia.bookings.fragment;

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
import com.expedia.bookings.animation.CubicBezierAnimation;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.TripBucketItemFlight;
import com.expedia.bookings.enums.ResultsFlightsState;
import com.expedia.bookings.interfaces.helpers.StateListenerHelper;
import com.expedia.bookings.utils.ScreenPositionUtils;
import com.mobiata.android.util.Ui;

/**
 * ResultsFlightAddToTrip: The add to trip fragment designed for tablet results 2013
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ResultsFlightAddToTrip extends Fragment {

	private static final String FTAG_BUCKET_FLIGHT = "FTAG_BUCKET_FLIGHT";

	public static ResultsFlightAddToTrip newInstance() {
		ResultsFlightAddToTrip frag = new ResultsFlightAddToTrip();
		return frag;
	}

	// Views
	private ViewGroup mRootC;
	private ViewGroup mBucketFlightC;
	private Rect mGlobalDestRect;
	private Rect mLocalDestRect;
	private TripBucketFlightFragment mBucketFlightFrag;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		mRootC = (ViewGroup) inflater.inflate(R.layout.fragment_tablet_flight_add_to_trip, null);
		mBucketFlightC = Ui.findView(mRootC, R.id.fragment_container);

		FragmentManager manager = getChildFragmentManager();
		mBucketFlightFrag = (TripBucketFlightFragment) manager.findFragmentByTag(FTAG_BUCKET_FLIGHT);
		if (mBucketFlightFrag == null) {
			mBucketFlightFrag = TripBucketFlightFragment.newInstance();
		}
		if (mBucketFlightFrag != null && !mBucketFlightFrag.isAdded()) {
			FragmentTransaction transaction = manager.beginTransaction();
			transaction.add(R.id.fragment_container, mBucketFlightFrag, FTAG_BUCKET_FLIGHT);
			transaction.commit();
		}

		setGlobalDestinationRect(mGlobalDestRect);

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

	public Rect getCenteredAddToTripRect() {
		if (mBucketFlightC != null) {
			Rect bucketFlightRect = ScreenPositionUtils.getGlobalScreenPosition(mBucketFlightC);
			if (mGlobalDestRect != null && mGlobalDestRect.width() > 0 && mRootC != null && mRootC.getWidth() > 0) {
				if (bucketFlightRect.width() != mGlobalDestRect.width()) {
					float halfWidth = bucketFlightRect.width() / 2f;
					float centerX = mRootC.getWidth() / 2f;
					//Center and size
					bucketFlightRect.left = (int) (centerX - halfWidth);
					bucketFlightRect.right = (int) (centerX + halfWidth);
				}
				if (bucketFlightRect.height() != mGlobalDestRect.height()) {
					float halfHeight = bucketFlightRect.height() / 2f;
					float centerY = mRootC.getHeight() / 2f;
					//Center and size
					bucketFlightRect.top = (int) (centerY - halfHeight);
					bucketFlightRect.bottom = (int) (centerY + halfHeight);
				}
			}
			return bucketFlightRect;
		}
		return new Rect();
	}


	public void setGlobalDestinationRect(Rect globalDestinationRect) {
		if (globalDestinationRect != null) {
			mGlobalDestRect = globalDestinationRect;
			if (mRootC != null) {
				mLocalDestRect = ScreenPositionUtils
					.translateGlobalPositionToLocalPosition(globalDestinationRect, mRootC);

				if (mBucketFlightC != null && mLocalDestRect != null && mLocalDestRect.height() > 0
					&& mLocalDestRect.width() > 0) {
					ViewGroup.LayoutParams params = mBucketFlightC.getLayoutParams();
					if (params == null || params.height != mLocalDestRect.height() || params.width != mLocalDestRect
						.width()) {
						if (params == null) {
							params = new ViewGroup.LayoutParams(mLocalDestRect.width(), mLocalDestRect.height());
						}
						else {
							params.height = mLocalDestRect.height();
							params.width = mLocalDestRect.width();
						}
						mBucketFlightC.setLayoutParams(params);
					}
				}
			}
		}
	}

	private void resetFlightCard() {
		mBucketFlightC.setTranslationY(0f);
		mBucketFlightC.setTranslationX(0f);
		mBucketFlightC.setScaleX(1f);
		mBucketFlightC.setScaleY(1f);
	}

	private StateListenerHelper<ResultsFlightsState> mFlightsStateHelper = new StateListenerHelper<ResultsFlightsState>() {

		private CubicBezierAnimation mCurve;

		@Override
		public void onStateTransitionStart(ResultsFlightsState stateOne, ResultsFlightsState stateTwo) {
			if (stateOne == ResultsFlightsState.CHOOSING_FLIGHT
				&& stateTwo == ResultsFlightsState.ADDING_FLIGHT_TO_TRIP) {
				resetFlightCard();
				mBucketFlightC.setAlpha(0f);
				mBucketFlightC.setVisibility(View.VISIBLE);
			}
			else if (stateOne == ResultsFlightsState.ADDING_FLIGHT_TO_TRIP
				&& stateTwo == ResultsFlightsState.FLIGHT_LIST_DOWN) {
				resetFlightCard();
				mCurve = CubicBezierAnimation
					.newOutsideInAnimation(0, 0, mLocalDestRect.left - mBucketFlightC.getLeft(),
						mLocalDestRect.top - mBucketFlightC.getTop());
				mBucketFlightC.setLayerType(View.LAYER_TYPE_HARDWARE, null);
			}
		}

		@Override
		public void onStateTransitionUpdate(ResultsFlightsState stateOne, ResultsFlightsState stateTwo,
			float percentage) {
			if (stateOne == ResultsFlightsState.CHOOSING_FLIGHT
				&& stateTwo == ResultsFlightsState.ADDING_FLIGHT_TO_TRIP) {
				float fadeInThreshold = 0.66f;
				if (percentage > fadeInThreshold) {
					float alpha = Math.max(0f, (percentage - fadeInThreshold) / (1f - fadeInThreshold));
					mBucketFlightC.setAlpha(alpha);
				}
			}
			else if (stateOne == ResultsFlightsState.ADDING_FLIGHT_TO_TRIP
				&& stateTwo == ResultsFlightsState.FLIGHT_LIST_DOWN) {


				float endScaleX = mLocalDestRect.width() / (float) mBucketFlightC.getWidth();
				float endScaleY = mLocalDestRect.height() / (float) mBucketFlightC.getHeight();
				float scaleX = 1f + percentage * (endScaleX - 1f);
				float scaleY = 1f + percentage * (endScaleY - 1f);
				mBucketFlightC.setPivotX(0);
				mBucketFlightC.setPivotY(0);
				mBucketFlightC.setScaleX(scaleX);
				mBucketFlightC.setScaleY(scaleY);

				mBucketFlightC.setTranslationX(mCurve.getXInterpolator().interpolate(percentage));
				mBucketFlightC.setTranslationY(mCurve.getYInterpolator().interpolate(percentage));
			}
		}

		@Override
		public void onStateTransitionEnd(ResultsFlightsState stateOne, ResultsFlightsState stateTwo) {
			if (stateOne == ResultsFlightsState.ADDING_FLIGHT_TO_TRIP
				&& stateTwo == ResultsFlightsState.FLIGHT_LIST_DOWN) {
				mBucketFlightC.setLayerType(View.LAYER_TYPE_NONE, null);
			}
		}

		@Override
		public void onStateFinalized(ResultsFlightsState state) {
			mBucketFlightC.setTranslationY(0f);
			if (state == ResultsFlightsState.ADDING_FLIGHT_TO_TRIP) {
				TripBucketItemFlight flight = Db.getTripBucket().getFlight();
				boolean isRoundTrip = Db.getFlightSearch().getSearchParams().isRoundTrip();
				int legs = isRoundTrip ? 2 : 1;

				if (mBucketFlightFrag != null) {
					mBucketFlightFrag.bind();
				}
				mBucketFlightC.setAlpha(1f);
				mBucketFlightC.setVisibility(View.VISIBLE);
			}
			else {
				mBucketFlightC.setVisibility(View.INVISIBLE);
				resetFlightCard();
			}
		}
	};
}
