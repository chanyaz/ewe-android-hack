package com.expedia.bookings.fragment.base;

import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;

import com.mobiata.android.util.Ui;

/**
 * A helper that makes a Fragment compatible with MeasurableFragmentListener.
 * 
 * Make sure to call all lifecycle methods:
 * 
 * - onAttach()
 * - onViewCreated()
 * - onDestroyView()
 * - onDetach()
 *
 */
public class MeasurableFragmentHelper {

	private Fragment mFragment;

	private MeasurableFragmentListener mListener;

	private boolean mIsMeasurable = false;

	public MeasurableFragmentHelper(Fragment fragment) {
		mFragment = fragment;
	}

	public void onAttach() {
		mListener = Ui.findFragmentListener(mFragment, MeasurableFragmentListener.class, false);
	}

	public void onViewCreated(final View view) {
		if (mListener != null) {
			view.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
				@Override
				public boolean onPreDraw() {
					view.getViewTreeObserver().removeOnPreDrawListener(this);

					if (mFragment.getActivity() != null && mListener != null) {
						mIsMeasurable = true;

						mListener.canMeasure(mFragment);
					}

					return true;
				}
			});
		}
	}

	public void onDestroyView() {
		mIsMeasurable = false;
	}

	public void onDetach() {
		mListener = null;
	}

	public boolean isMeasurable() {
		return mIsMeasurable;
	}

}
