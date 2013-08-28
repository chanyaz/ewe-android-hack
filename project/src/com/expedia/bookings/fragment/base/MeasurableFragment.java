package com.expedia.bookings.fragment.base;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

/**
 * Fragment that can use MeasurableFragmentListener.  Doesn't require that
 * the Activity implements MeasurableFragmentListener (ignores in that case).
 */
public class MeasurableFragment extends Fragment {

	private MeasurableFragmentHelper mHelper;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mHelper = new MeasurableFragmentHelper(this);
		mHelper.onAttach(activity);
	}

	@Override
	public void onViewCreated(final View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mHelper.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		mHelper.onDestroyView();
	}

	@Override
	public void onDetach() {
		super.onDetach();

		mHelper.onDetach();
	}

	public boolean isMeasurable() {
		return mHelper.isMeasurable();
	}

}
