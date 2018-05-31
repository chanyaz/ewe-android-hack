package com.expedia.bookings.flights.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.expedia.bookings.R
import com.expedia.bookings.data.Codes
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.flights.presenter.FlightShoppingPresenter
import com.expedia.bookings.otto.Events
import com.expedia.bookings.services.DateTimeTypeAdapter
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.FlightsV2DataUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.ui.AbstractAppCompatActivity
import com.expedia.vm.flights.TripType
import com.google.gson.GsonBuilder
import org.joda.time.DateTime

class FlightShoppingControllerActivity : AbstractAppCompatActivity() {
    val flightsPresenter by bindView<FlightShoppingPresenter>(R.id.flight_shopping_presenter)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Ui.getApplication(this).defaultFlightComponents()
        Ui.getApplication(this).defaultTravelerComponent()
        setContentView(R.layout.flight_shopping_activity)
        Ui.showTransparentStatusBar(this)
        if (intent.hasExtra(Codes.SEARCH_PARAMS)) {
            handleDeeplink()
        } else {
            flightsPresenter.setDefaultTransition(Screen.SEARCH)
        }
    }

    override fun onResume() {
        super.onResume()
        Events.post(Events.AppBackgroundedOnResume())
    }

    override fun onPause() {
        super.onPause()
        if (isFinishing) {
            clearCCNumber()
            clearStoredCard()
        } else {
            Ui.hideKeyboard(this)
        }
    }

    private fun handleDeeplink() {
        val searchParams = FlightsV2DataUtil.getFlightSearchParamsFromJSON(intent.getStringExtra(Codes.SEARCH_PARAMS))
        if (searchParams != null) {
            flightsPresenter.searchViewModel.searchTravelerParamsObservable.onNext(searchParams)
        } else {
            flightsPresenter.setDefaultTransition(Screen.SEARCH)
        }
    }

    override fun onBackPressed() {
        if (!flightsPresenter.back()) {
            super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            Constants.FLIGHT_REQUEST_CODE -> when (resultCode) {
                android.app.Activity.RESULT_OK -> finish()
            }
        }
        when (resultCode) {
            Activity.RESULT_OK -> {
                if (data!!.hasExtra("results")) {
                    val results = data.getStringExtra("results")
                    val gson = GsonBuilder()
                            .registerTypeAdapter(DateTime::class.java, DateTimeTypeAdapter())
                            .create()
                    flightsPresenter.openFlightsResults(gson.fromJson(results, TripType.RoundTrip::class.java))
                }
            }
        }
    }

    enum class Screen {
        SEARCH, RESULTS
    }
}

