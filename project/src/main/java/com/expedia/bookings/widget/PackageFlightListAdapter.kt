package com.expedia.bookings.widget

import android.content.Context
import android.support.annotation.UiThread
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.vm.packages.PackageFlightViewModel
import rx.subjects.PublishSubject
import java.util.ArrayList

class PackageFlightListAdapter(context: Context, flightSelectedSubject: PublishSubject<FlightLeg>, val isChangePackageSearch: Boolean)  : FlightListAdapter(context, flightSelectedSubject) {

    var shouldShowBestFlight = false

    @UiThread
    override fun setNewFlights(flights: List<FlightLeg>) {
        val newFlights = ArrayList(flights)

        //best flight could be filtered out
        shouldShowBestFlight = !isChangePackageSearch && newFlights[0].isBestFlight

        //remove best flight view if there is only 1 flight
        if (shouldShowBestFlight && newFlights.size == 2) {
            shouldShowBestFlight = false
            newFlights.removeAt(0)
        }

        super.setNewFlights(newFlights)
    }

    override fun getItemCount(): Int {
        return getFlights().size + adjustPosition()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        if (holder is PackageFlightListAdapter.BestFlightViewHolder) {
            holder.bind(PackageFlightViewModel(holder.itemView.context, getFlights()[0]))
        } else {
           super.onBindViewHolder(holder, position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        when (viewType) {
            ViewTypes.BEST_FLIGHT_VIEW.ordinal -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.flight_cell, parent, false)
                return BestFlightViewHolder(view as ViewGroup, parent.width)
            }
            else -> {
                return super.onCreateViewHolder(parent, viewType)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return ViewTypes.PRICING_STRUCTURE_HEADER_VIEW.ordinal
        } else if (!shouldShowBestFlight) {
            return ViewTypes.FLIGHT_CELL_VIEW.ordinal
        } else {
            when (position) {
                1 -> return ViewTypes.BEST_FLIGHT_VIEW.ordinal
                2 -> return ViewTypes.ALL_FLIGHTS_HEADER_VIEW.ordinal
                else -> return ViewTypes.FLIGHT_CELL_VIEW.ordinal
            }
        }
    }

    inner class BestFlightViewHolder(root: ViewGroup, width: Int) : FlightViewHolder(root, width) {
        init {
            bestFlightView.visibility = View.VISIBLE
        }

        override fun onClick(view: View) {
            flightSelectedSubject.onNext(getFlights()[0])
        }
    }

    override fun adjustPosition(): Int {
        return if (shouldShowBestFlight) 2 else (if (isChangePackageSearch) 0 else 1)
    }
}
