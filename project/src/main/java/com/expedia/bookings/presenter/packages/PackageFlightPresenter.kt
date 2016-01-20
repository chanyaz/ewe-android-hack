package com.expedia.bookings.presenter.packages

import android.app.Activity
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.View
import android.view.ViewStub
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.packages.FlightLeg
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.Constants
import com.expedia.vm.FlightResultsViewModel
import com.expedia.vm.FlightToolbarViewModel
import rx.Observer
import rx.exceptions.OnErrorNotImplementedException

public class PackageFlightPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {
    val resultsPresenter: PackageFlightResultsPresenter by lazy {
        var viewStub = findViewById(R.id.results_stub) as ViewStub
        var presenter = viewStub.inflate() as PackageFlightResultsPresenter
        presenter.resultsViewModel = FlightResultsViewModel()
        presenter.toolbarViewModel = FlightToolbarViewModel(context)
        presenter.outboundFlightSelectedSubject.subscribe(selectedOutboundFlightObserver)
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

    val selectedOutboundFlightObserver = object : Observer<FlightLeg> {
        override fun onNext(flight: FlightLeg) {
            val params = Db.getPackageParams();
            params.flightType = Constants.PACKAGE_FLIGHT_TYPE
            params.selectedLegId = flight.departureLeg
            params.packagePIID = flight.packageOfferModel.piid;
            val activity = (context as AppCompatActivity)
            activity.setResult(Activity.RESULT_OK)
            activity.finish()
        }

        override fun onCompleted() {
        }

        override fun onError(e: Throwable) {
            throw OnErrorNotImplementedException(e)
        }
    }

}
