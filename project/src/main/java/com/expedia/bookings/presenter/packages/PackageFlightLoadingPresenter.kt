package com.expedia.bookings.presenter.packages

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.presenter.flight.BaseFlightPresenter
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.isMidAPIEnabled
import com.expedia.bookings.widget.SlidingBundleWidget
import com.expedia.bookings.widget.packages.PackageFlightListAdapter
import com.expedia.vm.AbstractFlightOverviewViewModel
import com.expedia.vm.packages.FlightOverviewViewModel

class PackageFlightLoadingPresenter(context: Context, attrs: AttributeSet) : BaseFlightPresenter(context, attrs) {
    private val bundleSlidingWidget: SlidingBundleWidget by bindView(R.id.sliding_bundle_widget)

    init {
        View.inflate(getContext(), R.layout.package_flight_presenter, this)
        bundleSlidingWidget.visibility = View.VISIBLE
        bundleSlidingWidget.setupBundleViews(Constants.PRODUCT_PACKAGE_LOADING)

        resultsPresenter.showFilterButton = false
        val flightListAdapter = PackageFlightListAdapter(context, resultsPresenter.flightSelectedSubject, Db.getPackageParams().isChangePackageSearch())
        resultsPresenter.setAdapter(flightListAdapter)
        resultsPresenter.setLoadingState()

        if (!isOutboundResultsPresenter() && Db.getPackageSelectedOutboundFlight() != null) {
            resultsPresenter.outboundFlightSelectedSubject.onNext(Db.getPackageSelectedOutboundFlight())
        }

        val numTravelers = Db.getPackageParams().guests
        val cityBound: String = if (isOutboundResultsPresenter()) Db.getPackageParams().destination?.regionNames?.shortName as String else Db.getPackageParams().origin?.regionNames?.shortName as String
        toolbarViewModel.isOutboundSearch.onNext(isOutboundResultsPresenter())
        toolbarViewModel.city.onNext(cityBound)
        toolbarViewModel.travelers.onNext(numTravelers)
        toolbarViewModel.date.onNext(if (isOutboundResultsPresenter()) Db.getPackageParams().startDate else Db.getPackageParams().endDate)
    }

    override fun addResultOverViewTransition() {
        val activity = (context as AppCompatActivity)
        val intent = activity.intent
        if (intent.hasExtra(Constants.PACKAGE_LOAD_OUTBOUND_FLIGHT)) {
            show(resultsPresenter)
            selectedFlightResults.onNext(Db.getPackageSelectedOutboundFlight())
        } else if (intent.hasExtra(Constants.PACKAGE_LOAD_INBOUND_FLIGHT)) {
            show(resultsPresenter)
            selectedFlightResults.onNext(Db.getPackageFlightBundle().second)
        } else {
            super.addResultOverViewTransition()
            show(resultsPresenter)
        }
    }

    override fun makeFlightOverviewModel(): AbstractFlightOverviewViewModel {
        return FlightOverviewViewModel(context)
    }

    override fun getLineOfBusiness(): LineOfBusiness {
        return LineOfBusiness.PACKAGES
    }

    override fun isOutboundResultsPresenter(): Boolean = Db.getPackageParams()?.isOutboundSearch(isMidAPIEnabled(context)) ?: false

    override fun setupToolbarMenu() {
        //ignore
    }

    override fun trackFlightResultsLoad() {
        //ignore
    }

    override fun trackFlightOverviewLoad() {
        //ignore
    }

    override fun trackFlightSortFilterLoad() {
        //ignore
    }

    override fun trackShowBaggageFee() {
        //ignore
    }

    override fun trackShowPaymentFees() {
        //ignore
    }

    override fun viewBundleSetVisibility(forward: Boolean) {
        //ignore
    }

}