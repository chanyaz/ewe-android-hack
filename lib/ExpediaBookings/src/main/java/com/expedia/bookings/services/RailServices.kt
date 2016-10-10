package com.expedia.bookings.services

import com.expedia.bookings.data.CardFeeResponse
import com.expedia.bookings.data.rail.requests.RailCheckoutParams
import com.expedia.bookings.data.rail.requests.api.RailApiSearchModel
import com.expedia.bookings.data.rail.responses.RailCardsResponse
import com.expedia.bookings.data.rail.responses.RailCheckoutResponse
import com.expedia.bookings.data.rail.responses.RailCreateTripResponse
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.rail.RailConstants
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

class RailServices(endpointMap: HashMap<String, String>, okHttpClient: OkHttpClient, interceptor: Interceptor, val observeOn: Scheduler, val subscribeOn: Scheduler) {

    var subscription: Subscription? = null

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
    val domainUrl = endpointMap[Constants.MOCK_MODE] ?: endpointMap[Constants.DOMAIN]
    val mobileUrl = endpointMap[Constants.MOCK_MODE] ?: endpointMap[Constants.MOBILE]

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
        cancel()
        val subscription = railApi.railSearch(params)
                .doOnNext(BUCKET_FARE_QUALIFIERS_AND_CHEAPEST_PRICE)
                .subscribeOn(subscribeOn)
                .observeOn(observeOn)
                .subscribe(observer)
        this.subscription = subscription
        return subscription
    }

    private val BUCKET_FARE_QUALIFIERS_AND_CHEAPEST_PRICE = { response: RailSearchResponse ->
        if (!response.hasError()) {
            val outboundLeg = response.findLegWithBoundOrder(RailConstants.OUTBOUND_BOUND_ORDER)!!
            for (legOption: RailLegOption in outboundLeg.legOptionList) {
                for (railOffer: RailSearchResponse.RailOffer in response.offerList) {
                    val railOfferWithFareQualifiers = railOffer.railProductList.filter { !it.fareQualifierList.isEmpty() }
                    val railOfferLegListWithFareQualifiers = railOfferWithFareQualifiers.flatMap { it.legOptionIndexList }

                    if (railOfferLegListWithFareQualifiers.contains(legOption.legOptionIndex)) {
                        legOption.doesAnyOfferHasFareQualifier = true
                        break
                    }
                }
            }
            if (response.hasInbound()) {
                val inboundLeg = response.findLegWithBoundOrder(RailConstants.INBOUND_BOUND_ORDER)!!
                outboundLeg.cheapestInboundPrice = inboundLeg.cheapestPrice
            }
        }
    }

    fun railCreateTrip(railOfferToken: String, observer: Observer<RailCreateTripResponse>): Subscription {
        cancel()
        val subscription = railMApi.railCreateTrip(railOfferToken)
                .subscribeOn(subscribeOn)
                .observeOn(observeOn)
                .subscribe(observer)
        this.subscription = subscription
        return subscription
    }

    fun railCheckoutTrip(params: RailCheckoutParams, observer: Observer<RailCheckoutResponse>): Subscription {
        cancel()
        val subscription = railMApi.railCheckout(params)
                .subscribeOn(subscribeOn)
                .observeOn(observeOn)
                .subscribe(observer)
        this.subscription = subscription
        return subscription
    }

    fun railGetCards(locale: String, observer: Observer<RailCardsResponse>): Subscription {
        cancel()
        val subscription = railApi.railCards(locale)
                .subscribeOn(subscribeOn)
                .observeOn(observeOn)
                .subscribe(observer)
        this.subscription = subscription
        return subscription
    }

    fun railGetCardFees(tripId: String, creditCardId: String, ticketDeliveryOption: String, observer: Observer<CardFeeResponse>): Subscription {
        cancel()
        val subscription = railMApi.cardFees(tripId, creditCardId, ticketDeliveryOption)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribe(observer)
        this.subscription = subscription
        return subscription
    }

    fun cancel() {
        // cancels any existing calls we're waiting on
        subscription?.unsubscribe()
    }
}
