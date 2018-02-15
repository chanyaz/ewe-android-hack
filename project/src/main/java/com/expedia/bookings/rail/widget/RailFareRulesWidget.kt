package com.expedia.bookings.rail.widget

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.extensions.subscribeInverseVisibility
import com.expedia.bookings.extensions.subscribeText
import com.expedia.bookings.extensions.subscribeVisibility
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.rail.RailFareRulesViewModel

class RailFareRulesWidget(context: Context) : LinearLayout(context) {
    val fareInfo: TextView by bindView(R.id.fare_description)
    val fareRulesContainer: LinearLayout by bindView(R.id.fare_rules_container)
    val noFareRulesAvailable: android.widget.TextView by bindView(R.id.no_fare_rules_available)

    var viewModel: RailFareRulesViewModel by notNullAndObservable { vm ->
        vm.fareInfoObservable.subscribeText(fareInfo)
        vm.fareRulesObservable.subscribe { fareRules ->
            fareRulesContainer.removeAllViews()
            addFareRules(fareRules)
        }
        vm.noFareRulesObservable.subscribeVisibility(noFareRulesAvailable)
        vm.noFareRulesObservable.subscribeInverseVisibility(fareRulesContainer)
    }

    init {
        View.inflate(context, R.layout.rail_fare_rules_widget, this)
    }

    private fun addFareRules(fareRules: List<String>) {
        fareRules.forEach { fareRule ->
            val fareRuleView = RailFareRuleView(context)
            fareRuleView.updateFareRule(fareRule)
            fareRulesContainer.addView(fareRuleView)
        }
    }
}
