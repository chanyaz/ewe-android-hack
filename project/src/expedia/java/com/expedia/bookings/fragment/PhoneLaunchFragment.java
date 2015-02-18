package com.expedia.bookings.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.CarsActivity;
import com.expedia.bookings.interfaces.IPhoneLaunchActivityLaunchFragment;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AnimUtils;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.Ui;

public class PhoneLaunchFragment extends Fragment implements IPhoneLaunchActivityLaunchFragment {

	// Used to prevent launching of both flight and hotel activities at once
	// (as it is otherwise possible to quickly click on both sides).
	private boolean mLaunchingActivity;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_new_phone_launch, container, false);

		Ui.findView(v, R.id.hotels_launch_button).setOnClickListener(mHeaderItemOnClickListener);
		Ui.findView(v, R.id.flights_launch_button).setOnClickListener(mHeaderItemOnClickListener);
		Ui.findView(v, R.id.cars_launch_button).setOnClickListener(mHeaderItemOnClickListener);

		return v;
	}

	@Override
	public void onResume() {
		super.onResume();

		mLaunchingActivity = false;

	}

	// Listeners

	private final View.OnClickListener mHeaderItemOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Bundle animOptions = AnimUtils.createActivityScaleBundle(v);

			switch (v.getId()) {
			case R.id.hotels_launch_button:
				if (!mLaunchingActivity) {
					mLaunchingActivity = true;
					NavUtils.goToHotels(getActivity(), animOptions);
					OmnitureTracking.trackLinkLaunchScreenToHotels(getActivity());
				}
				break;
			case R.id.flights_launch_button:
				if (!mLaunchingActivity) {
					mLaunchingActivity = true;
					NavUtils.goToFlights(getActivity(), animOptions);
					OmnitureTracking.trackLinkLaunchScreenToFlights(getActivity());
				}
				break;
			case R.id.cars_launch_button:
				Intent carsIntent = new Intent(getActivity(), CarsActivity.class);
				getActivity().startActivity(carsIntent);
				break;
			}

			cleanUp();
		}
	};

	@Override
	public void startMarquee() {

	}

	@Override
	public void cleanUp() {

	}

	@Override
	public void reset() {

	}
}
