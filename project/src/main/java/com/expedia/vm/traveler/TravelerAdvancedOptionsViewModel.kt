package com.expedia.vm.traveler

import com.expedia.bookings.data.Traveler
import com.expedia.util.endlessObserver
import com.jakewharton.rxbinding.widget.TextViewAfterTextChangeEvent
import rx.Subscriber
import rx.exceptions.OnErrorNotImplementedException
import rx.subjects.BehaviorSubject

class TravelerAdvancedOptionsViewModel(var traveler: Traveler) {
    val redressNumberObserver = endlessObserver<TextViewAfterTextChangeEvent>() { redressText ->
        traveler.redressNumber = redressText.editable().toString()
    }

    val seatPreferenceObserver = endlessObserver<Traveler.SeatPreference> { seatPref ->
        traveler.seatPreference = seatPref
    }

    val assistancePreferenceObserver = endlessObserver<Traveler.AssistanceType> { assistancePref ->
        traveler.assistance = assistancePref
    }

    fun getSpecialAssistance(): Traveler.AssistanceType {
        return traveler.assistance
    }

    fun getSeatPreference(): Traveler.SeatPreference {
        if (traveler.seatPreference == null) {
            traveler.seatPreference == Traveler.SeatPreference.WINDOW
        }
        return traveler.seatPreference
    }

    fun getRedressNumber(): String {
        return traveler.redressNumber ?: ""
    }
}