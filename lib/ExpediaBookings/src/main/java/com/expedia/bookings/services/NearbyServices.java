package com.expedia.bookings.services;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.List;

import org.joda.time.DateTime;

import com.expedia.bookings.data.hotels.NearbyHotelOffer;
import com.expedia.bookings.data.hotels.NearbyHotelParams;
import com.expedia.bookings.data.hotels.NearbyHotelResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;
import rx.Observable;
import rx.Scheduler;
import rx.Subscription;
import rx.functions.Func1;

public class NearbyServices {

	Scheduler mObserveOn;
	Scheduler mSubscribeOn;
	NearbyHotelApi mHotelApi;

	public NearbyServices(String endpoint, OkHttpClient okHttpClient, RequestInterceptor requestInterceptor,
		Scheduler observeOn, Scheduler subscribeOn) {
		mObserveOn = observeOn;
		mSubscribeOn = subscribeOn;
		CookieManager cookieManager = new CookieManager();
		cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
		okHttpClient.setCookieHandler(cookieManager);

		Gson gson = new GsonBuilder()
			.registerTypeAdapter(DateTime.class, new DateTimeTypeAdapter())
			.create();

		RestAdapter adapter = new RestAdapter.Builder()
			.setEndpoint(endpoint)
			.setRequestInterceptor(requestInterceptor)
			.setLogLevel(RestAdapter.LogLevel.FULL)
			.setConverter(new GsonConverter(gson))
			.setClient(new OkClient(okHttpClient))
			.build();

		mHotelApi = adapter.create(NearbyHotelApi.class);
	}

	public Subscription hotelSearch(NearbyHotelParams params, rx.Observer<List<NearbyHotelOffer>> observer) {
		return mHotelApi.nearbyHotelSearch(params.latitude, params.longitude, params.guestCount, params.checkInDate,
			params.checkOutDate)
			.observeOn(mObserveOn)
			.subscribeOn(mSubscribeOn)
			.flatMap(NEARBY_RESPONSE_TO_OFFERS)
			.take(10)
			.toList()
			.subscribe(observer);
	}

	private static final Func1<NearbyHotelResponse, Observable<NearbyHotelOffer>> NEARBY_RESPONSE_TO_OFFERS = new Func1<NearbyHotelResponse, Observable<NearbyHotelOffer>>() {
		@Override
		public Observable<NearbyHotelOffer> call(NearbyHotelResponse nearbyHotelResponse) {
			return Observable.from(nearbyHotelResponse.hotelList);
		}
	};
}
