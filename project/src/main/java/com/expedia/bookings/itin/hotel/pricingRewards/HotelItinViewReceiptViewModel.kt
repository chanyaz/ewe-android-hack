package com.expedia.bookings.itin.hotel.pricingRewards

import android.content.Intent
import com.expedia.bookings.R
import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.features.Features
import com.expedia.bookings.itin.common.ItinViewReceiptViewModel
import com.expedia.bookings.itin.scopes.HasFeatureProvider
import com.expedia.bookings.itin.scopes.HasItinRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasTripsTracking
import com.expedia.bookings.itin.scopes.HasWebViewLauncher
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinHotel
import com.expedia.bookings.itin.tripstore.data.PaymentModel
import com.expedia.bookings.itin.tripstore.extensions.firstHotel
import com.expedia.bookings.itin.tripstore.extensions.isMultiItemCheckout
import com.expedia.bookings.itin.tripstore.extensions.isPackage
import io.reactivex.subjects.PublishSubject

class HotelItinViewReceiptViewModel<out S>(val scope: S) : ItinViewReceiptViewModel where S : HasLifecycleOwner, S : HasStringProvider, S : HasItinRepo, S : HasTripsTracking, S : HasWebViewLauncher, S : HasFeatureProvider {
    override val viewReceiptClickSubject: PublishSubject<Unit> = PublishSubject.create()
    override val webViewIntentSubject: PublishSubject<Intent> = PublishSubject.create()
    override var showReceiptSubject: PublishSubject<Unit> = PublishSubject.create()
    val itinObserver: LiveDataObserver<Itin> = LiveDataObserver { itin ->
        val hotel = itin?.firstHotel()
        if (!shouldShowViewReceipt(itin, hotel)) {
            return@LiveDataObserver
        }

        val receiptUrl = itin?.itineraryReceiptURL
        val hotelName = hotel?.hotelPropertyInfo?.name

        if (!receiptUrl.isNullOrBlank() && !hotelName.isNullOrBlank()) {
            viewReceiptClickSubject.subscribe {
                val title = scope.strings.fetchWithPhrase(R.string.itin_hotel_view_receipt_title_TEMPLATE, mapOf("hotelname" to hotelName!!))
                scope.webViewLauncher.launchWebViewSharableActivity(title, receiptUrl!!, null, itin.tripId, itin.isGuest)
                scope.tripsTracking.trackItinHotelViewReceipt()
            }
            showReceiptSubject.onNext(Unit)
        }
    }

    init {
        scope.itinRepo.liveDataItin.observe(scope.lifecycleOwner, itinObserver)
    }

    fun shouldShowViewReceipt(itin: Itin?, hotel: ItinHotel?): Boolean {
        if (itin == null || hotel == null) {
            return false
        } else {
            val isItinBundle = itin.isPackage() || itin.isMultiItemCheckout()
            val isHotelPayLater = (hotel.paymentModel == PaymentModel.HOTEL_COLLECT)
            val isViewReceiptFeatureEnabled = scope.features.isFeatureEnabled(Features.all.viewReceipt)
            return !(isItinBundle || isHotelPayLater) && isViewReceiptFeatureEnabled
        }
    }
}
