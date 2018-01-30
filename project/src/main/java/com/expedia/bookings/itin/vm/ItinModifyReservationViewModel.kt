package com.expedia.bookings.itin.vm

import android.content.Intent
import com.expedia.bookings.data.trips.ItinCardData
import io.reactivex.subjects.PublishSubject

interface ItinModifyReservationViewModel {

    val changeReservationSubject: PublishSubject<Unit>
    val cancelReservationSubject: PublishSubject<Unit>
    var customerSupportNumberSubject: String
    val itinCardSubject: PublishSubject<ItinCardData>
    val cancelTextViewClickSubject: PublishSubject<Unit>
    val changeTextViewClickSubject: PublishSubject<Unit>
    val cancelLearnMoreClickSubject: PublishSubject<Unit>
    val changeLearnMoreClickSubject: PublishSubject<Unit>
    val webViewIntentSubject: PublishSubject<Intent>
    var changeUrl: String?
    var cancelUrl: String?
    var helpDialogRes: Int

    fun publishChange(itinCardData: ItinCardData)
}
