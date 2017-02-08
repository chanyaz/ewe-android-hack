package com.expedia.bookings.services

import com.expedia.bookings.data.RoomUpgradeOffersResponse
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.Observer
import rx.Scheduler
import rx.Subscription

class RoomUpgradeOffersService(endpoint: String, okHttpClient: OkHttpClient, interceptor: Interceptor, val observeOn: Scheduler, val subscribeOn: Scheduler) {

    var requestSubscription: Subscription? = null

    val roomUpgradeOffersApi: RoomUpgradeOffersApi by lazy {
        val gson = GsonBuilder().create()

        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create(gson)) // may not need this
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient.newBuilder().addInterceptor(interceptor).build())
                .build()

        adapter.create(RoomUpgradeOffersApi::class.java)
    }

    fun fetchOffers(url: String, observer: Observer<RoomUpgradeOffersResponse>): Subscription {
        requestSubscription?.unsubscribe()

        requestSubscription = roomUpgradeOffersApi.fetchOffers(url).observeOn(observeOn)
                .subscribeOn(subscribeOn).subscribe(observer)

        return requestSubscription as Subscription
    }
}
