package com.expedia.bookings.services;

import org.joda.time.DateTime;

import com.expedia.bookings.data.collections.Collection;
import com.expedia.bookings.data.collections.CollectionResponse;
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
import rx.functions.Func1;

public class CollectionServices {

	Scheduler mObserveOn;
	Scheduler mSubscribeOn;
	CollectionApi mCollectionApi;

	public CollectionServices(String endpoint, OkHttpClient okHttpClient, RequestInterceptor requestInterceptor,
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

		mCollectionApi = adapter.create(CollectionApi.class);
	}

	// Keeping for future use with launch screen collections
	public Subscription getCollection(final String collectionId, String twoLetterCountryCode, String localeCode, Observer<Collection> observer) {
		return mCollectionApi.collections(twoLetterCountryCode, localeCode)
			.observeOn(mObserveOn)
			.subscribeOn(mSubscribeOn)
			.flatMap(COLLECTION_RESPONSE_TO_COLLECTIONS)
			.takeFirst(new Func1<Collection, Boolean>() {
				@Override
				public Boolean call(Collection collection) {
					return Strings.equals(collection.id, collectionId);
				}
			})
			.subscribe(observer);
	}

	public Subscription getPhoneCollection(String twoLetterCountryCode, String localeCode, Observer<Collection> observer) {
		return mCollectionApi.phoneCollection(twoLetterCountryCode, localeCode)
			.observeOn(mObserveOn)
			.subscribeOn(mSubscribeOn)
			.subscribe(observer);
	}

	private static final Func1<CollectionResponse, Observable<Collection>> COLLECTION_RESPONSE_TO_COLLECTIONS = new Func1<CollectionResponse, Observable<Collection>>() {
		@Override
		public Observable<Collection> call(CollectionResponse collectionResponse) {
			return Observable.from(collectionResponse.collections);
		}
	};
}
