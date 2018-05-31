package com.expedia.bookings.flights.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.expedia.bookings.R
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.presenter.flight.FlightOutboundPresenter
import com.expedia.bookings.presenter.flight.FlightResultsPresenter
import com.expedia.bookings.services.DateTimeTypeAdapter
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.Ui
import com.expedia.vm.flights.TripType
import com.expedia.bookings.utils.bindView
import com.expedia.ui.AbstractAppCompatActivity
import com.expedia.vm.flights.FlightOffersViewModel
import com.google.gson.GsonBuilder
import org.joda.time.DateTime

class FlightResultsActivity : AbstractAppCompatActivity() {

   val flightResultPresenter by bindView<FlightResultsPresenter>(R.id.flight_results_stub)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.flight_results_stub)
        Ui.showTransparentStatusBar(this)
        if (intent.hasExtra("results")) {
            val results = intent.getStringExtra("results")
            val gson = GsonBuilder()
                    .registerTypeAdapter(DateTime::class.java, DateTimeTypeAdapter())
                    .create()
            flightResultPresenter.resultsPresenter.resultsViewModel.flightResultObservable.onNext(gson.fromJson(results, TripType.RoundTrip::class.java))

        }
    }
}
