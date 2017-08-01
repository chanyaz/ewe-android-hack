package com.expedia.bookings.widget.traveler

import android.support.design.widget.TextInputLayout
import android.view.ViewGroup
import android.support.v7.widget.RecyclerView
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TelephoneSpinner
import com.expedia.bookings.widget.TextView
import com.expedia.vm.traveler.FlightTravelerFrequentFlyerItemViewModel

class FrequentFlyerViewHolder(val root: ViewGroup, private val vm: FlightTravelerFrequentFlyerItemViewModel) : RecyclerView.ViewHolder(root) {
//    TODO: set up spinner and input
//    val frequentFlyerNameSpinner: TelephoneSpinner by root.bindView(R.id.edit_frequent_flyer_program_spinner)
//    val frequentFlyerNumberInput: TextInputLayout by root.bindView(R.id.traveler_frequent_flyer_program_number)
    val frequentFlyerNameTitle: TextView by root.bindView(R.id.frequent_flyer_program_name_title)

    fun bind(frequentFlyerCard: FrequentFlyerCard) {
        vm.bind(frequentFlyerCard)
        frequentFlyerNameTitle.text = vm.getTitle()
    }
}