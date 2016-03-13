package com.expedia.bookings.presenter.flight

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.flights.FlightCreateTripParams
import com.expedia.bookings.data.flights.FlightCreateTripViewModel
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.presenter.LeftToRightTransition
import com.expedia.bookings.presenter.packages.FlightBasePresenter
import com.expedia.bookings.presenter.packages.FlightOverviewPresenter
import com.expedia.bookings.presenter.packages.PackageFlightOverviewPresenter
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.utils.CurrencyUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FlightListAdapter
import com.expedia.util.notNullAndObservable
import com.expedia.vm.FlightCheckoutViewModel
import com.expedia.vm.FlightSearchViewModel
import com.squareup.phrase.Phrase
import org.joda.time.LocalDate
import java.math.BigDecimal
import javax.inject.Inject

class FlightOutboundPresenter(context: Context, attrs: AttributeSet) : FlightBasePresenter(context, attrs) {

    init {
        resultsPresenter.adapterPackage = FlightListAdapter(context, resultsPresenter.flightSelectedSubject)
        resultsPresenter.recyclerView.adapter = resultsPresenter.adapterPackage
        toolbarViewModel.isOutboundSearch.onNext(true)
    }
}

