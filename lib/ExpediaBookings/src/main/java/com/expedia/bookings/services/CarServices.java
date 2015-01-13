package com.expedia.bookings.services;

import org.joda.time.DateTime;

import com.expedia.bookings.data.cars.CarDb;
import com.expedia.bookings.data.cars.CarOffer;
import com.expedia.bookings.data.cars.CarSearch;
import com.expedia.bookings.data.cars.CarSearchParams;
import com.expedia.bookings.data.cars.CarSearchResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;

import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

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

	public Subscription doBoringCarSearch(Observer<CarSearch> observer) {
		CarSearchParams params = new CarSearchParams();
		params.origin = "SFO";
		params.startTime = DateTime.now().plusDays(10).withHourOfDay(12).withMinuteOfHour(30);
		params.endTime = DateTime.now().plusDays(12).withHourOfDay(14).withMinuteOfHour(30);
		return carSearch(params, observer);
	}

	public Subscription carSearch(CarSearchParams params, Observer<CarSearch> observer) {
		CarDb.carSearch.reset();
		return mApi.roundtripCarSearch(params.origin, params.toServerPickupDate(), params.toServerDropOffDate())
			.observeOn(AndroidSchedulers.mainThread())
			.subscribeOn(Schedulers.io())
			.flatMap(emitOffer)
			.flatMap(processOffer)
			.subscribe(observer);
	}

	public static final Func1<CarSearchResponse, Observable<CarOffer>> emitOffer = new Func1<CarSearchResponse, Observable<CarOffer>>() {
		@Override
		public Observable<CarOffer> call(CarSearchResponse carSearchResponse) {
			return Observable.from(carSearchResponse.offers);
		}
	};

	public static final Func1<CarOffer, Observable<CarSearch>> processOffer = new Func1<CarOffer, Observable<CarSearch>>() {
		@Override
		public Observable<CarSearch> call(CarOffer carOffer) {
			CarDb.carSearch.updateOfferMap(carOffer);
			return Observable.just(CarDb.carSearch);
		}
	};
}
