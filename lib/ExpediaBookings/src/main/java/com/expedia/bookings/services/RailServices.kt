package com.expedia.bookings.services

import com.expedia.bookings.data.rail.requests.RailCheckoutRequest
import com.expedia.bookings.data.rail.requests.RailDetailsRequest
import com.expedia.bookings.data.rail.requests.RailValidateRequest
import com.expedia.bookings.data.rail.requests.api.RailApiSearchModel
import com.expedia.bookings.data.rail.responses.RailCheckoutResponse
import com.expedia.bookings.data.rail.responses.RailCreateTripResponse
import com.expedia.bookings.data.rail.responses.RailDetailsResponse
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import com.expedia.bookings.data.rail.responses.RailValidateResponse
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.Observer
import rx.Scheduler
import rx.Subscription

class RailServices(endpoint: String, okHttpClient: OkHttpClient, interceptor: Interceptor, val observeOn: Scheduler, val subscribeOn: Scheduler) {

    val railApi by lazy {

        val gson = GsonBuilder().create();
        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient.newBuilder().addInterceptor(interceptor).build())
                .build()

        adapter.create(RailApi::class.java)
    }

    fun railSearch(params: RailApiSearchModel, observer: Observer<RailSearchResponse>): Subscription {
        return railApi.railSearch(params)
                .subscribeOn(subscribeOn)
                .observeOn(observeOn)
                .subscribe(observer)
    }

    fun railDetails(params: RailDetailsRequest, observer: Observer<RailDetailsResponse>): Subscription {
        return railApi.railDetails(params)
                .subscribeOn(subscribeOn)
                .observeOn(observeOn)
                .subscribe(observer)
    }

    fun railValidate(params: RailValidateRequest, observer: Observer<RailValidateResponse>): Subscription {
        return railApi.railValidate(params)
                .subscribeOn(subscribeOn)
                .observeOn(observeOn)
                .subscribe(observer)
    }

    fun railCreateTrip(railOfferToken: String, observer: Observer<RailCreateTripResponse>): Subscription {
        return railApi.railCreateTrip(railOfferToken)
                .subscribeOn(subscribeOn)
                .observeOn(observeOn)
                .subscribe(observer)
    }

    fun railCheckoutTrip(params: RailCheckoutRequest, observer: Observer<RailCheckoutResponse>): Subscription {
        return railApi.railCheckout(params)
                .subscribeOn(subscribeOn)
                .observeOn(observeOn)
                .subscribe(observer)
    }
}
