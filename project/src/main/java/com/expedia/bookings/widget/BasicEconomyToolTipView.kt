package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import android.widget.TextView
import com.expedia.vm.flights.BasicEconomyTooltipViewModel

class BasicEconomyToolTipView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    val linearLayout: LinearLayout by bindView(R.id.basic_economy_tooltip_info_container)

    var viewmodel: BasicEconomyTooltipViewModel by notNullAndObservable { vm ->
        vm.basicEconomyTooltipFareRules.subscribe {
            linearLayout.removeAllViews()
            for (fareRule in it) {
                linearLayout.addView(
                        createRow(fareRule)
                )
            }
        }
    }

    init {
        View.inflate(context, R.layout.basic_economy_tooltip_info_stub, this)
    }

    private fun createRow(fareRule: String): View {
        val row = LayoutInflater.from(context).inflate(R.layout.basic_economy_rules_tv, linearLayout, false)
        val fareRuleTextView = row.findViewById<TextView>(R.id.basic_economy_rules_tv)
        fareRuleTextView.text = fareRule
        return row
    }
}
