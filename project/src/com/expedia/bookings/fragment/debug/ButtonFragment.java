package com.expedia.bookings.fragment.debug;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.expedia.bookings.fragment.base.MeasurableFragment;

/**
 * Simple test Fragment that has a button that counts the #
 * of times it's been pressed.  Useful for telling if you can
 * interact with something even if something else is layered on top.
 */
public class ButtonFragment extends MeasurableFragment {

	private static String ARG_MESSAGE = "ARG_MESSAGE";

	private static String INSTANCE_PRESSES = "INSTANCE_PRESSES";

	public static ButtonFragment newInstance(String msg) {
		ButtonFragment fragment = new ButtonFragment();
		Bundle args = new Bundle();
		args.putString(ARG_MESSAGE, msg);
		fragment.setArguments(args);
		return fragment;
	}

	private Button mButton;
	private String mMsg;
	private int mNumPresses = 0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mMsg = getArguments().getString(ARG_MESSAGE, "Press Me");

		if (savedInstanceState != null) {
			mNumPresses = savedInstanceState.getInt(INSTANCE_PRESSES, mNumPresses);
		}

		mButton = new Button(getActivity());
		mButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mNumPresses++;
				updateButtonText();
			}
		});
		updateButtonText();

		return mButton;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(INSTANCE_PRESSES, mNumPresses);
	}

	private void updateButtonText() {
		mButton.setText(mMsg + " " + mNumPresses);
	}
}
