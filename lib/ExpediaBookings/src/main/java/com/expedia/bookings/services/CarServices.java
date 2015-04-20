package com.expedia.bookings.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.cars.BaseApiResponse;
import com.expedia.bookings.data.cars.CarCategory;
import com.expedia.bookings.data.cars.CarCheckoutParams;
import com.expedia.bookings.data.cars.CarCheckoutResponse;
import com.expedia.bookings.data.cars.CarCreateTripResponse;
import com.expedia.bookings.data.cars.CarSearch;
import com.expedia.bookings.data.cars.CarSearchParams;
import com.expedia.bookings.data.cars.CarSearchResponse;
import com.expedia.bookings.data.cars.CategorizedCarOffers;
import com.expedia.bookings.data.cars.CreateTripCarOffer;
import com.expedia.bookings.data.cars.SearchCarOffer;
import com.expedia.bookings.utils.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Subscription;
import rx.exceptions.OnErrorNotImplementedException;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;

public class CarServices {

	private CarApi mApi;

	private Scheduler mObserveOn;
	private Scheduler mSubscribeOn;

	public CarServices(String endpoint, OkHttpClient okHttpClient, RequestInterceptor requestInterceptor,
		Scheduler observeOn, Scheduler subscribeOn, RestAdapter.LogLevel logLevel) {
		mObserveOn = observeOn;
		mSubscribeOn = subscribeOn;

		Gson gson = generateGson();

		RestAdapter adapter = new RestAdapter.Builder()
			.setEndpoint(endpoint)
			.setRequestInterceptor(requestInterceptor)
			.setLogLevel(logLevel)
			.setConverter(new GsonConverter(gson))
			.setClient(new OkClient(okHttpClient))
			.build();

		mApi = adapter.create(CarApi.class);
	}

	public Subscription carSearch(CarSearchParams params, Observer<CarSearch> observer) {
		return mApi.roundtripCarSearch(params.origin, params.toServerPickupDate(), params.toServerDropOffDate())
			.doOnNext(HANDLE_ERRORS)
			.flatMap(BUCKET_OFFERS)
			.toSortedList(SORT_BY_LOWEST_TOTAL)
			.map(PUT_IN_CAR_SEARCH)
			.subscribeOn(mSubscribeOn)
			.observeOn(mObserveOn)
			.subscribe(observer);
	}

	public Subscription createTrip(SearchCarOffer offer, Observer<CarCreateTripResponse> observer) {
		return mApi.createTrip(offer.productKey, offer.fare.total.amount.toString())
			.doOnNext(HANDLE_ERRORS)
			.map(new SearchOfferInjector(offer))
			.subscribeOn(mSubscribeOn)
			.observeOn(mObserveOn)
			.subscribe(observer);
	}

	public Subscription checkout(CreateTripCarOffer offer, CarCheckoutParams params,
		Observer<CarCheckoutResponse> observer) {
		return mApi.checkout(params.toQueryMap())
			.doOnNext(HANDLE_ERRORS)
			.map(new CreateTripOfferInjector(offer))
			.subscribeOn(mSubscribeOn)
			.observeOn(mObserveOn)
			.subscribe(observer);
	}

	public static Gson generateGson() {
		return new GsonBuilder()
			.registerTypeAdapter(DateTime.class, new DateTimeTypeAdapter())
			.create();
	}

	// Throw an error so the UI can handle it except for price changes.
	// Let the remaining pipline handle those
	private static final Action1<BaseApiResponse> HANDLE_ERRORS = new Action1<BaseApiResponse>() {
		@Override
		public void call(BaseApiResponse response) {
			if (response.hasErrors() && !response.hasPriceChange()) {
				throw response.getFirstError();
			}
		}
	};

	private static final Func1<CarSearchResponse, Observable<CategorizedCarOffers>> BUCKET_OFFERS = new Func1<CarSearchResponse, Observable<CategorizedCarOffers>>() {
		@Override
		public Observable<CategorizedCarOffers> call(CarSearchResponse carSearchResponse) {
			Map<String, CategorizedCarOffers> buckets = new HashMap<>();
			for (SearchCarOffer offer : carSearchResponse.offers) {
				String label = offer.vehicleInfo.carCategoryDisplayLabel;
				CarCategory category = offer.vehicleInfo.category;
				if (Strings.isEmpty(label)) {
					throw new OnErrorNotImplementedException(new RuntimeException("offer.vehicle.carCategoryDisplayLabel is empty for productKey=" + offer.productKey));
				}

				CategorizedCarOffers bucket = buckets.get(label);
				if (bucket == null) {
					bucket = new CategorizedCarOffers(label, category);
					buckets.put(label, bucket);
				}
				bucket.add(offer);
			}

			return Observable.from(new ArrayList<>(buckets.values()));
		}
	};

	private static final Func1<List<CategorizedCarOffers>, CarSearch> PUT_IN_CAR_SEARCH = new Func1<List<CategorizedCarOffers>, CarSearch>() {
		@Override
		public CarSearch call(List<CategorizedCarOffers> categories) {
			CarSearch search = new CarSearch();
			search.categories = categories;
			return search;
		}
	};

	private static final Func2<CategorizedCarOffers, CategorizedCarOffers, Integer> SORT_BY_LOWEST_TOTAL = new Func2<CategorizedCarOffers, CategorizedCarOffers, Integer>() {
		@Override
		public Integer call(CategorizedCarOffers left, CategorizedCarOffers right) {
			Money leftMoney = left.getLowestTotalPriceOffer().fare.total;
			Money rightMoney = right.getLowestTotalPriceOffer().fare.total;
			return leftMoney.compareTo(rightMoney);
		}
	};

	private class SearchOfferInjector implements Func1<CarCreateTripResponse, CarCreateTripResponse> {
		SearchCarOffer searchCarOffer;

		public SearchOfferInjector(SearchCarOffer offer) {
			searchCarOffer = offer;
		}

		@Override
		public CarCreateTripResponse call(CarCreateTripResponse carCreateTripResponse) {
			if (carCreateTripResponse.hasPriceChange()) {
				carCreateTripResponse.searchCarOffer = searchCarOffer;
			}
			return carCreateTripResponse;
		}
	}

	private class CreateTripOfferInjector implements Func1<CarCheckoutResponse, CarCheckoutResponse> {
		CreateTripCarOffer createTripCarOffer;

		public CreateTripOfferInjector(CreateTripCarOffer offer) {
			createTripCarOffer = offer;
		}

		@Override
		public CarCheckoutResponse call(CarCheckoutResponse carCheckoutResponse) {
			if (carCheckoutResponse.hasPriceChange()) {
				carCheckoutResponse.originalCarProduct = createTripCarOffer;
			}
			return carCheckoutResponse;
		}
	}
}
