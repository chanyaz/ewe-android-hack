package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.squareup.phrase.Phrase
import org.joda.time.Days
import org.joda.time.DateTime

class KrazyglueHotelSeeMoreHolderViewModel(val context: Context, val departureDate: DateTime) {

    fun getOfferValidDate(): String {
        val noOfDays: Int
        val currentDate = DateTime()
        var noOfDaysBeforeTraveling = Days.daysBetween(currentDate.withTimeAtStartOfDay(), departureDate.withTimeAtStartOfDay()).days
        if (noOfDaysBeforeTraveling > 7) {
            noOfDays = 7
        } else if (noOfDaysBeforeTraveling == 1 || noOfDaysBeforeTraveling == 0) {
            noOfDays = 1
        } else {
            noOfDays = noOfDaysBeforeTraveling
        }

        return Phrase.from(context.getResources().getQuantityString(R.plurals.krazy_glue_offer_expire_TEMPLATE, noOfDaysBeforeTraveling))
                .put("no_of_days", noOfDays)
                .format().toString()
    }
}
