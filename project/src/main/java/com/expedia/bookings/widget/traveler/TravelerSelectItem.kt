package com.expedia.bookings.widget.traveler

import android.content.Context
import com.expedia.bookings.extensions.subscribeFont
import com.expedia.bookings.extensions.subscribeText
import com.expedia.bookings.extensions.subscribeTextAndVisibility
import com.expedia.bookings.extensions.subscribeTextColor
import com.expedia.vm.traveler.TravelerSelectItemViewModel

class TravelerSelectItem(context: Context, travelerSelectItemViewModel: TravelerSelectItemViewModel) : TravelerDetailsCard(context) {

    init {
        travelerSelectItemViewModel.titleObservable.subscribeText(detailsText)
        travelerSelectItemViewModel.subtitleObservable.subscribeTextAndVisibility(secondaryText)
        travelerSelectItemViewModel.titleFontObservable.subscribeFont(detailsText)
        travelerSelectItemViewModel.iconStatusObservable.subscribe {
            travelerStatusIcon.status = it
            setTravelerCardContentDescription(it, travelerSelectItemViewModel.titleObservable.value, travelerSelectItemViewModel.subtitleObservable.value)
        }

        travelerSelectItemViewModel.subtitleTextColorObservable.subscribeTextColor(secondaryText)
    }
}
