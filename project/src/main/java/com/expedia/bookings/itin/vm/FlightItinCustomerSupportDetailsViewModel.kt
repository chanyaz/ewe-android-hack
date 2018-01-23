package com.expedia.bookings.itin.vm

class FlightItinCustomerSupportDetailsViewModel : ItinCustomerSupportDetailsViewModel() {

    override fun updateWidget(param: ItinCustomerSupportDetailsWidgetParams) {
        updateItinCustomerSupportDetailsWidgetSubject.onNext(param)
    }
}
