package com.expedia.bookings.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.interfaces.IPhoneLaunchActivityLaunchFragment;
import com.expedia.bookings.otto.Events;
import com.mobiata.android.Log;
import com.mobiata.android.util.NetUtils;

public class PhoneLaunchFragment extends Fragment implements IPhoneLaunchActivityLaunchFragment {

	// Invisible Fragment that handles FusedLocationProvider
	private FusedLocationProviderFragment locationFragment;

	private boolean wasOffline;

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
		Events.post(new Events.LaunchLobRefresh());
		if (checkConnection()) {
			onReactToUserActive();
		}

		IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		getActivity().registerReceiver(broadcastReceiver, filter);
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(broadcastReceiver);
		Events.unregister(this);
	}

	private void onReactToUserActive() {
		if (!checkConnection()) {
			return;
		}
		else {
			findLocation();
			signalAirAttachState();
		}
	}

	// Connectivity

	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i("Detected connectivity change, checking connection...");

			// If we changed state, react
			boolean isOffline = !checkConnection();

			if (isOffline != wasOffline) {
				Log.i("Connectivity changed from " + wasOffline + " to " + isOffline);
			}
			if (isOffline) {
				cleanUp();
			}
		}
	};

	private boolean checkConnection() {
		Context context = getActivity();
		if (context != null && !NetUtils.isOnline(context)) {
			wasOffline = true;
			Events.post(new Events.LaunchOfflineState());
			return false;
		}
		else {
			wasOffline = false;
			Events.post(new Events.LaunchOnlineState());
			return true;
		}
	}

	// Location finder

	private void findLocation() {

		locationFragment.find(new FusedLocationProviderFragment.FusedLocationProviderListener() {

			@Override
			public void onFound(Location currentLocation) {
				boolean isUserBucketedForTest = Db.getAbacusResponse()
					.isUserBucketedForTest(AbacusUtils.EBAndroidAppLaunchScreenTest);
				if (isUserBucketedForTest) {
					// show collection data to users irrespective of location Abacus A/B test
					Events.post(new Events.LaunchLocationFetchError());
				}
				else {
					Events.post(new Events.LaunchLocationFetchComplete(currentLocation));
				}
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
