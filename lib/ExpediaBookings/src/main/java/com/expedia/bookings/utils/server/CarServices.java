package com.expedia.bookings.utils.server;

import com.expedia.bookings.utils.data.cars.CarSearchResponse;

import retrofit.Callback;
import retrofit.RestAdapter;

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
		RestAdapter adapter = new RestAdapter.Builder()
			.setEndpoint(ENDPOINT)
			.setLogLevel(RestAdapter.LogLevel.FULL)
			.build();

		mApi = adapter.create(CarApi.class);
	}

	public void doBoringCarSearch(Callback<CarSearchResponse> callback) {
		sCarServices.mApi.roundtripCarSearch("SFO", "2015-02-20T12:30:00", "2015-02-21T12:30:00", callback);
	}

}
