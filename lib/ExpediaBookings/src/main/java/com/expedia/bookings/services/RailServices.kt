package com.expedia.bookings.services

import com.expedia.bookings.data.CardFeeResponse
import com.expedia.bookings.data.rail.deserializers.RailCheckoutResponseDeserializer
import com.expedia.bookings.data.rail.requests.MessageInfo
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
import java.util.Collections
import java.util.UUID

open class RailServices(endpoint: String, okHttpClient: OkHttpClient, interceptor: Interceptor, railRequestInterceptor: Interceptor, hmacInterceptor: Interceptor? = null, val isUserBucketedInAPIMAuth: Boolean, val observeOn: Scheduler, val subscribeOn: Scheduler) {

    // "Session" Good enough for MVP.
    val userSession = UUID.randomUUID().toString().replace("-".toRegex(), "")

    var subscription: Subscription? = null

    private val railApi by lazy {

        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(buildRailGsonConverter())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient.newBuilder().addInterceptor(interceptor).addInterceptor(railRequestInterceptor).build())
                .build()

        adapter.create(RailApi::class.java)
    }

    private val railApiHmac by lazy {

        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(buildRailGsonConverter())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient.newBuilder().addInterceptor(interceptor).addInterceptor(hmacInterceptor).build())
                .build()

        adapter.create(RailApiHMAC::class.java)
    }

    fun railSearch(params: RailApiSearchModel, observer: Observer<RailSearchResponse>): Subscription {
        cancel()
        params.messageInfo = generateMessageInfo()
        val observable = if (isUserBucketedInAPIMAuth) railApiHmac.railSearch(params) else railApi.railSearch(params)
        val subscription = observable.doOnNext(BUCKET_FARE_QUALIFIERS_AND_CHEAPEST_PRICE)
                .subscribeOn(subscribeOn)
                .observeOn(observeOn)
                .subscribe(observer)
        this.subscription = subscription
        return subscription
    }

    private val BUCKET_FARE_QUALIFIERS_AND_CHEAPEST_PRICE = { response: RailSearchResponse ->
        if (!response.hasError()) {
            val outboundLeg = response.outboundLeg!!

            // Sorting legOptionList by DepartureDateTime
            Collections.sort(outboundLeg.legOptionList)

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

                // Sorting legOptionList by DepartureDateTime
                Collections.sort(inboundLeg.legOptionList)

                outboundLeg.cheapestInboundPrice = inboundLeg.cheapestPrice
            }
        }
    }

    fun railCreateTrip(railOfferTokens: List<String>, observer: Observer<RailCreateTripResponse>): Subscription {
        cancel()
        val request = RailCreateTripRequest(railOfferTokens)
        request.messageInfo = generateMessageInfo()
        val observable = if (isUserBucketedInAPIMAuth) railApiHmac.railCreateTrip(request) else railApi.railCreateTrip(request)
        val subscription = observable.subscribeOn(subscribeOn)
                .observeOn(observeOn)
                .subscribe(observer)
        this.subscription = subscription
        return subscription
    }

    fun railCheckoutTrip(params: RailCheckoutParams, observer: Observer<RailCheckoutResponseWrapper>): Subscription {
        cancel()
        params.messageInfo = generateMessageInfo()
        val observable = if (isUserBucketedInAPIMAuth) railApiHmac.railCheckout(params) else railApi.railCheckout(params)
        val subscription = observable.subscribeOn(subscribeOn)
                .observeOn(observeOn)
                .subscribe(observer)
        this.subscription = subscription
        return subscription
    }

    open fun railGetCards(locale: String, observer: Observer<RailCardsResponse>): Subscription {
        cancel()
        val observable = if (isUserBucketedInAPIMAuth) railApiHmac.railCards(locale) else railApi.railCards(locale)
        val subscription = observable.subscribeOn(subscribeOn)
                .observeOn(observeOn)
                .subscribe(observer)
        this.subscription = subscription
        return subscription
    }

    fun railGetCardFees(tripId: String, creditCardId: String, ticketDeliveryOption: String, observer: Observer<CardFeeResponse>): Subscription {
        cancel()
        val observable = if (isUserBucketedInAPIMAuth) railApiHmac.cardFees(tripId, creditCardId, ticketDeliveryOption) else railApi.cardFees(tripId, creditCardId, ticketDeliveryOption)
        val subscription = observable.observeOn(observeOn)
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

    private fun generateMessageInfo() : MessageInfo {
        val messageInfo = MessageInfo(userSession)
        return messageInfo
    }
}
