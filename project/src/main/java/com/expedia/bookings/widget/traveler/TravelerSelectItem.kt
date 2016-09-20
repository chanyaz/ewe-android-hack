package com.expedia.bookings.widget.traveler

import android.content.Context
import com.expedia.util.subscribeFont
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.util.subscribeTextColor
import com.expedia.vm.traveler.TravelerPickerTravelerViewModel

class TravelerSelectItem(context: Context, travelerViewModel: TravelerPickerTravelerViewModel) : TravelerDetailsCard(context) {

    init {
        travelerViewModel.titleObservable.subscribeText(detailsText)
        travelerViewModel.subtitleObservable.subscribeTextAndVisibility(secondaryText)
        travelerViewModel.titleFontObservable.subscribeFont(detailsText)
        travelerViewModel.iconStatusObservable.subscribe {
            travelerStatusIcon.status = it
        }

        travelerViewModel.subtitleTextColorObservable.subscribeTextColor(secondaryText)
    }
}