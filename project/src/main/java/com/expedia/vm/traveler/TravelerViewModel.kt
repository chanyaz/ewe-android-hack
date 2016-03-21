package com.expedia.vm.traveler

import android.content.Context
import android.support.v4.content.ContextCompat
import com.expedia.bookings.R
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.widget.ContactDetailsCompletenessStatus
import rx.subjects.PublishSubject

class TravelerViewModel(private val context: Context, private val traveler: Traveler, val travelerNumber: Int) {
    val emptyTravelerObservable = PublishSubject.create<Boolean>()
    val completenessStatusObservable = PublishSubject.create<ContactDetailsCompletenessStatus>()
    val completenessTextColorObservable = PublishSubject.create<Int>()

    val nameViewModel = TravelerNameViewModel(traveler.name)
    val phoneViewModel = TravelerPhoneViewModel(traveler.getOrCreatePrimaryPhoneNumber())
    val tsaViewModel = TravelerTSAViewModel(context, traveler)
    val advancedOptionsViewModel = TravelerAdvancedOptionsViewModel(traveler)

    init {
        nameViewModel.fullNameSubject.subscribe {name ->
            if (name.isNullOrEmpty()) emptyTravelerObservable.onNext(true) else emptyTravelerObservable.onNext(false)
            completenessStatusObservable.onNext(ContactDetailsCompletenessStatus.DEFAULT)
        }
    }

    fun validate(): Boolean {
        val nameValid = nameViewModel.validate()
        val phoneValid = phoneViewModel.validate()
        val tsaValid = tsaViewModel.validate()

        val valid = nameValid && phoneValid && tsaValid
        updateCompletenessStatus(valid)
        return valid
    }

    fun getTraveler(): Traveler {
        return traveler;
    }

    private fun updateCompletenessStatus(valid: Boolean): Unit {
        if (!valid) {
            completenessTextColorObservable.onNext(ContextCompat.getColor(context, R.color.traveler_incomplete_text_color))
            completenessStatusObservable.onNext(ContactDetailsCompletenessStatus.INCOMPLETE)
        } else {
            completenessTextColorObservable.onNext(ContextCompat.getColor(context, R.color.traveler_default_card_text_color))
            completenessStatusObservable.onNext(ContactDetailsCompletenessStatus.COMPLETE)
        }
    }
}