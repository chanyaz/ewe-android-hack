package com.expedia.vm.traveler

import com.expedia.bookings.data.Traveler
import com.expedia.util.endlessObserver
import com.jakewharton.rxbinding.widget.TextViewAfterTextChangeEvent
import rx.subjects.BehaviorSubject
import kotlin.properties.Delegates

class TravelerAdvancedOptionsViewModel() {
    private var traveler: Traveler by Delegates.notNull()

    val redressNumberSubject = BehaviorSubject.create<String>()
    val seatPreferenceSubject = BehaviorSubject.create<Traveler.SeatPreference>()
    val assistancePreferenceSubject = BehaviorSubject.create<Traveler.AssistanceType>()

    val redressNumberObserver = endlessObserver<String>() { traveler.redressNumber = it }

    val seatPreferenceObserver = endlessObserver<Traveler.SeatPreference> { seatPref ->
        traveler.seatPreference = seatPref
    }

    val assistancePreferenceObserver = endlessObserver<Traveler.AssistanceType> { assistancePref ->
        traveler.assistance = assistancePref
    }

    fun updateTraveler(traveler: Traveler) {
        this.traveler = traveler
        redressNumberSubject.onNext(traveler.redressNumber)
        seatPreferenceSubject.onNext(traveler.seatPreference)
        assistancePreferenceSubject.onNext(traveler.assistance)
    }
}