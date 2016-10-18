package com.expedia.bookings.presenter.rail;

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewStub
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.requests.RailSearchRequest
import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.presenter.LeftToRightTransition
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.ScaleTransition
import com.expedia.bookings.services.RailServices
import com.expedia.bookings.utils.TravelerManager
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.rail.RailAmenitiesFareRulesWidget
import com.expedia.util.endlessObserver
import com.expedia.vm.rail.RailCheckoutOverviewViewModel
import com.expedia.vm.rail.RailConfirmationViewModel
import com.expedia.vm.rail.RailCreateTripViewModel
import com.expedia.vm.rail.RailDetailsViewModel
import com.expedia.vm.rail.RailErrorViewModel
import com.expedia.vm.rail.RailInboundResultsViewModel
import com.expedia.vm.rail.RailOutboundResultsViewModel
import com.expedia.vm.rail.RailSearchViewModel
import rx.Observer
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
    private val inboundDetailsViewModel = RailDetailsViewModel(context)

    val amenitiesFareRulesWidget: RailAmenitiesFareRulesWidget by lazy {
        var viewStub = findViewById(R.id.amenities_stub) as ViewStub
        var widget = viewStub.inflate() as RailAmenitiesFareRulesWidget
        widget
    }

    var railSearchParams: RailSearchRequest by Delegates.notNull()

    val searchObserver: Observer<RailSearchRequest> = endlessObserver { params ->
        railSearchParams = params

        travelerManager.updateRailTravelers()
        transitionToOutboundResults()
        outboundPresenter.viewmodel.paramsSubject.onNext(params)
        inboundPresenter.viewmodel.paramsSubject.onNext(params)
        errorPresenter.getViewModel().paramsSubject.onNext(params)
    }

    val outboundLegSelectedObserver: Observer<RailLegOption> = endlessObserver { selectedLegOption ->
        transitionToDetails()
        outboundDetailsViewModel.railLegOptionSubject.onNext(selectedLegOption)
        tripOverviewPresenter.tripSummaryViewModel.railLegObserver.onNext(selectedLegOption)
    }

    val inboundLegSelectedObserver: Observer<RailLegOption> = endlessObserver { selectedLegOption ->
        show(inboundDetailsPresenter)
        inboundDetailsViewModel.railLegOptionSubject.onNext(selectedLegOption)
    }

    private val searchToResults = LeftToRightTransition(this, RailSearchPresenter::class.java, RailOutboundPresenter::class.java)
    private val outboundToDetails = LeftToRightTransition(this, RailOutboundPresenter::class.java, RailDetailsPresenter::class.java)
    private val detailsToOverview = object : LeftToRightTransition(this, RailDetailsPresenter::class.java, RailTripOverviewPresenter::class.java) {
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            if (forward) {
                val overviewHeader = tripOverviewPresenter.bundleOverviewHeader
                overviewHeader.checkoutOverviewHeaderToolbar.visibility = View.VISIBLE
                overviewHeader.toggleOverviewHeader(true)
            }
        }
    }

    private val detailsToInbound = LeftToRightTransition(this, RailDetailsPresenter::class.java, RailInboundPresenter::class.java)
    private val inboundToDetails = LeftToRightTransition(this, RailInboundPresenter::class.java, RailInboundDetailsPresenter::class.java)
    private val overviewToCheckout = LeftToRightTransition(this, RailTripOverviewPresenter::class.java, RailCheckoutPresenter::class.java)
    private val checkoutToConfirmation = LeftToRightTransition(this, RailCheckoutPresenter::class.java, RailConfirmationPresenter::class.java)
    private val outboundDetailsToAmenities = ScaleTransition(this, RailDetailsPresenter::class.java, RailAmenitiesFareRulesWidget::class.java)
    private val inboundDetailsToAmenities = ScaleTransition(this, RailInboundDetailsPresenter::class.java, RailAmenitiesFareRulesWidget::class.java)
    private val overviewToAmenities = ScaleTransition(this, RailTripOverviewPresenter::class.java, RailAmenitiesFareRulesWidget::class.java)
    private val outboundToError = ScaleTransition(this, RailOutboundPresenter::class.java, RailErrorPresenter::class.java)
    private val errorToSearch = ScaleTransition(this, RailErrorPresenter::class.java, RailSearchPresenter::class.java)

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
        outboundPresenter.setOnClickListener { transitionToDetails() }
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
    }

    private fun initOutboundDetailsPresenter() {
        outboundDetailsPresenter.viewModel = outboundDetailsViewModel
        outboundDetailsViewModel.offerSelectedObservable.subscribe { offer ->
            if (searchPresenter.searchViewModel.isRoundTripSearchObservable.value) {
                inboundResultsViewModel.resultsReturnedObserver.onNext(outboundResultsViewModel.railResultsObservable.value)
                inboundResultsViewModel.outboundOfferSubject.onNext(offer)
                transitionToInboundResults()
            } else {
                transitionToTripSummary()
                tripOverviewPresenter.tripSummaryViewModel.railOfferObserver.onNext(offer)
                createTripViewModel.offerCodeSelectedObservable.onNext(offer.railOfferToken)
                confirmationPresenter.viewModel.railOfferObserver.onNext(offer)
            }
        }

        outboundDetailsViewModel.showAmenitiesObservable.subscribe { offer ->
            show(amenitiesFareRulesWidget)
            amenitiesFareRulesWidget.showAmenitiesForOffer(offer)
        }

        outboundDetailsViewModel.showFareRulesObservable.subscribe { offer ->
            show(amenitiesFareRulesWidget)
            amenitiesFareRulesWidget.showFareRulesForOffer(offer)
        }
    }

    private fun initInboundPresenter() {
        inboundPresenter.viewmodel = inboundResultsViewModel
        outboundPresenter.legSelectedSubject.subscribe(inboundResultsViewModel.outboundLegOptionSubject)
        inboundPresenter.legSelectedSubject.subscribe(inboundLegSelectedObserver)
    }

    private fun initInboundDetailsPresenter() {
        inboundDetailsPresenter.viewModel = inboundDetailsViewModel
        inboundDetailsViewModel.showAmenitiesObservable.subscribe { offer ->
            show(amenitiesFareRulesWidget)
            amenitiesFareRulesWidget.showAmenitiesForOffer(offer)
        }

        inboundDetailsViewModel.showFareRulesObservable.subscribe { offer ->
            show(amenitiesFareRulesWidget)
            amenitiesFareRulesWidget.showFareRulesForOffer(offer)
        }
    }

    private fun initOverviewPresenter() {
        tripOverviewPresenter.railTripSummary.outboundLegSummary.viewModel.showLegInfoObservable.subscribe {
            show(amenitiesFareRulesWidget)
            amenitiesFareRulesWidget.showFareRulesForOffer(tripOverviewPresenter.tripSummaryViewModel.railOfferObserver.value)
        }

        tripOverviewPresenter.showCheckoutSubject.subscribe {
            show(railCheckoutPresenter)
            railCheckoutPresenter.onCheckoutOpened()
        }

        tripOverviewPresenter.createTripViewModel = createTripViewModel
    }

    private fun initCheckoutPresenter() {
        railCheckoutPresenter.createTripViewModel = createTripViewModel
        confirmationPresenter.viewModel = RailConfirmationViewModel(context)

        railCheckoutPresenter.checkoutViewModel.bookingSuccessSubject.subscribe { pair ->
            confirmationPresenter.viewModel.confirmationObservable.onNext(pair)
            show(confirmationPresenter)
        }
    }

    private fun initErrorPresenter() {
        errorPresenter.viewmodel = RailErrorViewModel(context)
        errorPresenter.getViewModel().showSearch.subscribe { show(searchPresenter, FLAG_CLEAR_BACKSTACK) }
        errorPresenter.getViewModel().retrySearch.subscribe { searchPresenter.searchViewModel.searchObserver.onNext(Unit) }
        errorPresenter.getViewModel().defaultErrorObservable.subscribe { show(searchPresenter, FLAG_CLEAR_BACKSTACK) }
    }

    private fun addTransitions() {
        addTransition(searchToResults)
        addTransition(outboundToDetails)
        addTransition(detailsToOverview)
        addTransition(detailsToInbound)
        addTransition(inboundToDetails)
        addTransition(outboundDetailsToAmenities)
        addTransition(inboundDetailsToAmenities)
        addTransition(overviewToAmenities)
        addTransition(overviewToCheckout)
        addTransition(checkoutToConfirmation)
        addTransition(outboundToError)
        addTransition(errorToSearch)
    }

    private fun transitionToOutboundResults() {
        show(outboundPresenter, FLAG_CLEAR_TOP)
    }

    private fun transitionToInboundResults() {
        show(inboundPresenter, FLAG_CLEAR_TOP)
    }

    private fun transitionToDetails() {
        show(outboundDetailsPresenter)
    }

    private fun transitionToTripSummary() {
        tripOverviewPresenter.show(RailTripOverviewPresenter.BundleDefault(), FLAG_CLEAR_BACKSTACK)
        show(tripOverviewPresenter)
    }

    override fun back(): Boolean {
        if (currentState == RailErrorPresenter::class.java.name) {
            errorPresenter.getViewModel().clickBack.onNext(Unit)
            return true
        }
        return super.back()

    }
}
