package com.expedia.bookings.itin.common

import io.reactivex.subjects.PublishSubject

abstract class ItinCustomerSupportDetailsViewModel {

    data class ItinCustomerSupportDetailsWidgetParams(
            val header: String,
            val itineraryNumber: String,
            val callSupportNumber: String,
            val customerSupport: String,
            val customerSupportURL: String,
            val isGuest: Boolean = false
    )

    val updateItinCustomerSupportDetailsWidgetSubject: PublishSubject<ItinCustomerSupportDetailsWidgetParams> = PublishSubject.create<ItinCustomerSupportDetailsWidgetParams>()
    abstract fun updateWidget(param: ItinCustomerSupportDetailsWidgetParams)
}
