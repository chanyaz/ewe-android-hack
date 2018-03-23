package com.expedia.bookings.presenter

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.ViewModel
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Traveler
import javax.inject.Inject

class TravelersViewModel @Inject constructor(override val travelersRepository: TravelersRepository, override val travelersConfig: TravelerConfig) : ViewModel(), ITravelersViewModel {

    override val travelers: LiveData<List<Traveler>> by lazy {
        LiveDataReactiveStreams.fromPublisher(travelersRepository.travelers)
    }
}
