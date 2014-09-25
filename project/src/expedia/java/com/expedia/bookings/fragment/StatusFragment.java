package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.fragment.base.AbsStatusFragment;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.LayoutUtils;
import com.expedia.bookings.widget.PlaneWindowView;
import com.expedia.bookings.widget.PlaneWindowView.PlaneWindowListener;
import com.mobiata.android.util.Ui;

public class StatusFragment extends AbsStatusFragment implements PlaneWindowListener {

	private PlaneWindowView mPlaneWindowView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);

		mPlaneWindowView = Ui.findView(v, R.id.plane_window_view);
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
	public void onStop() {
		super.onStop();
		mPlaneWindowView.setRendering(false);
	}

	@Override
	protected void displayStatus() {
		super.displayStatus();
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
