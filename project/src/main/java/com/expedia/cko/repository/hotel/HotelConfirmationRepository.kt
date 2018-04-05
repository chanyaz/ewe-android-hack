package com.expedia.cko.repository.hotel

import com.expedia.bookings.data.Money
import com.expedia.bookings.services.HotelServices
import com.expedia.cko.repository.BookingCard
import com.expedia.cko.repository.IConfirmationRepository
import io.reactivex.Flowable

class HotelConfirmationRepository(hotelServices: HotelServices) : IConfirmationRepository {
    override val bookingSubHeader: Flowable<String>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val userEmailID: Flowable<String>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val itineraryNumber: Flowable<String>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val tripTotal: Flowable<Money>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val bookingDetails: List<BookingCard>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

}
