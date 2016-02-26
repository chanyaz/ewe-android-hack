package com.expedia.bookings.services

import com.expedia.bookings.data.clientlog.ClientLog
import com.expedia.bookings.data.clientlog.ClientLogApi
import com.expedia.bookings.data.clientlog.EmptyResponse
import com.google.gson.GsonBuilder
import com.squareup.okhttp.OkHttpClient
import org.joda.time.DateTime
import retrofit.RequestInterceptor
import retrofit.RestAdapter
import retrofit.client.OkClient
import retrofit.converter.GsonConverter
import rx.Observer
import rx.Scheduler

class ClientLogServices(endpoint: String, okHttpClient: OkHttpClient, requestInterceptor: RequestInterceptor, val observeOn: Scheduler, val subscribeOn: Scheduler, logLevel: RestAdapter.LogLevel) {

	val clientLogApi: ClientLogApi by lazy {
		val gson = GsonBuilder()
				.registerTypeAdapter(DateTime::class.java, DateTimeTypeAdapter())
				.create()

		val adapter = RestAdapter.Builder()
			.setEndpoint(endpoint)
			.setRequestInterceptor(requestInterceptor)
			.setLogLevel(logLevel)
			.setConverter(GsonConverter(gson))
			.setClient(OkClient(okHttpClient))
			.build()

		adapter.create(ClientLogApi::class.java)
	}

	fun log(clientLog: ClientLog) {
		clientLogApi.log(clientLog.pageName, clientLog.requestTime, clientLog.responseTime, clientLog.processingTime, clientLog.requestToUser)
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
