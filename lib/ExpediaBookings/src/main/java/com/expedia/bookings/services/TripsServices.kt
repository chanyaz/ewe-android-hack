package com.expedia.bookings.services

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import rx.Observer
import rx.Scheduler
import rx.Subscription


// "open" so we can mock for unit tests
open class TripsServices(endpoint: String, okHttpClient: OkHttpClient, interceptor: Interceptor, val observeOn: Scheduler, val subscribeOn: Scheduler) {

    val tripsApi: TripsApi by lazy {
        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(JsonConverterFactory())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient.newBuilder().addInterceptor(interceptor).build())
                .build()

        adapter.create(TripsApi::class.java)
    }

    open fun getTripDetails(tripId: String, useCache: Boolean): JSONObject? {
        val call = tripsApi.tripDetails(tripId, if (useCache) "1" else "0")
        val response = call.execute()
        return response.body()
    }

    open fun getGuestTrip(guestEmail: String, tripId: String, useCache: Boolean, observer: Observer<JSONObject>): Subscription {
        val subscription = tripsApi.guestTrip(tripId, guestEmail, if (useCache) "1" else "0")
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribe(observer)

        return subscription
    }

}
