package com.expedia.bookings.widget.traveler

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.enums.TravelerCheckoutStatus
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextColor
import com.expedia.vm.traveler.TravelerSummaryViewModel

class TravelerDefaultState(context: Context, attrs: AttributeSet?) : TravelerDetailsCard(context, attrs) {
    var status = TravelerCheckoutStatus.CLEAN

    var viewModel: TravelerSummaryViewModel by notNullAndObservable { vm ->
        vm.titleObservable.subscribeText(detailsText)
        vm.subtitleObservable.subscribeText(secondaryText)
        vm.subtitleColorObservable.subscribeTextColor(secondaryText)
        vm.iconStatusObservable.subscribe {
            travelerStatusIcon.status = it
        }
    }

    fun updateStatus(status: TravelerCheckoutStatus) {
        this.status = status
        viewModel.travelerStatusObserver.onNext(status)
    }
}

