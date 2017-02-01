package com.expedia.vm.traveler

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.util.endlessObserver
import rx.subjects.BehaviorSubject
import kotlin.properties.Delegates

class TravelerAdvancedOptionsViewModel(val context: Context) {
    private var traveler: Traveler by Delegates.notNull()

    val redressViewModel: RedressViewModel by lazy {
        RedressViewModel(context)
    }

    val travelerNumberViewModel by lazy {
        TravelerNumberViewModel(context)
    }

    val redressNumberSubject = BehaviorSubject.create<String>()
    val travelerNumberSubject = BehaviorSubject.create<String>()
    val seatPreferenceSubject = BehaviorSubject.create<Traveler.SeatPreference>()
    val assistancePreferenceSubject = BehaviorSubject.create<Traveler.AssistanceType>()
    val materialFormTestEnabled = FeatureToggleUtil.isUserBucketedAndFeatureEnabled(context,
            AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms, R.string.preference_universal_checkout_material_forms)

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
            redressNumberSubject.onNext(traveler.redressNumber)
        } else {
            redressNumberSubject.onNext("")
        }
        if (traveler.knownTravelerNumber?.isNotEmpty() ?: false) {
            travelerNumberSubject.onNext(traveler.knownTravelerNumber)
        } else {
            travelerNumberSubject.onNext("")
        }
        assistancePreferenceSubject.onNext(traveler.assistance)
        seatPreferenceSubject.onNext(traveler.safeSeatPreference)
    }
}