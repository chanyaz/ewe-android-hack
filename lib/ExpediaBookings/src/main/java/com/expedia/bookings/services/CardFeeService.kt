package com.expedia.bookings.services

import com.expedia.bookings.data.CardFeeResponse
import com.expedia.bookings.utils.Constants
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.Observer
import rx.Scheduler
import rx.Subscription

// open so we can mock for tests
open class CardFeeService(endpoint: String, okHttpClient: OkHttpClient, interceptor: Interceptor, val observeOn: Scheduler, val subscribeOn: Scheduler) {

    var subscription: Subscription? = null

    val cardFeeApi: CardFeeApi by lazy {
        val gson = GsonBuilder().create()

        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient.newBuilder().addInterceptor(interceptor).build())
                .build()

        adapter.create(CardFeeApi::class.java)
    }

    // open so we can mock for tests
    open fun getCardFees(tripId: String, creditCardId: String, isFlexEnabled: Boolean, observer: Observer<CardFeeResponse>): Subscription {
        subscription?.unsubscribe() // cancels any existing calls we're waiting on
        val subscription = cardFeeApi.cardFee(tripId, creditCardId, if (isFlexEnabled) Constants.FEATURE_FLEX else null)
                                     .observeOn(observeOn)
                                     .subscribeOn(subscribeOn)
                                     .subscribe(observer)
        this.subscription = subscription
        return subscription
    }

    fun cancel() {
        subscription?.unsubscribe()
    }
}
