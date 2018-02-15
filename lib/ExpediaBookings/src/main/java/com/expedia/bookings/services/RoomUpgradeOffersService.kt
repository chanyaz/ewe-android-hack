package com.expedia.bookings.services

import com.expedia.bookings.data.RoomUpgradeOffersResponse
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

class RoomUpgradeOffersService(endpoint: String, okHttpClient: OkHttpClient, interceptor: Interceptor, val observeOn: Scheduler, val subscribeOn: Scheduler) {

    var requestSubscription: Disposable? = null

    val roomUpgradeOffersApi: RoomUpgradeOffersApi by lazy {
        val gson = GsonBuilder().create()

        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create(gson)) // may not need this
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient.newBuilder().addInterceptor(interceptor).build())
                .build()

        adapter.create(RoomUpgradeOffersApi::class.java)
    }

    fun fetchOffers(url: String, observer: Observer<RoomUpgradeOffersResponse>): Disposable {
        requestSubscription?.dispose()

        requestSubscription = roomUpgradeOffersApi.fetchOffers(url).observeOn(observeOn)
                .subscribeOn(subscribeOn).subscribeObserver(observer)

        return requestSubscription as Disposable
    }
}
