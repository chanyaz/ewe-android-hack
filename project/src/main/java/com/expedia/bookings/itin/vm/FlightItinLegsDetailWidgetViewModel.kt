package com.expedia.bookings.itin.vm

import com.expedia.bookings.itin.data.FlightItinLegsDetailData
import rx.subjects.PublishSubject

class FlightItinLegsDetailWidgetViewModel {

    val updateWidgetRecyclerViewSubjet: PublishSubject<ArrayList<FlightItinLegsDetailData>> = PublishSubject.create<ArrayList<FlightItinLegsDetailData>>()
    val rulesAndRestrictionDialogTextSubject: PublishSubject<String> = PublishSubject.create<String>()
}