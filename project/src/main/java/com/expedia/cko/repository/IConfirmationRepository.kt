package com.expedia.cko.repository

import com.expedia.bookings.data.Money
import io.reactivex.Flowable

interface IConfirmationRepository {

    val bookingSubHeader: Flowable<String>
    val userEmailID: Flowable<String>
    val itineraryNumber: Flowable<String>
    val tripTotal: Flowable<Money>
    val bookingDetails: List<BookingCard>
}
