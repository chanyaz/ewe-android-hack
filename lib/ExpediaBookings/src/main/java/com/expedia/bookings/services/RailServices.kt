package com.expedia.bookings.services

import com.expedia.bookings.data.rail.requests.RailDetailsRequest
import com.expedia.bookings.data.rail.requests.RailSearchRequest
import com.expedia.bookings.data.rail.requests.api.RailApiSearchModel
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import com.expedia.bookings.data.rail.requests.RailValidateRequest
import com.expedia.bookings.data.rail.responses.RailCreateTripResponse
import com.expedia.bookings.data.rail.responses.RailDetailsResponse
import com.expedia.bookings.data.rail.responses.RailValidateResponse
import com.google.gson.GsonBuilder
import com.squareup.okhttp.OkHttpClient
import retrofit.RequestInterceptor
import retrofit.RestAdapter
import retrofit.client.OkClient
import retrofit.converter.GsonConverter
import rx.Observer
import rx.Scheduler
import rx.Subscription

class RailServices(endpoint: String, okHttpClient: OkHttpClient, requestInterceptor: RequestInterceptor, val observeOn: Scheduler, val subscribeOn: Scheduler, logLevel: RestAdapter.LogLevel) {

    val railApi by lazy {

        val gson = GsonBuilder().create();
        val adapter = RestAdapter.Builder()
                .setEndpoint(endpoint)
                .setRequestInterceptor(requestInterceptor)
                .setLogLevel(logLevel)
                .setConverter(GsonConverter(gson))
                .setClient(OkClient(okHttpClient))
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
}
