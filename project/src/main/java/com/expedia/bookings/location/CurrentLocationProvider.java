package com.expedia.bookings.location;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import com.expedia.bookings.data.cars.ApiError;
import com.expedia.bookings.data.cars.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

public class CurrentLocationProvider {
	public static Observable<Location> currentLocation(Context context) {
		return Observable.create(new OnSubscribeCurrentLocation(context));
	}

	static class OnSubscribeCurrentLocation implements
		GoogleApiClient.ConnectionCallbacks,
		Observable.OnSubscribe<Location> {

		private GoogleApiClient googleApiClient;

		public OnSubscribeCurrentLocation(Context context) {
			googleApiClient = new GoogleApiClient.Builder(context)
				.addApi(LocationServices.API)
				.addConnectionCallbacks(this)
				.build();
		}

		//////////////////////////////////////////////////////////////////////////
		// ConnectionCallbacks, OnConnectionFailedListener

		@Override
		public void onConnected(Bundle connectionHint) {
			deliverLocation();
		}

		@Override
		public void onConnectionSuspended(int i) {
		}

		public synchronized void deliverLocation() {
			if (googleApiClient == null || !googleApiClient.isConnected()) {
				return;
			}

			Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
			if (location == null) {
				ApiError apiError = new ApiError();
				apiError.errorCode = ApiError.Code.LOCATION_SERVICES_DISABLED;
				observer.onError(new ApiException(apiError));
			}
			else {
				observer.onNext(location);
			}
			observer.onCompleted();
		}

		Subscriber<? super Location> observer;

		@Override
		public void call(Subscriber<? super Location> observer) {
			googleApiClient.connect();
			this.observer = observer;

			final Subscription subscription = Subscriptions.create(new Action0() {
				@Override
				public void call() {
					googleApiClient.disconnect();
				}
			});

			this.observer.add(subscription);
		}
	}
}
