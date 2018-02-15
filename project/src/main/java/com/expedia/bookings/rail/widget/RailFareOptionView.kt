package com.expedia.bookings.rail.widget

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.extensions.subscribeOnClick
import com.expedia.bookings.extensions.subscribeText
import com.expedia.bookings.extensions.subscribeVisibility
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.rail.RailFareOptionViewModel

class RailFareOptionView(context: Context) : LinearLayout(context) {

    val priceView: TextView by bindView(R.id.rail_fare_price)
    val fareTitle: TextView by bindView(R.id.rail_fare_title)
    val railCardImage: ImageView by bindView(R.id.rail_card_image)
    val fareDescription: TextView by bindView(R.id.fare_description)
    val selectButton: View by bindView(R.id.select_button)
    val amenitiesButton: TextView by bindView(R.id.amenities_link)

    var viewModel by notNullAndObservable<RailFareOptionViewModel> { vm ->
        vm.priceObservable.subscribeText(priceView)
        vm.fareDescriptionObservable.subscribeText(fareDescription)
        vm.fareTitleObservable.subscribeText(fareTitle)
        vm.railCardAppliedObservable.subscribeVisibility(railCardImage)

        selectButton.subscribeOnClick(vm.offerSelectButtonClicked)
        amenitiesButton.subscribeOnClick(vm.showAmenitiesForFareClicked)
        fareTitle.subscribeOnClick(vm.showFareRulesForFareClicked)
    }

    init {
        View.inflate(context, R.layout.widget_rail_details_fare_option, this)
        orientation = VERTICAL
    }
}
