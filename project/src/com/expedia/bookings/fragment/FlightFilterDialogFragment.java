package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;

import com.expedia.bookings.R;

public class FlightFilterDialogFragment extends DialogFragment {

	public static final String TAG = FilterDialogFragment.class.getName();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setStyle(DialogFragment.STYLE_NO_TITLE, R.style.FlightFilterDialogTheme);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_flight_filter, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();

		// Setup the dialog so that it appears at the bottom of the screen,
		// taking up the entire width of the screen.
		//
		// We do this in onStart() because we need to wait until the dialog
		// is shown before we can start modifying its window like this.
		Window window = getDialog().getWindow();
		window.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		window.setGravity(Gravity.BOTTOM);
		window.setBackgroundDrawable(null);
	}
}
