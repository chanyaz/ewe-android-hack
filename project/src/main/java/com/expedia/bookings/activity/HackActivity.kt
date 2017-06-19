package com.expedia.bookings.activity

import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.responses.TranslateResponse
import com.expedia.bookings.services.TranslateServices
import com.jakewharton.rxbinding.widget.RxTextView
import com.uber.sdk.android.core.UberSdk
import com.uber.sdk.android.rides.RideRequestButton
import rx.Observer
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import com.uber.sdk.android.rides.RideParameters
import com.uber.sdk.core.auth.Scope
import com.uber.sdk.rides.client.ServerTokenSession
import com.uber.sdk.rides.client.SessionConfiguration
import java.util.Arrays


class HackActivity : AppCompatActivity() {

    var inputTranslate: EditText? = null
    var outputTranslate: TextView? = null
    var uberButton: RideRequestButton? = null
    val translateServices: TranslateServices = TranslateServices(AndroidSchedulers.mainThread(), Schedulers.io())
    var config: SessionConfiguration? = null
    init {
        /*config = SessionConfiguration.Builder().setClientId("03PqsgLaSnDv0oDa3QqNAHpOB-68qm_8")
                .setServerToken("IObjvb1JZW9Jpp5cxQUPyohlFGkyV6NVTvFUsKRq")
                .setScopes(Arrays.asList(Scope.RIDE_WIDGETS))
                .setEnvironment(SessionConfiguration.Environment.SANDBOX)
                .build()

        UberSdk.initialize(config)*/

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hack_activity)

        val config = SessionConfiguration.Builder().setClientId("03PqsgLaSnDv0oDa3QqNAHpOB-68qm_8")
                .setServerToken("IObjvb1JZW9Jpp5cxQUPyohlFGkyV6NVTvFUsKRq")
                .setScopes(Arrays.asList(Scope.RIDE_WIDGETS))
                .setEnvironment(SessionConfiguration.Environment.SANDBOX)
                .build()

        UberSdk.initialize(config)

        val myToolbar = findViewById(R.id.main_toolbar) as Toolbar

        val appBarLayout = findViewById(R.id.main_appbar) as AppBarLayout
        inputTranslate = findViewById(R.id.input_translate) as EditText
        outputTranslate = findViewById(R.id.output_translate) as TextView
        uberButton = findViewById(R.id.uber_button) as RideRequestButton


        val rideParams = RideParameters.Builder()
                // Optional product_id from /v1/products endpoint (e.g. UberX). If not provided, most cost-efficient product will be used
                .setProductId("a1111c8c-c720-46c3-8534-2fcdd730040d")
                // Required for price estimates; lat (Double), lng (Double), nickname (String), formatted address (String) of dropoff location
                .setDropoffLocation(
                        37.775303, -122.417522, "Uber HQ", "1455 Market Street, San Francisco")
                // Required for pickup estimates; lat (Double), lng (Double), nickname (String), formatted address (String) of pickup location
                .setPickupLocation(37.775304, -122.417522, "Uber HQ", "1455 Market Street, San Francisco")
                // Required for price estimates; lat (Double), lng (Double), nickname (String), formatted address (String) of dropoff location.
                .setDropoffLocation(37.795079, -122.4397805, "Embarcadero", "One Embarcadero Center, San Francisco")
                .build()
        // set parameters for the RideRequestButton instance
        (uberButton as RideRequestButton).setRideParameters(rideParams)

        val session = ServerTokenSession(config!!)
        (uberButton as RideRequestButton).setSession(session)

        (uberButton as RideRequestButton).loadRideInformation()

        appBarLayout.addOnOffsetChangedListener({ appBarLayout: AppBarLayout, offset: Int ->
            if (Math.abs(offset) >= appBarLayout.getTotalScrollRange()) {
                myToolbar.setNavigationIcon(R.drawable.ic_action_bar_brand_logo)
            } else {
                myToolbar.navigationIcon = null
            }
        })

        RxTextView.textChanges(inputTranslate as EditText).debounce(500, TimeUnit.MILLISECONDS).subscribe {
            Log.d("Testingggg", it.toString())
            translateServices.translate(it.toString(), object : Observer<TranslateResponse> {
                override fun onError(e: Throwable?) {
                    //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onCompleted() {
                    //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onNext(t: TranslateResponse?) {
                    Log.d("Testingggg", t!!.text[0])
                    (outputTranslate as TextView).text = t!!.text[0]

                }

            })
        }
    }

}