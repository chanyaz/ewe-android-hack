package com.expedia.bookings.widget

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.data.packages.FlightLeg
import com.expedia.bookings.utils.bindView
import com.expedia.util.subscribeText
import rx.subjects.BehaviorSubject
import java.util.ArrayList
import kotlin.collections.emptyList

public class FlightListAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var flights: List<FlightLeg> = emptyList()
    val resultsSubject = BehaviorSubject.create<List<FlightLeg>>()
    var loading = true


    init {
        resultsSubject.subscribe {
            loading = false
            flights = ArrayList(it)
            notifyDataSetChanged()
        }

    }
    override fun getItemCount(): Int {
        return flights.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        if (holder is FlightListAdapter.FlightViewHolder) {
            holder.bind(FlightViewModel(holder.itemView.context, flights.get(position)))

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.flight_cell, parent, false)
        return FlightViewHolder(view as ViewGroup, parent.width)
    }

    public inner class FlightViewHolder(root: ViewGroup, val width: Int) : RecyclerView.ViewHolder(root), View.OnClickListener {

//        val airlineContainer: LinearLayout by root.bindView(R.id.airline_container)
        val airlineTextView: TextView by root.bindView(R.id.airline_text_view)
//        val priceTextView: TextView by root.bindView(R.id.price_text_view)

        override fun onClick(p0: View?) {

        }

        public fun bind(viewModel: FlightViewModel) {
            viewModel.airlineObserver.subscribeText(airlineTextView)
        }
    }
}
