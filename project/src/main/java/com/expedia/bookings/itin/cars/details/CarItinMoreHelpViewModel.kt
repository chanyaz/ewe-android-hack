package com.expedia.bookings.itin.cars.details

import com.expedia.bookings.R
import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.common.IMoreHelpViewModel
import com.expedia.bookings.itin.flight.common.ItinOmnitureUtils
import com.expedia.bookings.itin.scopes.HasItinRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasTripsTracking
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.extensions.firstCar
import io.reactivex.subjects.PublishSubject

class CarItinMoreHelpViewModel<out S>(val scope: S) : IMoreHelpViewModel where S : HasStringProvider, S : HasLifecycleOwner, S : HasTripsTracking, S : HasItinRepo {
    override val phoneNumberSubject: PublishSubject<String> = PublishSubject.create()
    override val callButtonContentDescriptionSubject: PublishSubject<String> = PublishSubject.create()
    override val helpTextSubject: PublishSubject<String> = PublishSubject.create()
    override val confirmationNumberSubject: PublishSubject<String> = PublishSubject.create()
    override val confirmationTitleVisibilitySubject: PublishSubject<Boolean> = PublishSubject.create()
    override val confirmationNumberContentDescriptionSubject: PublishSubject<String> = PublishSubject.create()
    override val phoneNumberClickSubject: PublishSubject<Unit> = PublishSubject.create()
    val finishSubject: PublishSubject<Unit> = PublishSubject.create()
    val invalidSubject: PublishSubject<Unit> = PublishSubject.create()

    private var pageLoadingTracked = false

    val itinObserver: LiveDataObserver<Itin> = LiveDataObserver {
        it?.let { itin ->
            if (!pageLoadingTracked) {
                val omnitureValues = ItinOmnitureUtils.createOmnitureTrackingValuesNew(it, ItinOmnitureUtils.LOB.CAR)
                scope.tripsTracking.trackItinCarMoreHelpPageLoad(omnitureValues)
                pageLoadingTracked = true
            }
            val confirmationNumber = itin.orderNumber
            confirmationTitleVisibilitySubject.onNext(!confirmationNumber.isNullOrBlank())
            confirmationNumber?.let { number ->
                confirmationNumberSubject.onNext(number)
                val contDesc = scope.strings.fetchWithPhrase(
                        R.string.itin_more_help_confirmation_number_content_description_TEMPLATE,
                        mapOf("number" to number))
                confirmationNumberContentDescriptionSubject.onNext(contDesc)

                itin.firstCar()?.let { car ->
                    val vendorName = car.carVendor?.shortName
                    val phoneNumber = car.carVendor?.localPhoneNumber

                    vendorName?.let { name ->
                        if (vendorName.isNotBlank()) {
                            val helpText = scope.strings.fetchWithPhrase(
                                    R.string.itin_more_help_text_TEMPLATE, mapOf("supplier" to name))
                            helpTextSubject.onNext(helpText)
                        }
                    }

                    phoneNumber?.let { number ->
                        phoneNumberSubject.onNext(number)
                        vendorName?.let { name ->
                            val contDescVendorPhone = scope.strings.fetchWithPhrase(
                                    R.string.itin_car_call_button_content_description_TEMPLATE,
                                    mapOf("phonenumber" to number, "vendor" to name))
                            callButtonContentDescriptionSubject.onNext(contDescVendorPhone)
                        }
                    }
                }
            }
        }
    }

    init {
        phoneNumberClickSubject.subscribe {
            scope.tripsTracking.trackItinCarCallSupportClicked()
        }
        finishSubject.subscribe {
            scope.itinRepo.dispose()
        }
        scope.itinRepo.invalidDataSubject.subscribe {
            invalidSubject.onNext(Unit)
        }
        scope.itinRepo.liveDataItin.observe(scope.lifecycleOwner, itinObserver)
    }
}
