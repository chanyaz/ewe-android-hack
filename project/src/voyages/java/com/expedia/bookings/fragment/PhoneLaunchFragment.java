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

/**
 * This class exists to support the build system and the way in which we provide different
 * LaunchFragment implementations for a given product flavor.
 */
public class PhoneLaunchFragment extends Fragment implements IPhoneLaunchActivityLaunchFragment {
	private boolean mLaunchingActivity = false;
	private ImageView mSlidingImage;
	private int mCurrentImageIndex = 0;

	private static final Integer[] BACKGROUND_RES_IDS = new Integer[] {
		R.drawable.bg_launch_vsc_paris,
		R.drawable.bg_launch_vsc_london,
		R.drawable.bg_launch_vsc_lyon,
		R.drawable.bg_launch_vsc_marseille,
		R.drawable.bg_launch_vsc_paris,
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		final View view = inflater.inflate(R.layout.fragment_phone_launch, container, false);
		Ui.findView(view, R.id.vsc_hotel_button).setOnClickListener(mHeaderItemOnClickListener);
		Ui.findView(view, R.id.vsc_flight_button).setOnClickListener(mHeaderItemOnClickListener);
		mSlidingImage = Ui.findView(view, R.id.vsc_home_bg_view);

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
			case R.id.vsc_hotel_button: {
				if (!mLaunchingActivity) {
					mLaunchingActivity = true;
					NavUtils.goToHotels(getActivity(), animOptions);
					OmnitureTracking.trackLinkLaunchScreenToHotels(getActivity());
				}
				break;
			}
			case R.id.vsc_flight_button: {
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
	public boolean onBackPressed() {
		return false;
	}
}
