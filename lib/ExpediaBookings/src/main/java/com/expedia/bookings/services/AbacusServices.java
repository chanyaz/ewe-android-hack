package com.expedia.bookings.services;

import java.util.concurrent.TimeUnit;

import com.expedia.bookings.data.abacus.AbacusEvaluateQuery;
import com.expedia.bookings.data.abacus.AbacusLogQuery;
import com.expedia.bookings.data.abacus.AbacusLogResponse;
import com.expedia.bookings.data.abacus.AbacusResponse;
import com.expedia.bookings.data.abacus.PayloadDeserializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class AbacusServices {
	private AbacusApi api;

	private Scheduler observeOn;
	private Scheduler subscribeOn;

	public AbacusServices(String endpoint, OkHttpClient client, Interceptor interceptor, Scheduler observeOn,
		Scheduler subscribeOn) {
		this.observeOn = observeOn;
		this.subscribeOn = subscribeOn;

		Gson gson = new GsonBuilder().registerTypeAdapter(AbacusResponse.class, new PayloadDeserializer()).create();

		Retrofit adapter = new Retrofit.Builder()
			.baseUrl(endpoint)
			.addConverterFactory(GsonConverterFactory.create(gson))
			.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
			.client(client.newBuilder().addInterceptor(interceptor).build())
			.build();

		api = adapter.create(AbacusApi.class);
	}

	public void downloadBucket(AbacusEvaluateQuery query, Observer<AbacusResponse> observer) {
		downloadBucket(query, observer, 15, TimeUnit.SECONDS);
	}

	public void downloadBucket(AbacusEvaluateQuery query, Observer<AbacusResponse> observer, long timeout,
		TimeUnit timeUnit) {
		api.evaluateExperiments(query.guid, query.eapid, query.tpid, query.getEvaluatedExperiments())
			.observeOn(observeOn)
			.subscribeOn(subscribeOn)
			.timeout(timeout, timeUnit)
			.subscribe(observer);
	}

	public void logExperiment(AbacusLogQuery query) {
		api.logExperiment(query)
			.observeOn(observeOn)
			.subscribeOn(subscribeOn)
			.subscribe(emptyObserver);
	}

	private final static Observer<AbacusLogResponse> emptyObserver = new Observer<AbacusLogResponse>() {
		@Override
		public void onComplete() {
			//Ignore
		}

		@Override
		public void onError(Throwable e) {
			//Ignore
		}

		@Override
		public void onSubscribe(Disposable d) {
			//Ignore
		}

		@Override
		public void onNext(AbacusLogResponse abacusLogResponse) {
			//Ignore
		}
	};
}
