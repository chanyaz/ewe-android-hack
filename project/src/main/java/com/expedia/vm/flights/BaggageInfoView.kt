package com.expedia.vm.flights

import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.expedia.bookings.extensions.ObservableOld
import com.expedia.bookings.R
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.widget.DeprecatedProgressDialog
import com.expedia.util.notNullAndObservable
import com.squareup.phrase.Phrase

class BaggageInfoView(val context: Context) {

    lateinit var baggageInfoParentContainer: ViewGroup
    private val createLoader = DeprecatedProgressDialog(context)

    var baggageInfoViewModel: BaggageInfoViewModel by notNullAndObservable { vm ->
        vm.airlineNameSubject.subscribe {
            baggageInfoParentContainer.addView(populateChildView(context.getString(R.string.airline), it, false))
            setBaggageInfoAirlineMessage(it)
        }
        vm.showBaggageInfoDialogSubject.subscribe {
            createDialogBuilder(context).create().show()
        }

        ObservableOld.zip(vm.baggageChargeSubject, vm.doNotShowLastHorizontalLineSubject, { baggageCharge, doNotShowLastHorizontalLine ->
            object {
                val baggageChargePair = baggageCharge
                val doNotShowHorizontalLine = doNotShowLastHorizontalLine
            }
        }).subscribe {
            baggageInfoParentContainer.addView(populateChildView(it.baggageChargePair.first, it.baggageChargePair.second, it.doNotShowHorizontalLine))
        }
        vm.showLoaderSubject.subscribe { show ->
            if (show) {
                if (!createLoader.isShowing) {
                    createLoader.show()
                    createLoader.setCancelable(false)
                    createLoader.setContentView(R.layout.process_dialog_layout)
                    AccessibilityUtil.delayedFocusToView(createLoader.findViewById(R.id.progress_dialog_container), 0)
                    createLoader.findViewById<View>(R.id.progress_dialog_container).contentDescription = context.getString(R.string.spinner_text_baggage_info_fetching)
                    createLoader.findViewById<View>(R.id.progress_dialog_container).announceForAccessibility(context.getString(R.string.spinner_text_baggage_info_fetching))
                }
            } else {
                if (createLoader.isShowing) {
                    createLoader.dismiss()
                }
            }
        }
    }

    fun getBaggageInfo(flightLeg: FlightLeg) {
        baggageInfoParentContainer = View.inflate(context, R.layout.baggage_info_parent, null) as ViewGroup
        baggageInfoViewModel.baggageParamsSubject.onNext(flightLeg)
    }

    private fun setBaggageInfoAirlineMessage(airline: String) {
        var baggageInfoAirlineMessage = baggageInfoParentContainer.findViewById<TextView>(R.id.baggage_info_airline_message)
        baggageInfoAirlineMessage.text = Phrase.from(context.resources.getString(R.string.baggage_info_airline_message_TEMPLATE)).put("airline_name", airline).format()
    }

    private fun populateChildView(baggageKey: String, baggageValue: String, doNotShowHorizontalLine: Boolean): View {
        val baggageInfoChildContainer = View.inflate(context, R.layout.baggage_info_child, null)
        val baggageKeyView = baggageInfoChildContainer.findViewById<TextView>(R.id.baggage_fee_key)
        val horizontalLine = baggageInfoChildContainer.findViewById<View>(R.id.horizontal_line)
        val baggageInfoChildContainerLayout = baggageInfoChildContainer.findViewById<LinearLayout>(R.id.baggage_info_child_linear_container)
        val baggageValueView = baggageInfoChildContainer.findViewById<TextView>(R.id.baggage_fee_value)
        baggageKeyView.text = baggageKey
        baggageValueView.text = baggageValue
        if (doNotShowHorizontalLine) {
            var marginParams = baggageInfoChildContainerLayout.layoutParams as ViewGroup.MarginLayoutParams
            marginParams.bottomMargin = 0
            horizontalLine.visibility = View.GONE
        }
        return baggageInfoChildContainer
    }

    private fun createDialogBuilder(context: Context): AlertDialog.Builder {
        val builder = AlertDialog.Builder(context, android.R.style.Theme_Material_Light_Dialog_Alert)
        builder.setView(baggageInfoParentContainer)
        builder.setPositiveButton(context.getString(R.string.OK)) { dialog, _ -> dialog.dismiss() }
        builder.setNegativeButton(context.getString(R.string.view_details)) { _, _ ->
            baggageInfoViewModel.showBaggageInfoWebViewSubject.onNext(Unit)
        }
        return builder
    }
}
