package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.joda.time.LocalDate;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.InjectView;
import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.abacus.AbacusEvaluateQuery;
import com.expedia.bookings.data.abacus.AbacusResponse;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.collections.CollectionLocation;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.interfaces.IPhoneLaunchActivityLaunchFragment;
import com.expedia.bookings.location.CurrentLocationObservable;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.Constants;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.utils.UserAccountRefresher;
import com.expedia.bookings.widget.PhoneLaunchWidget;
import com.expedia.util.PermissionsHelperKt;
import com.mobiata.android.Log;
import com.mobiata.android.util.NetUtils;
import com.mobiata.android.util.SettingUtils;
import com.squareup.otto.Subscribe;
import rx.Observer;
import rx.Subscription;

public class PhoneLaunchFragment extends Fragment implements IPhoneLaunchActivityLaunchFragment {
	private Subscription locSubscription;
	private Subscription abacusSubscription;
	private boolean wasOffline;
	private List<Integer> abacusTestsAssociatedToPhoneLaunchScreen = Arrays
		.asList(AbacusUtils.EBAndroidAppSplitGTandActivities,
			AbacusUtils.EBAndroidAppHotelsSearchScreenTest);

	@InjectView(R.id.phone_launch_widget)
	PhoneLaunchWidget phoneLaunchWidget;

	UserAccountRefresher userAccountRefresher;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.widget_phone_launch, container, false);
		ButterKnife.inject(this, view);
		userAccountRefresher = new UserAccountRefresher(getContext(), LineOfBusiness.PROFILE, null);
		if (!PermissionsHelperKt.havePermissionToAccessLocation(getActivity())) {
			PermissionsHelperKt.requestLocationPermission(this);
		}
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		Events.register(this);
		Events.post(new Events.PhoneLaunchOnResume());
		if (checkConnection()) {
			bucketLaunchScreen();
			userAccountRefresher.ensureAccountIsRefreshed();
		}
		else {
			phoneLaunchWidget.bindLobWidget();
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
		if (abacusSubscription != null) {
			abacusSubscription.unsubscribe();
		}
		getActivity().unregisterReceiver(broadcastReceiver);
		Events.unregister(this);
	}

	private void onReactToUserActive() {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				phoneLaunchWidget.bindLobWidget();
			}
		});
		if (!checkConnection()) {
			return;
		}
		else {
			int permissionCheck = ContextCompat.checkSelfPermission(getActivity(),
				Manifest.permission.ACCESS_FINE_LOCATION);

			if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
				// show collection data to users irrespective of location Abacus A/B test
				Events.post(new Events.LaunchLocationFetchError());
			}
			else {
				findLocation();
			}
			signalAirAttachState();
			OmnitureTracking.trackPageLoadAbacusTestResults();
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


	@Override
	public boolean onBackPressed() {
		return false;
	}

	private void bucketLaunchScreen() {

		if (Db.getAbacusResponse() == null || !areAllLaunchScreenExperimentsEvaluated()) {
			AbacusEvaluateQuery query = new AbacusEvaluateQuery(Db.getAbacusGuid(),
				PointOfSale.getPointOfSale().getTpid(),
				0);
			query.addExperiments(getLaunchScreenTestsToEvaluate());
			abacusSubscription = Ui.getApplication(getActivity()).appComponent()
				.abacus()
				.downloadBucket(query, abacusObserver, 5, TimeUnit.SECONDS);
		}
		else {
			// onResume, could be returning from dev settings so we should update the test
			updateAbacus(Db.getAbacusResponse());
			onReactToUserActive();
		}
	}

	private boolean areAllLaunchScreenExperimentsEvaluated() {
		for (Integer key : abacusTestsAssociatedToPhoneLaunchScreen) {
			if (Db.getAbacusResponse().testForKey(key) == null) {
				return false;
			}
		}
		return true;
	}

	private List<Integer> getLaunchScreenTestsToEvaluate() {
		List<Integer> testsToEvaluate = new ArrayList<>();
		for (Integer key : abacusTestsAssociatedToPhoneLaunchScreen) {
			if (Db.getAbacusResponse().testForKey(key) == null) {
				testsToEvaluate.add(key);
			}
		}
		return testsToEvaluate;
	}

	private Observer<AbacusResponse> abacusObserver = new Observer<AbacusResponse>() {
		@Override
		public void onCompleted() {
			Log.d("AbacusResponse - onCompleted");
		}

		@Override
		public void onError(Throwable e) {
			updateAbacus(new AbacusResponse());
			onReactToUserActive();
			Log.d("AbacusResponse - onError", e);
		}

		@Override
		public void onNext(AbacusResponse abacusResponse) {
			updateAbacus(abacusResponse);
			onReactToUserActive();
			Log.d("AbacusResponse - onNext");
		}
	};

	private void updateAbacus(AbacusResponse launchAbacusResponse) {
		if (ExpediaBookingApp.isAutomation()) {
			return;
		}

		if (Db.getAbacusResponse() != null) {
			Db.getAbacusResponse().updateFrom(launchAbacusResponse);
		}
		else {
			Db.setAbacusResponse(launchAbacusResponse);
		}

		if (BuildConfig.DEBUG) {
			for (Integer key : abacusTestsAssociatedToPhoneLaunchScreen) {
				Db.getAbacusResponse().updateABTestForDebug(key,
					SettingUtils.get(getActivity(),
						String.valueOf(key),
						AbacusUtils.ABTEST_IGNORE_DEBUG));
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		switch (requestCode) {
		case Constants.PERMISSION_REQUEST_LOCATION:
			// If request is cancelled, the result arrays are empty.
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				// permission granted! Do stuff?
			}
			else {
				// permission denied, boo! Disable the
				// functionality that depends on this permission.
			}
			return;
		}
	}

	// Hotel search in collection location
	@Subscribe
	public void onCollectionLocationSelected(Events.LaunchCollectionItemSelected event) {
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
		NavUtils.goToHotels(getActivity(), params, event.animOptions, 0);
	}
}
