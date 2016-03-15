package com.expedia.bookings.widget.traveler

import android.content.Context
import com.expedia.bookings.R
import com.expedia.util.subscribeInverseVisibility
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextColor
import com.expedia.util.subscribeVisibility
import com.expedia.vm.traveler.TravelerViewModel
import com.squareup.phrase.Phrase

class TravelerSelectItem(context: Context, val travelerViewModel: TravelerViewModel) : TravelerDetailsCard(context) {

    init {
        editTravelerPromptText.text = Phrase.from(resources.getString(R.string.checkout_edit_traveler_TEMPLATE))
                .put("travelernumber", travelerViewModel.travelerNumber).format().toString()

        val nameViewModel = travelerViewModel.nameViewModel
        val phoneViewModel = travelerViewModel.phoneViewModel

        nameViewModel.fullNameSubject.subscribeText(detailsText)
        phoneViewModel.phoneNumberSubject.subscribeText(secondaryText)

        travelerViewModel.completenessTextColorObservable.subscribeTextColor(editTravelerPromptText)
        travelerViewModel.completenessStatusObservable.subscribe {
            travelerStatusIcon.status = it
        }

        travelerViewModel.emptyTravelerObservable.subscribeVisibility(editTravelerPromptContainer)
        travelerViewModel.emptyTravelerObservable.subscribeInverseVisibility(travelerSummaryContainer)
    }
}