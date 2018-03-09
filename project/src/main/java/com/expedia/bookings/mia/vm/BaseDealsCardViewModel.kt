package com.expedia.bookings.mia.vm

import android.content.Context
import android.content.res.Resources
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.sos.DealsDestination
import com.expedia.bookings.enums.DiscountColors
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.utils.DateRangeUtils
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.mobiata.android.text.StrikethroughTagHandler
import com.squareup.phrase.Phrase
import org.joda.time.DateTime
import org.joda.time.LocalDate
import java.lang.StringBuilder

abstract class BaseDealsCardViewModel(protected val context: Context, protected val leadingHotel: DealsDestination.Hotel, private val currency: String?) {

    companion object {
        private const val DESTINATION_IMAGE_URL_TEMPLATE = "https://a.travel-assets.com/dynamic_images/{regionId}.jpg"
    }

    abstract val numberOfTravelers: Int
    abstract val title: String?
    abstract val subtitle: String?
    abstract val hotelId: String?
    abstract val prioritizedBackgroundImageUrls: List<String?>
    abstract val discountColors: DiscountColors

    val backgroundFallbackResId: Int = R.color.gray600
    val backgroundPlaceHolderResId: Int = R.drawable.results_list_placeholder

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
        val percentSavings = leadingHotel.hotelPricingInfo?.percentSavings?.toInt() ?: 0
        if (percentSavings == 0)
            ""
        else "-$percentSavings%"
    }

    val discountPercent: String by lazy {
        val percentSavings = leadingHotel.hotelPricingInfo?.percentSavings?.toInt() ?: 0
        if (percentSavings == 0)
            ""
        else Phrase.from(context, R.string.hotel_discount_percent_Template)
                .put("discount", percentSavings)
                .format()
                .toString()
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

    open fun getCardContentDescription(): String {
        val result = StringBuilder()

        result.append("$title. ")
        result.append(DateRangeUtils.formatPackageDateRangeContDesc(context, startDate.toString(), endDate.toString()))
        result.append(" ")

        if (percentSavingsText.isNotEmpty()) {
            result.append(Phrase.from(context, R.string.hotel_price_discount_percent_cont_desc_TEMPLATE)
                    .put("percentage", discountPercent)
                    .format()
                    .toString())
        }

        if (strikeOutPriceText.isNotEmpty()) {
            result.append(Phrase.from(context, R.string.hotel_price_strike_through_cont_desc_TEMPLATE)
                    .put("strikethroughprice", strikeOutPriceText)
                    .put("price", priceText)
                    .format()
                    .toString())
        } else {
            result.append(Phrase.from(context, R.string.hotel_card_view_price_cont_desc_TEMPLATE)
                    .put("price", priceText)
                    .format()
                    .toString())
        }

        return result.toString()
    }

    protected fun getDestinationBackgroundImageUrl(): String? {
        if (regionId == null) {
            return null
        }
        return DESTINATION_IMAGE_URL_TEMPLATE.replace("{regionId}", regionId)
    }

    private fun getDateRangeText(startDate: LocalDate?, endDate: LocalDate?): String {
        if (startDate == null || endDate == null) {
            return ""
        }
        val startDateStr = LocaleBasedDateFormatUtils.localDateToEEEMMMd(startDate)
        val endDateStr = LocaleBasedDateFormatUtils.localDateToEEEMMMd(endDate)

        return Phrase.from(context, R.string.start_dash_end_date_range_TEMPLATE)
                .put("startdate", startDateStr).put("enddate", endDateStr)
                .format().toString()
    }

    private fun getDateInLocalDateFormat(intDate: List<Int>?): LocalDate {
        if (intDate == null) {
            return DateTime.now().toLocalDate()
        }

        return LocalDate(intDate[0], intDate[1], intDate[2])
    }

    private fun getFormattedPriceText(resources: Resources, price: Double?, strikeOut: Boolean): CharSequence {
        if (price == null || price == 0.00) {
            return ""
        }
        val money = Money(java.lang.Double.toString(price), currency).getFormattedMoney(Money.F_NO_DECIMAL)
        return if (strikeOut) HtmlCompat.fromHtml(resources.getString(R.string.strike_template, money), null, StrikethroughTagHandler()) else money
    }
}
