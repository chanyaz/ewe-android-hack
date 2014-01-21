package com.expedia.bookings.fragment;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.interfaces.IBackManageable;
import com.expedia.bookings.interfaces.helpers.BackManager;
import com.mobiata.android.util.Ui;

/**
 *  TabletCheckoutControllerFragment: designed for tablet checkout 2014
 *  This controls all the fragments relating to tablet checkout
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class TabletCheckoutControllerFragment extends Fragment implements IBackManageable {

	private static final String FRAG_TAG_BUCKET_FLIGHT = "FRAG_TAG_BUCKET_FLIGHT";
	private static final String FRAG_TAG_BUCKET_HOTEL = "FRAG_TAG_BUCKET_HOTEL";
	private static final String FRAG_TAG_CHECKOUT_INFO = "FRAG_TAG_CHECKOUT_INFO";

	//Containers
	private ViewGroup mRootC;
	private ViewGroup mTripBucketContainer;
	private ViewGroup mBucketHotelContainer;
	private ViewGroup mBucketFlightContainer;
	private ViewGroup mCheckoutFormsContainer;

	//Views
	private TextView mBucketDateRange;

	//frags
	private ResultsTripBucketFlightFragment mBucketFlightFrag;
	private ResultsTripBucketHotelFragment mBucketHotelFrag;
	private FlightCheckoutFragment mCheckoutFragment;

	//vars
	private LineOfBusiness mLob = LineOfBusiness.FLIGHTS;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_tablet_checkout_controller, null, false);

		mRootC = Ui.findView(view, R.id.root_layout);
		mTripBucketContainer = Ui.findView(view, R.id.trip_bucket_container);
		mCheckoutFormsContainer = Ui.findView(view, R.id.checkout_forms_container);

		mBucketDateRange = Ui.findView(view, R.id.trip_date_range);
		mBucketDateRange.setText("FEB 8 - CAT 12");//TODO: real date range

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		mBackManager.registerWithParent(this);

		setupBucketFrags();
		attachCheckoutFragment();
	}

	@Override
	public void onPause() {
		super.onPause();
		mBackManager.unregisterWithParent(this);
	}

	/*
	 * GETTERS/SETTERS
	 */

	public void setCheckoutMode(LineOfBusiness lob) {
		mLob = lob;
	}

	public LineOfBusiness getCheckoutMode() {
		return mLob;
	}

	/*
	 * CHECKOUT INFO FRAGMENT
	 */

	public void attachCheckoutFragment() {
		FragmentManager manager = getFragmentManager();
		mCheckoutFragment = (FlightCheckoutFragment) manager.findFragmentByTag(FRAG_TAG_CHECKOUT_INFO);
		if (mCheckoutFragment == null) {
			mCheckoutFragment = FlightCheckoutFragment.newInstance();
		}

		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		if (mCheckoutFragment.isDetached()) {
			transaction.attach(mCheckoutFragment);
			transaction.commit();
		}
		else if (!mCheckoutFragment.isAdded()) {
			transaction.add(R.id.checkout_forms_container, mCheckoutFragment, FRAG_TAG_CHECKOUT_INFO);
			transaction.commit();
		}

	}

	/*
	 * BUCKET FRAGMENTS
	 */

	private void setupBucketFrags() {

		//TODO: WE ONLY WANT TO SHOW A PARTICULAR BUCKET FRAG IF WE HAVE DATA,
		//WE SHOULD BE CHECKING THAT DATA HERE!

		if (mBucketFlightFrag == null || !mBucketFlightFrag.isAdded()) {
			attachBucketFlightFrag();
			mBucketFlightFrag.setExpanded(mLob == LineOfBusiness.FLIGHTS);
			mBucketFlightFrag.setShowButton(false);
		}
		if (mBucketHotelFrag == null || !mBucketHotelFrag.isAdded()) {
			attachBucketHotelFrag();
			mBucketHotelFrag.setExpanded(mLob == LineOfBusiness.HOTELS);
			mBucketHotelFrag.setShowButton(false);
		}
	}

	private void attachBucketFlightFrag() {
		FragmentManager manager = getFragmentManager();
		if (mBucketFlightFrag == null) {
			mBucketFlightFrag = (ResultsTripBucketFlightFragment) manager.findFragmentByTag(FRAG_TAG_BUCKET_FLIGHT);
		}
		if (mBucketFlightFrag == null) {
			mBucketFlightFrag = ResultsTripBucketFlightFragment.newInstance();
		}
		if (mBucketFlightFrag != null && !mBucketFlightFrag.isAdded()) {
			FragmentTransaction transaction = manager.beginTransaction();
			transaction.add(R.id.bucket_flight_frag_container, mBucketFlightFrag, FRAG_TAG_BUCKET_FLIGHT);
			transaction.commit();
		}
	}

	private void attachBucketHotelFrag() {
		FragmentManager manager = getFragmentManager();
		if (mBucketHotelFrag == null) {
			mBucketHotelFrag = (ResultsTripBucketHotelFragment) manager.findFragmentByTag(FRAG_TAG_BUCKET_HOTEL);
		}
		if (mBucketHotelFrag == null) {
			mBucketHotelFrag = ResultsTripBucketHotelFragment.newInstance();
		}
		if (mBucketHotelFrag != null && !mBucketHotelFrag.isAdded()) {
			FragmentTransaction transaction = manager.beginTransaction();
			transaction.add(R.id.bucket_hotel_frag_container, mBucketHotelFrag, FRAG_TAG_BUCKET_HOTEL);
			transaction.commit();
		}
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