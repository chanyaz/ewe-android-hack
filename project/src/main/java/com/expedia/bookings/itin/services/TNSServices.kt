package com.expedia.bookings.itin.services

import com.expedia.bookings.itin.data.*
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.Observer
import rx.Scheduler
import rx.Subscription


open class TNSServices(endpoint: String, okHttpClient: OkHttpClient, interceptor: Interceptor, val observeOn: Scheduler, val subscribeOn: Scheduler) {

    private val tnsAPI: TNSApi by lazy {
        val gson = GsonBuilder().create()

        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient.newBuilder().addInterceptor(interceptor).build())
                .build()

        adapter.create(TNSApi::class.java)
    }

    var tnsRegisterUserDeviceSubscription: Subscription? = null

    fun registerForFlights(applicationName: String, user: TNSUser, flights: List<TNSFlight>, token: String, observer: Observer<TNSRegisterDeviceResponse>): Subscription {
        tnsRegisterUserDeviceSubscription?.unsubscribe()

        val courier = Courier("gcm", applicationName, token);

        val requestBody = TNSRegisterUserDeviceRequestBody(courier, flights, user);

        tnsRegisterUserDeviceSubscription = tnsAPI.registerUserDevice(requestBody)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribe(observer)
        return tnsRegisterUserDeviceSubscription as Subscription
    }
}