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
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.BaseApiResponse
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightCheckoutResponse
import com.expedia.bookings.data.flights.FlightCreateTripParams
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.presenter.BaseTwoScreenOverviewPresenter
import com.expedia.bookings.presenter.LeftToRightTransition
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.ScaleTransition
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.tracking.flight.FlightSearchTrackingDataBuilder
import com.expedia.bookings.tracking.flight.FlightsV2Tracking
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.TravelerManager
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.flights.FlightListAdapter
import com.expedia.ui.FlightActivity
import com.expedia.util.notNullAndObservable
import com.expedia.util.safeSubscribe
import com.expedia.util.subscribeVisibility
import com.expedia.vm.FlightCheckoutOverviewViewModel
import com.expedia.vm.FlightSearchViewModel
import com.expedia.vm.flights.BaseFlightOffersViewModel
import com.expedia.vm.flights.FlightConfirmationCardViewModel
import com.expedia.vm.flights.FlightConfirmationViewModel
import com.expedia.vm.flights.FlightCreateTripViewModel
import com.expedia.vm.flights.FlightErrorViewModel
import com.expedia.vm.flights.FlightOffersViewModel
import com.expedia.vm.flights.FlightOffersViewModelByot
import com.expedia.vm.packages.PackageSearchType
import com.squareup.phrase.Phrase
import rx.Observable
import javax.inject.Inject

class FlightPresenter(context: Context, attrs: AttributeSet?) : Presenter(context, attrs) {

    lateinit var flightServices: FlightServices
        @Inject set

    lateinit var flightCreateTripViewModel: FlightCreateTripViewModel
        @Inject set

    lateinit var searchTrackingBuilder: FlightSearchTrackingDataBuilder
        @Inject set

    lateinit var travelerManager: TravelerManager

    val isByotEnabled = Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppFlightByotSearch)

    val errorPresenter: FlightErrorPresenter by lazy {
        val viewStub = findViewById(R.id.error_presenter_stub) as ViewStub
        val presenter = viewStub.inflate() as FlightErrorPresenter
        presenter.viewmodel = FlightErrorViewModel(context)
        presenter.getViewModel().defaultErrorObservable.subscribe {
            show(searchPresenter, Presenter.FLAG_CLEAR_BACKSTACK)
        }
        presenter.getViewModel().showOutboundResults.subscribe {
            show(outBoundPresenter)
        }
        presenter.getViewModel().fireRetryCreateTrip.subscribe {
            flightOverviewPresenter.getCheckoutPresenter().getCreateTripViewModel().performCreateTrip.onNext(Unit)
            show(presenter)
            presenter.show(BaseTwoScreenOverviewPresenter.BundleDefault(), FLAG_CLEAR_BACKSTACK)
        }
        presenter.getViewModel().checkoutUnknownErrorObservable.subscribe {
            flightOverviewPresenter.showCheckout()
        }
        presenter.getViewModel().retryCheckout.subscribe {
            show(presenter)
            flightOverviewPresenter.showCheckout()
            val params = flightOverviewPresenter.getCheckoutPresenter().getCheckoutViewModel().checkoutParams.value
            flightOverviewPresenter.getCheckoutPresenter().getCheckoutViewModel().checkoutParams.onNext(params)
        }
        presenter.getViewModel().showTravelerForm.subscribe {
            show(flightOverviewPresenter, Presenter.FLAG_CLEAR_TOP)
            flightOverviewPresenter.showCheckout()
            flightOverviewPresenter.getCheckoutPresenter().openTravelerPresenter()
        }
        presenter.getViewModel().showPaymentForm.subscribe {
            show(flightOverviewPresenter, Presenter.FLAG_CLEAR_TOP)
            flightOverviewPresenter.showCheckout()
            flightOverviewPresenter.getCheckoutPresenter().paymentWidget.showPaymentForm(fromPaymentError = true)
        }
        presenter.getViewModel().showConfirmation.subscribe {
            show(confirmationPresenter, Presenter.FLAG_CLEAR_BACKSTACK)
        }
        presenter.getViewModel().showSearch.subscribe {
            show(searchPresenter, Presenter.FLAG_CLEAR_BACKSTACK)
        }

        presenter.getViewModel().retrySearch.subscribe {
            searchPresenter.searchViewModel.performSearchObserver.onNext(Unit)
        }

        presenter
    }

    val searchPresenter: FlightSearchPresenter by lazy {
        if (displayFlightDropDownRoutes()) {
            val viewStub = findViewById(R.id.search_restricted_airport_dropdown_presenter) as ViewStub
            viewStub.inflate() as FlightSearchAirportDropdownPresenter
        } else {
            val viewStub = findViewById(R.id.search_presenter) as ViewStub
            viewStub.inflate() as FlightSearchPresenter
        }
    }

    val outBoundPresenter: FlightOutboundPresenter by lazy {
        val viewStub = findViewById(R.id.outbound_presenter) as ViewStub
        val presenter = viewStub.inflate() as FlightOutboundPresenter
        presenter.flightOfferViewModel = flightOfferViewModel
        searchViewModel.searchParamsObservable.subscribe { params ->
            presenter.toolbarViewModel.city.onNext(HtmlCompat.stripHtml(params.arrivalAirport.regionNames.displayName))
            presenter.toolbarViewModel.travelers.onNext(params.guests)
            presenter.toolbarViewModel.date.onNext(params.departureDate)
            searchTrackingBuilder.searchParams(params)
        }
        presenter.flightOfferViewModel.outboundResultsObservable.subscribe {
            searchTrackingBuilder.markResultsProcessed()
            searchTrackingBuilder.searchResponse(it)
        }
        presenter.menuSearch.setOnMenuItemClickListener({
            show(searchPresenter)
            true
        })
        presenter.setupComplete()
        (presenter.resultsPresenter.recyclerView.adapter as FlightListAdapter).allViewsLoadedTimeObservable.subscribe {
            searchTrackingBuilder.markResultsUsable()
            if (searchTrackingBuilder.isWorkComplete()) {
                val trackingData = searchTrackingBuilder.build()
                FlightsV2Tracking.trackResultOutBoundFlights(trackingData)
            }
        }
        if (isByotEnabled) {
            presenter.overviewPresenter.vm.selectedFlightClickedSubject.subscribe {
                searchTrackingBuilder.markSearchClicked()
                searchTrackingBuilder.searchParams(Db.getFlightSearchParams())
            }
        }
        presenter
    }

    val inboundPresenter: FlightInboundPresenter by lazy {
        val viewStub = findViewById(R.id.inbound_presenter) as ViewStub
        val presenter = viewStub.inflate() as FlightInboundPresenter
        presenter.flightOfferViewModel = flightOfferViewModel
        searchViewModel.searchParamsObservable.subscribe { params ->
            presenter.toolbarViewModel.city.onNext(HtmlCompat.stripHtml(params.departureAirport.regionNames.displayName))
            presenter.toolbarViewModel.travelers.onNext(params.guests)
            if (params.returnDate != null) {
                presenter.toolbarViewModel.date.onNext(params.returnDate)
            }
        }
        presenter.menuSearch.setOnMenuItemClickListener({
            show(searchPresenter)
            true
        })
        presenter.setupComplete()
        if (isByotEnabled) {
            presenter.flightOfferViewModel.inboundResultsObservable.subscribe {
                searchTrackingBuilder.markResultsProcessed()
                searchTrackingBuilder.searchResponse(it)
            }

            (presenter.resultsPresenter.recyclerView.adapter as FlightListAdapter).allViewsLoadedTimeObservable.subscribe {
                searchTrackingBuilder.markResultsUsable()
                if (searchTrackingBuilder.isWorkComplete()) {
                    val trackingData = searchTrackingBuilder.build()
                    FlightsV2Tracking.trackResultInBoundFlights(trackingData)
                }
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
            }
            presenter.flightSummary.outboundFlightWidget.viewModel.searchParams.onNext(params)
            presenter.flightSummary.inboundFlightWidget.viewModel.searchParams.onNext(params)
            presenter.flightSummary.outboundFlightWidget.viewModel.searchTypeStateObservable.onNext(PackageSearchType.OUTBOUND_FLIGHT)
            presenter.flightSummary.setPadding(0, 0, 0, 0)
        }

        Observable.combineLatest(flightOfferViewModel.confirmedOutboundFlightSelection,
                flightOfferViewModel.confirmedInboundFlightSelection,
                { outbound, inbound ->
                    val baggageFeesTextFormatted = Phrase.from(context, R.string.split_ticket_baggage_fees_TEMPLATE)
                            .put("departurelink", outbound.baggageFeesUrl)
                            .put("returnlink", inbound.baggageFeesUrl).format().toString()
                    val baggageFeesTextWithColoredClickableLinks =
                            StrUtils.getSpannableTextByColor(baggageFeesTextFormatted,
                                    ContextCompat.getColor(context, R.color.flight_primary_color), true)
                    presenter.viewModel.splitTicketBaggageFeesLinksObservable.onNext(baggageFeesTextWithColoredClickableLinks)
                }).subscribe()

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

        flightOfferViewModel.offerSelectedChargesObFeesSubject.subscribe(checkoutViewModel.selectedFlightChargesFees)
        flightOfferViewModel.obFeeDetailsUrlObservable.subscribe(checkoutViewModel.obFeeDetailsUrlSubject)
        flightOfferViewModel.confirmedOutboundFlightSelection.subscribe { presenter.viewModel.showFreeCancellationObservable.onNext(it.isFreeCancellable) }
        flightOfferViewModel.flightOfferSelected.subscribe { presenter.viewModel.showSplitTicketMessagingObservable.onNext(it.isSplitTicket) }

        presenter.toggleCheckoutButtonAndSliderVisibility(false)

        if (PointOfSale.getPointOfSale().shouldShowAirlinePaymentMethodFeeMessage()) {
            presenter.viewModel.showAirlineFeeWarningObservable.onNext(true)
            val resId = if (PointOfSale.getPointOfSale().airlineMayChargePaymentMethodFee()) {
                R.string.airline_maybe_fee_notice
            } else {
                R.string.airline_fee_notice
            }
            val message = context.getString(resId)
            presenter.viewModel.airlineFeeWarningTextObservable.onNext(message)
        } else {
            presenter.viewModel.showAirlineFeeWarningObservable.onNext(false)
        }

        checkoutViewModel.bookingSuccessResponse.subscribe { pair: Pair<BaseApiResponse, String> ->
            val flightCheckoutResponse = pair.first as FlightCheckoutResponse
            val userEmail = pair.second

            confirmationPresenter.showConfirmationInfo(flightCheckoutResponse, userEmail)
            show(confirmationPresenter)
            FlightsV2Tracking.trackCheckoutConfirmationPageLoad(flightCheckoutResponse)
        }
        val createTripViewModel = presenter.getCheckoutPresenter().getCreateTripViewModel()
        createTripViewModel.createTripResponseObservable.safeSubscribe { trip ->
            trip!!
            val expediaRewards = trip.rewards?.totalPointsToEarn?.toString()
            confirmationPresenter.viewModel.setRewardsPoints.onNext(expediaRewards)
        }
        createTripViewModel.createTripErrorObservable.subscribe(errorPresenter.viewmodel.createTripErrorObserverable)
        createTripViewModel.createTripErrorObservable.subscribe { show(errorPresenter) }
        createTripViewModel.noNetworkObservable.subscribe {
            show(inboundPresenter, FLAG_CLEAR_TOP)
        }
        checkoutViewModel.checkoutErrorObservable.subscribe(errorPresenter.viewmodel.checkoutApiErrorObserver)
        checkoutViewModel.checkoutErrorObservable
                .filter { it.errorCode != ApiError.Code.TRIP_ALREADY_BOOKED }
                .subscribe { show(errorPresenter) }
        presenter
    }

    val confirmationPresenter: FlightConfirmationPresenter by lazy {
        val viewStub = findViewById(R.id.confirmation_presenter) as ViewStub
        val presenter = viewStub.inflate() as FlightConfirmationPresenter
        presenter.viewModel = FlightConfirmationViewModel(context)

        flightOfferViewModel.confirmedInboundFlightSelection.subscribe { inbound ->
            val numberOfGuests = searchViewModel.searchParamsObservable.value.guests
            presenter.inboundFlightCard.viewModel = FlightConfirmationCardViewModel(context, inbound, numberOfGuests)
        }
        flightOfferViewModel.confirmedOutboundFlightSelection.subscribe { outbound ->
            val destinationCity = outbound.segments?.last()?.arrivalAirportAddress?.city
            val numberOfGuests = searchViewModel.searchParamsObservable.value.guests
            presenter.outboundFlightCard.viewModel = FlightConfirmationCardViewModel(context, outbound, numberOfGuests)
            presenter.viewModel.destinationObservable.onNext(destinationCity)
        }
        searchViewModel.searchParamsObservable.subscribe(presenter.hotelCrossSell.viewModel.searchParamsObservable)
        searchViewModel.isRoundTripSearchObservable.subscribe(presenter.viewModel.inboundCardVisibility)
        presenter
    }

    val flightOfferViewModel: BaseFlightOffersViewModel by lazy {
        val viewModel: BaseFlightOffersViewModel
        if (isByotEnabled) {
            viewModel = FlightOffersViewModelByot(context, flightServices)
        } else {
            viewModel = FlightOffersViewModel(context, flightServices)
        }

        viewModel.flightProductId.subscribe { productKey ->
            val createTripParams = FlightCreateTripParams(productKey)
            flightCreateTripViewModel.tripParams.onNext(createTripParams)
            show(flightOverviewPresenter)
            flightOverviewPresenter.show(BaseTwoScreenOverviewPresenter.BundleDefault(), FLAG_CLEAR_BACKSTACK)
        }
        viewModel.outboundResultsObservable.subscribe {
            announceForAccessibility(Phrase.from(context, R.string.accessibility_announcement_showing_outbound_flights_TEMPLATE)
                    .put("city", StrUtils.formatCity(viewModel.searchParamsObservable.value.arrivalAirport))
                    .format().toString())
        }
        viewModel.confirmedOutboundFlightSelection.subscribe {
            if (isByotEnabled && viewModel.isRoundTripSearchSubject.value) {
                inboundPresenter.showResults()
                showInboundPresenter(viewModel.searchParamsObservable.value.departureAirport)
            }
        }
        viewModel.inboundResultsObservable.subscribe {
            if (!isByotEnabled) {
                searchTrackingBuilder.searchResponse(it)
                inboundPresenter.trackFlightResultsLoad()
                showInboundPresenter(viewModel.searchParamsObservable.value.departureAirport)
            }
        }
        viewModel.errorObservable.subscribe {
            errorPresenter.viewmodel.searchApiErrorObserver.onNext(it)
            show(errorPresenter)
        }
        viewModel.noNetworkObservable.subscribe {
            show(searchPresenter)
        }
        viewModel.searchingForFlightDateTime.subscribe {
            searchTrackingBuilder.markSearchApiCallMade()
        }
        viewModel.resultsReceivedDateTimeObservable.subscribe {
            searchTrackingBuilder.markApiResponseReceived()
        }
        viewModel
    }

    var searchViewModel: FlightSearchViewModel by notNullAndObservable { vm ->
        searchPresenter.searchViewModel = vm
        vm.searchParamsObservable.subscribe { params ->
            announceForAccessibility(context.getString(R.string.accessibility_announcement_searching_flights))
            flightOfferViewModel.searchParamsObservable.onNext(params)
            flightOfferViewModel.isOutboundSearch = true
            errorPresenter.getViewModel().paramsSubject.onNext(params)
            travelerManager.updateDbTravelers(params, context)
            // Starting a new search clear previous selection
            Db.clearPackageFlightSelection()
            outBoundPresenter.clearBackStack()
            outBoundPresenter.showResults()
            show(outBoundPresenter, Presenter.FLAG_CLEAR_TOP)
        }
    }

    init {
        travelerManager = Ui.getApplication(getContext()).travelerComponent().travelerManager()
        Ui.getApplication(getContext()).flightComponent().inject(this)
        View.inflate(context, R.layout.flight_presenter, this)
        searchViewModel = FlightSearchViewModel(context)
        searchViewModel.deeplinkDefaultTransitionObservable.subscribe { screen ->
            setDefaultTransition(screen)
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        addTransition(searchToOutbound)
        addTransition(inboundFlightToOverview)
        addTransition(outboundToInbound)
        addTransition(overviewToConfirmation)
        addTransition(outboundFlightToOverview)
        addTransition(outboundToError)
        addTransition(flightOverviewToError)
        addTransition(errorToSearch)
        addTransition(searchToInbound)
        addTransition(errorToConfirmation)
        addTransition(inboundToError)
    }

    private fun flightListToOverviewTransition() {
        flightOverviewPresenter.bundleOverviewHeader.checkoutOverviewHeaderToolbar.visibility = View.VISIBLE
        flightOverviewPresenter.resetAndShowTotalPriceWidget()
        flightOverviewPresenter.totalPriceWidget.bundleTotalPrice.visibility = View.GONE
        flightOverviewPresenter.getCheckoutPresenter().clearPaymentInfo()
        flightOverviewPresenter.getCheckoutPresenter().updateDbTravelers()
        if (Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppFlightRateDetailExpansion)) {
            flightOverviewPresenter.flightSummary.outboundFlightWidget.expandFlightDetails()
            flightOverviewPresenter.flightSummary.inboundFlightWidget.expandFlightDetails()
        }
    }

    val searchArgbEvaluator = ArgbEvaluator()
    val searchBackgroundColor = TransitionElement(ContextCompat.getColor(context, R.color.search_anim_background), Color.TRANSPARENT)

    private val errorToSearch = object : Presenter.Transition(FlightErrorPresenter::class.java, searchPresenter.javaClass, DecelerateInterpolator(), 200) {
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
                searchPresenter.showDefault()
                flightCreateTripViewModel.reset()
            }
        }
    }

    private val ANIMATION_DURATION = 400
    private val flightOverviewToError = object : Presenter.Transition(FlightOverviewPresenter::class.java, FlightErrorPresenter::class.java, DecelerateInterpolator(), ANIMATION_DURATION) {
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
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

    private val inboundToError = object : Presenter.Transition(FlightInboundPresenter::class.java, FlightErrorPresenter::class.java, DecelerateInterpolator(), 200) {
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            errorPresenter.visibility = View.VISIBLE
            inboundPresenter.visibility = View.GONE
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            super.updateTransition(f, forward)
            errorPresenter.animationUpdate(f, !forward)
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            errorPresenter.visibility = if (forward) View.VISIBLE else View.GONE
            inboundPresenter.visibility = if (!forward) View.VISIBLE else View.GONE
            errorPresenter.animationFinalize()
            if (!forward) {
                inboundPresenter.showResults()
            }
        }
    }

    private inner class FlightResultsToCheckoutOverviewTransition(presenter: Presenter, left: Class<*>, right: Class<*>): LeftToRightTransition(presenter, left, right) {
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            if (forward) {
                flightOverviewPresenter.resetFlightSummary()
                flightOverviewPresenter.resetScrollSpaceHeight()
                flightOverviewPresenter.scrollSpaceView?.viewTreeObserver?.addOnGlobalLayoutListener(flightOverviewPresenter.overviewLayoutListener)
                flightListToOverviewTransition()
            } else {
                flightOverviewPresenter.scrollSpaceView?.viewTreeObserver?.removeOnGlobalLayoutListener(flightOverviewPresenter.overviewLayoutListener)
            }
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            if (!forward) {
                flightOverviewPresenter.bundleOverviewHeader.toggleOverviewHeader(false)
                flightCreateTripViewModel.reset()
            }
        }
    }

    private val inboundFlightToOverview = FlightResultsToCheckoutOverviewTransition(this, FlightInboundPresenter::class.java, FlightOverviewPresenter::class.java)
    private val outboundFlightToOverview = FlightResultsToCheckoutOverviewTransition(this, FlightOutboundPresenter::class.java, FlightOverviewPresenter::class.java)

    private val overviewToConfirmation = LeftToRightTransition(this, FlightOverviewPresenter::class.java, FlightConfirmationPresenter::class.java)

    private val errorToConfirmation = LeftToRightTransition(this, FlightErrorPresenter::class.java, FlightConfirmationPresenter::class.java)

    private val outboundToInbound = object : ScaleTransition(this, FlightOutboundPresenter::class.java, FlightInboundPresenter::class.java) {
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            if (forward) {
                inboundPresenter.resultsPresenter.recyclerView.scrollToPosition(0)
            }
        }
    }

    private val defaultOutboundTransition = object : Presenter.DefaultTransition(FlightOutboundPresenter::class.java.name) {
        override fun endTransition(forward: Boolean) {
            searchPresenter.visibility = View.GONE
            outBoundPresenter.visibility = View.VISIBLE
        }
    }

    private val searchToOutbound = SearchToOutboundTransition(this, searchPresenter.javaClass, FlightOutboundPresenter::class.java)
    private val searchToInbound = SearchToOutboundTransition(this, searchPresenter.javaClass, FlightInboundPresenter::class.java)

    private val defaultSearchTransition = object : Presenter.DefaultTransition(getDefaultSearchPresenterClassName()) {
        override fun endTransition(forward: Boolean) {
            searchPresenter.visibility = View.VISIBLE
            FlightsV2Tracking.trackSearchPageLoad()
        }
    }

    private inner class SearchToOutboundTransition(presenter: Presenter, left: Class<*>, right: Class<*>) : ScaleTransition(presenter, left, right) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            if (!forward) {
                FlightsV2Tracking.trackSearchPageLoad()
                flightCreateTripViewModel.reset()
                outBoundPresenter.resultsPresenter.recyclerView.scrollToPosition(0)
            }
        }
    }

    private fun displayFlightDropDownRoutes(): Boolean {
        return PointOfSale.getPointOfSale().displayFlightDropDownRoutes()
    }

    private fun getDefaultSearchPresenterClassName(): String {
        return if (displayFlightDropDownRoutes()) {
            FlightSearchAirportDropdownPresenter::class.java.name
        } else {
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

    private fun showInboundPresenter(city: SuggestionV4) {
        show(inboundPresenter)
        announceForAccessibility(Phrase.from(context, R.string.accessibility_announcement_showing_inbound_flights_TEMPLATE)
                .put("city", StrUtils.formatCity(city))
                .format().toString())
    }
}
