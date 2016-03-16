package com.expedia.vm.traveler

import android.content.Context
import android.support.v4.content.ContextCompat
import com.expedia.bookings.R
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.widget.ContactDetailsCompletenessStatus
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class TravelerViewModel(private val context: Context, val travelerNumber: Int) {
    val emptyTravelerObservable = PublishSubject.create<Boolean>()
    val travelerObservable = BehaviorSubject.create<Traveler>()
    val completenessStatusObservable = PublishSubject.create<ContactDetailsCompletenessStatus>()
    val completenessTextColorObservable = PublishSubject.create<Int>()

    var nameViewModel = TravelerNameViewModel()
    var phoneViewModel = TravelerPhoneViewModel()
    var tsaViewModel = TravelerTSAViewModel(context)
    var advancedOptionsViewModel = TravelerAdvancedOptionsViewModel()

    init {
        nameViewModel.fullNameSubject.subscribe { name ->
            if (name.isNullOrEmpty()) emptyTravelerObservable.onNext(true) else emptyTravelerObservable.onNext(false)
            completenessStatusObservable.onNext(ContactDetailsCompletenessStatus.DEFAULT)
        }

        travelerObservable.subscribe { traveler ->
            nameViewModel.updateTravelerName(traveler.name)
            phoneViewModel.updatePhone(traveler.orCreatePrimaryPhoneNumber)
            tsaViewModel.updateTraveler(traveler)
            advancedOptionsViewModel.updateTraveler(traveler)
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
        return travelerObservable.value;
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