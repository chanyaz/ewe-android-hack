package com.expedia.bookings.fragment;

import java.util.concurrent.TimeUnit;

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

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.abacus.AbacusEvaluateQuery;
import com.expedia.bookings.data.abacus.AbacusResponse;
import com.expedia.bookings.data.abacus.AbacusTest;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.interfaces.IPhoneLaunchActivityLaunchFragment;
import com.expedia.bookings.location.CurrentLocationObservable;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.services.AbacusServices;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;
import com.mobiata.android.util.NetUtils;
import com.mobiata.android.util.SettingUtils;

import rx.Observer;
import rx.Subscription;

public class PhoneLaunchFragment extends Fragment implements IPhoneLaunchActivityLaunchFragment {

	private Subscription locSubscription;
	private boolean wasOffline;
	private AbacusResponse launchScreenTest;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.widget_phone_launch, container, false);
	}

	@Override
	public void onResume() {
		super.onResume();
		Events.register(this);
		Events.post(new Events.PhoneLaunchOnResume());
		if (checkConnection()) {
			bucketLaunchScreen();
		}

		IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		getActivity().registerReceiver(broadcastReceiver, filter);
		OmnitureTracking.onResume(getActivity());

		AbacusTest test = null;
		if (launchScreenTest != null) {
			test = launchScreenTest.testForKey(AbacusUtils.EBAndroidAppLaunchScreenTest);
		}
		OmnitureTracking.trackPageLoadLaunchScreen(getActivity(), test);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (locSubscription != null) {
			locSubscription.unsubscribe();
		}
		getActivity().unregisterReceiver(broadcastReceiver);
		Events.unregister(this);
		OmnitureTracking.onPause();
	}

	private void onReactToUserActive() {
		if (!checkConnection()) {
			return;
		}
		else {
			boolean isUserBucketedForTest = launchScreenTest.isUserBucketedForTest(AbacusUtils.EBAndroidAppLaunchScreenTest);
			if (isUserBucketedForTest) {
				// show collection data to users irrespective of location Abacus A/B test
				Events.post(new Events.LaunchLocationFetchError());
			}
			else {
				findLocation();
			}
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
		if (context != null && !NetUtils.isOnline(context) && !ExpediaBookingApp.sIsAutomation) {
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

	public void bucketLaunchScreen() {
		if (launchScreenTest == null) {
			AbacusEvaluateQuery query = new AbacusEvaluateQuery(
				Db.getAbacusGuid(), PointOfSale.getPointOfSale().getTpid(), 0);
			query.addExperiment(AbacusUtils.EBAndroidAppLaunchScreenTest);
			Ui.getApplication(getActivity()).appComponent().abacus().downloadBucket(query, abacusSubscriber,
				AbacusServices.TIMEOUT_5_SECONDS, TimeUnit.SECONDS);
		}
		else {
			// onResume, could be returning from dev settings so we should update the test
			updateAbacus(launchScreenTest);
			onReactToUserActive();
		}
	}

	private Observer<AbacusResponse> abacusSubscriber = new Observer<AbacusResponse>() {
		@Override
		public void onCompleted() {
			Log.d("AbacusReponse - onCompleted");
		}

		@Override
		public void onError(Throwable e) {
			updateAbacus(new AbacusResponse());
			onReactToUserActive();
			Log.d("AbacusReponse - onError", e);
		}

		@Override
		public void onNext(AbacusResponse abacusResponse) {
			updateAbacus(abacusResponse);
			onReactToUserActive();
			Log.d("AbacusReponse - onNext");
		}
	};

	private void updateAbacus(AbacusResponse abacusResponse) {
		if (ExpediaBookingApp.sIsAutomation) {
			launchScreenTest = new AbacusResponse();
			return;
		}

		launchScreenTest = abacusResponse;

		if (BuildConfig.DEBUG) {
			launchScreenTest.updateABTestForDebug(AbacusUtils.EBAndroidAppLaunchScreenTest, SettingUtils
				.get(getActivity(), String.valueOf(AbacusUtils.EBAndroidAppLaunchScreenTest),
					AbacusUtils.ABTEST_IGNORE_DEBUG));
		}
	}

}
