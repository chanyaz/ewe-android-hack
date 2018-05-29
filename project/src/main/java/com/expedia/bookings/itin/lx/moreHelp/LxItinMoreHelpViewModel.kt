package com.expedia.bookings.itin.lx.moreHelp

import com.expedia.bookings.R
import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.common.IMoreHelpViewModel
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasLxRepo
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasTripsTracking
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinLx
import io.reactivex.subjects.PublishSubject

class LxItinMoreHelpViewModel<out S>(val scope: S) : IMoreHelpViewModel where S : HasStringProvider, S : HasLxRepo, S : HasLifecycleOwner, S : HasTripsTracking {
    override val phoneNumberSubject: PublishSubject<String> = PublishSubject.create()
    override val callButtonContentDescriptionSubject: PublishSubject<String> = PublishSubject.create()
    override val helpTextSubject: PublishSubject<String> = PublishSubject.create()
    override val confirmationNumberSubject: PublishSubject<String> = PublishSubject.create()
    override val confirmationTitleVisibilitySubject: PublishSubject<Boolean> = PublishSubject.create()
    override val confirmationNumberContentDescriptionSubject: PublishSubject<String> = PublishSubject.create()
    override val phoneNumberClickSubject: PublishSubject<Unit> = PublishSubject.create()

    var itinLxObserver: LiveDataObserver<ItinLx>
    var itinObserver: LiveDataObserver<Itin>

    init {
        itinLxObserver = LiveDataObserver { lx ->
            val vendorName = lx?.vendorCustomerServiceOffices?.firstOrNull()?.name
            val phoneNumber = lx?.vendorCustomerServiceOffices?.firstOrNull()?.phoneNumber

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
                        R.string.itin_lx_call_vendor_button_content_description_TEMPLATE,
                        mapOf("phonenumber" to number))
                callButtonContentDescriptionSubject.onNext(contDesc)
            }
        }

        itinObserver = LiveDataObserver { itin ->
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

        phoneNumberClickSubject.subscribe {
            scope.tripsTracking.trackItinLxCallSupportClicked()
        }

        scope.itinLxRepo.liveDataLx.observe(scope.lifecycleOwner, itinLxObserver)
        scope.itinLxRepo.liveDataItin.observe(scope.lifecycleOwner, itinObserver)
    }
}
