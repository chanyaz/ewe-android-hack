package com.expedia.bookings.widget.traveler

import com.expedia.vm.FrequentFlyerProgramViewModel

import android.content.Context
import android.app.AlertDialog
import android.view.ViewGroup
import android.support.v7.widget.RecyclerView
import com.expedia.bookings.R
import com.expedia.bookings.data.flights.FrequentFlyerCard
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FrequentFlyerDialogAdapter
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.vm.traveler.FlightTravelerFrequentFlyerItemViewModel

class FrequentFlyerViewHolder(val root: ViewGroup) : RecyclerView.ViewHolder(root) {
    val frequentFlyerProgram: TravelerEditText by bindView(R.id.edit_frequent_flyer_program_name)
    val frequentFlyerNameTitle: TextView by bindView(R.id.frequent_flyer_program_card_title)
    val frequentFlyerNumberInput: TravelerEditText by bindView(R.id.edit_frequent_flyer_number)
    val context: Context = root.context

    var viewModel: FlightTravelerFrequentFlyerItemViewModel by notNullAndObservable {
        viewModel.enrolledFrequentFlyerPlansObservable.subscribe { enrolledPlans ->
            frequentFlyerDialogAdapter.enrolledFrequentFlyerPlans = enrolledPlans
            frequentFlyerDialogAdapter.notifyDataSetChanged()
        }
        viewModel.frequentFlyerProgramObservable.subscribeText(frequentFlyerProgram)
        viewModel.frequentFlyerNumberObservable.subscribeText(frequentFlyerNumberInput)
    }

    val frequentFlyerDialogAdapter: FrequentFlyerDialogAdapter by lazy {
        val adapter = FrequentFlyerDialogAdapter(context, R.layout.material_item,
                R.layout.frequent_flyer_enrolled_list_item,
                viewModel.allFrequentFlyerPlans,
                viewModel.enrolledPlans,
                viewModel.allAirlineCodes,
                frequentFlyerProgram.text.toString())
        adapter
    }

    val frequentFlyerDialog: AlertDialog by lazy {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.frequent_flyer_programs)
        builder.setSingleChoiceItems(frequentFlyerDialogAdapter, frequentFlyerDialogAdapter.currentPosition, { dialogInterface, position ->
            val airlineName = frequentFlyerDialogAdapter.getFrequentFlyerProgram(position)
            val airlineNumber = frequentFlyerDialogAdapter.getFrequentFlyerNumber(position)
            viewModel.frequentFlyerProgramObservable.onNext(airlineName)
            viewModel.frequentFlyerNumberObservable.onNext(airlineNumber)
            frequentFlyerDialogAdapter.currentPosition = position
            dialogInterface.dismiss()
        })

        builder.create()
    }

    init {
        frequentFlyerProgram.viewModel = FrequentFlyerProgramViewModel()
        frequentFlyerProgram.setOnClickListener {
            frequentFlyerDialog.show()
        }
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

    fun setViewHolderViewModel(viewModel: FlightTravelerFrequentFlyerItemViewModel) {
        this.viewModel = viewModel
        frequentFlyerNumberInput.viewModel = viewModel.frequentFlyerProgramNumberViewModel
    }

    fun bind(frequentFlyerCard: FrequentFlyerCard) {
        viewModel.bind(frequentFlyerCard)
        frequentFlyerNumberInput.viewModel = viewModel.frequentFlyerProgramNumberViewModel
        frequentFlyerNameTitle.text = viewModel.getTitle()
    }
}