package com.expedia.bookings.itin.hotel.pricingRewards

import android.content.Intent
import com.expedia.bookings.R
import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.extensions.ObservableOld
import com.expedia.bookings.itin.common.ItinViewReceiptViewModel
import com.expedia.bookings.itin.scopes.HasHotelRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasTripsTracking
import com.expedia.bookings.itin.scopes.HasWebViewLauncher
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinHotel
import io.reactivex.subjects.PublishSubject

class HotelItinViewReceiptViewModel<S>(val scope: S) : ItinViewReceiptViewModel where S : HasLifecycleOwner, S : HasStringProvider, S : HasHotelRepo, S : HasTripsTracking, S : HasWebViewLauncher {
    override val viewReceiptClickSubject: PublishSubject<Unit> = PublishSubject.create<Unit>()
    override val webViewIntentSubject = PublishSubject.create<Intent>()
    override var showReceipt: PublishSubject<Unit> = PublishSubject.create()
    var itinObserver: LiveDataObserver<Itin>
    var itinHotelObserver: LiveDataObserver<ItinHotel>
    val receiptSubject: PublishSubject<HotelItinReceipt> = PublishSubject.create()
    val titleSubject: PublishSubject<String> = PublishSubject.create()

    init {
        itinHotelObserver = LiveDataObserver { itinHotel ->
            if (itinHotel != null) {
                val stringProvider = scope.strings
                val hotelName = itinHotel.hotelPropertyInfo?.name
                if (!hotelName.isNullOrEmpty()) {
                    val title = stringProvider.fetchWithPhrase(R.string.itin_hotel_view_receipt_title_TEMPLATE, mapOf("hotelname" to hotelName!!))
                    titleSubject.onNext(title)
                }
            }
        }

        itinObserver = LiveDataObserver { it ->
            val itin: Itin = it!!
            val tripID = itin.tripId
            val url = itin.itineraryReceiptURL
            val isGuest: Boolean = itin.isGuest
            if (!url.isNullOrEmpty()) {
                receiptSubject.onNext(HotelItinReceipt(url!!, tripID, isGuest))
            }
        }

        scope.itinHotelRepo.liveDataItin.observe(scope.lifecycleOwner, itinObserver)
        scope.itinHotelRepo.liveDataHotel.observe(scope.lifecycleOwner, itinHotelObserver)

        ObservableOld.zip(receiptSubject, titleSubject, { receipt, title ->
            object {
                val receipt = receipt
                val title = title
            }
        }).subscribe { obj ->
            viewReceiptClickSubject.subscribe {
                scope.tripsTracking.trackItinHotelViewReceipt()
                scope.webViewLauncher.launchWebViewSharableActivity(obj.title, obj.receipt.url, null, obj.receipt.tripID, obj.receipt.isGuest)
            }
            showReceipt.onNext(Unit)
        }
    }

    data class HotelItinReceipt(var url: String, var tripID: String?, val isGuest: Boolean = false)
}
