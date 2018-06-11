package com.expedia.bookings.itin.cars.details

import com.expedia.bookings.R
import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.common.IMoreHelpViewModel
import com.expedia.bookings.itin.scopes.HasCarRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasTripsTracking
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinCar
import io.reactivex.subjects.PublishSubject

class CarItinMoreHelpViewModel<out S>(val scope: S) : IMoreHelpViewModel where S : HasStringProvider, S : HasCarRepo, S : HasLifecycleOwner, S : HasTripsTracking {
    override val phoneNumberSubject: PublishSubject<String> = PublishSubject.create()
    override val callButtonContentDescriptionSubject: PublishSubject<String> = PublishSubject.create()
    override val helpTextSubject: PublishSubject<String> = PublishSubject.create()
    override val confirmationNumberSubject: PublishSubject<String> = PublishSubject.create()
    override val confirmationTitleVisibilitySubject: PublishSubject<Boolean> = PublishSubject.create()
    override val confirmationNumberContentDescriptionSubject: PublishSubject<String> = PublishSubject.create()
    override val phoneNumberClickSubject: PublishSubject<Unit> = PublishSubject.create()

    val itinCarObserver: LiveDataObserver<ItinCar> = LiveDataObserver { car ->
        val vendorName = car?.carVendor?.shortName
        val phoneNumber = car?.carVendor?.localPhoneNumber

        vendorName?.let { name ->
            if (vendorName.isNotBlank()) {
                val helpText = scope.strings.fetchWithPhrase(
                        R.string.itin_more_help_text_TEMPLATE, mapOf("supplier" to name))
                helpTextSubject.onNext(helpText)
            }
        }

        phoneNumber?.let { number ->
            phoneNumberSubject.onNext(number)
            val contDesc = scope.strings.fetchWithPhrase(
                    R.string.itin_car_call_button_content_description_TEMPLATE,
                    mapOf("phonenumber" to number))
            callButtonContentDescriptionSubject.onNext(contDesc)
        }
    }

    val itinObserver: LiveDataObserver<Itin> = LiveDataObserver { itin ->
        val confirmationNumber = itin?.orderNumber
        confirmationTitleVisibilitySubject.onNext(!confirmationNumber.isNullOrBlank())
        confirmationNumber?.let { number ->
            confirmationNumberSubject.onNext(number)
            val contDesc = scope.strings.fetchWithPhrase(
                    R.string.itin_more_help_confirmation_number_content_description_TEMPLATE,
                    mapOf("number" to number))
            confirmationNumberContentDescriptionSubject.onNext(contDesc)
        }
    }

    init {
        phoneNumberClickSubject.subscribe {
            scope.tripsTracking.trackItinCarCallSupportClicked()
        }
        scope.itinCarRepo.liveDataCar.observe(scope.lifecycleOwner, itinCarObserver)
        scope.itinCarRepo.liveDataItin.observe(scope.lifecycleOwner, itinObserver)
    }
}
