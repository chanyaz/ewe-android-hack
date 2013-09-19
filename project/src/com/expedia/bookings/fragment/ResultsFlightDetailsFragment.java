package com.expedia.bookings.fragment;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.ScreenPositionUtils;
import com.mobiata.android.Log;
import com.mobiata.android.util.Ui;

import android.annotation.TargetApi;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;

/**
 * ResultsFlightFiltersFragment: The filters fragment designed for tablet results 2013
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ResultsFlightDetailsFragment extends Fragment {

	public static ResultsFlightDetailsFragment newInstance() {
		ResultsFlightDetailsFragment frag = new ResultsFlightDetailsFragment();
		return frag;
	}

	//Views
	private ViewGroup mRootC;
	private ViewGroup mAnimationFlightRowC;
	private ViewGroup mDetailsC;

	//Position and size vars
	int mDetailsPositionLeft = -1;
	int mDetailsPositionTop = -1;
	int mDetailsWidth = -1;
	int mDetailsHeight = -1;
	int mRowPositionLeft = -1;
	int mRowPositionTop = -1;
	int mRowWidth = -1;
	int mRowHeight = -1;

	//Animation vars
	private Rect mAddToTripRect;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		mRootC = (ViewGroup) inflater.inflate(R.layout.fragment_tablet_flight_details, null);
		mAnimationFlightRowC = Ui.findView(mRootC, R.id.details_animation_row_container);
		mAnimationFlightRowC.setPivotX(0);
		mAnimationFlightRowC.setPivotY(0);
		mDetailsC = Ui.findView(mRootC, R.id.details_container);
		mDetailsC.setPivotX(0);
		mDetailsC.setPivotY(0);

		applyDetailsDimensions();
		applyRowDimensions();

		return mRootC;
	}

	/*
	 * SIZING AND POSITIONING.
	 * 
	 * We let the controller size and position things. This is less likely to bite us in the ass
	 * if/when we have a dedicated portrait mode.
	 */

	public void setDefaultDetailsPositionAndDimensions(Rect position, float additionalMarginPercentage) {
		int width = position.right - position.left;
		int height = position.bottom - position.top;

		mDetailsPositionLeft = (int) (position.left + (additionalMarginPercentage * width));
		mDetailsPositionTop = (int) (position.top + (additionalMarginPercentage * height));
		mDetailsWidth = (int) (width - (2 * width * additionalMarginPercentage));
		mDetailsHeight = (int) (height - (2 * height * additionalMarginPercentage));

		applyDetailsDimensions();
	}

	public void setDetaultRowDimensions(int width, int height) {
		mRowWidth = width;
		mRowHeight = height;

		applyRowDimensions();
	}

	private void applyDetailsDimensions() {
		if (mDetailsC != null && mDetailsPositionLeft >= 0) {
			LayoutParams params = (LayoutParams) mDetailsC.getLayoutParams();
			if (params == null) {
				params = new LayoutParams(mDetailsHeight, mDetailsWidth);
			}
			params.width = mDetailsWidth;
			params.height = mDetailsHeight;
			params.leftMargin = mDetailsPositionLeft;
			params.topMargin = mDetailsPositionTop;
			mDetailsC.setLayoutParams(params);
		}
	}

	private void applyRowDimensions() {
		if (mAnimationFlightRowC != null && mRowWidth >= 0) {
			Log.d("ResultsFlightDetails setting mAnimationFlightRowC dimensions - width:" + mRowWidth + " height:"
					+ mRowHeight);
			LayoutParams params = (LayoutParams) mAnimationFlightRowC.getLayoutParams();
			if (params == null) {
				params = new LayoutParams(mRowHeight, mRowWidth);
			}
			else {
				params.height = mRowHeight;
				params.width = mRowWidth;
			}
			mAnimationFlightRowC.setLayoutParams(params);
		}
	}

	private void applyRowPosition() {
		if (mAnimationFlightRowC != null && mRowPositionLeft >= 0) {
			Log.d("ResultsFlightDetails setting mAnimationFlightRowC position - left:" + mRowPositionLeft + " top:"
					+ mRowPositionTop);
			LayoutParams params = (LayoutParams) mAnimationFlightRowC.getLayoutParams();
			if (params == null) {
				params = new LayoutParams(mRowHeight, mRowWidth);
			}
			params.leftMargin = mRowPositionLeft;
			params.topMargin = mRowPositionTop;
			mAnimationFlightRowC.setLayoutParams(params);
		}
	}

	/*
	 * SLIDE IN ANIMATION STUFF
	 */

	public void setSlideInAnimationLayer(int layerType) {

	}

	public void prepareSlideInAnimation() {
		mAnimationFlightRowC.setVisibility(View.VISIBLE);
		mDetailsC.setVisibility(View.VISIBLE);
	}

	public void setDetailsSlideInAnimationState(float percentage, int totalSlideDistance, boolean fromLeft) {
		if (mDetailsC != null) {
			mDetailsC.setTranslationX((fromLeft ? -totalSlideDistance : totalSlideDistance) * (1f - percentage));
		}
	}

	public void finalizeSlideInPercentage() {

	}

	/*
	 * DEPARTURE FLIGHT SELECTED ANIMATION STUFF
	 */

	public void setDepartureTripSelectedAnimationLayer(int layerType) {

	}

	public void prepareDepartureFlightSelectedAnimation(Rect globalDestSpot) {
		//We move the row into its destination position. The animation itself will
		//then start behind the Details and slide and scale its way back here.
		Rect local = ScreenPositionUtils.translateGlobalPositionToLocalPosition(globalDestSpot, mRootC);
		mRowWidth = local.right - local.left;
		mRowHeight = local.bottom - local.top;
		mRowPositionLeft = local.left;
		mRowPositionTop = local.top;
		applyRowDimensions();
		applyRowPosition();

		mAnimationFlightRowC.setVisibility(View.VISIBLE);
		mDetailsC.setVisibility(View.VISIBLE);
	}

	public void setDepartureTripSelectedAnimationState(float percentage) {
		if (mAnimationFlightRowC != null) {
			float rowScaleX = 1f + (((float) mDetailsWidth / mRowWidth) - 1f) * (1f - percentage);
			float rowScaleY = 1f + (((float) mDetailsHeight / mRowHeight) - 1f) * (1f - percentage);

			float rowTranslationX = (1f - percentage) * (mDetailsPositionLeft - mRowPositionLeft);
			float rowTranslationY = (1f - percentage) * (mDetailsPositionTop - mRowPositionTop);

			mAnimationFlightRowC.setScaleX(rowScaleX);
			mAnimationFlightRowC.setScaleY(rowScaleY);
			mAnimationFlightRowC.setTranslationX(rowTranslationX);
			mAnimationFlightRowC.setTranslationY(rowTranslationY);
			mAnimationFlightRowC.setAlpha(percentage);
		}

		if (mDetailsC != null) {
			float detailsScaleX = 1f + (((float) mRowWidth / mDetailsWidth) - 1f) * percentage;
			float detailsScaleY = 1f + (((float) mRowHeight / mDetailsHeight) - 1f) * percentage;
			float detailsTranslationX = percentage * (mRowPositionLeft - mDetailsPositionLeft);
			float detailsTranslationY = percentage * (mRowPositionTop - mDetailsPositionTop);

			mDetailsC.setScaleX(detailsScaleX);
			mDetailsC.setScaleY(detailsScaleY);
			mDetailsC.setTranslationX(detailsTranslationX);
			mDetailsC.setTranslationY(detailsTranslationY);
			mDetailsC.setAlpha(1f - percentage);
		}

	}

	public void finalizeDepartureFlightSelectedAnimation() {

	}

	/*
	 * ADD TO TRIP FROM DETAILS ANIMATION STUFF
	 */

	public void setAddToTripFromDetailsAnimationLayer(int layerType) {
		//These solve the same problem, so we can just re-use for now
		setDepartureTripSelectedAnimationLayer(layerType);
	}

	public void prepareAddToTripFromDetailsAnimation(Rect globalAddToTripPosition) {
		//These solve the same problem, so we can just re-use for now
		prepareDepartureFlightSelectedAnimation(globalAddToTripPosition);
	}

	public void setAddToTripFromDetailsAnimationState(float percentage) {
		//These solve the same problem, so we can just re-use for now
		setDepartureTripSelectedAnimationState(percentage);
	}

	public void finalizeAddToTripFromDetailsAnimation() {

	}

	/*
	 * ADD TO TRIP FROM DEPARTURE ANIMATION STUFF
	 */

	public void setAddToTripFromDepartureAnimationLayer(int layerType) {

	}

	public void prepareAddToTripFromDepartureAnimation(Rect globalDepartureRowPosition, Rect globalAddToTripPosition) {
		//This one is different, we set the row to be the dimensions of the start of the animation,
		//this way it should just sit on top of the actual row and not cause a jump as the scaling gets strange
		Rect local = ScreenPositionUtils.translateGlobalPositionToLocalPosition(globalDepartureRowPosition, mRootC);
		mRowWidth = local.right - local.left;
		mRowHeight = local.bottom - local.top;
		mRowPositionLeft = local.left;
		mRowPositionTop = local.top;

		applyRowDimensions();
		applyRowPosition();

		mAddToTripRect = ScreenPositionUtils.translateGlobalPositionToLocalPosition(globalAddToTripPosition, mRootC);
		mAnimationFlightRowC.setVisibility(View.VISIBLE);
		mDetailsC.setVisibility(View.INVISIBLE);
	}

	public void setAddToTripFromDepartureAnimationState(float percentage) {
		if (mAnimationFlightRowC != null && mAddToTripRect != null) {
			float rowScaleX = 1f + (((float) (mAddToTripRect.right - mAddToTripRect.left) / mRowWidth) - 1f)
					* percentage;
			float rowScaleY = 1f + (((float) (mAddToTripRect.bottom - mAddToTripRect.top) / mRowHeight) - 1f)
					* percentage;

			float rowTranslationX = percentage * (mAddToTripRect.left - mRowPositionLeft);
			float rowTranslationY = percentage * (mAddToTripRect.top - mRowPositionTop);

			mAnimationFlightRowC.setScaleX(rowScaleX);
			mAnimationFlightRowC.setScaleY(rowScaleY);
			mAnimationFlightRowC.setTranslationX(rowTranslationX);
			mAnimationFlightRowC.setTranslationY(rowTranslationY);
		}
	}

	public void finalizeAddToTripFromDepartureAnimation() {

	}

}
