package com.expedia.bookings.services;

import java.util.List;

import com.expedia.bookings.data.cars.Suggestion;
import com.expedia.bookings.data.cars.SuggestionResponse;
import com.squareup.okhttp.OkHttpClient;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import rx.Observer;
import rx.Scheduler;
import rx.Subscription;
import rx.functions.Func1;

public class SuggestionServices {

	private SuggestApi mSuggestApi;
	private OkHttpClient mClient;

	private Scheduler mObserveOn;
	private Scheduler mSubscribeOn;

	private static final RequestInterceptor REQUEST_INTERCEPTOR = new RequestInterceptor() {
		@Override
		public void intercept(RequestFacade request) {
			request.addHeader("Accept", "application/json");
		}
	};

	public SuggestionServices(String endpoint, OkHttpClient okHttpClient, Scheduler observeOn, Scheduler subscribeOn) {
		mObserveOn = observeOn;
		mSubscribeOn = subscribeOn;
		mClient = okHttpClient;

		RestAdapter adapter = new RestAdapter.Builder()
			.setEndpoint(endpoint)
			.setLogLevel(RestAdapter.LogLevel.FULL)
			.setClient(new OkClient(mClient))
			.setRequestInterceptor(REQUEST_INTERCEPTOR)
			.build();

		mSuggestApi = adapter.create(SuggestApi.class);
	}

	private static final int MAX_AIRPORTS_RETURNED = 3;

	public Subscription getAirportSuggestions(String query, Observer<List<Suggestion>> observer) {
		return mSuggestApi.suggestAirport(query)
			.observeOn(mObserveOn)
			.subscribeOn(mSubscribeOn)
			.map(sToList)
			.subscribe(observer);
	}

	private static Func1<SuggestionResponse, List<Suggestion>> sToList = new Func1<SuggestionResponse, List<Suggestion>>() {
		@Override
		public List<Suggestion> call(SuggestionResponse suggestionResponse) {
			int maxSuggestions = suggestionResponse.suggestions.size() > MAX_AIRPORTS_RETURNED ? MAX_AIRPORTS_RETURNED : suggestionResponse.suggestions.size();
			return suggestionResponse.suggestions.subList(0, maxSuggestions);
		}
	};
}
