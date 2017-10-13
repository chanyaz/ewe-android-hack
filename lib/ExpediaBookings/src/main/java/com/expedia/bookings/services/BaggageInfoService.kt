package com.expedia.bookings.services

import com.expedia.bookings.data.flights.BaggageInfoResponse
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.Observer
import rx.Scheduler
import rx.Subscription

class BaggageInfoService(endpoint: String, okHttpClient: OkHttpClient, interceptor: Interceptor, val observeOn: Scheduler, val subscribeOn: Scheduler) {

    val baggageApi: BaggageApi by lazy {
        val gson = GsonBuilder().create()

        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient.newBuilder().addInterceptor(interceptor).build())
                .build()

        adapter.create(BaggageApi::class.java)
    }

    var baggageInfoSubscription: Subscription? = null

    fun getBaggageInfo(params: ArrayList<HashMap<String, String>>, observer: Observer<BaggageInfoResponse>): Subscription {
        baggageInfoSubscription?.unsubscribe()

        baggageInfoSubscription = baggageApi.baggageInfo(params)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribe(observer)
        return baggageInfoSubscription as Subscription
    }
}