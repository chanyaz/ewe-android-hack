package com.expedia.bookings.services

import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

import rx.Observer
import rx.Scheduler
import rx.Subscription

open class SatelliteServices(endpoint: String, okHttpClient: OkHttpClient,interceptor:Interceptor, hmacInterceptor: Interceptor, val observeOn: Scheduler, val subscribeOn: Scheduler) {

    private val satelliteApi by lazy {

        val gson = GsonBuilder()
                .create()

        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient.newBuilder().addInterceptor(interceptor).addInterceptor(hmacInterceptor).build())
                .build()
        adapter.create(SatelliteApi::class.java)
    }

    fun fetchFeatureConfig(observer: Observer<List<String>>, clientId: String): Subscription {

        val satelliteSubscription = satelliteApi.getFeatureConfigs(clientId)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribe(observer)
        return satelliteSubscription
    }
}

