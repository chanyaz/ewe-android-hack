package com.expedia.bookings.itin.flight.manageBooking

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.expedia.bookings.R
import com.expedia.bookings.activity.WebViewActivity
import com.expedia.bookings.data.trips.ItinCardData
import com.expedia.bookings.data.trips.ItinCardDataFlight
import com.expedia.bookings.data.trips.TripFlight
import com.expedia.bookings.itin.common.ItinModifyReservationViewModel
import com.expedia.bookings.itin.tripstore.utils.IJsonToItinUtil
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.Ui

class FlightItinModifyReservationViewModel(val context: Context) : ItinModifyReservationViewModel() {

    override val itinType: String = "FLIGHT_ITIN"
    var readJsonUtil: IJsonToItinUtil = Ui.getApplication(context).appComponent().jsonUtilProvider()

    init {
        itinCardSubject.subscribe {
            publishChange(it)
        }
        cancelTextViewClickSubject.subscribe {
            OmnitureTracking.trackFlightItinCancelFlight()
            webViewIntentSubject.onNext(buildWebViewIntent(R.string.itin_flight_modify_widget_cancel_reservation_text, cancelUrl, tripNumber))
        }
        changeTextViewClickSubject.subscribe {
            OmnitureTracking.trackFlightItinChangeFlight()
            webViewIntentSubject.onNext(buildWebViewIntent(R.string.itin_flight_modify_widget_change_reservation_text, changeUrl, tripNumber))
        }
        cancelLearnMoreClickSubject.subscribe {
            OmnitureTracking.trackItinFlightCancelLearnMore()
        }
        changeLearnMoreClickSubject.subscribe {
            OmnitureTracking.trackItinFlightChangeLearnMore()
        }
    }

    override fun publishChange(itinCardData: ItinCardData) {
        val data = itinCardData as ItinCardDataFlight
        val flightTrip = (data.tripComponent as TripFlight).flightTrip
        changeUrl = flightTrip.webChangePathURL
        tripNumber = data.tripNumber
        tripId = data.tripId
        customerSupportNumber = itinCardData.tripComponent.parentTrip.customerSupport.supportPhoneNumberDomestic
        cancelUrl = flightTrip.webCancelPathURL
        val cancelable = !cancelUrl.isNullOrEmpty() && flightTrip.action.isCancellable
        val changeable = !changeUrl.isNullOrEmpty() && flightTrip.action.isChangeable
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
