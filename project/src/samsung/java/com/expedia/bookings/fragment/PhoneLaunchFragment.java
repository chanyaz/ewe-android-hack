package com.expedia.bookings.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.activity.PhoneLaunchActivity;
import com.expedia.bookings.data.collections.CollectionLocation;
import com.expedia.bookings.interfaces.IPhoneLaunchActivityLaunchFragment;
import com.expedia.bookings.interfaces.IPhoneLaunchFragmentListener;
import com.expedia.bookings.location.CurrentLocationObservable;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.widget.CollectionLaunchWidget;
import com.expedia.bookings.widget.DisableableViewPager;
import com.mobiata.android.Log;
import com.mobiata.android.util.NetUtils;
import com.squareup.otto.Subscribe;

import rx.Observer;
import rx.Subscription;

public class PhoneLaunchFragment extends Fragment implements IPhoneLaunchActivityLaunchFragment {

	private Subscription locSubscription;
	private boolean wasOffline;

	private CollectionLaunchWidget collectionLaunchWidget;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.widget_phone_launch, container, false);
		collectionLaunchWidget = (CollectionLaunchWidget) LayoutInflater.from(getActivity()).inflate(
			R.layout.widget_collection_launch,
			(ViewGroup) rootView, false);
		((ViewGroup) rootView).addView(collectionLaunchWidget);
		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		Events.register(this);
		Events.post(new Events.PhoneLaunchOnResume());

		if (checkConnection()) {
			findLocation();
		}

		IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		getActivity().registerReceiver(broadcastReceiver, filter);
		OmnitureTracking.onResume(getActivity());
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
		updateCollectionDetailsView(event.collectionLocation, event.collectionUrl);
		switchView(true);
	}

	private void switchView(boolean isCollectionClicked) {
		ActionBar actionBar = ((PhoneLaunchActivity) getActivity()).getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(isCollectionClicked);
		actionBar.setHomeButtonEnabled(isCollectionClicked);
		actionBar.setBackgroundDrawable(new ColorDrawable(isCollectionClicked ? Color.TRANSPARENT
			: getResources().getColor(R.color.launch_actionbar_bg_color_samsung)));

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
