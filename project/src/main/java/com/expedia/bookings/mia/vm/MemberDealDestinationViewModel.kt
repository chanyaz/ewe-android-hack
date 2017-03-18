package com.expedia.bookings.mia.vm

import android.content.Context
import android.content.res.Resources
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.sos.MemberDealDestination
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.CurrencyUtils
import com.mobiata.android.text.StrikethroughTagHandler
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat

class MemberDealDestinationViewModel(val context: Context, val leadingHotel: MemberDealDestination.Hotel, val currency: String?) {

    val backgroundUrl: String? by lazy {
        getBackgroundImageUrl(leadingHotel.destination?.regionID)
    }
    var backgroundFallback: Int = R.color.gray6
    var backgroundPlaceHolder: Int = R.drawable.results_list_placeholder

    private val formatter = DateTimeFormat.forPattern("EEE, MMM dd")

    val cityName: String? = leadingHotel.destination?.shortName

    val dateRangeText: String by lazy {
        getDateRangeText(leadingHotel.offerDateRange?.travelStartDate, leadingHotel.offerDateRange?.travelEndDate)
    }

    val percentSavingsText: String by lazy {
        getPercentSavingsText(leadingHotel.hotelPricingInfo?.percentSavings)
    }

    val priceText: CharSequence by lazy {
        if (leadingHotel.hotelPricingInfo?.totalPriceValue != null) {
            getFormattedPriceText(context.resources, leadingHotel.hotelPricingInfo?.totalPriceValue, false)
        }
        else {
            getFormattedPriceText(context.resources, leadingHotel.hotelPricingInfo?.averagePriceValue, false)
        }
    }

    val strikeOutPriceText: CharSequence by lazy {
        getFormattedPriceText(context.resources, leadingHotel.hotelPricingInfo?.crossOutPriceValue, true)
    }

    fun getBackgroundImageUrl(regionId: String?): String? {
        if (regionId == null) {
            return null
        }

        return Constants.MOD_DESTINATION_IMAGE_BASE_URL.replace("{regionId}", leadingHotel.destination?.regionID!!)
    }

    fun getDateRangeText(startDate: List<Int>?, endDate: List<Int>?): String {
        if (startDate == null || endDate == null) {
            return ""
        }
        val dateRange = StringBuilder()
        val startDateStr = formatter.print(LocalDate(startDate[0], startDate[1], startDate[2]))
        val endDateStr = formatter.print(LocalDate(endDate[0], endDate[1], endDate[2]))

        return dateRange.append(startDateStr).append(" - ").append(endDateStr).toString()
    }

    fun getPercentSavingsText(percentSavings: Double?): String {
        if (percentSavings == null) {
            return ""
        }

        return StringBuilder("-").append(percentSavings.toInt()).append("%").toString()
    }

    fun getFormattedPriceText(resources: Resources, price: Double?, strikeOut: Boolean): CharSequence {
        if (price == null) {
            return ""
        }
        val money = Money(java.lang.Double.toString(price), currency).getFormattedMoney(Money.F_NO_DECIMAL)

        return if (strikeOut) HtmlCompat.fromHtml(resources.getString(R.string.strike_template, money), null, StrikethroughTagHandler()) else money
    }
}