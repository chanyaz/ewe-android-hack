package com.expedia.bookings.itin.vm

import com.expedia.bookings.itin.data.FlightItinLegsDetailData
import rx.subjects.PublishSubject

class FlightItinLegsDetailWidgetViewModel {

    val updateWidgetRecyclerViewSubject: PublishSubject<ArrayList<FlightItinLegsDetailData>> = PublishSubject.create<ArrayList<FlightItinLegsDetailData>>()
    val rulesAndRestrictionDialogTextSubject: PublishSubject<String> = PublishSubject.create<String>()
    val shouldShowSplitTicketTextSubject = PublishSubject.create<Boolean>()
}
