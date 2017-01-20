package com.expedia.vm.traveler

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.utils.FeatureToggleUtil
import rx.subjects.BehaviorSubject

class GenderViewModel(var traveler: Traveler, val context: Context) : BaseTravelerValidatorViewModel() {
    val genderSubject = BehaviorSubject.create<Traveler.Gender>()
    val materialFormTestEnabled = FeatureToggleUtil.isUserBucketedAndFeatureEnabled(context,
            AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms, R.string.preference_universal_checkout_material_forms)


    init {
        genderSubject.subscribe { gender ->
            traveler.gender = gender
        }

        if (!materialFormTestEnabled) {
            errorSubject.onNext(false)
        }
    }

    fun updateTravelerGender(traveler: Traveler) {
        this.traveler = traveler
        if (traveler.gender != null) {
            genderSubject.onNext(traveler.gender)
        }
    }

    override fun isValid(): Boolean {
        return traveler.gender != Traveler.Gender.GENDER
    }
}