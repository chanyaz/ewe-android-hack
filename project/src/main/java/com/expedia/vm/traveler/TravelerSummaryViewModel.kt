package com.expedia.vm.traveler

import android.content.Context
import android.support.v4.content.ContextCompat
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.widget.ContactDetailsCompletenessStatus
import com.squareup.phrase.Phrase
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

open class TravelerSummaryViewModel(val context: Context) {
    val resources = context.resources

    val travelersComplete = BehaviorSubject.create<Unit>()
    val incompleteTravelers = PublishSubject.create<Unit>()
    val emptyTravelers = PublishSubject.create<Unit>()

    val iconStatusObservable = PublishSubject.create<ContactDetailsCompletenessStatus>()
    val titleObservable = PublishSubject.create<String>()
    val subtitleObservable = PublishSubject.create<String>()
    val subtitleColorObservable = PublishSubject.create<Int>()

    init {
        emptyTravelers.subscribe {
            val title = resources.getString(R.string.checkout_enter_traveler_details)
            val subTitle = resources.getString(R.string.checkout_enter_traveler_details_line2)
            setTravelerSummaryInfo(title, subTitle, ContactDetailsCompletenessStatus.DEFAULT)
        }

        incompleteTravelers.subscribe {
            setTravelerSummaryInfo(getTitle(), getSubtitle(), ContactDetailsCompletenessStatus.INCOMPLETE)
        }

        travelersComplete.subscribe {
            setTravelerSummaryInfo(getTitle(), getSubtitle(), ContactDetailsCompletenessStatus.COMPLETE)
        }
    }

    private fun setTravelerSummaryInfo(title: String, subTitle: String, completenessStatus: ContactDetailsCompletenessStatus) {
        titleObservable.onNext(title)
        subtitleObservable.onNext(subTitle)
        iconStatusObservable.onNext(completenessStatus)
        if (ContactDetailsCompletenessStatus.INCOMPLETE == completenessStatus) {
            subtitleColorObservable.onNext(ContextCompat.getColor(context, R.color.traveler_incomplete_text_color))
        } else {
            subtitleColorObservable.onNext(ContextCompat.getColor(context, R.color.traveler_default_card_text_color))
        }
    }

    protected fun getTitle(): String {
        var traveler = getFirstTraveler()
        if (traveler?.fullName.isNullOrEmpty()) {
            return resources.getString(R.string.checkout_enter_traveler_details)
        } else {
            return traveler!!.fullName
        }
    }

    private fun getSubtitle(): String {
        var numberOfTravelers = Db.getTravelers().size
        if (numberOfTravelers > 1) {
            return Phrase.from(resources.getString(R.string.checkout_more_travelers_TEMPLATE))
                    .put("travelercount", numberOfTravelers - 1).format().toString()
        }

        var traveler = getFirstTraveler()
        if (traveler?.primaryPhoneNumber?.number.isNullOrEmpty()) {
            return resources.getString(R.string.checkout_enter_traveler_details_line2)
        } else {
            return traveler!!.primaryPhoneNumber.number
        }
    }

    fun getFirstTraveler(): Traveler? {
        if (Db.getTravelers().isNotEmpty()) {
            return Db.getTravelers()[0]
        }
        return null
    }
}