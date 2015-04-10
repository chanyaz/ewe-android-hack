package com.expedia.bookings.location;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import com.expedia.bookings.data.cars.ApiError;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.mobiata.android.Log;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

public class CurrentLocationObservable implements
	GoogleApiClient.ConnectionCallbacks,
	Observable.OnSubscribe<Location> {

	public static Observable<Location> create(Context context) {
		return Observable.create(new CurrentLocationObservable(context));
	}

	private GoogleApiClient googleApiClient;
	Subscriber<? super Location> subscriber;

	private CurrentLocationObservable(Context context) {
		googleApiClient = new GoogleApiClient.Builder(context)
			.addApi(LocationServices.API)
			.addConnectionCallbacks(this)
			.build();
	}

	//////////////////////////////////////////////////////////////////////////
	// ConnectionCallbacks

	@Override
	public void onConnected(Bundle connectionHint) {
		Log.d("GoogleApiClient LocationServices: connected");
		deliverLocation();
	}

	@Override
	public void onConnectionSuspended(int i) {
		// ignore
	}

	private synchronized void deliverLocation() {
		if (googleApiClient == null || !googleApiClient.isConnected()) {
			return;
		}

		Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
		if (location == null) {
			ApiError error = new ApiError(ApiError.Code.LOCATION_SERVICES_DISABLED);
			subscriber.onError(error);
		}
		else {
			subscriber.onNext(location);
		}
		subscriber.onCompleted();
	}

	//////////////////////////////////////////////////////////////////////////
	// OnSubscribe

	@Override
	public void call(Subscriber<? super Location> subscriber) {
		this.subscriber = subscriber;

		subscriber.add(Subscriptions.create(disconnectAction));

		Log.d("GoogleApiClient LocationServices: connect");
		googleApiClient.connect();
	}

	private final Action0 disconnectAction = new Action0() {
		@Override
		public void call() {
			Log.d("GoogleApiClient LocationServices: disconnect");
			googleApiClient.disconnect();
		}
	};
}
