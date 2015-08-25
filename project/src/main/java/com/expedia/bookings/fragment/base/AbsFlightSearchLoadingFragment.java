package com.expedia.bookings.fragment.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.mobiata.android.util.Ui;

public abstract class AbsFlightSearchLoadingFragment extends android.support.v4.app.Fragment {

	private static final String INSTANCE_TEXT = "INSTANCE_TEXT";
	private static final String INSTANCE_IS_GROUNDED = "INSTANCE_IS_GROUNDED";

	public static final String TAG = AbsFlightSearchLoadingFragment.class.toString();

	protected TextView mMessageTextView;
	protected View mCoverUpView;

	protected CharSequence mText;
	protected boolean mIsGrounded;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			mText = savedInstanceState.getCharSequence(INSTANCE_TEXT);
			mIsGrounded = savedInstanceState.getBoolean(INSTANCE_IS_GROUNDED);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_status, container, false);

		mMessageTextView = Ui.findView(v, R.id.message_text_view);
		mCoverUpView = Ui.findView(v, R.id.cover_up_view);

		return v;
	}

	@Override
	public void onStart() {
		super.onStart();
		OmnitureTracking.trackPageLoadFlightSearchResultsPlaneLoadingFragment();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putCharSequence(INSTANCE_TEXT, mText);
		outState.putBoolean(INSTANCE_IS_GROUNDED, mIsGrounded);
	}

	// The cover lets you cover up the tears between showing/hiding the SurfaceView
	public void setCoverEnabled(final boolean enabled) {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mCoverUpView.setVisibility(enabled ? View.VISIBLE : View.GONE);
			}
		});
	}

	protected void displayStatus() {
		if (mMessageTextView != null) {
			mMessageTextView.setText(mText);
		}
	}

	public void showLoading(CharSequence loadingText) {
		mText = loadingText;

		displayStatus();
	}

	public void showError(CharSequence errorText) {
		mText = errorText;

		displayStatus();
	}

	public void showGrounded(CharSequence groundedText) {
		mText = groundedText;
		mIsGrounded = true;

		displayStatus();
	}

}
