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

	public static ResultsHotelsRoomsAndRates newInstance() {
		ResultsHotelsRoomsAndRates frag = new ResultsHotelsRoomsAndRates();
		return frag;
	}

	private ViewGroup mRootC;
	private ViewGroup mRoomsAndRatesC;
	private ViewGroup mRoomsAndRatesTopC;
	private ViewGroup mRoomsAndRatesBottomC;

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
		mRoomsAndRatesTopC = Ui.findView(mRootC, R.id.rooms_and_rates_content_top);
		mRoomsAndRatesBottomC = Ui.findView(mRootC, R.id.rooms_and_rates_content_bottom);

		mRoomsAndRatesBottomC.setPivotY(0f);
		mRoomsAndRatesBottomC.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mAddToTripListener.beginAddToTrip(new Object(), getDestinationRect(), 0);
			}

		});

		return mRootC;
	}

	public void setTransitionToAddTripPercentage(float percentage) {
		if (mRoomsAndRatesBottomC != null) {
			mRoomsAndRatesBottomC.setScaleY(1f - percentage);
		}
	}

	public void setTransitionToAddTripHardwareLayer(int layerType) {
		if (mRoomsAndRatesBottomC != null) {
			mRoomsAndRatesBottomC.setLayerType(layerType, null);
		}
	}
	
	public Object getSelectedData(){
		return "SOME DATA";
	}

	public Rect getDestinationRect() {
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
