package com.expedia.bookings.services.os

import com.expedia.bookings.data.os.LastMinuteDealsRequest
import com.expedia.bookings.data.os.LastMinuteDealsResponse
import io.reactivex.Observer

interface IOfferService {
    fun fetchDeals(request: LastMinuteDealsRequest, dealsObserver: Observer<LastMinuteDealsResponse>)
}
