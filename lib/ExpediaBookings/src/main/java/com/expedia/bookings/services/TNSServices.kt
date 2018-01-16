package com.expedia.bookings.services

import com.expedia.bookings.data.Courier
import com.expedia.bookings.data.TNSDeregister
import com.expedia.bookings.data.TNSFlight
import com.expedia.bookings.data.TNSRegisterDeviceResponse
import com.expedia.bookings.data.TNSRegisterUserDeviceFlightsRequestBody
import com.expedia.bookings.data.TNSRegisterUserDeviceRequestBody
import com.expedia.bookings.data.TNSUser
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.Observer
import rx.Scheduler
import rx.Subscription

class TNSServices @JvmOverloads constructor(endpoint: String, okHttpClient: OkHttpClient, interceptors: List<Interceptor>, val observeOn: Scheduler, val subscribeOn: Scheduler, private val serviceObserver: Observer<TNSRegisterDeviceResponse> = TNSServices.Companion.noopObserver) : ITNSServices {

    companion object {

        val noopObserver: Observer<TNSRegisterDeviceResponse> =
                object : Observer<TNSRegisterDeviceResponse> {
                    override fun onCompleted() = Unit

                    override fun onError(e: Throwable) = Unit

                    override fun onNext(tnsRegisterDeviceResponse: TNSRegisterDeviceResponse?) = Unit
                }
    }

    private val tnsAPI: TNSApi by lazy {

        val okHttpClientBuilder = okHttpClient.newBuilder()
        for (interceptor in interceptors) {
            okHttpClientBuilder.addInterceptor(interceptor)
        }
        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClientBuilder.build())
                .build()

        adapter.create(TNSApi::class.java)
    }

    private var userDeviceFlightsSubscription: Subscription? = null
    private var userDeviceSubscription: Subscription? = null
    private var userDeviceDeregistrationSubscription: Subscription? = null

    override fun registerForFlights(user: TNSUser, courier: Courier, flights: List<TNSFlight>): Subscription {
        val requestBody = TNSRegisterUserDeviceFlightsRequestBody(courier, flights, user)
        userDeviceFlightsSubscription?.unsubscribe()

        userDeviceFlightsSubscription = tnsAPI.registerUserDeviceFlights(requestBody)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribe(serviceObserver)
        return userDeviceFlightsSubscription as Subscription
    }

    override fun deregisterForFlights(user: TNSUser, courier: Courier) {
        registerForFlights(user, courier, emptyList())
    }

    fun registerForUserDevice(user: TNSUser, courier: Courier): Subscription {
        userDeviceSubscription?.unsubscribe()
        val requestBody = TNSRegisterUserDeviceRequestBody(courier, user)
        userDeviceSubscription = tnsAPI.registerUserDevice(requestBody)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribe(serviceObserver)

        return userDeviceSubscription as Subscription
    }

    fun deregisterDevice(courier: Courier): Subscription {
        userDeviceDeregistrationSubscription?.unsubscribe()
        val requestBody = TNSDeregister(courier)
        userDeviceDeregistrationSubscription = tnsAPI.deregisterUserDevice(requestBody)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribe(serviceObserver)

        return userDeviceDeregistrationSubscription as Subscription
    }
}