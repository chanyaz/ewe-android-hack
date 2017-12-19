package com.expedia.bookings.services

import com.expedia.bookings.data.AbstractItinDetailsResponse
import com.expedia.bookings.data.abacus.ItinTripDeserializer
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.Observer
import rx.Scheduler
import rx.Subscription

// "open" so we can mock for unit tests
open class ItinTripServices(endpoint: String, okHttpClient: OkHttpClient, interceptor: Interceptor, val observeOn: Scheduler, val subscribeOn: Scheduler) {

    val tripsApi: ItinTripApi by lazy {
        val gson = GsonBuilder()
                .registerTypeAdapter(AbstractItinDetailsResponse::class.java, ItinTripDeserializer())
                .create()

        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient.newBuilder().addInterceptor(interceptor).build())
                .build()

        adapter.create(ItinTripApi::class.java)
    }

    open fun getTripDetails(tripId: String, observer: Observer<AbstractItinDetailsResponse>): Subscription {
        val subscription = tripsApi.tripDetails(tripId)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribe(observer)

        return subscription
    }
}