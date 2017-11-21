package com.expedia.bookings.itin.services

import android.content.Context
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.user.UserStateManager
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.itin.data.Courier
import com.expedia.bookings.itin.data.TNSFlight
import com.expedia.bookings.itin.data.TNSRegisterDeviceResponse
import com.expedia.bookings.itin.data.TNSRegisterUserDeviceFlightsRequestBody
import com.expedia.bookings.itin.data.TNSRegisterUserDeviceRequestBody
import com.expedia.bookings.itin.data.TNSUser
import com.expedia.bookings.notification.GCMRegistrationKeeper
import com.expedia.bookings.utils.Ui
import com.expedia.model.UserLoginStateChangedModel
import com.google.gson.GsonBuilder
import com.mobiata.android.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.Observable
import rx.Observer
import rx.Scheduler
import rx.Subscription


open class TNSServices(endpoint: String, okHttpClient: OkHttpClient, interceptor: Interceptor, val observeOn: Scheduler, val subscribeOn: Scheduler, val context: Context) {

    private val tnsAPI: TNSApi by lazy {
        val gson = GsonBuilder().create()

        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient.newBuilder().addInterceptor(interceptor).build())
                .build()

        adapter.create(TNSApi::class.java)
    }

    private var tnsRegisterUserDeviceSubscription: Subscription? = null

    private val tempObserver: Observer<TNSRegisterDeviceResponse> = object : Observer<TNSRegisterDeviceResponse> {
        override fun onCompleted() {
        }

        override fun onError(e: Throwable) {
        }

        override fun onNext(tnsRegisterDeviceResponse: TNSRegisterDeviceResponse?) {
            Log.d("TNSServices",
                    "registerForPushNotifications response:" + if (tnsRegisterDeviceResponse == null)
                        "null"
                    else
                        tnsRegisterDeviceResponse.status)
        }
    }

    fun init() {

        val userLoginStateChangedModel: UserLoginStateChangedModel = Ui.getApplication(context).appComponent().userLoginStateChangedModel()
        userLoginStateChangedModel.userLoginStateChanged.filter { true }.subscribe {
            handleSignIn()
        }

        userLoginStateChangedModel.userLoginStateChanged.filter { false }.subscribe {
            handleSignOut()
        }
    }

    fun registerForFlights(flights: List<TNSFlight>, observer: Observer<TNSRegisterDeviceResponse>?) {
        tnsRegisterUserDeviceSubscription?.unsubscribe()

        val user = getUser()
        val courier = getDevice()
        courier ?: return
        val requestBody = TNSRegisterUserDeviceFlightsRequestBody(courier, flights, user);
        tnsRegisterUserDeviceSubscription = tnsAPI.registerUserDeviceFlights(requestBody)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribe(observer ?: object : Observer<TNSRegisterDeviceResponse> {
                    override fun onCompleted() {
                    }

                    override fun onError(e: Throwable) {
                    }

                    override fun onNext(tnsRegisterDeviceResponse: TNSRegisterDeviceResponse?) {
                        Log.d("TNSServices",
                                "registerForPushNotifications response:" + if (tnsRegisterDeviceResponse == null)
                                    "null"
                                else
                                    tnsRegisterDeviceResponse.status)
                    }
                })
    }

    fun registerForUserDevice(courier: Courier?, observer: Observer<TNSRegisterDeviceResponse> = tempObserver): Observable<TNSRegisterDeviceResponse>? {
        tnsRegisterUserDeviceSubscription?.unsubscribe()

        val user = getUser()
        val courier = courier ?: getDevice()
        courier ?: return null
        val requestBody = TNSRegisterUserDeviceRequestBody(courier, user);

        val registerUserDevice = tnsAPI.registerUserDevice(requestBody)
        val observable = registerUserDevice
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribe(observer)
        tnsRegisterUserDeviceSubscription = observable
        return registerUserDevice
    }


    private fun handleSignIn() {
        registerForUserDevice(null)
    }

    private fun handleSignOut() {
        //future work once API is ready
    }

    private fun getUser(): TNSUser {
        val siteId = PointOfSale.getPointOfSale().siteId
        var userTuid: Long = 0

        val userStateManager: UserStateManager = Ui.getApplication(context).appComponent().userStateManager()

        if (userStateManager.isUserAuthenticated()) {
            val user = userStateManager.userSource.user

            if (user != null && user.getPrimaryTraveler() != null) {
                userTuid = user.getPrimaryTraveler().getTuid()!!
            }
        }
        return TNSUser(siteId, userTuid.toString())
    }

    private fun getDevice(): Courier? {
        val token = GCMRegistrationKeeper.getInstance(context).getRegistrationId(context)
        val courier: Courier
        val applicationName = ProductFlavorFeatureConfiguration.getInstance().appNameForMobiataPushNameHeader
        if (!token.isNullOrEmpty()) {
            courier = Courier("gcm", applicationName, token)
            return courier
        }
        return null
    }

}