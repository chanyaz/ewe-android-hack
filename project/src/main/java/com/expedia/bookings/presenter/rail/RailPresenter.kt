package com.expedia.bookings.presenter.rail;

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.requests.RailSearchRequest
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import com.expedia.bookings.presenter.LeftToRightTransition
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.services.RailServices
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.endlessObserver
import com.expedia.vm.rail.RailCheckoutOverviewViewModel
import com.expedia.vm.rail.RailDetailsViewModel
import com.expedia.vm.rail.RailResultsViewModel
import com.expedia.vm.rail.RailSearchViewModel
import rx.Observer
import javax.inject.Inject
import kotlin.properties.Delegates

class RailPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {

    lateinit var railServices: RailServices
        @Inject set

    val searchPresenter: RailSearchPresenter by bindView(R.id.widget_rail_search_presenter)
    val resultsPresenter: RailResultsPresenter by bindView(R.id.widget_rail_results_presenter)
    val detailsPresenter: RailDetailsPresenter by bindView(R.id.widget_rail_details_presenter)
    val tripOverviewPresenter: RailTripOverviewPresenter by bindView(R.id.widget_rail_trip_overview_presenter)

    private val searchToResults = LeftToRightTransition(this, RailSearchPresenter::class.java, RailResultsPresenter::class.java)
    private val resultsToDetails = LeftToRightTransition(this, RailResultsPresenter::class.java, RailDetailsPresenter::class.java)
    private val checkoutToSearch = LeftToRightTransition(this, RailTripOverviewPresenter::class.java, RailSearchPresenter::class.java)
    private val detailsToOverview = object : LeftToRightTransition(this, RailDetailsPresenter::class.java, RailTripOverviewPresenter::class.java) {
        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            if (forward) {
                val overviewHeader = tripOverviewPresenter.bundleOverviewHeader
                overviewHeader.checkoutOverviewHeaderToolbar.visibility = View.VISIBLE
                overviewHeader.toggleOverviewHeader(true)
                tripOverviewPresenter.getCheckoutPresenter().resetAndShowTotalPriceWidget()
                tripOverviewPresenter.getCheckoutPresenter().totalPriceWidget.viewModel.bundleTextLabelObservable.onNext(context.getString(R.string.total))
                tripOverviewPresenter.getCheckoutPresenter().totalPriceWidget.viewModel.bundleTotalIncludesObservable.onNext(context.getString(R.string.includes_taxes_and_fees))
            }
        }
    }

    var railSearchParams: RailSearchRequest by Delegates.notNull()

    val searchObserver: Observer<RailSearchRequest> = endlessObserver { params ->
        railSearchParams = params

        transitionToResults()
        resultsPresenter.viewmodel.searchViewModel = searchPresenter.searchViewModel
        resultsPresenter.viewmodel.paramsSubject.onNext(params)
    }

    val offerSelectedObserver: Observer<RailSearchResponse.RailOffer> = endlessObserver { selectedOffer ->
        transitionToDetails()
        detailsPresenter.viewmodel.offerViewModel.offerSubject.onNext(selectedOffer)
    }

    init {
        Ui.getApplication(context).railComponent().inject(this)
        View.inflate(context, R.layout.rail_presenter, this)
        addTransitions()

        resultsPresenter.setOnClickListener { transitionToDetails() }
        tripOverviewPresenter.setOnClickListener { transitionToSearch() } //todo - should show confirmation
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
            tripOverviewPresenter.railTripSummary.outboundLegSummary.viewModel.railOfferObserver.onNext(offer)
            tripOverviewPresenter.getCheckoutPresenter().getCreateTripViewModel().offerCodeSelectedObservable.onNext(offer.railOfferToken)
        }

        show(searchPresenter)
    }

    private fun addTransitions() {
        addTransition(searchToResults)
        addTransition(resultsToDetails)
        addTransition(detailsToOverview)
        addTransition(checkoutToSearch)
    }

    private fun transitionToSearch() {
        show(searchPresenter)
    }

    private fun transitionToResults() {
        show(resultsPresenter, FLAG_CLEAR_TOP)
    }

    private fun transitionToDetails() {
        show(detailsPresenter)
    }

    private fun transitionToTripSummary() {
        show(tripOverviewPresenter)
    }
}