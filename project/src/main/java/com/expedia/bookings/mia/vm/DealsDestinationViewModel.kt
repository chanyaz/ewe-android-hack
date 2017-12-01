package com.expedia.bookings.mia.vm

import android.content.Context
import android.content.res.Resources
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.sos.DealsDestination
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.mobiata.android.text.StrikethroughTagHandler
import com.squareup.phrase.Phrase
import org.joda.time.DateTime
import org.joda.time.LocalDate

class DealsDestinationViewModel(val context: Context, val leadingHotel: DealsDestination.Hotel, val currency: String?) {

    val backgroundUrl: String? by lazy {
        getBackgroundImageUrl(leadingHotel.destination?.regionID)
    }
    var backgroundFallback: Int = R.color.gray600
    var backgroundPlaceHolder: Int = R.drawable.results_list_placeholder

    val cityName: String? = leadingHotel.destination?.shortName ?: leadingHotel.destination?.city

    val regionId: String? = leadingHotel.destination?.regionID

    val startDate: LocalDate by lazy {
        getDateInLocalDateFormat(leadingHotel.offerDateRange?.travelStartDate)
    }

    val endDate: LocalDate by lazy {
        getDateInLocalDateFormat(leadingHotel.offerDateRange?.travelEndDate)
    }

    val dateRangeText: String by lazy {
        getDateRangeText(startDate, endDate)
    }

    val percentSavingsText: String by lazy {
        getPercentSavingsText(leadingHotel.hotelPricingInfo?.percentSavings)
    }

    val discountPercent: String by lazy {
        getDiscountPercentForContentDesc(leadingHotel.hotelPricingInfo?.percentSavings)
    }

    val priceText: CharSequence by lazy {
        val totalPriceValue = leadingHotel.hotelPricingInfo?.totalPriceValue
        if (totalPriceValue != null && totalPriceValue > 0.0) {
            getFormattedPriceText(context.resources, leadingHotel.hotelPricingInfo?.totalPriceValue, false)
        } else {
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

        return Constants.SOS_DESTINATION_IMAGE_BASE_URL.replace("{regionId}", leadingHotel.destination?.regionID!!)
    }

    fun getDateRangeText(startDate: LocalDate?, endDate: LocalDate?): String {
        if (startDate == null || endDate == null) {
            return ""
        }
        val startDateStr = LocaleBasedDateFormatUtils.localDateToEEEMMMd(startDate)
        val endDateStr = LocaleBasedDateFormatUtils.localDateToEEEMMMd(endDate)

        return Phrase.from(context, R.string.start_dash_end_date_range_TEMPLATE)
                .put("startdate", startDateStr).put("enddate", endDateStr)
                .format().toString()
    }

    fun getDateInLocalDateFormat(intDate: List<Int>?): LocalDate {
        if (intDate == null) {
            return DateTime.now().toLocalDate()
        }

        return LocalDate(intDate[0], intDate[1], intDate[2])
    }


    fun getPercentSavingsText(percentSavings: Double?): String {
        val percentDiscount = calculatePercentDiscount(percentSavings)
        if (!percentDiscount.isEmpty()) {
            return StringBuilder("-").append(percentDiscount).append("%").toString()
        } else {
            return percentDiscount
        }
    }

    fun getDiscountPercentForContentDesc(percentSavings: Double?): String {
        val percentDiscount = calculatePercentDiscount(percentSavings)
        if (!percentDiscount.isEmpty()) {
            return Phrase.from(context, R.string.hotel_discount_percent_Template).put("discount", percentDiscount).format().toString()
        } else {
            return ""
        }
    }

    private fun calculatePercentDiscount(percentSavingsValue: Double?): String {
        val crossOutPriceValue = leadingHotel.hotelPricingInfo?.crossOutPriceValue
        val totalPriceValue = leadingHotel.hotelPricingInfo?.totalPriceValue
        if (percentSavingsValue == null) {
            return calculatePercentDiscount(crossOutPriceValue, totalPriceValue)
        }
        return percentSavingsValue.toInt().toString()
    }

    private fun calculatePercentDiscount(crossOutPriceValue: Double?, totalPriceValue: Double?): String {
        if (crossOutPriceValue != null && crossOutPriceValue > 0 && totalPriceValue != null) {
            return ((crossOutPriceValue - totalPriceValue) / crossOutPriceValue * 100).toInt().toString()
        } else {
            return ""
        }
    }

    fun getFormattedPriceText(resources: Resources, price: Double?, strikeOut: Boolean): CharSequence {
        if (price == null) {
            return ""
        }
        val money = Money(java.lang.Double.toString(price), currency).getFormattedMoney(Money.F_NO_DECIMAL)
        return if (strikeOut) HtmlCompat.fromHtml(resources.getString(R.string.strike_template, money), null, StrikethroughTagHandler()) else money
    }
}