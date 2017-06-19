package com.expedia.bookings.activity

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.expedia.bookings.R
import com.expedia.bookings.data.cars.CarSearch
import com.expedia.bookings.data.cars.CarSearchParam
import com.expedia.bookings.data.lx.LXSearchResponse
import com.expedia.bookings.data.lx.LxSearchParams
import com.expedia.bookings.services.LxServices
import com.expedia.bookings.utils.Ui
import com.expedia.hackathon.LXCrossSellAdapter
import com.tomerrosenfeld.customanalogclockview.CustomAnalogClock
import org.joda.time.LocalDate
import java.util.*
import com.expedia.bookings.data.rail.responses.TranslateResponse
import com.expedia.bookings.services.TranslateServices
import com.expedia.hackathon.CarCrossSellAdapter
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
import org.joda.time.DateTime
import java.util.Arrays


class HackActivity : AppCompatActivity() {

    var inputTranslate: EditText? = null
    var outputTranslate: TextView? = null
    var uberButton: RideRequestButton? = null
    val translateServices: TranslateServices = TranslateServices(AndroidSchedulers.mainThread(), Schedulers.io())
    var config: SessionConfiguration? = null
    var button: FloatingActionButton? = null

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
        button = findViewById(R.id.floating_button) as FloatingActionButton

        (button as FloatingActionButton).setOnClickListener {
            val intent = Intent(this, DownloadPrompt::class.java)
            startActivity(intent)
        }

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
            val drawable = getDrawable(R.drawable.ic_action_bar_brand_logo)
            drawable.alpha = Math.abs(offset * 255) / appBarLayout.totalScrollRange
            myToolbar.navigationIcon = drawable
        })

        setupMyViews()
    }

    private fun setupMyViews() {
        val departureClock = findViewById(R.id.departure_clock) as CustomAnalogClock
        departureClock.setTimezone(TimeZone.getTimeZone("Asia/Calcutta"))

        val arrivalClock = findViewById(R.id.arrival_clock) as CustomAnalogClock
        arrivalClock.setTimezone(TimeZone.getTimeZone("PST"))

        val lxCrossSell = findViewById(R.id.lx_cross_sell) as RecyclerView
        val params = LxSearchParams.Builder()
                .location("Paris")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(14))
                .build() as LxSearchParams

        Ui.getApplication(this).defaultLXComponents()

        Ui.getApplication(this).lxComponent().lxService.lxCategorySearch(params, object: Observer<LXSearchResponse> {
            override fun onError(e: Throwable?) {

            }

            override fun onCompleted() {
            }

            override fun onNext(t: LXSearchResponse?) {
                lxCrossSell.adapter = LXCrossSellAdapter(t!!.activities)
            }

        })

        val carCrossSell = findViewById(R.id.car_cross_sell) as RecyclerView

        val carparams = CarSearchParam("SFO", DateTime.now().plusDays(5), DateTime.now().plusDays(6))
        Ui.getApplication(this).defaultCarComponents()
        Ui.getApplication(this).carComponent().carServices.carSearch(carparams, object: Observer<CarSearch> {
            override fun onNext(t: CarSearch?) {
                carCrossSell.adapter = CarCrossSellAdapter(t!!.categories)
            }

            override fun onError(e: Throwable?) {
            }

            override fun onCompleted() {

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