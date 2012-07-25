package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.mobiata.android.util.Ui;

public class StatusFragment extends Fragment {

	public static final String TAG = StatusFragment.class.toString();

	private static final String INSTANCE_LOADING_TEXT = "INSTANCE_LOADING_TEXT";
	private static final String INSTANCE_ERROR_TEXT = "INSTANCE_ERROR_TEXT";

	private ProgressBar mProgressBar;
	private TextView mProgressTextView;
	private TextView mErrorTextView;

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

		mProgressBar = Ui.findView(v, R.id.progress_bar);
		mProgressTextView = Ui.findView(v, R.id.progress_text_view);
		mErrorTextView = Ui.findView(v, R.id.error_text_view);

		displayStatus();

		return v;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putCharSequence(INSTANCE_LOADING_TEXT, mLoadingText);
		outState.putCharSequence(INSTANCE_ERROR_TEXT, mErrorText);
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
		if (mProgressBar != null && mProgressTextView != null && mErrorTextView != null) {
			if (!TextUtils.isEmpty(mErrorText)) {
				mProgressBar.setVisibility(View.GONE);
				mProgressTextView.setVisibility(View.GONE);
				mErrorTextView.setVisibility(View.VISIBLE);

				mErrorTextView.setText(mErrorText);
			}
			else {
				mProgressBar.setVisibility(View.VISIBLE);
				mProgressTextView.setVisibility(View.VISIBLE);
				mErrorTextView.setVisibility(View.GONE);

				mProgressTextView.setText(mLoadingText);
			}
		}
	}
}
