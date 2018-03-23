package com.expedia.bookings.presenter

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.ViewModel
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Traveler
import javax.inject.Inject

interface ITravelersViewModel {
    val travelersRepository: TravelersRepository
    val travelersConfig: TravelerConfig
    val travelers: LiveData<List<Traveler>>
    fun getComponentType(lineOfBusiness: String): ComponentType
}
