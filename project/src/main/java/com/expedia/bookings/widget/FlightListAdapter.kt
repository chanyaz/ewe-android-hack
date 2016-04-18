package com.expedia.bookings.widget

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.packages.FlightAirlineWidget
import com.expedia.bookings.widget.packages.FlightLayoverWidget
import com.expedia.util.subscribeText
import com.expedia.vm.packages.PackageFlightViewModel
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.ArrayList

open class FlightListAdapter(val context: Context, val flightSelectedSubject: PublishSubject<FlightLeg>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    protected  var flights: ArrayList<FlightLeg> = ArrayList()
    var maxFlightDuration = 0
    val resultsSubject = BehaviorSubject.create<List<FlightLeg>>()
    open val PRICING_STRUCTURE_HEADER_VIEW = 0
    open val ALL_FLIGHTS_HEADER_VIEW = 1
    open val ALL_FLIGHTS_VIEW = 2

    init {
        resultsSubject.subscribe {
            flights = ArrayList(it)

            for (flightLeg in flights) {
                if (flightLeg.durationHour * 60 + flightLeg.durationMinute > maxFlightDuration) {
                    maxFlightDuration = flightLeg.durationHour * 60 + flightLeg.durationMinute
                }

            }
            notifyDataSetChanged()
        }

    }

    override fun getItemCount(): Int {
        return flights.size + adjustPosition()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        if (holder is FlightViewHolder) {
            holder.bind(PackageFlightViewModel(holder.itemView.context, flights[position - adjustPosition()]))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        when (viewType) {
            PRICING_STRUCTURE_HEADER_VIEW -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.flight_results_pricing_structure_header_cell, parent, false)
                return HeaderViewHolder(view as ViewGroup)
            }
            ALL_FLIGHTS_HEADER_VIEW -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.all_flights_header_cell, parent, false)
                return HeaderViewHolder(view as ViewGroup)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.flight_cell, parent, false)
                return FlightViewHolder(view as ViewGroup, parent.width)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        var viewType =
                when (position) {
                    0 -> PRICING_STRUCTURE_HEADER_VIEW
                    1 -> ALL_FLIGHTS_HEADER_VIEW
                    else -> ALL_FLIGHTS_VIEW
                }
        return viewType
    }

    inner class HeaderViewHolder(val root: ViewGroup) : RecyclerView.ViewHolder(root) {

    }

    inner open class FlightViewHolder(root: ViewGroup, val width: Int) : RecyclerView.ViewHolder(root), View.OnClickListener {

        val flightTimeTextView: TextView by root.bindView(R.id.flight_time_detail_text_view)
        val priceTextView: TextView by root.bindView(R.id.price_text_view)
        val flightDurationTextView: TextView by root.bindView(R.id.flight_duration_text_view)
        val flightLayoverWidget: FlightLayoverWidget by root.bindView(R.id.custom_flight_layover_widget)
        val flightAirlineWidget: FlightAirlineWidget by root.bindView(R.id.flight_airline_widget)
        val bestFlightView: ViewGroup by root.bindView(R.id.package_best_flight)

        init {
            itemView.setOnClickListener(this)
            bestFlightView.visibility = View.GONE
        }

        override fun onClick(view: View) {
            val flight: FlightLeg = getFlight(adapterPosition)
            flightSelectedSubject.onNext(flight)
        }

        fun bind(viewModel: PackageFlightViewModel) {
            viewModel.flightTimeObserver.subscribeText(flightTimeTextView)
            viewModel.priceObserver.subscribeText(priceTextView)
            viewModel.durationObserver.subscribeText(flightDurationTextView)
            viewModel.layoverObserver.subscribe { flight ->
                flightLayoverWidget.update(flight.flightSegments, flight.durationHour, flight.durationMinute, maxFlightDuration)
            }

            viewModel.airlineObserver.subscribe { airlines ->
                flightAirlineWidget.update(airlines)
            }
        }
    }

    private fun getFlight(rawAdapterPosition: Int): FlightLeg {
        return flights[rawAdapterPosition - adjustPosition()]
    }

    open fun adjustPosition(): Int {
        return 2
    }
}
