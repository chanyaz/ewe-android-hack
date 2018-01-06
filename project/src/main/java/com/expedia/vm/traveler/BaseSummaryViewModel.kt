package com.expedia.vm.traveler

import android.content.Context
import android.support.v4.content.ContextCompat
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.enums.TravelerCheckoutStatus
import com.expedia.bookings.widget.ContactDetailsCompletenessStatus
import rx.subjects.BehaviorSubject

abstract class BaseSummaryViewModel(val context: Context) {

    val resources = context.resources

    val iconStatusObservable = BehaviorSubject.create<ContactDetailsCompletenessStatus>()
    val titleObservable = BehaviorSubject.create<String>()
    val subtitleObservable = BehaviorSubject.create<String>()
    val subtitleColorObservable = BehaviorSubject.create<Int>()
    val travelerStatusObserver = BehaviorSubject.create<TravelerCheckoutStatus>(TravelerCheckoutStatus.CLEAN)

    abstract fun getTitle() : String
    abstract fun getSubtitle() : String
    abstract fun isTravelerEmpty(traveler: Traveler?) : Boolean
    abstract fun inject() // Can't inject components into a base class but need b4 subscribing travelerStatusObserver

    init {
        inject()
        travelerStatusObserver.subscribe { status ->
            val subTitle = resources.getString(R.string.enter_missing_traveler_details)
            if (status == TravelerCheckoutStatus.CLEAN) {
                if (Db.sharedInstance.travelers.size > 1 || isTravelerOneEmpty()) {
                    setTravelerSummaryInfo(getTitle(), getSubtitle(), ContactDetailsCompletenessStatus.DEFAULT)
                } else {
                    setTravelerSummaryInfo(getTitle(), subTitle, ContactDetailsCompletenessStatus.INCOMPLETE)
                }
            } else if (status == TravelerCheckoutStatus.DIRTY) {
                setTravelerSummaryInfo(getTitle(), subTitle, ContactDetailsCompletenessStatus.INCOMPLETE)
            } else {
                setTravelerSummaryInfo(getTitle(), getSubtitle(), ContactDetailsCompletenessStatus.COMPLETE)
            }
        }
    }

    fun isTravelerOneEmpty(): Boolean {
        val firstTraveler = getFirstTraveler()
        return isTravelerEmpty(firstTraveler)
    }

    fun getFirstTraveler(): Traveler? {
        if (Db.sharedInstance.travelers.isNotEmpty()) {
            return Db.sharedInstance.travelers[0]
        }
        return null
    }

    fun setTravelerSummaryInfo(title: String, subTitle: String, completenessStatus: ContactDetailsCompletenessStatus) {
        titleObservable.onNext(title)
        subtitleObservable.onNext(subTitle)
        iconStatusObservable.onNext(completenessStatus)
        if (ContactDetailsCompletenessStatus.INCOMPLETE == completenessStatus) {
            subtitleColorObservable.onNext(ContextCompat.getColor(context, R.color.traveler_incomplete_text_color))
        } else {
            subtitleColorObservable.onNext(ContextCompat.getColor(context, R.color.traveler_default_card_text_color))
        }
    }
}
