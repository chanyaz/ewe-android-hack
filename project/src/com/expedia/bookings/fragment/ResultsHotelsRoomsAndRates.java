package com.expedia.bookings.fragment;

import com.expedia.bookings.R;
import com.expedia.bookings.interfaces.IAddToTripListener;
import com.expedia.bookings.utils.ColumnManager;
import com.mobiata.android.util.Ui;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

/**
 * ResultsFlightFiltersFragment: The filters fragment designed for tablet results 2013
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ResultsHotelsRoomsAndRates extends Fragment {

	public static ResultsHotelsRoomsAndRates newInstance(ColumnManager manager) {
		ResultsHotelsRoomsAndRates frag = new ResultsHotelsRoomsAndRates();
		frag.setColumnManager(manager);
		return frag;
	}

	private ColumnManager mColumnManager;

	private ViewGroup mRootC;
	private ViewGroup mRoomsAndRatesC;
	private ViewGroup mRoomsAndRatesShadeC;
	private ViewGroup mRoomsAndRatesTopC;
	private ViewGroup mRoomsAndRatesBottomC;

	private int mShadeColor = Color.argb(220, 0, 0, 0);
	private IAddToTripListener mAddToTripListener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mAddToTripListener = Ui.findFragmentListener(this, IAddToTripListener.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		mRootC = (ViewGroup) inflater.inflate(R.layout.fragment_tablet_hotels_rooms_and_rates, null);
		mRoomsAndRatesC = Ui.findView(mRootC, R.id.rooms_and_rates_content);
		mRoomsAndRatesShadeC = Ui.findView(mRootC, R.id.rooms_and_rates_shade);
		mRoomsAndRatesTopC = Ui.findView(mRootC, R.id.rooms_and_rates_content_top);
		mRoomsAndRatesBottomC = Ui.findView(mRootC, R.id.rooms_and_rates_content_bottom);

		mRoomsAndRatesShadeC.setBackgroundColor(mShadeColor);
		mRoomsAndRatesShadeC.setAlpha(0f);

		mRoomsAndRatesBottomC.setPivotY(0f);
		mRoomsAndRatesBottomC.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				beginTransitionToAddTrip();
			}

		});

		updateColumns();

		return mRootC;
	}

	public void setColumnManager(ColumnManager manager) {
		mColumnManager = manager;
		updateColumns();
	}

	private void setTransitionToAddTripPercentage(float percentage) {
		mRoomsAndRatesBottomC.setScaleY(1f - percentage);
		mRoomsAndRatesShadeC.setAlpha(percentage);
	}

	private void beginTransitionToAddTrip() {
		ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f).setDuration(3000);
		animator.addUpdateListener(new AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animator) {
				setTransitionToAddTripPercentage(animator.getAnimatedFraction());

			}

		});
		animator.addListener(new AnimatorListener() {

			@Override
			public void onAnimationCancel(Animator arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animator arg0) {
				mAddToTripListener.guiElementInPosition();
				mRoomsAndRatesShadeC.setAlpha(0f);
				setTransitionToAddTripHardwareLayer(View.LAYER_TYPE_NONE);
			}

			@Override
			public void onAnimationRepeat(Animator arg0) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onAnimationStart(Animator arg0) {
				mAddToTripListener.beginAddToTrip(new Object(), getDestinationRect(), mShadeColor);

			}
		});

		setTransitionToAddTripHardwareLayer(View.LAYER_TYPE_HARDWARE);
		animator.start();

	}

	private void setTransitionToAddTripHardwareLayer(int layerType) {
		mRoomsAndRatesBottomC.setLayerType(layerType, null);
		mRoomsAndRatesShadeC.setLayerType(layerType, null);
	}

	private void updateColumns() {
		if (mColumnManager != null) {
			if (mRootC != null) {
				mColumnManager.setContainerToColumnSpan(mRootC, 0, 2);
			}
			if (mRoomsAndRatesC != null) {
				mColumnManager.setContainerToColumnSpan(mRoomsAndRatesC, 1, 2);
			}
		}
	}

	private Rect getDestinationRect() {
		int[] currentGlobalLocation = new int[2];
		mRoomsAndRatesTopC.getLocationOnScreen(currentGlobalLocation);

		Rect retRect = new Rect();
		retRect.left = currentGlobalLocation[0];
		retRect.right = retRect.left + mRoomsAndRatesTopC.getWidth();
		retRect.top = currentGlobalLocation[1];
		retRect.bottom = retRect.top + mRoomsAndRatesTopC.getHeight();

		return retRect;
	}

}
