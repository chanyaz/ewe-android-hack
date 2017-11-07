package com.expedia.bookings.services

import com.expedia.bookings.data.flights.KrazyglueResponse
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import org.joda.time.DateTime
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.Observer
import rx.Scheduler
import rx.Subscription

class KrazyglueServices(endpoint: String, okHttpClient: OkHttpClient, val observeOn: Scheduler, val subscribeOn: Scheduler) {

    val krazyglueApi: KrazyglueApi by lazy {
        val gson = GsonBuilder()
                .registerTypeAdapter(DateTime::class.java, DateTimeTypeAdapter())
                .create()

        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient.newBuilder().build())
                .build()

        adapter.create(KrazyglueApi::class.java)
    }

    var krazyglueSubscription: Subscription? = null

    open fun getKrazyglueHotels(signedUrl: String, observer: Observer<KrazyglueResponse>) : Subscription {
        krazyglueSubscription?.unsubscribe()

        krazyglueSubscription = krazyglueApi.getKrazyglueHotels(signedUrl)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribe(observer)

        return krazyglueSubscription as Subscription
    }
}
