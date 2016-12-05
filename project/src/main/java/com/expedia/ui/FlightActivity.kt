package com.expedia.ui

import android.os.Bundle
import com.expedia.bookings.R
import com.expedia.bookings.data.Codes
import com.expedia.bookings.otto.Events
import com.expedia.bookings.presenter.flight.FlightPresenter
import com.expedia.bookings.utils.FlightsV2DataUtil
import com.expedia.bookings.utils.Ui

class FlightActivity : TrackingAbstractAppCompatActivity() {
    val flightsPresenter: FlightPresenter by lazy {
        findViewById(R.id.flight_presenter) as FlightPresenter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Ui.getApplication(this).defaultFlightComponents()
        Ui.getApplication(this).defaultTravelerComponent()
        setContentView(R.layout.flight_activity)
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
        }
    }

    private fun handleDeeplink() {
        val searchParams = FlightsV2DataUtil.getFlightSearchParamsFromJSON(intent.getStringExtra(Codes.SEARCH_PARAMS))
        if (searchParams != null) {
            flightsPresenter.searchViewModel.performDeepLinkFlightSearch(searchParams)
        } else {
            flightsPresenter.setDefaultTransition(Screen.SEARCH)
        }
    }

    override fun onBackPressed() {
        if (!flightsPresenter.back()) {
            super.onBackPressed()
        }
    }

    enum class Screen {
        SEARCH, RESULTS
    }
}