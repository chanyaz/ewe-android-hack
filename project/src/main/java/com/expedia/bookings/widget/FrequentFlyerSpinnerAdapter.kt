package com.expedia.bookings.widget

import java.util.ArrayList
import java.util.LinkedHashMap
import java.util.Locale

import android.content.Context
import android.text.SpannableString
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.expedia.bookings.data.flights.TravelerFrequentFlyerMembership

import com.expedia.bookings.utils.Strings
import com.mobiata.android.util.Ui

class FrequentFlyerSpinnerAdapter(context: Context, textViewResId: Int, dropDownViewResId: Int) : ArrayAdapter<String>(context, textViewResId) {
    private var frequentFlyerProgram: ArrayList<String>? = null
    var currentPosition: Int = 0

    init {
        setDropDownViewResource(dropDownViewResId)
        init()
    }

    private fun init() {
        fillAirlines()
        frequentFlyerProgram = ArrayList(FrequentFlyerAirlines.keys)
    }

    override fun getCount(): Int {
        return FrequentFlyerAirlines.size
    }

    override fun getItem(position: Int): String? {
        return String.format(Locale.getDefault(), "%s Number: (%d)", getFrequentFlyerProgram(position), getFrequentFlyerNumber(position))
    }

    fun getFrequentFlyerProgram(position: Int): String {
        return frequentFlyerProgram!![position]
    }

    fun getFrequentFlyerNumber(position: Int): Int {
        return (FrequentFlyerAirlines[getFrequentFlyerProgram(position)])!!
    }

    fun getPositionFromName(airlineName: String): Int {
        if (Strings.isEmpty(airlineName)) {
            return currentPosition
        }
        for (i in 0..FrequentFlyerAirlines.size - 1) {
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

    private fun fillAirlines() {
        FrequentFlyerAirlines.put("Airline A", 1234)
        FrequentFlyerAirlines.put("Airline B", 2345)
        FrequentFlyerAirlines.put("Airline C", 3456)
        FrequentFlyerAirlines.put("Airline D", 4567)
        FrequentFlyerAirlines.put("Airline E", 5678)
    }

    companion object {
        private val FrequentFlyerAirlines = LinkedHashMap<String, Int>()
        private val FrequentFlyerPrograms = HashMap<String, TravelerFrequentFlyerMembership>()
    }

}
