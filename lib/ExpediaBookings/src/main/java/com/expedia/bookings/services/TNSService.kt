package com.expedia.bookings.services

import com.expedia.bookings.data.flights.*
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.Observer
import rx.Scheduler
import rx.Subscription

/**
 * Created by napandey on 11/15/17.
 */

class TNSService(endpoint: String, okHttpClient: OkHttpClient, interceptor: Interceptor, val observeOn: Scheduler, val subscribeOn: Scheduler) {

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

    fun registerForFlights(applicationName: String, user: User, flights: List<Flight>, token: String, observer: Observer<TNSRegisterDeviceResponse>): Subscription {
        tnsRegisterUserDeviceSubscription?.unsubscribe()

        var requestBody = TNSRegisterUserDeviceRequestBody();

        var courier: Courier = Courier();
        courier.group = "gcm"
        courier.name = applicationName
        courier.token = token

        requestBody.courier = courier
        requestBody.user = user
        requestBody.flights = flights

        tnsRegisterUserDeviceSubscription = tnsAPI.registerUserDevice(requestBody)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribe(observer)
        return tnsRegisterUserDeviceSubscription as Subscription
    }
}