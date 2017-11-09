package com.expedia.bookings.itin.vm


class FlightItinToolbarViewModel : ItinToolbarViewModel() {

    override fun updateWidget(toolbarParams: ToolbarParams) {
        toolbarTitleSubject.onNext(toolbarParams.title)
        toolbarSubTitleSubject.onNext(toolbarParams.subTitle)
        shareIconVisibleSubject.onNext(toolbarParams.showShareIcon)
    }
}