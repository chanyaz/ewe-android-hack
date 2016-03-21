package com.expedia.bookings.presenter.packages

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.packages.FlightLeg
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FlightListRecyclerView
import com.expedia.bookings.widget.PackageFlightListAdapter
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.expedia.vm.FlightResultsViewModel
import rx.subjects.PublishSubject
import kotlin.properties.Delegates

class PackageFlightResultsPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {
    val recyclerView: FlightListRecyclerView by bindView(R.id.list_view)
    var adapterPackage: PackageFlightListAdapter by Delegates.notNull()
    val flightSelectedSubject = PublishSubject.create<FlightLeg>()

    init {
        View.inflate(getContext(), R.layout.widget_flight_results_package, this)
        adapterPackage = PackageFlightListAdapter(flightSelectedSubject, context)
        recyclerView.adapter = adapterPackage
    }

    var resultsViewModel: FlightResultsViewModel by notNullAndObservable { vm ->
        vm.flightResultsObservable.subscribe(listResultsObserver)
    }

    val listResultsObserver = endlessObserver<List<FlightLeg>> {
        adapterPackage.resultsSubject.onNext(it)
    }

}