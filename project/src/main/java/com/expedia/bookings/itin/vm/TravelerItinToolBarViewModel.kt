package com.expedia.bookings.itin.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItinCardDataFlight
import com.squareup.phrase.Phrase

class TravelerItinToolBarViewModel(private val context: Context): ItinToolbarViewModel() {
    override fun updateWidget(toolbarParams: ToolbarParams) {
        toolbarTitleSubject.onNext(toolbarParams.title)
        toolbarSubTitleSubject.onNext(toolbarParams.subTitle)
    }
}