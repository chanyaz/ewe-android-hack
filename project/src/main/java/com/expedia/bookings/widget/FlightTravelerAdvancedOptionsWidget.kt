package com.expedia.bookings.widget

import android.app.AlertDialog
import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.Spinner
import com.expedia.bookings.R
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.section.AssistanceTypeSpinnerAdapter
import com.expedia.bookings.section.SeatPreferenceSpinnerAdapter
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.traveler.TravelerEditText
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeEditText
import com.expedia.vm.traveler.TravelerAdvancedOptionsViewModel

class FlightTravelerAdvancedOptionsWidget(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    val travelerNumber: TravelerEditText by bindView(R.id.traveler_number)
    val redressNumber: TravelerEditText by bindView(R.id.redress_number)
    val assistancePreferenceSpinner: Spinner by bindView(R.id.edit_assistance_preference_spinner)
    val seatPreferenceSpinner: Spinner by bindView(R.id.edit_seat_preference_spinner)

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
        if (FeatureToggleUtil.isFeatureEnabled(context, R.string.preference_enable_checkout_traveler_number)
                && PointOfSale.getPointOfSale().shouldShowKnownTravelerNumber()) {
            travelerNumber.viewModel = vm.travelerNumberViewModel
            vm.travelerNumberSubject.subscribeEditText(travelerNumber)

        }
        redressNumber.viewModel = vm.redressViewModel
        vm.redressNumberSubject.subscribeEditText(redressNumber)

        vm.seatPreferenceSubject.subscribe { seatPref ->
            val seatAdapter = seatPreferenceSpinner.adapter as SeatPreferenceSpinnerAdapter
            seatPreferenceSpinner.setSelection(seatAdapter.getSeatPreferencePosition(seatPref))
        }

        vm.assistancePreferenceSubject.subscribe { assist ->
            val assistanceAdapter = assistancePreferenceSpinner.adapter as AssistanceTypeSpinnerAdapter
            assistancePreferenceSpinner.setSelection(assistanceAdapter.getAssistanceTypePosition(assist))
        }
    }

    init {
        View.inflate(context, R.layout.traveler_advanced_options_widget, this)
        orientation = VERTICAL

        if (FeatureToggleUtil.isFeatureEnabled(context, R.string.preference_enable_checkout_traveler_number)
                && PointOfSale.getPointOfSale().shouldShowKnownTravelerNumber()) {
            // make visible and drawable in xml when removing feature toggle
            travelerNumber.visibility = View.VISIBLE
            val drawables = travelerNumber.compoundDrawables
            travelerNumber.setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1],
                    ContextCompat.getDrawable(context, R.drawable.ic_checkout_info), drawables[2])
            redressNumber.setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1],
                    ContextCompat.getDrawable(context, R.drawable.ic_checkout_info), drawables[2])
            travelerNumber.setOnTouchListener { view, motionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_UP) {
                    if (motionEvent.rawX >= travelerNumber.right - travelerNumber.totalPaddingRight) {
                        travelerInfoDialog.show()
                        true
                    }
                }
                false
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

        }

        val seatPreferenceAdapter = SeatPreferenceSpinnerAdapter(context, R.layout.material_spinner_item, R.layout.spinner_dropdown_item)
        seatPreferenceAdapter.setFormatString(context.getString(R.string.prefers_seat_colored_TEMPLATE2))
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
            adapter.setSelectedIndex(position);
            viewModel.assistancePreferenceObserver.onNext(adapter.getAssistanceType(position))
        }
    }
}