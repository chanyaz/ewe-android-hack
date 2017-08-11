package com.expedia.bookings.widget.traveler

import android.app.AlertDialog
import android.content.Context
import android.view.ViewGroup
import android.support.v7.widget.RecyclerView
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FrequentFlyerSpinnerAdapter
import com.expedia.bookings.widget.TextView
import com.expedia.util.subscribeText
import com.expedia.vm.FrequentFlyerNumberViewModel
import com.expedia.vm.FrequentFlyerProgramViewModel
import com.expedia.vm.traveler.FlightTravelerFrequentFlyerItemViewModel

class FrequentFlyerViewHolder(val root: ViewGroup, private val vm: FlightTravelerFrequentFlyerItemViewModel, context: Context) : RecyclerView.ViewHolder(root) {
    val frequentFlyerProgram: TravelerEditText by bindView(R.id.edit_frequent_flyer_program_name)
    val frequentFlyerNumber: TravelerEditText by bindView(R.id.edit_frequent_flyer_number)
    val frequentFlyerNameTitle: TextView by bindView(R.id.frequent_flyer_program_card_title)

    val frequentFlyerAdapter : FrequentFlyerSpinnerAdapter by lazy {
        val adapter = FrequentFlyerSpinnerAdapter(context, R.layout.material_item, R.layout.simple_spinner_dropdown_item)
        adapter.currentPosition = adapter.getPositionFromName(vm.frequentFlyerProgramObservable.toString())
        adapter
    }

    val frequentFlyerDialog: AlertDialog by lazy {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.resources.getString(R.string.frequent_flyer_my_programs))
        builder.setSingleChoiceItems(frequentFlyerAdapter, frequentFlyerAdapter.currentPosition, { dialogInterface, position ->
            val airlineName = frequentFlyerAdapter.getFrequentFlyerProgram(position)
            val airlineCode = frequentFlyerAdapter.getFrequentFlyerNumber(position)
            vm.frequentFlyerProgramObservable.onNext(airlineName)
            vm.frequentFlyerNumberObservable.onNext(airlineCode.toString())
            dialogInterface.dismiss()
        })

        builder.create()
    }

    init {
        frequentFlyerProgram.viewModel = FrequentFlyerProgramViewModel()
        frequentFlyerNumber.viewModel = FrequentFlyerNumberViewModel()
        frequentFlyerProgram.setOnClickListener {
            frequentFlyerDialog.show()
        }
        vm.frequentFlyerProgramObservable.subscribeText(frequentFlyerProgram)
        vm.frequentFlyerNumberObservable.subscribeText(frequentFlyerNumber)
    }

    fun bind(frequentFlyerCard: FrequentFlyerCard) {
        vm.bind(frequentFlyerCard)
        frequentFlyerNameTitle.text = vm.getTitle()
    }
}