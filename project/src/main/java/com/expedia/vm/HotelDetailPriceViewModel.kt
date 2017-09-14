package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.HotelRate
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.utils.StrUtils
import com.squareup.phrase.Phrase
import java.math.BigDecimal

open class HotelDetailPriceViewModel(context: Context): BaseHotelDetailPriceViewModel(context) {

    override fun getPriceString(): String? {
        if (chargeableRateInfo == null || isSoldOut.value) {
            return null
        }

        val currencyCode = chargeableRateInfo!!.currencyCode
        if (showTotalPrice) {
            val totalPriceWithMandatoryFees = BigDecimal(chargeableRateInfo!!.totalPriceWithMandatoryFees.toDouble())
            return Money(totalPriceWithMandatoryFees, currencyCode).getFormattedMoney(Money.F_NO_DECIMAL)
        } else {
            val averageRate = BigDecimal(chargeableRateInfo!!.averageRate.toDouble())
            return Money(averageRate, currencyCode).getFormattedMoney(Money.F_NO_DECIMAL)
        }
    }

    override fun getPerDescriptorString(): String? {
        if (isSoldOut.value) {
            return null
        }

        val priceType = chargeableRateInfo?.getUserPriceType()
        val bucketedToShowPriceDescriptorProminence = Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelPriceDescriptorProminence)
        if (priceType != null && bucketedToShowPriceDescriptorProminence) {
            return when (priceType) {
                HotelRate.UserPriceType.RATE_FOR_WHOLE_STAY_WITH_TAXES -> context.getString(R.string.total_stay)
                else -> context.getString(R.string.per_night)
            }
        } else if (showTotalPrice) {
            return null
        } else {
            return context.getString(R.string.per_night)
        }
    }

    override fun getSearchInfoString(): String {
        val parsedSearchInfoString = Phrase.from(context, R.string.calendar_instructions_date_range_with_guests_TEMPLATE)
                    .put("startdate", LocaleBasedDateFormatUtils.localDateToMMMd(hotelSearchParams.checkIn))
                    .put("enddate", LocaleBasedDateFormatUtils.localDateToMMMd(hotelSearchParams.checkOut))
                    .put("guests", StrUtils.formatGuestString(context, hotelSearchParams.guests))
                    .format().toString()

        return parsedSearchInfoString
    }

    override fun getPriceContainerContentDescriptionString(): String {
        if (getPriceString() == null || getPerDescriptorString() == null) {
            return ""
        }

        if (getStrikeThroughPriceString() != null) {
            val strikeThroughString = Phrase.from(context, R.string.hotel_price_strike_through_cont_desc_TEMPLATE)
                    .put("strikethroughprice", getStrikeThroughPriceString()!!)
                    .put("price", getPriceString()!!)
                    .format().toString()

            if (!isShopWithPointsRate && chargeableRateInfo?.isDiscountPercentNotZero ?: false) {
                val discountPercentage: Int? = chargeableRateInfo?.discountPercent?.toInt()
                if (discountPercentage != null) {
                    val discountPercentString = Phrase.from(context, R.string.hotel_discount_percent_Template)
                            .put("discount", discountPercentage)
                            .format().toString()
                    val discountString = Phrase.from(context, R.string.hotel_price_discount_percent_cont_desc_TEMPLATE)
                            .put("percentage", discountPercentString)
                            .format().toString()
                    return strikeThroughString + discountString
                }
            }

            return strikeThroughString
        } else {
            return getPriceString()!! + " " + getPerDescriptorString()!!
        }
    }
}
