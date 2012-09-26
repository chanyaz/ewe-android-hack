package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.widget.PlaneWindowView;
import com.expedia.bookings.widget.PlaneWindowView.PlaneWindowListener;
import com.mobiata.android.util.Ui;

public class StatusFragment extends Fragment implements PlaneWindowListener {

	public static final String TAG = StatusFragment.class.toString();

	private static final String INSTANCE_TEXT = "INSTANCE_TEXT";
	private static final String INSTANCE_IS_GROUNDED = "INSTANCE_IS_GROUNDED";

	private PlaneWindowView mPlaneWindowView;
	private TextView mMessageTextView;
	private View mCoverUpView;

	private CharSequence mText;
	private boolean mIsGrounded;

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

		mPlaneWindowView = Ui.findView(v, R.id.plane_window_view);
		mMessageTextView = Ui.findView(v, R.id.message_text_view);
		mCoverUpView = Ui.findView(v, R.id.cover_up_view);

		mPlaneWindowView.setListener(this);

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

		outState.putCharSequence(INSTANCE_TEXT, mText);
		outState.putBoolean(INSTANCE_IS_GROUNDED, mIsGrounded);
	}

	@Override
	public void onStop() {
		super.onStop();

		mPlaneWindowView.setRendering(false);
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

	private void displayStatus() {
		if (mMessageTextView != null) {
			mMessageTextView.setText(mText);
		}

		if (mPlaneWindowView != null) {
			mPlaneWindowView.setGrounded(mIsGrounded);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// PlaneWindowListener

	@Override
	public void onFirstRender() {
		setCoverEnabled(false);
	}
}
