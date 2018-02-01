package com.expedia.bookings.itin.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItinCardData
import com.expedia.bookings.data.trips.TripHotel
import com.expedia.bookings.itin.data.ItinCardDataHotel
import com.expedia.bookings.tracking.OmnitureTracking

class HotelItinModifyReservationViewModel(context: Context) : ItinModifyReservationViewModel(context) {

    override val itinType: String = "HOTEL_ITIN"

    init {
        itinCardSubject.subscribe {
            publishChange(it)
        }

        roomChangeSubject.subscribe {
            if (it.roomCancelLink.isNotEmpty()) {
                cancelUrl = it.roomCancelLink
                cancelReservationSubject.onNext(Unit)
            }

            if (it.roomChangeLinkForMobileWebView.isNotEmpty()) {
                changeUrl = it.roomChangeLinkForMobileWebView
                changeReservationSubject.onNext(Unit)
            } else if (it.roomChangeLink.isNotEmpty()) {
                changeUrl = it.roomChangeLink
                changeReservationSubject.onNext(Unit)
            }
        }

        cancelTextViewClickSubject.subscribe {
            webViewIntentSubject.onNext(buildWebViewIntent(R.string.itin_flight_modify_widget_cancel_reservation_text, cancelUrl))
            OmnitureTracking.trackHotelItinCancelHotel()
        }

        changeTextViewClickSubject.subscribe {
            webViewIntentSubject.onNext(buildWebViewIntent(R.string.itin_flight_modify_widget_change_reservation_text, changeUrl))
            OmnitureTracking.trackHotelItinChangeHotel()
        }

        cancelLearnMoreClickSubject.subscribe {
            OmnitureTracking.trackItinHotelCancelLearnMore()
        }

        changeLearnMoreClickSubject.subscribe {
            OmnitureTracking.trackItinHotelChangeLearnMore()
        }
    }

    override fun publishChange(itinCardData: ItinCardData) {
        val data = itinCardData as ItinCardDataHotel
        val hotelTrip = data.tripComponent as TripHotel
        customerSupportNumber = data.tripComponent.parentTrip.customerSupport.supportPhoneNumberDomestic
        val cancelable = !cancelUrl.isNullOrEmpty() && hotelTrip.action != null && hotelTrip.action.isCancellable
        val changeable = !changeUrl.isNullOrEmpty() && hotelTrip.action != null && hotelTrip.action.isChangeable
        setupCancelAndChange(cancelable, changeable)
    }
}
