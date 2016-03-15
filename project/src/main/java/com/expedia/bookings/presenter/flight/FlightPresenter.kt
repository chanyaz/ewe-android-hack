package com.expedia.bookings.presenter.flight

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewStub
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightCreateTripParams
import com.expedia.bookings.data.flights.FlightCreateTripViewModel
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.presenter.LeftToRightTransition
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.ScaleTransition
import com.expedia.bookings.presenter.flight.FlightOverviewPresenter
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.utils.CurrencyUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.FlightCheckoutViewModel
import com.expedia.vm.FlightSearchViewModel
import com.expedia.vm.PackageSearchType
import com.squareup.phrase.Phrase
import org.joda.time.LocalDate
import java.math.BigDecimal
import javax.inject.Inject

class FlightPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {
    lateinit var flightServices: FlightServices
        @Inject set

    val outBoundPresenter: FlightOutboundPresenter by lazy {
        var viewStub = findViewById(R.id.outbound_presenter) as ViewStub
        var presenter = viewStub.inflate() as FlightOutboundPresenter
        presenter
    }

    val inboundPresenter: FlightInboundPresenter by lazy {
        var viewStub = findViewById(R.id.inbound_presenter) as ViewStub
        var presenter = viewStub.inflate() as FlightInboundPresenter
        presenter
    }

    val flightOverviewPresenter: FlightOverviewPresenter by bindView(R.id.widget_bundle_overview)
    val searchParamsBuilder = FlightSearchParams.Builder(context.resources.getInteger(R.integer.calendar_max_days_flight_search))
    var searchParams : FlightSearchParams? = null

    // TODO: Add FlightSearchPresenter
    var searchViewModel: FlightSearchViewModel by notNullAndObservable { vm ->
        flightOverviewPresenter.flightSummary.outboundFlightWidget.viewModel.selectedFlightObservable.onNext(PackageSearchType.OUTBOUND_FLIGHT)
        flightOverviewPresenter.flightSummary.inboundFlightWidget.viewModel.selectedFlightObservable.onNext(PackageSearchType.INBOUND_FLIGHT)
        outBoundPresenter.overviewPresenter.vm.selectedFlightClicked.subscribe(flightOverviewPresenter.flightSummary.outboundFlightWidget.viewModel.flight)
        inboundPresenter.overviewPresenter.vm.selectedFlightClicked.subscribe(flightOverviewPresenter.flightSummary.inboundFlightWidget.viewModel.flight)
        inboundPresenter.overviewPresenter.vm.selectedFlightClicked.subscribe(searchViewModel.inboundFlightSelected)
        outBoundPresenter.overviewPresenter.vm.selectedFlightClicked.subscribe(searchViewModel.outboundFlightSelected)
        vm.outboundResultsObservable.subscribe(outBoundPresenter.resultsPresenter.resultsViewModel.flightResultsObservable)
        vm.inboundResultsObservable.subscribe(inboundPresenter.resultsPresenter.resultsViewModel.flightResultsObservable)
        vm.flightParamsObservable.subscribe { params ->
            outBoundPresenter.toolbarViewModel.city.onNext(params.departureAirport.regionNames.shortName)
            outBoundPresenter.toolbarViewModel.travelers.onNext(params.guests())
            outBoundPresenter.toolbarViewModel.date.onNext(searchParams?.departureDate)
            inboundPresenter.toolbarViewModel.city.onNext(params.arrivalAirport?.regionNames?.shortName)
            inboundPresenter.toolbarViewModel.travelers.onNext(params.guests())
            inboundPresenter.toolbarViewModel.date.onNext(searchParams?.returnDate)

            flightOverviewPresenter.flightSummary.outboundFlightWidget.viewModel.suggestion.onNext(params.departureAirport)
            flightOverviewPresenter.flightSummary.outboundFlightWidget.viewModel.date.onNext(searchParams?.departureDate)
            flightOverviewPresenter.flightSummary.outboundFlightWidget.viewModel.guests.onNext(params.guests())

            flightOverviewPresenter.flightSummary.inboundFlightWidget.viewModel.suggestion.onNext(params.arrivalAirport)
            flightOverviewPresenter.flightSummary.inboundFlightWidget.viewModel.date.onNext(searchParams?.returnDate)
            flightOverviewPresenter.flightSummary.inboundFlightWidget.viewModel.guests.onNext(params.guests())

            flightOverviewPresenter.flightSummary.outboundFlightWidget.viewModel.hotelLoadingStateObservable.onNext(PackageSearchType.OUTBOUND_FLIGHT)
            flightOverviewPresenter.flightSummary.inboundFlightWidget.viewModel.hotelLoadingStateObservable.onNext(PackageSearchType.INBOUND_FLIGHT)

        }
        vm.flightProductId.subscribe { productKey ->
            val requestInsurance = Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.INS_AddInsuranceOnMobileAppFlights)
            val createTripParams = FlightCreateTripParams(productKey, requestInsurance)
            flightOverviewPresenter.getCheckoutPresenter().createTripViewModel.tripParams.onNext(createTripParams)
            show(flightOverviewPresenter)
        }
        vm.inboundResultsObservable.subscribe {
            show(inboundPresenter)
        }
        vm.outboundResultsObservable.subscribe {
            show(outBoundPresenter)
        }
    }

    init {
        Ui.getApplication(getContext()).flightComponent().inject(this)
        View.inflate(context, R.layout.flight_presenter, this)
        flightOverviewPresenter.getCheckoutPresenter().checkoutViewModel = FlightCheckoutViewModel(context, flightServices)
        flightOverviewPresenter.getCheckoutPresenter().createTripViewModel = FlightCreateTripViewModel(flightServices)
        flightOverviewPresenter.getCheckoutPresenter().createTripViewModel.tripResponseObservable.subscribe(flightOverviewPresenter.getCheckoutPresenter().checkoutViewModel.tripResponseObservable)
        searchViewModel = FlightSearchViewModel(context, flightServices)

        val departure = getFakeSuggestion("SFO")
        val arrival = getFakeSuggestion("SEA")
        searchParamsBuilder.departureAirport(departure)
        searchParamsBuilder.arrivalAirport(arrival)
        searchParamsBuilder.departureDate(LocalDate.now().plusDays(7))
        searchParamsBuilder.returnDate(LocalDate.now().plusDays(14))
        searchParamsBuilder.adults(1)
        searchParams = searchParamsBuilder.build()
        searchViewModel.flightParamsObservable.onNext(searchParams)

    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        addTransition(flightsToBundle)
        addTransition(outboundToInbound)
        addDefaultTransition(defaultTransition)
    }

    private fun getFakeSuggestion(airportCode: String): SuggestionV4 {
        val suggestion = SuggestionV4()
        val hierarchyInfo = SuggestionV4.HierarchyInfo()
        val airport = SuggestionV4.Airport()
        airport.airportCode = airportCode
        hierarchyInfo.airport = airport
        suggestion.hierarchyInfo = hierarchyInfo

        val regionName = SuggestionV4.RegionNames()
        regionName.shortName = "San Francisco, CA"
        regionName.displayName = "San Francisco, CA"
        suggestion.regionNames = regionName
        return suggestion
    }

    private val flightsToBundle = object : LeftToRightTransition(this, FlightInboundPresenter::class.java, FlightOverviewPresenter::class.java) {
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            if (forward) {
                flightOverviewPresenter.bundleOverviewHeader.checkoutOverviewHeaderToolbar.visibility = View.GONE
                flightOverviewPresenter.bundleOverviewHeader.toggleOverviewHeader(false)
                flightOverviewPresenter.getCheckoutPresenter().toggleCheckoutButton(false)
                var countryCode = PointOfSale.getPointOfSale().threeLetterCountryCode
                var currencyCode = CurrencyUtils.currencyForLocale(countryCode)
                flightOverviewPresenter.getCheckoutPresenter().totalPriceWidget.visibility = View.VISIBLE
                flightOverviewPresenter.getCheckoutPresenter().totalPriceWidget.viewModel.setTextObservable.onNext(Pair(Money(BigDecimal("0.00"), currencyCode).formattedMoney,
                        Phrase.from(context, R.string.bundle_total_savings_TEMPLATE)
                                .put("savings", Money(BigDecimal("0.00"), currencyCode).formattedMoney)
                                .format().toString()))
            }
        }
    }

    private val outboundToInbound = ScaleTransition(this, FlightOutboundPresenter::class.java, FlightInboundPresenter::class.java)

    private val defaultTransition = object : Presenter.DefaultTransition(FlightOutboundPresenter::class.java.name) {
        override fun endTransition(forward: Boolean) {
            outBoundPresenter.visibility = View.VISIBLE
            inboundPresenter.visibility = View.GONE
            flightOverviewPresenter.visibility = View.GONE
        }
    }

}

