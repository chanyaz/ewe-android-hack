package com.expedia.bookings.itin.vm

import android.content.Context
import android.content.Intent
import com.expedia.bookings.R
import com.expedia.bookings.activity.WebViewActivity
import com.expedia.bookings.data.trips.ItinCardData
import com.expedia.bookings.data.trips.ItinCardDataFlight
import com.expedia.bookings.data.trips.TripFlight
import com.expedia.bookings.tracking.OmnitureTracking
import io.reactivex.subjects.PublishSubject

class FlightItinModifyReservationViewModel(val context: Context) : ItinModifyReservationViewModel {
    override val cancelReservationSubject: PublishSubject<Unit> = PublishSubject.create()
    override var customerSupportNumberSubject = ""
    override val itinCardSubject: PublishSubject<ItinCardData> = PublishSubject.create()
    override val changeReservationSubject: PublishSubject<Unit> = PublishSubject.create()
    override val cancelTextViewClickSubject: PublishSubject<Unit> = PublishSubject.create()
    override val changeTextViewClickSubject: PublishSubject<Unit> = PublishSubject.create()
    override val cancelLearnMoreClickSubject: PublishSubject<Unit> = PublishSubject.create()
    override val changeLearnMoreClickSubject: PublishSubject<Unit> = PublishSubject.create()
    override val webViewIntentSubject: PublishSubject<Intent> = PublishSubject.create()
    override var changeUrl: String? = ""
    override var cancelUrl: String? = ""
    override var helpDialogRes = R.string.itin_flight_modify_widget_neither_changeable_nor_cancellable_reservation_dialog_text

    init {
        itinCardSubject.subscribe {
            publishChange(it)
        }
        cancelTextViewClickSubject.subscribe {
            OmnitureTracking.trackFlightItinCancelFlight()
            webViewIntentSubject.onNext(buildWebViewIntent(R.string.itin_flight_modify_widget_cancel_reservation_text, cancelUrl))
        }
        changeTextViewClickSubject.subscribe {
            OmnitureTracking.trackFlightItinChangeFlight()
            webViewIntentSubject.onNext(buildWebViewIntent(R.string.itin_flight_modify_widget_change_reservation_text, changeUrl))
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
        customerSupportNumberSubject = itinCardData.tripComponent.parentTrip.customerSupport.supportPhoneNumberDomestic
        cancelUrl = flightTrip.webCancelPathURL
        val cancelable = !cancelUrl.isNullOrEmpty() && flightTrip.action.isCancellable
        val changeable = !changeUrl.isNullOrEmpty() && flightTrip.action.isChangeable
        if (cancelable) {
            cancelReservationSubject.onNext(Unit)
        } else {
            helpDialogRes = R.string.itin_flight_modify_widget_cancel_reservation_dialog_text
        }
        if (changeable) {
            changeReservationSubject.onNext(Unit)
        } else {
            helpDialogRes = R.string.itin_flight_modify_widget_change_reservation_dialog_text
        }
        if (!cancelable && !changeable) {
            helpDialogRes = R.string.itin_flight_modify_widget_neither_changeable_nor_cancellable_reservation_dialog_text
        }
    }

    fun buildWebViewIntent(title: Int, url: String?): Intent {
        val builder: WebViewActivity.IntentBuilder = WebViewActivity.IntentBuilder(context)
        builder.setTitle(title)
        builder.setUrl(url)
        builder.setInjectExpediaCookies(true)
        builder.setAllowMobileRedirects(true)
        return builder.intent
    }
}
