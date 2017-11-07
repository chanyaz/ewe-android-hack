package com.expedia.bookings.services.travelgraph

import com.expedia.bookings.data.travelgraph.TravelGraphUserHistoryResponse
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.Observer
import rx.Scheduler
import rx.Subscription
import java.util.UUID

class TravelGraphServices(endpoint: String, okHttpClient: OkHttpClient, interceptor: Interceptor, tgInterceptor: Interceptor,
                          private val observeOn: Scheduler, private val subscribeOn: Scheduler) {

    private val travelGraphApi: TravelGraphApi by lazy {
        val gson = GsonBuilder().create()

        val retrofit = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient.newBuilder().addInterceptor(interceptor).addInterceptor(tgInterceptor).build())
                .build()

        retrofit.create(TravelGraphApi::class.java)
    }

    //Guest user not supported for now
    fun fetchUserHistory(expUserId: String, siteId: String, locale: String, observer: Observer<TravelGraphUserHistoryResponse>): Subscription {
        val transactionGUID = UUID.randomUUID().toString().replace("-".toRegex(), "")
        val lobs = arrayListOf("HOTEL_SEARCH")      //Hardcoded to get just search info for now.

        return travelGraphApi.fetchUserHistory(siteId, locale, expUserId, transactionGUID, lobs)
                .subscribeOn(subscribeOn)
                .observeOn(observeOn)
                .subscribe(observer)
    }
}