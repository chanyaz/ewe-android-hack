package com.expedia.bookings.services

import com.expedia.bookings.data.AbstractItinDetailsResponse
import com.expedia.bookings.data.abacus.ItinTripDeserializer
import com.expedia.bookings.extensions.subscribeObserver
import com.google.gson.GsonBuilder
import io.reactivex.Observer
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

// "open" so we can mock for unit tests
open class ItinTripServices(endpoint: String, okHttpClient: OkHttpClient, interceptor: Interceptor, val observeOn: Scheduler, val subscribeOn: Scheduler) {

    val tripsApi: ItinTripApi by lazy {
        val gson = GsonBuilder()
                .registerTypeAdapter(AbstractItinDetailsResponse::class.java, ItinTripDeserializer())
                .create()

        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient.newBuilder().addInterceptor(interceptor).build())
                .build()

        adapter.create(ItinTripApi::class.java)
    }

    open fun getTripDetails(tripId: String, observer: Observer<AbstractItinDetailsResponse>): Disposable {
        return tripsApi.tripDetails(tripId)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribeObserver(observer)
    }
}
