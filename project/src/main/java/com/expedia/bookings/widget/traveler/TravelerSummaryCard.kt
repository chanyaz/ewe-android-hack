package com.expedia.bookings.widget.traveler

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.R
import com.expedia.bookings.enums.TravelerCheckoutStatus
import com.expedia.bookings.widget.ContactDetailsCompletenessStatus
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextColor
import com.expedia.vm.traveler.BaseSummaryViewModel

class TravelerSummaryCard(context: Context, attrs: AttributeSet?) : TravelerDetailsCard(context, attrs) {

    var viewModel: BaseSummaryViewModel by notNullAndObservable { vm ->
        vm.titleObservable.subscribeText(detailsText)
        vm.subtitleObservable.subscribeText(secondaryText)
        vm.subtitleColorObservable.subscribeTextColor(secondaryText)
        vm.iconStatusObservable.subscribe {
            travelerStatusIcon.status = it
            setTravelerCardContentDescription(it)
        }
    }

    fun getStatus() : TravelerCheckoutStatus {
        return viewModel.travelerStatusObserver.value
    }

    fun setTravelerCardContentDescription(status: ContactDetailsCompletenessStatus) {
        if (ContactDetailsCompletenessStatus.INCOMPLETE == status) {
            this.contentDescription = context.getString(R.string.traveler_details_incomplete_cont_desc)
        } else if (ContactDetailsCompletenessStatus.COMPLETE == status) {
            this.contentDescription = context.getString(R.string.traveler_details_complete_cont_desc)
        }
    }
}

