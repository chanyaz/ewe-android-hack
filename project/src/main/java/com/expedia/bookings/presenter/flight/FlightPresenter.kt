package com.expedia.bookings.presenter.flight

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewStub
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightCreateTripParams
import com.expedia.bookings.presenter.LeftToRightTransition
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.ScaleTransition
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.tracking.FlightsV2Tracking
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.FlightCheckoutOverviewViewModel
import com.expedia.vm.FlightSearchViewModel
import com.expedia.vm.packages.PackageSearchType
import javax.inject.Inject

class FlightPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {
    lateinit var flightServices: FlightServices
        @Inject set

    val searchPresenter: FlightSearchPresenter by lazy {
        val viewStub = findViewById(R.id.search_presenter) as ViewStub
        val presenter = viewStub.inflate() as FlightSearchPresenter
        presenter
    }

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

    var searchViewModel: FlightSearchViewModel by notNullAndObservable { vm ->
        searchPresenter.searchViewModel = vm
        outBoundPresenter.flightSearchViewModel = vm
        inboundPresenter.flightSearchViewModel = vm
        flightOverviewPresenter.flightSummary.outboundFlightWidget.viewModel.selectedFlightObservable.onNext(PackageSearchType.OUTBOUND_FLIGHT)
        flightOverviewPresenter.flightSummary.inboundFlightWidget.viewModel.selectedFlightObservable.onNext(PackageSearchType.INBOUND_FLIGHT)

        // TODO - may wanna change this too
        outBoundPresenter.overviewPresenter.vm.selectedFlightClickedSubject.subscribe(flightOverviewPresenter.flightSummary.outboundFlightWidget.viewModel.flight)
        inboundPresenter.overviewPresenter.vm.selectedFlightClickedSubject.subscribe(flightOverviewPresenter.flightSummary.inboundFlightWidget.viewModel.flight)

        inboundPresenter.overviewPresenter.vm.selectedFlightClickedSubject.subscribe(searchViewModel.confirmedInboundFlightSelection)
        outBoundPresenter.overviewPresenter.vm.selectedFlightClickedSubject.subscribe(searchViewModel.confirmedOutboundFlightSelection)

        inboundPresenter.overviewPresenter.vm.selectedFlightLegSubject.subscribe(searchViewModel.inboundSelected)
        outBoundPresenter.overviewPresenter.vm.selectedFlightLegSubject.subscribe(searchViewModel.outboundSelected)

        vm.outboundResultsObservable.subscribe(outBoundPresenter.resultsPresenter.resultsViewModel.flightResultsObservable)
        vm.inboundResultsObservable.subscribe(inboundPresenter.resultsPresenter.resultsViewModel.flightResultsObservable)
        vm.searchParamsObservable.subscribe((flightOverviewPresenter.bundleOverviewHeader.checkoutOverviewFloatingToolbar.viewmodel as FlightCheckoutOverviewViewModel).params)
        vm.searchParamsObservable.subscribe((flightOverviewPresenter.bundleOverviewHeader.checkoutOverviewHeaderToolbar.viewmodel as FlightCheckoutOverviewViewModel).params)
        vm.searchParamsObservable.subscribe { params ->
            outBoundPresenter.toolbarViewModel.city.onNext(params.departureAirport.regionNames.shortName)
            outBoundPresenter.toolbarViewModel.travelers.onNext(params.guests)
            outBoundPresenter.toolbarViewModel.date.onNext(params.departureDate)

            inboundPresenter.toolbarViewModel.city.onNext(params.arrivalAirport?.regionNames?.shortName)
            inboundPresenter.toolbarViewModel.travelers.onNext(params.guests)
            if (params.returnDate != null) {
                inboundPresenter.toolbarViewModel.date.onNext(params.returnDate)
                flightOverviewPresenter.flightSummary.inboundFlightWidget.viewModel.searchTypeStateObservable.onNext(PackageSearchType.INBOUND_FLIGHT)
            }
            flightOverviewPresenter.flightSummary.outboundFlightWidget.viewModel.suggestion.onNext(params.departureAirport)
            flightOverviewPresenter.flightSummary.outboundFlightWidget.viewModel.date.onNext(params.departureDate)
            flightOverviewPresenter.flightSummary.outboundFlightWidget.viewModel.guests.onNext(params.guests)

            flightOverviewPresenter.flightSummary.inboundFlightWidget.viewModel.suggestion.onNext(params.arrivalAirport)
            flightOverviewPresenter.flightSummary.inboundFlightWidget.viewModel.date.onNext(params.returnDate)
            flightOverviewPresenter.flightSummary.inboundFlightWidget.viewModel.guests.onNext(params.guests)

            flightOverviewPresenter.flightSummary.outboundFlightWidget.viewModel.searchTypeStateObservable.onNext(PackageSearchType.OUTBOUND_FLIGHT)

            flightOverviewPresenter.flightSummary.setPadding(0, 0, 0, 0)

            // Starting a new search clear previous selection
            Db.clearPackageFlightSelection()
            outBoundPresenter.resultsPresenter.setLoadingState()
            show(outBoundPresenter)
        }
        vm.flightProductId.subscribe { productKey ->
            val requestInsurance = Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.INS_AddInsuranceOnMobileAppFlights)
            val createTripParams = FlightCreateTripParams(productKey, requestInsurance)
            flightOverviewPresenter.getCheckoutPresenter().getCreateTripViewModel().tripParams.onNext(createTripParams)
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
        searchViewModel = FlightSearchViewModel(context, flightServices)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        addTransition(flightsToBundle)
        addTransition(searchToOutbound)
        addTransition(outboundToInbound)
        addDefaultTransition(defaultTransition)
        flightOverviewPresenter.getCheckoutPresenter().toggleCheckoutButton(false)

        show(searchPresenter)
    }

    private val flightsToBundle = object : LeftToRightTransition(this, FlightInboundPresenter::class.java, FlightOverviewPresenter::class.java) {
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            if (forward) {
                flightOverviewPresenter.bundleOverviewHeader.checkoutOverviewHeaderToolbar.visibility = View.VISIBLE
                flightOverviewPresenter.bundleOverviewHeader.toggleOverviewHeader(true)
                flightOverviewPresenter.getCheckoutPresenter().resetAndShowTotalPriceWidget()
            }
        }
    }

    private val outboundToInbound = ScaleTransition(this, FlightOutboundPresenter::class.java, FlightInboundPresenter::class.java)

    private val searchToOutbound = object : ScaleTransition(this, FlightSearchPresenter::class.java, FlightOutboundPresenter::class.java) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            if (!forward) {
                FlightsV2Tracking.trackSearchPageLoad()
            }
        }
    }

    private val defaultTransition = object : Presenter.DefaultTransition(FlightSearchPresenter::class.java.name) {
        override fun endTransition(forward: Boolean) {
            searchPresenter.visibility = View.VISIBLE
            outBoundPresenter.visibility = View.GONE
            inboundPresenter.visibility = View.GONE
            flightOverviewPresenter.visibility = View.GONE
            FlightsV2Tracking.trackSearchPageLoad()
        }
    }

}
