package com.expedia.bookings.itin.vm

import android.content.Context
import com.expedia.bookings.R
import com.squareup.phrase.Phrase

class FlightItinToolbarViewModel(private val context: Context) : ItinToolbarViewModel() {

    override fun updateWidget(toolbarParams: ToolbarParams) {
        toolbarTitleSubject.onNext(Phrase.from(context, R.string.itin_flight_toolbar_title_TEMPLATE).
                put("destination", toolbarParams.title).format().toString())
        toolbarSubTitleSubject.onNext(toolbarParams.subTitle)
        shareIconVisibleSubject.onNext(toolbarParams.showShareIcon)
    }
}