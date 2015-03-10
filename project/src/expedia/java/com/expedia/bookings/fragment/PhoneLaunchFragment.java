package com.expedia.bookings.fragment;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.interfaces.IPhoneLaunchActivityLaunchFragment;
import com.expedia.bookings.otto.Events;
import com.mobiata.android.util.NetUtils;

import butterknife.ButterKnife;

public class PhoneLaunchFragment extends Fragment implements IPhoneLaunchActivityLaunchFragment {

	// Invisible Fragment that handles FusedLocationProvider
	private FusedLocationProviderFragment locationFragment;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		locationFragment = FusedLocationProviderFragment.getInstance(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.widget_phone_launch, container, false);
	}

	@Override
	public void onResume() {
		super.onResume();
		Events.register(this);
		// TODO no internet state
		if (!NetUtils.isOnline(getActivity())) {
			Events.post(new Events.LaunchLocationFetchError());
		}
		else {
			findLocation();
			signalAirAttachState();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		Events.unregister(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		ButterKnife.reset(this);
	}

	// Location finder

	private void findLocation() {

		locationFragment.find(new FusedLocationProviderFragment.FusedLocationProviderListener() {

			@Override
			public void onFound(Location currentLocation) {
				Events.post(new Events.LaunchLocationFetchComplete(currentLocation));
			}

			@Override
			public void onError() {
				Events.post(new Events.LaunchLocationFetchError());
			}
		});
	}

	private void signalAirAttachState() {
		if (Db.getTripBucket().isUserAirAttachQualified()) {
			final ItineraryManager itinMan = ItineraryManager.getInstance();
			final HotelSearchParams hotelSearchParams = itinMan.getHotelSearchParamsForAirAttach();
			Events.post(new Events.LaunchAirAttachBannerShow(hotelSearchParams));
		}
		else {
			Events.post(new Events.LaunchAirAttachBannerHide());
		}
	}

	// Listeners

	@Override
	public void startMarquee() {
		// ignore
	}

	@Override
	public void cleanUp() {
		// ignore
	}

	@Override
	public void reset() {
		// ignore
	}
}
