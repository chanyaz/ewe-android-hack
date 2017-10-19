package com.expedia.vm.traveler

import android.content.Context
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.utils.isMaterialFormsEnabled
import com.expedia.util.endlessObserver
import rx.subjects.BehaviorSubject
import kotlin.properties.Delegates

class TravelerAdvancedOptionsViewModel(val context: Context) {
    private var traveler: Traveler by Delegates.notNull()

    val redressViewModel: RedressViewModel by lazy {
        RedressViewModel(context)
    }

    val travelerNumberViewModel by lazy {
        TravelerNumberViewModel()
    }

    val seatPreferenceSubject = BehaviorSubject.create<Traveler.SeatPreference>()
    val assistancePreferenceSubject = BehaviorSubject.create<Traveler.AssistanceType>()
    val materialFormTestEnabled = isMaterialFormsEnabled()
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
        if (traveler.redressNumber?.isNotEmpty() ?: false) {
            redressViewModel.textSubject.onNext(traveler.redressNumber)
        } else {
            redressViewModel.textSubject.onNext("")
        }
        if (traveler.knownTravelerNumber?.isNotEmpty() ?: false) {
            travelerNumberViewModel.textSubject.onNext(traveler.knownTravelerNumber)
        } else {
            travelerNumberViewModel.textSubject.onNext("")
        }
        assistancePreferenceSubject.onNext(traveler.assistance)
        seatPreferenceSubject.onNext(traveler.safeSeatPreference)
    }
}