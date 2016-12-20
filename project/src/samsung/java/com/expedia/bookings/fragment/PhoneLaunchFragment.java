package com.expedia.bookings.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.activity.PhoneLaunchActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.collections.CollectionLocation;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.interfaces.IPhoneLaunchActivityLaunchFragment;
import com.expedia.bookings.interfaces.IPhoneLaunchFragmentListener;
import com.expedia.bookings.location.CurrentLocationObservable;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.widget.CollectionLaunchWidget;
import com.expedia.bookings.widget.DisableableViewPager;
import com.expedia.bookings.widget.PhoneLaunchWidget;
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

	private CollectionLaunchWidget collectionLaunchWidget;

	@InjectView(R.id.phone_launch_widget)
	PhoneLaunchWidget phoneLaunchWidget;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.widget_phone_launch, container, false);
		collectionLaunchWidget = (CollectionLaunchWidget) LayoutInflater.from(getActivity()).inflate(
				R.layout.widget_collection_launch,
				(ViewGroup) rootView, false);
		((ViewGroup) rootView).addView(collectionLaunchWidget);

		ButterKnife.inject(this, rootView);
		int permissionCheck = ContextCompat.checkSelfPermission(getActivity(),
				Manifest.permission.ACCESS_FINE_LOCATION);

		if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(getActivity(),
					new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
					MY_PERMISSIONS_REQUEST_LOCATION);
		}
		return rootView;
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

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		((IPhoneLaunchFragmentListener) activity).onLaunchFragmentAttached(this);
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
		if (collectionLaunchWidget.getVisibility() == View.VISIBLE) {
			switchView(false);
			return true;
		}
		return false;
	}

	// Hotel search in collection location
	@Subscribe
	public void onCollectionLocationSelected(Events.LaunchCollectionItemSelected event) {
		// CollectionLaunchWidget will not be launched in case of KR POS.
		if (PointOfSale.getPointOfSale().getPointOfSaleId() == PointOfSaleId.SOUTH_KOREA) {
			collectionLaunchWidget.hotelClicked();
		}
		else {
			updateCollectionDetailsView(event.collectionLocation, event.collectionUrl);
			switchView(true);
		}
	}

	private void switchView(boolean isCollectionClicked) {
		ActionBar actionBar = ((PhoneLaunchActivity) getActivity()).getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(isCollectionClicked);
		actionBar.setHomeButtonEnabled(isCollectionClicked);
		actionBar.setBackgroundDrawable(new ColorDrawable(isCollectionClicked ? Color.TRANSPARENT
			: getResources().getColor(R.color.launch_toolbar_background_color)));

		collectionLaunchWidget.setVisibility(isCollectionClicked ? View.VISIBLE : View.GONE);
		((DisableableViewPager) getActivity().findViewById(R.id.viewpager)).setPageSwipingEnabled(!isCollectionClicked);

		Toolbar toolBar = ((PhoneLaunchActivity) getActivity()).getToolbar();
		toolBar.findViewById(R.id.collection_layout).setVisibility(isCollectionClicked ? View.VISIBLE : View.GONE);
		toolBar.findViewById(R.id.tab_layout).setVisibility(isCollectionClicked ? View.GONE : View.VISIBLE);
	}

	private void updateCollectionDetailsView(CollectionLocation collectionLocation, String collectionUrl) {
		Toolbar toolBar = ((PhoneLaunchActivity) getActivity()).getToolbar();
		((TextView) toolBar.findViewById(R.id.locationName)).setText(collectionLocation.title);
		((TextView) toolBar.findViewById(R.id.locationCountryName)).setText(collectionLocation.subtitle);

		collectionLaunchWidget.updateWidget(collectionLocation, collectionUrl);
	}
}
