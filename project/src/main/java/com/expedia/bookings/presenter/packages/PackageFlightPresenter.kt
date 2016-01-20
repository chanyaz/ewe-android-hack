package com.expedia.bookings.presenter.packages

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewStub
import android.view.animation.DecelerateInterpolator
import com.expedia.bookings.R
import com.expedia.bookings.data.packages.FlightLeg
import com.expedia.bookings.presenter.Presenter
import com.expedia.vm.FlightOverviewViewModel
import com.expedia.vm.FlightResultsViewModel
import com.expedia.vm.FlightToolbarViewModel
import rx.Observer
import rx.exceptions.OnErrorNotImplementedException

public class PackageFlightPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {

    val ANIMATION_DURATION = 400

    val resultsPresenter: PackageFlightResultsPresenter by lazy {
        var viewStub = findViewById(R.id.results_stub) as ViewStub
        var presenter = viewStub.inflate() as PackageFlightResultsPresenter
        presenter.resultsViewModel = FlightResultsViewModel()
        presenter.toolbarViewModel = FlightToolbarViewModel(context)
        presenter.outboundFlightSelectedSubject.subscribe(selectedOutboundFlightObserver)
        presenter
    }

    val overViewPresenter: PackageFlightOverviewPresenter by lazy {
        var viewStub = findViewById(R.id.overview_stub) as ViewStub
        var presenter = viewStub.inflate() as PackageFlightOverviewPresenter
        presenter.viewmodel = FlightOverviewViewModel(context)
        presenter
    }

    init {
        View.inflate(getContext(), R.layout.package_flight_presenter, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        addTransition(overviewTransition)
        addDefaultTransition(defaultTransition)
        show(resultsPresenter)
    }

    private val defaultTransition = object : Presenter.DefaultTransition(PackageFlightResultsPresenter::class.java.name) {
        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
            resultsPresenter.visibility = if (forward) View.VISIBLE else View.GONE
            overViewPresenter.visibility = if (forward) View.GONE else View.VISIBLE
        }
    }

    private val overviewTransition = object : Presenter.Transition(PackageFlightOverviewPresenter::class.java, PackageFlightResultsPresenter::class.java, DecelerateInterpolator(), ANIMATION_DURATION) {
        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
            overViewPresenter.visibility = if (!forward) View.VISIBLE else View.GONE
            resultsPresenter.visibility = if (!forward) View.GONE else View.VISIBLE
        }
    }

    val selectedOutboundFlightObserver = object : Observer<FlightLeg> {
        override fun onNext(flight: FlightLeg) {
            show(overViewPresenter)
            overViewPresenter.viewmodel.selectedFlightLeg.onNext(flight)
        }

        override fun onCompleted() {
        }

        override fun onError(e: Throwable) {
            throw OnErrorNotImplementedException(e)
        }
    }

}
