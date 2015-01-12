package com.expedia.bookings.services;

import org.joda.time.DateTime;

import com.expedia.bookings.data.cars.CarSearchParams;
import com.expedia.bookings.data.cars.CarSearchResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

public class CarServices {
	private static final String TRUNK = "http://wwwexpediacom.trunk.sb.karmalab.net";

	private static CarServices sCarServices = new CarServices(TRUNK);

	public static CarServices getInstance() {
		return sCarServices;
	}

	private CarApi mApi;
	private Gson mGson;
	private OkHttpClient mClient;

	public CarServices(String endpoint) {
		mGson = new GsonBuilder()
			.registerTypeAdapter(DateTime.class, new DateTimeTypeAdapter())
			.create();

		mClient = new OkHttpClient();

		RestAdapter adapter = new RestAdapter.Builder()
			.setEndpoint(endpoint)
			.setLogLevel(RestAdapter.LogLevel.FULL)
			.setConverter(new GsonConverter(mGson))
			.setClient(new OkClient(mClient))
			.build();

		mApi = adapter.create(CarApi.class);
	}

	public CarServices() {
		this(TRUNK);
	}

	public void doBoringCarSearch(Callback<CarSearchResponse> callback) {
		mApi.roundtripCarSearch("SFO", "2015-02-20T12:30:00", "2015-02-21T12:30:00", callback);
	}

	public void carSearch(CarSearchParams params, Callback<CarSearchResponse> callback) {
		mApi.roundtripCarSearch(params.origin, params.toServerPickupDate(), params.toServerDropOffDate(), callback);
	}
}
