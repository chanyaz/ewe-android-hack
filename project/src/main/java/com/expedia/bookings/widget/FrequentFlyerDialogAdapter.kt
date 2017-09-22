package com.expedia.bookings.widget

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.flights.FrequentFlyerPlansTripResponse
import com.mobiata.android.util.Ui
import java.util.LinkedHashMap
import java.util.ArrayList

class FrequentFlyerDialogAdapter(context: Context, textViewResId: Int, val dropDownViewResId: Int,
                                 val allFrequentFlyerPlans: LinkedHashMap<String, FrequentFlyerPlansTripResponse>,
                                 var enrolledFrequentFlyerPlans: LinkedHashMap<String, FrequentFlyerPlansTripResponse>,
                                 val allAirlineCodes: ArrayList<String>,
                                 startingProgramName: String) : ArrayAdapter<String>(context, textViewResId) {
    var currentPosition: Int

    init {
        setDropDownViewResource(dropDownViewResId)
        currentPosition = getPositionFromName(startingProgramName)
    }

    override fun getCount(): Int {
        return allFrequentFlyerPlans.size
    }

    override fun getItem(position: Int): String? {
        return getFrequentFlyerProgram(position)
    }

    fun getPositionFromName(airlineName: String?): Int {
        return (0..allFrequentFlyerPlans.size - 1).firstOrNull {
            getFrequentFlyerProgram(it) == airlineName
        } ?: -1
    }

    fun getFrequentFlyerNumber(position: Int): String {
        return enrolledFrequentFlyerPlans[allAirlineCodes[position]]?.membershipNumber ?: ""
    }

    fun getFrequentFlyerProgram(position: Int) : String {
        return allFrequentFlyerPlans[allAirlineCodes[position]]?.frequentFlyerPlanName ?: ""
    }

    fun getFrequentFlyerFlyerProgramId(position: Int) : String {
        return allFrequentFlyerPlans[allAirlineCodes[position]]?.frequentFlyerPlanID ?: ""
    }

    fun getFrequentFlyerProgramCode(position: Int) : String {
        return allAirlineCodes[position]
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val retView = Ui.inflate<LinearLayout>(dropDownViewResId, parent, false)

        setTextAndColor(retView, R.id.frequent_flyer_program_list_title, getFrequentFlyerProgram(position), position)
        setTextAndColor(retView, R.id.frequent_flyer_program_list_number, getFrequentFlyerNumber(position), position)

        if (position == enrolledFrequentFlyerPlans.size - 1) {
            Ui.findView<View>(retView, R.id.ffn_divider).visibility = View.VISIBLE
        }

        return retView
    }

    private fun setTextAndColor(parent: LinearLayout, textViewId: Int, text: String, position: Int) {
        val textView = Ui.findView<TextView>(parent, textViewId)
        textView.text = text
        TextViewExtensions.setTextColorBasedOnPosition(textView, currentPosition, position)
    }
}
