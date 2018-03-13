package com.expedia.bookings.itin.flight.manageBooking

import com.expedia.bookings.itin.common.ItinCustomerSupportDetailsViewModel

class FlightItinCustomerSupportDetailsViewModel : ItinCustomerSupportDetailsViewModel() {

    override fun updateWidget(param: ItinCustomerSupportDetailsWidgetParams) {
        updateItinCustomerSupportDetailsWidgetSubject.onNext(param)
    }
}
