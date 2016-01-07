package com.expedia.bookings.presenter.packages

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewStub
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.presenter.Presenter
import com.expedia.vm.FlightResultsViewModel

public class PackageFlightPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {
    val resultsPresenter: PackageFlightResultsPresenter by lazy {
        var viewStub = findViewById(R.id.results_stub) as ViewStub
        var presenter = viewStub.inflate() as PackageFlightResultsPresenter
        presenter.viewmodel = FlightResultsViewModel(context)
        presenter
    }

    init {
        View.inflate(getContext(), R.layout.package_flight_presenter, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        addDefaultTransition(defaultTransition)
        show(resultsPresenter)
        resultsPresenter.showDefault()
//        resultsPresenter.viewmodel.paramsSubject.onNext(convertPackageToSearchParams(Db.getPackageParams(), resources.getInteger(R.integer.calendar_max_days_hotel_stay)))
        resultsPresenter.viewmodel.flightResultsObservable.onNext(Db.getPackageResponse().packageResult.flightsPackage.flights)
    }

    private val defaultTransition = object : Presenter.DefaultTransition(PackageFlightResultsPresenter::class.java.name) {

        override fun startTransition(forward: Boolean) {
            super.startTransition(forward)
            resultsPresenter.visibility = View.VISIBLE
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            super.updateTransition(f, forward)
        }

        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
            resultsPresenter.visibility = if (forward) View.VISIBLE else View.GONE
        }
    }

}
