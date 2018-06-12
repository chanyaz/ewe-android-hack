package com.expedia.bookings.itin.common

import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.scopes.HasItinType
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasLxRepo
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasTripsTracking
import com.expedia.bookings.itin.scopes.HasWebViewLauncher
import com.expedia.bookings.itin.tripstore.data.Itin
import io.reactivex.subjects.PublishSubject

class ItinCustomerSupportViewModel<out S>(val scope: S) : ICustomerSupportViewModel where S : HasStringProvider, S : HasLxRepo, S : HasLifecycleOwner, S : HasTripsTracking, S : HasWebViewLauncher, S : HasItinType {
    override val customerSupportHeaderTextSubject: PublishSubject<String> = PublishSubject.create()
    override val phoneNumberSubject: PublishSubject<String> = PublishSubject.create()
    override val phoneNumberClickedSubject: PublishSubject<Unit> = PublishSubject.create()
    override val customerSupportTextSubject: PublishSubject<String> = PublishSubject.create()
    override val customerSupportButtonClickedSubject: PublishSubject<Unit> = PublishSubject.create()
    override val itineraryNumberSubject: PublishSubject<String> = PublishSubject.create()
    override val itineraryHeaderVisibilitySubject: PublishSubject<Boolean> = PublishSubject.create()
    override val phoneNumberContentDescriptionSubject: PublishSubject<String> = PublishSubject.create()
    override val customerSupportTextContentDescriptionSubject: PublishSubject<String> = PublishSubject.create()
    override val itineraryNumberContentDescriptionSubject: PublishSubject<String> = PublishSubject.create()

    val itinObserver: LiveDataObserver<Itin> = LiveDataObserver { itin ->
        val headerText = scope.strings.fetchWithPhrase(R.string.itin_customer_support_header_text_TEMPLATE,
                mapOf("brand" to BuildConfig.brand))
        customerSupportHeaderTextSubject.onNext(headerText)

        val tripNumber = itin?.tripNumber
        itineraryHeaderVisibilitySubject.onNext(!tripNumber.isNullOrBlank())

        tripNumber?.let { number ->
            itineraryNumberSubject.onNext(number)
            val contDesc =
                    scope.strings.fetchWithPhrase(R.string.itin_customer_support_itin_number_content_description_TEMPLATE,
                            mapOf("number" to number.replace(".".toRegex(), "$0 ")))
            itineraryNumberContentDescriptionSubject.onNext(contDesc)
        }

        itin?.customerSupport?.customerSupportPhoneNumberDomestic?.let { number ->
            phoneNumberSubject.onNext(number)
            val contDesc =
                    scope.strings.fetchWithPhrase(R.string.itin_call_support_button_content_description_TEMPLATE,
                            mapOf("brand" to BuildConfig.brand, "phonenumber" to number))
            phoneNumberContentDescriptionSubject.onNext(contDesc)
            phoneNumberClickedSubject.subscribe {
                trackingCallCustomerSupportClickBasedOnLOB()
            }
        }

        itin?.customerSupport?.customerSupportURL?.let { url ->
            val customerSupportText =
                    scope.strings.fetchWithPhrase(R.string.itin_hotel_customer_support_site_header_TEMPLATE,
                            mapOf("brand" to BuildConfig.brand))
            customerSupportTextSubject.onNext(customerSupportText)

            val contDesc =
                    scope.strings.fetchWithPhrase(R.string.itin_customer_support_site_button_content_description_TEMPLATE,
                            mapOf("brand" to BuildConfig.brand))
            customerSupportTextContentDescriptionSubject.onNext(contDesc)

            customerSupportButtonClickedSubject.subscribe {
                trackingCustomerServiceLinkClickBasedOnLOB()
                scope.webViewLauncher.launchWebViewActivity(R.string.itin_customer_service_webview_heading, url, null, itin.tripId!!, isGuest = itin.isGuest)
            }
        }
    }

    fun trackingCallCustomerSupportClickBasedOnLOB() {
        when (scope.type) {
            TripProducts.ACTIVITY.name -> {
                scope.tripsTracking.trackItinLxCallCustomerSupportClicked()
            }

            TripProducts.CAR.name -> {
                //scope.tripsTracking.trackItinCarCallCustomerSupportClicked()
            }
        }
    }

    fun trackingCustomerServiceLinkClickBasedOnLOB() {
        when (scope.type) {
            TripProducts.ACTIVITY.name -> {
                scope.tripsTracking.trackItinLxCustomerServiceLinkClicked()
            }

            TripProducts.CAR.name -> {
                //scope.tripsTracking.trackItinCarCustomerServiceLinkClicked()
            }
        }
    }

    init {
        scope.itinLxRepo.liveDataItin.observe(scope.lifecycleOwner, itinObserver)
    }
}
