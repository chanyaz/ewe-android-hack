package com.expedia.bookings.services

import com.expedia.bookings.data.clientlog.ClientLog
import com.expedia.bookings.data.clientlog.EmptyResponse
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.Observer
import rx.Scheduler
import java.net.URL

class ClientLogServices(endpoint: String, okHttpClient: OkHttpClient, interceptor: Interceptor, val observeOn: Scheduler, val subscribeOn: Scheduler) : IClientLogServices {
	var domain: String? = null
	val clientLogApi: ClientLogApi by lazy {
		domain = URL(endpoint).host

		val gson = GsonBuilder()
				.create()

		val adapter = Retrofit.Builder()
			.baseUrl(endpoint)
			.addConverterFactory(GsonConverterFactory.create(gson))
			.addCallAdapterFactory(RxJavaCallAdapterFactory.create())
			.client(okHttpClient.newBuilder().addInterceptor(interceptor).build())
			.build()

		adapter.create(ClientLogApi::class.java)
	}

	override fun log(clientLog: ClientLog) {
		clientLogApi.log(clientLog.pageName, clientLog.eventName, domain, clientLog.deviceName, clientLog.requestTime, clientLog.responseTime, clientLog.processingTime, clientLog.requestToUser, clientLog.deviceType)
				.observeOn(observeOn)
				.subscribeOn(subscribeOn)
				.subscribe(makeEmptyObserver())
	}

	override fun deepLinkMarketingIdLog(queryParams: Map<String, String> ) {
		clientLogApi.deepLinkMarketingIdlog(queryParams)
				.observeOn(observeOn)
				.subscribeOn(subscribeOn)
				.subscribe(makeEmptyObserver())
	}

	//This endpoint doesn't return json so it will always onError
	private fun makeEmptyObserver() : Observer<EmptyResponse>  {
		return object : Observer<EmptyResponse> {
			override fun onCompleted() {
				//ignore
			}

			override fun onError(error: Throwable?) {
				//ignore
			}

			override fun onNext(response: EmptyResponse?) {
				//ignore
			}
		}
	}
}
