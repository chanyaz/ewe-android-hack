package com.expedia.bookings.utils

import android.content.Context
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.Db
import org.joda.time.LocalDate

class PaymentTestUtils {

    companion object {
        fun setUpCompletePayment(context: Context) {
            val info = BillingInfo()
            info.setNumberAndDetectType("345104799171123", context)
            info.nameOnCard = "Expedia Chicago"
            info.expirationDate = LocalDate(2017, 1, 1)
            info.securityCode = "123"
            Db.sharedInstance.temporarilySavedCard = info
            Db.sharedInstance.temporarilySavedCard.saveCardToExpediaAccount = true
        }
    }
}
