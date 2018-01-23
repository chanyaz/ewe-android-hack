package com.expedia.bookings.widget

import android.app.AlertDialog
import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.section.AssistanceTypeSpinnerAdapter
import com.expedia.bookings.section.SeatPreferenceSpinnerAdapter
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.accessibility.AccessibleEditTextForSpinner
import com.expedia.bookings.widget.traveler.TravelerEditText
import com.expedia.util.notNullAndObservable
import com.expedia.vm.traveler.TravelerAdvancedOptionsViewModel

class FlightTravelerAdvancedOptionsWidget(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    val travelerNumber: TravelerEditText by bindView(R.id.traveler_number)
    val travelerNumberIcon: ImageView by bindView(R.id.traveler_number_icon)
    val redressNumber: TravelerEditText by bindView(R.id.redress_number)
    val redressNumberIcon: ImageView by bindView(R.id.redress_number_icon)
    val assistancePreferenceEditBox: AccessibleEditTextForSpinner by bindView(R.id.edit_assistance_preference_button)
    val seatPreferenceEditBox: AccessibleEditTextForSpinner by bindView(R.id.edit_seat_preference_button)

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

    val seatPreferenceAdapter: SeatPreferenceSpinnerAdapter by lazy {
        val adapter = SeatPreferenceSpinnerAdapter(context, R.layout.material_item)
        adapter.setFormatString(context.getString(R.string.prefers_seat_colored_TEMPLATE2))
        adapter
    }

    val seatPreferenceDialog: AlertDialog by lazy {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.resources.getString(R.string.seat_preference))

        builder.setSingleChoiceItems(seatPreferenceAdapter, seatPreferenceAdapter.currentPosition) { dialog, position ->
            viewModel.seatPreferenceObserver.onNext(seatPreferenceAdapter.getSeatPreference(position))
            viewModel.seatPreferenceSubject.onNext(seatPreferenceAdapter.getSeatPreference(position))
            dialog.dismiss()
        }

        val alert = builder.create()
        alert.listView.divider = (ContextCompat.getDrawable(context, R.drawable.divider_row_filter_refinement))
        alert
    }

    val assistanceAdapter: AssistanceTypeSpinnerAdapter by lazy {
        val adapter = AssistanceTypeSpinnerAdapter(context, R.layout.material_item)
        adapter
    }

    val assistanceDialog: AlertDialog by lazy {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.resources.getString(R.string.special_assistance))

        builder.setSingleChoiceItems(assistanceAdapter, assistanceAdapter.currentPosition) { dialog, position ->
            viewModel.assistancePreferenceObserver.onNext(assistanceAdapter.getAssistanceType(position))
            viewModel.assistancePreferenceSubject.onNext(assistanceAdapter.getAssistanceType(position))
            dialog.dismiss()
        }

        val alert = builder.create()
        alert.listView.divider = (ContextCompat.getDrawable(context, R.drawable.divider_row_filter_refinement))
        alert
    }

    var viewModel: TravelerAdvancedOptionsViewModel by notNullAndObservable { vm ->

        if (PointOfSale.getPointOfSale().shouldShowKnownTravelerNumber()) {
            travelerNumber.viewModel = vm.travelerNumberViewModel
        }
        redressNumber.viewModel = vm.redressViewModel

        vm.seatPreferenceSubject.subscribe { seatPref ->
            seatPreferenceAdapter.currentPosition = seatPreferenceAdapter.getSeatPreferencePosition(seatPref)
            val seatStringTemplate = context.getString(R.string.prefers_seat_colored_TEMPLATE2)
            seatPreferenceEditBox.setText(String.format(seatStringTemplate, Strings.capitalizeFirstLetter(seatPref.name)))
        }

        vm.assistancePreferenceSubject.subscribe { assist ->
            assistanceAdapter.currentPosition = assistanceAdapter.getAssistanceTypePosition(assist)
            val assistanceString = assistanceAdapter.getItem(assistanceAdapter.getAssistanceTypePosition(assist))
            assistancePreferenceEditBox.setText(assistanceString)
        }
    }

    init {
        View.inflate(context, R.layout.material_traveler_advanced_options_widget, this)
        orientation = VERTICAL

        if (PointOfSale.getPointOfSale().shouldShowKnownTravelerNumber()) {
            travelerNumber.visibility = View.VISIBLE
            travelerNumberIcon.visibility = View.VISIBLE
            travelerNumberIcon.setOnClickListener {
                travelerInfoDialog.show()
            }
        }

        redressNumberIcon.setOnClickListener {
            redressInfoDialog.show()
        }
        assistancePreferenceEditBox.setOnClickListener {
            assistanceDialog.show()
        }
        seatPreferenceEditBox.setOnClickListener {
            seatPreferenceDialog.show()
        }
    }
}
