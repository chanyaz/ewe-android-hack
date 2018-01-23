package com.expedia.bookings.itin.vm

import android.content.Context

class TravelerItinToolBarViewModel(private val context: Context) : ItinToolbarViewModel() {
    override fun updateWidget(toolbarParams: ToolbarParams) {
        toolbarTitleSubject.onNext(toolbarParams.title)
        toolbarSubTitleSubject.onNext(toolbarParams.subTitle)
    }
}
