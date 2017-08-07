package com.expedia.bookings.widget.traveler

import android.app.AlertDialog
import android.content.Context
import android.view.ViewGroup
import android.support.v7.widget.RecyclerView
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FFNSpinnerAdapter
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.vm.traveler.FlightTravelerFrequentFlyerItemViewModel

class FrequentFlyerViewHolder(val root: ViewGroup, private val vm: FlightTravelerFrequentFlyerItemViewModel, context: Context) : RecyclerView.ViewHolder(root) {
    val frequentFlyerProgram: TravelerEditText by bindView(R.id.edit_frequent_flyer_program_name)
    val frequentFlyerNumber: TravelerEditText by bindView(R.id.edit_frequent_flyer_number)
    val frequentFlyerNameTitle: TextView by bindView(R.id.frequent_flyer_program_card_title)

    var viewModel: FlightTravelerFrequentFlyerItemViewModel by notNullAndObservable {
        frequentFlyerDialog.show()
    }

    val frequentFlyerAdapter : FFNSpinnerAdapter by lazy {
        val adapter = FFNSpinnerAdapter(context, R.layout.simple_spinner_item)
        adapter.currentPosition = adapter.getPositionFromName(viewModel.frequentFlyerProgramObservable.value)
        adapter
    }

    val frequentFlyerDialog: AlertDialog by lazy {
        val builder = AlertDialog.Builder(context)
        builder.setSingleChoiceItems(frequentFlyerAdapter, frequentFlyerAdapter.currentPosition, { dialogInterface, position ->
            val airlineName = frequentFlyerAdapter.getAirlineName(position)
            val airlineCode = frequentFlyerAdapter.getFFNNumber(position)
            viewModel.frequentFlyerProgramObservable.onNext(airlineName)
            viewModel.frequentFlyerNumberObservable.onNext(airlineCode.toString())
            dialogInterface.dismiss()
        })

        builder.create()
    }

    fun bind(frequentFlyerCard: FrequentFlyerCard) {
        vm.bind(frequentFlyerCard)
        frequentFlyerNameTitle.text = vm.getTitle()
        vm.frequentFlyerNumberObservable.subscribeText(frequentFlyerNumber)
        vm.frequentFlyerProgramObservable.subscribeText(frequentFlyerProgram)
    }
}