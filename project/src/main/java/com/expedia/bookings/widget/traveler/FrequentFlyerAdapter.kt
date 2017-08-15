package com.expedia.bookings.widget.traveler

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import com.expedia.bookings.R
import com.expedia.vm.traveler.FlightTravelerFrequentFlyerItemViewModel
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import rx.subjects.BehaviorSubject
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList

class FrequentFlyerAdapter(val traveler: Traveler) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private var frequentFlyerCards: List<FrequentFlyerCard> = emptyList()
    val frequentFlyerPlans = BehaviorSubject.create<FlightCreateTripResponse.FrequentFlyerPlans>()

    fun setFrequentFlyerCards(frequentFlyerCard: List<FrequentFlyerCard>) {
        this.frequentFlyerCards = frequentFlyerCard
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return frequentFlyerCards.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.frequent_flyer_program_card_layout, parent, false)
        val vm = FlightTravelerFrequentFlyerItemViewModel(traveler)
        setUpFrequentFlyerPlans(frequentFlyerPlans.value, vm)
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

    private fun setUpFrequentFlyerPlans (frequentFlyerPlans: FlightCreateTripResponse.FrequentFlyerPlans, vm: FlightTravelerFrequentFlyerItemViewModel) {
        frequentFlyerPlans.allFrequentFlyerPlans.forEach {
            val formattedAirlineCode = it.airlineCode.replace(" ", "")
            vm.allFrequentFlyerPlans.put(formattedAirlineCode, it)
            vm.allAirlineNames.add(it.frequentFlyerPlanName)

        }

        frequentFlyerPlans.enrolledFrequentFlyerPlans.forEach {
            val formattedAirlineCode = it.airlineCode.replace(" ","")
            vm.enrolledPlans.put(formattedAirlineCode, it)
        }
    }
}

class FrequentFlyerCard(airlineName: String, airlineCode: String) {
    val airlineName = airlineName
    val airlineCode = airlineCode
}
