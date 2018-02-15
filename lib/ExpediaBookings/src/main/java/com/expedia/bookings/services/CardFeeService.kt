package com.expedia.bookings.services

import com.expedia.bookings.data.CardFeeResponse
import com.expedia.bookings.extensions.subscribeObserver
import com.expedia.bookings.utils.Constants
import com.google.gson.GsonBuilder
import io.reactivex.Observer
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

// open so we can mock for tests
open class CardFeeService(endpoint: String, okHttpClient: OkHttpClient, interceptors: List<Interceptor>, val observeOn: Scheduler, val subscribeOn: Scheduler) {

    var subscription: Disposable? = null

    val cardFeeApi: CardFeeApi by lazy {
        val gson = GsonBuilder().create()

        val okHttpClientBuilder = okHttpClient.newBuilder()

        for (interceptor in interceptors) {
            okHttpClientBuilder.addInterceptor(interceptor)
        }

        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClientBuilder.build())
                .build()

        adapter.create(CardFeeApi::class.java)
    }

    // open so we can mock for tests
    open fun getCardFees(tripId: String, creditCardId: String, isFlexEnabled: Boolean, observer: Observer<CardFeeResponse>): Disposable {
        subscription?.dispose() // cancels any existing calls we're waiting on
        val subscription = cardFeeApi.cardFee(tripId, creditCardId, if (isFlexEnabled) Constants.FEATURE_FLEX else null)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribeObserver(observer)
        this.subscription = subscription
        return subscription
    }

    fun cancel() {
        subscription?.dispose()
    }
}
