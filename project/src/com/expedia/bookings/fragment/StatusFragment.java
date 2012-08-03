package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.widget.PlaneWindowView;
import com.mobiata.android.util.Ui;

public class StatusFragment extends Fragment {

	public static final String TAG = StatusFragment.class.toString();

	private static final String INSTANCE_LOADING_TEXT = "INSTANCE_LOADING_TEXT";
	private static final String INSTANCE_ERROR_TEXT = "INSTANCE_ERROR_TEXT";

	private PlaneWindowView mPlaneWindowView;
	private TextView mMessageTextView;

	private CharSequence mLoadingText;
	private CharSequence mErrorText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			mLoadingText = savedInstanceState.getCharSequence(INSTANCE_LOADING_TEXT);
			mErrorText = savedInstanceState.getCharSequence(INSTANCE_ERROR_TEXT);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_status, container, false);

		mPlaneWindowView = Ui.findView(v, R.id.plane_window_view);
		mMessageTextView = Ui.findView(v, R.id.message_text_view);

		displayStatus();

		return v;
	}

	@Override
	public void onStart() {
		super.onStart();

		mPlaneWindowView.setRendering(true);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putCharSequence(INSTANCE_LOADING_TEXT, mLoadingText);
		outState.putCharSequence(INSTANCE_ERROR_TEXT, mErrorText);
	}

	@Override
	public void onStop() {
		super.onStop();

		mPlaneWindowView.setRendering(false);
	}

	public void showLoading(CharSequence loadingText) {
		mLoadingText = loadingText;
		mErrorText = null;

		displayStatus();
	}

	public void showError(CharSequence errorText) {
		mLoadingText = null;
		mErrorText = errorText;

		displayStatus();
	}

	private void displayStatus() {
		if (mMessageTextView != null) {
			if (!TextUtils.isEmpty(mErrorText)) {
				mMessageTextView.setText(mErrorText);
			}
			else {
				mMessageTextView.setText(mLoadingText);
			}
		}
	}
}
