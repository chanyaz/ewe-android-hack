package com.expedia.bookings.widget.traveler

import android.content.Context
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextColor
import com.expedia.vm.traveler.TravelerSelectViewModel

class TravelerSelectItem(context: Context, val travelerViewModel: TravelerSelectViewModel) : TravelerDetailsCard(context) {

    init {
        travelerViewModel.titleObservable.subscribeText(detailsText)
        travelerViewModel.subtitleObservable.subscribeTextAndVisibility(secondaryText)

        travelerViewModel.iconStatusObservable.subscribe {
            travelerStatusIcon.status = it
        }

        travelerViewModel.textColorObservable.subscribeTextColor(detailsText)
    }
}