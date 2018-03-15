package com.expedia.bookings.services

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

open class SatelliteServices(
    endpoint: String,
    okHttpClient: OkHttpClient,
    interceptor: Interceptor,
    satelliteInterceptor: Interceptor,
    hmacInterceptor: Interceptor,
    val observeOn: Scheduler,
    val subscribeOn: Scheduler
) {

    private val satelliteApi by lazy {

        val gson = GsonBuilder()
                .create()

        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient.newBuilder()
                        .addInterceptor(interceptor).addInterceptor(satelliteInterceptor).addInterceptor(hmacInterceptor)
                        .build())
                .build()
        adapter.create(SatelliteApi::class.java)
    }

    fun fetchFeatureConfig(observer: Observer<List<String>>): Disposable {

        val satelliteSubscription = satelliteApi.getFeatureConfigs()
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribeObserver(observer)
        return satelliteSubscription
    }
}
