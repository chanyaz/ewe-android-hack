package com.expedia.bookings.itin.vm

import io.reactivex.subjects.PublishSubject

abstract class ItinWebviewInfoButtonViewModel() {

    data class ItinWebviewInfoButtonWidgetParams(
            val text: String?,
            val drawable: Int?,
            val textColor: Int?,
            val url: String?
    )

    val createWebviewButtonWidgetSubject: PublishSubject<ItinWebviewInfoButtonWidgetParams> = PublishSubject.create<ItinWebviewInfoButtonWidgetParams>()

    abstract fun updateWidgetWithBaggageInfoUrl(webviewLink: String)
}
