package com.expedia.bookings.presenter.rail;

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewStub
import com.expedia.bookings.R
import com.expedia.bookings.data.BaseApiResponse
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.packages.PackageCheckoutResponse
import com.expedia.bookings.data.rail.requests.RailSearchRequest
import com.expedia.bookings.data.rail.responses.RailCheckoutResponse
import com.expedia.bookings.data.rail.responses.RailSearchResponse.RailOffer
import com.expedia.bookings.presenter.LeftToRightTransition
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.ScaleTransition
import com.expedia.bookings.services.RailServices
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.TravelerManager
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.rail.RailAmenitiesFareRulesWidget
import com.expedia.util.endlessObserver
import com.expedia.vm.rail.RailSearchViewModel
import com.expedia.vm.rail.RailCheckoutOverviewViewModel
import com.expedia.vm.rail.RailCreateTripViewModel
import com.expedia.vm.rail.RailDetailsViewModel
import com.expedia.vm.rail.RailResultsViewModel
import rx.Observer
import javax.inject.Inject
import kotlin.properties.Delegates

class RailPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {

    lateinit var railServices: RailServices
        @Inject set
    lateinit var travelerManager: TravelerManager

    val searchPresenter: RailSearchPresenter by bindView(R.id.widget_rail_search_presenter)
    val resultsPresenter: RailResultsPresenter by bindView(R.id.widget_rail_results_presenter)
    val detailsPresenter: RailDetailsPresenter by bindView(R.id.widget_rail_details_presenter)
    val tripOverviewPresenter: RailTripOverviewPresenter by bindView(R.id.widget_rail_trip_overview_presenter)
    val railCheckoutPresenter: RailCheckoutPresenter by bindView(R.id.widget_rail_checkout_presenter)
    val confirmationPresenter: RailConfirmationPresenter by bindView(R.id.rail_confirmation_presenter)

    val createTripViewModel = RailCreateTripViewModel(Ui.getApplication(context).railComponent().railService())


    val amenitiesFareRulesWidget: RailAmenitiesFareRulesWidget by lazy {
        var viewStub = findViewById(R.id.amenities_stub) as ViewStub
        var widget = viewStub.inflate() as RailAmenitiesFareRulesWidget
        widget
    }
    private val searchToResults = LeftToRightTransition(this, RailSearchPresenter::class.java, RailResultsPresenter::class.java)
    private val resultsToDetails = LeftToRightTransition(this, RailResultsPresenter::class.java, RailDetailsPresenter::class.java)
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

    private val overviewToCheckout = LeftToRightTransition(this, RailTripOverviewPresenter::class.java, RailCheckoutPresenter::class.java)
    private val checkoutToConfirmation = LeftToRightTransition(this, RailCheckoutPresenter::class.java, RailConfirmationPresenter::class.java)
    private val detailsToAmenities = ScaleTransition(this, RailDetailsPresenter::class.java, RailAmenitiesFareRulesWidget::class.java)
    private val overviewToAmenities = ScaleTransition(this, RailTripOverviewPresenter::class.java, RailAmenitiesFareRulesWidget::class.java)

    var railSearchParams: RailSearchRequest by Delegates.notNull()

    val searchObserver: Observer<RailSearchRequest> = endlessObserver { params ->
        railSearchParams = params

        travelerManager.updateRailTravelers()
        transitionToResults()
        resultsPresenter.viewmodel.searchViewModel = searchPresenter.searchViewModel
        resultsPresenter.viewmodel.paramsSubject.onNext(params)
    }

    val offerSelectedObserver: Observer<RailOffer> = endlessObserver { selectedOffer ->
        transitionToDetails()
        detailsPresenter.viewmodel.offerViewModel.offerSubject.onNext(selectedOffer)
    }

    init {
        Ui.getApplication(context).railComponent().inject(this)
        travelerManager = Ui.getApplication(getContext()).travelerComponent().travelerManager()
        View.inflate(context, R.layout.rail_presenter, this)
        addTransitions()

        resultsPresenter.setOnClickListener { transitionToDetails() }
        searchPresenter.searchViewModel = RailSearchViewModel(context)
        searchPresenter.searchViewModel.searchParamsObservable.subscribe(searchObserver)

        val overviewHeader = tripOverviewPresenter.bundleOverviewHeader
        searchPresenter.searchViewModel.searchParamsObservable.subscribe(
                (overviewHeader.checkoutOverviewFloatingToolbar.viewmodel as RailCheckoutOverviewViewModel).params)
        searchPresenter.searchViewModel.searchParamsObservable.subscribe(
                (overviewHeader.checkoutOverviewHeaderToolbar.viewmodel as RailCheckoutOverviewViewModel).params)

        resultsPresenter.viewmodel = RailResultsViewModel(context, railServices)
        resultsPresenter.viewmodel.railResultsObservable.subscribe {
            detailsPresenter.viewmodel.railResultsObservable.onNext(it)
        }
        resultsPresenter.offerSelectedObserver = offerSelectedObserver
        detailsPresenter.viewmodel = RailDetailsViewModel(context)
        detailsPresenter.viewmodel.offerSelectedObservable.subscribe { offer ->
            transitionToTripSummary()
            tripOverviewPresenter.railTripSummary.viewModel.railOfferObserver.onNext(offer)
            createTripViewModel.offerCodeSelectedObservable.onNext(offer.railOfferToken)
        }

        detailsPresenter.viewmodel.showAmenitiesObservable.subscribe { offer ->
            show(amenitiesFareRulesWidget)
            amenitiesFareRulesWidget.showAmenitiesForOffer(offer)
        }

        detailsPresenter.viewmodel.showFareRulesObservable.subscribe { offer ->
            show(amenitiesFareRulesWidget)
            amenitiesFareRulesWidget.showFareRulesForOffer(offer)
        }

        tripOverviewPresenter.railTripSummary.outboundLegSummary.viewModel.showLegInfoObservable.subscribe {
            show(amenitiesFareRulesWidget)
            amenitiesFareRulesWidget.showFareRulesForOffer(tripOverviewPresenter.railTripSummary.outboundLegSummary.viewModel.railOfferObserver.value)
        }

        tripOverviewPresenter.showCheckoutSubject.subscribe {
            show(railCheckoutPresenter)
            railCheckoutPresenter.onCheckoutOpened()
        }

        tripOverviewPresenter.createTripViewModel = createTripViewModel
        railCheckoutPresenter.createTripViewModel = createTripViewModel

        railCheckoutPresenter.checkoutViewModel.bookingSuccessSubject.subscribe { response ->
            confirmationPresenter.update(response)
            show(confirmationPresenter)
        }

        show(searchPresenter)
    }

    private fun addTransitions() {
        addTransition(searchToResults)
        addTransition(resultsToDetails)
        addTransition(detailsToOverview)
        addTransition(detailsToAmenities)
        addTransition(overviewToAmenities)
        addTransition(overviewToCheckout)
        addTransition(checkoutToConfirmation)
    }

    private fun transitionToResults() {
        show(resultsPresenter, FLAG_CLEAR_TOP)
    }

    private fun transitionToDetails() {
        show(detailsPresenter)
    }

    private fun transitionToTripSummary() {
        tripOverviewPresenter.show(RailTripOverviewPresenter.BundleDefault(), FLAG_CLEAR_BACKSTACK)
        show(tripOverviewPresenter)
    }
}