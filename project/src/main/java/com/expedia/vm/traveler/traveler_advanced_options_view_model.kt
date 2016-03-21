package com.expedia.vm.traveler

import com.expedia.bookings.data.Traveler
import com.expedia.util.endlessObserver
import rx.subjects.BehaviorSubject

class TravelerAdvancedOptionsViewModel(var traveler: Traveler) {
    val redressNumberSubject = BehaviorSubject.create<String>()
    val seatPreferenceSubject = BehaviorSubject.create<Traveler.SeatPreference>()
    val assistancePreferenceSubject = BehaviorSubject.create<Traveler.AssistanceType>()

    init {
        redressNumberSubject.onNext(if (traveler.redressNumber.isNullOrEmpty()) "" else traveler.redressNumber)
        if (traveler.seatPreference != null) {
            seatPreferenceSubject.onNext(traveler.seatPreference)
        }

        if (traveler.assistance != null) {
            assistancePreferenceSubject.onNext(traveler.assistance)
        }
    }

    val redressNumberObserver = endlessObserver<String> { redressNumber ->
        traveler.redressNumber = redressNumber
    }

    val seatPreferenceObserver = endlessObserver<Traveler.SeatPreference> { seatPref ->
        traveler.seatPreference = seatPref
    }

    val assistancePreferenceObserver = endlessObserver<Traveler.AssistanceType> { assistancePref ->
        traveler.assistance = assistancePref
    }
}