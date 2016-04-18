package com.expedia.vm.traveler

import android.content.Context
import android.support.v4.content.ContextCompat
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.enums.PassengerCategory
import com.expedia.bookings.enums.TravelerCheckoutStatus
import com.expedia.bookings.utils.validation.TravelerValidator
import com.expedia.bookings.widget.ContactDetailsCompletenessStatus
import com.squareup.phrase.Phrase
import rx.subjects.BehaviorSubject

open class TravelerSelectViewModel(val context: Context, val index: Int, val category: PassengerCategory) {
    val resources = context.resources
    val emptyText = Phrase.from(resources.getString(R.string.checkout_edit_traveler_TEMPLATE))
            .put("travelernumber", index + 1)
            .put("passengercategory", getPassengerString(category))
            .format().toString()

    val iconStatusObservable = BehaviorSubject.create<ContactDetailsCompletenessStatus>()
    val titleObservable = BehaviorSubject.create<String>()
    val subtitleObservable = BehaviorSubject.create<String>()
    val textColorObservable = BehaviorSubject.create<Int>()

    var status: TravelerCheckoutStatus

    init {
        setTravelerSummaryInfo(emptyText, "", ContactDetailsCompletenessStatus.DEFAULT)
        status = TravelerCheckoutStatus.CLEAN
    }

    fun updateStatus(status: TravelerCheckoutStatus) {
        this.status = status
        val traveler = getTraveler()
        textColorObservable.onNext(ContextCompat.getColor(context, R.color.traveler_default_card_text_color))
        if (status != TravelerCheckoutStatus.CLEAN) {
            if (isNameEmpty(traveler)) {
                setTravelerSummaryInfo(emptyText, "", ContactDetailsCompletenessStatus.INCOMPLETE)
                textColorObservable.onNext(ContextCompat.getColor(context, R.color.traveler_incomplete_text_color))
            } else if (isPhoneEmpty(traveler)) {
                setTravelerSummaryInfo(traveler.fullName, "", ContactDetailsCompletenessStatus.INCOMPLETE)
            } else if (!TravelerValidator.isValidForPackageBooking(traveler)) {
                setTravelerSummaryInfo(traveler.fullName, traveler.primaryPhoneNumber.number,
                        ContactDetailsCompletenessStatus.INCOMPLETE)
            } else {
                setTravelerSummaryInfo(traveler.fullName, traveler.primaryPhoneNumber.number,
                        ContactDetailsCompletenessStatus.COMPLETE)
            }
        }
    }

    private fun isNameEmpty(traveler: Traveler): Boolean{
        return traveler.fullName.isNullOrEmpty()
    }

    private fun isPhoneEmpty(traveler: Traveler): Boolean {
        return traveler.primaryPhoneNumber?.number.isNullOrEmpty()
    }

    private fun setTravelerSummaryInfo(title: String, subTitle: String, completenessStatus: ContactDetailsCompletenessStatus) {
        titleObservable.onNext(title)
        subtitleObservable.onNext(subTitle)
        iconStatusObservable.onNext(completenessStatus)
    }

    open fun getTraveler() : Traveler {
        return Db.getTravelers()[index]
    }

    private fun getPassengerString(category: PassengerCategory): String {
        if (category == PassengerCategory.INFANT_IN_LAP || category == PassengerCategory.INFANT_IN_SEAT) {
            return context.getString(R.string.ticket_type_infant)
        } else if (category == PassengerCategory.CHILD) {
            return context.getString(R.string.ticket_type_child)
        } else {
            return context.getString(R.string.ticket_type_adult)
        }
    }
}