package com.expedia.bookings.widget

import android.app.AlertDialog
import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import com.expedia.bookings.R
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.section.AssistanceTypeSpinnerAdapter
import com.expedia.bookings.section.SeatPreferenceSpinnerAdapter
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.traveler.TravelerEditText
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeEditText
import com.expedia.vm.traveler.TravelerAdvancedOptionsViewModel

class FlightTravelerAdvancedOptionsWidget(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    val travelerNumber: TravelerEditText by bindView(R.id.traveler_number)
    val redressNumber: TravelerEditText by bindView(R.id.redress_number)
    val assistancePreferenceSpinner: Spinner by bindView(R.id.edit_assistance_preference_spinner)
    val assistancePreferenceEditBox: EditText by bindView(R.id.edit_assistance_preference_button)
    val seatPreferenceSpinner: Spinner by bindView(R.id.edit_seat_preference_spinner)
    val seatPreferenceEditBox: EditText by bindView(R.id.edit_seat_preference_button)
    val materialFormTestEnabled = FeatureToggleUtil.isUserBucketedAndFeatureEnabled(context,
            AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms, R.string.preference_universal_checkout_material_forms)


    val travelerInfoDialog: AlertDialog by lazy {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(R.string.known_traveler_msg)
        builder.setCancelable(false)
        builder.setPositiveButton(context.getString(R.string.ok), { dialog, which ->
            dialog.dismiss()
        })
        builder.create()
    }

    val redressInfoDialog: AlertDialog by lazy {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(R.string.redress_number_msg)
        builder.setCancelable(false)
        builder.setPositiveButton(context.getString(R.string.ok), { dialog, which ->
            dialog.dismiss()
        })
        builder.create()
    }

    var viewModel: TravelerAdvancedOptionsViewModel by notNullAndObservable { vm ->

        if (PointOfSale.getPointOfSale().shouldShowKnownTravelerNumber()) {
            travelerNumber.viewModel = vm.travelerNumberViewModel
            vm.travelerNumberSubject.subscribeEditText(travelerNumber)
        }
        redressNumber.viewModel = vm.redressViewModel
        vm.redressNumberSubject.subscribeEditText(redressNumber)

        vm.seatPreferenceSubject.subscribe { seatPref ->
            if (materialFormTestEnabled) {
                val seatStringTemplate = context.getString(R.string.prefers_seat_colored_TEMPLATE2)
                seatPreferenceEditBox.setText(String.format(seatStringTemplate, Strings.capitalizeFirstLetter(seatPref.name)))
            }
            else {
                val seatAdapter = seatPreferenceSpinner.adapter as SeatPreferenceSpinnerAdapter
                seatPreferenceSpinner.setSelection(seatAdapter.getSeatPreferencePosition(seatPref))
            }
        }

        vm.assistancePreferenceSubject.subscribe { assist ->
            if (materialFormTestEnabled) {
                val assistanceAdapter = AssistanceTypeSpinnerAdapter(context)
                val assistanceString = assistanceAdapter.getItem(assistanceAdapter.getAssistanceTypePosition(assist))
                assistancePreferenceEditBox.setText(assistanceString)
            }
            else {
                val assistanceAdapter = assistancePreferenceSpinner.adapter as AssistanceTypeSpinnerAdapter
                assistancePreferenceSpinner.setSelection(assistanceAdapter.getAssistanceTypePosition(assist))
            }
        }
    }

    init {
        View.inflate(context, if (materialFormTestEnabled) R.layout.material_traveler_advanced_options_widget
        else R.layout.traveler_advanced_options_widget, this)
        orientation = VERTICAL

        if (PointOfSale.getPointOfSale().shouldShowKnownTravelerNumber()) {
            travelerNumber.visibility = View.VISIBLE
            travelerNumber.setOnTouchListener { view, motionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_UP) {
                    if (motionEvent.rawX >= travelerNumber.right - travelerNumber.totalPaddingRight) {
                        travelerInfoDialog.show()
                        true
                    }
                }
                false
            }
        }

        redressNumber.setOnTouchListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                if (motionEvent.rawX >= redressNumber.right - redressNumber.totalPaddingRight) {
                    redressInfoDialog.show()
                    true
                }
            }
            false
        }

        if (materialFormTestEnabled) {
            assistancePreferenceEditBox.setOnClickListener {
                showAssistanceTypeDialog()
            }
            seatPreferenceEditBox.setOnClickListener {
                showSeatPreferanceDialog()
            }
        } else {
            assistancePreferenceSpinner.adapter = AssistanceTypeSpinnerAdapter(context, R.layout.material_spinner_item, R.layout.spinner_traveler_entry_dropdown_2line_item)
            assistancePreferenceSpinner.onItemSelectedListener = AssistanceTypeSelectedListener()

            val seatPreferenceAdapter = SeatPreferenceSpinnerAdapter(context, R.layout.material_spinner_item)
            seatPreferenceAdapter.setFormatString(context.getString(R.string.prefers_seat_colored_TEMPLATE2))
            seatPreferenceSpinner.adapter = seatPreferenceAdapter
            seatPreferenceSpinner.onItemSelectedListener = SeatPreferenceItemSelectedListener()
        }
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
            adapter.setSelectedIndex(position);
            viewModel.assistancePreferenceObserver.onNext(adapter.getAssistanceType(position))
        }
    }

    private fun showAssistanceTypeDialog() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.resources.getString(R.string.special_assistance))
        val assistanceAdapter = AssistanceTypeSpinnerAdapter(context, R.layout.material_item)

        builder.setAdapter(assistanceAdapter) { dialog, position ->
            viewModel.assistancePreferenceSubject.onNext(assistanceAdapter.getAssistanceType(position))
            viewModel.assistancePreferenceObserver.onNext(assistanceAdapter.getAssistanceType(position))
        }

        val alert = builder.create()
        alert.listView.divider = (ContextCompat.getDrawable(context, R.drawable.divider_row_filter_refinement))
        alert.show()
    }

    private fun showSeatPreferanceDialog() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.resources.getString(R.string.seat_preference))
        val seatStringTemplate = context.getString(R.string.prefers_seat_colored_TEMPLATE2)
        val preferenceArray = arrayOf(String.format(seatStringTemplate, context.getString(R.string.aisle)),
                String.format(seatStringTemplate, context.getString(R.string.window)))

        builder.setItems(preferenceArray) { dialog, position ->
            val preference = if (position == 0) Traveler.SeatPreference.AISLE else Traveler.SeatPreference.WINDOW
            viewModel.seatPreferenceObserver.onNext(preference)
            viewModel.seatPreferenceSubject.onNext(preference)
        }

        val alert = builder.create()
        alert.listView.divider = (ContextCompat.getDrawable(context, R.drawable.divider_row_filter_refinement))
        alert.show()
    }
}