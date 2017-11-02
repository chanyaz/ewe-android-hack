package com.expedia.bookings.services

import com.expedia.bookings.data.flights.BaggageInfoResponse
import com.expedia.bookings.subscribeObserver
import com.google.gson.GsonBuilder
import io.reactivex.Observer
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class BaggageInfoService(endpoint: String, okHttpClient: OkHttpClient, interceptor: Interceptor, val observeOn: Scheduler, val subscribeOn: Scheduler) {

    val baggageApi: BaggageApi by lazy {
        val gson = GsonBuilder().create()

        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient.newBuilder().addInterceptor(interceptor).build())
                .build()

        adapter.create(BaggageApi::class.java)
    }

    var baggageInfoSubscription: Disposable? = null

    fun getBaggageInfo(params: ArrayList<HashMap<String, String>>, observer: Observer<BaggageInfoResponse>): Disposable {
        baggageInfoSubscription?.dispose()

        val baggageInfoSubscription = baggageApi.baggageInfo(params)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribeObserver(observer)
        this.baggageInfoSubscription = baggageInfoSubscription
        return baggageInfoSubscription
    }
}