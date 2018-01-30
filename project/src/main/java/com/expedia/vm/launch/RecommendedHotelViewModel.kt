package com.expedia.vm.launch

import android.content.Context
import android.content.res.Resources
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.utils.HotelUtils
import com.expedia.bookings.utils.SpannableBuilder
import com.expedia.bookings.utils.StrUtils
import com.mobiata.android.text.StrikethroughTagHandler
import com.squareup.phrase.Phrase
import java.lang.Float

class RecommendedHotelViewModel(val context: Context, val hotel: Hotel) {

    val title: String = hotel.localizedName
    val rating: String = Float.toString(hotel.hotelGuestRating)
    val isDiscountRestrictedToCurrentSourceType: Boolean = hotel.isDiscountRestrictedToCurrentSourceType
    val isSameDayDRR: Boolean = hotel.isSameDayDRR
    val isAirAttached: Boolean = hotel.lowRateInfo.airAttached

    val price: String by lazy {
        formatHotelPrice()
    }

    val discountTenPercentOrBetter: Boolean by lazy {
        HotelUtils.isDiscountTenPercentOrBetter(hotel.lowRateInfo)
    }

    val saleText: String by lazy {
        getDiscountSaleText()
    }

    private val priceContentDesc: CharSequence by lazy {
        getPriceContentDescription()
    }

    val strikeThroughPrice: CharSequence by lazy {
        getStrikethroughPrice(context.resources)
    }

    val hotelContentDesc: CharSequence by lazy {
        getHotelContentDescription()
    }

    private fun formatHotelPrice(): String {
        return StrUtils.formatHotelPrice(Money(Math.round(hotel.lowRateInfo.priceToShowUsers).toString(), hotel.lowRateInfo.currencyCode))
    }

    private fun getPriceContentDescription(): CharSequence {
        val result = SpannableBuilder()
        if (discountTenPercentOrBetter) {
            result.append(Phrase.from(context, R.string.hotel_price_strike_through_cont_desc_TEMPLATE)
                    .put("strikethroughprice", strikeThroughPrice)
                    .put("price", price)
                    .format()
                    .toString())
        } else {
            result.append(price)
        }

        return result.build()
    }

    private fun getDiscountSaleText(): String = context.getString(R.string.percent_off_TEMPLATE,
            HotelUtils.getDiscountPercent(hotel.lowRateInfo))

    private fun getHotelContentDescription(): CharSequence {
        val result = SpannableBuilder()
        result.append(Phrase.from(context, R.string.hotel_details_cont_desc_zero_starrating_TEMPLATE)
                .put("hotel", title)
                .put("guestrating", rating)
                .format().toString())
        if (discountTenPercentOrBetter) {
            result.append(Phrase.from(context, R.string.hotel_price_discount_percent_cont_desc_TEMPLATE).put("percentage", saleText).format().toString())
        }

        result.append(priceContentDesc)
        return result.build()
    }

    private fun getStrikethroughPrice(resources: Resources): CharSequence {
        val formattedMoney = StrUtils.formatHotelPrice(Money(Math.round(hotel.lowRateInfo.strikethroughPriceToShowUsers).toString(), hotel.lowRateInfo.currencyCode))
        return HtmlCompat.fromHtml(resources.getString(R.string.strike_template,
                formattedMoney),
                null,
                StrikethroughTagHandler())
    }
}
