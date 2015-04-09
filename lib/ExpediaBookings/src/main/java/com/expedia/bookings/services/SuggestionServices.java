package com.expedia.bookings.services;

import java.util.List;

import com.expedia.bookings.data.SuggestionResultType;
import com.expedia.bookings.data.cars.Suggestion;
import com.expedia.bookings.data.cars.SuggestionResponse;
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

public class SuggestionServices {

	private SuggestApi mSuggestApi;
	private OkHttpClient mClient;

	private Scheduler mObserveOn;
	private Scheduler mSubscribeOn;
	private static final int MAX_NEARBY_AIRPORTS = 2;

	private static final RequestInterceptor REQUEST_INTERCEPTOR = new RequestInterceptor() {
		@Override
		public void intercept(RequestFacade request) {
			request.addHeader("Accept", "application/json");
		}
	};

	public SuggestionServices(String endpoint, OkHttpClient okHttpClient, Scheduler observeOn, Scheduler subscribeOn, RestAdapter.LogLevel logLevel) {
		mObserveOn = observeOn;
		mSubscribeOn = subscribeOn;
		mClient = okHttpClient;

		Gson gson = new GsonBuilder()
			.registerTypeAdapter(SuggestionResponse.class, new SuggestionResponse())
			.create();

		RestAdapter adapter = new RestAdapter.Builder()
			.setEndpoint(endpoint)
			.setLogLevel(logLevel)
			.setConverter(new GsonConverter(gson))
			.setClient(new OkClient(mClient))
			.setRequestInterceptor(REQUEST_INTERCEPTOR)
			.build();

		mSuggestApi = adapter.create(SuggestApi.class);
	}

	private static final int MAX_AIRPORTS_RETURNED = 3;

	public Subscription getAirportSuggestions(String query, Observer<List<Suggestion>> observer) {
		return mSuggestApi.suggestV3(query, SuggestionResultType.AIRPORT, null)
			.observeOn(mObserveOn)
			.subscribeOn(mSubscribeOn)
			.concatMap(FLATTEN_SUGGESTIONS)
			.take(MAX_AIRPORTS_RETURNED)
			.toList()
			.subscribe(observer);
	}

	private static final int MAX_LX_SUGGESTIONS_RETURNED = 3;

	public Subscription getLxSuggestions(String query, Observer<List<Suggestion>> observer) {
		return mSuggestApi.suggestV3(query,
			SuggestionResultType.CITY | SuggestionResultType.MULTI_CITY | SuggestionResultType.NEIGHBORHOOD,
			"ACTIVITIES")
			.observeOn(mObserveOn)
			.subscribeOn(mSubscribeOn)
			.flatMap(FLATTEN_SUGGESTIONS)
			.take(MAX_LX_SUGGESTIONS_RETURNED)
			.toList()
			.subscribe(observer);
	}

	private static final Func1<SuggestionResponse, Observable<Suggestion>> FLATTEN_SUGGESTIONS = new Func1<SuggestionResponse, Observable<Suggestion>>() {
		@Override
		public Observable<Suggestion> call(SuggestionResponse suggestionResponse) {
			return Observable.from(suggestionResponse.suggestions);
		}
	};

	private static Func1<SuggestionResponse, List<Suggestion>> sToListNearby = new Func1<SuggestionResponse, List<Suggestion>>() {
		@Override
		public List<Suggestion> call(SuggestionResponse suggestionResponse) {
			if (suggestionResponse != null) {
				List<Suggestion> result = suggestionResponse.suggestions.subList(0, suggestionResponse.suggestions.size() >= 2 ? MAX_NEARBY_AIRPORTS : 1);
				for (Suggestion suggestion : result) {
					suggestion.iconType = Suggestion.IconType.CURRENT_LOCATION_ICON;
				}
				return result;
			}
			return null;
		}
	};

	public Subscription getNearbyAirportSuggestions(String locale, String latlong, int siteId, Observer<List<Suggestion>> observer) {
		return mSuggestApi.suggestNearbyV1(locale, latlong, siteId, SuggestionResultType.AIRPORT, "p")
			.observeOn(mObserveOn)
			.subscribeOn(mSubscribeOn)
			.map(sToListNearby)
			.subscribe(observer);
	}

	public Subscription getNearbyLxSuggestions(String locale, String latlong, int siteId, Observer<List<Suggestion>> observer) {
		return getNearbyLxSuggestions(locale, latlong, siteId)
			.subscribe(observer);
	}

	public Observable<List<Suggestion>> getNearbyLxSuggestions(String locale, String latlong, int siteId) {
		return mSuggestApi.suggestNearbyV1(locale, latlong, siteId,
			SuggestionResultType.CITY | SuggestionResultType.MULTI_CITY | SuggestionResultType.NEIGHBORHOOD, "d")
			.observeOn(mObserveOn)
			.subscribeOn(mSubscribeOn)
			.map(sToListNearby);
	}
}
