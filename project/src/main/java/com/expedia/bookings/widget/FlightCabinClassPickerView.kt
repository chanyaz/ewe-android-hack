package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.R
import com.expedia.bookings.data.flights.FlightServiceClassType
import com.expedia.vm.flights.FlightCabinClassViewModel

class FlightCabinClassPickerView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    val radioGroup: RadioGroup by bindView(R.id.flight_cabin_class_radioGroup)
    val firstClassRadioButton: RadioButton by bindView(R.id.first_class)
    val businessClassRadioButton: RadioButton by bindView(R.id.business_class)
    val premiumEcoClassRadioButton: RadioButton by bindView(R.id.premium_economy)
    val economyClassRadioButton: RadioButton by bindView(R.id.economy_class)

    var viewmodel = FlightCabinClassViewModel()

    init {
        View.inflate(context, R.layout.widget_flight_class_picker, this)
    }

    fun getSelectedClass(): FlightServiceClassType.CabinCode {
        when (radioGroup.checkedRadioButtonId) {
            firstClassRadioButton.id -> return FlightServiceClassType.CabinCode.FIRST
            businessClassRadioButton.id -> return FlightServiceClassType.CabinCode.BUSINESS
            premiumEcoClassRadioButton.id -> return FlightServiceClassType.CabinCode.PREMIUM_COACH
            economyClassRadioButton.id -> return FlightServiceClassType.CabinCode.COACH
            else -> throw RuntimeException("Radio button id unknown : " + radioGroup.checkedRadioButtonId)
        }
    }

    fun getIdByClass(cabinCode: FlightServiceClassType.CabinCode): Int {
        when (cabinCode) {
            FlightServiceClassType.CabinCode.FIRST -> return firstClassRadioButton.id
            FlightServiceClassType.CabinCode.BUSINESS -> return businessClassRadioButton.id
            FlightServiceClassType.CabinCode.PREMIUM_COACH -> return premiumEcoClassRadioButton.id
            FlightServiceClassType.CabinCode.COACH -> return economyClassRadioButton.id
            else -> throw RuntimeException("Could not find cabinCode : " + cabinCode)
        }
    }
}
