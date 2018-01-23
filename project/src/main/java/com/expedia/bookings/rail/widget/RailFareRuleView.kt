package com.expedia.bookings.rail.widget

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.squareup.phrase.Phrase

class RailFareRuleView(context: Context) : LinearLayout(context) {

    val fareRulesText: TextView by bindView(R.id.fare_rules_text)

    init {
        View.inflate(getContext(), R.layout.rail_fare_rule_view, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        orientation = VERTICAL
    }

    fun updateFareRule(fareRule: String) {
        fareRulesText.text = Phrase.from(context, R.string.fare_rule_text_TEMPLATE)
                .put("farerule", fareRule)
                .format().toString()
    }
}
