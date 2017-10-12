package com.expedia.bookings.services

import com.expedia.bookings.data.flights.KrazyGlueResponse
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.joda.time.DateTime
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.Observer
import rx.Scheduler
import rx.Subscription

class KrazyGlueServices(endpoint: String, okHttpClient: OkHttpClient, interceptor: Interceptor, val observeOn: Scheduler, val subscribeOn: Scheduler) {

    val krazyGlueApi: KrazyGlueApi by lazy {
        val gson = GsonBuilder()
                .registerTypeAdapter(DateTime::class.java, DateTimeTypeAdapter())
                .create()

        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient.newBuilder().addInterceptor(interceptor).build())
                .build()

        adapter.create(KrazyGlueApi::class.java)
    }

    var krazyGlueSubscription: Subscription? = null

    open fun getKrazyGlueHotels(signedUrl: String, observer: Observer<KrazyGlueResponse>) : Subscription {
        krazyGlueSubscription?.unsubscribe()

        krazyGlueSubscription = krazyGlueApi.getKrazyGlueHotels(signedUrl)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribe(observer)

        return krazyGlueSubscription as Subscription
    }
}
