package com.expedia.bookings.services;

import java.util.List;

import org.joda.time.DateTime;

import com.expedia.bookings.data.hotels.Hotel;
import com.expedia.bookings.data.hotels.HotelSearchResponse;
import com.expedia.bookings.data.hotels.NearbyHotelParams;
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

public class HotelServices {

	Scheduler mObserveOn;
	Scheduler mSubscribeOn;
	HotelApi mHotelApi;

	public HotelServices(String endpoint, OkHttpClient okHttpClient, RequestInterceptor requestInterceptor,
		Scheduler observeOn, Scheduler subscribeOn) {
		mObserveOn = observeOn;
		mSubscribeOn = subscribeOn;

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

		mHotelApi = adapter.create(HotelApi.class);
	}

	public Subscription hotelSearch(NearbyHotelParams params, rx.Observer<List<Hotel>> observer) {
		return mHotelApi.nearbyHotelSearch(params.latitude, params.longitude, params.guestCount, params.checkInDate,
			params.checkOutDate, params.sortOrder)
			.observeOn(mObserveOn)
			.subscribeOn(mSubscribeOn)
			.flatMap(NEARBY_RESPONSE_TO_OFFERS)
			.take(10)
			.toList()
			.subscribe(observer);
	}

	private static final Func1<HotelSearchResponse, Observable<Hotel>> NEARBY_RESPONSE_TO_OFFERS = new Func1<HotelSearchResponse, Observable<Hotel>>() {
		@Override
		public Observable<Hotel> call(HotelSearchResponse hotelSearchResponse) {
			return Observable.from(hotelSearchResponse.hotelList);
		}
	};
}
