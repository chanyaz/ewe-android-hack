package com.expedia.bookings.services

import com.expedia.bookings.data.SatelliteSearchResponse
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

import rx.Observer
import rx.Scheduler
import rx.Subscription

open class SatelliteServices(endpoint: String, okHttpClient: OkHttpClient, hmacInterceptor: Interceptor, val observeOn: Scheduler, val subscribeOn: Scheduler) {

    var subscription: Subscription? = null

    val satelliteApi by lazy {

        val gson = GsonBuilder()
                .registerTypeAdapter(SatelliteSearchResponse::class.java, SatelliteDeserializer())
                .create()

        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient.newBuilder().addInterceptor(hmacInterceptor).build())
                .build()
        adapter.create(SatelliteApi::class.java)
    }

    fun satelliteSearch(observer: Observer<SatelliteSearchResponse>, clientId: String): Subscription {

        val search= satelliteApi.satelliteSearch(clientId,1,1)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribe(observer)
        return  search
    }
}

