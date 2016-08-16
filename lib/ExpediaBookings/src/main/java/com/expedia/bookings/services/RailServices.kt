package com.expedia.bookings.services

import com.expedia.bookings.data.rail.requests.RailCheckoutRequest
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
import java.util.*

class RailServices(endpointMap: HashMap<String, String>, okHttpClient: OkHttpClient, interceptor: Interceptor, val observeOn: Scheduler, val subscribeOn: Scheduler) {

    val railApi by lazy {
        val gson = GsonBuilder().create();
        val adapter = Retrofit.Builder()
                .baseUrl(domainUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient.newBuilder().addInterceptor(interceptor).build())
                .build()

        adapter.create(RailApi::class.java)
    }

    //TODO remove this once domain and mobile endpoints available in integration
    val domainUrl = endpointMap.get(Constants.MOCK_MODE) ?: endpointMap.get(Constants.DOMAIN)
    val mobileUrl = endpointMap.get(Constants.MOCK_MODE) ?: endpointMap.get(Constants.MOBILE)

    val railMApi by lazy {
        val gson = GsonBuilder().create();
        val adapter = Retrofit.Builder()
                .baseUrl(mobileUrl)
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
        return railMApi.railCreateTrip(railOfferToken)
                .subscribeOn(subscribeOn)
                .observeOn(observeOn)
                .subscribe(observer)
    }

    fun railCheckoutTrip(params: RailCheckoutRequest, observer: Observer<RailCheckoutResponse>): Subscription {
        return railMApi.railCheckout(params)
                .subscribeOn(subscribeOn)
                .observeOn(observeOn)
                .subscribe(observer)
    }

    fun railGetCards(locale: String, observer: Observer<RailCardsResponse>): Subscription {
        return railApi.railCards(locale)
                .subscribeOn(subscribeOn)
                .observeOn(observeOn)
                .subscribe(observer)
    }
}
