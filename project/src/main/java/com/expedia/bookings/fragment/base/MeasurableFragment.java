package com.expedia.bookings.fragment.base;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

/**
 * Fragment that can use MeasurableFragmentListener.  Doesn't require that
 * the Activity implements MeasurableFragmentListener (ignores in that case).
 */
public class MeasurableFragment extends Fragment implements Measurable {

	private MeasurableFragmentHelper mHelper;

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);

		mHelper = new MeasurableFragmentHelper(this);
		mHelper.onAttach();
	}

	@Override
	public void onViewCreated(final View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mHelper.onViewCreated(view);
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

	//////////////////////////////////////////////////////////////////////////
	// Measurable

	@Override
	public boolean isMeasurable() {
		return mHelper.isMeasurable();
	}
}
