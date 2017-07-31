package com.expedia.bookings.widget

import android.content.Context
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import android.view.ViewStub
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.flights.FlightFareFamilyWidget
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.util.subscribeVisibility
import com.expedia.vm.FareFamilyViewModel
import com.expedia.vm.flights.FlightFareFamilyViewModel


class FareFamilyCardView(context: Context, attrs: AttributeSet) : CardView(context, attrs) {
    val selectedClassTextView: TextView by bindView(R.id.selected_classes)
    val deltaPriceView: TextView by bindView(R.id.upgrade_delta_price)

    var viewModel: FareFamilyViewModel by notNullAndObservable { vm ->
        vm.selectedClassObservable.subscribeText(selectedClassTextView)
        vm.deltaPriceObservable.subscribeText(deltaPriceView)
        vm.widgetVisibilityObservable.subscribeVisibility(this)
    }

    init {
        View.inflate(context, R.layout.fare_family_card_view, this)
    }
}
