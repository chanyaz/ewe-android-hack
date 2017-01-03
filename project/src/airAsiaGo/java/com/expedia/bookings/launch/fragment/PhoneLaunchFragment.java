package com.expedia.bookings.launch.fragment;

import org.joda.time.LocalDate;
import org.json.JSONException;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.collections.CollectionLocation;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.launch.interfaces.IPhoneLaunchActivityLaunchFragment;
import com.expedia.bookings.location.CurrentLocationObservable;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.launch.widget.PhoneLaunchWidget;
import com.mobiata.android.Log;
import com.mobiata.android.util.NetUtils;
import com.squareup.otto.Subscribe;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observer;
import rx.Subscription;

public class PhoneLaunchFragment extends Fragment implements IPhoneLaunchActivityLaunchFragment {

	private static final int MY_PERMISSIONS_REQUEST_LOCATION = 7;
	private Subscription locSubscription;
	private boolean wasOffline;

	@InjectView(R.id.phone_launch_widget)
	PhoneLaunchWidget phoneLaunchWidget;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.widget_phone_launch, container, false);
		ButterKnife.inject(this, view);

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
		Events.register(this);
		Events.post(new Events.PhoneLaunchOnResume());
		phoneLaunchWidget.bindLobWidget();
		if (checkConnection()) {
			findLocation();
			signalAirAttachState();
		}

		IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		getActivity().registerReceiver(broadcastReceiver, filter);
		OmnitureTracking.trackPageLoadLaunchScreen();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (locSubscription != null) {
			locSubscription.unsubscribe();
		}
		getActivity().unregisterReceiver(broadcastReceiver);
		Events.unregister(this);
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
		}
	};

	private boolean checkConnection() {
		Context context = getActivity();
		if (context != null && !NetUtils.isOnline(context) && !ExpediaBookingApp.isAutomation()) {
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
		locSubscription = CurrentLocationObservable.create(getActivity()).subscribe(new Observer<Location>() {
			@Override
			public void onCompleted() {
				// ignore
			}

			@Override
			public void onError(Throwable e) {
				Events.post(new Events.LaunchLocationFetchError());
			}

			@Override
			public void onNext(Location currentLocation) {
				Events.post(new Events.LaunchLocationFetchComplete(currentLocation));
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
	public boolean onBackPressed() {
		return false;
	}

	// Hotel search in collection location
	@Subscribe
	public void onCollectionLocationSelected(Events.LaunchCollectionItemSelected event) throws JSONException {
		CollectionLocation.Location location = event.collectionLocation.location;
		HotelSearchParams params = new HotelSearchParams();
		params.setQuery(location.shortName);
		params.setSearchType(HotelSearchParams.SearchType.valueOf(location.type));
		params.setRegionId(location.id);
		params.setSearchLatLon(location.latLong.lat, location.latLong.lng);
		LocalDate now = LocalDate.now();
		params.setCheckInDate(now.plusDays(1));
		params.setCheckOutDate(now.plusDays(2));
		params.setNumAdults(2);
		params.setChildren(null);
		NavUtils.goToHotels(getContext(), params, event.animOptions, 0);
	}
}
