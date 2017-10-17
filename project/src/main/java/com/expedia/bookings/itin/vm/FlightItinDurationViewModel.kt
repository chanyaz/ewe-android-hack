package com.expedia.bookings.itin.vm

import rx.subjects.PublishSubject

class FlightItinDurationViewModel {
    data class WidgetParams(
            val totalDuration: String,
            val totalDurationContDesc: String
    )

    val totalDurationSubject: PublishSubject<String> = PublishSubject.create<String>()
    val totalDurationContDescSubject: PublishSubject<String> = PublishSubject.create<String>()

    fun updateWidget(widgetParams: WidgetParams) {
        totalDurationSubject.onNext(widgetParams.totalDuration)
        totalDurationContDescSubject.onNext(widgetParams.totalDurationContDesc)
    }

}