package com.expedia.bookings.widget

import android.content.Context
import android.support.v7.app.AlertDialog
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.tracking.flight.FlightsV2Tracking
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.widget.shared.SearchInputTextView
import com.squareup.phrase.Phrase

class FlightCabinClassWidget(context: Context, attrs: AttributeSet?) : SearchInputTextView(context, attrs) {


    init {
        setOnClickListener {
            showFlightCabinClassDialog()
        }
    }

    val flightCabinClassDialogView: View by lazy {
        val view = LayoutInflater.from(context).inflate(R.layout.widget_flight_cabin_class_search, null)
        view
    }

    val flightCabinClassView: FlightCabinClassPickerView by lazy {
        val flightClassView = flightCabinClassDialogView.findViewById<FlightCabinClassPickerView>(R.id.flight_class_view)
        flightClassView.viewmodel.flightCabinClassObservable.subscribe { cabinClass ->
            val cabinClassName = context.resources.getString(cabinClass.resId)
            this.contentDescription = Phrase.from(context.resources.getString(R.string.select_preferred_flight_class_cont_desc_TEMPLATE)).
                    put("seatingclass", cabinClassName).format().toString()
            this.text = cabinClassName
            flightClassView.viewmodel.flightSelectedCabinClassIdObservable.onNext(flightClassView.getIdByClass(cabinClass))
        }
        flightClassView.viewmodel.flightSelectedCabinClassIdObservable.subscribe { cabinClassId ->
            flightClassView.radioGroup.check(cabinClassId)
        }
        flightClassView
    }

    fun showFlightCabinClassDialog() {
        dialog.show()
        AccessibilityUtil.delayedFocusToView(dialog.findViewById(R.id.flight_cabin_class_radioGroup)!!, 250)
    }

    val dialog: AlertDialog by lazy {
        val builder = AlertDialog.Builder(context, R.style.Theme_AlertDialog)
        builder.setView(flightCabinClassDialogView)
        builder.setPositiveButton(context.getString(R.string.DONE), { dialog, which ->
            flightCabinClassView.viewmodel.flightCabinClassObservable.onNext(flightCabinClassView.getSelectedClass())
            FlightsV2Tracking.trackFlightCabinClassSelect(flightCabinClassView.getSelectedClass().name)
            dialog.dismiss()
        })
        val dialog: AlertDialog = builder.create()
        dialog.setCancelable(true)
        dialog.setOnDismissListener {
            flightCabinClassView.viewmodel.flightSelectedCabinClassIdObservable.onNext(flightCabinClassView.getIdByClass(flightCabinClassView.viewmodel.flightCabinClassObservable.value))
        }
        dialog
    }
}
