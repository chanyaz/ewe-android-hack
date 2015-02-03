package com.expedia.bookings.services;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import org.joda.time.DateTime;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.cars.CarCategory;
import com.expedia.bookings.data.cars.CarCheckoutParams;
import com.expedia.bookings.data.cars.CarCheckoutResponse;
import com.expedia.bookings.data.cars.CarCreateTripResponse;
import com.expedia.bookings.data.cars.CarSearch;
import com.expedia.bookings.data.cars.CarSearchParams;
import com.expedia.bookings.data.cars.CarSearchResponse;
import com.expedia.bookings.data.cars.CategorizedCarOffers;
import com.expedia.bookings.data.cars.SearchCarOffer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;

import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Subscription;
import rx.functions.Func1;
import rx.functions.Func2;

public class CarServices {

	private CarApi mApi;
	private Gson mGson;
	private OkHttpClient mClient;

	private Scheduler mObserveOn;
	private Scheduler mSubscribeOn;

	public CarServices(String endpoint, OkHttpClient okHttpClient, Scheduler observeOn, Scheduler subscribeOn) {
		mObserveOn = observeOn;
		mSubscribeOn = subscribeOn;
		CookieManager cookieManager = new CookieManager();
		cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
		mClient = okHttpClient;
		mClient.setCookieHandler(cookieManager);

		mGson = new GsonBuilder()
			.registerTypeAdapter(DateTime.class, new DateTimeTypeAdapter())
			.create();

		RestAdapter adapter = new RestAdapter.Builder()
			.setEndpoint(endpoint)
			.setLogLevel(RestAdapter.LogLevel.FULL)
			.setConverter(new GsonConverter(mGson))
			.setClient(new OkClient(mClient))
			.build();

		mApi = adapter.create(CarApi.class);
	}

	public Subscription carSearch(CarSearchParams params, Observer<CarSearch> observer) {
		return mApi.roundtripCarSearch(params.origin, params.toServerPickupDate(), params.toServerDropOffDate())
			.observeOn(mObserveOn)
			.subscribeOn(mSubscribeOn)
			.flatMap(BUCKET_OFFERS)
			.toSortedList(SORT_BY_LOWEST_TOTAL)
			.map(PUT_IN_CARSEARCH)
			.subscribe(observer);
	}

	private static final Func1<CarSearchResponse, Observable<CategorizedCarOffers>> BUCKET_OFFERS = new Func1<CarSearchResponse, Observable<CategorizedCarOffers>>() {
		@Override
		public Observable<CategorizedCarOffers> call(CarSearchResponse carSearchResponse) {
			EnumMap<CarCategory, CategorizedCarOffers> buckets = new EnumMap<>(CarCategory.class);
			List<SearchCarOffer> offers = carSearchResponse.offers;

			for (SearchCarOffer offer : offers) {
				CarCategory category = offer.vehicleInfo.category;
				CategorizedCarOffers bucket = buckets.get(category);
				if (bucket == null) {
					bucket = new CategorizedCarOffers(category);
					buckets.put(category, bucket);
				}
				bucket.add(offer);
			}

			return Observable.from(new ArrayList<>(buckets.values()));
		}
	};

	private static final Func1<List<CategorizedCarOffers>, CarSearch> PUT_IN_CARSEARCH = new Func1<List<CategorizedCarOffers>, CarSearch>() {
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

	public Subscription createTrip(String productKey, String expectedTotalFare, Observer<CarCreateTripResponse> observer) {
		return mApi.createTrip(productKey, expectedTotalFare)
			.observeOn(mObserveOn)
			.subscribeOn(mSubscribeOn)
			.subscribe(observer);
	}

	public Subscription checkout(CarCheckoutParams params, Observer<CarCheckoutResponse> observer) {
		return mApi.checkoutWithoutCreditCard(true, params.tripId, params.grandTotal.amount.toString(),
			params.grandTotal.currencyCode, params.phoneCountryCode, params.phoneNumber, params.emailAddress,
			params.firstName, params.lastName)
			.observeOn(mObserveOn)
			.subscribeOn(mSubscribeOn)
			.subscribe(observer);
	}

}
