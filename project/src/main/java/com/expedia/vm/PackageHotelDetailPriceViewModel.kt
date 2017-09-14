package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.utils.StrUtils
import com.squareup.phrase.Phrase
import org.joda.time.format.DateTimeFormat
import java.math.BigDecimal

class PackageHotelDetailPriceViewModel(context: Context): BaseHotelDetailPriceViewModel(context) {

    override fun getPriceString(): String? {
        if (chargeableRateInfo == null || isSoldOut.value) {
            return null
        }

        val currencyCode = chargeableRateInfo!!.currencyCode
        if (showTotalPrice) {
            val totalPriceWithMandatoryFees = BigDecimal(chargeableRateInfo!!.totalPriceWithMandatoryFees.toDouble())
            return Money(totalPriceWithMandatoryFees, currencyCode).getFormattedMoney(Money.F_NO_DECIMAL)
        } else {
            return chargeableRateInfo!!.packagePricePerPerson.getFormattedMoney(Money.F_NO_DECIMAL)
        }
    }

    override fun getPerDescriptorString(): String? {
        if (isSoldOut.value) {
            return null
        }

        return " " + context.getString(R.string.price_per_person)
    }

    override fun getSearchInfoString(): String {
        val parsedSearchInfoString: String
        val dtf = DateTimeFormat.forPattern("yyyy-MM-dd")
        parsedSearchInfoString = Phrase.from(context, R.string.calendar_instructions_date_range_with_guests_TEMPLATE)
                .put("startdate", LocaleBasedDateFormatUtils.localDateToMMMd(dtf.parseLocalDate(Db.getPackageResponse().getHotelCheckInDate())))
                .put("enddate", LocaleBasedDateFormatUtils.localDateToMMMd(dtf.parseLocalDate(Db.getPackageResponse().getHotelCheckOutDate())))
                .put("guests", StrUtils.formatGuestString(context, hotelSearchParams.guests))
                .format().toString()
        return parsedSearchInfoString
    }

    override fun getPriceContainerContentDescriptionString(): String {
        if (getPriceString() == null || getPerDescriptorString() == null) {
            return ""
        }

        return getPriceString()!! + getPerDescriptorString()!!
    }
}
