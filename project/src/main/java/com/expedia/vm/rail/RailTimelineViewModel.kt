package com.expedia.vm.rail

import com.expedia.bookings.data.rail.responses.LegOption
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import rx.subjects.BehaviorSubject

class RailTimelineViewModel {
    val legOptionObserver = BehaviorSubject.create<LegOption>()
}
