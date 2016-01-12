package com.expedia.bookings.presenter.packages

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.packages.FlightLeg
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.PackageFlightListAdapter
import com.expedia.bookings.widget.FlightListRecyclerView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.FlightResultsViewModel
import rx.subjects.PublishSubject
import kotlin.collections.filter
import kotlin.properties.Delegates

public class PackageFlightResultsPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {
    val recyclerView: FlightListRecyclerView by bindView(R.id.list_view)
    var adapterPackage: PackageFlightListAdapter by Delegates.notNull()
    val outboundFlightSelectedSubject = PublishSubject.create<FlightLeg>()


    init {
        View.inflate(getContext(), R.layout.widget_flight_results_package, this)
        adapterPackage = PackageFlightListAdapter(outboundFlightSelectedSubject)
        recyclerView.adapter = adapterPackage
    }

    var viewmodel: FlightResultsViewModel by notNullAndObservable { vm ->
        val isOutboundSearch = Db.getPackageParams().flightType == Constants.PACKAGE_FLIGHT_TYPE ?: false
        //filter results according to outbound or inbound flight search
        adapterPackage.resultsSubject.onNext(Db.getPackageResponse().packageResult.flightsPackage.flights.filter { it.outbound == isOutboundSearch && it.packageOfferModel != null})
    }

    public fun showDefault() {

        show(ResultsList())
    }

    public class ResultsList

}