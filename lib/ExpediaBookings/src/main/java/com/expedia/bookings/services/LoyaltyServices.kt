package com.expedia.bookings.services

import com.expedia.bookings.data.payment.CalculatePointsParams
import com.expedia.bookings.data.payment.CalculatePointsResponse
import com.expedia.bookings.extensions.subscribeObserver
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import io.reactivex.Observer
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable

class LoyaltyServices(endpoint: String, okHttpClient: OkHttpClient, interceptor: Interceptor, val observeOn: Scheduler, val subscribeOn: Scheduler) {

    val loyaltyApi: LoyaltyApi by lazy {
        val gson = GsonBuilder().create()

        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient.newBuilder().addInterceptor(interceptor).build())
                .build()

        adapter.create(LoyaltyApi::class.java)
    }

    fun currencyToPoints(calculatePointsParams: CalculatePointsParams, observer: Observer<CalculatePointsResponse>): Disposable {
        return loyaltyApi.currencyToPoints(calculatePointsParams.tripId, calculatePointsParams.programName, calculatePointsParams.amount, calculatePointsParams.rateId)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribeObserver(observer)
    }
}
