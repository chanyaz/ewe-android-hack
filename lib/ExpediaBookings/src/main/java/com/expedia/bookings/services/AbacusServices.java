package com.expedia.bookings.services;

import java.util.concurrent.TimeUnit;

import com.expedia.bookings.data.abacus.AbacusEvaluateQuery;
import com.expedia.bookings.data.abacus.AbacusLogQuery;
import com.expedia.bookings.data.abacus.AbacusLogResponse;
import com.expedia.bookings.data.abacus.AbacusResponse;
import com.expedia.bookings.data.abacus.PayloadDeserializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;
import rx.Observer;
import rx.Scheduler;
import rx.Subscription;

public class AbacusServices {
	private AbacusApi api;
	private Gson gson;

	private Scheduler observeOn;
	private Scheduler subscribeOn;

	public AbacusServices(String endpoint, OkHttpClient client, RequestInterceptor interceptor, Scheduler observeOn,
		Scheduler subscribeOn, RestAdapter.LogLevel logLevel) {
		this.observeOn = observeOn;
		this.subscribeOn = subscribeOn;

		gson = new GsonBuilder().registerTypeAdapter(AbacusResponse.class, new PayloadDeserializer()).create();

		RestAdapter adapter = new RestAdapter.Builder()
			.setEndpoint(endpoint)
			.setLogLevel(logLevel)
			.setConverter(new GsonConverter(gson))
			.setClient(new OkClient(client))
			.setRequestInterceptor(interceptor)
			.build();

		api = adapter.create(AbacusApi.class);
	}

	public Subscription downloadBucket(AbacusEvaluateQuery query, Observer<AbacusResponse> observer) {
		return downloadBucket(query, observer, 15, TimeUnit.SECONDS);
	}

	public Subscription downloadBucket(AbacusEvaluateQuery query, Observer<AbacusResponse> observer, long timeout,
		TimeUnit timeUnit) {
		return api.evaluateExperiments(query.guid, query.eapid, query.tpid, query.evaluatedExperiments)
			.observeOn(observeOn)
			.subscribeOn(subscribeOn)
			.timeout(timeout, timeUnit)
			.subscribe(observer);
	}

	public Subscription logExperiment(AbacusLogQuery query) {
		return api.logExperiment(query)
			.observeOn(observeOn)
			.subscribeOn(subscribeOn)
			.subscribe(emptyObserver);
	}

	private final static Observer<AbacusLogResponse> emptyObserver = new Observer<AbacusLogResponse>() {
		@Override
		public void onCompleted() {
			//Ignore
		}

		@Override
		public void onError(Throwable e) {
			//Ignore
		}

		@Override
		public void onNext(AbacusLogResponse abacusLogResponse) {
			//Ignore
		}
	};
}
