package com.expedia.bookings.services;

import org.joda.time.DateTime;

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
import rx.Scheduler;
import rx.functions.Action2;
import rx.functions.Func0;
import rx.functions.Func1;

public class CarServices {
	public static final String TRUNK = "http://wwwexpediacom.trunk.sb.karmalab.net";

	private CarApi mApi;
	private Gson mGson;
	private OkHttpClient mClient;

	private Scheduler mObserveOn;
	private Scheduler mSubscribeOn;

	public CarServices(String endpoint, Scheduler observeOn, Scheduler subscribeOn) {
		mObserveOn = observeOn;
		mSubscribeOn = subscribeOn;

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

	public Subscription doBoringCarSearch(Observer<CarSearch> observer) {
		CarSearchParams params = new CarSearchParams();
		params.origin = "SFO";
		params.startDateTime = DateTime.now().plusDays(10).withHourOfDay(12).withMinuteOfHour(30);
		params.endDateTime = DateTime.now().plusDays(12).withHourOfDay(14).withMinuteOfHour(30);
		return carSearch(params, observer);
	}

	public Subscription carSearch(CarSearchParams params, Observer<CarSearch> observer) {
		return mApi.roundtripCarSearch(params.origin, params.toServerPickupDate(), params.toServerDropOffDate())
			.observeOn(mObserveOn)
			.subscribeOn(mSubscribeOn)
			.flatMap(offerEmitter)
			.collect(new CarSearchHolder(), offerProcessor)
			.subscribe(observer);
	}

	private static Func1<CarSearchResponse, Observable<CarOffer>> offerEmitter = new Func1<CarSearchResponse, Observable<CarOffer>>() {
		@Override
		public Observable<CarOffer> call(CarSearchResponse carSearchResponse) {
			return Observable.from(carSearchResponse.offers);
		}
	};

	public class CarSearchHolder implements Func0<CarSearch> {
		private CarSearch carSearch = new CarSearch();

		@Override
		public CarSearch call() {
			return carSearch;
		}
	}

	private static Action2<CarSearch, CarOffer> offerProcessor = new Action2<CarSearch, CarOffer>() {
		@Override
		public void call(CarSearch carSearch, CarOffer carOffer) {
			carSearch.updateOfferMap(carOffer);
		}
	};

}
