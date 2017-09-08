package com.expedia.bookings.widget.traveler

import com.expedia.vm.FrequentFlyerProgramViewModel

import android.content.Context
import android.app.AlertDialog
import android.view.ViewGroup
import android.support.v7.widget.RecyclerView
import com.expedia.bookings.R
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FrequentFlyerDialogAdapter
import com.expedia.bookings.widget.TextView
import com.expedia.util.subscribeText
import com.expedia.vm.traveler.FlightTravelerFrequentFlyerItemViewModel

class FrequentFlyerViewHolder(val root: ViewGroup, private val vm: FlightTravelerFrequentFlyerItemViewModel, context: Context) : RecyclerView.ViewHolder(root) {
    val frequentFlyerProgram: TravelerEditText by bindView(R.id.edit_frequent_flyer_program_name)
    val frequentFlyerNameTitle: TextView by bindView(R.id.frequent_flyer_program_card_title)
    val frequentFlyerNumberInput: TravelerEditText by bindView(R.id.edit_frequent_flyer_number)

    val frequentFlyerAdapter: FrequentFlyerDialogAdapter by lazy {
        val adapter = FrequentFlyerDialogAdapter(context, R.layout.material_item,
                R.layout.frequent_flyer_enrolled_list_item,
                vm.allFrequentFlyerPlans,
                vm.enrolledPlans,
                vm.allAirlineCodes,
                frequentFlyerProgram.text.toString())
        adapter
    }

    val frequentFlyerDialog: AlertDialog by lazy {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.frequent_flyer_programs)
        builder.setSingleChoiceItems(frequentFlyerAdapter, frequentFlyerAdapter.currentPosition, { dialogInterface, position ->
            val airlineName = frequentFlyerAdapter.getFrequentFlyerProgram(position)
            val airlineNumber = frequentFlyerAdapter.getFrequentFlyerNumber(position)
            vm.frequentFlyerProgramObservable.onNext(airlineName)
            vm.frequentFlyerNumberObservable.onNext(airlineNumber)
            frequentFlyerAdapter.currentPosition = position
            dialogInterface.dismiss()
        })

        builder.create()
    }

    init {
        frequentFlyerProgram.viewModel = FrequentFlyerProgramViewModel()
        frequentFlyerNumberInput.viewModel = vm.frequentFlyerProgramNumberViewModel
        frequentFlyerProgram.setOnClickListener {
            frequentFlyerDialog.show()
        }
        vm.enrolledFrequentFlyerPlansObservable.subscribe { enrolledPlans ->
            frequentFlyerAdapter.enrolledFrequentFlyerPlans = enrolledPlans
            frequentFlyerAdapter.notifyDataSetChanged()
        }
        vm.frequentFlyerProgramObservable.subscribeText(frequentFlyerProgram)
        vm.frequentFlyerNumberObservable.subscribeText(frequentFlyerNumberInput)
        frequentFlyerProgram.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                v.performClick()
                Ui.hideKeyboard(v)
            }
        }
        frequentFlyerNumberInput.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                v.performClick()
                Ui.hideKeyboard(v)
            }
        }
    }

    fun bind(frequentFlyerCard: FrequentFlyerCard) {
        vm.bind(frequentFlyerCard)
        frequentFlyerNameTitle.text = vm.getTitle()
    }
}