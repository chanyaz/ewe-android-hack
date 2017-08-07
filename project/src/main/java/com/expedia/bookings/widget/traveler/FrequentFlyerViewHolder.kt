package com.expedia.bookings.widget.traveler

import android.app.AlertDialog
import android.content.Context
import android.view.ViewGroup
import android.support.v7.widget.RecyclerView
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FFNSpinnerAdapter
import com.expedia.bookings.widget.TextView
import com.expedia.vm.traveler.FlightTravelerFrequentFlyerItemViewModel

class FrequentFlyerViewHolder(val root: ViewGroup, private val vm: FlightTravelerFrequentFlyerItemViewModel, context: Context) : RecyclerView.ViewHolder(root) {
//    TODO: set up spinner and input
//    val frequentFlyerNameSpinner: TravelerEditText by bindView(R.id.frequent_flyer_program_name)
//    val frequentFlyerNumberInput: TravelerEditText by bindView(R.id.traveler_frequent_flyer_program_number)
    val frequentFlyerNameTitle: TextView by bindView(R.id.frequent_flyer_program_name_title)

    val frequentFlyerAdapter : FFNSpinnerAdapter by lazy {
        val adapter = FFNSpinnerAdapter(context, R.layout.simple_spinner_item)

        adapter
    }

    val frequentFlyerDialog: AlertDialog by lazy {
        val builder = AlertDialog.Builder(context)
        builder.setSingleChoiceItems(frequentFlyerAdapter, frequentFlyerAdapter.currentPosition, { dialogInterface, position ->
            val airlineCode = frequentFlyerAdapter.getFFNNumber(position)



            dialogInterface.dismiss()
        })

        builder.create()
    }

    fun bind(frequentFlyerCard: FrequentFlyerCard) {
        vm.bind(frequentFlyerCard)
        frequentFlyerNameTitle.text = vm.getTitle()
    }
}