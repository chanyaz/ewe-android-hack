package com.expedia.bookings.widget.traveler

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.enums.TravelerCheckoutStatus
import com.expedia.bookings.extensions.subscribeText
import com.expedia.bookings.extensions.subscribeTextAndVisibility
import com.expedia.bookings.extensions.subscribeTextColor
import com.expedia.util.notNullAndObservable
import com.expedia.vm.traveler.BaseSummaryViewModel

class TravelerSummaryCard(context: Context, attrs: AttributeSet?) : TravelerDetailsCard(context, attrs) {

    var viewModel: BaseSummaryViewModel by notNullAndObservable { vm ->

        vm.titleObservable.subscribeText(detailsText)
        vm.subtitleObservable.subscribeTextAndVisibility(secondaryText)
        vm.subtitleColorObservable.subscribeTextColor(secondaryText)
        vm.iconStatusObservable.subscribe {
            travelerStatusIcon.status = it
            setTravelerCardContentDescription(it, vm.titleObservable.value, vm.subtitleObservable.value)
        }
    }

    fun getStatus(): TravelerCheckoutStatus {
        return viewModel.travelerStatusObserver.value
    }
}
