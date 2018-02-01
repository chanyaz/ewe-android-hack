package com.expedia.bookings.itin.vm

import android.content.Context
import android.content.Intent
import com.expedia.bookings.R
import com.expedia.bookings.activity.WebViewActivity
import com.expedia.bookings.data.trips.ItinCardData
import com.expedia.bookings.data.trips.TripHotelRoom
import io.reactivex.subjects.PublishSubject

abstract class ItinModifyReservationViewModel(val context: Context) {

    abstract val itinType: String
    val changeReservationSubject = PublishSubject.create<Unit>()
    val cancelReservationSubject = PublishSubject.create<Unit>()
    var customerSupportNumber: String = ""
    val itinCardSubject = PublishSubject.create<ItinCardData>()
    val cancelTextViewClickSubject = PublishSubject.create<Unit>()
    val changeTextViewClickSubject = PublishSubject.create<Unit>()
    val cancelLearnMoreClickSubject = PublishSubject.create<Unit>()
    val changeLearnMoreClickSubject = PublishSubject.create<Unit>()
    val roomChangeSubject = PublishSubject.create<TripHotelRoom>()
    val webViewIntentSubject = PublishSubject.create<Intent>()
    var changeUrl: String? = ""
    var cancelUrl: String? = ""
    var helpDialogRes: Int = R.string.itin_flight_modify_widget_neither_changeable_nor_cancellable_reservation_dialog_text

    abstract fun publishChange(itinCardData: ItinCardData)

    protected fun buildWebViewIntent(title: Int, url: String?): Intent {
        val builder: WebViewActivity.IntentBuilder = WebViewActivity.IntentBuilder(context)
        builder.setTitle(title)
        builder.setUrl(url)
        builder.setInjectExpediaCookies(true)
        builder.setAllowMobileRedirects(true)
        return builder.intent
    }

    fun setupCancelAndChange(cancelable: Boolean, changeable: Boolean) {
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
}
