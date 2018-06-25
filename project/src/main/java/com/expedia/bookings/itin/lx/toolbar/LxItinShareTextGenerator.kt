package com.expedia.bookings.itin.lx.toolbar

import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.itin.common.TripProducts
import com.expedia.bookings.itin.tripstore.data.Traveler
import com.expedia.bookings.itin.utils.IPOSInfoProvider
import com.expedia.bookings.itin.utils.ItinShareTextGenerator
import com.expedia.bookings.itin.utils.StringSource

class LxItinShareTextGenerator(val trip: String,
                               val startDate: String,
                               val endDate: String,
                               val travelers: List<Traveler>?,
                               private val stringSource: StringSource,
                               private val posInfoProvider: IPOSInfoProvider) : ItinShareTextGenerator {

    override fun getLOBTypeString(): String {
        return TripProducts.ACTIVITY.name.toLowerCase().capitalize()
    }

    override fun getEmailSubject(): String {
        return stringSource.fetchWithPhrase(R.string.itin_lx_share_email_subject_TEMPLATE, mapOf("trip" to trip))
    }

    override fun getEmailBody(): String {
        return stringSource.fetchWithPhrase(R.string.itin_lx_share_email_body_TEMPLATE,
                mapOf("trip" to trip, "startdate" to startDate,
                        "enddate" to endDate, "travelers" to getTravelersString(), "brand" to BuildConfig.brand,
                        "link" to posInfoProvider.getAppInfoURL()))
    }

    override fun getSmsBody(): String {
        return stringSource.fetchWithPhrase(R.string.itin_lx_share_sms_body_TEMPLATE,
                mapOf("trip" to trip, "startdate" to startDate, "enddate" to endDate, "travelers" to getTravelersString()))
    }

    private fun getTravelersString(): String {
        var travelersString = ""
        travelers?.let { travelersList ->
            for (i in 0.until(travelersList.size)) {
                val traveler = travelersList[i]
                travelersString += if (i == travelersList.size - 1) traveler.fullName else "${traveler.fullName}, "
            }
        }
        return travelersString
    }
}
