package com.expedia.bookings.presenter.flight

import android.animation.ArgbEvaluator
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.view.ViewStub
import android.view.animation.DecelerateInterpolator
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.animation.TransitionElement
import com.expedia.bookings.data.AbstractItinDetailsResponse
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.BaseApiResponse
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.FlightItinDetailsResponse
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.TravelerParams
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightCheckoutResponse
import com.expedia.bookings.data.flights.FlightCreateTripParams
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.enums.TwoScreenOverviewState
import com.expedia.bookings.extensions.ObservableOld
import com.expedia.bookings.extensions.safeSubscribeOptional
import com.expedia.bookings.extensions.setInverseVisibility
import com.expedia.bookings.extensions.setVisibility
import com.expedia.bookings.extensions.subscribeVisibility
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.presenter.BaseTwoScreenOverviewPresenter
import com.expedia.bookings.presenter.LeftToRightTransition
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.ScaleTransition
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.services.ItinTripServices
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.tracking.flight.FlightSearchTrackingDataBuilder
import com.expedia.bookings.tracking.flight.FlightsV2Tracking
import com.expedia.bookings.tracking.hotel.PageUsableData
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.SearchParamsHistoryUtil
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.TravelerManager
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.isFlexEnabled
import com.expedia.bookings.utils.isFlightGreedySearchEnabled
import com.expedia.bookings.utils.isRecentSearchesForFlightsEnabled
import com.expedia.bookings.utils.isShowFlightsNativeRateDetailsWebviewCheckoutEnabled
import com.expedia.bookings.widget.flights.FlightListAdapter
import com.expedia.bookings.widget.shared.WebCheckoutView
import com.expedia.ui.FlightActivity
import com.expedia.util.Optional
import com.expedia.vm.FlightCheckoutOverviewViewModel
import com.expedia.vm.FlightSearchViewModel
import com.expedia.vm.FlightWebCheckoutViewViewModel
import com.expedia.vm.flights.BaseFlightOffersViewModel
import com.expedia.vm.flights.FlightConfirmationViewModel
import com.expedia.vm.flights.FlightCreateTripViewModel
import com.expedia.vm.flights.FlightErrorViewModel
import com.expedia.vm.flights.FlightOffersViewModel
import com.expedia.vm.flights.FlightOffersViewModelByot
import com.expedia.bookings.services.PackageProductSearchType
import com.mobiata.android.Log
import com.mobiata.android.util.SettingUtils
import com.squareup.phrase.Phrase
import io.reactivex.Observer
import io.reactivex.observers.DisposableObserver
import java.util.Date
import javax.inject.Inject

class FlightPresenter(context: Context, attrs: AttributeSet?) : Presenter(context, attrs) {

    lateinit var flightServices: FlightServices
        @Inject set

    lateinit var flightCreateTripViewModel: FlightCreateTripViewModel
        @Inject set

    lateinit var searchTrackingBuilder: FlightSearchTrackingDataBuilder
        @Inject set
    lateinit var webCheckoutViewModel: FlightWebCheckoutViewViewModel
        @Inject set

    val itinTripServices: ItinTripServices by lazy {
        Ui.getApplication(context).flightComponent().itinTripService()
    }

    lateinit var travelerManager: TravelerManager
    lateinit var createTripBuilder: FlightCreateTripParams.Builder

    val isByotEnabled = AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppFlightByotSearch)
    val pageUsableData = PageUsableData()
    val EBAndroidAppFlightSubpubChange = AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppFlightSubpubChange)
    val isUserEvolableBucketed = AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppFlightsEvolable)
    val isNativeRateDetailsWebviewCheckoutEnabled = isShowFlightsNativeRateDetailsWebviewCheckoutEnabled(context)

    val errorPresenter: FlightErrorPresenter by lazy {
        val viewStub = findViewById<ViewStub>(R.id.error_presenter_stub)
        val presenter = viewStub.inflate() as FlightErrorPresenter
        presenter.viewmodel = FlightErrorViewModel(context)
        presenter.getViewModel().defaultErrorObservable.subscribe {
            show(searchPresenter, Presenter.FLAG_CLEAR_BACKSTACK)
        }
        presenter.getViewModel().fireRetryCreateTrip.subscribe {
            if (shouldShowWebCheckoutWithoutNativeRateDetails()) {
                (webCheckoutView.viewModel as FlightWebCheckoutViewViewModel).doCreateTrip()
                show(webCheckoutView)
            } else {
                if (isNativeRateDetailsWebviewCheckoutEnabled) {
                    clearWebViewHistoryThenCreateTrip()
                } else {
                    flightOverviewPresenter.getCheckoutPresenter().getCreateTripViewModel().performCreateTrip.onNext(Unit)
                }
                show(flightOverviewPresenter, FLAG_CLEAR_TOP)
                flightOverviewPresenter.show(BaseTwoScreenOverviewPresenter.BundleDefault(), FLAG_CLEAR_BACKSTACK)
            }
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
            val viewStub = findViewById<ViewStub>(R.id.search_restricted_airport_dropdown_presenter)
            viewStub.inflate() as FlightSearchAirportDropdownPresenter
        } else {
            val viewStub = findViewById<ViewStub>(R.id.search_presenter)
            viewStub.inflate() as FlightSearchPresenter
        }
    }

    val outBoundPresenter: FlightOutboundPresenter by lazy {
        val viewStub = findViewById<ViewStub>(R.id.outbound_presenter)
        val presenter = viewStub.inflate() as FlightOutboundPresenter
        presenter.flightOfferViewModel = flightOfferViewModel
        searchViewModel.searchParamsObservable.subscribe { params ->
            presenter.toolbarViewModel.regionNames.onNext(Optional(params.arrivalAirport.regionNames))
            presenter.toolbarViewModel.country.onNext(Optional(params.arrivalAirport.hierarchyInfo?.country?.name))
            presenter.toolbarViewModel.airport.onNext(Optional(params.arrivalAirport.hierarchyInfo?.airport?.airportCode))
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
            flightOfferViewModel.isGreedyCallAborted = true
            true
        })
        presenter.setupComplete()

        (presenter.resultsPresenter.recyclerView.adapter as FlightListAdapter).allViewsLoadedTimeObservable.subscribe {
            trackResultsLoaded()
        }
        if (AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppFLightLoadingStateV1)) {
            presenter.flightOfferViewModel.retrySearchObservable.subscribe {
                presenter.resultsPresenter.setLoadingState()
            }
        }

        presenter.detailsPresenter.vm.selectedFlightClickedSubject.subscribe {
            searchTrackingBuilder.markSearchClicked()
            searchTrackingBuilder.searchParams(Db.getFlightSearchParams())
        }
        presenter
    }

    private fun trackResultsLoaded() {
        searchTrackingBuilder.markResultsUsable()
        if (searchTrackingBuilder.isWorkComplete()) {
            val trackingData = searchTrackingBuilder.build()
            FlightsV2Tracking.trackResultOutBoundFlights(trackingData, flightOfferViewModel.isSubPub)
        }
    }

    val inboundPresenter: FlightInboundPresenter by lazy {
        val viewStub = findViewById<ViewStub>(R.id.inbound_presenter)
        val presenter = viewStub.inflate() as FlightInboundPresenter
        presenter.flightOfferViewModel = flightOfferViewModel
        searchViewModel.searchParamsObservable.subscribe { params ->
            presenter.toolbarViewModel.regionNames.onNext(Optional(params.departureAirport.regionNames))
            presenter.toolbarViewModel.country.onNext(Optional(params.departureAirport.hierarchyInfo?.country?.name))
            presenter.toolbarViewModel.airport.onNext(Optional(params.departureAirport.hierarchyInfo?.airport?.airportCode))
            presenter.toolbarViewModel.travelers.onNext(params.guests)
            params.returnDate?.let {
                presenter.toolbarViewModel.date.onNext(it)
            }
        }
        presenter.menuSearch.setOnMenuItemClickListener({
            show(searchPresenter)
            flightOfferViewModel.isGreedyCallAborted = true
            true
        })
        presenter.setupComplete()
        presenter.flightOfferViewModel.inboundResultsObservable.subscribe {
            searchTrackingBuilder.markResultsProcessed()
            searchTrackingBuilder.searchResponse(it)
        }

        (presenter.resultsPresenter.recyclerView.adapter as FlightListAdapter).allViewsLoadedTimeObservable.subscribe {
            searchTrackingBuilder.markResultsUsable()
            if (searchTrackingBuilder.isWorkComplete()) {
                val trackingData = searchTrackingBuilder.build()
                FlightsV2Tracking.trackResultInBoundFlights(trackingData, Pair(flightOfferViewModel.confirmedOutboundFlightSelection.value.legRank,
                        flightOfferViewModel.totalOutboundResults))
            }
        }
        presenter
    }

    val flightOverviewPresenter: FlightOverviewPresenter by lazy {
        val viewStub = findViewById<ViewStub>(R.id.overview_presenter)
        val presenter = viewStub.inflate() as FlightOverviewPresenter
        presenter.flightSummary.outboundFlightWidget.viewModel.selectedFlightObservable.onNext(PackageProductSearchType.MultiItemOutboundFlights)
        presenter.flightSummary.inboundFlightWidget.viewModel.selectedFlightObservable.onNext(PackageProductSearchType.MultiItemInboundFlights)
        searchViewModel.searchParamsObservable.subscribe((presenter.bundleOverviewHeader.checkoutOverviewFloatingToolbar.viewmodel as FlightCheckoutOverviewViewModel).params)
        searchViewModel.searchParamsObservable.subscribe((presenter.bundleOverviewHeader.checkoutOverviewHeaderToolbar.viewmodel as FlightCheckoutOverviewViewModel).params)
        searchViewModel.isRoundTripSearchObservable.subscribeVisibility(presenter.flightSummary.inboundFlightWidget)
        searchViewModel.searchParamsObservable.subscribe { params ->
            presenter.flightSummary.viewmodel.params.onNext(params)
            if (params.returnDate != null) {
                presenter.flightSummary.inboundFlightWidget.viewModel.searchTypeStateObservable.onNext(PackageProductSearchType.MultiItemInboundFlights)
            }
            presenter.flightSummary.outboundFlightWidget.viewModel.searchParams.onNext(params)
            presenter.flightSummary.inboundFlightWidget.viewModel.searchParams.onNext(params)
            presenter.flightSummary.outboundFlightWidget.viewModel.searchTypeStateObservable.onNext(PackageProductSearchType.MultiItemOutboundFlights)
            presenter.flightSummary.setPadding(0, 0, 0, 0)
        }
        presenter.fareFamilyCardView.viewModel.updateTripObserver.subscribe {
            createTripBuilder.productKey(it.first)
            createTripBuilder.fareFamilyCode(it.second.fareFamilyCode)
            createTripBuilder.fareFamilyTotalPrice(it.second.totalPrice.amount)
            flightCreateTripViewModel.tripParams.onNext(createTripBuilder.build())
            if (isNativeRateDetailsWebviewCheckoutEnabled) {
                clearWebViewHistory()
            }
        }
        ObservableOld.combineLatest(flightOfferViewModel.confirmedOutboundFlightSelection,
                flightOfferViewModel.confirmedInboundFlightSelection,
                { outbound, inbound ->
                    val baggageFeesTextWithColoredClickableLinks = StrUtils.generateBaggageFeesTextWithClickableLinks(context, outbound.baggageFeesUrl, inbound.baggageFeesUrl)
                    presenter.viewModel.splitTicketBaggageFeesLinksObservable.onNext(baggageFeesTextWithColoredClickableLinks)
                }).subscribe()

        inboundPresenter.detailsPresenter.vm.selectedFlightClickedSubject.subscribe(presenter.flightSummary.inboundFlightWidget.viewModel.flight)
        outBoundPresenter.detailsPresenter.vm.selectedFlightClickedSubject.subscribe(presenter.flightSummary.outboundFlightWidget.viewModel.flight)

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
        flightOfferViewModel.obFeeDetailsUrlObservable.subscribe { obFeeDetailsUrl ->
            presenter.viewModel.obFeeDetailsUrlObservable.onNext(obFeeDetailsUrl)
            checkoutViewModel.obFeeDetailsUrlSubject.onNext(obFeeDetailsUrl)
        }
        flightOfferViewModel.confirmedOutboundFlightSelection.subscribe {
            presenter.viewModel.showFreeCancellationObservable.onNext(it.isFreeCancellable)
            presenter.viewModel.outboundSelectedAndTotalLegRank = Pair(it.legRank, flightOfferViewModel.totalOutboundResults)
            presenter.viewModel.inboundSelectedAndTotalLegRank = null
        }
        flightOfferViewModel.confirmedInboundFlightSelection.subscribe {
            presenter.viewModel.inboundSelectedAndTotalLegRank = Pair(it.legRank, flightOfferViewModel.totalInboundResults)
        }
        flightOfferViewModel.ticketsLeftObservable.subscribe(checkoutViewModel.seatsRemainingObservable)
        flightOfferViewModel.flightOfferSelected.subscribe { flightOffer ->
            val mayChargeObFees = flightOffer.mayChargeOBFees
            presenter.viewModel.showSplitTicketMessagingObservable.onNext(flightOffer.isSplitTicket)
            presenter.viewModel.showAirlineFeeWarningObservable.onNext(mayChargeObFees)
            checkoutViewModel.hasPaymentChargeFeesSubject.onNext(mayChargeObFees)

            if (mayChargeObFees) {
                presenter.viewModel.airlineFeeWarningTextObservable.onNext(context.resources.getString(R.string.airline_additional_fee_notice))
            } else {
                presenter.viewModel.airlineFeeWarningTextObservable.onNext("")
            }
        }

        checkoutViewModel.checkoutRequestStartTimeObservable.subscribe { startTime ->
            pageUsableData.markPageLoadStarted(startTime)
        }

        checkoutViewModel.bookingSuccessResponse.subscribe { pair: Pair<BaseApiResponse, String> ->
            val flightCheckoutResponse = pair.first as FlightCheckoutResponse
            val userEmail = pair.second
            confirmationPresenter.showConfirmationInfo(flightCheckoutResponse, userEmail)
            show(confirmationPresenter)
            pageUsableData.markAllViewsLoaded(Date().time)
            FlightsV2Tracking.trackCheckoutConfirmationPageLoad(flightCheckoutResponse, pageUsableData, presenter.flightSummary)
        }

        presenter.viewModel.showWebviewCheckoutObservable.subscribe {
            webCheckoutView.toggleLoading(webCheckoutView.webView.progress < 33)
            show(webCheckoutView)
            webCheckoutView.visibility = View.VISIBLE
            webCheckoutView.viewModel.showWebViewObservable.onNext(true)
            flightOverviewPresenter.visibility = View.GONE
        }

        val createTripViewModel = presenter.getCheckoutPresenter().getCreateTripViewModel()
        createTripViewModel.createTripResponseObservable.safeSubscribeOptional { trip ->
            val expediaRewards = trip.rewards?.totalPointsToEarn?.toString()
            confirmationPresenter.viewModel.setRewardsPoints.onNext(Optional(expediaRewards))
        }
        createTripViewModel.createTripErrorObservable.subscribe(errorPresenter.viewmodel.createTripErrorObserverable)
        createTripViewModel.createTripErrorObservable.subscribe { show(errorPresenter) }
        createTripViewModel.noNetworkObservable.subscribe {
            show(inboundPresenter, FLAG_CLEAR_TOP)
        }
        checkoutViewModel.checkoutErrorObservable.subscribe(errorPresenter.viewmodel.checkoutApiErrorObserver)
        checkoutViewModel.checkoutErrorObservable
                .filter { it.getErrorCode() != ApiError.Code.TRIP_ALREADY_BOOKED }
                .subscribe { show(errorPresenter) }
        presenter
    }

    val confirmationPresenter: FlightConfirmationPresenter by lazy {
        val viewStub = findViewById<ViewStub>(R.id.confirmation_presenter)
        val presenter = viewStub.inflate() as FlightConfirmationPresenter
        presenter.viewModel = FlightConfirmationViewModel(context)

        searchViewModel.searchParamsObservable.subscribe(presenter.hotelCrossSell.viewModel.searchParamsObservable)
        searchViewModel.isRoundTripSearchObservable.subscribe(presenter.viewModel.inboundCardVisibility)
        searchViewModel.searchParamsObservable.subscribe(presenter.viewModel.flightSearchParamsObservable)
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
            createTripBuilder = FlightCreateTripParams.Builder()
            createTripBuilder.productKey(productKey)
            createTripBuilder.setFlexEnabled(isFlexEnabled(context))
            if (AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppFlightsAPIKongEndPoint)) {
                createTripBuilder.setNumberOfAdultTravelers(Db.getFlightSearchParams().adults)
                createTripBuilder.setChildTravelerAge(Db.getFlightSearchParams().children)
                createTripBuilder.setInfantSeatingInLap(Db.getFlightSearchParams().infantSeatingInLap)
            }
            if (EBAndroidAppFlightSubpubChange) {
                createTripBuilder.setFeatureOverride(Constants.FEATURE_SUBPUB)
            }
            if (isUserEvolableBucketed) {
                createTripBuilder.setFeatureOverride(Constants.FEATURE_EVOLABLE)
            }
            flightCreateTripViewModel.tripParams.onNext(createTripBuilder.build())
            if (shouldShowWebCheckoutWithoutNativeRateDetails()) {
                (webCheckoutView.viewModel as FlightWebCheckoutViewViewModel).doCreateTrip()
                show(webCheckoutView)
                webCheckoutView.visibility = View.VISIBLE
            } else {
                if (isNativeRateDetailsWebviewCheckoutEnabled) {
                    clearWebViewHistoryThenCreateTrip()
                }
                flightOverviewPresenter.overviewPageUsableData.markPageLoadStarted(System.currentTimeMillis())
                show(flightOverviewPresenter)
                flightOverviewPresenter.visibility = View.VISIBLE
                flightOverviewPresenter.show(BaseTwoScreenOverviewPresenter.BundleDefault(), FLAG_CLEAR_BACKSTACK)
            }
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
                showInboundPresenter(viewModel.searchParamsObservable.value.departureAirport)
            }
        }
        if (isFlightGreedySearchEnabled(context)) {
            ObservableOld.zip(searchViewModel.searchParamsObservable, viewModel.errorObservableForGreedyCall, viewModel.hasUserClickedSearchObservable,
                    { _, error, hasUserClickedSearch ->
                        object {
                            val error = error
                            val hasUserClickedSearch = hasUserClickedSearch
                        }
                    }).filter { !viewModel.isGreedyCallAborted }
                    .subscribe {
                        var delayMillis = 0L
                        if (!it.hasUserClickedSearch) {
                            delayMillis = 700L
                        }
                        postDelayed({ viewModel.errorObservable.onNext(it.error) }, delayMillis)
                    }
        }

        ObservableOld.combineLatest(viewModel.isRoundTripSearchSubject, viewModel.mayChargePaymentFeesSubject, {
            isRoundTripSearch, mayChargePaymentFees ->
            outBoundPresenter.displayPaymentFeeHeaderInfo(mayChargePaymentFees)
            if (isRoundTripSearch) {
                inboundPresenter.displayPaymentFeeHeaderInfo(mayChargePaymentFees)
            }
        }).subscribe()

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

    val searchViewModel: FlightSearchViewModel by lazy {
        val vm = FlightSearchViewModel(context)
        searchPresenter.searchViewModel = vm
        vm.searchParamsObservable.subscribe { params ->
            announceForAccessibility(context.getString(R.string.accessibility_announcement_searching_flights))
            flightOfferViewModel.searchParamsObservable.onNext(params)
            flightOfferViewModel.isOutboundSearch = true
            errorPresenter.getViewModel().paramsSubject.onNext(params)
            travelerManager.updateDbTravelers(params)
            // Starting a new search clear previous selection
            Db.sharedInstance.clearPackageFlightSelection()
            outBoundPresenter.clearBackStack()
            outBoundPresenter.showResults()
            show(outBoundPresenter, Presenter.FLAG_CLEAR_TOP)
        }
        if (isFlightGreedySearchEnabled(context)) {
            vm.greedySearchParamsObservable.subscribe(flightOfferViewModel.greedyFlightSearchObservable)
        }
        vm
    }

    val webCheckoutView: WebCheckoutView by lazy {
        val viewStub = findViewById<ViewStub>(R.id.flight_web_checkout_stub)
        val webCheckoutView = viewStub.inflate() as WebCheckoutView
        val flightWebCheckoutViewModel = webCheckoutViewModel
        flightWebCheckoutViewModel.flightCreateTripViewModel = flightCreateTripViewModel
        flightWebCheckoutViewModel.flightCreateTripViewModel.createTripErrorObservable.subscribe(errorPresenter.viewmodel.createTripErrorObserverable)
        flightWebCheckoutViewModel.flightCreateTripViewModel.createTripErrorObservable.subscribe { show(errorPresenter) }
        webCheckoutView.viewModel = flightWebCheckoutViewModel

        flightWebCheckoutViewModel.closeView.subscribe {
            if (isNativeRateDetailsWebviewCheckoutEnabled) {
                if (webCheckoutView.visibility == View.VISIBLE) {
                    super.back()
                }
            } else {
                webCheckoutView.clearHistory()
                flightWebCheckoutViewModel.webViewURLObservable.onNext(context.getString(R.string.clear_webview_url))
            }
        }

        flightWebCheckoutViewModel.backObservable.subscribe {
            webCheckoutView.back()
        }

        flightWebCheckoutViewModel.blankViewObservable.subscribe {
            if (isNativeRateDetailsWebviewCheckoutEnabled) {
                webCheckoutView.toggleLoading(true)
            } else {
                super.back()
            }
        }

        flightWebCheckoutViewModel.fetchItinObservable.subscribe { bookedTripID ->
            itinTripServices.getTripDetails(bookedTripID, makeNewItinResponseObserver())
        }

        flightWebCheckoutViewModel.showNativeSearchObservable.subscribe {
            show(searchPresenter, FLAG_CLEAR_TOP)
            webCheckoutView.visibility = View.GONE
            searchPresenter.visibility = View.VISIBLE
        }

        webCheckoutView
    }

    val bookingSuccessDialog: android.app.AlertDialog by lazy {
        val builder = android.app.AlertDialog.Builder(context)
        builder.setTitle(context.getString(R.string.booking_successful))
        builder.setMessage(context.getString(R.string.check_your_email_for_itin))
        builder.setPositiveButton(context.getString(R.string.ok), { dialog, _ ->
            if (currentState == WebCheckoutView::class.java.name) {
                (context as Activity).finish()
            }
            dialog.dismiss()
        })
        val dialog = builder.create()
        dialog.setOnShowListener {
            if (!ExpediaBookingApp.isRobolectric()) {
                OmnitureTracking.trackFlightsBookingConfirmationDialog(pageUsableData)
            }
        }
        dialog
    }

    init {
        travelerManager = Ui.getApplication(getContext()).travelerComponent().travelerManager()
        Ui.getApplication(getContext()).flightComponent().inject(this)
        View.inflate(context, R.layout.flight_presenter, this)
        searchViewModel.deeplinkDefaultTransitionObservable.subscribe { screen ->
            setDefaultTransition(screen)
        }
        searchViewModel.searchTravelerParamsObservable.subscribe { searchParams ->
            searchPresenter.travelerWidgetV2.traveler.getViewModel().travelerParamsObservable
                    .onNext(TravelerParams(searchParams.numAdults, emptyList(), emptyList(), emptyList()))
        }
        if (isFlightGreedySearchEnabled(context)) {
            ObservableOld.zip(searchViewModel.searchParamsObservable, flightOfferViewModel.greedyOutboundResultsObservable, flightOfferViewModel.hasUserClickedSearchObservable,
                    { _, results, hasUserClickedSearch ->
                        object {
                            val results = results
                            val hasUserClickedSearch = hasUserClickedSearch
                        }
                    }).filter { !flightOfferViewModel.isGreedyCallAborted }.subscribe {
                var delayMillis = 0L
                if (!it.hasUserClickedSearch) {
                    delayMillis = 700L
                }
                postDelayed({ flightOfferViewModel.outboundResultsObservable.onNext(it.results) }, delayMillis)
            }
            searchViewModel.cancelGreedyCallObservable.subscribe {
                flightOfferViewModel.cancelGreedySearchObservable.onNext(Unit)
                flightOfferViewModel.isGreedyCallAborted = true
            }
            searchViewModel.trackSearchClicked.subscribe {
                FlightsV2Tracking.trackSearchClick(Db.getFlightSearchParams(), true, flightOfferViewModel.isGreedyCallAborted)
            }
        }

        if (isRecentSearchesForFlightsEnabled(context)) {
            flightOfferViewModel.outboundResultsObservable.map({ offers -> offers.first().packageOfferModel.price.averageTotalPricePerTicket }).subscribe {
                searchPresenter.recentSearchWidgetContainer.viewModel.saveRecentSearchObservable.onNext(it)
            }
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        addTransition(searchToOutbound)
        addTransition(outboundToInbound)
        addTransition(outboundToError)
        addTransition(flightOverviewToError)
        addTransition(errorToSearch)
        addTransition(searchToInbound)
        addTransition(errorToConfirmation)
        addTransition(inboundToError)
        when {
            isNativeRateDetailsWebviewCheckoutEnabled -> {
                addTransition(overviewToWebCheckoutView)
                addTransition(inboundFlightToOverview)
                addTransition(outboundFlightToOverview)
                addTransition(flightWebViewToError)
                addTransition(webCheckoutViewToConfirmation)
                addTransition(flightWebViewErrorToSearch)
            }
            shouldShowWebCheckoutWithoutNativeRateDetails() -> {
                addTransition(inboundToWebCheckoutView)
                addTransition(outboundToWebCheckoutView)
                addTransition(flightWebViewToError)
                addTransition(webCheckoutViewToConfirmation)
            }
            else -> {
                addTransition(inboundFlightToOverview)
                addTransition(outboundFlightToOverview)
                addTransition(overviewToConfirmation)
            }
        }

        if (BuildConfig.DEBUG && SettingUtils.get(context, R.string.preference_enable_retain_prev_flight_search_params, false)) {
            SearchParamsHistoryUtil.loadPreviousFlightSearchParams(context, loadSuccess, loadFailed)
        } else {
            searchViewModel.isReadyForInteractionTracking.onNext(Unit)
        }
    }

    private val loadSuccess: (FlightSearchParams) -> Unit = { params ->
        (context as Activity).runOnUiThread {
            searchViewModel.previousSearchParamsObservable.onNext(params)
        }
    }

    private val loadFailed: () -> Unit = {
        searchViewModel.isReadyForInteractionTracking.onNext(Unit)
    }

    private fun flightListToOverviewTransition() {
        flightOverviewPresenter.bundleOverviewHeader.checkoutOverviewHeaderToolbar.visibility = View.VISIBLE
        flightOverviewPresenter.resetAndShowTotalPriceWidget()
        flightOverviewPresenter.totalPriceWidget.bundleTotalPrice.visibility = View.GONE
        flightOverviewPresenter.getCheckoutPresenter().clearPaymentInfo()
        flightOverviewPresenter.getCheckoutPresenter().updateDbTravelers()
        if (AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppFlightRateDetailsFromCache)) {
            flightOverviewPresenter.getCheckoutPresenter().getCheckoutViewModel()
                    .bottomCheckoutContainerStateObservable.onNext(TwoScreenOverviewState.CHECKOUT)
        } else {
            flightOverviewPresenter.getCheckoutPresenter().getCheckoutViewModel()
                    .bottomCheckoutContainerStateObservable.onNext(TwoScreenOverviewState.BUNDLE)
        }
        flightOverviewPresenter.fareFamilyCardView.visibility = View.GONE
        if (isUserEvolableBucketed) {
            flightOverviewPresenter.flightSummary.evolableTermsConditionTextView.visibility = View.GONE
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
            searchPresenter.animationFinalize()
            errorPresenter.visibility = if (forward) View.GONE else View.VISIBLE
            searchPresenter.visibility = if (forward) View.VISIBLE else View.GONE
            if (forward) {
                FlightsV2Tracking.trackSearchPageLoad()
                searchPresenter.showDefault()
                flightCreateTripViewModel.reset()
                if (isFlightGreedySearchEnabled(context)) {
                    searchViewModel.abortGreedyCallObservable.onNext(Unit)
                }
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

    private val overviewToWebCheckoutView = object : Transition(FlightOverviewPresenter::class.java, WebCheckoutView::class.java, DecelerateInterpolator(), ANIMATION_DURATION) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            transitionToWebView(forward)
            flightOverviewPresenter.setInverseVisibility(forward)
        }
    }

    private val flightWebViewErrorToSearch = object : Transition(WebCheckoutView::class.java, FlightSearchPresenter::class.java, DecelerateInterpolator(), ANIMATION_DURATION) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(!forward)
            transitionToWebView(!forward)
            searchPresenter.setVisibility(forward)
        }
    }

    private val flightWebViewToError = object : Presenter.Transition(WebCheckoutView::class.java, FlightErrorPresenter::class.java, DecelerateInterpolator(), ANIMATION_DURATION) {
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            errorPresenter.visibility = View.VISIBLE
            webCheckoutView.visibility = View.GONE
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            super.updateTransition(f, forward)
            errorPresenter.animationUpdate(f, !forward)
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            webCheckoutView.visibility = if (forward) View.GONE else View.VISIBLE
            errorPresenter.visibility = if (forward) View.VISIBLE else View.GONE
        }
    }

    private val inboundToWebCheckoutView = object : Transition(FlightInboundPresenter::class.java, WebCheckoutView::class.java, DecelerateInterpolator(), ANIMATION_DURATION) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            transitionToWebView(forward)
            inboundPresenter.setInverseVisibility(forward)
        }
    }

    private val outboundToWebCheckoutView = object : Transition(FlightOutboundPresenter::class.java, WebCheckoutView::class.java, DecelerateInterpolator(), ANIMATION_DURATION) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            transitionToWebView(forward)
            outBoundPresenter.setInverseVisibility(forward)
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

    private inner class FlightResultsToCheckoutOverviewTransition(presenter: Presenter, left: Class<*>, right: Class<*>) : LeftToRightTransition(presenter, left, right) {
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
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            if (forward) {
                if ((searchPresenter.flightAdvanceSearchView.visibility == View.VISIBLE) && !PointOfSale.getPointOfSale().hideAdvancedSearchOnFlights() &&
                        AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppFlightAdvanceSearch)) {
                    searchPresenter.flightAdvanceSearchWidget.toggleAdvanceSearchWidget()
                }
            }
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            if (!forward) {
                FlightsV2Tracking.trackSearchPageLoad()
                flightCreateTripViewModel.reset()
                outBoundPresenter.resultsPresenter.recyclerView.scrollToPosition(0)
            }
        }
    }

    private val webCheckoutViewToConfirmation = object : Transition(WebCheckoutView::class.java, FlightConfirmationPresenter::class.java) {
        override fun endTransition(forward: Boolean) {
            if (forward) {
                confirmationPresenter.visibility = View.VISIBLE
                webCheckoutView.visibility = View.GONE
                AccessibilityUtil.delayFocusToToolbarNavigationIcon(confirmationPresenter.toolbar, 300)
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

    fun makeNewItinResponseObserver(): Observer<AbstractItinDetailsResponse> {
        confirmationPresenter.viewModel = FlightConfirmationViewModel(context, isWebCheckout = true)
        pageUsableData.markPageLoadStarted(System.currentTimeMillis())
        return object : DisposableObserver<AbstractItinDetailsResponse>() {
            override fun onComplete() {
            }

            override fun onNext(itinDetailsResponse: AbstractItinDetailsResponse) {
                if (itinDetailsResponse.errors != null) {
                    bookingSuccessDialog.show()
                } else {
                    val flightItinDetailsResponse = itinDetailsResponse as FlightItinDetailsResponse
                    confirmationPresenter.showConfirmationInfoFromWebCheckoutView(flightItinDetailsResponse)
                    show(confirmationPresenter, FLAG_CLEAR_BACKSTACK)
                    pageUsableData.markAllViewsLoaded(System.currentTimeMillis())
                    OmnitureTracking.trackWebFlightCheckoutConfirmation(flightItinDetailsResponse, pageUsableData)
                }
            }

            override fun onError(e: Throwable) {
                Log.d("Error fetching itin:" + e.stackTrace)
                bookingSuccessDialog.show()
            }
        }
    }

    override fun back(): Boolean {
        if (currentState == WebCheckoutView::class.java.name) {
            webCheckoutView.back()
            return true
        } else {
            if (isFlightGreedySearchEnabled(context) && currentState == FlightSearchPresenter::class.java.name) {
                if (backStack.size == 1) {
                    searchViewModel.abortGreedyCallObservable.onNext(Unit)
                } else {
                    flightOfferViewModel.isGreedyCallAborted = false
                }
            }
            return super.back()
        }
    }

    private fun clearWebViewHistoryThenCreateTrip() {
        clearWebViewHistory()
        webCheckoutViewModel.doCreateTrip()
    }

    private fun clearWebViewHistory() {
        webCheckoutViewModel.webViewURLObservable.onNext("about:blank")
        webCheckoutView.clearHistory()
        webCheckoutView.webView.clearHistory()
    }

    fun shouldShowWebCheckoutWithoutNativeRateDetails(): Boolean {
        return PointOfSale.getPointOfSale().shouldShowWebCheckout() && !isNativeRateDetailsWebviewCheckoutEnabled
    }

    private fun transitionToWebView(forward: Boolean) {
        webCheckoutView.setVisibility(forward)
        webCheckoutView.toolbar.setVisibility(forward)
        webCheckoutView.viewModel.showWebViewObservable.onNext(forward)
        AccessibilityUtil.setFocusToToolbarNavigationIcon(webCheckoutView.toolbar)
    }
}
