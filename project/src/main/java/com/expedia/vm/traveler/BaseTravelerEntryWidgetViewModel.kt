package com.expedia.vm.traveler

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Traveler
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

abstract class BaseTravelerEntryWidgetViewModel(val context: Context, val travelerIndex: Int) {
    var nameViewModel = TravelerNameViewModel(context)
    var phoneViewModel = TravelerPhoneViewModel(context)
    var emailViewModel = TravelerEmailViewModel(getTraveler(), context)

    val showTravelerButtonObservable = BehaviorSubject.create<Boolean>()
    val showEmailSubject = BehaviorSubject.create<Boolean>()
    val clearPopupsSubject = PublishSubject.create<Unit>()
    val selectedTravelerSubject = PublishSubject.create<String>()

    init {
        showTravelerButtonObservable.subscribe {
            val traveler = getTraveler()
            if (traveler.isStoredTraveler || traveler.hasTuid()) {
                val travelerFullName = traveler.fullNameBasedOnPos
                selectedTravelerSubject.onNext(travelerFullName)
            } else {
                selectedTravelerSubject.onNext(context.getString(R.string.traveler_saved_contacts_text))
            }
        }
    }

    abstract fun updateTraveler(traveler: Traveler)
    abstract fun validate(): Boolean

    open fun getTraveler(): Traveler {
        return Db.getTravelers()[travelerIndex]
    }
}