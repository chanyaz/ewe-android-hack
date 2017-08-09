package com.expedia.bookings.widget

import android.content.Context
import android.text.SpannableString
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.expedia.bookings.data.flights.FrequentFlyerPlansTripResponse
import com.expedia.bookings.utils.Strings
import com.mobiata.android.util.Ui
import java.util.LinkedHashMap
import java.util.ArrayList

class FrequentFlyerDialogAdapter(context: Context, textViewResId: Int, dropDownViewResId: Int, frequentFlyerPlans: LinkedHashMap<String, FrequentFlyerPlansTripResponse>) : ArrayAdapter<String>(context, textViewResId) {
    var frequentFlyerProgramNames: ArrayList<String> = ArrayList()
    var currentPosition: Int = 0

    init {
        setDropDownViewResource(dropDownViewResId)
        init(frequentFlyerPlans)
    }

    private fun init(frequentFlyerPlans: LinkedHashMap<String, FrequentFlyerPlansTripResponse>) {
        FrequentFlyerPlans = frequentFlyerPlans
    }

    override fun getCount(): Int {
        return FrequentFlyerPlans.size
    }

    override fun getItem(position: Int): String? {
        return getFrequentFlyerProgram(position)
    }

    fun getFrequentFlyerProgram(position: Int): String {
        return frequentFlyerProgramNames!![position]
    }

    fun getFrequentFlyerNumber(position: Int): String? {
        return FrequentFlyerPlans[getFrequentFlyerProgram(position)]?.membershipNumber ?: ""
    }

    fun getPositionFromName(airlineName: String): Int {
        if (Strings.isEmpty(airlineName)) {
            return currentPosition
        }

        for (i in 0..FrequentFlyerPlans.size - 1) {
            if (getFrequentFlyerProgram(i) == airlineName) {
                return i
            }
        }
        return currentPosition
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val retView = super.getView(position, convertView, parent)
        val tv = Ui.findView<android.widget.TextView>(retView, android.R.id.text1)
        val item = getItem(position)
        val stringToSpan = SpannableString(String.format(getItem(position)!!, item))
        tv.text = stringToSpan
        TextViewExtensions.setTextColorBasedOnPosition(tv, currentPosition, position)

        return retView
    }

    companion object {
        private var FrequentFlyerPlans = LinkedHashMap<String, FrequentFlyerPlansTripResponse>()
    }

}
