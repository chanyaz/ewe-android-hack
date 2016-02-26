package com.expedia.bookings.services

import com.expedia.bookings.data.payment.CalculatePointsParams
import com.expedia.bookings.data.payment.CalculatePointsResponse
import com.google.gson.GsonBuilder
import com.squareup.okhttp.OkHttpClient
import retrofit.RequestInterceptor
import retrofit.RestAdapter
import retrofit.client.OkClient
import retrofit.converter.GsonConverter
import rx.Observer
import rx.Scheduler
import rx.Subscription

class LoyaltyServices(endpoint: String, okHttpClient: OkHttpClient, requestInterceptor: RequestInterceptor, val observeOn: Scheduler, val subscribeOn: Scheduler, logLevel: RestAdapter.LogLevel) {

    val loyaltyApi: LoyaltyApi by lazy {
        val gson = GsonBuilder().create()

        val adapter = RestAdapter.Builder()
                .setEndpoint(endpoint)
                .setRequestInterceptor(requestInterceptor)
                .setLogLevel(logLevel)
                .setConverter(GsonConverter(gson))
                .setClient(OkClient(okHttpClient))
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
