package com.expedia.bookings.itin.hotel.manageBooking

import android.content.Context
import android.content.Intent
import com.expedia.bookings.R
import com.expedia.bookings.activity.WebViewActivity
import com.expedia.bookings.data.trips.ItinCardData
import com.expedia.bookings.data.trips.TripHotel
import com.expedia.bookings.data.trips.ItinCardDataHotel
import com.expedia.bookings.itin.common.ItinModifyReservationViewModel
import com.expedia.bookings.tracking.TripsTracking

class HotelItinModifyReservationViewModel(val context: Context) : ItinModifyReservationViewModel() {

    override val itinType: String = "HOTEL_ITIN"
    var tripNumber: String = ""

    init {
        itinCardSubject.subscribe {
            publishChange(it)
        }

        roomChangeSubject.subscribe {
            if (!it.roomCancelLink.isNullOrEmpty()) {
                cancelUrl = it.roomCancelLink
                cancelReservationSubject.onNext(Unit)
            }
            if (!it.roomChangeLinkForMobileWebView.isNullOrEmpty()) {
                changeUrl = it.roomChangeLinkForMobileWebView
                changeReservationSubject.onNext(Unit)
            } else if (!it.roomChangeLink.isNullOrEmpty()) {
                changeUrl = it.roomChangeLink
                changeReservationSubject.onNext(Unit)
            }
        }

        cancelTextViewClickSubject.subscribe {
            webViewRefreshOnExitIntentSubject.onNext(buildWebViewIntent(R.string.itin_flight_modify_widget_cancel_reservation_text, cancelUrl, tripNumber))
            TripsTracking.trackHotelItinCancelHotel()
        }

        changeTextViewClickSubject.subscribe {
            webViewRefreshOnExitIntentSubject.onNext(buildWebViewIntent(R.string.itin_flight_modify_widget_change_reservation_text, changeUrl, tripNumber))
            TripsTracking.trackHotelItinChangeHotel()
        }

        cancelLearnMoreClickSubject.subscribe {
            TripsTracking.trackItinHotelCancelLearnMore()
        }

        changeLearnMoreClickSubject.subscribe {
            TripsTracking.trackItinHotelChangeLearnMore()
        }
    }

    override fun publishChange(itinCardData: ItinCardData) {
        val data = itinCardData as ItinCardDataHotel
        val hotelTrip = data.tripComponent as TripHotel
        customerSupportNumber = data.tripComponent.parentTrip.customerSupport.supportPhoneNumberDomestic
        tripNumber = data.tripNumber
        val cancelable = !cancelUrl.isNullOrEmpty() && hotelTrip.action != null && hotelTrip.action.isCancellable
        val changeable = !changeUrl.isNullOrEmpty() && hotelTrip.action != null && hotelTrip.action.isChangeable
        setupCancelAndChange(cancelable, changeable)
    }

    fun buildWebViewIntent(title: Int, url: String?, tripId: String): Intent {
        val builder: WebViewActivity.IntentBuilder = WebViewActivity.IntentBuilder(context)
        builder.setTitle(title)
        builder.setUrl(url)
        builder.setInjectExpediaCookies(true)
        builder.setAllowMobileRedirects(true)
        builder.setItinTripIdForRefresh(tripId)
        return builder.intent
    }
}
