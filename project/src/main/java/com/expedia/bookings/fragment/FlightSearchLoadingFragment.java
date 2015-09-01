package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.LayoutUtils;
import com.expedia.bookings.widget.PlaneWindowView;
import com.mobiata.android.util.Ui;

public class FlightSearchLoadingFragment extends android.support.v4.app.Fragment
	implements PlaneWindowView.PlaneWindowListener {

	private static final String INSTANCE_TEXT = "INSTANCE_TEXT";
	private static final String INSTANCE_IS_GROUNDED = "INSTANCE_IS_GROUNDED";

	public static final String TAG = FlightSearchLoadingFragment.class.toString();

	protected TextView mMessageTextView;
	protected View mCoverUpView;

	protected CharSequence mText;
	protected boolean mIsGrounded;

	private PlaneWindowView mPlaneWindowView;
	private boolean showDefaultPlaneWindowAnimation;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			mText = savedInstanceState.getCharSequence(INSTANCE_TEXT);
			mIsGrounded = savedInstanceState.getBoolean(INSTANCE_IS_GROUNDED);
		}
		showDefaultPlaneWindowAnimation =
			ProductFlavorFeatureConfiguration.getInstance().getFlightSearchProgressImageResId() == 0;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_status, container, false);

		LayoutUtils.adjustPaddingForOverlayMode(getActivity(), v, false);

		mMessageTextView = Ui.findView(v, R.id.message_text_view);
		mCoverUpView = Ui.findView(v, R.id.cover_up_view);
		mPlaneWindowView = Ui.findView(v, R.id.plane_window_view);

		if (!showDefaultPlaneWindowAnimation) {
			ImageView flightSearchView = Ui.findView(v, R.id.search_progress_flight);
			flightSearchView
				.setImageResource(ProductFlavorFeatureConfiguration.getInstance().getFlightSearchProgressImageResId());
			mPlaneWindowView.setVisibility(View.GONE);
			flightSearchView.setVisibility(View.VISIBLE);

			RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mMessageTextView.getLayoutParams();
			layoutParams.addRule(RelativeLayout.ABOVE, R.id.search_progress_flight);
			mMessageTextView.setLayoutParams(layoutParams);

			flightSearchView.bringToFront();
			mMessageTextView.bringToFront();
		}
		else {
			mPlaneWindowView.setListener(this);
		}
		displayStatus();

		return v;
	}

	@Override
	public void onStart() {
		super.onStart();
		if (showDefaultPlaneWindowAnimation) {
			mPlaneWindowView.setRendering(true);
		}
		OmnitureTracking.trackPageLoadFlightSearchResultsPlaneLoadingFragment();
	}

	@Override
	public void onStop() {
		super.onStop();
		if (showDefaultPlaneWindowAnimation) {
			mPlaneWindowView.setRendering(false);
		}
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
		if (showDefaultPlaneWindowAnimation && mPlaneWindowView != null) {
			mPlaneWindowView.setGrounded(mIsGrounded);
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

	//////////////////////////////////////////////////////////////////////////
	// PlaneWindowListener

	@Override
	public void onFirstRender() {
		if (showDefaultPlaneWindowAnimation) {
			setCoverEnabled(false);
		}
	}
}
