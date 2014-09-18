package com.expedia.bookings.fragment.debug;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;

import com.expedia.bookings.R;
import com.expedia.bookings.fragment.base.MeasurableFragment;
import com.expedia.bookings.utils.Ui;

/**
 * Simple test Fragment that has a button that counts the #
 * of times it's been pressed.  Useful for telling if you can
 * interact with something even if something else is layered on top.
 */
public class ButtonFragment extends MeasurableFragment {

	private static String ARG_MESSAGE = "ARG_MESSAGE";
	private static String ARG_WIDTH_DIMEN_RES_ID = "ARG_WIDTH_DIMEN_RES_ID";

	private static String INSTANCE_PRESSES = "INSTANCE_PRESSES";

	public static ButtonFragment newInstance(String msg, int widthDimenResId) {
		ButtonFragment fragment = new ButtonFragment();
		Bundle args = new Bundle();
		args.putString(ARG_MESSAGE, msg);
		args.putInt(ARG_WIDTH_DIMEN_RES_ID, widthDimenResId);
		fragment.setArguments(args);
		return fragment;
	}

	private Button mButton;
	private String mMsg;
	private int mNumPresses = 0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_button, container, false);

		mMsg = getArguments().getString(ARG_MESSAGE, "Press Me");

		if (savedInstanceState != null) {
			mNumPresses = savedInstanceState.getInt(INSTANCE_PRESSES, mNumPresses);
		}

		mButton = Ui.findView(view, R.id.button);
		mButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mNumPresses++;
				updateButtonText();
			}
		});
		updateButtonText();

		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		int widthDimenResId = getArguments().getInt(ARG_WIDTH_DIMEN_RES_ID);
		if (widthDimenResId != 0) {
			LayoutParams params = mButton.getLayoutParams();
			params.width = getResources().getDimensionPixelSize(widthDimenResId);
			mButton.setLayoutParams(params);
		}
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
