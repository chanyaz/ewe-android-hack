package com.expedia.bookings.utils.server;

import org.joda.time.DateTime;

import com.expedia.bookings.utils.data.cars.CarSearchParams;
import com.expedia.bookings.utils.data.cars.CarSearchResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

public class CarServices {

	private static final String ENDPOINT = "http://wwwexpediacom.trunk.sb.karmalab.net/m/api/cars/search";

	private CarApi mApi;
	private static CarServices sCarServices;

	public static CarServices getInstance() {
		if (sCarServices == null) {
			sCarServices = new CarServices();
		}
		return sCarServices;
	}

	public CarServices() {
		Gson gson = new GsonBuilder()
			.registerTypeAdapter(DateTime.class, new DateTimeTypeAdapter())
			.create();

		RestAdapter adapter = new RestAdapter.Builder()
			.setEndpoint(ENDPOINT)
			.setLogLevel(RestAdapter.LogLevel.FULL)
			.setConverter(new GsonConverter(gson))
			.build();

		mApi = adapter.create(CarApi.class);
	}

	public void doBoringCarSearch(Callback<CarSearchResponse> callback) {
		sCarServices.mApi.roundtripCarSearch("SFO", "2015-02-20T12:30:00", "2015-02-21T12:30:00", callback);
	}

	public void carSearch(CarSearchParams params, Callback<CarSearchResponse> callback) {
		sCarServices.mApi.roundtripCarSearch(params.origin, params.toServerPickupDate(), params.toServerDropOffDate(), callback);
	}
}
