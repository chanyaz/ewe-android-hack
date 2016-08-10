package com.expedia.vm.rail

import com.expedia.bookings.data.rail.responses.RailLegOption
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import rx.subjects.BehaviorSubject

class RailTimelineViewModel {
    val legOptionObserver = BehaviorSubject.create<RailLegOption>()
}
