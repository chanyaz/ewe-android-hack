package com.expedia.bookings.services;

import com.expedia.bookings.data.abacus.AbacusEvaluateQuery;
import com.expedia.bookings.data.abacus.AbacusLogQuery;
import com.expedia.bookings.data.abacus.AbacusLogResponse;
import com.expedia.bookings.data.abacus.AbacusResponse;
import com.expedia.bookings.data.abacus.PayloadDeserializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;

import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;
import rx.Observer;
import rx.Scheduler;
import rx.Subscription;

public class AbacusServices {
	private AbacusApi mApi;
	private Gson mGson;

	private Scheduler mObserveOn;
	private Scheduler mSubscribeOn;

	public AbacusServices(OkHttpClient client, String endpoint, Scheduler observeOn, Scheduler subscribeOn,
		RestAdapter.LogLevel logLevel) {
		mObserveOn = observeOn;
		mSubscribeOn = subscribeOn;

		mGson = new GsonBuilder().registerTypeAdapter(AbacusResponse.class, new PayloadDeserializer()).create();

		RestAdapter adapter = new RestAdapter.Builder()
			.setEndpoint(endpoint)
			.setLogLevel(logLevel)
			.setConverter(new GsonConverter(mGson))
			.setClient(new OkClient(client))
			.build();

		mApi = adapter.create(AbacusApi.class);
	}

	public Subscription downloadBucket(AbacusEvaluateQuery query, Observer<AbacusResponse> observer) {
		return mApi.evaluateExperiments(query.guid, query.eapid, query.tpid, query.evaluatedExperiments)
			.observeOn(mObserveOn)
			.subscribeOn(mSubscribeOn)
			.subscribe(observer);
	}

	public Subscription logExperiment(AbacusLogQuery query) {
		return mApi.logExperiment(query)
			.observeOn(mObserveOn)
			.subscribeOn(mSubscribeOn)
			.subscribe(mAbacusLogObserver);
	}

	private Observer<AbacusLogResponse> mAbacusLogObserver = new Observer<AbacusLogResponse>() {
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
