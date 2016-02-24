package com.expedia.bookings.widget

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.packages.FlightLeg
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.packages.FlightAirlineWidget
import com.expedia.bookings.widget.packages.FlightLayoverWidget
import com.expedia.util.subscribeText
import com.expedia.vm.PackageFlightViewModel
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.ArrayList
import kotlin.collections.emptyList

public class PackageFlightListAdapter(val flightSelectedSubject: PublishSubject<FlightLeg>, val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var flights: List<FlightLeg> = emptyList()
    var maxFlightDuration = 0
    val resultsSubject = BehaviorSubject.create<List<FlightLeg>>()
    var loading = true

    init {
        resultsSubject.subscribe {
            loading = false
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
        return flights.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        if (holder is PackageFlightListAdapter.FlightViewHolder) {
            holder.bind(PackageFlightViewModel(holder.itemView.context, flights.get(position)))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.flight_cell, parent, false)
        return FlightViewHolder(view as ViewGroup, parent.width)
    }

    public inner class FlightViewHolder(root: ViewGroup, val width: Int) : RecyclerView.ViewHolder(root), View.OnClickListener {

        val flightTimeTextView: TextView by root.bindView(R.id.flight_time_detail_text_view)
        val priceTextView: TextView by root.bindView(R.id.price_text_view)
        val flightDurationTextView: TextView by root.bindView(R.id.flight_duration_text_view)
        val flightLayoverWidget: FlightLayoverWidget by root.bindView(R.id.custom_flight_layover_widget)
        val flightAirlineWidget: FlightAirlineWidget by root.bindView(R.id.flight_airline_widget)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            val flight: FlightLeg = getFlight(adapterPosition)
            flightSelectedSubject.onNext(flight)
        }

        public fun bind(viewModel: PackageFlightViewModel) {
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
        return flights.get(rawAdapterPosition)
    }
}
