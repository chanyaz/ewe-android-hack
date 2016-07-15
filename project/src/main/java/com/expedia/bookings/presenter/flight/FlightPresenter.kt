package com.expedia.bookings.presenter.flight

import android.animation.ArgbEvaluator
import android.content.Context
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.view.ViewStub
import android.view.animation.DecelerateInterpolator
import com.expedia.bookings.R
import com.expedia.bookings.animation.TransitionElement
import com.expedia.bookings.data.BaseApiResponse
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightCreateTripParams
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.presenter.BaseOverviewPresenter
import com.expedia.bookings.presenter.LeftToRightTransition
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.ScaleTransition
import com.expedia.bookings.presenter.packages.FlightErrorPresenter
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.tracking.FlightsV2Tracking
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.TravelerManager
import com.expedia.bookings.utils.Ui
import com.expedia.ui.FlightActivity
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeVisibility
import com.expedia.vm.FlightCheckoutOverviewViewModel
import com.expedia.vm.FlightSearchViewModel
import com.expedia.vm.packages.FlightErrorViewModel
import com.expedia.vm.packages.PackageSearchType
import rx.Observable
import javax.inject.Inject

class FlightPresenter(context: Context, attrs: AttributeSet?) : Presenter(context, attrs) {
    lateinit var flightServices: FlightServices
        @Inject set
    lateinit var travelerManager: TravelerManager

    val errorPresenter: FlightErrorPresenter by lazy {
        val viewStub = findViewById(R.id.error_presenter) as ViewStub
        val presenter = viewStub.inflate() as FlightErrorPresenter
        presenter
    }

    val searchPresenter: FlightSearchPresenter by lazy {
        if (displayFlightDropDownRoutes()) {
            val viewStub = findViewById(R.id.search_restricted_airport_dropdown_presenter) as ViewStub
            viewStub.inflate() as FlightSearchAirportDropdownPresenter
        }
        else {
            val viewStub = findViewById(R.id.search_presenter) as ViewStub
            viewStub.inflate() as FlightSearchPresenter
        }
    }

    val outBoundPresenter: FlightOutboundPresenter by lazy {
        val viewStub = findViewById(R.id.outbound_presenter) as ViewStub
        val presenter = viewStub.inflate() as FlightOutboundPresenter
        presenter
    }

    val inboundPresenter: FlightInboundPresenter by lazy {
        val viewStub = findViewById(R.id.inbound_presenter) as ViewStub
        val presenter = viewStub.inflate() as FlightInboundPresenter
        presenter
    }

    val flightOverviewPresenter: FlightOverviewPresenter by lazy {
        val viewStub = findViewById(R.id.overview_presenter) as ViewStub
        val presenter = viewStub.inflate() as FlightOverviewPresenter
        presenter
    }

    val confirmationPresenter: FlightConfirmationPresenter by lazy {
        val viewStub = findViewById(R.id.confirmation_presenter) as ViewStub
        val presenter = viewStub.inflate() as FlightConfirmationPresenter
        presenter
    }

    val e3EndpointProvider = Ui.getApplication(getContext()).appComponent().endpointProvider()

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
        vm.isRoundTripSearchObservable.subscribeVisibility(flightOverviewPresenter.flightSummary.inboundFlightWidget)

        vm.searchParamsObservable.map { it.arrivalAirport }
                .subscribe(flightOverviewPresenter.flightSummary.outboundFlightWidget.viewModel.suggestion)
        vm.searchParamsObservable.map { it.departureDate }
                .subscribe(flightOverviewPresenter.flightSummary.outboundFlightWidget.viewModel.date)
        vm.searchParamsObservable.map { it.guests }
                .subscribe(flightOverviewPresenter.flightSummary.outboundFlightWidget.viewModel.guests)
        vm.searchParamsObservable.filter { vm.isRoundTripSearchObservable.value }
                .map { it.departureAirport }
                .subscribe(flightOverviewPresenter.flightSummary.inboundFlightWidget.viewModel.suggestion)
        vm.searchParamsObservable.filter { vm.isRoundTripSearchObservable.value }
                .map { it.returnDate }
                .subscribe(flightOverviewPresenter.flightSummary.inboundFlightWidget.viewModel.date)
        vm.searchParamsObservable.filter { vm.isRoundTripSearchObservable.value }
                .map { it.guests }
                .subscribe(flightOverviewPresenter.flightSummary.inboundFlightWidget.viewModel.guests)

        vm.searchParamsObservable.subscribe { params ->
            outBoundPresenter.toolbarViewModel.city.onNext(params.arrivalAirport?.regionNames?.shortName)
            outBoundPresenter.toolbarViewModel.travelers.onNext(params.guests)
            outBoundPresenter.toolbarViewModel.date.onNext(params.departureDate)
            errorPresenter.viewmodel.paramsSubject.onNext(params)

            inboundPresenter.toolbarViewModel.city.onNext(params.departureAirport.regionNames.shortName)
            inboundPresenter.toolbarViewModel.travelers.onNext(params.guests)
            if (params.returnDate != null) {
                inboundPresenter.toolbarViewModel.date.onNext(params.returnDate)
                flightOverviewPresenter.flightSummary.inboundFlightWidget.viewModel.searchTypeStateObservable.onNext(PackageSearchType.INBOUND_FLIGHT)
            }

            flightOverviewPresenter.flightSummary.outboundFlightWidget.viewModel.searchTypeStateObservable.onNext(PackageSearchType.OUTBOUND_FLIGHT)

            flightOverviewPresenter.flightSummary.setPadding(0, 0, 0, 0)
            travelerManager.updateDbTravelers(params)
            // Starting a new search clear previous selection
            Db.clearPackageFlightSelection()
            outBoundPresenter.resultsPresenter.setLoadingState()
            show(outBoundPresenter)
        }
        outBoundPresenter.flightSearchViewModel.errorObservable.subscribe(errorPresenter.viewmodel.searchApiErrorObserver)
        outBoundPresenter.flightSearchViewModel.errorObservable.subscribe { show(errorPresenter) }

        errorPresenter.viewmodel.defaultErrorObservable.subscribe {
            show(searchPresenter, FLAG_CLEAR_TOP)
        }

        vm.flightProductId.subscribe { productKey ->
            val requestInsurance = Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppFlightInsurance)
            val createTripParams = FlightCreateTripParams(productKey, requestInsurance)
            flightOverviewPresenter.getCheckoutPresenter().getCreateTripViewModel().tripParams.onNext(createTripParams)
            show(flightOverviewPresenter)
            flightOverviewPresenter.show(BaseOverviewPresenter.BundleDefault(), FLAG_CLEAR_BACKSTACK)

        }
        vm.inboundResultsObservable.subscribe {
            show(inboundPresenter)
        }
        vm.outboundResultsObservable.subscribe {
            show(outBoundPresenter)
        }
        vm.confirmedOutboundFlightSelection.subscribe { flightOverviewPresenter.viewModel.showFreeCancellationObservable.onNext(it.isFreeCancellable) }
        vm.flightOfferSelected.subscribe { flightOverviewPresenter.viewModel.showSplitTicketMessagingObservable.onNext(it.isSplitTicket) }
        Observable.combineLatest(vm.confirmedOutboundFlightSelection, vm.confirmedInboundFlightSelection, { outbound, inbound ->
            val outboundBaggageFeeUrl = e3EndpointProvider.getE3EndpointUrlWithPath(outbound.baggageFeesUrl)
            val inboundBaggageFeeUrl = e3EndpointProvider.getE3EndpointUrlWithPath(inbound.baggageFeesUrl)
            val baggageFeesTextWithLinks = resources.getString(R.string.split_ticket_baggage_fees, outboundBaggageFeeUrl, inboundBaggageFeeUrl)
            val spannableStringBuilder = StrUtils.getSpannableTextByColor(baggageFeesTextWithLinks, Color.BLACK, true)
            flightOverviewPresenter.viewModel.splitTicketBaggageFeesLinksObservable.onNext(spannableStringBuilder)
        }).subscribe()

        val checkoutViewModel = flightOverviewPresenter.getCheckoutPresenter().getCheckoutViewModel()
        vm.offerSelectedChargesObFeesSubject.subscribe(checkoutViewModel.selectedFlightChargesFees)
        vm.obFeeDetailsUrlObservable.subscribe(checkoutViewModel.obFeeDetailsUrlSubject)
    }

    init {
        travelerManager = Ui.getApplication(getContext()).travelerComponent().travelerManager()
        Ui.getApplication(getContext()).flightComponent().inject(this)
        View.inflate(context, R.layout.flight_presenter, this)

        errorPresenter.viewmodel = FlightErrorViewModel(context)
        searchViewModel = FlightSearchViewModel(context, flightServices)
        searchViewModel.deeplinkDefaultTransitionObservable.subscribe { screen ->
            setDefaultTransition(screen)
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        addTransition(if (displayFlightDropDownRoutes()) restrictedSearchToOutbound else searchToOutbound)
        addTransition(inboundFlightToOverview)
        addTransition(outboundToInbound)
        addTransition(overviewToConfirmation)
        addTransition(outboundFlightToOverview)
        addTransition(outboundToError)
        addTransition(errorToSearch)
        flightOverviewPresenter.getCheckoutPresenter().toggleCheckoutButton(false)

        flightOverviewPresenter.getCheckoutPresenter().getCheckoutViewModel().bookingSuccessResponse.subscribe { pair: Pair<BaseApiResponse, String> ->
            show(confirmationPresenter)
            FlightsV2Tracking.trackCheckoutConfirmationPageLoad()
        }

    }

    private val outboundToError = object : Presenter.Transition(FlightOutboundPresenter::class.java, FlightErrorPresenter::class.java, DecelerateInterpolator(), 200) {
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            errorPresenter.visibility = View.VISIBLE
            outBoundPresenter.visibility = View.GONE
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            super.updateTransition(f, forward)
            errorPresenter.animationUpdate(f, !forward)
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            errorPresenter.visibility = if (forward) View.VISIBLE else View.GONE
            errorPresenter.animationFinalize()
        }
    }

    val searchArgbEvaluator = ArgbEvaluator()
    val searchBackgroundColor = TransitionElement(ContextCompat.getColor(context, R.color.search_anim_background), Color.TRANSPARENT)

    private val errorToSearch = object : Presenter.Transition(FlightErrorPresenter::class.java, FlightSearchPresenter::class.java, DecelerateInterpolator(), 200) {
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            searchPresenter.visibility = View.VISIBLE
            searchPresenter.animationStart(forward)
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            super.updateTransition(f, forward)
            searchPresenter.animationUpdate(f, forward)
            if (forward) {
                searchPresenter.setBackgroundColor(searchArgbEvaluator.evaluate(f, searchBackgroundColor.start, searchBackgroundColor.end) as Int)
            } else {
                searchPresenter.setBackgroundColor(searchArgbEvaluator.evaluate(f, searchBackgroundColor.end, searchBackgroundColor.start) as Int)
            }
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            searchPresenter.setBackgroundColor(if (!forward) searchBackgroundColor.end else searchBackgroundColor.start)
            searchPresenter.animationFinalize(!forward)
            errorPresenter.visibility = if (forward) View.GONE else View.VISIBLE
            searchPresenter.visibility = if (forward) View.VISIBLE else View.GONE

        }
    }

    private val inboundFlightToOverview = object : LeftToRightTransition(this, FlightInboundPresenter::class.java, FlightOverviewPresenter::class.java) {
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            if (forward) {
                flightOverviewPresenter.bundleOverviewHeader.checkoutOverviewHeaderToolbar.visibility = View.VISIBLE
                flightOverviewPresenter.bundleOverviewHeader.toggleOverviewHeader(true)
                flightOverviewPresenter.getCheckoutPresenter().resetAndShowTotalPriceWidget()
            }
        }
    }

    private val outboundFlightToOverview = object : LeftToRightTransition(this, FlightOutboundPresenter::class.java, FlightOverviewPresenter::class.java) {
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            if (forward) {
                flightOverviewPresenter.bundleOverviewHeader.checkoutOverviewHeaderToolbar.visibility = View.VISIBLE
                flightOverviewPresenter.bundleOverviewHeader.toggleOverviewHeader(true)
                flightOverviewPresenter.getCheckoutPresenter().resetAndShowTotalPriceWidget()
            }
        }
    }

    private val overviewToConfirmation = object : LeftToRightTransition(this, FlightOverviewPresenter::class.java, FlightConfirmationPresenter::class.java) {}

    private val outboundToInbound = object: ScaleTransition(this, FlightOutboundPresenter::class.java, FlightInboundPresenter::class.java) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            if (!forward) {
                searchViewModel.resetFlightSelections()
            }
        }
    }

    private val defaultOutboundTransition = object : Presenter.DefaultTransition(FlightOutboundPresenter::class.java.name){
        override fun endTransition(forward: Boolean) {
            searchPresenter.visibility = View.GONE
            outBoundPresenter.visibility = View.VISIBLE
            inboundPresenter.visibility = View.GONE
            flightOverviewPresenter.visibility = View.GONE
        }
    }

    private val searchToOutbound = object : SearchToOutboundTransition(this, FlightSearchPresenter::class.java, FlightOutboundPresenter::class.java) {}
    private val restrictedSearchToOutbound = object : SearchToOutboundTransition(this, FlightSearchAirportDropdownPresenter::class.java, FlightOutboundPresenter::class.java) {}

    private val defaultSearchTransition = object : Presenter.DefaultTransition(getDefaultSearchPresenterClassName()) {
        override fun endTransition(forward: Boolean) {
            searchPresenter.visibility = View.VISIBLE
            outBoundPresenter.visibility = View.GONE
            inboundPresenter.visibility = View.GONE
            flightOverviewPresenter.visibility = View.GONE
            FlightsV2Tracking.trackSearchPageLoad()
        }
    }

    private open class SearchToOutboundTransition(presenter: Presenter, left: Class<*>, right: Class<*>) : ScaleTransition(presenter, left, right) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            if (!forward) {
                FlightsV2Tracking.trackSearchPageLoad()
            }
        }
    }

    private fun displayFlightDropDownRoutes(): Boolean {
        return PointOfSale.getPointOfSale().displayFlightDropDownRoutes()
    }

    private fun getDefaultSearchPresenterClassName(): String {
        return if (displayFlightDropDownRoutes()) {
            FlightSearchAirportDropdownPresenter::class.java.name
        }
        else {
            FlightSearchPresenter::class.java.name
        }
    }

    fun setDefaultTransition(screen: FlightActivity.Screen) {
        val defaultTransition = when (screen) {
            FlightActivity.Screen.RESULTS -> defaultOutboundTransition
            else -> defaultSearchTransition
        }
        if (!hasDefaultTransition()) {
            addDefaultTransition(defaultTransition)
        }
        if (screen == FlightActivity.Screen.SEARCH) {
            show(searchPresenter)
        }
    }
}
