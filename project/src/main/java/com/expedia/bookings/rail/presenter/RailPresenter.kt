package com.expedia.bookings.rail.presenter

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewStub
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.requests.RailSearchRequest
import com.expedia.bookings.data.rail.responses.BaseRailOffer
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.data.rail.responses.RailOffer
import com.expedia.bookings.data.rail.responses.RailProduct
import com.expedia.bookings.dialog.DialogFactory
import com.expedia.bookings.presenter.LeftToRightTransition
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.ScaleTransition
import com.expedia.bookings.rail.widget.RailAmenitiesFareRulesWidget
import com.expedia.bookings.rail.widget.RailSearchLegalInfoWebView
import com.expedia.bookings.services.RailServices
import com.expedia.bookings.tracking.RailTracking
import com.expedia.bookings.utils.TravelerManager
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.extensions.withLatestFrom
import com.expedia.util.endlessObserver
import com.expedia.vm.rail.RailCheckoutOverviewViewModel
import com.expedia.vm.rail.RailConfirmationViewModel
import com.expedia.vm.rail.RailCreateTripViewModel
import com.expedia.vm.rail.RailDetailsViewModel
import com.expedia.vm.rail.RailErrorViewModel
import com.expedia.vm.rail.RailInboundDetailsViewModel
import com.expedia.vm.rail.RailInboundResultsViewModel
import com.expedia.vm.rail.RailOutboundResultsViewModel
import com.expedia.vm.rail.RailSearchViewModel
import io.reactivex.Observer
import javax.inject.Inject
import kotlin.properties.Delegates

class RailPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {

    lateinit var railServices: RailServices
        @Inject set
    lateinit var travelerManager: TravelerManager

    val searchPresenter: RailSearchPresenter by bindView(R.id.rail_search_presenter)
    val outboundPresenter: RailOutboundPresenter by bindView(R.id.rail_outbound_presenter)
    val outboundDetailsPresenter: RailDetailsPresenter by bindView(R.id.rail_outbound_details_presenter)
    val inboundPresenter: RailInboundPresenter by bindView(R.id.rail_inbound_presenter)
    val inboundDetailsPresenter: RailInboundDetailsPresenter by bindView(R.id.rail_inbound_details_presenter)
    val tripOverviewPresenter: RailTripOverviewPresenter by bindView(R.id.rail_trip_overview_presenter)
    val railCheckoutPresenter: RailCheckoutPresenter by bindView(R.id.rail_checkout_presenter)
    val confirmationPresenter: RailConfirmationPresenter by bindView(R.id.rail_confirmation_presenter)
    val errorPresenter: RailErrorPresenter by bindView(R.id.widget_rail_errors)

    private val outboundResultsViewModel: RailOutboundResultsViewModel
    private val inboundResultsViewModel: RailInboundResultsViewModel
    private val createTripViewModel: RailCreateTripViewModel
    private val outboundDetailsViewModel = RailDetailsViewModel(context)
    private val inboundDetailsViewModel = RailInboundDetailsViewModel(context)

    val amenitiesFareRulesWidget: RailAmenitiesFareRulesWidget by lazy {
        val viewStub = findViewById<ViewStub>(R.id.amenities_stub)
        val widget = viewStub.inflate() as RailAmenitiesFareRulesWidget
        widget
    }

    var railSearchParams: RailSearchRequest by Delegates.notNull()
    var latestOutboundOfferToken: String? = null

    val searchObserver: Observer<RailSearchRequest> = endlessObserver { params ->
        railSearchParams = params

        travelerManager.updateRailTravelers()
        transitionToOutboundResults()
        outboundPresenter.viewmodel.paramsSubject.onNext(params)
        inboundPresenter.viewmodel.paramsSubject.onNext(params)
        errorPresenter.viewmodel.paramsSubject.onNext(params)
    }

    val outboundLegSelectedObserver: Observer<RailLegOption> = endlessObserver { selectedLegOption ->
        transitionToDetails()
        outboundDetailsViewModel.railLegOptionSubject.onNext(selectedLegOption)
        tripOverviewPresenter.tripSummaryViewModel.railOutboundLegObserver.onNext(selectedLegOption)
    }

    val inboundLegSelectedObserver: Observer<RailLegOption> = endlessObserver { selectedLegOption ->
        show(inboundDetailsPresenter)
        RailTracking().trackRailRoundTripInDetails()
        inboundDetailsViewModel.railLegOptionSubject.onNext(selectedLegOption)
        tripOverviewPresenter.tripSummaryViewModel.railInboundLegObserver.onNext(selectedLegOption)
    }

    private val defaultSearchTransition = object : DefaultTransition(RailSearchPresenter::class.java.name) {
        override fun endTransition(forward: Boolean) {
            searchPresenter.visibility = View.VISIBLE
            RailTracking().trackRailSearchInit()
        }
    }
    private val searchToOutbound = object : LeftToRightTransition(this, RailSearchPresenter::class.java, RailOutboundPresenter::class.java) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            if (!forward) {
                RailTracking().trackRailSearchInit()
            }
        }
    }

    val legalInfoWebView: RailSearchLegalInfoWebView by lazy {
        val viewStub = findViewById<ViewStub>(R.id.search_legal_info_stub)
        val legalInfoView = viewStub.inflate() as RailSearchLegalInfoWebView
        legalInfoView.setExitButtonOnClickListener(View.OnClickListener { this.back() })
        legalInfoView.loadUrl()
        legalInfoView
    }

    private val outboundToDetails = ScaleTransition(this, RailOutboundPresenter::class.java, RailDetailsPresenter::class.java)
    private val outboundDetailsToOverview = object : ScaleTransition(this, RailDetailsPresenter::class.java, RailTripOverviewPresenter::class.java) {
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            if (forward) {
                val overviewHeader = tripOverviewPresenter.bundleOverviewHeader
                overviewHeader.checkoutOverviewHeaderToolbar.visibility = View.VISIBLE
                overviewHeader.toggleOverviewHeader(true)
            } else {
                RailTracking().trackRailOneWayTripDetails()
            }
        }
    }

    private val detailsToInbound = object : LeftToRightTransition(this, RailDetailsPresenter::class.java, RailInboundPresenter::class.java) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            if (forward) {
                RailTracking().trackRailRoundTripInbound()
            } else {
                RailTracking().trackRailRoundTripJourneyDetailsAndFareOptions()
            }
        }
    }
    private val inboundToDetails = ScaleTransition(this, RailInboundPresenter::class.java, RailInboundDetailsPresenter::class.java)
    private val inboundDetailsToOverview = object : ScaleTransition(this, RailInboundDetailsPresenter::class.java, RailTripOverviewPresenter::class.java) {
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            if (forward) {
                val overviewHeader = tripOverviewPresenter.bundleOverviewHeader
                overviewHeader.checkoutOverviewHeaderToolbar.visibility = View.VISIBLE
                overviewHeader.toggleOverviewHeader(true)
            } else {
                RailTracking().trackRailRoundTripInDetails()
            }
        }
    }

    private val overviewToCheckout = object : LeftToRightTransition(this, RailTripOverviewPresenter::class.java, RailCheckoutPresenter::class.java) {
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            if (!forward) {
                RailTracking().trackRailDetails(tripOverviewPresenter.createTripViewModel.tripResponseObservable.value)
            } else {
                RailTracking().trackRailCheckoutInfo(tripOverviewPresenter.createTripViewModel.tripResponseObservable.value)
            }
        }
    }

    private val checkoutToConfirmation = LeftToRightTransition(this, RailCheckoutPresenter::class.java, RailConfirmationPresenter::class.java)

    private val outboundDetailsToAmenities = object : ScaleTransition(this, RailDetailsPresenter::class.java, RailAmenitiesFareRulesWidget::class.java) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            if (!forward) {
                if (searchPresenter.searchViewModel.isRoundTripSearchObservable.value) {
                    RailTracking().trackRailRoundTripJourneyDetailsAndFareOptions()
                } else {
                    RailTracking().trackRailOneWayTripDetails()
                }
            }
        }
    }
    private val inboundDetailsToAmenities = object : ScaleTransition(this, RailInboundDetailsPresenter::class.java, RailAmenitiesFareRulesWidget::class.java) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            if (!forward) {
                RailTracking().trackRailRoundTripInDetails()
            }
        }
    }
    private val overviewToAmenities = object : ScaleTransition(this, RailTripOverviewPresenter::class.java, RailAmenitiesFareRulesWidget::class.java) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            if (!forward) {
                RailTracking().trackRailDetails(tripOverviewPresenter.createTripViewModel.tripResponseObservable.value)
            }
        }
    }

    private val outboundToError = ScaleTransition(this, RailOutboundPresenter::class.java, RailErrorPresenter::class.java)
    private val createTripToError = ScaleTransition(this, RailTripOverviewPresenter::class.java, RailErrorPresenter::class.java)
    private val checkoutToError = ScaleTransition(this, RailCheckoutPresenter::class.java, RailErrorPresenter::class.java)
    private val errorToSearch = object : ScaleTransition(this, RailErrorPresenter::class.java, RailSearchPresenter::class.java) {
        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
            if (forward) {
                RailTracking().trackRailSearchInit()
                searchPresenter.showDefault()
            }
        }
    }
    private val outboundToLegalInfo = ScaleTransition(this, RailOutboundPresenter::class.java, RailSearchLegalInfoWebView::class.java)
    private val inboundToLegalInfo = ScaleTransition(this, RailInboundPresenter::class.java, RailSearchLegalInfoWebView::class.java)

    init {
        Ui.getApplication(context).railComponent().inject(this)
        travelerManager = Ui.getApplication(getContext()).travelerComponent().travelerManager()
        View.inflate(context, R.layout.rail_presenter, this)

        outboundResultsViewModel = RailOutboundResultsViewModel(context, railServices)
        inboundResultsViewModel = RailInboundResultsViewModel(context)
        createTripViewModel = RailCreateTripViewModel(railServices)

        addTransitions()
        initSearchPresenter()
        initOutboundPresenter()
        initOutboundDetailsPresenter()
        initInboundPresenter()
        initInboundDetailsPresenter()
        initOverviewPresenter()
        initCheckoutPresenter()
        initConfirmationPresenter()
        initErrorPresenter()

        show(searchPresenter)
    }

    private fun initSearchPresenter() {
        searchPresenter.searchViewModel = RailSearchViewModel(context)
        searchPresenter.searchViewModel.searchParamsObservable.subscribe(searchObserver)

        val overviewHeader = tripOverviewPresenter.bundleOverviewHeader
        searchPresenter.searchViewModel.searchParamsObservable.subscribe(
                (overviewHeader.checkoutOverviewFloatingToolbar.viewmodel as RailCheckoutOverviewViewModel).params)
        searchPresenter.searchViewModel.searchParamsObservable.subscribe(
                (overviewHeader.checkoutOverviewHeaderToolbar.viewmodel as RailCheckoutOverviewViewModel).params)
    }

    private fun initOutboundPresenter() {
        outboundPresenter.viewmodel = outboundResultsViewModel
        outboundPresenter.viewmodel.railResultsObservable.subscribe {
            outboundDetailsViewModel.railResultsObservable.onNext(it)
            inboundDetailsViewModel.railResultsObservable.onNext(it)
        }

        outboundPresenter.legSelectedSubject.subscribe(outboundLegSelectedObserver)
        outboundPresenter.viewmodel.errorObservable.subscribe {
            errorPresenter.viewmodel.searchApiErrorObserver.onNext(it)
            show(errorPresenter)
        }
        outboundPresenter.viewmodel.noNetworkObservable.subscribe {
            show(searchPresenter, FLAG_CLEAR_BACKSTACK)
        }

        outboundPresenter.legalBannerClicked.subscribe {
            legalInfoWebView.loadUrl()
            show(legalInfoWebView)
        }
    }

    private fun initOutboundDetailsPresenter() {
        outboundDetailsPresenter.viewModel = outboundDetailsViewModel
        outboundDetailsViewModel.offerSelectedObservable.subscribe { offer ->
            latestOutboundOfferToken = offer.railOfferToken

            if (searchPresenter.searchViewModel.isRoundTripSearchObservable.value) {
                inboundResultsViewModel.resultsReturnedSubject.onNext(outboundResultsViewModel
                        .railResultsObservable.value)
                inboundResultsViewModel.outboundOfferSubject.onNext(offer)
                transitionToInboundResults()
            } else {
                transitionToTripSummary()
                createTripViewModel.offerTokensSelected.onNext(listOf(offer.railOfferToken))
            }
        }

        outboundDetailsViewModel.showAmenitiesObservable.withLatestFrom(outboundDetailsViewModel.railLegOptionSubject,
                { offer, legOption -> showAmenities(offer, legOption) }).subscribe()
        outboundDetailsViewModel.showFareRulesObservable.withLatestFrom(outboundDetailsViewModel.railLegOptionSubject,
                { offer, legOption -> showFareRules(offer.railProductList[0], legOption) }).subscribe()
    }

    private fun initInboundPresenter() {
        inboundPresenter.viewmodel = inboundResultsViewModel
        outboundPresenter.legSelectedSubject.subscribe(inboundResultsViewModel.outboundLegOptionSubject)
        inboundPresenter.legSelectedSubject.subscribe(inboundLegSelectedObserver)
        inboundPresenter.legalBannerClicked.subscribe {
            legalInfoWebView.loadUrl()
            show(legalInfoWebView)
        }
    }

    private fun initInboundDetailsPresenter() {
        inboundDetailsPresenter.viewModel = inboundDetailsViewModel
        inboundResultsViewModel.outboundOfferSubject.subscribe(inboundDetailsViewModel.selectedOutboundOfferSubject)

        inboundDetailsViewModel.offerSelectedObservable.subscribe { offer ->
            transitionToTripSummary()
            createTripViewModel.offerTokensSelected.onNext(getRoundTripOfferTokens(offer))
        }
        inboundDetailsViewModel.showAmenitiesObservable.withLatestFrom(inboundDetailsViewModel.railLegOptionSubject,
                { offer, legOption -> showAmenities(offer, legOption) }).subscribe()

        inboundDetailsViewModel.showFareRulesObservable.withLatestFrom(inboundDetailsViewModel.railLegOptionSubject,
                { offer, legOption -> showFareRules(offer.railProductList[0], legOption) }).subscribe()
    }

    private fun initOverviewPresenter() {
        tripOverviewPresenter.tripSummaryViewModel.moreInfoOutboundClicked.subscribe {
            val railProduct = tripOverviewPresenter.tripSummaryViewModel.railOfferObserver.value.railProductList[0]
            showFareRules(railProduct, railProduct.firstLegOption)
        }

        tripOverviewPresenter.tripSummaryViewModel.moreInfoInboundClicked.subscribe {
            val offer = tripOverviewPresenter.tripSummaryViewModel.railOfferObserver.value
            if (offer.isRoundTrip) {
                showFareRules(offer.railProductList[1], offer.railProductList[1].firstLegOption)
            } else if (offer.isOpenReturn) {
                showFareRules(offer.railProductList[0], offer.railProductList[0].secondLegOption)
            }
        }

        tripOverviewPresenter.showCheckoutSubject.subscribe {
            show(railCheckoutPresenter)
            railCheckoutPresenter.onCheckoutOpened()
        }

        tripOverviewPresenter.createTripViewModel = createTripViewModel
        tripOverviewPresenter.createTripViewModel.showNoInternetRetryDialog.subscribe {
            tripOverviewPresenter.createTripDialog.dismiss()
            val retryFun = fun() {
                createTripViewModel.retryObservable.onNext(Unit)
            }
            val cancelFun = fun() {
                back()
            }
            DialogFactory.showNoInternetRetryDialog(context, retryFun, cancelFun)
        }

        tripOverviewPresenter.createTripViewModel.createTripErrorObservable.subscribe {
            tripOverviewPresenter.createTripDialog.dismiss()
            errorPresenter.viewmodel.createTripErrorObserverable.onNext(it)
            show(errorPresenter)
        }
    }

    private fun initCheckoutPresenter() {
        railCheckoutPresenter.createTripViewModel = createTripViewModel
        confirmationPresenter.viewModel = RailConfirmationViewModel(context)

        railCheckoutPresenter.checkoutViewModel.bookingSuccessSubject.subscribe { pair ->
            confirmationPresenter.viewModel.confirmationObservable.onNext(pair)
            show(confirmationPresenter)
        }

        railCheckoutPresenter.checkoutViewModel.checkoutErrorObservable.subscribe {
            railCheckoutPresenter.checkoutDialog.dismiss()
            railCheckoutPresenter.slideToPurchaseWidget.reset()
            errorPresenter.viewmodel.checkoutApiErrorObserver.onNext(it)
            show(errorPresenter)
        }
    }

    private fun initConfirmationPresenter() {
        createTripViewModel.tripResponseObservable.subscribe { response ->
            confirmationPresenter.viewModel.railOfferObserver.onNext(response.railDomainProduct.railOffer)
        }
    }

    private fun initErrorPresenter() {
        errorPresenter.viewmodel = RailErrorViewModel(context)
        errorPresenter.viewmodel.showSearch.subscribe { show(searchPresenter, FLAG_CLEAR_BACKSTACK) }
        errorPresenter.viewmodel.retrySearch.subscribe { searchPresenter.searchViewModel.searchObserver.onNext(Unit) }
        errorPresenter.viewmodel.defaultErrorObservable.subscribe { show(searchPresenter, FLAG_CLEAR_BACKSTACK) }

        errorPresenter.viewmodel.showCheckoutForm.subscribe {
            show(railCheckoutPresenter, Presenter.FLAG_CLEAR_TOP)
            railCheckoutPresenter.slideToPurchaseWidget.reset()
            railCheckoutPresenter.travelersViewModel.refresh()
        }
    }

    private fun addTransitions() {
        addDefaultTransition(defaultSearchTransition)
        addTransition(searchToOutbound)
        addTransition(outboundToDetails)
        addTransition(outboundDetailsToOverview)
        addTransition(detailsToInbound)
        addTransition(inboundToDetails)
        addTransition(inboundDetailsToOverview)
        addTransition(outboundDetailsToAmenities)
        addTransition(inboundDetailsToAmenities)
        addTransition(overviewToAmenities)
        addTransition(overviewToCheckout)
        addTransition(checkoutToConfirmation)
        addTransition(outboundToError)
        addTransition(errorToSearch)
        addTransition(createTripToError)
        addTransition(checkoutToError)
        addTransition(outboundToLegalInfo)
        addTransition(inboundToLegalInfo)
    }

    private fun transitionToOutboundResults() {
        show(outboundPresenter, FLAG_CLEAR_TOP)
    }

    private fun transitionToInboundResults() {
        show(inboundPresenter, FLAG_CLEAR_TOP)
    }

    private fun transitionToDetails() {
        show(outboundDetailsPresenter)
        if (searchPresenter.searchViewModel.isRoundTripSearchObservable.value) {
            RailTracking().trackRailRoundTripJourneyDetailsAndFareOptions()
        } else {
            RailTracking().trackRailOneWayTripDetails()
        }
    }

    private fun transitionToTripSummary() {
        tripOverviewPresenter.show(RailTripOverviewPresenter.BundleDefault(), FLAG_CLEAR_BACKSTACK)
        show(tripOverviewPresenter)
    }

    private fun showAmenities(offer: BaseRailOffer, legOption: RailLegOption) {
        show(amenitiesFareRulesWidget)
        amenitiesFareRulesWidget.showAmenitiesForOffer(legOption, offer.railProductList[0])
    }

    private fun showFareRules(railProduct: RailProduct, legOption: RailLegOption) {
        show(amenitiesFareRulesWidget)
        amenitiesFareRulesWidget.showFareRulesForOffer(legOption, railProduct)
    }

    private fun getRoundTripOfferTokens(offer: RailOffer): List<String> {
        if (offer.isOpenReturn) {
            return listOf(offer.railOfferToken)
        }
        return listOf(latestOutboundOfferToken!!, offer.railOfferToken)
    }

    override fun back(): Boolean {
        if (currentState == RailErrorPresenter::class.java.name) {
            errorPresenter.viewmodel.clickBack.onNext(Unit)
            return true
        }
        return super.back()
    }
}
