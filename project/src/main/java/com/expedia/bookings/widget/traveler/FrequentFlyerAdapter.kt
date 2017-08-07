package com.expedia.bookings.widget.traveler

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import com.expedia.bookings.R
import com.expedia.vm.traveler.FlightTravelerFrequentFlyerItemViewModel

class FrequentFlyerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private var frequentFlyerCards: List<FrequentFlyerCard> = emptyList()

    fun setFrequentFlyerCards(frequentFlyerCard: List<FrequentFlyerCard>) {
        this.frequentFlyerCards = frequentFlyerCard
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return frequentFlyerCards.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.frequent_flyer_program_card_layout, parent, false)
        val vm = FlightTravelerFrequentFlyerItemViewModel()
        return FrequentFlyerViewHolder(view as ViewGroup, vm, parent.context)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        when (holder) {
            is FrequentFlyerViewHolder -> {
                val frequentFlyerCard = frequentFlyerCards[position]
                holder.bind(frequentFlyerCard)
            }
        }
    }

    override fun getFilter(): Filter? {
        return filter
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}

class FrequentFlyerCard {
    val airlineName = "Alaska Airlines"
}
