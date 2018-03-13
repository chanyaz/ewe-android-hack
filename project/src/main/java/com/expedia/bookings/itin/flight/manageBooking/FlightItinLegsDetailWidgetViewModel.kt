package com.expedia.bookings.itin.flight.manageBooking

import io.reactivex.subjects.PublishSubject

class FlightItinLegsDetailWidgetViewModel {

    val updateWidgetRecyclerViewSubject: PublishSubject<ArrayList<FlightItinLegsDetailData>> = PublishSubject.create<ArrayList<FlightItinLegsDetailData>>()
    val rulesAndRestrictionDialogTextSubject: PublishSubject<String> = PublishSubject.create<String>()
    val shouldShowSplitTicketTextSubject = PublishSubject.create<Boolean>()
}
