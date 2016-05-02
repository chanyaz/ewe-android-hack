package com.expedia.bookings.services

import com.expedia.bookings.data.payment.CalculatePointsParams
import com.expedia.bookings.data.payment.CalculatePointsResponse
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.Observer
import rx.Scheduler
import rx.Subscription

class LoyaltyServices(endpoint: String, okHttpClient: OkHttpClient, val observeOn: Scheduler, val subscribeOn: Scheduler) {

    val loyaltyApi: LoyaltyApi by lazy {
        val gson = GsonBuilder().create()

        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient)
                .build()

        adapter.create(LoyaltyApi::class.java)
    }

    fun currencyToPoints(calculatePointsParams: CalculatePointsParams, observer: Observer<CalculatePointsResponse>): Subscription {
        return loyaltyApi.currencyToPoints(calculatePointsParams.tripId, calculatePointsParams.programName, calculatePointsParams.amount, calculatePointsParams.rateId)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribe(observer)
    }
}
