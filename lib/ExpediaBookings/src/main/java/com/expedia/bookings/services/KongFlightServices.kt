package com.expedia.bookings.services

import com.expedia.bookings.data.flights.RouteHappyRequest
import com.expedia.bookings.data.flights.RouteHappyResponse
import com.expedia.bookings.extensions.subscribeObserver
import com.google.gson.GsonBuilder
import io.reactivex.Observer
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.joda.time.DateTime
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class KongFlightServices(val endpoint: String, okHttpClient: OkHttpClient, interceptors: List<Interceptor>, val observeOn: Scheduler, val subscribeOn: Scheduler) {
    val flightKongApi: KongFlightApi by lazy {
        val gson = GsonBuilder()
                .registerTypeAdapter(DateTime::class.java, DateTimeTypeAdapter())
                .create()

        val okHttpClientBuilder = okHttpClient.newBuilder()

        for (interceptor in interceptors) {
            okHttpClientBuilder.addInterceptor(interceptor)
        }

        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClientBuilder.build())
                .build()

        adapter.create(KongFlightApi::class.java)
    }

    var routeHappySubscription: Disposable? = null

    fun getFlightRouteHappy(requestPayload: RouteHappyRequest, observer: Observer<RouteHappyResponse>): Disposable {
        routeHappySubscription?.dispose()
        routeHappySubscription = flightKongApi.routeHappy(requestPayload)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribeObserver(observer)
        return routeHappySubscription as Disposable
    }
}
