package com.expedia.bookings.fragment;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.interfaces.IBackManageable;
import com.expedia.bookings.interfaces.helpers.BackManager;
import com.mobiata.android.util.Ui;

/**
 *  TabletCheckoutControllerFragment: designed for tablet checkout 2014
 *  This controls all the fragments relating to tablet checkout
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class TabletCheckoutControllerFragment extends Fragment implements IBackManageable {

	//Containers
	private ViewGroup mRootC;
	private ViewGroup mTripBucketContainer;
	private ViewGroup mCheckoutFormsContainer;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_tablet_checkout_controller, null, false);

		mRootC = Ui.findView(view, R.id.root_layout);
		mTripBucketContainer = Ui.findView(view, R.id.trip_bucket_container);
		mCheckoutFormsContainer = Ui.findView(view, R.id.checkout_forms_container);

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		mBackManager.registerWithParent(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		mBackManager.unregisterWithParent(this);
	}

	/*
	 * BACK STACK MANAGEMENT
	 */

	@Override
	public BackManager getBackManager() {
		return mBackManager;
	}

	private BackManager mBackManager = new BackManager(this) {

		@Override
		public boolean handleBackPressed() {
			return false;
		}

	};

}