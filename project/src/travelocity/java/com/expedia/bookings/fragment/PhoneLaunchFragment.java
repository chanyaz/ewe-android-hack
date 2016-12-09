package com.expedia.bookings.fragment;

import java.util.Random;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.interfaces.IPhoneLaunchActivityLaunchFragment;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AnimUtils;
import com.expedia.bookings.utils.NavUtils;
import com.mobiata.android.util.Ui;

public class PhoneLaunchFragment extends Fragment implements IPhoneLaunchActivityLaunchFragment {
	private static final int MY_PERMISSIONS_REQUEST_LOCATION = 7;
	public static final String TAG = PhoneLaunchFragment.class.getName();
	private boolean mLaunchingActivity = false;
	private ImageView mSlidingImage;
	private int mCurrentImageIndex = 0;

	private static final int[] BACKGROUND_RES_IDS = new int[] {
		R.drawable.bg_launch_tvly_maldives
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_phone_launch, container, false);
		Ui.findView(view, R.id.tvly_flights).setOnClickListener(mHeaderItemOnClickListener);
		Ui.findView(view, R.id.tvly_hotels).setOnClickListener(mHeaderItemOnClickListener);
		Ui.findView(view, R.id.tvly_cars).setOnClickListener(mHeaderItemOnClickListener);
		Ui.findView(view, R.id.tvly_activities).setOnClickListener(mHeaderItemOnClickListener);
		mSlidingImage = Ui.findView(view, R.id.tvly_home_bg_view);

		//Randomly select an image to display
		mCurrentImageIndex = new Random().nextInt(BACKGROUND_RES_IDS.length);
		mSlidingImage.setImageResource(BACKGROUND_RES_IDS[mCurrentImageIndex]);

		ActionBar actionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();
		actionBar.setIcon(R.drawable.ic_ab_travelocity_logo);
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setElevation(0);

		int permissionCheck = ContextCompat.checkSelfPermission(getActivity(),
			Manifest.permission.ACCESS_FINE_LOCATION);

		if (permissionCheck != PackageManager.PERMISSION_GRANTED) {

			ActivityCompat.requestPermissions(getActivity(),
				new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
				MY_PERMISSIONS_REQUEST_LOCATION);
		}

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		mLaunchingActivity = false;
	}

	private final View.OnClickListener mHeaderItemOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Bundle animOptions = AnimUtils.createActivityScaleBundle(v);

			switch (v.getId()) {
				case R.id.tvly_hotels: {
					if (!mLaunchingActivity) {
						mLaunchingActivity = true;
						OmnitureTracking.trackLinkLaunchScreenToHotels();
						NavUtils.goToHotels(getActivity(), animOptions);
					}
					break;
				}
				case R.id.tvly_flights: {
					if (!mLaunchingActivity) {
						mLaunchingActivity = true;
						OmnitureTracking.trackLinkLaunchScreenToFlights();
						NavUtils.goToFlights(getActivity(), animOptions);
					}
					break;
				}
				case R.id.tvly_cars: {
					if (!mLaunchingActivity) {
						mLaunchingActivity = true;
						OmnitureTracking.trackNewLaunchScreenLobNavigation(LineOfBusiness.CARS);
						NavUtils.goToCars(getActivity(), animOptions);
					}
					break;
				}
				case R.id.tvly_activities: {
					if (!mLaunchingActivity) {
						mLaunchingActivity = true;
						OmnitureTracking.trackNewLaunchScreenLobNavigation(LineOfBusiness.LX);
						NavUtils.goToActivities(getActivity(), animOptions);
					}
					break;
				}
			}
		}
	};

	////////////////////////////////////////////////////////////
	// IPhoneLaunchActivityLaunchFragment
	//
	// Note: If you intend to add code to these methods, make sure to override
	// onAttach and invoke IPhoneLaunchFragmentListener.onLaunchFragmentAttached,
	// otherwise PhoneLaunchActivity will never grab reference to this Fragment
	// instance and thus will not be able to invoke the following methods.

	@Override
	public boolean onBackPressed() {
		return false;
	}
}
