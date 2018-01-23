package com.expedia.bookings.widget.traveler

import android.content.Context
import com.expedia.util.subscribeFont
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.util.subscribeTextColor
import com.expedia.vm.traveler.TravelerSelectItemViewModel

class TravelerSelectItem(context: Context, travelerSelectItemViewModel: TravelerSelectItemViewModel) : TravelerDetailsCard(context) {

    init {
        travelerSelectItemViewModel.titleObservable.subscribeText(detailsText)
        travelerSelectItemViewModel.subtitleObservable.subscribeTextAndVisibility(secondaryText)
        travelerSelectItemViewModel.titleFontObservable.subscribeFont(detailsText)
        travelerSelectItemViewModel.iconStatusObservable.subscribe {
            travelerStatusIcon.status = it
        }

        travelerSelectItemViewModel.subtitleTextColorObservable.subscribeTextColor(secondaryText)
    }
}
