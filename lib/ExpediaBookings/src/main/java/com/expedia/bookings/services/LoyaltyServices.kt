package com.expedia.bookings.services

import com.expedia.bookings.data.payment.CalculatePointsParams
import com.expedia.bookings.data.payment.CalculatePointsResponse
import com.expedia.bookings.data.payment.CampaignDetails
import com.expedia.bookings.data.payment.ContributeResponse
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.Observer
import rx.Scheduler
import rx.Subscription

class LoyaltyServices(endpoint: String, val okHttpClient: OkHttpClient, interceptor: Interceptor, val observeOn: Scheduler, val subscribeOn: Scheduler) {

    val loyaltyApi: LoyaltyApi by lazy {
        val gson = GsonBuilder().create()

        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient.newBuilder().addInterceptor(interceptor).build())
                .build()

        adapter.create(LoyaltyApi::class.java)
    }

    val contributeApi: LoyaltyApi by lazy {
        val gson = GsonBuilder().create()

        val adapter = Retrofit.Builder()
                .baseUrl("http://172.26.64.21:8080")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(OkHttpClient().newBuilder().addNetworkInterceptor(okHttpClient.networkInterceptors()[0]).build())
                .build()

        adapter.create(LoyaltyApi::class.java)
    }

    fun currencyToPoints(calculatePointsParams: CalculatePointsParams, observer: Observer<CalculatePointsResponse>): Subscription {
        return loyaltyApi.currencyToPoints(calculatePointsParams.tripId, calculatePointsParams.programName, calculatePointsParams.amount, calculatePointsParams.rateId)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribe(observer)
    }

    fun contribute(donorTUID: String, donorName: String, recieverTUID: String, amount: String, tripID: String, observer: Observer<ContributeResponse>): Subscription {
        return contributeApi.contribute(donorTUID, donorName, recieverTUID, amount, tripID)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribe(observer)
    }

    fun register(tripID: String, title: String, message: String, fundsRequested: String, fundsAvailable: String, imageURL: String, observer: Observer<ContributeResponse>) {
        contributeApi.register(tripID, title, message, fundsRequested, fundsAvailable, imageURL)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribe(observer)
    }

    fun getCampainDetails(tripID: String, observer: Observer<CampaignDetails>) : Subscription {
        return contributeApi.campaignDetails(tripID)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribe(observer)
    }

}
