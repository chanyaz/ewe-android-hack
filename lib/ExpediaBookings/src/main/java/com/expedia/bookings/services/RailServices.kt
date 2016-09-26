package com.expedia.bookings.services

import com.expedia.bookings.data.rail.requests.RailCheckoutParams
import com.expedia.bookings.data.rail.requests.api.RailApiSearchModel
import com.expedia.bookings.data.rail.responses.RailCardsResponse
import com.expedia.bookings.data.rail.responses.RailCheckoutResponse
import com.expedia.bookings.data.rail.responses.RailCreateTripResponse
import com.expedia.bookings.data.rail.responses.RailSearchResponse
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
import java.util.HashMap

class RailServices(endpoint: String, okHttpClient: OkHttpClient, interceptor: Interceptor, railRequestInterceptor: Interceptor, val observeOn: Scheduler, val subscribeOn: Scheduler) {

    val railApi by lazy {
        val gson = GsonBuilder().create()
        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient.newBuilder().addInterceptor(interceptor).addInterceptor(railRequestInterceptor).build())
                .build()

        adapter.create(RailApi::class.java)
    }

    // TODO remove this when cards endpoint is available through APIM
    val railCardsApi by lazy {
        val gson = GsonBuilder().create()
        val adapter = Retrofit.Builder()
                .baseUrl(if (endpoint.contains("localhost")) endpoint else "http://rails-domain-service.us-west-2.int.expedia.com/")
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

    fun railCreateTrip(railOfferToken: String, observer: Observer<RailCreateTripResponse>): Subscription {
        return railApi.railCreateTrip(railOfferToken)
                .subscribeOn(subscribeOn)
                .observeOn(observeOn)
                .subscribe(observer)
    }

    fun railCheckoutTrip(params: RailCheckoutParams, observer: Observer<RailCheckoutResponse>): Subscription {
        return railApi.railCheckout(params)
                .subscribeOn(subscribeOn)
                .observeOn(observeOn)
                .subscribe(observer)
    }

    fun railGetCards(locale: String, observer: Observer<RailCardsResponse>): Subscription {
        return railCardsApi.railCards(locale)
                .subscribeOn(subscribeOn)
                .observeOn(observeOn)
                .subscribe(observer)
    }
}
