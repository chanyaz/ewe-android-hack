package com.expedia.bookings.presenter.packages

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.packages.FlightLeg
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FlightListAdapter
import com.expedia.bookings.widget.FlightListRecyclerView
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.expedia.vm.FlightResultsViewModel
import kotlin.properties.Delegates

public class PackageFlightResultsPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {
    val recyclerView: FlightListRecyclerView by bindView(R.id.list_view)
    var adapter: FlightListAdapter by Delegates.notNull()

    init {
        View.inflate(getContext(), R.layout.widget_flight_results_package, this)
        adapter = FlightListAdapter()
        recyclerView.adapter = adapter
    }

    var viewmodel: FlightResultsViewModel by notNullAndObservable { vm ->
        vm.flightResultsObservable.subscribe(listResultsObserver)
    }

    val listResultsObserver = endlessObserver<List<FlightLeg>> {
        adapter.resultsSubject.onNext(it)
    }

    public fun showDefault() {
        show(ResultsList())
    }

    public class ResultsList

}