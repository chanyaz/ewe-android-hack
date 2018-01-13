package com.expedia.vm.traveler

import android.content.Context
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.enums.MerchandiseSpam
import com.expedia.bookings.enums.TravelerCheckoutStatus
import com.squareup.phrase.Phrase
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class HotelTravelerEntryWidgetViewModel(val context: Context, travelerCheckoutStatus: TravelerCheckoutStatus,
                                        val createTripOptInStatus: BehaviorSubject<MerchandiseSpam>)
    : AbstractUniversalCKOTravelerEntryWidgetViewModel(context, 0) {

    val optInEmailStatusSubject = PublishSubject.create<MerchandiseSpam>()
    val checkboxTextSubject = PublishSubject.create<String>()
    val checkBoxVisibilitySubject = PublishSubject.create<Boolean>()
    val emailOptInSubject = PublishSubject.create<Boolean>()

    init {
        updateTraveler(getTraveler())
        if (travelerCheckoutStatus != TravelerCheckoutStatus.CLEAN) {
            validate()
        }
        showPhoneNumberObservable.onNext(true)
        optInEmailStatusSubject.subscribe { status ->
            updateMerchandiseStatus(status)
        }
    }

    override fun getTraveler(): Traveler {
        if (Db.sharedInstance.travelers.isNotEmpty()) {
            return Db.sharedInstance.travelers[0]
        } else {
            val traveler = Traveler()
            traveler.email = ""
            return traveler
        }
    }

    fun updateMerchandiseStatus(status: MerchandiseSpam) {
        if (!userStateManager.isUserAuthenticated()) {
            when (status) {
                MerchandiseSpam.ALWAYS -> {
                    emailOptInSubject.onNext(true)
                    checkBoxVisibilitySubject.onNext(false)
                }
                MerchandiseSpam.CONSENT_TO_OPT_IN -> {
                    val optInText = Phrase.from(context, R.string.hotel_checkout_merchandise_guest_opt_in_TEMPLATE)
                            .put("brand", BuildConfig.brand)
                            .format().toString()
                    checkboxTextSubject.onNext(optInText)
                    checkBoxVisibilitySubject.onNext(true)
                }
                MerchandiseSpam.CONSENT_TO_OPT_OUT -> {
                    val optOutText = Phrase.from(context, R.string.hotel_checkout_merchandise_guest_opt_out_TEMPLATE)
                            .put("brand", BuildConfig.brand)
                            .format().toString()
                    checkboxTextSubject.onNext(optOutText)
                    checkBoxVisibilitySubject.onNext(true)
                }
                else -> checkBoxVisibilitySubject.onNext(false)
            }
        }
    }
}
