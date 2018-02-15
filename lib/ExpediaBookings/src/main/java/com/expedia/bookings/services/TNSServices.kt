package com.expedia.bookings.services

import com.expedia.bookings.data.Courier
import com.expedia.bookings.data.TNSDeregister
import com.expedia.bookings.data.TNSFlight
import com.expedia.bookings.data.TNSRegisterDeviceResponse
import com.expedia.bookings.data.TNSRegisterUserDeviceFlightsRequestBody
import com.expedia.bookings.data.TNSRegisterUserDeviceRequestBody
import com.expedia.bookings.data.TNSUser
import com.expedia.bookings.extensions.subscribeObserver
import io.reactivex.Observer
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class TNSServices @JvmOverloads constructor(endpoint: String, okHttpClient: OkHttpClient, interceptors: List<Interceptor>, val observeOn: Scheduler, val subscribeOn: Scheduler, private val serviceObserver: Observer<TNSRegisterDeviceResponse> = TNSServices.Companion.noopObserver) : ITNSServices {

    companion object {

        val noopObserver: Observer<TNSRegisterDeviceResponse> =
                object : Observer<TNSRegisterDeviceResponse> {
                    override fun onSubscribe(d: Disposable) = Unit

                    override fun onComplete() = Unit

                    override fun onError(e: Throwable) = Unit

                    override fun onNext(tnsRegisterDeviceResponse: TNSRegisterDeviceResponse) = Unit
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
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClientBuilder.build())
                .build()

        adapter.create(TNSApi::class.java)
    }

    private var userDeviceFlightsSubscription: Disposable? = null
    private var userDeviceSubscription: Disposable? = null
    private var userDeviceDeregistrationSubscription: Disposable? = null

    override fun registerForFlights(user: TNSUser, courier: Courier, flights: List<TNSFlight>): Disposable {
        val requestBody = TNSRegisterUserDeviceFlightsRequestBody(courier, flights, user)
        userDeviceFlightsSubscription?.dispose()

        userDeviceFlightsSubscription = tnsAPI.registerUserDeviceFlights(requestBody)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribeObserver(serviceObserver)
        return userDeviceFlightsSubscription as Disposable
    }

    override fun deregisterForFlights(user: TNSUser, courier: Courier) {
        registerForFlights(user, courier, emptyList())
    }

    fun registerForUserDevice(user: TNSUser, courier: Courier): Disposable {
        userDeviceSubscription?.dispose()
        val requestBody = TNSRegisterUserDeviceRequestBody(courier, user)
        userDeviceSubscription = tnsAPI.registerUserDevice(requestBody)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribeObserver(serviceObserver)

        return userDeviceSubscription as Disposable
    }

    fun deregisterDevice(courier: Courier): Disposable {
        userDeviceDeregistrationSubscription?.dispose()
        val requestBody = TNSDeregister(courier)
        userDeviceDeregistrationSubscription = tnsAPI.deregisterUserDevice(requestBody)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribeObserver(serviceObserver)

        return userDeviceDeregistrationSubscription as Disposable
    }
}
