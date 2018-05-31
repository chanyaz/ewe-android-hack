package com.expedia.bookings.itin.hotel.pricingRewards

import android.content.Intent
import com.expedia.bookings.R
import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.common.ItinViewReceiptViewModel
import com.expedia.bookings.itin.scopes.HasFeature
import com.expedia.bookings.itin.scopes.HasHotelRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasTripsTracking
import com.expedia.bookings.itin.scopes.HasWebViewLauncher
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.extensions.firstHotel
import com.expedia.bookings.itin.tripstore.extensions.isMultiItemCheckout
import com.expedia.bookings.itin.tripstore.extensions.isPackage
import io.reactivex.subjects.PublishSubject

class HotelItinViewReceiptViewModel<out S>(val scope: S) : ItinViewReceiptViewModel where S : HasLifecycleOwner, S : HasStringProvider, S : HasHotelRepo, S : HasTripsTracking, S : HasWebViewLauncher, S : HasFeature {
    override val viewReceiptClickSubject: PublishSubject<Unit> = PublishSubject.create()
    override val webViewIntentSubject: PublishSubject<Intent> = PublishSubject.create()
    override var showReceiptSubject: PublishSubject<Unit> = PublishSubject.create()
    var itinObserver: LiveDataObserver<Itin>

    init {
        itinObserver = LiveDataObserver { itin ->
            if (!shouldShowViewReceipt(itin)) {
                return@LiveDataObserver
            }

            val receiptUrl = itin?.itineraryReceiptURL
            val hotelName = itin?.firstHotel()?.hotelPropertyInfo?.name

            if (!receiptUrl.isNullOrBlank() && !hotelName.isNullOrBlank()) {
                viewReceiptClickSubject.subscribe {
                    val title = scope.strings.fetchWithPhrase(R.string.itin_hotel_view_receipt_title_TEMPLATE, mapOf("hotelname" to hotelName!!))
                    scope.webViewLauncher.launchWebViewSharableActivity(title, receiptUrl!!, null, itin.tripId, itin.isGuest)
                    scope.tripsTracking.trackItinHotelViewReceipt()
                }
                showReceiptSubject.onNext(Unit)
            }
        }

        scope.itinHotelRepo.liveDataItin.observe(scope.lifecycleOwner, itinObserver)
    }

    fun shouldShowViewReceipt(itin: Itin?): Boolean {
        return !(itin == null || itin.isPackage() || itin.isMultiItemCheckout()) && scope.feature.enabled()
    }
}
