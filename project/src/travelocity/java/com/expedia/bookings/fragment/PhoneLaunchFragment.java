package com.expedia.bookings.fragment;

import java.util.Random;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.interfaces.IPhoneLaunchActivityLaunchFragment;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AnimUtils;
import com.expedia.bookings.utils.NavUtils;
import com.mobiata.android.util.Ui;

public class PhoneLaunchFragment extends Fragment implements IPhoneLaunchActivityLaunchFragment {

	public static final String TAG = PhoneLaunchFragment.class.getName();
	private boolean mLaunchingActivity = false;
	private ImageView mSlidingImage;
	private int mCurrentImageIndex = 0;

	private static final Integer[] BACKGROUND_RES_IDS = new Integer[] {
		R.drawable.bg_launch_tvly_sf,
		R.drawable.bg_launch_tvly_las_vegas,
		R.drawable.bg_launch_tvly_london,
		R.drawable.bg_launch_tvly_paris,
		R.drawable.bg_launch_tvly_swiss,
		R.drawable.bg_launch_tvly_japan,
		R.drawable.bg_launch_tvly_rome,
		R.drawable.bg_launch_tvly_venice,
		R.drawable.bg_launch_tvly_seattle,
		R.drawable.bg_launch_tvly_ny,
		R.drawable.bg_launch_tvly_chicago,
		R.drawable.bg_launch_tvly_china,
		R.drawable.bg_launch_tvly_dallas,
		R.drawable.bg_launch_tvly_arizona,
		R.drawable.bg_launch_tvly_maldives,
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_phone_launch, container, false);
		Ui.findView(view, R.id.tvly_flights).setOnClickListener(mHeaderItemOnClickListener);
		Ui.findView(view, R.id.tvly_hotels).setOnClickListener(mHeaderItemOnClickListener);
		mSlidingImage = Ui.findView(view, R.id.tvly_home_bg_view);

		//Randomly select an image to display
		mCurrentImageIndex = new Random().nextInt(BACKGROUND_RES_IDS.length);
		mSlidingImage.setImageResource(BACKGROUND_RES_IDS[mCurrentImageIndex]);
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
					NavUtils.goToHotels(getActivity(), animOptions);
					OmnitureTracking.trackLinkLaunchScreenToHotels(getActivity());
				}
				break;
			}
			case R.id.tvly_flights: {
				if (!mLaunchingActivity) {
					mLaunchingActivity = true;
					NavUtils.goToFlights(getActivity(), animOptions);
					OmnitureTracking.trackLinkLaunchScreenToFlights(getActivity());
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
	public void startMarquee() {
		// No work required
	}

	@Override
	public void cleanUp() {
		// No work required
	}

	@Override
	public void reset() {
		// No work required
	}
}
