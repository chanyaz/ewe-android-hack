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
import com.expedia.bookings.data.flights.FlightCheckoutResponse
import com.expedia.bookings.data.flights.FlightCreateTripParams
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.presenter.BaseOverviewPresenter
import com.expedia.bookings.presenter.LeftToRightTransition
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.ScaleTransition
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
import com.expedia.vm.flights.FlightConfirmationCardViewModel
import com.expedia.vm.flights.FlightConfirmationViewModel
import com.expedia.vm.flights.FlightErrorViewModel
import com.expedia.vm.packages.PackageSearchType
import com.squareup.phrase.Phrase
import rx.Observable
import javax.inject.Inject

class FlightPresenter(context: Context, attrs: AttributeSet?) : Presenter(context, attrs) {
    lateinit var flightServices: FlightServices
        @Inject set
    lateinit var travelerManager: TravelerManager

    val errorPresenter: FlightErrorPresenter by lazy {
        val viewStub = findViewById(R.id.error_presenter_stub) as ViewStub
        val presenter = viewStub.inflate() as FlightErrorPresenter
        presenter.viewmodel = FlightErrorViewModel(context)
        presenter.getViewModel().defaultErrorObservable.subscribe { show(searchPresenter, Presenter.FLAG_CLEAR_TOP) }
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
        presenter.flightSearchViewModel = searchViewModel
        searchViewModel.outboundResultsObservable.subscribe(presenter.resultsPresenter.resultsViewModel.flightResultsObservable)
        presenter.flightSearchViewModel.errorObservable.subscribe {
            errorPresenter.viewmodel.searchApiErrorObserver.onNext(it)
            show(errorPresenter)
        }
        presenter.overviewPresenter.vm.selectedFlightClickedSubject.subscribe(searchViewModel.confirmedOutboundFlightSelection)
        presenter.overviewPresenter.vm.selectedFlightLegSubject.subscribe(searchViewModel.outboundSelected)
        presenter
    }

    val inboundPresenter: FlightInboundPresenter by lazy {
        val viewStub = findViewById(R.id.inbound_presenter) as ViewStub
        val presenter = viewStub.inflate() as FlightInboundPresenter
        presenter.flightSearchViewModel = searchViewModel
        presenter.overviewPresenter.vm.selectedFlightClickedSubject.subscribe(searchViewModel.confirmedInboundFlightSelection)
        presenter.overviewPresenter.vm.selectedFlightLegSubject.subscribe(searchViewModel.inboundSelected)
        searchViewModel.inboundResultsObservable.subscribe(presenter.resultsPresenter.resultsViewModel.flightResultsObservable)
        searchViewModel.searchParamsObservable.subscribe { params ->
            presenter.toolbarViewModel.city.onNext(params.departureAirport.regionNames.shortName)
            presenter.toolbarViewModel.travelers.onNext(params.guests)
            if (params.returnDate != null) {
                presenter.toolbarViewModel.date.onNext(params.returnDate)
            }
        }
        presenter
    }

    val flightOverviewPresenter: FlightOverviewPresenter by lazy {
        val viewStub = findViewById(R.id.overview_presenter) as ViewStub
        val presenter = viewStub.inflate() as FlightOverviewPresenter
        presenter.flightSummary.outboundFlightWidget.viewModel.selectedFlightObservable.onNext(PackageSearchType.OUTBOUND_FLIGHT)
        presenter.flightSummary.inboundFlightWidget.viewModel.selectedFlightObservable.onNext(PackageSearchType.INBOUND_FLIGHT)
        searchViewModel.searchParamsObservable.subscribe((presenter.bundleOverviewHeader.checkoutOverviewFloatingToolbar.viewmodel as FlightCheckoutOverviewViewModel).params)
        searchViewModel.searchParamsObservable.subscribe((presenter.bundleOverviewHeader.checkoutOverviewHeaderToolbar.viewmodel as FlightCheckoutOverviewViewModel).params)
        searchViewModel.isRoundTripSearchObservable.subscribeVisibility(presenter.flightSummary.inboundFlightWidget)

        searchViewModel.searchParamsObservable.subscribe { params ->
            if (params.returnDate != null) {
                presenter.flightSummary.inboundFlightWidget.viewModel.searchTypeStateObservable.onNext(PackageSearchType.INBOUND_FLIGHT)
                Observable.combineLatest(searchViewModel.confirmedOutboundFlightSelection, searchViewModel.confirmedInboundFlightSelection, { outbound, inbound ->
                    val outboundBaggageFeeUrl = e3EndpointProvider.getE3EndpointUrlWithPath(outbound.baggageFeesUrl)
                    val inboundBaggageFeeUrl = e3EndpointProvider.getE3EndpointUrlWithPath(inbound.baggageFeesUrl)
                    val baggageFeesTextWithLinks = Phrase.from(context, R.string.split_ticket_baggage_fees_TEMPLATE)
                                                    .put("departurelink", outboundBaggageFeeUrl)
                                                    .put("returnlink", inboundBaggageFeeUrl).toString()
                    val spannableStringBuilder = StrUtils.getSpannableTextByColor(baggageFeesTextWithLinks, ContextCompat.getColor(context, R.color.flight_primary_color), true)
                    val destinationCity = outbound.segments?.last()?.arrivalAirportAddress?.city
                    confirmationPresenter.viewModel.destinationObservable.onNext(destinationCity)
                    presenter.viewModel.splitTicketBaggageFeesLinksObservable.onNext(spannableStringBuilder)
                    confirmationPresenter.outboundFlightCard.viewModel = FlightConfirmationCardViewModel(context, outbound, params.guests)
                    confirmationPresenter.inboundFlightCard.viewModel = FlightConfirmationCardViewModel(context, inbound, params.guests)
                }).subscribe()
            } else {
                searchViewModel.confirmedOutboundFlightSelection.subscribe { outbound ->
                    val destinationCity = outbound.segments?.last()?.arrivalAirportAddress?.city
                    confirmationPresenter.outboundFlightCard.viewModel = FlightConfirmationCardViewModel(context, outbound, params.guests)
                    confirmationPresenter.viewModel.destinationObservable.onNext(destinationCity)
                    confirmationPresenter.viewModel.inboundCardVisibility.onNext(false)
                }
            }

            errorPresenter.getViewModel().fireRetryCreateTrip.subscribe {
                presenter.getCheckoutPresenter().getCreateTripViewModel().performCreateTrip.onNext(Unit)
                show(presenter)
                presenter.show(BaseOverviewPresenter.BundleDefault(), FLAG_CLEAR_BACKSTACK)
            }

            presenter.flightSummary.outboundFlightWidget.viewModel.searchParams.onNext(params)
            presenter.flightSummary.inboundFlightWidget.viewModel.searchParams.onNext(params)

            presenter.flightSummary.outboundFlightWidget.viewModel.searchTypeStateObservable.onNext(PackageSearchType.OUTBOUND_FLIGHT)

            presenter.flightSummary.setPadding(0, 0, 0, 0)
        }

        inboundPresenter.overviewPresenter.vm.selectedFlightClickedSubject.subscribe(presenter.flightSummary.inboundFlightWidget.viewModel.flight)
        outBoundPresenter.overviewPresenter.vm.selectedFlightClickedSubject.subscribe(presenter.flightSummary.outboundFlightWidget.viewModel.flight)

        searchViewModel.searchParamsObservable.map { it.arrivalAirport }
                .subscribe(presenter.flightSummary.outboundFlightWidget.viewModel.suggestion)
        searchViewModel.searchParamsObservable.map { it.departureDate }
                .subscribe(presenter.flightSummary.outboundFlightWidget.viewModel.date)
        searchViewModel.searchParamsObservable.map { it.guests }
                .subscribe(presenter.flightSummary.outboundFlightWidget.viewModel.guests)
        searchViewModel.searchParamsObservable.filter { searchViewModel.isRoundTripSearchObservable.value }
                .map { it.departureAirport }
                .subscribe(presenter.flightSummary.inboundFlightWidget.viewModel.suggestion)
        searchViewModel.searchParamsObservable.filter { searchViewModel.isRoundTripSearchObservable.value }
                .map { it.returnDate }
                .subscribe(presenter.flightSummary.inboundFlightWidget.viewModel.date)
        searchViewModel.searchParamsObservable.filter { searchViewModel.isRoundTripSearchObservable.value }
                .map { it.guests }
                .subscribe(presenter.flightSummary.inboundFlightWidget.viewModel.guests)
        val checkoutViewModel = presenter.getCheckoutPresenter().getCheckoutViewModel()
        searchViewModel.offerSelectedChargesObFeesSubject.subscribe(checkoutViewModel.selectedFlightChargesFees)
        searchViewModel.obFeeDetailsUrlObservable.subscribe(checkoutViewModel.obFeeDetailsUrlSubject)

        presenter.getCheckoutPresenter().toggleCheckoutButton(false)

        presenter.getCheckoutPresenter().getCheckoutViewModel().bookingSuccessResponse.subscribe { pair: Pair<BaseApiResponse, String> ->
            show(confirmationPresenter)
            FlightsV2Tracking.trackCheckoutConfirmationPageLoad()
        }

        searchViewModel.confirmedOutboundFlightSelection.subscribe { presenter.viewModel.showFreeCancellationObservable.onNext(it.isFreeCancellable) }
        searchViewModel.flightOfferSelected.subscribe { presenter.viewModel.showSplitTicketMessagingObservable.onNext(it.isSplitTicket) }

        presenter.getCheckoutPresenter().toggleCheckoutButton(false)


        presenter.getCheckoutPresenter().getCheckoutViewModel().tripResponseObservable.subscribe { trip ->
            val expediaRewards = trip.rewards?.totalPointsToEarn?.toString()
            confirmationPresenter.viewModel.rewardPointsObservable.onNext(expediaRewards)
        }
        presenter.getCheckoutPresenter().getCheckoutViewModel().bookingSuccessResponse.subscribe { pair: Pair<BaseApiResponse, String> ->
            val flightCheckoutResponse = pair.first as FlightCheckoutResponse
            val userEmail = pair.second
            confirmationPresenter.showConfirmationInfo(flightCheckoutResponse, userEmail)

            show(confirmationPresenter)
            FlightsV2Tracking.trackCheckoutConfirmationPageLoad()
        }
        presenter.getCheckoutPresenter().getCreateTripViewModel().createTripErrorObservable.subscribe(errorPresenter.viewmodel.createTripErrorObserverable)
        presenter.getCheckoutPresenter().getCheckoutViewModel().checkoutErrorObservable.subscribe(errorPresenter.viewmodel.checkoutApiErrorObserver)
        presenter.getCheckoutPresenter().getCreateTripViewModel().createTripErrorObservable.subscribe { show(errorPresenter) }
        presenter
    }

    val confirmationPresenter: FlightConfirmationPresenter by lazy {
        val viewStub = findViewById(R.id.confirmation_presenter) as ViewStub
        val presenter = viewStub.inflate() as FlightConfirmationPresenter
        presenter.viewModel = FlightConfirmationViewModel(context)
        presenter
    }

    val e3EndpointProvider = Ui.getApplication(getContext()).appComponent().endpointProvider()

    var searchViewModel: FlightSearchViewModel by notNullAndObservable { vm ->
        searchPresenter.searchViewModel = vm
        vm.searchParamsObservable.subscribe { params ->
            outBoundPresenter.toolbarViewModel.city.onNext(params.arrivalAirport?.regionNames?.shortName)
            outBoundPresenter.toolbarViewModel.travelers.onNext(params.guests)
            outBoundPresenter.toolbarViewModel.date.onNext(params.departureDate)
            errorPresenter.getViewModel().paramsSubject.onNext(params)
            travelerManager.updateDbTravelers(params, context)
            // Starting a new search clear previous selection
            Db.clearPackageFlightSelection()
            outBoundPresenter.resultsPresenter.setLoadingState()
            show(outBoundPresenter)
        }

        vm.flightProductId.subscribe { productKey ->
            val requestInsurance = Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppFlightInsurance)
            val createTripParams = FlightCreateTripParams(productKey, requestInsurance)
            flightOverviewPresenter.getCheckoutPresenter().getCreateTripViewModel().tripParams.onNext(createTripParams)
            show(flightOverviewPresenter)
            flightOverviewPresenter.show(BaseOverviewPresenter.BundleDefault(), FLAG_CLEAR_BACKSTACK)

        }
        vm.inboundResultsObservable.subscribe {
            inboundPresenter.trackFlightResultsLoad()
            show(inboundPresenter)
        }
        vm.outboundResultsObservable.subscribe {
            outBoundPresenter.trackFlightResultsLoad()
            show(outBoundPresenter)
        }
    }

    init {
        travelerManager = Ui.getApplication(getContext()).travelerComponent().travelerManager()
        Ui.getApplication(getContext()).flightComponent().inject(this)
        View.inflate(context, R.layout.flight_presenter, this)
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
        addTransition(flightOverviewToError)
        addTransition(errorToSearch)
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
            if (forward) {
                FlightsV2Tracking.trackSearchPageLoad()
            }

        }
    }

    private val ANIMATION_DURATION = 400
    private val flightOverviewToError = object : Presenter.Transition(FlightOverviewPresenter::class.java, FlightErrorPresenter::class.java, DecelerateInterpolator(), ANIMATION_DURATION) {
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            flightOverviewPresenter.getCheckoutPresenter().checkoutDialog.hide()
            flightOverviewPresenter.getCheckoutPresenter().createTripDialog.hide()
            errorPresenter.visibility = View.VISIBLE
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            super.updateTransition(f, forward)
            errorPresenter.animationUpdate(f, !forward)
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            flightOverviewPresenter.visibility = if (forward) View.GONE else View.VISIBLE
            errorPresenter.visibility = if (forward) View.VISIBLE else View.GONE
            errorPresenter.animationFinalize()
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
            outBoundPresenter.visibility = if (!forward) View.VISIBLE else View.GONE
            errorPresenter.animationFinalize()
            if (!forward) {
                outBoundPresenter.showResults()
            }
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
        }
    }

    private val searchToOutbound = object : SearchToOutboundTransition(this, FlightSearchPresenter::class.java, FlightOutboundPresenter::class.java) {}
    private val restrictedSearchToOutbound = object : SearchToOutboundTransition(this, FlightSearchAirportDropdownPresenter::class.java, FlightOutboundPresenter::class.java) {}

    private val defaultSearchTransition = object : Presenter.DefaultTransition(getDefaultSearchPresenterClassName()) {
        override fun endTransition(forward: Boolean) {
            searchPresenter.visibility = View.VISIBLE
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
