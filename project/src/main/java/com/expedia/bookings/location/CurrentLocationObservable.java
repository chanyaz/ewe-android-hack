package com.expedia.bookings.location;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import com.expedia.bookings.data.ApiError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.mobiata.android.Log;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

public class CurrentLocationObservable implements
	GoogleApiClient.ConnectionCallbacks,
	Observable.OnSubscribe<Location>, GoogleApiClient.OnConnectionFailedListener {

	public static Observable<Location> create(Context context) {
		return Observable.create(new CurrentLocationObservable(context));
	}

	private final GoogleApiClient googleApiClient;
	Subscriber<? super Location> subscriber;

	private CurrentLocationObservable(Context context) {
		googleApiClient = new GoogleApiClient.Builder(context)
			.addApi(LocationServices.API)
			.addConnectionCallbacks(this)
			.addOnConnectionFailedListener(this)
			.build();
	}

	//////////////////////////////////////////////////////////////////////////
	// ConnectionCallbacks

	@Override
	public synchronized void onConnected(Bundle connectionHint) {
		Log.d("CurrentLocationObservable: onConnected");
		//Deliver the location
		Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
		if (location == null) {
			sendError();
		}
		else {
			subscriber.onNext(location);
			subscriber.onCompleted();
		}
	}

	@Override
	public synchronized void onConnectionSuspended(int i) {
		Log.d("CurrentLocationObservable: onConnectionSuspended");
		// ignore as GoogleApiClient will automatically attempt to restore the connection.
	}

	//////////////////////////////////////////////////////////////////////////
	// OnSubscribe

	@Override
	public void call(Subscriber<? super Location> subscriber) {
		this.subscriber = subscriber;

		subscriber.add(Subscriptions.create(disconnectAction));

		//Attempt to connect to Google Api Client on Subscribe
		googleApiClient.connect();
	}

	private final Action0 disconnectAction = new Action0() {
		@Override
		public void call() {
			Log.d("CurrentLocationObservable: Disconnecting");
			googleApiClient.disconnect();
		}
	};

	@Override
	public synchronized void onConnectionFailed(ConnectionResult connectionResult) {
		Log.d("CurrentLocationObservable: onConnectionFailed");
		sendError();
	}

	private void sendError() {
		ApiError error = new ApiError(ApiError.Code.CURRENT_LOCATION_ERROR);
		ApiError.ErrorInfo errorInfo = new ApiError.ErrorInfo();
		errorInfo.cause = "Could not determine users current location.";
		error.errorInfo = errorInfo;
		subscriber.onError(error);
	}
}
