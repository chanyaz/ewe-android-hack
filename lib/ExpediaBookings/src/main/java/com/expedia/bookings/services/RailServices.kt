package com.expedia.bookings.services

import com.expedia.bookings.data.CardFeeResponse
import com.expedia.bookings.data.rail.deserializers.RailCheckoutResponseDeserializer
import com.expedia.bookings.data.rail.requests.RailCheckoutParams
import com.expedia.bookings.data.rail.requests.RailCreateTripRequest
import com.expedia.bookings.data.rail.requests.api.RailApiSearchModel
import com.expedia.bookings.data.rail.responses.RailCardsResponse
import com.expedia.bookings.data.rail.responses.RailCheckoutResponseWrapper
import com.expedia.bookings.data.rail.responses.RailCreateTripResponse
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.data.rail.responses.RailOffer
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.Observer
import rx.Scheduler
import rx.Subscription

class RailServices(endpoint: String, okHttpClient: OkHttpClient, interceptor: Interceptor, railRequestInterceptor: Interceptor, val observeOn: Scheduler, val subscribeOn: Scheduler) {

    var subscription: Subscription? = null

    val railApi by lazy {
        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(buildRailGsonConverter())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient.newBuilder().addInterceptor(interceptor).addInterceptor(railRequestInterceptor).build())
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
            val outboundLeg = response.outboundLeg!!
            for (legOption: RailLegOption in outboundLeg.legOptionList) {
                for (railOffer: RailOffer in response.offerList) {
                    val railOfferWithFareQualifiers = railOffer.railProductList.filter { !it.fareQualifierList.isEmpty() }
                    val railOfferLegListWithFareQualifiers = railOfferWithFareQualifiers.flatMap { it.legOptionIndexList }

                    if (railOfferLegListWithFareQualifiers.contains(legOption.legOptionIndex)) {
                        legOption.doesAnyOfferHasFareQualifier = true
                        break
                    }
                }
            }

            if (response.hasInbound()) {
                val inboundLeg = response.inboundLeg!!
                outboundLeg.cheapestInboundPrice = inboundLeg.cheapestPrice
            }
        }
    }

    fun railCreateTrip(railOfferTokens: List<String>, observer: Observer<RailCreateTripResponse>): Subscription {
        cancel()
        val subscription = railApi.railCreateTrip(RailCreateTripRequest(railOfferTokens))
                .subscribeOn(subscribeOn)
                .observeOn(observeOn)
                .subscribe(observer)
        this.subscription = subscription
        return subscription
    }

    fun railCheckoutTrip(params: RailCheckoutParams, observer: Observer<RailCheckoutResponseWrapper>): Subscription {
        cancel()
        val subscription = railApi.railCheckout(params)
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
        val subscription = railApi.cardFees(tripId, creditCardId, ticketDeliveryOption)
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

    private fun buildRailGsonConverter() : GsonConverterFactory {
        val gsonBuilder = GsonBuilder();
        gsonBuilder.registerTypeAdapter(RailCheckoutResponseWrapper::class.java, RailCheckoutResponseDeserializer());
        val myGson = gsonBuilder.create();
        return GsonConverterFactory.create(myGson);
    }
}
