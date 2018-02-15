package com.expedia.bookings.widget

import android.content.Context
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.extensions.setFocusForView
import com.expedia.bookings.extensions.subscribeContentDescription
import com.expedia.bookings.extensions.subscribeOnClick
import com.expedia.bookings.extensions.subscribeText
import com.expedia.bookings.extensions.subscribeTextAndVisibility
import com.expedia.bookings.extensions.subscribeTextColor
import com.expedia.bookings.extensions.subscribeVisibility
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.FareFamilyViewModel

class FareFamilyCardView(context: Context, attrs: AttributeSet) : CardView(context, attrs) {

    val selectedClassTextView: TextView by bindView(R.id.selected_classes)
    val deltaPriceView: TextView by bindView(R.id.upgrade_delta_price)
    val fareFamilyTitle: TextView by bindView(R.id.fare_family_title)
    val fromLabel: TextView by bindView(R.id.fare_family_from_label)
    val travellerTextView: TextView by bindView(R.id.traveller)

    var viewModel: FareFamilyViewModel by notNullAndObservable { vm ->
        vm.selectedClassObservable.subscribeText(selectedClassTextView)
        vm.deltaPriceObservable.subscribeTextAndVisibility(deltaPriceView)
        vm.fromLabelVisibility.subscribeVisibility(fromLabel)
        vm.selectedClassColorObservable.subscribeTextColor(selectedClassTextView)
        vm.fareFamilyTitleObservable.subscribeText(fareFamilyTitle)
        vm.travellerObservable.subscribeTextAndVisibility(travellerTextView)
        vm.widgetVisibilityObservable.subscribeVisibility(this)
        vm.contentDescriptionObservable.subscribeContentDescription(this)
        subscribeOnClick(vm.fareFamilyCardClickObserver)
        vm.tripObservable.filter { it.isFareFamilyUpgraded }.subscribe {
            this.setFocusForView()
        }
    }

    init {
        View.inflate(context, R.layout.fare_family_card_view, this)
    }
}
