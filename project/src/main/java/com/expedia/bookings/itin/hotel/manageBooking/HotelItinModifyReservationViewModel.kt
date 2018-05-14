package com.expedia.bookings.itin.hotel.manageBooking

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.expedia.bookings.R
import com.expedia.bookings.activity.WebViewActivity
import com.expedia.bookings.data.trips.ItinCardData
import com.expedia.bookings.data.trips.TripHotel
import com.expedia.bookings.data.trips.ItinCardDataHotel
import com.expedia.bookings.itin.common.ItinModifyReservationViewModel
import com.expedia.bookings.itin.tripstore.utils.IJsonToItinUtil
import com.expedia.bookings.tracking.TripsTracking
import com.expedia.bookings.utils.Ui

class HotelItinModifyReservationViewModel(val context: Context) : ItinModifyReservationViewModel() {

    override val itinType: String = "HOTEL_ITIN"
    var readJsonUtil: IJsonToItinUtil = Ui.getApplication(context).tripComponent().jsonUtilProvider()

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
            webViewIntentSubject.onNext(buildWebViewIntent(R.string.itin_flight_modify_widget_cancel_reservation_text, cancelUrl, tripNumber))
            TripsTracking.trackHotelItinCancelHotel()
        }

        changeTextViewClickSubject.subscribe {
            webViewIntentSubject.onNext(buildWebViewIntent(R.string.itin_flight_modify_widget_change_reservation_text, changeUrl, tripNumber))
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
        tripId = data.tripId
        val cancelable = !cancelUrl.isNullOrEmpty() && hotelTrip.action != null && hotelTrip.action.isCancellable
        val changeable = !changeUrl.isNullOrEmpty() && hotelTrip.action != null && hotelTrip.action.isChangeable
        setupCancelAndChange(cancelable, changeable)
    }

    fun buildWebViewIntent(title: Int, url: String?, tripNumber: String?): Intent {
        val isGuest = readJsonUtil.getItin(tripId)?.isGuest
        return if (isGuest != null && isGuest) {
            Intent(Intent.ACTION_VIEW, Uri.parse(url))
        } else {
            val builder: WebViewActivity.IntentBuilder = WebViewActivity.IntentBuilder(context)
            builder.setTitle(title)
            builder.setUrl(url)
            builder.setInjectExpediaCookies(true)
            builder.setAllowMobileRedirects(true)
            builder.setItinTripIdForRefresh(tripNumber)
            builder.intent
        }
    }
}
