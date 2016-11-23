package com.expedia.vm.traveler

import android.content.Context
import com.expedia.bookings.data.Traveler
import com.expedia.util.endlessObserver
import rx.subjects.BehaviorSubject
import kotlin.properties.Delegates

class TravelerAdvancedOptionsViewModel(context: Context) {
    private var traveler: Traveler by Delegates.notNull()

    val redressViewModel: RedressViewModel by lazy {
        RedressViewModel(context)
    }

    val travelerNumberViewModel by lazy {
        TravelerNumberViewModel(context)
    }

    val seatPreferenceSubject = BehaviorSubject.create<Traveler.SeatPreference>()
    val assistancePreferenceSubject = BehaviorSubject.create<Traveler.AssistanceType>()

    val seatPreferenceObserver = endlessObserver<Traveler.SeatPreference> { seatPref ->
        traveler.seatPreference = seatPref
    }

    val assistancePreferenceObserver = endlessObserver<Traveler.AssistanceType> { assistancePref ->
        traveler.assistance = assistancePref
    }

    fun updateTraveler(traveler: Traveler) {
        this.traveler = traveler
        redressViewModel.traveler = traveler
        travelerNumberViewModel.traveler = traveler
        seatPreferenceSubject.onNext(traveler.seatPreference)
        assistancePreferenceSubject.onNext(traveler.assistance)
    }
}