package com.expedia.bookings.itin.flight.traveler

import android.content.Context
import com.expedia.bookings.itin.common.ItinToolbarViewModel

class FlightItinTravelerToolBarViewModel : ItinToolbarViewModel() {
    override fun updateWidget(toolbarParams: ToolbarParams) {
        toolbarTitleSubject.onNext(toolbarParams.title)
        toolbarSubTitleSubject.onNext(toolbarParams.subTitle)
    }
}
