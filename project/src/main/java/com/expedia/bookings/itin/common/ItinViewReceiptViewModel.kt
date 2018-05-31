package com.expedia.bookings.itin.common

import android.content.Intent
import io.reactivex.subjects.PublishSubject

interface ItinViewReceiptViewModel {

    val viewReceiptClickSubject: PublishSubject<Unit>
    val webViewIntentSubject: PublishSubject<Intent>
    val showReceiptSubject: PublishSubject<Unit>
}
