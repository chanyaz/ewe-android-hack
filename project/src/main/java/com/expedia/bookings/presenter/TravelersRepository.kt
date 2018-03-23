package com.expedia.bookings.presenter

import com.expedia.bookings.data.Traveler
import io.reactivex.Flowable

interface TravelersRepository {

    val travelers: Flowable<List<Traveler>>

    fun updateTraveler(index: Int, traveler: Traveler)
}
