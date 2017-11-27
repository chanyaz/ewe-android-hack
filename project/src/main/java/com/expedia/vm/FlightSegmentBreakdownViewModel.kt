package com.expedia.vm

import android.content.Context
import rx.subjects.PublishSubject

class FlightSegmentBreakdownViewModel(val context: Context) {
    val addSegmentRowsObserver = PublishSubject.create<List<FlightSegmentBreakdown>>()
}
