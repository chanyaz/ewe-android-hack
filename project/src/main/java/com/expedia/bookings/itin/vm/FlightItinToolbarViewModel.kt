package com.expedia.bookings.itin.vm

import com.expedia.bookings.data.trips.ItinCardDataFlight
import io.reactivex.subjects.PublishSubject

class FlightItinToolbarViewModel : ItinToolbarViewModel() {

    override fun updateWidget(toolbarParams: ToolbarParams) {
        toolbarTitleSubject.onNext(toolbarParams.title)
        toolbarSubTitleSubject.onNext(toolbarParams.subTitle)
        shareIconVisibleSubject.onNext(toolbarParams.showShareIcon)
    }

    val itinCardDataSubject = PublishSubject.create<ItinCardDataFlight>()
    lateinit var itinCardData: ItinCardDataFlight

    init {
        itinCardDataSubject.subscribe {
            itinCardData = it
        }
    }
}
