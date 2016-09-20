package com.expedia.bookings.widget.rail

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeOnClick
import com.expedia.util.subscribeText
import com.expedia.vm.rail.RailFareOptionViewModel

class RailFareOptionView(context: Context) : LinearLayout(context) {

    val priceView: TextView by bindView(R.id.price)
    val fareTitle: TextView by bindView(R.id.fare_title)
    val fareDescription: TextView by bindView(R.id.fare_description)
    val selectButton: View by bindView(R.id.select_button)
    val amenitiesButton: TextView by bindView(R.id.amenities_link)

    var viewModel by notNullAndObservable<RailFareOptionViewModel> { vm ->
        vm.priceObservable.subscribeText(priceView)
        vm.fareDescriptionObservable.subscribeText(fareDescription)
        vm.fareTitleObservable.subscribeText(fareTitle)

        selectButton.subscribeOnClick(vm.offerSelectButtonClicked)
        amenitiesButton.subscribeOnClick(vm.showAmenitiesForFareClicked)
        fareTitle.subscribeOnClick(vm.showFareRulesForFareClicked)
    }

    init {
        View.inflate(context, R.layout.widget_rail_details_fare_option, this)
    }
}
