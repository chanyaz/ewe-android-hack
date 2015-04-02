package com.expedia.bookings.services;

import java.io.File;

import com.expedia.bookings.data.abacus.AbacusResponse;
import com.expedia.bookings.data.abacus.PayloadDeserializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;

import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;
import rx.Observer;
import rx.Scheduler;
import rx.Subscription;

public class AbacusServices {
	public static final String PRODUCTION = "http://services.mobiata.com";
	public static final String DEV = "http://test.services.mobiata.com";

	private AbacusApi mApi;
	private Gson mGson;
	private OkHttpClient mClient;

	private Scheduler mObserveOn;
	private Scheduler mSubscribeOn;

	public AbacusServices(String endpoint, File directory, Scheduler observeOn, Scheduler subscribeOn) {
		mObserveOn = observeOn;
		mSubscribeOn = subscribeOn;

		mGson = new GsonBuilder().registerTypeAdapter(AbacusResponse.class, new PayloadDeserializer()).create();

		mClient = new OkHttpClient();
		if (directory != null) {
			Cache cache = new Cache(directory, 10 * 1024 * 1024);
			mClient.setCache(cache);
		}

		RestAdapter adapter = new RestAdapter.Builder()
			.setEndpoint(endpoint)
			.setLogLevel(RestAdapter.LogLevel.FULL)
			.setConverter(new GsonConverter(mGson))
			.setClient(new OkClient(mClient))
			.build();

		mApi = adapter.create(AbacusApi.class);
	}

	public Subscription downloadBucket(String guid, String id,  Observer<AbacusResponse> observer) {
		return mApi.downloadBucket(guid, id)
			.observeOn(mObserveOn)
			.subscribeOn(mSubscribeOn)
			.subscribe(observer);
	}

}
