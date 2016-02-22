package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.Spinner
import com.expedia.bookings.R
import com.expedia.bookings.section.AssistanceTypeSpinnerAdapter
import com.expedia.bookings.section.SeatPreferenceSpinnerAdapter
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.traveler.TravelerEditText
import com.expedia.bookings.widget.traveler.TravelerEditTextWatcher
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.vm.traveler.TravelerAdvancedOptionsViewModel

class FlightTravelerAdvancedOptionsWidget(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    val redressNumber: TravelerEditText by bindView(R.id.redress_number)
    val assistancePreferenceSpinner: Spinner by bindView(R.id.edit_assistance_preference_spinner)
    val seatPreferenceSpinner: Spinner by bindView(R.id.edit_seat_preference_spinner)

    var viewModel: TravelerAdvancedOptionsViewModel by notNullAndObservable { vm ->
        vm.redressNumberSubject.subscribeText(redressNumber)
        vm.seatPreferenceSubject.subscribe { seatPref ->
            val adapter = seatPreferenceSpinner.adapter as SeatPreferenceSpinnerAdapter
            seatPreferenceSpinner.setSelection(adapter.getSeatPreferencePosition(seatPref))
        }

        vm.assistancePreferenceSubject.subscribe { assistancePref ->
            val adapter = assistancePreferenceSpinner.adapter as AssistanceTypeSpinnerAdapter
            assistancePreferenceSpinner.setSelection(adapter.getAssistanceTypePosition(assistancePref))
        }

        redressNumber.addTextChangedListener(TravelerEditTextWatcher(vm.redressNumberObserver, redressNumber))
    }

    init {
        View.inflate(context, R.layout.traveler_advanced_options_widget, this)
        orientation = VERTICAL

        val seatPreferenceAdapter = SeatPreferenceSpinnerAdapter(context, R.layout.material_spinner_item, R.layout.spinner_traveler_entry_dropdown_item)
        seatPreferenceSpinner.adapter = seatPreferenceAdapter
        seatPreferenceSpinner.onItemSelectedListener = SeatPreferenceItemSelectedListener()

        assistancePreferenceSpinner.adapter = AssistanceTypeSpinnerAdapter(context, R.layout.material_spinner_item, R.layout.spinner_traveler_entry_dropdown_2line_item)
        assistancePreferenceSpinner.onItemSelectedListener = AssistanceTypeSelectedListener()
    }

    private inner class SeatPreferenceItemSelectedListener() : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
            //do nothing
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val adapter = seatPreferenceSpinner.adapter as SeatPreferenceSpinnerAdapter
            viewModel.seatPreferenceObserver.onNext(adapter.getSeatPreference(position))
        }
    }

    private inner class AssistanceTypeSelectedListener() : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
            //do nothing
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val adapter = assistancePreferenceSpinner.adapter as AssistanceTypeSpinnerAdapter
            viewModel.assistancePreferenceObserver.onNext(adapter.getAssistanceType(position))
        }
    }
}